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
package net.raphimc.thingl.text.font.impl;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.text.FreeTypeLibrary;
import net.raphimc.thingl.text.font.Font;
import net.raphimc.thingl.util.BufferUtil;
import net.raphimc.thingl.util.ImageUtil;
import org.joml.Vector2f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.*;
import org.lwjgl.util.harfbuzz.HarfBuzz;

import java.nio.ByteBuffer;

public class FreeTypeFont extends Font {

    private final ByteBuffer fontDataBuffer;
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
    private final String styleName;

    public FreeTypeFont(final byte[] fontData, final int size) {
        this(fontData, size, new Vector2f());
    }

    public FreeTypeFont(final byte[] fontData, final int size, final Vector2f shift) {
        this(fontData, size, shift, false);
    }

    public FreeTypeFont(final byte[] fontData, final int size, final Vector2f shift, final boolean useHinting) {
        super(size);
        ThinGL.capabilities().ensureFreeTypePresent();
        this.fontDataBuffer = MemoryUtil.memAlloc(fontData.length).put(fontData).flip();
        try {
            try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                final PointerBuffer fontFaceBuffer = memoryStack.mallocPointer(1);
                FreeTypeLibrary.checkError(FreeType.FT_New_Memory_Face(ThinGL.freeTypeLibrary().getPointer(), this.fontDataBuffer, 0L, fontFaceBuffer), "Failed to load font face");
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
            this.styleName = this.fontFace.style_nameString();

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
    public GlyphBitmap createGlyphBitmap(final int glyphIndex, final GlyphBitmap.RenderMode renderMode) {
        int loadFlags = this.glyphLoadFlags;
        if (renderMode == GlyphBitmap.RenderMode.PIXELATED || renderMode == GlyphBitmap.RenderMode.COLORED_PIXELATED) {
            loadFlags |= FreeType.FT_FT_LOAD_TARGET_MONO;
        }
        if (renderMode == GlyphBitmap.RenderMode.COLORED_PIXELATED || renderMode == GlyphBitmap.RenderMode.COLORED_ANTIALIASED) {
            loadFlags |= FreeType.FT_LOAD_COLOR;
        }
        FreeTypeLibrary.checkError(FreeType.FT_Load_Glyph(this.fontFace, glyphIndex, loadFlags), "Failed to load glyph");
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
        final int pixelMode = bitmap.pixel_mode();
        final int pitch = bitmap.pitch();
        final int width = bitmap.width();
        final int rows = bitmap.rows();
        if (pixelMode == FreeType.FT_PIXEL_MODE_NONE || width <= 0 || rows <= 0) {
            return null;
        }
        final ByteBuffer buffer = bitmap.buffer(Math.abs(pitch) * rows);
        if (buffer == null) {
            return null;
        }

        final IntObjectPair<ByteBuffer> pixelData = switch (pixelMode) {
            case FreeType.FT_PIXEL_MODE_MONO -> switch (renderMode) {
                case PIXELATED -> IntObjectPair.of(GL11C.GL_RED, ImageUtil.convertMonochromeToGrayscale(buffer, width, rows, pitch));
                case COLORED_PIXELATED -> {
                    final ByteBuffer grayscaleBuffer = ImageUtil.convertMonochromeToGrayscale(buffer, width, rows, pitch);
                    final ByteBuffer argbBuffer = ImageUtil.convertGrayscaleToARGB(grayscaleBuffer, width, rows);
                    BufferUtil.memFree(grayscaleBuffer);
                    yield IntObjectPair.of(GL12C.GL_BGRA, argbBuffer);
                }
                default -> throw new IllegalStateException("Unsupported render mode for monochrome glyph: " + renderMode);
            };
            case FreeType.FT_PIXEL_MODE_GRAY -> switch (renderMode) {
                case ANTIALIASED, BSDF, SDF -> {
                    if (pitch == width) {
                        yield IntObjectPair.of(GL11C.GL_RED, BufferUtil.createCopy(buffer));
                    } else {
                        throw new IllegalStateException("Unsupported pitch: " + pitch + " (width: " + width + ")");
                    }
                }
                case COLORED_ANTIALIASED -> IntObjectPair.of(GL12C.GL_BGRA, ImageUtil.convertGrayscaleToARGB(buffer, width, rows, pitch));
                default -> throw new IllegalStateException("Unsupported render mode for grayscale glyph: " + renderMode);
            };
            case FreeType.FT_PIXEL_MODE_BGRA -> switch (renderMode) {
                case COLORED_PIXELATED, COLORED_ANTIALIASED -> {
                    if (pitch == width * 4) {
                        yield IntObjectPair.of(GL12C.GL_BGRA, BufferUtil.createCopy(buffer));
                    } else {
                        throw new IllegalStateException("Unsupported pitch: " + pitch + " (width: " + width + ")");
                    }
                }
                default -> throw new IllegalStateException("Unsupported render mode for color glyph: " + renderMode);
            };
            default -> throw new IllegalStateException("Unsupported pixel mode: " + pixelMode);
        };

        final int xOffset = glyphSlot.bitmap_left();
        final int yOffset = -glyphSlot.bitmap_top();
        return new GlyphBitmap(width, rows, xOffset, yOffset, pixelData.leftInt(), pixelData.right());
    }

    @Override
    public void free() {
        super.free();
        if (this.fontFace != null) {
            FreeTypeLibrary.checkError(FreeType.FT_Done_Face(this.fontFace), "Failed to free font face");
        }
        BufferUtil.memFree(this.fontDataBuffer);
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

    public String getStyleName() {
        return this.styleName;
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

}
