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
package net.raphimc.thingl.rendering.bufferbuilder;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.memory.MemoryBuffer;
import org.joml.*;
import org.joml.Math;

public abstract class DataBufferBuilder<T extends DataBufferBuilder<T>> extends BufferBuilder<T> {

    public DataBufferBuilder(final MemoryBuffer memoryBuffer) {
        super(memoryBuffer);
    }

    public T writeInt(final int i) {
        this.memoryBuffer.writeInt(i);
        return (T) this;
    }

    public T writeFloat(final float f) {
        this.memoryBuffer.writeFloat(f);
        return (T) this;
    }

    public T writeDouble(final double d) {
        this.memoryBuffer.writeDouble(d);
        return (T) this;
    }

    public T writeVector2i(final Vector2i vector) {
        return this.writeVector2i(vector.x, vector.y);
    }

    public T writeVector2i(final int x, final int y) {
        this.memoryBuffer.writeVector2i(x, y);
        return (T) this;
    }

    public T writeVector3i(final Vector3i vector) {
        return this.writeVector3i(vector.x, vector.y, vector.z);
    }

    public T writeVector3i(final int x, final int y, final int z) {
        this.memoryBuffer.writeVector3i(x, y, z);
        return (T) this;
    }

    public T writeVector4i(final Vector4i vector) {
        return this.writeVector4i(vector.x, vector.y, vector.z, vector.w);
    }

    public T writeVector4i(final int x, final int y, final int z, final int w) {
        this.memoryBuffer.writeVector4i(x, y, z, w);
        return (T) this;
    }

    public T writeVector2f(final Vector2f vector) {
        return this.writeVector2f(vector.x, vector.y);
    }

    public T writeVector2f(final float x, final float y) {
        this.memoryBuffer.writeVector2f(x, y);
        return (T) this;
    }

    public T writeVector3f(final Vector3f vector) {
        return this.writeVector3f(vector.x, vector.y, vector.z);
    }

    public T writeVector3f(final float x, final float y, final float z) {
        this.memoryBuffer.writeVector3f(x, y, z);
        return (T) this;
    }

    public T writeVector4f(final Vector4f vector) {
        return this.writeVector4f(vector.x, vector.y, vector.z, vector.w);
    }

    public T writeVector4f(final float x, final float y, final float z, final float w) {
        this.memoryBuffer.writeVector4f(x, y, z, w);
        return (T) this;
    }

    public T writeVector2d(final Vector2d vector) {
        return this.writeVector2d(vector.x, vector.y);
    }

    public T writeVector2d(final double x, final double y) {
        this.memoryBuffer.writeVector2d(x, y);
        return (T) this;
    }

    public T writeVector3d(final Vector3d vector) {
        return this.writeVector3d(vector.x, vector.y, vector.z);
    }

    public T writeVector3d(final double x, final double y, final double z) {
        this.memoryBuffer.writeVector3d(x, y, z);
        return (T) this;
    }

    public T writeVector4d(final Vector4d vector) {
        return this.writeVector4d(vector.x, vector.y, vector.z, vector.w);
    }

    public T writeVector4d(final double x, final double y, final double z, final double w) {
        this.memoryBuffer.writeVector4d(x, y, z, w);
        return (T) this;
    }

    public T writeVector3f(final Matrix4f positionMatrix, final Vector3f vector) {
        return this.writeVector3f(positionMatrix, vector.x, vector.y, vector.z);
    }

    public T writeVector3f(final Matrix4f positionMatrix, final float x, final float y, final float z) {
        // Code from Vector3f#mulPosition
        if ((positionMatrix.properties() & Matrix4fc.PROPERTY_IDENTITY) != 0) {
            return this.writeVector3f(x, y, z);
        } else if ((positionMatrix.properties() & Matrix4fc.PROPERTY_TRANSLATION) != 0) {
            return this.writeVector3f(x + positionMatrix.m30(), y + positionMatrix.m31(), z + positionMatrix.m32());
        } else {
            return this.writeVector3f(
                    Math.fma(positionMatrix.m00(), x, Math.fma(positionMatrix.m10(), y, Math.fma(positionMatrix.m20(), z, positionMatrix.m30()))),
                    Math.fma(positionMatrix.m01(), x, Math.fma(positionMatrix.m11(), y, Math.fma(positionMatrix.m21(), z, positionMatrix.m31()))),
                    Math.fma(positionMatrix.m02(), x, Math.fma(positionMatrix.m12(), y, Math.fma(positionMatrix.m22(), z, positionMatrix.m32())))
            );
        }
    }

    public T writeColor(final int r, final int g, final int b, final int a) {
        return this.writeColor(a << 24 | b << 16 | g << 8 | r);
    }

    public T writeColor(final Color color) {
        return this.writeColor(color.toABGR());
    }

    public T writeColor(final int abgrColor) {
        return this.writeInt(abgrColor);
    }

}
