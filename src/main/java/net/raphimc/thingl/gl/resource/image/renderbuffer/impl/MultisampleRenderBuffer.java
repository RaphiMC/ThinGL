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
package net.raphimc.thingl.gl.resource.image.renderbuffer.impl;

import net.raphimc.thingl.gl.resource.image.MultisampleImageStorage2D;
import net.raphimc.thingl.gl.resource.image.renderbuffer.RenderBuffer;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;

public class MultisampleRenderBuffer extends RenderBuffer implements MultisampleImageStorage2D {

    public MultisampleRenderBuffer(final int internalFormat, final int width, final int height, final int samples) {
        this.initialize(internalFormat, width, height, samples);
    }

    protected MultisampleRenderBuffer(final int glId) {
        super(glId);
    }

    public static MultisampleRenderBuffer fromGlIdUnsafe(final int glId) {
        return new MultisampleRenderBuffer(glId);
    }

    public void initialize(final int internalFormat, final int width, final int height, final int samples) {
        this.parameters.clear();
        GL45C.glNamedRenderbufferStorageMultisample(this.getGlId(), samples, internalFormat, width, height);
    }

    @Override
    public int getSamples() {
        return this.getParameterInt(GL30C.GL_RENDERBUFFER_SAMPLES);
    }

}
