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

package net.raphimc.thingl.resource.buffer;

import org.lwjgl.opengl.GL44C;
import org.lwjgl.opengl.GL45C;

import java.nio.ByteBuffer;

public class ImmutableBuffer extends AbstractBuffer {

    private final int flags;

    public ImmutableBuffer(final long size, final int flags) {
        super(size);
        this.flags = flags;
        GL45C.glNamedBufferStorage(this.getGlId(), size, flags);
    }

    public ImmutableBuffer(final ByteBuffer data, final int flags) {
        super((long) data.remaining());
        this.flags = flags;
        GL45C.glNamedBufferStorage(this.getGlId(), data, flags);
    }

    protected ImmutableBuffer(final int glId) {
        super(glId);
        this.flags = GL45C.glGetNamedBufferParameteri(glId, GL44C.GL_BUFFER_STORAGE_FLAGS);
    }

    public int getFlags() {
        return this.flags;
    }

}
