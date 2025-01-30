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

package net.raphimc.thingl.drawbuilder;

import net.raphimc.thingl.drawbuilder.vertex.VertexDataLayout;
import net.raphimc.thingl.resource.program.Program;

import java.util.function.Supplier;

public record DrawBatch(Supplier<Program> program, Supplier<Program> instanceProgram, Supplier<Program> multiDrawProgram, DrawMode drawMode, VertexDataLayout vertexDataLayout, VertexDataLayout instanceDataLayout, Runnable setupAction, Runnable cleanupAction) {

    public static final DrawBatch[] EMPTY_ARRAY = new DrawBatch[0];

    public DrawBatch(final Supplier<Program> program, final DrawMode drawMode, final VertexDataLayout vertexDataLayout, final Runnable setupAction, final Runnable cleanupAction) {
        this(program, null, drawMode, vertexDataLayout, null, setupAction, cleanupAction);
    }

    public DrawBatch(final Supplier<Program> program, final Supplier<Program> instanceProgram, final DrawMode drawMode, final VertexDataLayout vertexDataLayout, final VertexDataLayout instanceDataLayout, final Runnable setupAction, final Runnable cleanupAction) {
        this(program, instanceProgram, null, drawMode, vertexDataLayout, instanceDataLayout, setupAction, cleanupAction);
    }

    public DrawBatch(final Supplier<Program> program, final Supplier<Program> multiDrawProgram, final DrawMode drawMode, final VertexDataLayout vertexDataLayout, final Runnable setupAction, final Runnable cleanupAction) {
        this(program, null, multiDrawProgram, drawMode, vertexDataLayout, null, setupAction, cleanupAction);
    }

    public Program getProgram(final boolean instancing, final boolean multiDraw) {
        if (instancing) {
            if (multiDraw) {
                throw new IllegalArgumentException("Cannot use instancing and multi draw at the same time");
            }
            if (this.instanceProgram == null) {
                return null;
            }
            return this.instanceProgram.get();
        } else {
            if (multiDraw) {
                if (this.multiDrawProgram == null) {
                    return null;
                }
                return this.multiDrawProgram.get();
            }
            if (this.program == null) {
                return null;
            }
            return this.program.get();
        }
    }

    public boolean supportsInstancing() {
        return this.instanceProgram != null && this.instanceDataLayout != null;
    }

}
