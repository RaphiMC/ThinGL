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

import org.joml.Vector3ic;
import org.joml.primitives.AABBi;
import org.joml.primitives.AABBic;

public class ExtendedAABBi extends AABBi {

    public ExtendedAABBi() {
    }

    public ExtendedAABBi(final AABBic source) {
        super(source);
    }

    public ExtendedAABBi(final Vector3ic min, final Vector3ic max) {
        super(min, max);
    }

    public ExtendedAABBi(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public ExtendedAABBi(final Vector3ic min) {
        this(min.x(), min.y(), min.z());
    }

    public ExtendedAABBi(final int minX, final int minY, final int minZ) {
        super(minX, minY, minZ, minX + 1, minY + 1, minZ + 1);
    }

    public ExtendedAABBi expand(final int x, final int y, final int z) {
        this.minX -= x;
        this.minY -= y;
        this.minZ -= z;
        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
        return this;
    }

}
