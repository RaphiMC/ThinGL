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

package net.raphimc.thingl.drawbuilder.databuilder.writer;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public abstract class BufferVertexDataWriter<T extends BufferVertexDataWriter<T>> extends BufferDataWriter<T> {

    public BufferVertexDataWriter(final BufferBuilder bufferBuilder) {
        super(bufferBuilder);
    }

    public T position(final Matrix4f positionMatrix, final float x, final float y, final float z) {
        if ((positionMatrix.properties() & Matrix4fc.PROPERTY_IDENTITY) != 0) {
            return this.position(x, y, z);
        } else {
            final Vector3f vector3f = positionMatrix.transformPosition(new Vector3f(x, y, z));
            return this.position(vector3f.x, vector3f.y, vector3f.z);
        }
    }

    public T position(final float x, final float y, final float z) {
        this.bufferBuilder.putVec3f(x, y, z);
        return (T) this;
    }

    public T halfPosition(final Matrix4f positionMatrix, final float x, final float y, final float z) {
        if ((positionMatrix.properties() & Matrix4fc.PROPERTY_IDENTITY) != 0) {
            return this.halfPosition(x, y, z);
        } else {
            final Vector3f vector3f = positionMatrix.transformPosition(new Vector3f(x, y, z));
            return this.halfPosition(vector3f.x, vector3f.y, vector3f.z);
        }
    }

    public T halfPosition(final float x, final float y, final float z) {
        this.bufferBuilder.putHalfFloat(x).putHalfFloat(y).putHalfFloat(z);
        return (T) this;
    }

    public T color(final int r, final int g, final int b, final int a) {
        return this.color(a << 24 | b << 16 | g << 8 | r);
    }

    public T color(final Color color) {
        return this.color(color.toABGR());
    }

    public T color(final int abgr) {
        this.bufferBuilder.putInt(abgr);
        return (T) this;
    }

    public T texture(final float u, final float v) {
        this.bufferBuilder.putFloat(u).putFloat(v);
        return (T) this;
    }

    public T rawByte(final byte b) {
        this.bufferBuilder.putByte(b);
        return (T) this;
    }

    public T rawShort(final short s) {
        this.bufferBuilder.putShort(s);
        return (T) this;
    }

    public T rawHalfFloat(final float f) {
        this.bufferBuilder.putHalfFloat(f);
        return (T) this;
    }

}
