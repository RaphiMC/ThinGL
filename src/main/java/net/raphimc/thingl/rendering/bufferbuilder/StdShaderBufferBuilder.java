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
package net.raphimc.thingl.rendering.bufferbuilder;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.raphimc.thingl.memory.MemoryBuffer;
import net.raphimc.thingl.resource.memory.Memory;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public abstract class StdShaderBufferBuilder extends ShaderBufferBuilder {

    private static final int STRUCT_DATA_CALCULATED_MAX_MEMBER_ALIGNMENT = 0;
    private static final int STRUCT_DATA_GIVEN_MAX_MEMBER_ALIGNMENT = 1;

    protected final Stack<int[]> structDataStack = new ObjectArrayList<>(2);

    public StdShaderBufferBuilder(final MemoryBuffer memoryBuffer) {
        super(memoryBuffer);
    }

    @Override
    public ShaderBufferBuilder writeInt(final int i) {
        this.memoryBuffer.alignWritePosition(Memory.INT_SIZE);
        super.writeInt(i);
        this.trackStructMaxMemberAlignment(Memory.INT_SIZE);
        return this;
    }

    @Override
    public ShaderBufferBuilder writeFloat(final float f) {
        this.memoryBuffer.alignWritePosition(Memory.FLOAT_SIZE);
        super.writeFloat(f);
        this.trackStructMaxMemberAlignment(Memory.FLOAT_SIZE);
        return this;
    }

    @Override
    public ShaderBufferBuilder writeDouble(final double d) {
        this.memoryBuffer.alignWritePosition(Memory.DOUBLE_SIZE);
        super.writeDouble(d);
        this.trackStructMaxMemberAlignment(Memory.DOUBLE_SIZE);
        return this;
    }

    @Override
    public ShaderBufferBuilder writeVector2i(final int x, final int y) {
        this.memoryBuffer.alignWritePosition(Memory.VECTOR2I_SIZE);
        super.writeVector2i(x, y);
        this.trackStructMaxMemberAlignment(Memory.VECTOR2I_SIZE);
        return this;
    }

    @Override
    public ShaderBufferBuilder writeVector3i(final int x, final int y, final int z) {
        this.memoryBuffer.alignWritePosition(Memory.VECTOR4I_SIZE);
        super.writeVector3i(x, y, z);
        this.trackStructMaxMemberAlignment(Memory.VECTOR4I_SIZE);
        return this;
    }

    @Override
    public ShaderBufferBuilder writeVector4i(final int x, final int y, final int z, final int w) {
        this.memoryBuffer.alignWritePosition(Memory.VECTOR4I_SIZE);
        super.writeVector4i(x, y, z, w);
        this.trackStructMaxMemberAlignment(Memory.VECTOR4I_SIZE);
        return this;
    }

    @Override
    public ShaderBufferBuilder writeVector2f(final float x, final float y) {
        this.memoryBuffer.alignWritePosition(Memory.VECTOR2F_SIZE);
        super.writeVector2f(x, y);
        this.trackStructMaxMemberAlignment(Memory.VECTOR2F_SIZE);
        return this;
    }

    @Override
    public ShaderBufferBuilder writeVector3f(final float x, final float y, final float z) {
        this.memoryBuffer.alignWritePosition(Memory.VECTOR4F_SIZE);
        super.writeVector3f(x, y, z);
        this.trackStructMaxMemberAlignment(Memory.VECTOR4F_SIZE);
        return this;
    }

    @Override
    public ShaderBufferBuilder writeVector4f(final float x, final float y, final float z, final float w) {
        this.memoryBuffer.alignWritePosition(Memory.VECTOR4F_SIZE);
        super.writeVector4f(x, y, z, w);
        this.trackStructMaxMemberAlignment(Memory.VECTOR4F_SIZE);
        return this;
    }

    @Override
    public ShaderBufferBuilder writeVector2d(final double x, final double y) {
        this.memoryBuffer.alignWritePosition(Memory.VECTOR2D_SIZE);
        super.writeVector2d(x, y);
        this.trackStructMaxMemberAlignment(Memory.VECTOR2D_SIZE);
        return this;
    }

    @Override
    public ShaderBufferBuilder writeVector3d(final double x, final double y, final double z) {
        this.memoryBuffer.alignWritePosition(Memory.VECTOR4D_SIZE);
        super.writeVector3d(x, y, z);
        this.trackStructMaxMemberAlignment(Memory.VECTOR4D_SIZE);
        return this;
    }

    @Override
    public ShaderBufferBuilder writeVector4d(final double x, final double y, final double z, final double w) {
        this.memoryBuffer.alignWritePosition(Memory.VECTOR4D_SIZE);
        super.writeVector4d(x, y, z, w);
        this.trackStructMaxMemberAlignment(Memory.VECTOR4D_SIZE);
        return this;
    }

    public ShaderBufferBuilder writeMatrix3f(final Matrix3f matrix) {
        this.inArrayStack.push(true);
        this.writeVector3f(matrix.m00, matrix.m01, matrix.m02);
        this.writeVector3f(matrix.m10, matrix.m11, matrix.m12);
        this.writeVector3f(matrix.m20, matrix.m21, matrix.m22);
        this.inArrayStack.popBoolean();
        return this;
    }

    public ShaderBufferBuilder writeMatrix4f(final Matrix4f matrix) {
        this.memoryBuffer.alignWritePosition(Memory.MATRIX4F_SIZE);
        this.memoryBuffer.writeMatrix4f(matrix);
        this.trackStructMaxMemberAlignment(Memory.MATRIX4F_SIZE);
        return this;
    }

    @Override
    public ShaderBufferBuilder beginStruct(final int maxMemberAlignment) {
        super.beginStruct(maxMemberAlignment);
        this.memoryBuffer.alignWritePosition(this.getStructAlignment(maxMemberAlignment));
        this.structDataStack.push(new int[]{0, maxMemberAlignment});
        return this;
    }

    @Override
    public ShaderBufferBuilder endStruct() {
        super.endStruct();
        final int[] structData = this.structDataStack.pop();
        if (structData[STRUCT_DATA_CALCULATED_MAX_MEMBER_ALIGNMENT] != structData[STRUCT_DATA_GIVEN_MAX_MEMBER_ALIGNMENT]) {
            throw new IllegalStateException("Struct max member alignment mismatch. Calculated " + structData[STRUCT_DATA_CALCULATED_MAX_MEMBER_ALIGNMENT] + " but got " + structData[STRUCT_DATA_GIVEN_MAX_MEMBER_ALIGNMENT]);
        }
        this.memoryBuffer.alignWritePosition(this.getStructAlignment(structData[STRUCT_DATA_GIVEN_MAX_MEMBER_ALIGNMENT]));
        this.trackStructMaxMemberAlignment(structData[STRUCT_DATA_GIVEN_MAX_MEMBER_ALIGNMENT]);
        return this;
    }

    protected abstract int getStructAlignment(final int maxMemberAlignment);

    protected void trackStructMaxMemberAlignment(final int alignment) {
        if (!this.structDataStack.isEmpty()) {
            final int[] structData = this.structDataStack.top();
            if (structData[STRUCT_DATA_CALCULATED_MAX_MEMBER_ALIGNMENT] < alignment) {
                structData[STRUCT_DATA_CALCULATED_MAX_MEMBER_ALIGNMENT] = alignment;
            }
        }
    }

}
