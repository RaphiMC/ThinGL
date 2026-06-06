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
package net.raphimc.thingl.resource.font.face.impl;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.resource.font.face.FontFace;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.resource.font.instance.impl.FreeTypeFontInstance;
import net.raphimc.thingl.resource.memory.Memory;
import net.raphimc.thingl.text.util.FreeTypeLibrary;
import net.raphimc.thingl.util.MathUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;
import org.lwjgl.util.freetype.TT_OS2;
import org.lwjgl.util.harfbuzz.HarfBuzz;

import java.util.concurrent.Semaphore;

public class FreeTypeFontFace extends FontFace {

    static {
        Capabilities.assertFreeTypeAvailable();
    }

    private final Memory fontData;
    private final boolean freeFontData;
    private final FT_Face face;
    private final float ascender;
    private final float descender;
    private final float height;
    private final float underlinePosition;
    private final float underlineThickness;
    private final Float os2StrikeoutPosition;
    private final Float os2StrikeoutSize;
    private final String postScriptName;
    private final String familyName;
    private final String subFamilyName;
    private final int glyphCount;
    private final Semaphore sizeContextSemaphore = new Semaphore(1);

    public FreeTypeFontFace(final byte[] fontBytes) {
        this(MemoryAllocator.allocateMemory(fontBytes));
    }

    public FreeTypeFontFace(final Memory fontData) {
        this(fontData, true);
    }

    public FreeTypeFontFace(final Memory fontData, final boolean freeFontData) {
        this.fontData = fontData;
        this.freeFontData = freeFontData;
        try {
            try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                final PointerBuffer fontFaceBuffer = memoryStack.mallocPointer(1);
                FreeTypeLibrary.checkError(FreeType.FT_New_Memory_Face(ThinGL.freeTypeLibrary().getPointer(), fontData.asByteBuffer(), 0L, fontFaceBuffer), "Failed to load font face");
                this.face = FT_Face.create(fontFaceBuffer.get(0));
            }
            this.ascender = this.face.ascender() / MathUtil.FIXED_26_6;
            this.descender = this.face.descender() / MathUtil.FIXED_26_6;
            this.height = this.face.height() / MathUtil.FIXED_26_6;
            this.underlinePosition = this.face.underline_position() / MathUtil.FIXED_26_6;
            this.underlineThickness = this.face.underline_thickness() / MathUtil.FIXED_26_6;
            final TT_OS2 os2Table = TT_OS2.createSafe(FreeType.FT_Get_Sfnt_Table(this.face, FreeType.FT_SFNT_OS2));
            if (os2Table != null) {
                this.os2StrikeoutPosition = os2Table.yStrikeoutPosition() / MathUtil.FIXED_26_6;
                this.os2StrikeoutSize = os2Table.yStrikeoutSize() / MathUtil.FIXED_26_6;
            } else {
                this.os2StrikeoutPosition = null;
                this.os2StrikeoutSize = null;
            }
            this.postScriptName = FreeType.FT_Get_Postscript_Name(this.face);
            this.familyName = this.face.family_nameString();
            this.subFamilyName = this.face.style_nameString();
            this.glyphCount = Math.toIntExact(this.face.num_glyphs());
        } catch (Throwable e) {
            this.free();
            throw e;
        }
    }

    public SizeContext acquireSizeContext(final int size) {
        return new SizeContext(size);
    }

    @Override
    public FreeTypeFontInstance getInstance(final int size) {
        return (FreeTypeFontInstance) super.getInstance(size);
    }

    public FT_Face getFtFace() {
        return this.face;
    }

    public float getAscender() {
        return this.ascender;
    }

    public float getDescender() {
        return this.descender;
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

    public Float getOs2StrikeoutPosition() {
        return this.os2StrikeoutPosition;
    }

    public Float getOs2StrikeoutSize() {
        return this.os2StrikeoutSize;
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
    public int getGlyphCount() {
        return this.glyphCount;
    }

    @Override
    protected int loadGlyphIndex(final int codePoint) {
        return FreeType.FT_Get_Char_Index(this.face, codePoint);
    }

    @Override
    protected FontInstance createInstance(final int size) {
        return new FreeTypeFontInstance(this, size);
    }

    @Override
    protected long createHarfBuzzInstance() {
        return HarfBuzz.hb_ft_face_create_referenced(this.face.address());
    }

    @Override
    protected void free0() {
        super.free0();
        if (this.face != null) {
            FreeTypeLibrary.checkError(FreeType.FT_Done_Face(this.face), "Failed to free font face");
        }
        if (this.freeFontData) {
            this.fontData.free();
        }
    }

    public class SizeContext implements AutoCloseable {

        private SizeContext(final int size) {
            try {
                FreeTypeFontFace.this.sizeContextSemaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            this.setPixelSizes(0, size);
        }

        public void setPixelSizes(final int pixelWidth, final int pixelHeight) {
            FreeTypeLibrary.checkError(FreeType.FT_Set_Pixel_Sizes(FreeTypeFontFace.this.face, pixelWidth, pixelHeight), "Failed to set pixel sizes");
        }

        public FT_GlyphSlot loadGlyph(final int glyphIndex, final int loadFlags) {
            FreeTypeLibrary.checkError(FreeType.FT_Load_Glyph(FreeTypeFontFace.this.face, glyphIndex, loadFlags), "Failed to load glyph");
            return FreeTypeFontFace.this.face.glyph();
        }

        public void renderGlyph(final FT_GlyphSlot slot, final int renderMode) {
            FreeTypeLibrary.checkError(FreeType.FT_Render_Glyph(slot, renderMode), "Failed to render glyph");
        }

        public float getMetricsYScale() {
            return FreeTypeFontFace.this.face.size().metrics().y_scale() / MathUtil.FIXED_16_16;
        }

        @Override
        public void close() {
            FreeTypeFontFace.this.sizeContextSemaphore.release();
        }

    }

}
