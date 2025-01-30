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

package net.raphimc.thingl.resource.renderbuffer;

import net.raphimc.thingl.resource.GLResource;
import net.raphimc.thingl.resource.framebuffer.FramebufferAttachment;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;

public abstract class AbstractRenderBuffer extends GLResource implements FramebufferAttachment {

    private final int internalFormat;
    private final int width;
    private final int height;

    public AbstractRenderBuffer(final int internalFormat, final int width, final int height) {
        super(GL30C.GL_RENDERBUFFER, GL45C.glCreateRenderbuffers());
        this.internalFormat = internalFormat;
        this.width = width;
        this.height = height;
    }

    protected AbstractRenderBuffer(final int glId) {
        super(GL30C.GL_RENDERBUFFER, glId);
        this.internalFormat = GL45C.glGetNamedRenderbufferParameteri(this.getGlId(), GL30C.GL_RENDERBUFFER_INTERNAL_FORMAT);
        this.width = GL45C.glGetNamedRenderbufferParameteri(this.getGlId(), GL30C.GL_RENDERBUFFER_WIDTH);
        this.height = GL45C.glGetNamedRenderbufferParameteri(this.getGlId(), GL30C.GL_RENDERBUFFER_HEIGHT);
    }

    public static AbstractRenderBuffer fromGlId(final int glId) {
        if (!GL30C.glIsRenderbuffer(glId)) {
            throw new IllegalArgumentException("Invalid OpenGL resource");
        }
        final int samples = GL45C.glGetNamedRenderbufferParameteri(glId, GL30C.GL_RENDERBUFFER_SAMPLES);
        if (samples <= 0) {
            return new RenderBuffer(glId);
        } else {
            return new MultisampleRenderBuffer(glId);
        }
    }

    @Override
    protected void delete0() {
        GL30C.glDeleteRenderbuffers(this.getGlId());
    }

    public int getInternalFormat() {
        return this.internalFormat;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

}
