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
package net.raphimc.thingl.text.shaper;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.text.TextSegment;
import net.raphimc.thingl.text.font.Font;
import net.raphimc.thingl.text.renderer.TextRenderer;
import org.joml.primitives.Rectanglef;

import java.util.List;

public record ShapedTextSegment(List<TextShaper.Glyph> glyphs, Color color, int styleFlags, Color outlineColor, float xVisualOffset, float yVisualOffset, Rectanglef bounds, Rectanglef extendedBounds) {

    public ShapedTextSegment(final List<TextShaper.Glyph> glyphs, final Color color, final int styleFlags, final Color outlineColor, float xVisualOffset, float yVisualOffset) {
        this(glyphs, color, styleFlags, outlineColor, xVisualOffset, yVisualOffset, new Rectanglef(), new Rectanglef());
        this.calculateBounds();
    }

    public void calculateBounds() {
        if (this.glyphs.isEmpty()) {
            this.bounds.setMin(0F, 0F).setMax(0F, 0F);
            this.extendedBounds.setMin(0F, 0F).setMax(0F, 0F);
            return;
        }

        this.bounds.setMin(Float.MAX_VALUE, Float.MAX_VALUE).setMax(-Float.MAX_VALUE, -Float.MAX_VALUE);
        for (TextShaper.Glyph shapedGlyph : this.glyphs) {
            final Font.Glyph fontGlyph = shapedGlyph.fontGlyph();
            final float minX = shapedGlyph.x() + fontGlyph.bearingX();
            final float minY = shapedGlyph.y() + fontGlyph.bearingY();
            final float maxX = minX + fontGlyph.width();
            final float maxY = minY + fontGlyph.height();
            if (minX < this.bounds.minX) {
                this.bounds.minX = minX;
            }
            if (minY < this.bounds.minY) {
                this.bounds.minY = minY;
            }
            if (maxX > this.bounds.maxX) {
                this.bounds.maxX = maxX;
            }
            if (maxY > this.bounds.maxY) {
                this.bounds.maxY = maxY;
            }
        }

        if ((this.styleFlags & TextSegment.STYLE_SHADOW_BIT) != 0) {
            final float shadowOffset = TextRenderer.SHADOW_OFFSET_FACTOR * this.glyphs.get(this.glyphs.size() - 1).fontGlyph().font().getSize();
            this.bounds.maxX += shadowOffset;
            this.bounds.maxY += shadowOffset;
        }
        if ((this.styleFlags & TextSegment.STYLE_BOLD_BIT) != 0 || this.outlineColor.getAlpha() > 0) {
            final float boldOffset = this.glyphs.get(0).fontGlyph().font().getSize() / TextRenderer.BOLD_OFFSET_DIVIDER;
            this.bounds.minX -= boldOffset;
            this.bounds.minY -= boldOffset;
            this.bounds.maxX += boldOffset;
            this.bounds.maxY += boldOffset;
        }
        if ((this.styleFlags & TextSegment.STYLE_ITALIC_BIT) != 0) {
            this.bounds.maxX += TextRenderer.ITALIC_SHEAR_FACTOR * -this.glyphs.get(this.glyphs.size() - 1).fontGlyph().bearingY();
        }

        this.extendedBounds.set(this.bounds);
        if ((this.styleFlags & TextSegment.STYLE_UNDERLINE_BIT) != 0 || (this.styleFlags & TextSegment.STYLE_STRIKETHROUGH_BIT) != 0) {
            for (TextShaper.Glyph shapedGlyph : this.glyphs) {
                final Font.Glyph fontGlyph = shapedGlyph.fontGlyph();
                final float minX = shapedGlyph.x();
                final float maxX = minX + fontGlyph.bearingX() + fontGlyph.xAdvance();
                if (minX < this.extendedBounds.minX) {
                    this.extendedBounds.minX = minX;
                }
                if (maxX > this.extendedBounds.maxX) {
                    this.extendedBounds.maxX = maxX;
                }
            }
        }
    }

}
