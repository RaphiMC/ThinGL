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

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanStack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import net.raphimc.thingl.drawbuilder.databuilder.writer.BufferDataWriter;

public abstract class ShaderDataHolder extends BufferDataWriter<ShaderDataHolder> {

    protected final BooleanStack inArrayStack = new BooleanArrayList(2);
    protected final IntStack structStartPositionStack = new IntArrayList(2);

    public ShaderDataHolder(final BufferBuilder bufferBuilder) {
        super(bufferBuilder);
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
        this.structStartPositionStack.push(this.bufferBuilder.getPosition());
        return this;
    }

    public ShaderDataHolder endStruct() {
        if (this.structStartPositionStack.isEmpty()) {
            throw new IllegalStateException("Not in a struct");
        }
        this.structStartPositionStack.popInt();
        if (!this.inArrayStack.isEmpty()) {
            this.inArrayStack.popBoolean();
        }
        return this;
    }

    public int endStructAndGetTopLevelArrayIndex() {
        if (this.structStartPositionStack.isEmpty() || this.inArrayStack.isEmpty()) {
            throw new IllegalStateException("Not in a struct or not in an array");
        }
        final int startPosition = this.structStartPositionStack.topInt();
        this.endStruct();
        final int structSize = this.bufferBuilder.getPosition() - startPosition;
        return (this.bufferBuilder.getPosition() / structSize) - 1;
    }

}
