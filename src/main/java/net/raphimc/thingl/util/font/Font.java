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

package net.raphimc.thingl.util.font;

import org.joml.Vector2f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.*;

import java.nio.ByteBuffer;

public class Font {

    private final ByteBuffer fontDataBuffer;
    private final FT_Face fontFace;
    private final int size;
    private final GlyphPredicate glyphPredicate;
    private final float ascent;
    private final float descent;
    private final float paddedHeight;
    private final float lineThickness;
    private final String family;
    private final String style;

    public Font(final byte[] fontData, final int size) {
        this(fontData, size, new Vector2f(), GlyphPredicate.all());
    }

    public Font(final byte[] fontData, final int size, final Vector2f shift, final GlyphPredicate glyphPredicate) {
        this.fontDataBuffer = MemoryUtil.memAlloc(fontData.length).put(fontData).flip();
        try {
            try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                final PointerBuffer fontFaceBuffer = memoryStack.mallocPointer(1);
                FreeTypeInstance.checkError(FreeType.FT_New_Memory_Face(FreeTypeInstance.get(), this.fontDataBuffer, 0L, fontFaceBuffer), "Failed to load font face");
                this.fontFace = FT_Face.create(fontFaceBuffer.get());

                FreeTypeInstance.checkError(FreeType.FT_Set_Pixel_Sizes(this.fontFace, 0, size), "Failed to set font size");
                FreeType.FT_Set_Transform(this.fontFace, null, FT_Vector.malloc(memoryStack).set(Math.round(shift.x * 64F), Math.round(-shift.y * 64F)));
            }
            this.size = size;
            this.glyphPredicate = glyphPredicate;

            this.ascent = this.fontFace.size().metrics().ascender() / 64F;
            this.descent = this.fontFace.size().metrics().descender() / 64F;
            this.paddedHeight = this.fontFace.size().metrics().height() / 64F;
            this.lineThickness = Math.max(this.size / 16F, 1F);
            this.family = this.fontFace.family_nameString();
            this.style = this.fontFace.style_nameString();
        } catch (Throwable e) {
            this.delete();
            throw e;
        }
    }

    public FontGlyph loadGlyphByCodePoint(final int codePoint) {
        if (this.glyphPredicate.test(codePoint)) {
            FreeTypeInstance.checkError(FreeType.FT_Load_Char(this.fontFace, codePoint, FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_NO_BITMAP), "Failed to load glyph");
        } else {
            FreeTypeInstance.checkError(FreeType.FT_Load_Glyph(this.fontFace, 0, FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_NO_BITMAP), "Failed to load glyph");
        }
        final FT_GlyphSlot glyphSlot = this.fontFace.glyph();

        final FT_Glyph_Metrics metrics = glyphSlot.metrics();
        final int glyphIndex = glyphSlot.glyph_index();
        final float width = metrics.width() / 64F;
        final float height = metrics.height() / 64F;
        final float advance = metrics.horiAdvance() / 64F;
        final float bearingX = metrics.horiBearingX() / 64F;
        final float bearingY = metrics.horiBearingY() / 64F;
        return new FontGlyph(this, glyphIndex, width, height, advance, bearingX, bearingY);
    }

    public GlyphBitmap loadGlyphBitmap(final int glyphIndex, final int renderMode) {
        FreeTypeInstance.checkError(FreeType.FT_Load_Glyph(this.fontFace, glyphIndex, FreeType.FT_LOAD_DEFAULT), "Failed to load glyph");
        final FT_GlyphSlot glyphSlot = this.fontFace.glyph();

        FreeTypeInstance.checkError(FreeType.FT_Render_Glyph(glyphSlot, renderMode), "Failed to render glyph");
        final FT_Bitmap bitmap = glyphSlot.bitmap();
        if (bitmap.pixel_mode() != FreeType.FT_PIXEL_MODE_GRAY) {
            throw new IllegalStateException("Unsupported pixel mode: " + bitmap.pixel_mode());
        }

        final int width = bitmap.width();
        final int height = bitmap.rows();
        final int xOffset = glyphSlot.bitmap_left();
        final int yOffset = -glyphSlot.bitmap_top();
        final ByteBuffer pixels = bitmap.buffer(width * height);
        return new GlyphBitmap(pixels, width, height, xOffset, yOffset);
    }

    public void delete() {
        if (this.fontFace != null) {
            FreeTypeInstance.checkError(FreeType.FT_Done_Face(this.fontFace), "Failed to delete font face");
        }
        MemoryUtil.memFree(this.fontDataBuffer);
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

    public float getPaddedHeight() {
        return this.paddedHeight;
    }

    public float getLineThickness() {
        return this.lineThickness;
    }

    public String getFamily() {
        return this.family;
    }

    public String getStyle() {
        return this.style;
    }

}
