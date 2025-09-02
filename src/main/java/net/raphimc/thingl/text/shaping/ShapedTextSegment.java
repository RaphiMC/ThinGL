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
package net.raphimc.thingl.text.shaping;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.text.TextSegment;
import net.raphimc.thingl.text.font.Font;
import net.raphimc.thingl.text.renderer.TextRenderer;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;

import java.util.List;

public record ShapedTextSegment(List<TextShaper.Glyph> glyphs, Color color, int styleFlags, Color outlineColor, Vector2f visualOffset, Rectanglef visualBounds, Rectanglef logicalBounds) {

    public ShapedTextSegment(final List<TextShaper.Glyph> glyphs, final Color color, final int styleFlags, final Color outlineColor, final Vector2f visualOffset) {
        this(glyphs, color, styleFlags, outlineColor, visualOffset, new Rectanglef(), new Rectanglef());
        this.calculateBounds();
    }

    public void calculateBounds() {
        if (this.glyphs.isEmpty()) {
            this.visualBounds.setMin(0F, 0F).setMax(0F, 0F);
            this.logicalBounds.setMin(0F, 0F).setMax(0F, 0F);
            return;
        }

        final Font font = this.glyphs.get(0).fontGlyph().font();
        this.visualBounds.setMin(Float.MAX_VALUE, Float.MAX_VALUE).setMax(-Float.MAX_VALUE, -Float.MAX_VALUE);
        this.logicalBounds.setMin(Float.MAX_VALUE, -font.getAscent()).setMax(-Float.MAX_VALUE, font.getDescent());
        for (TextShaper.Glyph shapedGlyph : this.glyphs) {
            final Font.Glyph fontGlyph = shapedGlyph.fontGlyph();
            { // Visual bounds
                final float minX = shapedGlyph.x() + fontGlyph.bearingX();
                final float minY = shapedGlyph.y() + fontGlyph.bearingY();
                final float maxX = minX + fontGlyph.width();
                final float maxY = minY + fontGlyph.height();
                if (minX < this.visualBounds.minX) {
                    this.visualBounds.minX = minX;
                }
                if (minY < this.visualBounds.minY) {
                    this.visualBounds.minY = minY;
                }
                if (maxX > this.visualBounds.maxX) {
                    this.visualBounds.maxX = maxX;
                }
                if (maxY > this.visualBounds.maxY) {
                    this.visualBounds.maxY = maxY;
                }
            }
            { // Logical bounds
                final float minX = shapedGlyph.x();
                final float maxX = minX + fontGlyph.xAdvance();
                if (minX < this.logicalBounds.minX) {
                    this.logicalBounds.minX = minX;
                }
                if (maxX > this.logicalBounds.maxX) {
                    this.logicalBounds.maxX = maxX;
                }
            }
        }

        if ((this.styleFlags & TextSegment.STYLE_SHADOW_BIT) != 0) {
            final float shadowOffset = TextRenderer.SHADOW_OFFSET_FACTOR * this.glyphs.get(this.glyphs.size() - 1).fontGlyph().font().getSize();
            this.visualBounds.maxX += shadowOffset;
            this.visualBounds.maxY += shadowOffset;
        }
        if ((this.styleFlags & TextSegment.STYLE_BOLD_BIT) != 0 || this.outlineColor.getAlpha() > 0) {
            final float boldOffset = this.glyphs.get(0).fontGlyph().font().getSize() / TextRenderer.BOLD_OFFSET_DIVIDER;
            this.visualBounds.minX -= boldOffset;
            this.visualBounds.minY -= boldOffset;
            this.visualBounds.maxX += boldOffset;
            this.visualBounds.maxY += boldOffset;
        }
        if ((this.styleFlags & TextSegment.STYLE_ITALIC_BIT) != 0) {
            this.visualBounds.maxX += TextRenderer.ITALIC_SHEAR_FACTOR * -this.glyphs.get(this.glyphs.size() - 1).fontGlyph().bearingY();
        }
    }

}
