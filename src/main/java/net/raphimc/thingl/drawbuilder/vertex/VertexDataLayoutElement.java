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

package net.raphimc.thingl.drawbuilder.vertex;

public record VertexDataLayoutElement(DataType type, int count, boolean normalized, boolean isInteger, int padding) {

    public VertexDataLayoutElement(final DataType type, final int count) {
        this(type, count, false, type.isInteger());
    }

    public VertexDataLayoutElement(final DataType type, final int count, final boolean normalized) {
        this(type, count, normalized, type.isInteger() && !normalized);
    }

    public VertexDataLayoutElement(final DataType type, final int count, final boolean normalized, final boolean isInteger) {
        this(type, count, normalized, isInteger, 0);
    }

}
