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
package net.raphimc.thingl.gl.resource.buffer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.raphimc.thingl.gl.resource.GLObject;
import net.raphimc.thingl.gl.resource.buffer.impl.ImmutableBuffer;
import net.raphimc.thingl.gl.resource.buffer.impl.MutableBuffer;
import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.resource.memory.Memory;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL45C;

public abstract class Buffer extends GLObject {

    protected final Int2ObjectMap<Object> parameters = new Int2ObjectOpenHashMap<>();

    public Buffer() {
        super(GL45C.glCreateBuffers());
    }

    protected Buffer(final int glId) {
        super(glId);
    }

    public static Buffer fromGlId(final int glId) {
        if (!GL15C.glIsBuffer(glId)) {
            throw new IllegalArgumentException("Not a buffer object");
        }
        return fromGlIdUnsafe(glId);
    }

    public static Buffer fromGlIdUnsafe(final int glId) {
        final boolean immutable = GL45C.glGetNamedBufferParameteri(glId, GL45C.GL_BUFFER_IMMUTABLE_STORAGE) != GL11C.GL_FALSE;
        if (immutable) {
            return ImmutableBuffer.fromGlIdUnsafe(glId);
        } else {
            return MutableBuffer.fromGlIdUnsafe(glId);
        }
    }

    public void upload(final Memory data) {
        this.upload(0L, data);
    }

    public void upload(final long offset, final Memory data) {
        GL45C.nglNamedBufferSubData(this.getGlId(), offset, data.getSize(), data.getAddress());
    }

    public Memory download() {
        return this.download(0L, this.getSize());
    }

    public Memory download(final long offset, final long length) {
        final Memory data = MemoryAllocator.allocateMemory(length);
        GL45C.nglGetNamedBufferSubData(this.getGlId(), offset, data.getSize(), data.getAddress());
        return data;
    }

    public void copyTo(final Buffer target, final long readOffset, final long writeOffset, final long length) {
        GL45C.glCopyNamedBufferSubData(this.getGlId(), target.getGlId(), readOffset, writeOffset, length);
    }

    public Memory map(final int access) {
        this.parameters.clear();
        return MemoryAllocator.wrapMemory(GL45C.nglMapNamedBuffer(this.getGlId(), access), this.getSize());
    }

    public Memory mapFullRange(final int accessFlags) {
        return this.mapRange(0L, this.getSize(), accessFlags);
    }

    public Memory mapRange(final long offset, final long length, final int accessFlags) {
        this.parameters.clear();
        return MemoryAllocator.wrapMemory(GL45C.nglMapNamedBufferRange(this.getGlId(), offset, length, accessFlags), length);
    }

    public void flush(final long offset, final long length) {
        GL45C.glFlushMappedNamedBufferRange(this.getGlId(), offset, length);
    }

    public void unmap() {
        this.parameters.clear();
        GL45C.glUnmapNamedBuffer(this.getGlId());
    }

    @Override
    protected void free0() {
        GL15C.glDeleteBuffers(this.getGlId());
    }

    @Override
    public final int getGlType() {
        return GL43C.GL_BUFFER;
    }

    public int getParameterInt(final int parameter) {
        Object value = this.parameters.get(parameter);
        if (!(value instanceof Integer)) {
            value = GL45C.glGetNamedBufferParameteri(this.getGlId(), parameter);
            this.parameters.put(parameter, value);
        }
        return (int) value;
    }

    public long getParameterLong(final int parameter) {
        Object value = this.parameters.get(parameter);
        if (!(value instanceof Long)) {
            value = GL45C.glGetNamedBufferParameteri64(this.getGlId(), parameter);
            this.parameters.put(parameter, value);
        }
        return (long) value;
    }

    public long getSize() {
        return this.getParameterLong(GL15C.GL_BUFFER_SIZE);
    }

}
