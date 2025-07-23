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

public class VertexDataLayout {

    private final VertexDataLayoutElement[] elements;
    private final int size;
    private final int unpaddedSize;

    public VertexDataLayout(final VertexDataLayoutElement... elements) {
        this.elements = elements;
        int size = 0;
        int unpaddedSize = 0;
        for (VertexDataLayoutElement element : elements) {
            final int exactSize = element.count() * element.dataType().getSize();
            size += exactSize + element.padding();
            unpaddedSize += exactSize;
        }
        this.size = size;
        this.unpaddedSize = unpaddedSize;
    }

    public VertexDataLayoutElement[] getElements() {
        return this.elements;
    }

    public int getSize() {
        return this.size;
    }

    public int getUnpaddedSize() {
        return this.unpaddedSize;
    }

}
