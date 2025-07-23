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
package net.raphimc.thingl.resource.sync;

import org.lwjgl.opengl.GL32C;

import java.util.Objects;

public abstract class GLSyncObject {

    private long pointer;

    protected GLSyncObject(final long pointer) {
        this.pointer = pointer;
    }

    public void free() {
        if (this.isAllocated()) {
            try {
                GL32C.glDeleteSync(this.pointer);
            } finally {
                this.pointer = 0L;
            }
        }
    }

    public boolean isAllocated() {
        return this.pointer != 0L;
    }

    public abstract int getGlType();

    public final long getPointer() {
        this.checkAllocated();
        return this.pointer;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GLSyncObject that = (GLSyncObject) o;
        return pointer == that.pointer;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(pointer);
    }

    protected final void checkAllocated() {
        if (!this.isAllocated()) {
            throw new IllegalStateException("The OpenGL object is not allocated or has been freed");
        }
    }

}
