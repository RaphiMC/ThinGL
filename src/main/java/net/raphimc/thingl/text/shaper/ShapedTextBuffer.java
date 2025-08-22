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

import org.joml.primitives.Rectanglef;

import java.util.List;

public record ShapedTextBuffer(List<ShapedTextRun> runs, Rectanglef bounds, Rectanglef fontBounds) {

    public ShapedTextBuffer(final List<ShapedTextRun> runs) {
        this(runs, new Rectanglef(), new Rectanglef());
        this.calculateBounds();
    }

    public void calculateBounds() {
        if (this.runs.isEmpty()) {
            this.bounds.setMin(0F, 0F).setMax(0F, 0F);
            this.fontBounds.setMin(0F, 0F).setMax(0F, 0F);
            return;
        }

        this.bounds.setMin(Float.MAX_VALUE, Float.MAX_VALUE).setMax(-Float.MAX_VALUE, -Float.MAX_VALUE);
        this.fontBounds.setMin(Float.MAX_VALUE, Float.MAX_VALUE).setMax(-Float.MAX_VALUE, -Float.MAX_VALUE);
        float x = 0F;
        float y = 0F;
        for (ShapedTextRun run : this.runs) {
            x += run.xOffset();
            y += run.yOffset();
            this.bounds.union(run.bounds().translate(x, y, new Rectanglef()));
            this.fontBounds.union(run.fontBounds().translate(x, y, new Rectanglef()));
            x += run.nextRunX();
            y += run.nextRunY();
        }
    }

}
