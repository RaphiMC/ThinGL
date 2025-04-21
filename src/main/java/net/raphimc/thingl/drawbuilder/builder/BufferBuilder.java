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

import net.raphimc.thingl.util.MathUtil;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class BufferBuilder {

    private static final int DEFAULT_INITIAL_SIZE = 64 * 1024;
    private static final int GROW_ALIGNMENT = 1024;

    private final boolean isExternallyAllocated;
    private long baseAddress;
    private long cursorAddress;
    private long limitAddress;

    public BufferBuilder() {
        this(DEFAULT_INITIAL_SIZE);
    }

    public BufferBuilder(final int initialSize) {
        this.isExternallyAllocated = false;
        this.baseAddress = MemoryUtil.nmemAlloc(initialSize);
        if (this.baseAddress == 0) {
            throw new OutOfMemoryError("Failed to allocate memory of size: " + initialSize);
        }
        this.cursorAddress = this.baseAddress;
        this.limitAddress = this.baseAddress + initialSize;
    }

    public BufferBuilder(final MemoryStack memoryStack, final int size) {
        this.isExternallyAllocated = true;
        this.baseAddress = memoryStack.nmalloc(size);
        this.cursorAddress = this.baseAddress;
        this.limitAddress = this.baseAddress + size;
    }

    public BufferBuilder(final ByteBuffer byteBuffer) {
        if (!byteBuffer.isDirect()) {
            throw new IllegalArgumentException("ByteBuffer must be direct");
        }
        this.isExternallyAllocated = true;
        this.baseAddress = MemoryUtil.memAddress0((Buffer) byteBuffer);
        this.cursorAddress = this.baseAddress + byteBuffer.position();
        this.limitAddress = this.baseAddress + byteBuffer.capacity();
    }

    public BufferBuilder putByte(final byte b) {
        if (this.limitAddress - this.cursorAddress < 1) {
            this.ensureHasEnoughSpace(1);
        }
        MemoryUtil.memPutByte(this.cursorAddress, b);
        this.cursorAddress++;
        return this;
    }

    public BufferBuilder putShort(final short s) {
        if (this.limitAddress - this.cursorAddress < 2) {
            this.ensureHasEnoughSpace(2);
        }
        MemoryUtil.memPutShort(this.cursorAddress, s);
        this.cursorAddress += 2;
        return this;
    }

    public BufferBuilder putInt(final int i) {
        if (this.limitAddress - this.cursorAddress < 4) {
            this.ensureHasEnoughSpace(4);
        }
        MemoryUtil.memPutInt(this.cursorAddress, i);
        this.cursorAddress += 4;
        return this;
    }

    public BufferBuilder putFloat(final float f) {
        if (this.limitAddress - this.cursorAddress < 4) {
            this.ensureHasEnoughSpace(4);
        }
        MemoryUtil.memPutFloat(this.cursorAddress, f);
        this.cursorAddress += 4;
        return this;
    }

    public BufferBuilder putDouble(final double d) {
        if (this.limitAddress - this.cursorAddress < 8) {
            this.ensureHasEnoughSpace(8);
        }
        MemoryUtil.memPutDouble(this.cursorAddress, d);
        this.cursorAddress += 8;
        return this;
    }

    public BufferBuilder putHalfFloat(final float f) {
        return this.putShort(MathUtil.encodeHalfFloat(f));
    }

    public BufferBuilder putVector2f(final Vector2f vector) {
        return this.putVector2f(vector.x, vector.y);
    }

    public BufferBuilder putVector2f(final float x, final float y) {
        if (this.limitAddress - this.cursorAddress < 8) {
            this.ensureHasEnoughSpace(8);
        }
        MemoryUtil.memPutFloat(this.cursorAddress, x);
        MemoryUtil.memPutFloat(this.cursorAddress + 4, y);
        this.cursorAddress += 8;
        return this;
    }

    public BufferBuilder putVector3f(final Vector3f vector) {
        return this.putVector3f(vector.x, vector.y, vector.z);
    }

    public BufferBuilder putVector3f(final float x, final float y, final float z) {
        if (this.limitAddress - this.cursorAddress < 12) {
            this.ensureHasEnoughSpace(12);
        }
        MemoryUtil.memPutFloat(this.cursorAddress, x);
        MemoryUtil.memPutFloat(this.cursorAddress + 4, y);
        MemoryUtil.memPutFloat(this.cursorAddress + 8, z);
        this.cursorAddress += 12;
        return this;
    }

    public BufferBuilder align(final int alignment) {
        final int position = this.getPosition();
        final int alignedPosition = MathUtil.align(position, alignment);
        return this.skip(alignedPosition - position);
    }

    public BufferBuilder skip(final int bytes) {
        this.ensureHasEnoughSpace(bytes);
        this.cursorAddress += bytes;
        return this;
    }

    public ByteBuffer finish() {
        final ByteBuffer byteBuffer = MemoryUtil.memByteBuffer(this.baseAddress, this.getPosition());
        this.reset();
        return byteBuffer;
    }

    public void reset() {
        this.cursorAddress = this.baseAddress;
    }

    public void free() {
        if (!this.isExternallyAllocated) {
            MemoryUtil.nmemFree(this.baseAddress);
        }
        this.baseAddress = 0;
        this.cursorAddress = 0;
        this.limitAddress = 0;
    }

    public void ensureHasEnoughSpace(final int bytes) {
        if (this.getRemaining() < bytes) {
            if (!this.isExternallyAllocated) {
                final int oldSize = this.getSize();
                this.resize(MathUtil.align(oldSize + Math.max(bytes, oldSize), GROW_ALIGNMENT));
            } else {
                throw new IllegalStateException("Buffer is full");
            }
        }
    }

    public long getBaseAddress() {
        return this.baseAddress;
    }

    public long getCursorAddress() {
        return this.cursorAddress;
    }

    public void setCursorAddress(final long cursorAddress) {
        if (cursorAddress < this.baseAddress || cursorAddress >= this.limitAddress) {
            throw new IllegalArgumentException("Cursor address is out of bounds");
        }

        this.cursorAddress = cursorAddress;
    }

    public long getLimitAddress() {
        return this.limitAddress;
    }

    public int getSize() {
        return (int) (this.limitAddress - this.baseAddress);
    }

    public int getPosition() {
        return (int) (this.cursorAddress - this.baseAddress);
    }

    public int getRemaining() {
        return (int) (this.limitAddress - this.cursorAddress);
    }

    private void resize(final int newSize) {
        if (this.isExternallyAllocated) {
            throw new IllegalStateException("Cannot resize externally allocated buffer");
        }

        final int position = this.getPosition();
        final long newBaseAddress = MemoryUtil.nmemRealloc(this.baseAddress, newSize);
        if (newBaseAddress == 0) {
            throw new OutOfMemoryError("Failed to allocate memory of size: " + newSize);
        }
        this.baseAddress = newBaseAddress;
        this.cursorAddress = this.baseAddress + position;
        this.limitAddress = this.baseAddress + newSize;
    }

}
