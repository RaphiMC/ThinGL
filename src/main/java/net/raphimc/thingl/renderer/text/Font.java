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

package net.raphimc.thingl.renderer.text;

import net.raphimc.thingl.util.FreeTypeInstance;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;

import java.nio.ByteBuffer;

public class Font {

    private final int size;
    private final float ascent;
    private final float descent;
    private final float baseLineHeight;
    private final float paddedHeight;
    private final float boundingHeight;
    private final float lineThickness;
    private final String family;
    private final String style;
    private final ByteBuffer fontDataBuffer;
    private final FT_Face fontFace;

    public Font(final byte[] fontData, final int size) {
        this.size = size;
        this.fontDataBuffer = MemoryUtil.memAlloc(fontData.length).put(fontData).flip();
        try {
            try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                final PointerBuffer fontFaceBuffer = memoryStack.mallocPointer(1);
                FreeTypeInstance.checkError(FreeType.FT_New_Memory_Face(FreeTypeInstance.get(), this.fontDataBuffer, 0L, fontFaceBuffer), "Failed to load font face");
                this.fontFace = FT_Face.create(fontFaceBuffer.get());
            }

            FreeTypeInstance.checkError(FreeType.FT_Set_Pixel_Sizes(this.fontFace, 0, this.size), "Failed to set font size");

            final float sizeMultiplier = (float) this.size / this.fontFace.units_per_EM();

            this.ascent = this.fontFace.size().metrics().ascender() / 64F;
            this.descent = this.fontFace.size().metrics().descender() / 64F;
            this.baseLineHeight = this.size - this.ascent - this.descent;
            this.paddedHeight = this.fontFace.size().metrics().height() / 64F;
            this.boundingHeight = (this.fontFace.bbox().yMax() + this.fontFace.bbox().yMin()) * sizeMultiplier;
            this.lineThickness = Math.max(this.size / 16F, 1F);
            this.family = this.fontFace.family_nameString();
            this.style = this.fontFace.style_nameString();
        } catch (Throwable e) {
            this.delete();
            throw e;
        }
    }

    public int getGlyphIndex(final int codePoint) {
        return FreeType.FT_Get_Char_Index(this.fontFace, codePoint);
    }

    public FontGlyph getGlyphByCodePoint(final int codePoint) {
        FreeTypeInstance.checkError(FreeType.FT_Load_Char(this.fontFace, codePoint, FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_NO_BITMAP), "Failed to load glyph");
        return this.getLoadedGlyph();
    }

    public FontGlyph getGlyphByIndex(final int glyphIndex) {
        FreeTypeInstance.checkError(FreeType.FT_Load_Glyph(this.fontFace, glyphIndex, FreeType.FT_LOAD_DEFAULT | FreeType.FT_LOAD_NO_BITMAP), "Failed to load glyph");
        return this.getLoadedGlyph();
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

    public float getAscent() {
        return this.ascent;
    }

    public float getDescent() {
        return this.descent;
    }

    public float getBaseLineHeight() {
        return this.baseLineHeight;
    }

    public float getPaddedHeight() {
        return this.paddedHeight;
    }

    public float getBoundingHeight() {
        return this.boundingHeight;
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

    public FT_Face getFontFace() {
        return this.fontFace;
    }

    private FontGlyph getLoadedGlyph() {
        final FT_GlyphSlot glyphSlot = this.fontFace.glyph();
        final int glyphIndex = glyphSlot.glyph_index();
        final float advance = glyphSlot.advance().x() / 64F;
        final float bearingX = glyphSlot.metrics().horiBearingX() / 64F;
        return new FontGlyph(this, glyphIndex, advance, bearingX);
    }

}
