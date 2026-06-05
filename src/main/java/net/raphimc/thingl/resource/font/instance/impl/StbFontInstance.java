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

import net.raphimc.thingl.gl.text.SDFTextRenderer;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.resource.font.face.impl.StbFontFace;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.resource.image.impl.ByteImage2D;
import net.raphimc.thingl.resource.memory.Memory;
import org.joml.primitives.Rectanglei;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.stb.STBTruetype;

public class StbFontInstance extends FontInstance {

    static {
        Capabilities.assertStbAvailable();
    }

    private final float scale;

    public StbFontInstance(final StbFontFace face, final int size) {
        super(face, size);
        this.scale = face.getScaleForMappingEmToPixels(size);
    }

    @Override
    public GlyphBitmap createGlyphBitmap(final int glyphIndex, final GlyphBitmap.RenderMode renderMode) {
        final int[] width = new int[1];
        final int[] height = new int[1];
        final int[] xOffset = new int[1];
        final int[] yOffset = new int[1];
        final Memory pixels = MemoryAllocator.wrapMemory(switch (renderMode) {
            case PIXELATED, COLORED_PIXELATED, ANTIALIASED, COLORED_ANTIALIASED -> STBTruetype.stbtt_GetGlyphBitmap(this.getFace().getFontInfo(), this.scale, this.scale, glyphIndex, width, height, xOffset, yOffset);
            case SDF -> {
                final int padding = SDFTextRenderer.DF_PX_RANGE;
                final int onEdgeValue = 128;
                final int pixelDistScale = Math.round((float) onEdgeValue / (float) padding);
                yield STBTruetype.stbtt_GetGlyphSDF(this.getFace().getFontInfo(), this.scale, glyphIndex, (byte) padding, (byte) onEdgeValue, (byte) pixelDistScale, width, height, xOffset, yOffset);
            }
            default -> throw new IllegalArgumentException("Unsupported render mode: " + renderMode);
        });
        if (pixels == null) {
            return null;
        }
        try {
            ByteImage2D image = new ByteImage2D(width[0], height[0], GL11C.GL_RED, MemoryAllocator.copyMemory(pixels));
            if (renderMode == GlyphBitmap.RenderMode.PIXELATED || renderMode == GlyphBitmap.RenderMode.COLORED_PIXELATED) {
                image.thresholdGrayscale(127);
            }
            if (renderMode == GlyphBitmap.RenderMode.COLORED_PIXELATED || renderMode == GlyphBitmap.RenderMode.COLORED_ANTIALIASED) {
                image = image.withPixelFormat(GL11C.GL_ALPHA).convertGrayscaleToColor();
            }
            return new GlyphBitmap(image, xOffset[0], yOffset[0]);
        } finally {
            STBTruetype.nstbtt_FreeBitmap(pixels.getAddress(), 0L);
        }
    }

    @Override
    public StbFontFace getFace() {
        return (StbFontFace) super.getFace();
    }

    @Override
    public float getAscent() {
        return this.getFace().getAscent() * this.scale;
    }

    @Override
    public float getDescent() {
        return -this.getFace().getDescent() * this.scale;
    }

    @Override
    public float getHeight() {
        return (this.getFace().getAscent() - this.getFace().getDescent() + this.getFace().getLineGap()) * this.scale;
    }

    @Override
    public float getUnderlinePosition() {
        return this.getUnderlineThickness();
    }

    @Override
    public float getUnderlineThickness() {
        return this.getSize() * 0.05F;
    }

    @Override
    public float getStrikethroughPosition() {
        return this.getAscent() * -0.3F;
    }

    @Override
    public float getStrikethroughThickness() {
        return this.getUnderlineThickness();
    }

    @Override
    protected GlyphMetrics loadGlyphMetrics(final int glyphIndex) {
        final StbFontFace.GlyphHMetrics hMetrics = this.getFace().getGlyphHMetrics(glyphIndex);
        final Rectanglei box = this.getFace().getGlyphBox(glyphIndex);
        return new GlyphMetrics(
                box.lengthX() * this.scale,
                box.lengthY() * this.scale,
                hMetrics.advanceWidth() * this.scale,
                box.minX * this.scale,
                -box.maxY * this.scale
        );
    }

}
