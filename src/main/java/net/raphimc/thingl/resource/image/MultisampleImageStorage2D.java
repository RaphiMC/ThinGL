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
package net.raphimc.thingl.resource.image;

public interface MultisampleImageStorage2D extends MultisampleImageStorage {

    default void copyTo(final MultisampleImageStorage2D target, final int srcX, final int srcY, final int dstX, final int dstY, final int width, final int height) {
        this.copyTo(target, 0, srcX, srcY, 0, 0, dstX, dstY, 0, width, height, 1);
    }

    default void copyTo(final MultisampleImageStorage3D target, final int srcX, final int srcY, final int dstX, final int dstY, final int dstZ, final int width, final int height) {
        this.copyTo(target, 0, srcX, srcY, 0, 0, dstX, dstY, dstZ, width, height, 1);
    }

}
