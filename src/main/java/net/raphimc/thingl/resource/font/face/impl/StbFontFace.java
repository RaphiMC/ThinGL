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

import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.resource.font.face.FontFace;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.resource.font.instance.impl.StbFontInstance;
import net.raphimc.thingl.resource.memory.Memory;
import org.joml.primitives.Rectanglei;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.util.harfbuzz.HarfBuzz;

public class StbFontFace extends FontFace {

    static {
        Capabilities.assertStbAvailable();
    }

    private final Memory fontData;
    private final boolean freeFontData;
    private final STBTTFontinfo fontInfo;
    private final int ascent;
    private final int descent;
    private final int lineGap;
    private final int glyphCount;

    public StbFontFace(final byte[] fontBytes) {
        this(MemoryAllocator.allocateMemory(fontBytes));
    }

    public StbFontFace(final Memory fontData) {
        this(fontData, true);
    }

    public StbFontFace(final Memory fontData, final boolean freeFontData) {
        this.fontData = fontData;
        this.freeFontData = freeFontData;
        try {
            this.fontInfo = STBTTFontinfo.calloc();
            if (!STBTruetype.stbtt_InitFont(this.fontInfo, fontData.asByteBuffer())) {
                throw new IllegalStateException("Failed to load font");
            }
            this.glyphCount = MemoryUtil.memGetInt(this.fontInfo.address() + Pointer.POINTER_SIZE + Pointer.POINTER_SIZE + Integer.BYTES);

            final int[] ascent = new int[1];
            final int[] descent = new int[1];
            final int[] lineGap = new int[1];
            STBTruetype.stbtt_GetFontVMetrics(this.fontInfo, ascent, descent, lineGap);
            this.ascent = ascent[0];
            this.descent = descent[0];
            this.lineGap = lineGap[0];
        } catch (Throwable e) {
            this.free();
            throw e;
        }
    }

    public float getScaleForMappingEmToPixels(final float pixels) {
        return STBTruetype.stbtt_ScaleForMappingEmToPixels(this.fontInfo, pixels);
    }

    public GlyphHMetrics getGlyphHMetrics(final int glyphIndex) {
        final int[] advanceWidth = new int[1];
        final int[] leftSideBearing = new int[1];
        STBTruetype.stbtt_GetGlyphHMetrics(this.fontInfo, glyphIndex, advanceWidth, leftSideBearing);
        return new GlyphHMetrics(advanceWidth[0], leftSideBearing[0]);
    }

    public Rectanglei getGlyphBox(final int glyphIndex) {
        final int[] x0 = new int[1];
        final int[] y0 = new int[1];
        final int[] x1 = new int[1];
        final int[] y1 = new int[1];
        STBTruetype.stbtt_GetGlyphBox(this.fontInfo, glyphIndex, x0, y0, x1, y1);
        return new Rectanglei(x0[0], y0[0], x1[0], y1[0]);
    }

    @Override
    public StbFontInstance getInstance(final int size) {
        return (StbFontInstance) super.getInstance(size);
    }

    public STBTTFontinfo getFontInfo() {
        return this.fontInfo;
    }

    public int getAscent() {
        return this.ascent;
    }

    public int getDescent() {
        return this.descent;
    }

    public int getLineGap() {
        return this.lineGap;
    }

    @Override
    public String getPostScriptName() {
        return null;
    }

    @Override
    public String getFamilyName() {
        return null;
    }

    @Override
    public String getSubFamilyName() {
        return null;
    }

    @Override
    public int getGlyphCount() {
        return this.glyphCount;
    }

    @Override
    protected int loadGlyphIndex(final int codePoint) {
        return STBTruetype.stbtt_FindGlyphIndex(this.fontInfo, codePoint);
    }

    @Override
    protected FontInstance createInstance(final int size) {
        return new StbFontInstance(this, size);
    }

    @Override
    protected long createHarfBuzzInstance() {
        final long hbBlob = HarfBuzz.hb_blob_create_or_fail(this.fontData.asByteBuffer(), HarfBuzz.HB_MEMORY_MODE_READONLY, 0L, null);
        if (hbBlob == 0L) {
            throw new IllegalStateException("Failed to create HarfBuzz blob");
        }
        final long hbFace = HarfBuzz.hb_face_create_or_fail(hbBlob, 0);
        HarfBuzz.hb_blob_destroy(hbBlob);
        if (hbFace == 0L) {
            throw new IllegalStateException("Failed to create HarfBuzz face");
        }
        return hbFace;
    }

    @Override
    protected void free0() {
        super.free0();
        if (this.fontInfo != null) {
            this.fontInfo.free();
        }
        if (this.freeFontData) {
            this.fontData.free();
        }
    }

    public record GlyphHMetrics(int advanceWidth, int leftSideBearing) {
    }

}
