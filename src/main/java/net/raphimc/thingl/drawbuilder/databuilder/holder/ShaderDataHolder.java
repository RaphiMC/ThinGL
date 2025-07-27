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
package net.raphimc.thingl.drawbuilder.databuilder.holder;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import net.raphimc.thingl.drawbuilder.databuilder.writer.BufferDataWriter;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public abstract class ShaderDataHolder extends BufferDataWriter<ShaderDataHolder> {

    private static final int STRUCT_DATA_CALCULATED_MAX_MEMBER_ALIGNMENT = 0;
    private static final int STRUCT_DATA_GIVEN_MAX_MEMBER_ALIGNMENT = 1;
    private static final int STRUCT_DATA_START_POSITION = 2;

    protected final BooleanStack inArrayStack = new BooleanArrayList(4);
    protected final Stack<int[]> structDataStack = new ObjectArrayList<>(4);

    public ShaderDataHolder(final BufferBuilder bufferBuilder) {
        super(bufferBuilder);
    }

    @Override
    public ShaderDataHolder putInt(final int i) {
        this.bufferBuilder.align(Integer.BYTES);
        super.putInt(i);
        this.trackStructMaxMemberAlignment(Integer.BYTES);
        return this;
    }

    @Override
    public ShaderDataHolder putFloat(final float f) {
        this.bufferBuilder.align(Float.BYTES);
        super.putFloat(f);
        this.trackStructMaxMemberAlignment(Float.BYTES);
        return this;
    }

    @Override
    public ShaderDataHolder putDouble(final double d) {
        this.bufferBuilder.align(Double.BYTES);
        super.putDouble(d);
        this.trackStructMaxMemberAlignment(Double.BYTES);
        return this;
    }

    @Override
    public ShaderDataHolder putVector2i(final int x, final int y) {
        this.bufferBuilder.align(Integer.BYTES * 2);
        super.putVector2i(x, y);
        this.trackStructMaxMemberAlignment(Integer.BYTES * 2);
        return this;
    }

    @Override
    public ShaderDataHolder putVector3i(final int x, final int y, final int z) {
        this.bufferBuilder.align(Integer.BYTES * 4);
        super.putVector3i(x, y, z);
        this.trackStructMaxMemberAlignment(Integer.BYTES * 4);
        return this;
    }

    @Override
    public ShaderDataHolder putVector4i(final int x, final int y, final int z, final int w) {
        this.bufferBuilder.align(Integer.BYTES * 4);
        super.putVector4i(x, y, z, w);
        this.trackStructMaxMemberAlignment(Integer.BYTES * 4);
        return this;
    }

    @Override
    public ShaderDataHolder putVector2f(final float x, final float y) {
        this.bufferBuilder.align(Float.BYTES * 2);
        super.putVector2f(x, y);
        this.trackStructMaxMemberAlignment(Float.BYTES * 2);
        return this;
    }

    @Override
    public ShaderDataHolder putVector3f(final float x, final float y, final float z) {
        this.bufferBuilder.align(Float.BYTES * 4);
        super.putVector3f(x, y, z);
        this.trackStructMaxMemberAlignment(Float.BYTES * 4);
        return this;
    }

    @Override
    public ShaderDataHolder putVector4f(final float x, final float y, final float z, final float w) {
        this.bufferBuilder.align(Float.BYTES * 4);
        super.putVector4f(x, y, z, w);
        this.trackStructMaxMemberAlignment(Float.BYTES * 4);
        return this;
    }

    @Override
    public ShaderDataHolder putVector2d(final double x, final double y) {
        this.bufferBuilder.align(Double.BYTES * 2);
        super.putVector2d(x, y);
        this.trackStructMaxMemberAlignment(Double.BYTES * 2);
        return this;
    }

    @Override
    public ShaderDataHolder putVector3d(final double x, final double y, final double z) {
        this.bufferBuilder.align(Double.BYTES * 4);
        super.putVector3d(x, y, z);
        this.trackStructMaxMemberAlignment(Double.BYTES * 4);
        return this;
    }

    @Override
    public ShaderDataHolder putVector4d(final double x, final double y, final double z, final double w) {
        this.bufferBuilder.align(Double.BYTES * 4);
        super.putVector4d(x, y, z, w);
        this.trackStructMaxMemberAlignment(Double.BYTES * 4);
        return this;
    }

    public ShaderDataHolder putMatrix3f(final Matrix3f matrix) {
        this.inArrayStack.push(true);
        this.putVector3f(matrix.m00, matrix.m01, matrix.m02);
        this.putVector3f(matrix.m10, matrix.m11, matrix.m12);
        this.putVector3f(matrix.m20, matrix.m21, matrix.m22);
        this.inArrayStack.popBoolean();
        return this;
    }

    public ShaderDataHolder putMatrix4f(final Matrix4f matrix) {
        this.bufferBuilder.align(Float.BYTES * 4);
        this.bufferBuilder.putMatrix4f(matrix);
        this.trackStructMaxMemberAlignment(Float.BYTES * 4);
        return this;
    }

    public ShaderDataHolder beginArray() {
        if (!this.inArrayStack.isEmpty() && this.inArrayStack.topBoolean()) {
            throw new IllegalStateException("Already in an array");
        }
        this.inArrayStack.push(true);
        return this;
    }

    public ShaderDataHolder endArray() {
        if (this.inArrayStack.isEmpty() || !this.inArrayStack.topBoolean()) {
            throw new IllegalStateException("Not in an array");
        }
        this.inArrayStack.popBoolean();
        return this;
    }

    public ShaderDataHolder ensureInTopLevelArray() {
        if (this.inArrayStack.isEmpty()) {
            this.inArrayStack.push(true);
        } else if (!this.inArrayStack.topBoolean()) {
            throw new IllegalStateException("Not in top level array");
        }
        return this;
    }

    public ShaderDataHolder beginStruct(final int maxMemberAlignment) {
        if (!this.inArrayStack.isEmpty()) {
            this.inArrayStack.push(false);
        }
        this.bufferBuilder.align(this.getStructAlignment(maxMemberAlignment));
        this.structDataStack.push(new int[]{0, maxMemberAlignment, this.bufferBuilder.getPosition()});
        return this;
    }

    public ShaderDataHolder endStruct() {
        if (this.structDataStack.isEmpty()) {
            throw new IllegalStateException("Not in a struct");
        }
        if (!this.inArrayStack.isEmpty()) {
            this.inArrayStack.popBoolean();
        }
        final int[] structData = this.structDataStack.pop();
        if (structData[STRUCT_DATA_CALCULATED_MAX_MEMBER_ALIGNMENT] != structData[STRUCT_DATA_GIVEN_MAX_MEMBER_ALIGNMENT]) {
            throw new IllegalStateException("Struct max member alignment mismatch. Calculated " + structData[STRUCT_DATA_CALCULATED_MAX_MEMBER_ALIGNMENT] + " but got " + structData[STRUCT_DATA_GIVEN_MAX_MEMBER_ALIGNMENT]);
        }
        this.bufferBuilder.align(this.getStructAlignment(structData[STRUCT_DATA_GIVEN_MAX_MEMBER_ALIGNMENT]));
        this.trackStructMaxMemberAlignment(structData[STRUCT_DATA_GIVEN_MAX_MEMBER_ALIGNMENT]);
        return this;
    }

    public int endStructAndGetTopLevelArrayIndex() {
        if (this.structDataStack.isEmpty() || this.inArrayStack.isEmpty()) {
            throw new IllegalStateException("Not in a struct or not in an array");
        }
        final int startPosition = this.structDataStack.top()[STRUCT_DATA_START_POSITION];
        this.endStruct();
        final int structSize = this.bufferBuilder.getPosition() - startPosition;
        return (this.bufferBuilder.getPosition() / structSize) - 1;
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
