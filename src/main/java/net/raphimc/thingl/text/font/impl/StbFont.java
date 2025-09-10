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
import net.raphimc.thingl.text.font.Font;
import net.raphimc.thingl.text.renderer.SDFTextRenderer;
import net.raphimc.thingl.util.ImageUtil;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.harfbuzz.HarfBuzz;

import java.nio.ByteBuffer;

public class StbFont extends Font {

    private final ByteBuffer fontDataBuffer;
    private final STBTTFontinfo fontInfo;
    private final float scale;
    private final float ascent;
    private final float descent;
    private final float height;
    private final float lineThickness;
    private final float underlinePosition;
    private final float strikethroughPosition;

    public StbFont(final byte[] fontData, final int size) {
        super(size);
        this.fontDataBuffer = MemoryUtil.memAlloc(fontData.length).put(fontData).flip();
        this.fontInfo = STBTTFontinfo.calloc();
        try {
            if (!STBTruetype.stbtt_InitFont(this.fontInfo, this.fontDataBuffer)) {
                throw new IllegalStateException("Failed to load font");
            }
            this.scale = STBTruetype.stbtt_ScaleForMappingEmToPixels(this.fontInfo, size);

            final int[] ascent = new int[1];
            final int[] descent = new int[1];
            final int[] lineGap = new int[1];
            STBTruetype.stbtt_GetFontVMetrics(this.fontInfo, ascent, descent, lineGap);
            this.ascent = ascent[0] * this.scale;
            this.descent = -descent[0] * this.scale;
            this.height = this.ascent + this.descent + lineGap[0] * this.scale;

            this.lineThickness = size * 0.05F;
            this.underlinePosition = this.lineThickness;
            this.strikethroughPosition = this.ascent * -0.3F;
        } catch (Throwable e) {
            this.free();
            throw e;
        }
    }

    @Override
    public GlyphBitmap createGlyphBitmap(final Glyph glyph, final GlyphBitmap.RenderMode renderMode) {
        final int[] width = new int[1];
        final int[] height = new int[1];
        final int[] xOffset = new int[1];
        final int[] yOffset = new int[1];
        final ByteBuffer buffer = switch (renderMode) {
            case PIXELATED, COLORED_PIXELATED, ANTIALIASED, COLORED_ANTIALIASED -> STBTruetype.stbtt_GetGlyphBitmap(this.fontInfo, 0F, this.scale, glyph.glyphIndex(), width, height, xOffset, yOffset);
            case SDF -> {
                final int padding = SDFTextRenderer.DF_PX_RANGE;
                final int onEdgeValue = 128;
                final int pixelDistScale = Math.round((float) onEdgeValue / (float) padding);
                yield STBTruetype.stbtt_GetGlyphSDF(this.fontInfo, this.scale, glyph.glyphIndex(), (byte) padding, (byte) onEdgeValue, (byte) pixelDistScale, width, height, xOffset, yOffset);
            }
            default -> throw new IllegalArgumentException("Unsupported render mode: " + renderMode);
        };
        if (buffer == null) {
            return null;
        }

        final IntObjectPair<ByteBuffer> pixelData = switch (renderMode) {
            case PIXELATED -> IntObjectPair.of(GL11C.GL_RED, ImageUtil.thresholdGrayscale(buffer, 127));
            case COLORED_PIXELATED -> {
                final ByteBuffer thresholded = ImageUtil.thresholdGrayscale(buffer, 127);
                yield IntObjectPair.of(GL12C.GL_BGRA, ImageUtil.convertGrayscaleToColor(thresholded, width[0], height[0], 3));
            }
            case ANTIALIASED, SDF -> IntObjectPair.of(GL11C.GL_RED, buffer);
            case COLORED_ANTIALIASED -> IntObjectPair.of(GL12C.GL_BGRA, ImageUtil.convertGrayscaleToColor(buffer, width[0], height[0], 3));
            default -> throw new IllegalArgumentException("Unsupported render mode: " + renderMode);
        };

        return new GlyphBitmap(width[0], height[0], xOffset[0], yOffset[0], pixelData.leftInt(), pixelData.right());
    }

    @Override
    public void free() {
        super.free();
        this.fontInfo.free();
        MemoryUtil.memFree(this.fontDataBuffer);
    }

    public STBTTFontinfo getFontInfo() {
        return this.fontInfo;
    }

    public float getScale() {
        return this.scale;
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
        return this.lineThickness;
    }

    @Override
    public float getStrikethroughPosition() {
        return this.strikethroughPosition;
    }

    @Override
    public float getStrikethroughThickness() {
        return this.lineThickness;
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
    protected Glyph loadGlyphByIndex(final int glyphIndex) {
        final int[] advanceWidth = new int[1];
        STBTruetype.stbtt_GetGlyphHMetrics(this.fontInfo, glyphIndex, advanceWidth, null);

        final int[] x0 = new int[1];
        final int[] y0 = new int[1];
        final int[] x1 = new int[1];
        final int[] y1 = new int[1];
        STBTruetype.stbtt_GetGlyphBox(this.fontInfo, glyphIndex, x0, y0, x1, y1);

        final float width = (x1[0] - x0[0]) * this.scale;
        final float height = (y1[0] - y0[0]) * this.scale;
        final float xAdvance = advanceWidth[0] * this.scale;
        final float bearingX = x0[0] * this.scale;
        final float bearingY = -y1[0] * this.scale;
        return new Glyph(this, glyphIndex, width, height, xAdvance, bearingX, bearingY);
    }

    @Override
    protected Glyph loadGlyphByCodePoint(final int codePoint) {
        return this.getGlyphByIndex(STBTruetype.stbtt_FindGlyphIndex(this.fontInfo, codePoint));
    }

    @Override
    protected long createHarfBuzzInstance() {
        final long hbBlob = HarfBuzz.hb_blob_create(this.fontDataBuffer, HarfBuzz.HB_MEMORY_MODE_READONLY, 0L, null);
        if (hbBlob == 0L) {
            throw new IllegalStateException("Failed to create HarfBuzz blob");
        }
        final long hbFace = HarfBuzz.hb_face_create(hbBlob, 0);
        HarfBuzz.hb_blob_destroy(hbBlob);
        if (hbFace == 0L) {
            throw new IllegalStateException("Failed to create HarfBuzz face");
        }
        final long hbFont = HarfBuzz.hb_font_create(hbFace);
        HarfBuzz.hb_face_destroy(hbFace);
        if (hbFont == 0L) {
            throw new IllegalStateException("Failed to create HarfBuzz font");
        }
        return hbFont;
    }

}
