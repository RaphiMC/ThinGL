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

import net.raphimc.thingl.text.font.Font;
import org.joml.primitives.Rectanglef;

import java.util.List;

public record ShapedTextRun(Font font, List<ShapedTextSegment> segments, Rectanglef bounds, Rectanglef fontBounds, float xOffset, float yOffset, float nextRunX, float nextRunY) {

    public ShapedTextRun(final Font font, final List<ShapedTextSegment> segments, final float xOffset, final float yOffset, final float nextRunX, final float nextRunY) {
        this(font, segments, new Rectanglef(), new Rectanglef(), xOffset, yOffset, nextRunX, nextRunY);
        this.calculateBounds();
    }

    public void calculateBounds() {
        if (this.segments.isEmpty()) {
            this.bounds.setMin(0F, 0F).setMax(0F, 0F);
            this.fontBounds.setMin(0F, 0F).setMax(0F, 0F);
            return;
        }

        this.bounds.setMin(Float.MAX_VALUE, Float.MAX_VALUE).setMax(-Float.MAX_VALUE, -Float.MAX_VALUE);
        for (ShapedTextSegment textSegment : this.segments) {
            bounds.union(textSegment.bounds());
        }
        this.fontBounds.setMin(this.bounds.minX, -this.font.getAscent()).setMax(this.bounds.maxX, this.font.getDescent());
    }

}
