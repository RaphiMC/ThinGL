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

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.DrawBatch;
import net.raphimc.thingl.drawbuilder.DrawMode;
import net.raphimc.thingl.drawbuilder.builder.command.DrawArraysCommand;
import net.raphimc.thingl.drawbuilder.builder.command.DrawCommand;
import net.raphimc.thingl.drawbuilder.builder.command.DrawElementsCommand;
import net.raphimc.thingl.drawbuilder.databuilder.holder.IndexDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.InstanceDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.ShaderDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.VertexDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.DrawBatchDataHolder;
import net.raphimc.thingl.drawbuilder.index.IndexType;
import net.raphimc.thingl.drawbuilder.index.QuadIndexBuffer;
import net.raphimc.thingl.drawbuilder.vertex.VertexDataLayout;
import net.raphimc.thingl.program.RegularProgram;
import net.raphimc.thingl.resource.buffer.AbstractBuffer;
import net.raphimc.thingl.resource.buffer.Buffer;
import net.raphimc.thingl.resource.buffer.ImmutableBuffer;
import net.raphimc.thingl.resource.program.Program;
import net.raphimc.thingl.resource.vertexarray.VertexArray;
import net.raphimc.thingl.util.BufferUtil;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.meshoptimizer.MeshOptimizer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BufferRenderer {

    public static Color COLOR_MODIFIER = null;

    public static PreparedBuffer prepareBuffer(final DrawBatch drawBatch, final DrawBatchDataHolder drawBatchDataHolder, final boolean optimizeMesh) {
        final VertexDataHolder vertexDataHolder = drawBatchDataHolder.getVertexDataHolder();
        final BufferBuilder vertexBufferBuilder = vertexDataHolder.getBufferBuilder();
        final IntList connectedPrimitiveIndices = vertexDataHolder.getConnectedPrimitiveIndices();

        if (vertexDataHolder.getVertexCount() == 0 && vertexBufferBuilder.getPosition() != 0) {
            throw new IllegalStateException("Trying to build a buffer with no vertices but the buffer builder is not empty. Did you forget to call endVertex()?");
        }

        int totalVertexCount = vertexDataHolder.getVertexCount();
        Pair<IndexType, ByteBuffer> indexBuffer = null;
        if (drawBatchDataHolder.hasIndexDataHolder()) {
            if (!drawBatch.drawMode().usesConnectedPrimitives()) {
                final IndexDataHolder indexDataHolder = drawBatchDataHolder.getIndexDataHolder();
                final BufferBuilder indexBufferBuilder = indexDataHolder.getBufferBuilder();
                if (indexDataHolder.getIndexCount() == 0 && indexBufferBuilder.getPosition() != 0) {
                    throw new IllegalStateException("Trying to build a buffer with no indices but the buffer builder is not empty");
                }

                indexBuffer = Pair.of(IndexType.UNSIGNED_INT, indexBufferBuilder.finish());
                totalVertexCount = indexDataHolder.getIndexCount();
            } else {
                throw new IllegalStateException("Draw mode does not support indexed drawing but index data was provided");
            }
        } else if (drawBatch.drawMode() == DrawMode.QUADS) {
            final int quadCount = vertexDataHolder.getVertexCount() / QuadIndexBuffer.QUAD_VERTEX_COUNT;
            if (optimizeMesh) {
                indexBuffer = Pair.of(IndexType.UNSIGNED_INT, ThinGL.quadIndexBuffer().createIndexData(quadCount));
            } else {
                ThinGL.quadIndexBuffer().ensureSize(quadCount);
                indexBuffer = Pair.of(IndexType.UNSIGNED_INT, ThinGL.quadIndexBuffer().getSharedByteBuffer());
            }
            totalVertexCount = quadCount * QuadIndexBuffer.QUAD_INDEX_COUNT;
        }

        if (optimizeMesh && connectedPrimitiveIndices == null && drawBatch.drawMode().getGlMode() == GL11C.GL_TRIANGLES) {
            ThinGL.capabilities().ensureMeshOptimizerPresent();
            if (indexBuffer != null && indexBuffer.first() != IndexType.UNSIGNED_INT) {
                throw new IllegalStateException("Optimizing the mesh requires the index buffer to be of type UNSIGNED_INT");
            }
            final int vertexSize = drawBatch.vertexDataLayout().getSize();
            final ByteBuffer originalVertexBuffer = vertexBufferBuilder.finish();
            final IntBuffer originalIndexBuffer = indexBuffer != null ? indexBuffer.second().asIntBuffer() : null;

            final IntBuffer remapTable = MemoryUtil.memAllocInt(vertexDataHolder.getVertexCount());
            final int uniqueVertexCount = (int) MeshOptimizer.meshopt_generateVertexRemap(remapTable, originalIndexBuffer, totalVertexCount, originalVertexBuffer, vertexDataHolder.getVertexCount(), vertexSize);
            if (uniqueVertexCount > totalVertexCount) {
                throw new IllegalStateException("Optimized mesh contains more vertices than the original mesh");
            }

            final ByteBuffer newIndexBuffer = MemoryUtil.memAlloc(totalVertexCount * Integer.BYTES);
            final ByteBuffer newVertexBuffer = MemoryUtil.memAlloc(uniqueVertexCount * vertexSize);
            final IntBuffer newIndexBufferInt = newIndexBuffer.asIntBuffer();
            MeshOptimizer.meshopt_remapIndexBuffer(newIndexBufferInt, originalIndexBuffer, totalVertexCount, remapTable);
            MeshOptimizer.meshopt_remapVertexBuffer(newVertexBuffer, originalVertexBuffer, vertexDataHolder.getVertexCount(), vertexSize, remapTable);
            BufferUtil.memFree(remapTable);

            MeshOptimizer.meshopt_optimizeVertexCache(newIndexBufferInt, newIndexBufferInt, uniqueVertexCount);
            MeshOptimizer.meshopt_optimizeOverdraw(newIndexBufferInt, newIndexBufferInt, newVertexBuffer.asFloatBuffer(), uniqueVertexCount, vertexSize, 1.05F);
            final long usedVertexCount = MeshOptimizer.meshopt_optimizeVertexFetch(newVertexBuffer, newIndexBufferInt, newVertexBuffer, uniqueVertexCount, vertexSize);
            if (usedVertexCount != uniqueVertexCount) {
                throw new IllegalStateException("Mesh contains unused vertices");
            }

            vertexBufferBuilder.reset();
            MemoryUtil.memCopy(MemoryUtil.memAddress(newVertexBuffer), vertexBufferBuilder.getCursorAddress(), newVertexBuffer.remaining());
            vertexBufferBuilder.setCursorAddress(vertexBufferBuilder.getCursorAddress() + newVertexBuffer.remaining());
            BufferUtil.memFree(newVertexBuffer);

            if (indexBuffer != null) {
                MemoryUtil.memCopy(newIndexBuffer, indexBuffer.second());
                BufferUtil.memFree(newIndexBuffer);
            } else {
                indexBuffer = Pair.of(IndexType.UNSIGNED_INT, newIndexBuffer);
            }
        }

        int instanceCount = 1;
        ByteBuffer instanceBuffer = null;
        if (drawBatchDataHolder.hasInstanceDataHolder()) {
            if (drawBatch.supportsInstancing()) {
                final InstanceDataHolder instanceDataHolder = drawBatchDataHolder.getInstanceDataHolder();
                final BufferBuilder instanceBufferBuilder = instanceDataHolder.getBufferBuilder();
                if (instanceDataHolder.getInstanceCount() == 0 && instanceBufferBuilder.getPosition() != 0) {
                    throw new IllegalStateException("Trying to build a buffer with no instances but the buffer builder is not empty. Did you forget to call endInstance()?");
                }

                instanceBuffer = instanceBufferBuilder.finish();
                instanceCount = instanceDataHolder.getInstanceCount();
            } else {
                throw new IllegalStateException("Draw mode does not support instancing but instance data was provided");
            }
        }

        final Object2ObjectMap<String, ByteBuffer> shaderDataBuffers = new Object2ObjectOpenHashMap<>();
        for (Map.Entry<String, ShaderDataHolder> entry : drawBatchDataHolder.getShaderDataHolders().entrySet()) {
            shaderDataBuffers.put(entry.getKey(), entry.getValue().getBufferBuilder().finish());
        }

        final List<DrawCommand> drawCommands = new ArrayList<>(1);
        if (indexBuffer == null) {
            if (drawBatch.drawMode().usesConnectedPrimitives()) {
                if (connectedPrimitiveIndices == null) {
                    throw new IllegalStateException("Draw mode uses connected primitives but no connected primitive indices were provided");
                }
                final int[] startIndices = new int[connectedPrimitiveIndices.size() - 1];
                final int[] vertexCounts = new int[startIndices.length];
                for (int i = 0; i < startIndices.length; i++) {
                    final int vertexIndex = connectedPrimitiveIndices.getInt(i);
                    startIndices[i] = vertexIndex;
                    vertexCounts[i] = connectedPrimitiveIndices.getInt(i + 1) - vertexIndex;
                }
                for (int i = 0; i < startIndices.length; i++) {
                    drawCommands.add(new DrawArraysCommand(vertexCounts[i], instanceCount, startIndices[i], 0));
                }
            } else {
                if (connectedPrimitiveIndices != null) {
                    throw new IllegalStateException("Draw mode does not use connected primitives but connected primitive indices were provided");
                }
                drawCommands.add(new DrawArraysCommand(totalVertexCount, instanceCount));
            }
        } else {
            if (drawBatch.drawMode().usesConnectedPrimitives()) {
                throw new IllegalStateException("Draw mode uses connected primitives but connected primitives are not supported with indexed drawing");
            } else {
                if (connectedPrimitiveIndices != null) {
                    throw new IllegalStateException("Draw mode does not use connected primitives but connected primitive indices were provided");
                }
                drawCommands.add(new DrawElementsCommand(totalVertexCount, instanceCount));
            }
        }

        return new PreparedBuffer(drawBatch, drawBatchDataHolder, vertexBufferBuilder.finish(), indexBuffer, instanceBuffer, shaderDataBuffers, drawCommands);
    }

    public static BuiltBuffer buildTemporaryBuffer(final PreparedBuffer preparedBuffer) {
        final DrawBatch drawBatch = preparedBuffer.drawBatch();
        final VertexDataLayout vertexDataLayout = drawBatch.vertexDataLayout();
        final VertexArray vertexArray = ThinGL.immediateVertexArrays().getVertexArray(vertexDataLayout);

        if (preparedBuffer.indexBuffer() != null) {
            final ByteBuffer indexData = preparedBuffer.indexBuffer().second();
            if (indexData == ThinGL.quadIndexBuffer().getSharedByteBuffer()) {
                vertexArray.setIndexBuffer(preparedBuffer.indexBuffer().first(), ThinGL.quadIndexBuffer().getSharedGlBuffer());
            } else {
                final Buffer indexBuffer = ThinGL.immediateBuffers().getIndexBuffer();
                if (indexBuffer.getSize() < indexData.remaining()) {
                    indexBuffer.setSize(indexData.remaining());
                }
                indexBuffer.upload(0, indexData);
                vertexArray.setIndexBuffer(preparedBuffer.indexBuffer().first(), indexBuffer);
            }
        } else if (vertexArray.getIndexBuffer() != null) {
            vertexArray.setIndexBuffer(null, null);
        }

        final ByteBuffer vertexData = preparedBuffer.vertexBuffer();
        final Buffer vertexBuffer = (Buffer) vertexArray.getVertexBuffers().get(0);
        if (vertexBuffer.getSize() < vertexData.remaining()) {
            vertexBuffer.setSize(vertexData.remaining());
        }
        vertexBuffer.upload(0, vertexData);

        final ByteBuffer instanceData = preparedBuffer.instanceBuffer();
        if (instanceData != null) {
            final Buffer instanceBuffer = ThinGL.immediateBuffers().getInstanceBuffer();
            if (instanceBuffer.getSize() < instanceData.remaining()) {
                instanceBuffer.setSize(instanceData.remaining());
            }
            instanceBuffer.upload(0, instanceData);
            vertexArray.setVertexBuffer(1, instanceBuffer, 0, drawBatch.instanceDataLayout().getSize());
            vertexArray.configureVertexDataLayout(1, vertexDataLayout.getElements().length, drawBatch.instanceDataLayout(), 1);
        } else if (vertexArray.getVertexBuffers().containsKey(1)) {
            vertexArray.setVertexBuffer(1, null, 0, 0);
            int vertexAttribIndex = vertexDataLayout.getElements().length;
            while (GL45C.glGetVertexArrayIndexedi(vertexArray.getGlId(), vertexAttribIndex, GL45C.GL_VERTEX_ATTRIB_ARRAY_ENABLED) == GL11C.GL_TRUE) {
                GL45C.glDisableVertexArrayAttrib(vertexArray.getGlId(), vertexAttribIndex);
                vertexAttribIndex++;
            }
        }

        final Object2ObjectMap<String, AbstractBuffer> shaderDataBuffers = new Object2ObjectOpenHashMap<>();
        int ssboPoolIndex = 0;
        for (Map.Entry<String, ByteBuffer> entry : preparedBuffer.shaderDataBuffers().entrySet()) {
            final ByteBuffer ssboData = entry.getValue();
            final Buffer ssboBuffer = ThinGL.immediateBuffers().getSSBOBuffer(ssboPoolIndex);
            if (ssboBuffer.getSize() < ssboData.remaining()) {
                ssboBuffer.setSize(ssboData.remaining());
            }
            ssboBuffer.upload(0, ssboData);
            shaderDataBuffers.put(entry.getKey(), ssboBuffer);
            ssboPoolIndex++;
        }

        Buffer commandBuffer = null;
        if (preparedBuffer.drawCommands().size() > 1) {
            final BufferBuilder commandBufferBuilder = ThinGL.bufferBuilderPool().borrowBufferBuilder();
            commandBufferBuilder.ensureHasEnoughSpace(preparedBuffer.drawCommands().size() * DrawCommand.SIZE);
            for (DrawCommand drawCommand : preparedBuffer.drawCommands()) {
                drawCommand.write(commandBufferBuilder);
            }
            final ByteBuffer commandData = commandBufferBuilder.finish();
            commandBuffer = ThinGL.immediateBuffers().getCommandBuffer();
            if (commandBuffer.getSize() < commandData.remaining()) {
                commandBuffer.setSize(commandData.remaining());
            }
            commandBuffer.upload(0, commandData);
            ThinGL.bufferBuilderPool().returnBufferBuilder(commandBufferBuilder);
        }

        preparedBuffer.free();
        return new BuiltBuffer(drawBatch, vertexArray, shaderDataBuffers, commandBuffer, preparedBuffer.drawCommands());
    }

    public static BuiltBuffer buildPersistentBuffer(final PreparedBuffer preparedBuffer) {
        final DrawBatch drawBatch = preparedBuffer.drawBatch();
        final VertexDataLayout vertexDataLayout = drawBatch.vertexDataLayout();
        final VertexArray vertexArray = new VertexArray();

        if (preparedBuffer.indexBuffer() != null) {
            final ByteBuffer indexByteBuffer = preparedBuffer.indexBuffer().second();
            if (indexByteBuffer == ThinGL.quadIndexBuffer().getSharedByteBuffer()) {
                vertexArray.setIndexBuffer(preparedBuffer.indexBuffer().first(), ThinGL.quadIndexBuffer().getSharedGlBuffer());
            } else {
                final AbstractBuffer indexBuffer = new ImmutableBuffer(indexByteBuffer, 0);
                vertexArray.setIndexBuffer(preparedBuffer.indexBuffer().first(), indexBuffer);
            }
        }

        final AbstractBuffer vertexBuffer = new ImmutableBuffer(preparedBuffer.vertexBuffer(), 0);
        vertexArray.setVertexBuffer(0, vertexBuffer, 0, vertexDataLayout.getSize());
        vertexArray.configureVertexDataLayout(0, 0, vertexDataLayout, 0);

        if (preparedBuffer.instanceBuffer() != null) {
            final AbstractBuffer instanceBuffer = new ImmutableBuffer(preparedBuffer.instanceBuffer(), 0);
            vertexArray.setVertexBuffer(1, instanceBuffer, 0, drawBatch.instanceDataLayout().getSize());
            vertexArray.configureVertexDataLayout(1, vertexDataLayout.getElements().length, drawBatch.instanceDataLayout(), 1);
        }

        final Object2ObjectMap<String, AbstractBuffer> shaderDataBuffers = new Object2ObjectOpenHashMap<>();
        for (Map.Entry<String, ByteBuffer> entry : preparedBuffer.shaderDataBuffers().entrySet()) {
            shaderDataBuffers.put(entry.getKey(), new ImmutableBuffer(entry.getValue(), 0));
        }

        AbstractBuffer commandBuffer = null;
        if (preparedBuffer.drawCommands().size() > 1) {
            final BufferBuilder commandBufferBuilder = ThinGL.bufferBuilderPool().borrowBufferBuilder();
            commandBufferBuilder.ensureHasEnoughSpace(preparedBuffer.drawCommands().size() * DrawCommand.SIZE);
            for (DrawCommand drawCommand : preparedBuffer.drawCommands()) {
                drawCommand.write(commandBufferBuilder);
            }
            commandBuffer = new ImmutableBuffer(commandBufferBuilder.finish(), 0);
            ThinGL.bufferBuilderPool().returnBufferBuilder(commandBufferBuilder);
        }

        preparedBuffer.free();
        return new BuiltBuffer(drawBatch, vertexArray, shaderDataBuffers, commandBuffer, preparedBuffer.drawCommands());
    }

    public static void render(final BuiltBuffer builtBuffer, final Matrix4f modelMatrix) {
        render(builtBuffer, false, modelMatrix);
    }

    public static void render(final BuiltBuffer builtBuffer, final boolean useMultiDrawProgram, final Matrix4f modelMatrix) {
        final DrawBatch drawBatch = builtBuffer.drawBatch();
        final DrawMode drawMode = drawBatch.drawMode();
        final VertexArray vertexArray = builtBuffer.vertexArray();
        final List<DrawCommand> drawCommands = builtBuffer.drawCommands();
        if (drawCommands.isEmpty()) {
            return;
        }

        drawBatch.setupAction().run();

        final Program program = drawBatch.getProgram(vertexArray.getVertexBuffers().containsKey(1), useMultiDrawProgram);
        if (program != null) {
            program.bind();
            if (program instanceof RegularProgram) {
                if ((modelMatrix.properties() & Matrix4fc.PROPERTY_IDENTITY) == 0) {
                    program.setUniform("u_ModelMatrix", modelMatrix);
                }
                if (COLOR_MODIFIER != null) {
                    program.setUniform("u_ColorModifier", COLOR_MODIFIER);
                }
                for (Map.Entry<String, AbstractBuffer> entry : builtBuffer.shaderDataBuffers().entrySet()) {
                    program.setShaderStorageBuffer(entry.getKey(), entry.getValue());
                }
            }
        }

        if (drawCommands.size() == 1) {
            final DrawCommand drawCommand = drawCommands.get(0);
            if (drawCommand instanceof DrawElementsCommand drawElementsCommand) {
                vertexArray.drawElements(drawMode, drawElementsCommand.vertexCount(), drawElementsCommand.firstIndex(), drawElementsCommand.instanceCount(), drawElementsCommand.baseVertex(), drawElementsCommand.baseInstance());
            } else if (drawCommand instanceof DrawArraysCommand drawArraysCommand) {
                vertexArray.drawArrays(drawMode, drawArraysCommand.vertexCount(), drawArraysCommand.firstVertex(), drawArraysCommand.instanceCount(), drawArraysCommand.baseInstance());
            }
        } else if (builtBuffer.commandBuffer() != null) {
            if (vertexArray.getIndexBuffer() != null) {
                vertexArray.drawElementsIndirect(drawMode, builtBuffer.commandBuffer(), 0, drawCommands.size());
            } else {
                vertexArray.drawArraysIndirect(drawMode, builtBuffer.commandBuffer(), 0, drawCommands.size());
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
