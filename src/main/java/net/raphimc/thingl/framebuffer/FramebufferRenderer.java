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
import net.raphimc.thingl.resource.image.texture.Texture2D;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL30C;

public class FramebufferRenderer {

    private final Texture2D colorAttachment;
    private final Texture2D depthStencilAttachment;
    private final Framebuffer framebuffer;

    public FramebufferRenderer(final int width, final int height) {
        this(width, height, true);
    }

    public FramebufferRenderer(final int width, final int height, final boolean addDepthAttachment) {
        this.colorAttachment = new Texture2D(GL11C.GL_RGBA8, width, height);
        if (addDepthAttachment) {
            this.depthStencilAttachment = new Texture2D(GL30C.GL_DEPTH32F_STENCIL8, width, height);
            this.depthStencilAttachment.setParameterInt(GL14C.GL_TEXTURE_COMPARE_MODE, GL11C.GL_NONE);
            this.framebuffer = new Framebuffer(this.colorAttachment, this.depthStencilAttachment, this.depthStencilAttachment);
        } else {
            this.depthStencilAttachment = null;
            this.framebuffer = new Framebuffer(this.colorAttachment);
        }
    }

    public void begin() {
        ThinGL.globalUniforms().getProjectionMatrix().pushMatrix().setOrtho(0F, this.framebuffer.getWidth(), this.framebuffer.getHeight(), 0F, -1000F, 1000F);
        ThinGL.globalUniforms().getViewMatrix().pushMatrix().identity();
        ThinGL.glStateStack().pushFramebuffer();
        ThinGL.glStateStack().pushViewport();
        this.framebuffer.bindAndConfigureViewport();
    }

    public void end() {
        ThinGL.glStateStack().popViewport();
        ThinGL.glStateStack().popFramebuffer();
        ThinGL.globalUniforms().getViewMatrix().popMatrix();
        ThinGL.globalUniforms().getProjectionMatrix().popMatrix();
    }

    public void clear() {
        this.framebuffer.clear();
    }

    public void setTextureFilter(final int filter) {
        this.colorAttachment.setFilter(filter);
        if (this.depthStencilAttachment != null) {
            this.depthStencilAttachment.setFilter(filter);
        }
    }

    public Texture2D getColorAttachment() {
        return this.colorAttachment;
    }

    public Texture2D getDepthStencilAttachment() {
        return this.depthStencilAttachment;
    }

    public Framebuffer getFramebuffer() {
        return this.framebuffer;
    }

    public void free() {
        this.framebuffer.freeFully();
    }

}
