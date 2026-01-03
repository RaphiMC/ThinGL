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
package net.raphimc.thingl.gl.resource.vertexarray;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.GLContainerObject;
import net.raphimc.thingl.gl.resource.buffer.Buffer;
import net.raphimc.thingl.rendering.DrawMode;
import net.raphimc.thingl.rendering.vertex.VertexDataLayout;
import net.raphimc.thingl.rendering.vertex.VertexDataLayoutElement;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL40C;

public class VertexArray extends GLContainerObject {

    private final Int2ObjectMap<Buffer> vertexBuffers = new Int2ObjectOpenHashMap<>();
    private int indexType;
    private Buffer indexBuffer;

    public VertexArray() {
        super(ThinGL.glBackend().createVertexArrays());
    }

    protected VertexArray(final int glId) {
        super(glId);
    }

    public static VertexArray fromGlId(final int glId) {
        if (!ThinGL.glBackend().isVertexArray(glId)) {
            throw new IllegalArgumentException("Not a vertex array object");
        }
        return fromGlIdUnsafe(glId);
    }

    public static VertexArray fromGlIdUnsafe(final int glId) {
        return new VertexArray(glId);
    }

    public void setVertexBuffer(final int bindingIndex, final Buffer buffer, final long offset, final int stride) {
        if (buffer != null) {
            this.vertexBuffers.put(bindingIndex, buffer);
            ThinGL.glBackend().vertexArrayVertexBuffer(this.getGlId(), bindingIndex, buffer.getGlId(), offset, stride);
        } else {
            this.vertexBuffers.remove(bindingIndex);
            ThinGL.glBackend().vertexArrayVertexBuffer(this.getGlId(), bindingIndex, 0, 0, 0);
        }
    }

    public void setIndexBuffer(final int indexType, final Buffer buffer) {
        this.indexType = indexType;
        this.indexBuffer = buffer;
        if (buffer != null) {
            ThinGL.glBackend().vertexArrayElementBuffer(this.getGlId(), buffer.getGlId());
        } else {
            ThinGL.glBackend().vertexArrayElementBuffer(this.getGlId(), 0);
        }
    }

    public void configureVertexDataLayout(final int bindingIndex, final int attribOffset, final VertexDataLayout vertexDataLayout, final int divisor) {
        int relativeOffset = 0;
        for (int i = 0; i < vertexDataLayout.getElements().length; i++) {
            final VertexDataLayoutElement element = vertexDataLayout.getElements()[i];
            switch (element.targetDataType()) {
                case INT -> ThinGL.glBackend().vertexArrayAttribIFormat(this.getGlId(), i + attribOffset, element.count(), element.dataType().getGlType(), relativeOffset);
                case FLOAT -> ThinGL.glBackend().vertexArrayAttribFormat(this.getGlId(), i + attribOffset, element.count(), element.dataType().getGlType(), false, relativeOffset);
                case FLOAT_NORMALIZED -> ThinGL.glBackend().vertexArrayAttribFormat(this.getGlId(), i + attribOffset, element.count(), element.dataType().getGlType(), true, relativeOffset);
                case DOUBLE -> ThinGL.glBackend().vertexArrayAttribLFormat(this.getGlId(), i + attribOffset, element.count(), element.dataType().getGlType(), relativeOffset);
            }
            ThinGL.glBackend().vertexArrayAttribBinding(this.getGlId(), i + attribOffset, bindingIndex);
            ThinGL.glBackend().enableVertexArrayAttrib(this.getGlId(), i + attribOffset);
            relativeOffset += element.count() * element.dataType().getSize() + element.padding();
        }
        ThinGL.glBackend().vertexArrayBindingDivisor(this.getGlId(), bindingIndex, divisor);
    }

    public void drawArrays(final DrawMode drawMode, final int count, final int offset) {
        this.bind();
        ThinGL.glBackend().drawArrays(drawMode.getGlMode(), offset, count);
        this.unbind();
    }

    public void drawArrays(final DrawMode drawMode, final int count, final int offset, final int instanceCount, final int baseInstance) {
        this.bind();
        ThinGL.glBackend().drawArraysInstancedBaseInstance(drawMode.getGlMode(), offset, count, instanceCount, baseInstance);
        this.unbind();
    }

    public void drawArraysIndirect(final DrawMode drawMode, final Buffer indirectCommandBuffer, final long offset, final int count) {
        this.bind();
        final int prevIndirectCommandBuffer = ThinGL.glBackend().getInteger(GL40C.GL_DRAW_INDIRECT_BUFFER_BINDING);
        ThinGL.glBackend().bindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, indirectCommandBuffer.getGlId());
        if (count == 1) {
            ThinGL.glBackend().drawArraysIndirect(drawMode.getGlMode(), offset);
        } else {
            ThinGL.glBackend().multiDrawArraysIndirect(drawMode.getGlMode(), offset, count, 0);
        }
        ThinGL.glBackend().bindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, prevIndirectCommandBuffer);
        this.unbind();
    }

    public void drawElements(final DrawMode drawMode, final int count, final int offset) {
        this.bind();
        ThinGL.glBackend().drawElements(drawMode.getGlMode(), count, this.indexType, offset);
        this.unbind();
    }

    public void drawElements(final DrawMode drawMode, final int count, final int offset, final int instanceCount, final int baseVertex, final int baseInstance) {
        this.bind();
        ThinGL.glBackend().drawElementsInstancedBaseVertexBaseInstance(drawMode.getGlMode(), count, this.indexType, offset, instanceCount, baseVertex, baseInstance);
        this.unbind();
    }

    public void drawElementsIndirect(final DrawMode drawMode, final Buffer indirectCommandBuffer, final long offset, final int count) {
        this.bind();
        final int prevIndirectCommandBuffer = ThinGL.glBackend().getInteger(GL40C.GL_DRAW_INDIRECT_BUFFER_BINDING);
        ThinGL.glBackend().bindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, indirectCommandBuffer.getGlId());
        if (count == 1) {
            ThinGL.glBackend().drawElementsIndirect(drawMode.getGlMode(), this.indexType, offset);
        } else {
            ThinGL.glBackend().multiDrawElementsIndirect(drawMode.getGlMode(), this.indexType, offset, count, 0);
        }
        ThinGL.glBackend().bindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, prevIndirectCommandBuffer);
        this.unbind();
    }

    @Override
    protected void free0() {
        ThinGL.glBackend().deleteVertexArrays(this.getGlId());
    }

    @Override
    protected void freeContainingObjects() {
        for (Buffer buffer : this.vertexBuffers.values()) {
            buffer.free();
        }
        this.vertexBuffers.clear();
        if (this.indexBuffer != null && this.indexBuffer != ThinGL.quadIndexBuffer().getSharedBuffer()) {
            this.indexBuffer.free();
        }
        this.indexBuffer = null;
    }

    @Override
    public final int getGlType() {
        return GL11C.GL_VERTEX_ARRAY;
    }

    public Int2ObjectMap<Buffer> getVertexBuffers() {
        return this.vertexBuffers;
    }

    public int getIndexType() {
        return this.indexType;
    }

    public Buffer getIndexBuffer() {
        return this.indexBuffer;
    }

    private void bind() {
        if (ThinGL.config().restoreVertexArrayBinding()) {
            ThinGL.glStateStack().pushVertexArray();
        }
        ThinGL.glStateManager().setVertexArray(this.getGlId());
    }

    private void unbind() {
        if (ThinGL.config().restoreVertexArrayBinding()) {
            ThinGL.glStateStack().popVertexArray();
        }
    }

}
