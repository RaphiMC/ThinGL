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

import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL45C;

import java.nio.ByteBuffer;

public class MutableBuffer extends Buffer {

    public MutableBuffer(final long size, final int usage) {
        this.initialize(size, usage);
    }

    public MutableBuffer(final ByteBuffer dataBuffer, final int usage) {
        this.initialize(dataBuffer, usage);
    }

    protected MutableBuffer(final int glId) {
        super(glId);
    }

    public void initialize(final long size, final int usage) {
        this.parameters.clear();
        GL45C.glNamedBufferData(this.getGlId(), size, usage);
    }

    public void initialize(final ByteBuffer dataBuffer, final int usage) {
        if (!dataBuffer.isDirect()) {
            throw new IllegalArgumentException("Data buffer must be a direct ByteBuffer");
        }

        this.parameters.clear();
        GL45C.glNamedBufferData(this.getGlId(), dataBuffer, usage);
    }

    public void ensureSize(final long size) {
        if (this.getSize() < size) {
            this.setSize(size);
        }
    }

    public void setSize(final long size) {
        this.initialize(size, this.getUsage());
    }

    public int getUsage() {
        return this.getParameterInt(GL15C.GL_BUFFER_USAGE);
    }

}
