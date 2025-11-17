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

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanStack;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongStack;
import net.raphimc.thingl.memory.MemoryBuffer;

public abstract class ShaderBufferBuilder extends DataBufferBuilder<ShaderBufferBuilder> {

    protected final BooleanStack inArrayStack = new BooleanArrayList(2);
    protected final LongStack structStartPositionStack = new LongArrayList(2);

    public ShaderBufferBuilder(final MemoryBuffer memoryBuffer) {
        super(memoryBuffer);
    }

    public ShaderBufferBuilder beginArray() {
        if (!this.inArrayStack.isEmpty() && this.inArrayStack.topBoolean()) {
            throw new IllegalStateException("Already in an array");
        }
        this.inArrayStack.push(true);
        return this;
    }

    public ShaderBufferBuilder endArray() {
        if (this.inArrayStack.isEmpty() || !this.inArrayStack.topBoolean()) {
            throw new IllegalStateException("Not in an array");
        }
        this.inArrayStack.popBoolean();
        return this;
    }

    public ShaderBufferBuilder ensureInTopLevelArray() {
        if (this.inArrayStack.isEmpty()) {
            this.inArrayStack.push(true);
        } else if (!this.inArrayStack.topBoolean()) {
            throw new IllegalStateException("Not in top level array");
        }
        return this;
    }

    public ShaderBufferBuilder beginStruct(final int maxMemberAlignment) {
        if (!this.inArrayStack.isEmpty()) {
            this.inArrayStack.push(false);
        }
        this.structStartPositionStack.push(this.memoryBuffer.getWritePosition());
        return this;
    }

    public ShaderBufferBuilder endStruct() {
        if (this.structStartPositionStack.isEmpty()) {
            throw new IllegalStateException("Not in a struct");
        }
        this.structStartPositionStack.popLong();
        if (!this.inArrayStack.isEmpty()) {
            this.inArrayStack.popBoolean();
        }
        return this;
    }

    public int endStructAndGetTopLevelArrayIndex() {
        if (this.structStartPositionStack.isEmpty() || this.inArrayStack.isEmpty()) {
            throw new IllegalStateException("Not in a struct or not in an array");
        }
        final long startPosition = this.structStartPositionStack.topLong();
        this.endStruct();
        final long structSize = this.memoryBuffer.getWritePosition() - startPosition;
        return (int) ((this.memoryBuffer.getWritePosition() / structSize) - 1);
    }

}
