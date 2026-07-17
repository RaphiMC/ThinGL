/*
 * This file is part of ThinGL - https://github.com/RaphiMC/ThinGL
 * Copyright (C) 2024-2026 RK_01/RaphiMC and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.thingl.resource.font.instance.impl;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.resource.font.face.impl.FreeTypeFontFace;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.resource.image.impl.ByteImage2D;
import net.raphimc.thingl.resource.memory.Memory;
import net.raphimc.thingl.text.util.freetype.FreeTypeException;
import net.raphimc.thingl.util.MathUtil;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_Glyph_Metrics;
import org.lwjgl.util.freetype.FreeType;

public class FreeTypeFontInstance extends FontInstance {

    static {
        Capabilities.assertFreeTypeAvailable();
    }

    private final float ascent;
    private final float descent;
    private final float height;
    private final float underlinePosition;
    private final float underlineThickness;
    private final float strikethroughPosition;
    private final float strikethroughThickness;

    public FreeTypeFontInstance(final FreeTypeFontFace face, final int size) {
        super(face, size);
        try (FreeTypeFontFace.SizeContext ctx = face.acquireSizeContext(size)) {
            final float yScale = ctx.getMetricsYScale();
            this.ascent = face.getAscender() * yScale;
            this.descent = -face.getDescender() * yScale;
            this.height = face.getHeight() * yScale;
            this.underlinePosition = -face.getUnderlinePosition() * yScale;
            this.underlineThickness = face.getUnderlineThickness() * yScale;
            if (face.getOs2StrikeoutPosition() != null && face.getOs2StrikeoutSize() != null) {
                this.strikethroughThickness = face.getOs2StrikeoutSize() * yScale;
                this.strikethroughPosition = -face.getOs2StrikeoutPosition() * yScale + this.strikethroughThickness / 2F;
            } else {
                this.strikethroughPosition = this.ascent * -0.3F;
                this.strikethroughThickness = this.underlineThickness;
            }
        }
    }

    @Override
    public GlyphBitmap createGlyphBitmap(final int glyphIndex, final GlyphBitmap.RenderMode renderMode) {
        int loadFlags = FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_NO_HINTING;
        if (renderMode == GlyphBitmap.RenderMode.PIXELATED || renderMode == GlyphBitmap.RenderMode.COLORED_PIXELATED) {
            loadFlags |= FreeType.FT_FT_LOAD_TARGET_MONO;
        }
        if (renderMode == GlyphBitmap.RenderMode.COLORED_PIXELATED || renderMode == GlyphBitmap.RenderMode.COLORED_ANTIALIASED) {
            loadFlags |= FreeType.FT_LOAD_COLOR;
        }
        try (FreeTypeFontFace.SizeContext ctx = this.getFace().acquireSizeContext(this.getSize())) {
            final FT_GlyphSlot glyphSlot = ctx.loadGlyph(glyphIndex, loadFlags);
            switch (renderMode) {
                case PIXELATED, COLORED_PIXELATED -> ctx.renderGlyph(glyphSlot, FreeType.FT_RENDER_MODE_MONO);
                case ANTIALIASED, COLORED_ANTIALIASED -> ctx.renderGlyph(glyphSlot, FreeType.FT_RENDER_MODE_NORMAL);
                case BSDF -> {
                    ctx.renderGlyph(glyphSlot, FreeType.FT_RENDER_MODE_NORMAL);
                    ctx.renderGlyph(glyphSlot, FreeType.FT_RENDER_MODE_SDF);
                }
                case SDF -> ctx.renderGlyph(glyphSlot, FreeType.FT_RENDER_MODE_SDF);
                default -> throw new IllegalArgumentException("Unsupported render mode: " + renderMode);
            }

            final FT_Bitmap bitmap = glyphSlot.bitmap();
            if (bitmap.width() <= 0 || bitmap.rows() <= 0) {
                return null;
            }
            if (bitmap.pitch() < 0) {
                throw new IllegalStateException("Negative pitch is not supported");
            }
            ByteImage2D image = switch (bitmap.pixel_mode()) {
                case FreeType.FT_PIXEL_MODE_NONE -> null;
                case FreeType.FT_PIXEL_MODE_MONO -> {
                    try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                        final FT_Bitmap convertedBitmap = FT_Bitmap.calloc(memoryStack);
                        FreeType.FT_Bitmap_Init(convertedBitmap);
                        try {
                            FreeTypeException.check(FreeType.FT_Bitmap_Convert(ThinGL.freeTypeLibrary().getPointer(), bitmap, convertedBitmap, 1), "Failed to convert bitmap to grayscale");
                            if (convertedBitmap.pitch() != convertedBitmap.width()) {
                                throw new IllegalStateException("Bitmap is not tightly packed");
                            }
                            final Memory pixels = MemoryAllocator.wrapMemory(convertedBitmap.buffer(convertedBitmap.width() * convertedBitmap.rows()));
                            for (long i = 0; i < pixels.getSize(); i++) {
                                pixels.putByte(i, (byte) (pixels.getByte(i) * 255));
                            }
                            yield new ByteImage2D(convertedBitmap.width(), convertedBitmap.rows(), GL11C.GL_RED, MemoryAllocator.copyMemory(pixels));
                        } finally {
                            FreeTypeException.check(FreeType.FT_Bitmap_Done(ThinGL.freeTypeLibrary().getPointer(), convertedBitmap), "Failed to free converted bitmap");
                        }
                    }
                }
                case FreeType.FT_PIXEL_MODE_GRAY -> {
                    if (bitmap.pitch() != bitmap.width()) {
                        throw new IllegalStateException("Bitmap is not tightly packed");
                    }
                    final Memory pixels = MemoryAllocator.wrapMemory(bitmap.buffer(bitmap.width() * bitmap.rows()));
                    yield new ByteImage2D(bitmap.width(), bitmap.rows(), GL11C.GL_RED, pixels, false);
                }
                case FreeType.FT_PIXEL_MODE_BGRA -> {
                    if (bitmap.pitch() != bitmap.width() * Integer.BYTES) {
                        throw new IllegalStateException("Bitmap is not tightly packed");
                    }
                    final Memory pixels = MemoryAllocator.wrapMemory(bitmap.buffer(bitmap.width() * bitmap.rows()));
                    final ByteImage2D glyphImage = new ByteImage2D(bitmap.width(), bitmap.rows(), GL12C.GL_BGRA, pixels, false);
                    glyphImage.unpremultiplyAlpha();
                    yield glyphImage;
                }
                default -> throw new IllegalStateException("Unsupported pixel mode: " + bitmap.pixel_mode());
            };
            if (image == null) {
                return null;
            }
            if ((renderMode == GlyphBitmap.RenderMode.COLORED_PIXELATED || renderMode == GlyphBitmap.RenderMode.COLORED_ANTIALIASED) && image.getChannels() < 3) {
                image = image.withPixelFormat(GL11C.GL_ALPHA).convertGrayscaleToColor();
            }
            if ((renderMode == GlyphBitmap.RenderMode.PIXELATED || renderMode == GlyphBitmap.RenderMode.ANTIALIASED) && image.getChannels() > 2) {
                image = image.convertColorToGrayscale();
            }
            return new GlyphBitmap(image, glyphSlot.bitmap_left(), -glyphSlot.bitmap_top());
        }
    }

    @Override
    public FreeTypeFontFace getFace() {
        return (FreeTypeFontFace) super.getFace();
    }

    @Override
    public float getAscent() {
        return this.ascent;
    }

    @Override
    public float getDescent() {
        return this.descent;
    }

    @Override
    public float getHeight() {
        return this.height;
    }

    @Override
    public float getUnderlinePosition() {
        return this.underlinePosition;
    }

    @Override
    public float getUnderlineThickness() {
        return this.underlineThickness;
    }

    @Override
    public float getStrikethroughPosition() {
        return this.strikethroughPosition;
    }

    @Override
    public float getStrikethroughThickness() {
        return this.strikethroughThickness;
    }

    @Override
    protected GlyphMetrics loadGlyphMetrics(final int glyphIndex) {
        try (FreeTypeFontFace.SizeContext ctx = this.getFace().acquireSizeContext(this.getSize())) {
            final FT_Glyph_Metrics metrics = ctx.loadGlyph(glyphIndex, FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_NO_HINTING).metrics();
            final float width = metrics.width() / MathUtil.FIXED_26_6;
            final float height = metrics.height() / MathUtil.FIXED_26_6;
            final float xAdvance = metrics.horiAdvance() / MathUtil.FIXED_26_6;
            final float bearingX = metrics.horiBearingX() / MathUtil.FIXED_26_6;
            final float bearingY = -metrics.horiBearingY() / MathUtil.FIXED_26_6;
            return new GlyphMetrics(width, height, xAdvance, bearingX, bearingY);
        }
    }

}
