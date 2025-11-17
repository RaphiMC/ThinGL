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

import net.raphimc.thingl.gl.resource.image.ImageStorage2D;
import net.raphimc.thingl.gl.resource.image.renderbuffer.RenderBuffer;
import org.lwjgl.opengl.GL45C;

public class StandardRenderBuffer extends RenderBuffer implements ImageStorage2D {

    public StandardRenderBuffer(final int internalFormat, final int width, final int height) {
        this.initialize(internalFormat, width, height);
    }

    protected StandardRenderBuffer(final int glId) {
        super(glId);
    }

    public static StandardRenderBuffer fromGlIdUnsafe(final int glId) {
        return new StandardRenderBuffer(glId);
    }

    public void initialize(final int internalFormat, final int width, final int height) {
        this.parameters.clear();
        GL45C.glNamedRenderbufferStorage(this.getGlId(), internalFormat, width, height);
    }

}
