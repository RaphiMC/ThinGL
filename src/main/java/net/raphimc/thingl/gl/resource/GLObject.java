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
package net.raphimc.thingl.gl.resource;

import net.raphimc.thingl.ThinGL;

import java.util.Objects;

public abstract class GLObject {

    private int glId;

    protected GLObject(final int glId) {
        this.glId = glId;
    }

    public void free() {
        if (this.isAllocated()) {
            try {
                this.free0();
            } finally {
                this.glId = -1;
            }
        }
    }

    protected abstract void free0();

    public boolean isAllocated() {
        return this.glId > 0;
    }

    public abstract int getGlType();

    public final int getGlId() {
        this.assertAllocated();
        return this.glId;
    }

    public final String getDebugName() {
        this.assertAllocated();
        return ThinGL.glBackend().getObjectLabel(this.getGlType(), this.glId);
    }

    public final void setDebugName(final String name) {
        this.assertAllocated();
        ThinGL.glBackend().objectLabel(this.getGlType(), this.glId, name);
    }

    @Override
    public final boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GLObject that = (GLObject) o;
        return glId == that.glId;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(glId);
    }

    protected final void assertAllocated() {
        if (!this.isAllocated()) {
            throw new IllegalStateException("The OpenGL object is not allocated or has been freed");
        }
    }

}
