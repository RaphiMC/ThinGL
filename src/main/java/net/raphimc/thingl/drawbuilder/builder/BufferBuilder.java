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

import net.lenni0451.commons.math.MathUtils;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.util.MathUtil;
import org.joml.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.lang.Math;
import java.nio.Buffer;
import java.nio.ByteBuffer;

@SuppressWarnings("PointlessArithmeticExpression")
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
        if (this.limitAddress - this.cursorAddress < Byte.BYTES) {
            this.ensureHasEnoughSpace(Byte.BYTES);
        }
        MemoryUtil.memPutByte(this.cursorAddress, b);
        this.cursorAddress += Byte.BYTES;
        return this;
    }

    public BufferBuilder putShort(final short s) {
        if (this.limitAddress - this.cursorAddress < Short.BYTES) {
            this.ensureHasEnoughSpace(Short.BYTES);
        }
        MemoryUtil.memPutShort(this.cursorAddress, s);
        this.cursorAddress += Short.BYTES;
        return this;
    }

    public BufferBuilder putInt(final int i) {
        if (this.limitAddress - this.cursorAddress < Integer.BYTES) {
            this.ensureHasEnoughSpace(Integer.BYTES);
        }
        MemoryUtil.memPutInt(this.cursorAddress, i);
        this.cursorAddress += Integer.BYTES;
        return this;
    }

    public BufferBuilder putFloat(final float f) {
        if (this.limitAddress - this.cursorAddress < Float.BYTES) {
            this.ensureHasEnoughSpace(Float.BYTES);
        }
        MemoryUtil.memPutFloat(this.cursorAddress, f);
        this.cursorAddress += Float.BYTES;
        return this;
    }

    public BufferBuilder putDouble(final double d) {
        if (this.limitAddress - this.cursorAddress < Double.BYTES) {
            this.ensureHasEnoughSpace(Double.BYTES);
        }
        MemoryUtil.memPutDouble(this.cursorAddress, d);
        this.cursorAddress += Double.BYTES;
        return this;
    }

    public BufferBuilder putHalfFloat(final float f) {
        return this.putShort(MathUtil.encodeHalfFloat(f));
    }

    public BufferBuilder putVector2i(final Vector2i vector) {
        return this.putVector2i(vector.x, vector.y);
    }

    public BufferBuilder putVector2i(final int x, final int y) {
        if (this.limitAddress - this.cursorAddress < Integer.BYTES * 2) {
            this.ensureHasEnoughSpace(Integer.BYTES * 2);
        }
        MemoryUtil.memPutInt(this.cursorAddress + Integer.BYTES * 0, x);
        MemoryUtil.memPutInt(this.cursorAddress + Integer.BYTES * 1, y);
        this.cursorAddress += Integer.BYTES * 2;
        return this;
    }

    public BufferBuilder putVector3i(final Vector3i vector) {
        return this.putVector3i(vector.x, vector.y, vector.z);
    }

    public BufferBuilder putVector3i(final int x, final int y, final int z) {
        if (this.limitAddress - this.cursorAddress < Integer.BYTES * 3) {
            this.ensureHasEnoughSpace(Integer.BYTES * 3);
        }
        MemoryUtil.memPutInt(this.cursorAddress + Integer.BYTES * 0, x);
        MemoryUtil.memPutInt(this.cursorAddress + Integer.BYTES * 1, y);
        MemoryUtil.memPutInt(this.cursorAddress + Integer.BYTES * 2, z);
        this.cursorAddress += Integer.BYTES * 3;
        return this;
    }

    public BufferBuilder putVector4i(final Vector4i vector) {
        return this.putVector4i(vector.x, vector.y, vector.z, vector.w);
    }

    public BufferBuilder putVector4i(final int x, final int y, final int z, final int w) {
        if (this.limitAddress - this.cursorAddress < Integer.BYTES * 4) {
            this.ensureHasEnoughSpace(Integer.BYTES * 4);
        }
        MemoryUtil.memPutInt(this.cursorAddress + Integer.BYTES * 0, x);
        MemoryUtil.memPutInt(this.cursorAddress + Integer.BYTES * 1, y);
        MemoryUtil.memPutInt(this.cursorAddress + Integer.BYTES * 2, z);
        MemoryUtil.memPutInt(this.cursorAddress + Integer.BYTES * 3, w);
        this.cursorAddress += Integer.BYTES * 4;
        return this;
    }

    public BufferBuilder putVector2f(final Vector2f vector) {
        return this.putVector2f(vector.x, vector.y);
    }

    public BufferBuilder putVector2f(final float x, final float y) {
        if (this.limitAddress - this.cursorAddress < Float.BYTES * 2) {
            this.ensureHasEnoughSpace(Float.BYTES * 2);
        }
        MemoryUtil.memPutFloat(this.cursorAddress + Float.BYTES * 0, x);
        MemoryUtil.memPutFloat(this.cursorAddress + Float.BYTES * 1, y);
        this.cursorAddress += Float.BYTES * 2;
        return this;
    }

    public BufferBuilder putVector3f(final Vector3f vector) {
        return this.putVector3f(vector.x, vector.y, vector.z);
    }

    public BufferBuilder putVector3f(final float x, final float y, final float z) {
        if (this.limitAddress - this.cursorAddress < Float.BYTES * 3) {
            this.ensureHasEnoughSpace(Float.BYTES * 3);
        }
        MemoryUtil.memPutFloat(this.cursorAddress + Float.BYTES * 0, x);
        MemoryUtil.memPutFloat(this.cursorAddress + Float.BYTES * 1, y);
        MemoryUtil.memPutFloat(this.cursorAddress + Float.BYTES * 2, z);
        this.cursorAddress += Float.BYTES * 3;
        return this;
    }

    public BufferBuilder putVector4f(final Vector4f vector) {
        return this.putVector4f(vector.x, vector.y, vector.z, vector.w);
    }

    public BufferBuilder putVector4f(final float x, final float y, final float z, final float w) {
        if (this.limitAddress - this.cursorAddress < Float.BYTES * 4) {
            this.ensureHasEnoughSpace(Float.BYTES * 4);
        }
        MemoryUtil.memPutFloat(this.cursorAddress + Float.BYTES * 0, x);
        MemoryUtil.memPutFloat(this.cursorAddress + Float.BYTES * 1, y);
        MemoryUtil.memPutFloat(this.cursorAddress + Float.BYTES * 2, z);
        MemoryUtil.memPutFloat(this.cursorAddress + Float.BYTES * 3, w);
        this.cursorAddress += Float.BYTES * 4;
        return this;
    }

    public BufferBuilder putVector2d(final Vector2d vector) {
        return this.putVector2d(vector.x, vector.y);
    }

    public BufferBuilder putVector2d(final double x, final double y) {
        if (this.limitAddress - this.cursorAddress < Double.BYTES * 2) {
            this.ensureHasEnoughSpace(Double.BYTES * 2);
        }
        MemoryUtil.memPutDouble(this.cursorAddress + Double.BYTES * 0, x);
        MemoryUtil.memPutDouble(this.cursorAddress + Double.BYTES * 1, y);
        this.cursorAddress += Double.BYTES * 2;
        return this;
    }

    public BufferBuilder putVector3d(final Vector3d vector) {
        return this.putVector3d(vector.x, vector.y, vector.z);
    }

    public BufferBuilder putVector3d(final double x, final double y, final double z) {
        if (this.limitAddress - this.cursorAddress < Double.BYTES * 3) {
            this.ensureHasEnoughSpace(Double.BYTES * 3);
        }
        MemoryUtil.memPutDouble(this.cursorAddress + Double.BYTES * 0, x);
        MemoryUtil.memPutDouble(this.cursorAddress + Double.BYTES * 1, y);
        MemoryUtil.memPutDouble(this.cursorAddress + Double.BYTES * 2, z);
        this.cursorAddress += Double.BYTES * 3;
        return this;
    }

    public BufferBuilder putVector4d(final Vector4d vector) {
        return this.putVector4d(vector.x, vector.y, vector.z, vector.w);
    }

    public BufferBuilder putVector4d(final double x, final double y, final double z, final double w) {
        if (this.limitAddress - this.cursorAddress < Double.BYTES * 4) {
            this.ensureHasEnoughSpace(Double.BYTES * 4);
        }
        MemoryUtil.memPutDouble(this.cursorAddress + Double.BYTES * 0, x);
        MemoryUtil.memPutDouble(this.cursorAddress + Double.BYTES * 1, y);
        MemoryUtil.memPutDouble(this.cursorAddress + Double.BYTES * 2, z);
        MemoryUtil.memPutDouble(this.cursorAddress + Double.BYTES * 3, w);
        this.cursorAddress += Double.BYTES * 4;
        return this;
    }

    public BufferBuilder putMatrix3f(final Matrix3f matrix) {
        if (this.limitAddress - this.cursorAddress < Float.BYTES * 3 * 3) {
            this.ensureHasEnoughSpace(Float.BYTES * 3 * 3);
        }
        if (ThinGL.capabilities().supportsJomlUnsafe()) {
            matrix.getToAddress(this.cursorAddress);
        } else {
            matrix.get(MemoryUtil.memFloatBuffer(this.cursorAddress, 3 * 3));
        }
        this.cursorAddress += Float.BYTES * 3 * 3;
        return this;
    }

    public BufferBuilder putMatrix4f(final Matrix4f matrix) {
        if (this.limitAddress - this.cursorAddress < Float.BYTES * 4 * 4) {
            this.ensureHasEnoughSpace(Float.BYTES * 4 * 4);
        }
        if (ThinGL.capabilities().supportsJomlUnsafe()) {
            matrix.getToAddress(this.cursorAddress);
        } else {
            matrix.get(MemoryUtil.memFloatBuffer(this.cursorAddress, 4 * 4));
        }
        this.cursorAddress += Float.BYTES * 4 * 4;
        return this;
    }

    public BufferBuilder putMatrix3d(final Matrix3d matrix) {
        if (this.limitAddress - this.cursorAddress < Double.BYTES * 3 * 3) {
            this.ensureHasEnoughSpace(Double.BYTES * 3 * 3);
        }
        if (ThinGL.capabilities().supportsJomlUnsafe()) {
            matrix.getToAddress(this.cursorAddress);
        } else {
            matrix.get(MemoryUtil.memDoubleBuffer(this.cursorAddress, 3 * 3));
        }
        this.cursorAddress += Double.BYTES * 3 * 3;
        return this;
    }

    public BufferBuilder putMatrix4d(final Matrix4d matrix) {
        if (this.limitAddress - this.cursorAddress < Double.BYTES * 4 * 4) {
            this.ensureHasEnoughSpace(Double.BYTES * 4 * 4);
        }
        if (ThinGL.capabilities().supportsJomlUnsafe()) {
            matrix.getToAddress(this.cursorAddress);
        } else {
            matrix.get(MemoryUtil.memDoubleBuffer(this.cursorAddress, 4 * 4));
        }
        this.cursorAddress += Double.BYTES * 4 * 4;
        return this;
    }

    public BufferBuilder align(final int alignment) {
        final int position = this.getPosition();
        final int alignedPosition = MathUtils.align(position, alignment);
        return this.skip(alignedPosition - position);
    }

    public BufferBuilder skip(final int bytes) {
        if (bytes != 0) {
            this.ensureHasEnoughSpace(bytes);
            this.cursorAddress += bytes;
        }
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
                this.resize(MathUtils.align(oldSize + Math.max(bytes, oldSize), GROW_ALIGNMENT));
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
        if (cursorAddress < this.baseAddress || cursorAddress > this.limitAddress) {
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
