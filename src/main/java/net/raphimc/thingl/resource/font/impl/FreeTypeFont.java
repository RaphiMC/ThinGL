/*
 * This file is part of ThinGL - https://github.com/RaphiMC/ThinGL
 * Copyright (C) 2024-2025 RK_01/RaphiMC and contributors
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
package net.raphimc.thingl.resource.font.impl;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.resource.image.impl.ByteImage2D;
import net.raphimc.thingl.resource.memory.Memory;
import net.raphimc.thingl.text.util.FreeTypeLibrary;
import org.joml.Vector2f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.*;
import org.lwjgl.util.harfbuzz.HarfBuzz;

public class FreeTypeFont extends Font {

    static {
        Capabilities.assertFreeTypeAvailable();
    }

    private final Memory fontData;
    private final boolean freeFontData;
    private final FT_Face fontFace;
    private final int glyphLoadFlags;
    private final float ascent;
    private final float descent;
    private final float height;
    private final float underlinePosition;
    private final float underlineThickness;
    private final float strikethroughPosition;
    private final float strikethroughThickness;
    private final String postScriptName;
    private final String familyName;
    private final String subFamilyName;

    public FreeTypeFont(final byte[] fontBytes, final int size) {
        this(fontBytes, size, new Vector2f());
    }

    public FreeTypeFont(final byte[] fontBytes, final int size, final Vector2f shift) {
        this(fontBytes, size, shift, false);
    }

    public FreeTypeFont(final byte[] fontBytes, final int size, final Vector2f shift, final boolean useHinting) {
        this(MemoryAllocator.allocateMemory(fontBytes), size, shift, useHinting);
    }

    public FreeTypeFont(final Memory fontData, final int size) {
        this(fontData, size, new Vector2f());
    }

    public FreeTypeFont(final Memory fontData, final int size, final Vector2f shift) {
        this(fontData, size, shift, false);
    }

    public FreeTypeFont(final Memory fontData, final int size, final Vector2f shift, final boolean useHinting) {
        this(fontData, size, shift, useHinting, true);
    }

    public FreeTypeFont(final Memory fontData, final int size, final Vector2f shift, final boolean useHinting, final boolean freeFontData) {
        super(size);
        this.fontData = fontData;
        this.freeFontData = freeFontData;
        try {
            try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                final PointerBuffer fontFaceBuffer = memoryStack.mallocPointer(1);
                FreeTypeLibrary.checkError(FreeType.FT_New_Memory_Face(ThinGL.freeTypeLibrary().getPointer(), fontData.asByteBuffer(), 0L, fontFaceBuffer), "Failed to load font face");
                this.fontFace = FT_Face.create(fontFaceBuffer.get(0));

                FreeTypeLibrary.checkError(FreeType.FT_Set_Pixel_Sizes(this.fontFace, 0, size), "Failed to set font size");
                FreeType.FT_Set_Transform(this.fontFace, null, FT_Vector.malloc(memoryStack).set(Math.round(shift.x * 64F), Math.round(-shift.y * 64F)));
            }
            this.glyphLoadFlags = FreeType.FT_LOAD_DEFAULT | (useHinting ? 0 : FreeType.FT_LOAD_NO_HINTING);

            final long yScale = this.fontFace.size().metrics().y_scale();
            this.ascent = FreeType.FT_MulFix(this.fontFace.ascender(), yScale) / 64F;
            this.descent = -FreeType.FT_MulFix(this.fontFace.descender(), yScale) / 64F;
            this.height = FreeType.FT_MulFix(this.fontFace.height(), yScale) / 64F;
            this.underlinePosition = -FreeType.FT_MulFix(this.fontFace.underline_position(), yScale) / 64F;
            this.underlineThickness = FreeType.FT_MulFix(this.fontFace.underline_thickness(), yScale) / 64F;
            this.postScriptName = FreeType.FT_Get_Postscript_Name(this.fontFace);
            this.familyName = this.fontFace.family_nameString();
            this.subFamilyName = this.fontFace.style_nameString();

            final TT_OS2 os2Table = TT_OS2.createSafe(FreeType.FT_Get_Sfnt_Table(this.fontFace, FreeType.FT_SFNT_OS2));
            if (os2Table != null) {
                this.strikethroughThickness = FreeType.FT_MulFix(os2Table.yStrikeoutSize(), yScale) / 64F;
                this.strikethroughPosition = -FreeType.FT_MulFix(os2Table.yStrikeoutPosition(), yScale) / 64F + this.strikethroughThickness / 2F;
            } else {
                this.strikethroughPosition = this.ascent * -0.3F;
                this.strikethroughThickness = this.underlineThickness;
            }
        } catch (Throwable e) {
            this.free();
            throw e;
        }
    }

    @Override
    public GlyphBitmap createGlyphBitmap(final Glyph glyph, final GlyphBitmap.RenderMode renderMode) {
        int loadFlags = this.glyphLoadFlags;
        if (renderMode == GlyphBitmap.RenderMode.PIXELATED || renderMode == GlyphBitmap.RenderMode.COLORED_PIXELATED) {
            loadFlags |= FreeType.FT_FT_LOAD_TARGET_MONO;
        }
        if (renderMode == GlyphBitmap.RenderMode.COLORED_PIXELATED || renderMode == GlyphBitmap.RenderMode.COLORED_ANTIALIASED) {
            loadFlags |= FreeType.FT_LOAD_COLOR;
        }
        FreeTypeLibrary.checkError(FreeType.FT_Load_Glyph(this.fontFace, glyph.glyphIndex(), loadFlags), "Failed to load glyph");
        final FT_GlyphSlot glyphSlot = this.fontFace.glyph();
        switch (renderMode) {
            case PIXELATED, COLORED_PIXELATED -> FreeTypeLibrary.checkError(FreeType.FT_Render_Glyph(glyphSlot, FreeType.FT_RENDER_MODE_MONO), "Failed to render glyph");
            case ANTIALIASED, COLORED_ANTIALIASED -> FreeTypeLibrary.checkError(FreeType.FT_Render_Glyph(glyphSlot, FreeType.FT_RENDER_MODE_NORMAL), "Failed to render glyph");
            case BSDF -> {
                FreeTypeLibrary.checkError(FreeType.FT_Render_Glyph(glyphSlot, FreeType.FT_RENDER_MODE_NORMAL), "Failed to render glyph");
                FreeTypeLibrary.checkError(FreeType.FT_Render_Glyph(glyphSlot, FreeType.FT_RENDER_MODE_SDF), "Failed to render glyph");
            }
            case SDF -> FreeTypeLibrary.checkError(FreeType.FT_Render_Glyph(glyphSlot, FreeType.FT_RENDER_MODE_SDF), "Failed to render glyph");
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
                        FreeTypeLibrary.checkError(FreeType.FT_Bitmap_Convert(ThinGL.freeTypeLibrary().getPointer(), bitmap, convertedBitmap, 1), "Failed to convert bitmap to grayscale");
                        if (convertedBitmap.pitch() != convertedBitmap.width()) {
                            throw new IllegalStateException("Bitmap is not tightly packed");
                        }
                        final Memory pixels = MemoryAllocator.wrapMemory(convertedBitmap.buffer(convertedBitmap.width() * convertedBitmap.rows()));
                        for (long i = 0; i < pixels.getSize(); i++) {
                            pixels.putByte(i, (byte) (pixels.getByte(i) * 255));
                        }
                        yield new ByteImage2D(convertedBitmap.width(), convertedBitmap.rows(), GL11C.GL_RED, MemoryAllocator.copyMemory(pixels));
                    } finally {
                        FreeTypeLibrary.checkError(FreeType.FT_Bitmap_Done(ThinGL.freeTypeLibrary().getPointer(), convertedBitmap), "Failed to free converted bitmap");
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

    public FT_Face getFontFace() {
        return this.fontFace;
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
    public String getPostScriptName() {
        return this.postScriptName;
    }

    @Override
    public String getFamilyName() {
        return this.familyName;
    }

    @Override
    public String getSubFamilyName() {
        return this.subFamilyName;
    }

    @Override
    protected Glyph loadGlyphByIndex(final int glyphIndex) {
        FreeTypeLibrary.checkError(FreeType.FT_Load_Glyph(this.fontFace, glyphIndex, this.glyphLoadFlags | FreeType.FT_LOAD_NO_BITMAP), "Failed to load glyph");
        final FT_GlyphSlot glyphSlot = this.fontFace.glyph();

        final FT_Glyph_Metrics metrics = glyphSlot.metrics();
        final float width = metrics.width() / 64F;
        final float height = metrics.height() / 64F;
        final float xAdvance = metrics.horiAdvance() / 64F;
        final float bearingX = metrics.horiBearingX() / 64F;
        final float bearingY = -metrics.horiBearingY() / 64F;
        return new Glyph(this, glyphIndex, width, height, xAdvance, bearingX, bearingY);
    }

    @Override
    protected Glyph loadGlyphByCodePoint(final int codePoint) {
        return this.getGlyphByIndex(FreeType.FT_Get_Char_Index(this.fontFace, codePoint));
    }

    @Override
    protected long createHarfBuzzInstance() {
        final long harfBuzzInstance = HarfBuzz.hb_ft_font_create_referenced(this.fontFace.address());
        HarfBuzz.hb_ft_font_set_load_flags(harfBuzzInstance, this.glyphLoadFlags | FreeType.FT_LOAD_NO_BITMAP);
        return harfBuzzInstance;
    }

    @Override
    protected void free0() {
        super.free0();
        if (this.fontFace != null) {
            FreeTypeLibrary.checkError(FreeType.FT_Done_Face(this.fontFace), "Failed to free font face");
        }
        if (this.freeFontData) {
            this.fontData.free();
        }
    }

}
