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

package net.raphimc.thingl.drawbuilder.builder.command;

import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;

public record DrawElementsCommand(int vertexCount, int instanceCount, int firstIndex, int baseVertex, int baseInstance) implements DrawCommand {

    public DrawElementsCommand(final int vertexCount, final int instanceCount) {
        this(vertexCount, instanceCount, 0, 0, 0);
    }

    @Override
    public DrawCommand withVertexOffset(final int vertexOffset) {
        return new DrawElementsCommand(this.vertexCount, this.instanceCount, this.firstIndex, this.baseVertex + vertexOffset, this.baseInstance);
    }

    public DrawCommand withIndexOffset(final int indexOffset) {
        return new DrawElementsCommand(this.vertexCount, this.instanceCount, this.firstIndex + indexOffset, this.baseVertex, this.baseInstance);
    }

    @Override
    public void write(final BufferBuilder bufferBuilder) {
        bufferBuilder.putInt(this.vertexCount).putInt(this.instanceCount).putInt(this.firstIndex).putInt(this.baseVertex).putInt(this.baseInstance);
    }

}
