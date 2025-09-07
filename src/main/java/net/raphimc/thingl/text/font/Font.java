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
package net.raphimc.thingl.text.font;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.text.FreeTypeLibrary;
import net.raphimc.thingl.util.BufferUtil;
import net.raphimc.thingl.util.ImageUtil;
import org.joml.Vector2f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.*;
import org.lwjgl.util.harfbuzz.HarfBuzz;

import java.nio.ByteBuffer;

public class Font {

    private final ByteBuffer fontDataBuffer;
    private final FT_Face fontFace;
    private final int size;
    private final float ascent;
    private final float descent;
    private final float height;
    private final float underlinePosition;
    private final float underlineThickness;
    private final float strikethroughPosition;
    private final float strikethroughThickness;
    private final String postscriptName;
    private final String family;
    private final String style;
    private final Int2ObjectMap<Glyph> indexToGlyph = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Glyph> codePointToGlyph = new Int2ObjectOpenHashMap<>();
    private final Glyph[] asciiCodePointToGlyph = new Glyph[256];
    private long harfBuzzInstance = 0L;

    public Font(final byte[] fontData, final int size) {
        this(fontData, size, new Vector2f());
    }

    public Font(final byte[] fontData, final int size, final Vector2f shift) {
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
            this.size = size;

            final long yScale = this.fontFace.size().metrics().y_scale();
            this.ascent = FreeType.FT_MulFix(this.fontFace.ascender(), yScale) / 64F;
            this.descent = -FreeType.FT_MulFix(this.fontFace.descender(), yScale) / 64F;
            this.height = FreeType.FT_MulFix(this.fontFace.height(), yScale) / 64F;
            this.underlinePosition = -FreeType.FT_MulFix(this.fontFace.underline_position(), yScale) / 64F;
            this.underlineThickness = FreeType.FT_MulFix(this.fontFace.underline_thickness(), yScale) / 64F;
            this.postscriptName = FreeType.FT_Get_Postscript_Name(this.fontFace);
            this.family = this.fontFace.family_nameString();
            this.style = this.fontFace.style_nameString();

            final TT_OS2 os2Table = TT_OS2.createSafe(FreeType.FT_Get_Sfnt_Table(this.fontFace, FreeType.FT_SFNT_OS2));
            if (os2Table != null) {
                this.strikethroughPosition = -FreeType.FT_MulFix(os2Table.yStrikeoutPosition(), yScale) / 64F;
                this.strikethroughThickness = FreeType.FT_MulFix(os2Table.yStrikeoutSize(), yScale) / 64F;
            } else {
                this.strikethroughPosition = this.size * -0.3F;
                this.strikethroughThickness = this.underlineThickness;
            }
        } catch (Throwable e) {
            this.free();
            throw e;
        }
    }

    public Glyph getGlyphByIndex(final int glyphIndex) {
        return this.indexToGlyph.computeIfAbsent(glyphIndex, this::loadGlyphByIndex);
    }

    public Glyph getGlyphByCodePoint(final int codePoint) {
        if (codePoint >= 0 && codePoint < this.asciiCodePointToGlyph.length) {
            final Glyph glyph = this.asciiCodePointToGlyph[codePoint];
            if (glyph != null) {
                return glyph;
            } else {
                return this.asciiCodePointToGlyph[codePoint] = this.loadGlyphByCodePoint(codePoint);
            }
        } else {
            return this.codePointToGlyph.computeIfAbsent(codePoint, this::loadGlyphByCodePoint);
        }
    }

    public GlyphBitmap createGlyphBitmap(final int glyphIndex, final GlyphBitmap.RenderMode renderMode) {
        int loadFlags = FreeType.FT_LOAD_DEFAULT;
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
                    MemoryUtil.memFree(grayscaleBuffer);
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

    public void free() {
        if (this.harfBuzzInstance != 0L) {
            HarfBuzz.hb_font_destroy(this.harfBuzzInstance);
        }
        if (this.fontFace != null) {
            FreeTypeLibrary.checkError(FreeType.FT_Done_Face(this.fontFace), "Failed to free font face");
        }
        BufferUtil.memFree(this.fontDataBuffer);
    }

    public int getSize() {
        return this.size;
    }

    public FT_Face getFontFace() {
        return this.fontFace;
    }

    public float getAscent() {
        return this.ascent;
    }

    public float getDescent() {
        return this.descent;
    }

    public float getHeight() {
        return this.height;
    }

    public float getUnderlinePosition() {
        return this.underlinePosition;
    }

    public float getUnderlineThickness() {
        return this.underlineThickness;
    }

    public float getStrikethroughPosition() {
        return this.strikethroughPosition;
    }

    public float getStrikethroughThickness() {
        return this.strikethroughThickness;
    }

    public String getPostscriptName() {
        return this.postscriptName;
    }

    public String getFamily() {
        return this.family;
    }

    public String getStyle() {
        return this.style;
    }

    public long getHarfBuzzInstance() {
        if (this.harfBuzzInstance == 0L) {
            ThinGL.capabilities().ensureHarfBuzzPresent();
            this.harfBuzzInstance = HarfBuzz.hb_ft_font_create_referenced(this.fontFace.address());
            HarfBuzz.hb_ft_font_set_load_flags(this.harfBuzzInstance, FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_NO_BITMAP);
        }
        return this.harfBuzzInstance;
    }

    private Glyph loadGlyphByIndex(final int glyphIndex) {
        FreeTypeLibrary.checkError(FreeType.FT_Load_Glyph(this.fontFace, glyphIndex, FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_NO_BITMAP), "Failed to load glyph");
        final FT_GlyphSlot glyphSlot = this.fontFace.glyph();

        final FT_Glyph_Metrics metrics = glyphSlot.metrics();
        final float width = metrics.width() / 64F;
        final float height = metrics.height() / 64F;
        final float xAdvance = metrics.horiAdvance() / 64F;
        final float bearingX = metrics.horiBearingX() / 64F;
        final float bearingY = -metrics.horiBearingY() / 64F;
        return new Glyph(this, glyphIndex, width, height, xAdvance, bearingX, bearingY);
    }

    private Glyph loadGlyphByCodePoint(final int codePoint) {
        return this.getGlyphByIndex(FreeType.FT_Get_Char_Index(this.fontFace, codePoint));
    }

    public record Glyph(Font font, int glyphIndex, float width, float height, float xAdvance, float bearingX, float bearingY) {
    }

    public record GlyphBitmap(int width, int height, int xOffset, int yOffset, int pixelFormat, ByteBuffer pixelBuffer) {

        public enum RenderMode {

            PIXELATED(GL30C.GL_R8, GL11C.GL_NEAREST),
            COLORED_PIXELATED(GL11C.GL_RGBA8, GL11C.GL_NEAREST),
            ANTIALIASED(GL30C.GL_R8, GL11C.GL_LINEAR),
            COLORED_ANTIALIASED(GL11C.GL_RGBA8, GL11C.GL_LINEAR),
            BSDF(GL30C.GL_R8, GL11C.GL_LINEAR),
            SDF(GL30C.GL_R8, GL11C.GL_LINEAR),
            ;

            private final int textureFormat;
            private final int textureFilter;

            RenderMode(final int textureFormat, final int textureFilter) {
                this.textureFormat = textureFormat;
                this.textureFilter = textureFilter;
            }

            public int getTextureFormat() {
                return this.textureFormat;
            }

            public int getTextureFilter() {
                return this.textureFilter;
            }

        }

    }

}
