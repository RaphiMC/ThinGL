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
package net.raphimc.thingl.util.rectpack;

import net.lenni0451.commons.math.MathUtils;

public class GridRectanglePacker {

    private final int rectsPerRow;
    private final int rectWidth;
    private final int rectHeight;
    private final int paddedRectWidth;
    private final int paddedRectHeight;
    private final int width;
    private final int height;

    public GridRectanglePacker(final int rectCount, final int rectWidth, final int rectHeight) {
        this.rectsPerRow = MathUtils.ceilInt(Math.sqrt(rectCount));
        this.rectWidth = rectWidth;
        this.rectHeight = rectHeight;
        this.paddedRectWidth = rectWidth + 1;
        this.paddedRectHeight = rectHeight + 1;
        this.width = this.rectsPerRow * this.paddedRectWidth;
        this.height = this.rectsPerRow * this.paddedRectHeight;
    }

    public Slot getSlot(final int index) {
        final int x = (index % this.rectsPerRow) * this.paddedRectWidth;
        final int y = (index / this.rectsPerRow) * this.paddedRectHeight;
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            return null;
        }

        final float u1 = x / (float) this.width;
        final float v1 = y / (float) this.height;
        final float u2 = (x + this.rectWidth) / (float) this.width;
        final float v2 = (y + this.rectHeight) / (float) this.height;
        return new Slot(x, y, this.rectWidth, this.rectHeight, u1, v1, u2, v2);
    }

    public int getRectWidth() {
        return this.rectWidth;
    }

    public int getRectHeight() {
        return this.rectHeight;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

}
