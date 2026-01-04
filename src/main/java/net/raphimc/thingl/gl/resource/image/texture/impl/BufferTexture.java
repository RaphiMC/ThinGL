/*
 * This file is part of ThinGL - https://github.com/RaphiMC/ThinGL
 * Copyright (C) 2024-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.thingl.gl.resource.image.texture.impl;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.buffer.Buffer;
import net.raphimc.thingl.gl.resource.image.texture.Texture;
import org.lwjgl.opengl.GL31C;

public class BufferTexture extends Texture {

    public BufferTexture() {
        super(GL31C.GL_TEXTURE_BUFFER);
    }

    protected BufferTexture(final int glId) {
        super(glId, null);
    }

    public static BufferTexture fromGlIdUnsafe(final int glId) {
        return new BufferTexture(glId);
    }

    public void initialize(final int internalFormat, final Buffer buffer) {
        this.levelParameters.clear();
        ThinGL.glBackend().textureBuffer(this.getGlId(), internalFormat, buffer.getGlId());
    }

    @Override
    public int getTarget() {
        return GL31C.GL_TEXTURE_BUFFER;
    }

}
