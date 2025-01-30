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

package net.raphimc.thingl.resource;

import org.lwjgl.opengl.GL43C;

import java.util.Objects;

public abstract class GLResource {

    private final int glType;
    private int glId;

    protected GLResource(final int glType, final int glId) {
        this.glType = glType;
        this.glId = glId;
    }

    public void refreshCachedData() {
    }

    public void delete() {
        if (this.isAllocated()) {
            try {
                this.delete0();
            } finally {
                this.glId = -1;
            }
        }
    }

    protected abstract void delete0();

    public boolean isAllocated() {
        return this.glId > 0;
    }

    public final int getGlType() {
        return this.glType;
    }

    public final int getGlId() {
        this.checkAllocated();
        return this.glId;
    }

    public final String getDebugName() {
        this.checkAllocated();
        return GL43C.glGetObjectLabel(this.glType, this.glId);
    }

    public final void setDebugName(final String name) {
        this.checkAllocated();
        GL43C.glObjectLabel(this.glType, this.glId, name);
    }

    @Override
    public final boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GLResource that = (GLResource) o;
        return glType == that.glType && glId == that.glId;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(glType, glId);
    }

    protected final void checkAllocated() {
        if (!this.isAllocated()) {
            throw new IllegalStateException("The OpenGL resource is not allocated or has been deleted");
        }
    }

}
