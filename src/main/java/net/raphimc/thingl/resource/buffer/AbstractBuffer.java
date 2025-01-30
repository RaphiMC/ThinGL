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

package net.raphimc.thingl.resource.buffer;

import net.raphimc.thingl.resource.GLResource;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public abstract class AbstractBuffer extends GLResource {

    protected long size;

    public AbstractBuffer(final long size) {
        super(GL43C.GL_BUFFER, GL45C.glCreateBuffers());
        this.size = size;
    }

    protected AbstractBuffer(final int glId) {
        super(GL43C.GL_BUFFER, glId);
        this.size = GL45C.glGetNamedBufferParameteri64(glId, GL15C.GL_BUFFER_SIZE);
    }

    public static AbstractBuffer fromGlId(final int glId) {
        if (!GL15C.glIsBuffer(glId)) {
            throw new IllegalArgumentException("Invalid OpenGL resource");
        }
        final boolean immutable = GL45C.glGetNamedBufferParameteri(glId, GL45C.GL_BUFFER_IMMUTABLE_STORAGE) != GL11C.GL_FALSE;
        if (immutable) {
            return new ImmutableBuffer(glId);
        } else {
            return new Buffer(glId);
        }
    }

    public void upload(final int offset, final ByteBuffer buffer) {
        if (buffer.remaining() > this.size - offset) {
            throw new IllegalArgumentException("Buffer is too large");
        }

        GL45C.glNamedBufferSubData(this.getGlId(), offset, buffer);
    }

    public ByteBuffer download(final int offset) {
        return this.download(offset, this.size - offset);
    }

    public ByteBuffer download(final int offset, final long length) {
        if (length > this.size - offset) {
            throw new IllegalArgumentException("Buffer is too large");
        }

        final ByteBuffer buffer = MemoryUtil.memAlloc((int) length);
        GL45C.glGetNamedBufferSubData(this.getGlId(), offset, buffer);
        return buffer;
    }

    public ByteBuffer map(final int offset, final long length, final int access) {
        return GL45C.glMapNamedBufferRange(this.getGlId(), offset, length, access);
    }

    public void unmap() {
        GL45C.glUnmapNamedBuffer(this.getGlId());
    }

    public void flush(final int offset, final long length) {
        GL45C.glFlushMappedNamedBufferRange(this.getGlId(), offset, length);
    }

    @Override
    protected void delete0() {
        GL15C.glDeleteBuffers(this.getGlId());
    }

    public long getSize() {
        return this.size;
    }

}
