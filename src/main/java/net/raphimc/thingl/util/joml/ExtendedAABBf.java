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
package net.raphimc.thingl.util.joml;

import org.joml.Vector3fc;
import org.joml.primitives.AABBf;

public class ExtendedAABBf extends AABBf {

    public ExtendedAABBf() {
    }

    public ExtendedAABBf(final AABBf source) {
        super(source);
    }

    public ExtendedAABBf(final Vector3fc min, final Vector3fc max) {
        super(min, max);
    }

    public ExtendedAABBf(final float minX, final float minY, final float minZ, final float maxX, final float maxY, final float maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public ExtendedAABBf(final Vector3fc min) {
        this(min.x(), min.y(), min.z());
    }

    public ExtendedAABBf(final float minX, final float minY, final float minZ) {
        super(minX, minY, minZ, minX + 1, minY + 1, minZ + 1);
    }

    public ExtendedAABBf expand(final float x, final float y, final float z) {
        this.minX -= x;
        this.minY -= y;
        this.minZ -= z;
        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
        return this;
    }

}
