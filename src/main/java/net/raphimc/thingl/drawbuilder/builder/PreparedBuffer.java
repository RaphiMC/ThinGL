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
package net.raphimc.thingl.drawbuilder.builder;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.DrawBatch;
import net.raphimc.thingl.drawbuilder.builder.command.DrawCommand;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.DrawBatchDataHolder;
import net.raphimc.thingl.drawbuilder.index.IndexByteBuffer;
import net.raphimc.thingl.util.BufferUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public record PreparedBuffer(DrawBatch drawBatch, DrawBatchDataHolder drawBatchDataHolder, ByteBuffer vertexBuffer, ByteBuffer instanceVertexBuffer, IndexByteBuffer indexBuffer, Map<String, ByteBuffer> shaderDataBuffers, List<DrawCommand> drawCommands) {

    public void free() {
        if (!this.drawBatchDataHolder.hasIndexDataHolder() && this.indexBuffer != null) {
            final ByteBuffer indexByteBuffer = this.indexBuffer.buffer();
            if (indexByteBuffer != ThinGL.quadIndexBuffer().getSharedData()) {
                BufferUtil.memFree(indexByteBuffer);
            }
        }
        this.drawBatchDataHolder.free();
    }

}
