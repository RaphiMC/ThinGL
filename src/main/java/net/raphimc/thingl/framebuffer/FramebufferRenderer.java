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

package net.raphimc.thingl.framebuffer;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import net.raphimc.thingl.resource.texture.Texture2D;
import net.raphimc.thingl.util.RenderMathUtil;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL45C;

public class FramebufferRenderer {

    private final Texture2D colorAttachment;
    private final Framebuffer framebuffer;

    public FramebufferRenderer(final int width, final int height) {
        this(width, height, GL11C.GL_LINEAR);
    }

    public FramebufferRenderer(final int width, final int height, final int textureFilter) {
        this.colorAttachment = new Texture2D(Texture2D.InternalFormat.RGBA8, width, height);
        this.colorAttachment.setFilter(textureFilter);
        final Texture2D depthStencilAttachment = new Texture2D(Texture2D.InternalFormat.DEPTH32_STENCIL8, width, height);
        depthStencilAttachment.setFilter(textureFilter);
        GL45C.glTextureParameteri(depthStencilAttachment.getGlId(), GL14C.GL_TEXTURE_COMPARE_MODE, GL11C.GL_NONE);
        this.framebuffer = new Framebuffer(this.colorAttachment, depthStencilAttachment);
    }

    public void begin() {
        ThinGL.glStateTracker().pushFramebuffer();
        this.framebuffer.clear();
        this.framebuffer.bind(true);
        ThinGL.applicationInterface().pushProjectionMatrix(new Matrix4f().setOrtho(0F, this.framebuffer.getWidth(), this.framebuffer.getHeight(), 0F, -5000F, 5000F));
        ThinGL.applicationInterface().pushViewMatrix(RenderMathUtil.getIdentityMatrix());
    }

    public void end() {
        ThinGL.applicationInterface().popViewMatrix();
        ThinGL.applicationInterface().popProjectionMatrix();
        ThinGL.glStateTracker().popFramebuffer(true);
    }

    public Framebuffer getFramebuffer() {
        return this.framebuffer;
    }

    public int getGlId() {
        return this.colorAttachment.getGlId();
    }

    public void free() {
        this.framebuffer.freeFully();
    }

}
