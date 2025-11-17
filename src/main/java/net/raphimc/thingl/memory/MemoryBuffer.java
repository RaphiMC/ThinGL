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
package net.raphimc.thingl.memory;

import net.lenni0451.commons.math.MathUtils;
import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.resource.memory.Memory;
import org.joml.*;

import java.lang.Math;

public class MemoryBuffer {

    private static final long DEFAULT_INITIAL_SIZE = 64 * 1024L;
    private static final int GROW_ALIGNMENT = 1024;

    private Memory memory;
    private final boolean resizable;
    private long readPosition;
    private long writePosition;

    public MemoryBuffer() {
        this(DEFAULT_INITIAL_SIZE);
    }

    public MemoryBuffer(final long initialSize) {
        this.memory = MemoryAllocator.allocateMemory(initialSize);
        this.resizable = true;
    }

    public MemoryBuffer(final Memory memory) {
        this.memory = memory;
        this.resizable = false;
    }

    public byte readByte() {
        if (this.getRemainingReadable() < Memory.BYTE_SIZE) {
            this.ensureCanRead(Memory.BYTE_SIZE);
        }
        final byte b = this.memory.getByte(this.readPosition);
        this.readPosition += Memory.BYTE_SIZE;
        return b;
    }

    public MemoryBuffer writeByte(final byte b) {
        if (this.getRemainingWritable() < Memory.BYTE_SIZE) {
            this.ensureCanWrite(Memory.BYTE_SIZE);
        }
        this.memory.putByte(this.writePosition, b);
        this.writePosition += Memory.BYTE_SIZE;
        return this;
    }

    public short readShort() {
        if (this.getRemainingReadable() < Memory.SHORT_SIZE) {
            this.ensureCanRead(Memory.SHORT_SIZE);
        }
        final short s = this.memory.getShort(this.readPosition);
        this.readPosition += Memory.SHORT_SIZE;
        return s;
    }

    public MemoryBuffer writeShort(final short s) {
        if (this.getRemainingWritable() < Memory.SHORT_SIZE) {
            this.ensureCanWrite(Memory.SHORT_SIZE);
        }
        this.memory.putShort(this.writePosition, s);
        this.writePosition += Memory.SHORT_SIZE;
        return this;
    }

    public int readInt() {
        if (this.getRemainingReadable() < Memory.INT_SIZE) {
            this.ensureCanRead(Memory.INT_SIZE);
        }
        final int i = this.memory.getInt(this.readPosition);
        this.readPosition += Memory.INT_SIZE;
        return i;
    }

    public MemoryBuffer writeInt(final int i) {
        if (this.getRemainingWritable() < Memory.INT_SIZE) {
            this.ensureCanWrite(Memory.INT_SIZE);
        }
        this.memory.putInt(this.writePosition, i);
        this.writePosition += Memory.INT_SIZE;
        return this;
    }

    public long readLong() {
        if (this.getRemainingReadable() < Memory.LONG_SIZE) {
            this.ensureCanRead(Memory.LONG_SIZE);
        }
        final long l = this.memory.getLong(this.readPosition);
        this.readPosition += Memory.LONG_SIZE;
        return l;
    }

    public MemoryBuffer writeLong(final long l) {
        if (this.getRemainingWritable() < Memory.LONG_SIZE) {
            this.ensureCanWrite(Memory.LONG_SIZE);
        }
        this.memory.putLong(this.writePosition, l);
        this.writePosition += Memory.LONG_SIZE;
        return this;
    }

    public float readFloat() {
        if (this.getRemainingReadable() < Memory.FLOAT_SIZE) {
            this.ensureCanRead(Memory.FLOAT_SIZE);
        }
        final float f = this.memory.getFloat(this.readPosition);
        this.readPosition += Memory.FLOAT_SIZE;
        return f;
    }

    public MemoryBuffer writeFloat(final float f) {
        if (this.getRemainingWritable() < Memory.FLOAT_SIZE) {
            this.ensureCanWrite(Memory.FLOAT_SIZE);
        }
        this.memory.putFloat(this.writePosition, f);
        this.writePosition += Memory.FLOAT_SIZE;
        return this;
    }

    public double readDouble() {
        if (this.getRemainingReadable() < Memory.DOUBLE_SIZE) {
            this.ensureCanRead(Memory.DOUBLE_SIZE);
        }
        final double d = this.memory.getDouble(this.readPosition);
        this.readPosition += Memory.DOUBLE_SIZE;
        return d;
    }

    public MemoryBuffer writeDouble(final double d) {
        if (this.getRemainingWritable() < Memory.DOUBLE_SIZE) {
            this.ensureCanWrite(Memory.DOUBLE_SIZE);
        }
        this.memory.putDouble(this.writePosition, d);
        this.writePosition += Memory.DOUBLE_SIZE;
        return this;
    }

    public Vector2i readVector2i() {
        if (this.getRemainingReadable() < Memory.VECTOR2I_SIZE) {
            this.ensureCanRead(Memory.VECTOR2I_SIZE);
        }
        final Vector2i vector = this.memory.getVector2i(this.readPosition);
        this.readPosition += Memory.VECTOR2I_SIZE;
        return vector;
    }

    public MemoryBuffer writeVector2i(final Vector2i vector) {
        return this.writeVector2i(vector.x, vector.y);
    }

    public MemoryBuffer writeVector2i(final int x, final int y) {
        if (this.getRemainingWritable() < Memory.VECTOR2I_SIZE) {
            this.ensureCanWrite(Memory.VECTOR2I_SIZE);
        }
        this.memory.putVector2i(this.writePosition, x, y);
        this.writePosition += Memory.VECTOR2I_SIZE;
        return this;
    }

    public Vector3i readVector3i() {
        if (this.getRemainingReadable() < Memory.VECTOR3I_SIZE) {
            this.ensureCanRead(Memory.VECTOR3I_SIZE);
        }
        final Vector3i vector = this.memory.getVector3i(this.readPosition);
        this.readPosition += Memory.VECTOR3I_SIZE;
        return vector;
    }

    public MemoryBuffer writeVector3i(final Vector3i vector) {
        return this.writeVector3i(vector.x, vector.y, vector.z);
    }

    public MemoryBuffer writeVector3i(final int x, final int y, final int z) {
        if (this.getRemainingWritable() < Memory.VECTOR3I_SIZE) {
            this.ensureCanWrite(Memory.VECTOR3I_SIZE);
        }
        this.memory.putVector3i(this.writePosition, x, y, z);
        this.writePosition += Memory.VECTOR3I_SIZE;
        return this;
    }

    public Vector4i readVector4i() {
        if (this.getRemainingReadable() < Memory.VECTOR4I_SIZE) {
            this.ensureCanRead(Memory.VECTOR4I_SIZE);
        }
        final Vector4i vector = this.memory.getVector4i(this.readPosition);
        this.readPosition += Memory.VECTOR4I_SIZE;
        return vector;
    }

    public MemoryBuffer writeVector4i(final Vector4i vector) {
        return this.writeVector4i(vector.x, vector.y, vector.z, vector.w);
    }

    public MemoryBuffer writeVector4i(final int x, final int y, final int z, final int w) {
        if (this.getRemainingWritable() < Memory.VECTOR4I_SIZE) {
            this.ensureCanWrite(Memory.VECTOR4I_SIZE);
        }
        this.memory.putVector4i(this.writePosition, x, y, z, w);
        this.writePosition += Memory.VECTOR4I_SIZE;
        return this;
    }

    public Vector2f readVector2f() {
        if (this.getRemainingReadable() < Memory.VECTOR2F_SIZE) {
            this.ensureCanRead(Memory.VECTOR2F_SIZE);
        }
        final Vector2f vector = this.memory.getVector2f(this.readPosition);
        this.readPosition += Memory.VECTOR2F_SIZE;
        return vector;
    }

    public MemoryBuffer writeVector2f(final Vector2f vector) {
        return this.writeVector2f(vector.x, vector.y);
    }

    public MemoryBuffer writeVector2f(final float x, final float y) {
        if (this.getRemainingWritable() < Memory.VECTOR2F_SIZE) {
            this.ensureCanWrite(Memory.VECTOR2F_SIZE);
        }
        this.memory.putVector2f(this.writePosition, x, y);
        this.writePosition += Memory.VECTOR2F_SIZE;
        return this;
    }

    public Vector3f readVector3f() {
        if (this.getRemainingReadable() < Memory.VECTOR3F_SIZE) {
            this.ensureCanRead(Memory.VECTOR3F_SIZE);
        }
        final Vector3f vector = this.memory.getVector3f(this.readPosition);
        this.readPosition += Memory.VECTOR3F_SIZE;
        return vector;
    }

    public MemoryBuffer writeVector3f(final Vector3f vector) {
        return this.writeVector3f(vector.x, vector.y, vector.z);
    }

    public MemoryBuffer writeVector3f(final float x, final float y, final float z) {
        if (this.getRemainingWritable() < Memory.VECTOR3F_SIZE) {
            this.ensureCanWrite(Memory.VECTOR3F_SIZE);
        }
        this.memory.putVector3f(this.writePosition, x, y, z);
        this.writePosition += Memory.VECTOR3F_SIZE;
        return this;
    }

    public Vector4f readVector4f() {
        if (this.getRemainingReadable() < Memory.VECTOR4F_SIZE) {
            this.ensureCanRead(Memory.VECTOR4F_SIZE);
        }
        final Vector4f vector = this.memory.getVector4f(this.readPosition);
        this.readPosition += Memory.VECTOR4F_SIZE;
        return vector;
    }

    public MemoryBuffer writeVector4f(final Vector4f vector) {
        return this.writeVector4f(vector.x, vector.y, vector.z, vector.w);
    }

    public MemoryBuffer writeVector4f(final float x, final float y, final float z, final float w) {
        if (this.getRemainingWritable() < Memory.VECTOR4F_SIZE) {
            this.ensureCanWrite(Memory.VECTOR4F_SIZE);
        }
        this.memory.putVector4f(this.writePosition, x, y, z, w);
        this.writePosition += Memory.VECTOR4F_SIZE;
        return this;
    }

    public Vector2d readVector2d() {
        if (this.getRemainingReadable() < Memory.VECTOR2D_SIZE) {
            this.ensureCanRead(Memory.VECTOR2D_SIZE);
        }
        final Vector2d vector = this.memory.getVector2d(this.readPosition);
        this.readPosition += Memory.VECTOR2D_SIZE;
        return vector;
    }

    public MemoryBuffer writeVector2d(final Vector2d vector) {
        return this.writeVector2d(vector.x, vector.y);
    }

    public MemoryBuffer writeVector2d(final double x, final double y) {
        if (this.getRemainingWritable() < Memory.VECTOR2D_SIZE) {
            this.ensureCanWrite(Memory.VECTOR2D_SIZE);
        }
        this.memory.putVector2d(this.writePosition, x, y);
        this.writePosition += Memory.VECTOR2D_SIZE;
        return this;
    }

    public Vector3d readVector3d() {
        if (this.getRemainingReadable() < Memory.VECTOR3D_SIZE) {
            this.ensureCanRead(Memory.VECTOR3D_SIZE);
        }
        final Vector3d vector = this.memory.getVector3d(this.readPosition);
        this.readPosition += Memory.VECTOR3D_SIZE;
        return vector;
    }

    public MemoryBuffer writeVector3d(final Vector3d vector) {
        return this.writeVector3d(vector.x, vector.y, vector.z);
    }

    public MemoryBuffer writeVector3d(final double x, final double y, final double z) {
        if (this.getRemainingWritable() < Memory.VECTOR3D_SIZE) {
            this.ensureCanWrite(Memory.VECTOR3D_SIZE);
        }
        this.memory.putVector3d(this.writePosition, x, y, z);
        this.writePosition += Memory.VECTOR3D_SIZE;
        return this;
    }

    public Vector4d readVector4d() {
        if (this.getRemainingReadable() < Memory.VECTOR4D_SIZE) {
            this.ensureCanRead(Memory.VECTOR4D_SIZE);
        }
        final Vector4d vector = this.memory.getVector4d(this.readPosition);
        this.readPosition += Memory.VECTOR4D_SIZE;
        return vector;
    }

    public MemoryBuffer writeVector4d(final Vector4d vector) {
        return this.writeVector4d(vector.x, vector.y, vector.z, vector.w);
    }

    public MemoryBuffer writeVector4d(final double x, final double y, final double z, final double w) {
        if (this.getRemainingWritable() < Memory.VECTOR4D_SIZE) {
            this.ensureCanWrite(Memory.VECTOR4D_SIZE);
        }
        this.memory.putVector4d(this.writePosition, x, y, z, w);
        this.writePosition += Memory.VECTOR4D_SIZE;
        return this;
    }

    public Matrix3f readMatrix3f() {
        if (this.getRemainingReadable() < Memory.MATRIX3F_SIZE) {
            this.ensureCanRead(Memory.MATRIX3F_SIZE);
        }
        final Matrix3f matrix = this.memory.getMatrix3f(this.readPosition);
        this.readPosition += Memory.MATRIX3F_SIZE;
        return matrix;
    }

    public MemoryBuffer writeMatrix3f(final Matrix3f matrix) {
        if (this.getRemainingWritable() < Memory.MATRIX3F_SIZE) {
            this.ensureCanWrite(Memory.MATRIX3F_SIZE);
        }
        this.memory.putMatrix3f(this.writePosition, matrix);
        this.writePosition += Memory.MATRIX3F_SIZE;
        return this;
    }

    public Matrix4f readMatrix4f() {
        if (this.getRemainingReadable() < Memory.MATRIX4F_SIZE) {
            this.ensureCanRead(Memory.MATRIX4F_SIZE);
        }
        final Matrix4f matrix = this.memory.getMatrix4f(this.readPosition);
        this.readPosition += Memory.MATRIX4F_SIZE;
        return matrix;
    }

    public MemoryBuffer writeMatrix4f(final Matrix4f matrix) {
        if (this.getRemainingWritable() < Memory.MATRIX4F_SIZE) {
            this.ensureCanWrite(Memory.MATRIX4F_SIZE);
        }
        this.memory.putMatrix4f(this.writePosition, matrix);
        this.writePosition += Memory.MATRIX4F_SIZE;
        return this;
    }

    public Matrix3d readMatrix3d() {
        if (this.getRemainingReadable() < Memory.MATRIX3D_SIZE) {
            this.ensureCanRead(Memory.MATRIX3D_SIZE);
        }
        final Matrix3d matrix = this.memory.getMatrix3d(this.readPosition);
        this.readPosition += Memory.MATRIX3D_SIZE;
        return matrix;
    }

    public MemoryBuffer writeMatrix3d(final Matrix3d matrix) {
        if (this.getRemainingWritable() < Memory.MATRIX3D_SIZE) {
            this.ensureCanWrite(Memory.MATRIX3D_SIZE);
        }
        this.memory.putMatrix3d(this.writePosition, matrix);
        this.writePosition += Memory.MATRIX3D_SIZE;
        return this;
    }

    public Matrix4d readMatrix4d() {
        if (this.getRemainingReadable() < Memory.MATRIX4D_SIZE) {
            this.ensureCanRead(Memory.MATRIX4D_SIZE);
        }
        final Matrix4d matrix = this.memory.getMatrix4d(this.readPosition);
        this.readPosition += Memory.MATRIX4D_SIZE;
        return matrix;
    }

    public MemoryBuffer writeMatrix4d(final Matrix4d matrix) {
        if (this.getRemainingWritable() < Memory.MATRIX4D_SIZE) {
            this.ensureCanWrite(Memory.MATRIX4D_SIZE);
        }
        this.memory.putMatrix4d(this.writePosition, matrix);
        this.writePosition += Memory.MATRIX4D_SIZE;
        return this;
    }

    public MemoryBuffer writeMemory(final Memory sourceMemory) {
        if (this.getRemainingWritable() < sourceMemory.getSize()) {
            this.ensureCanWrite(sourceMemory.getSize());
        }
        sourceMemory.copyTo(this.memory, 0L, this.writePosition, sourceMemory.getSize());
        this.writePosition += sourceMemory.getSize();
        return this;
    }

    public MemoryBuffer alignReadPosition(final int alignment) {
        final long position = this.getReadPosition();
        final long alignedPosition = MathUtils.align(position, alignment);
        final long skipBytes = alignedPosition - position;
        if (skipBytes > 0) {
            this.ensureCanRead(skipBytes);
            this.readPosition += skipBytes;
        }
        return this;
    }

    public MemoryBuffer alignWritePosition(final int alignment) {
        final long position = this.getWritePosition();
        final long alignedPosition = MathUtils.align(position, alignment);
        final long skipBytes = alignedPosition - position;
        if (skipBytes > 0) {
            this.ensureCanWrite(skipBytes);
            this.writePosition += skipBytes;
        }
        return this;
    }

    public void ensureCanRead(final long bytes) {
        if (this.getWritePosition() - this.readPosition < bytes) {
            throw new IllegalStateException("Tried to read more data than is available in the buffer");
        }
    }

    public void ensureCanWrite(final long bytes) {
        if (this.getRemainingWritable() < bytes) {
            if (!this.resizable) {
                throw new IllegalStateException("Tried to write more data than the buffer can hold");
            }

            final long oldSize = this.getSize();
            final long newSize = MathUtils.align(oldSize + Math.max(bytes, oldSize), GROW_ALIGNMENT);
            this.memory = MemoryAllocator.reallocateMemory(this.memory, newSize);
        }
    }

    public Memory finish() {
        final Memory memory = this.memory.slice(0, this.getWritePosition());
        this.reset();
        return memory;
    }

    public void reset() {
        this.readPosition = 0L;
        this.writePosition = 0L;
    }

    public void free() {
        this.reset();
        this.memory.free();
        this.memory = null;
    }

    public Memory getMemory() {
        return this.memory;
    }

    public long getSize() {
        return this.memory.getSize();
    }

    public long getReadPosition() {
        return this.readPosition;
    }

    public void setReadPosition(final long readPosition) {
        if (readPosition < 0 || readPosition > this.getSize()) {
            throw new IllegalArgumentException("Read position is out of bounds");
        }
        this.readPosition = readPosition;
    }

    public long getWritePosition() {
        return this.writePosition;
    }

    public void setWritePosition(final long writePosition) {
        if (writePosition < 0 || writePosition > this.getSize()) {
            throw new IllegalArgumentException("Write position is out of bounds");
        }
        this.writePosition = writePosition;
    }

    public long getRemainingReadable() {
        return this.getSize() - this.getReadPosition();
    }

    public long getRemainingWritable() {
        return this.getSize() - this.getWritePosition();
    }

}
