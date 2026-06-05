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
package net.raphimc.thingl.resource.font.instance;

public class ScaledFontInstance extends FontInstance {

    private final FontInstance baseInstance;
    private final float scale;

    ScaledFontInstance(final FontInstance baseInstance, final int size) {
        super(baseInstance.getFace(), size);
        this.baseInstance = baseInstance;
        this.scale = (float) size / baseInstance.getSize();
    }

    @Override
    public GlyphBitmap createGlyphBitmap(final int glyphIndex, final GlyphBitmap.RenderMode renderMode) {
        throw new UnsupportedOperationException("Scaled instance does not support creating glyph bitmaps directly. Create a glyph bitmap from the base instance and scale it manually.");
    }

    public FontInstance getBaseInstance() {
        return this.baseInstance;
    }

    public float getScale() {
        return this.scale;
    }

    @Override
    public float getAscent() {
        return this.baseInstance.getAscent() * this.scale;
    }

    @Override
    public float getDescent() {
        return this.baseInstance.getDescent() * this.scale;
    }

    @Override
    public float getHeight() {
        return this.baseInstance.getHeight() * this.scale;
    }

    @Override
    public float getUnderlinePosition() {
        return this.baseInstance.getUnderlinePosition() * this.scale;
    }

    @Override
    public float getUnderlineThickness() {
        return this.baseInstance.getUnderlineThickness() * this.scale;
    }

    @Override
    public float getStrikethroughPosition() {
        return this.baseInstance.getStrikethroughPosition() * this.scale;
    }

    @Override
    public float getStrikethroughThickness() {
        return this.baseInstance.getStrikethroughThickness() * this.scale;
    }

    @Override
    protected GlyphMetrics loadGlyphMetrics(final int glyphIndex) {
        final GlyphMetrics baseMetrics = this.baseInstance.getGlyphMetrics(glyphIndex);
        return new GlyphMetrics(
                baseMetrics.width() * this.scale,
                baseMetrics.height() * this.scale,
                baseMetrics.xAdvance() * this.scale,
                baseMetrics.bearingX() * this.scale,
                baseMetrics.bearingY() * this.scale
        );
    }

}
