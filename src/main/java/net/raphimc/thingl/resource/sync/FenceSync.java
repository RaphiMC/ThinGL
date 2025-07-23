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

public class FenceSync extends GLSyncObject {

    private Integer condition;
    private Integer flags;

    public FenceSync() {
        this(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
    }

    public FenceSync(final int condition, final int flags) {
        super(GL32C.glFenceSync(condition, flags));
        this.condition = condition;
        this.flags = flags;
    }

    protected FenceSync(final long pointer) {
        super(pointer);
    }

    public static FenceSync fromPointer(final long pointer) {
        if (!GL32C.glIsSync(pointer)) {
            throw new IllegalArgumentException("Not a sync object");
        }
        final int objectType = GL32C.glGetSynci(pointer, GL32C.GL_OBJECT_TYPE, null);
        if (objectType != GL32C.GL_SYNC_FENCE) {
            throw new IllegalArgumentException("Not a fence sync object");
        }
        return new FenceSync(pointer);
    }

    public boolean isSignaled() {
        return GL32C.glGetSynci(this.getPointer(), GL32C.GL_SYNC_STATUS, null) == GL32C.GL_SIGNALED;
    }

    public int clientWait(final int flags, final long timeout) {
        return GL32C.glClientWaitSync(this.getPointer(), flags, timeout);
    }

    public void serverWait(final int flags, final long timeout) {
        GL32C.glWaitSync(this.getPointer(), flags, timeout);
    }

    @Override
    public int getGlType() {
        return GL32C.GL_SYNC_FENCE;
    }

    public int getCondition() {
        if (this.condition == null) {
            this.condition = GL32C.glGetSynci(this.getPointer(), GL32C.GL_SYNC_CONDITION, null);
        }
        return this.condition;
    }

    public int getFlags() {
        if (this.flags == null) {
            this.flags = GL32C.glGetSynci(this.getPointer(), GL32C.GL_SYNC_FLAGS, null);
        }
        return this.flags;
    }

}
