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

import org.joml.primitives.Rectanglef;

import java.util.List;

public record ShapedTextLine(List<ShapedTextRun> runs, Rectanglef visualBounds, Rectanglef logicalBounds) {

    public ShapedTextLine(final List<ShapedTextRun> runs) {
        this(runs, new Rectanglef(), new Rectanglef());
        this.calculateBounds();
    }

    public void calculateBounds() {
        if (this.runs.isEmpty()) {
            this.visualBounds.setMin(0F, 0F).setMax(0F, 0F);
            this.logicalBounds.setMin(0F, 0F).setMax(0F, 0F);
            return;
        }

        this.visualBounds.setMin(Float.MAX_VALUE, Float.MAX_VALUE).setMax(-Float.MAX_VALUE, -Float.MAX_VALUE);
        this.logicalBounds.setMin(Float.MAX_VALUE, Float.MAX_VALUE).setMax(-Float.MAX_VALUE, -Float.MAX_VALUE);
        float x = 0F;
        for (int i = 0; i < this.runs.size(); i++) {
            final ShapedTextRun run = this.runs.get(i);
            if (i == 0) {
                this.visualBounds.union(run.visualBounds());
                this.logicalBounds.union(run.logicalBounds());
                x += run.visualBounds().minX;
            } else {
                this.visualBounds.union(run.visualBounds().translate(x - run.visualBounds().minX, 0F, new Rectanglef()));
                this.logicalBounds.union(run.logicalBounds().translate(x - run.visualBounds().minX, 0F, new Rectanglef()));
            }
            x += run.logicalBounds().maxX;
        }
    }

}
