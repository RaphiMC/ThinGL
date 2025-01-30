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

package net.raphimc.thingl.resource.vertexarray;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.raphimc.thingl.drawbuilder.DrawMode;
import net.raphimc.thingl.drawbuilder.index.IndexType;
import net.raphimc.thingl.drawbuilder.vertex.VertexDataLayout;
import net.raphimc.thingl.drawbuilder.vertex.VertexDataLayoutElement;
import net.raphimc.thingl.resource.GLResource;
import net.raphimc.thingl.resource.buffer.AbstractBuffer;
import org.lwjgl.opengl.*;

public class VertexArray extends GLResource {

    private final Int2ObjectMap<AbstractBuffer> vertexBuffers = new Int2ObjectOpenHashMap<>();
    private IndexType indexType;
    private AbstractBuffer indexBuffer;

    public VertexArray() {
        super(GL11C.GL_VERTEX_ARRAY, GL45C.glCreateVertexArrays());
    }

    protected VertexArray(final int glId) {
        super(GL11C.GL_VERTEX_ARRAY, glId);
    }

    public static VertexArray fromGlId(final int glId) {
        if (!GL30C.glIsVertexArray(glId)) {
            throw new IllegalArgumentException("Invalid OpenGL resource");
        }
        return new VertexArray(glId);
    }

    public void setVertexBuffer(final int bindingIndex, final AbstractBuffer buffer, final long offset, final int stride) {
        if (buffer != null) {
            this.vertexBuffers.put(bindingIndex, buffer);
            GL45C.glVertexArrayVertexBuffer(this.getGlId(), bindingIndex, buffer.getGlId(), offset, stride);
        } else {
            this.vertexBuffers.remove(bindingIndex);
            GL45C.glVertexArrayVertexBuffer(this.getGlId(), bindingIndex, 0, 0, 0);
        }
    }

    public void setIndexBuffer(final IndexType indexType, final AbstractBuffer buffer) {
        this.indexType = indexType;
        this.indexBuffer = buffer;
        if (buffer != null) {
            GL45C.glVertexArrayElementBuffer(this.getGlId(), buffer.getGlId());
        } else {
            GL45C.glVertexArrayElementBuffer(this.getGlId(), 0);
        }
    }

    public void configureVertexDataLayout(final int bindingIndex, final int attribOffset, final VertexDataLayout vertexDataLayout, final int divisor) {
        int relativeOffset = 0;
        for (int i = 0; i < vertexDataLayout.getElements().length; i++) {
            final VertexDataLayoutElement element = vertexDataLayout.getElements()[i];
            if (element.isInteger()) {
                GL45C.glVertexArrayAttribIFormat(this.getGlId(), i + attribOffset, element.count(), element.type().getGlType(), relativeOffset);
            } else {
                GL45C.glVertexArrayAttribFormat(this.getGlId(), i + attribOffset, element.count(), element.type().getGlType(), element.normalized(), relativeOffset);
            }
            GL45C.glVertexArrayAttribBinding(this.getGlId(), i + attribOffset, bindingIndex);
            GL45C.glEnableVertexArrayAttrib(this.getGlId(), i + attribOffset);
            relativeOffset += element.count() * element.type().getSize() + element.padding();
        }
        GL45C.glVertexArrayBindingDivisor(this.getGlId(), bindingIndex, divisor);
    }

    public void drawArrays(final DrawMode drawMode, final int count, final int offset) {
        final int prevVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
        GL30C.glBindVertexArray(this.getGlId());
        GL11C.glDrawArrays(drawMode.getGlMode(), offset, count);
        GL30C.glBindVertexArray(prevVertexArray);
    }

    public void drawArrays(final DrawMode drawMode, final int count, final int offset, final int instanceCount, final int baseInstance) {
        final int prevVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
        GL30C.glBindVertexArray(this.getGlId());
        GL42C.glDrawArraysInstancedBaseInstance(drawMode.getGlMode(), offset, count, instanceCount, baseInstance);
        GL30C.glBindVertexArray(prevVertexArray);
    }

    public void drawArraysIndirect(final DrawMode drawMode, final AbstractBuffer indirectCommandBuffer, final long offset, final int count) {
        final int prevVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
        final int prevIndirectCommandBuffer = GL11C.glGetInteger(GL40C.GL_DRAW_INDIRECT_BUFFER_BINDING);
        GL30C.glBindVertexArray(this.getGlId());
        GL15C.glBindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, indirectCommandBuffer.getGlId());
        if (count == 1) {
            GL40C.glDrawArraysIndirect(drawMode.getGlMode(), offset);
        } else {
            GL43C.glMultiDrawArraysIndirect(drawMode.getGlMode(), offset, count, 0);
        }
        GL15C.glBindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, prevIndirectCommandBuffer);
        GL30C.glBindVertexArray(prevVertexArray);
    }

    public void drawElements(final DrawMode drawMode, final int count, final int offset) {
        final int prevVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
        GL30C.glBindVertexArray(this.getGlId());
        GL11C.glDrawElements(drawMode.getGlMode(), count, this.indexType.getGlType(), offset);
        GL30C.glBindVertexArray(prevVertexArray);
    }

    public void drawElements(final DrawMode drawMode, final int count, final int offset, final int instanceCount, final int baseVertex, final int baseInstance) {
        final int prevVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
        GL30C.glBindVertexArray(this.getGlId());
        GL42C.glDrawElementsInstancedBaseVertexBaseInstance(drawMode.getGlMode(), count, this.indexType.getGlType(), offset, instanceCount, baseVertex, baseInstance);
        GL30C.glBindVertexArray(prevVertexArray);
    }

    public void drawElementsIndirect(final DrawMode drawMode, final AbstractBuffer indirectCommandBuffer, final long offset, final int count) {
        final int prevVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
        final int prevIndirectCommandBuffer = GL11C.glGetInteger(GL40C.GL_DRAW_INDIRECT_BUFFER_BINDING);
        GL30C.glBindVertexArray(this.getGlId());
        GL15C.glBindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, indirectCommandBuffer.getGlId());
        if (count == 1) {
            GL40C.glDrawElementsIndirect(drawMode.getGlMode(), this.indexType.getGlType(), offset);
        } else {
            GL43C.glMultiDrawElementsIndirect(drawMode.getGlMode(), this.indexType.getGlType(), offset, count, 0);
        }
        GL15C.glBindBuffer(GL40C.GL_DRAW_INDIRECT_BUFFER, prevIndirectCommandBuffer);
        GL30C.glBindVertexArray(prevVertexArray);
    }

    @Override
    protected void delete0() {
        GL30C.glDeleteVertexArrays(this.getGlId());
    }

    public Int2ObjectMap<AbstractBuffer> getVertexBuffers() {
        return this.vertexBuffers;
    }

    public IndexType getIndexType() {
        return this.indexType;
    }

    public AbstractBuffer getIndexBuffer() {
        return this.indexBuffer;
    }

}
