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
package net.raphimc.thingl.gl.rendering;

import net.raphimc.thingl.gl.program.RegularProgram;
import net.raphimc.thingl.gl.rendering.upload.UploadedDrawBatchData;
import net.raphimc.thingl.gl.resource.buffer.Buffer;
import net.raphimc.thingl.gl.resource.program.Program;
import net.raphimc.thingl.gl.resource.vertexarray.VertexArray;
import net.raphimc.thingl.rendering.DrawBatch;
import net.raphimc.thingl.rendering.DrawMode;
import net.raphimc.thingl.rendering.command.DrawCommand;
import net.raphimc.thingl.rendering.command.impl.DrawArraysCommand;
import net.raphimc.thingl.rendering.command.impl.DrawElementsCommand;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

public class DrawBatchRenderer {

    public static void render(final UploadedDrawBatchData uploadedDrawBatchData, final Matrix4f modelMatrix) {
        final DrawBatch drawBatch = uploadedDrawBatchData.drawBatch();
        final DrawMode drawMode = drawBatch.drawMode();
        final VertexArray vertexArray = uploadedDrawBatchData.vertexArray();
        final List<DrawCommand> drawCommands = uploadedDrawBatchData.drawCommands();
        if (drawCommands.isEmpty()) {
            return;
        }

        drawBatch.setupAction().run();
        final Program program = drawBatch.program().get();
        if (program != null) {
            program.bind();
            if (program instanceof RegularProgram regularProgram) {
                regularProgram.configureParameters(modelMatrix);
                for (Map.Entry<String, Buffer> entry : uploadedDrawBatchData.uniformBuffers().entrySet()) {
                    program.setUniformBuffer(entry.getKey(), entry.getValue());
                }
                for (Map.Entry<String, Buffer> entry : uploadedDrawBatchData.shaderStorageBuffers().entrySet()) {
                    program.setShaderStorageBuffer(entry.getKey(), entry.getValue());
                }
            }
        }

        if (drawCommands.size() == 1) {
            final DrawCommand drawCommand = drawCommands.getFirst();
            if (drawCommand instanceof DrawElementsCommand drawElementsCommand) {
                vertexArray.drawElements(drawMode, drawElementsCommand.vertexCount(), drawElementsCommand.firstIndex(), drawElementsCommand.instanceCount(), drawElementsCommand.baseVertex(), drawElementsCommand.baseInstance());
            } else if (drawCommand instanceof DrawArraysCommand drawArraysCommand) {
                vertexArray.drawArrays(drawMode, drawArraysCommand.vertexCount(), drawArraysCommand.firstVertex(), drawArraysCommand.instanceCount(), drawArraysCommand.baseInstance());
            }
        } else if (uploadedDrawBatchData.commandBuffer() != null) {
            if (vertexArray.getIndexBuffer() != null) {
                vertexArray.drawElementsIndirect(drawMode, uploadedDrawBatchData.commandBuffer(), 0, drawCommands.size());
            } else {
                vertexArray.drawArraysIndirect(drawMode, uploadedDrawBatchData.commandBuffer(), 0, drawCommands.size());
            }
        } else {
            throw new IllegalStateException("Draw calls with multiple draw commands require a command buffer");
        }

        if (program != null) {
            program.unbind();
        }
        drawBatch.cleanupAction().run();
    }

}
