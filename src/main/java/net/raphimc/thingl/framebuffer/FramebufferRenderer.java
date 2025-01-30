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
import net.raphimc.thingl.util.GlobalObjects;
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import net.raphimc.thingl.wrapper.GLStateTracker;
import net.raphimc.thingl.resource.texture.Texture2D;
import org.joml.Matrix4f;

public class FramebufferRenderer {

    private final Texture2D colorAttachment;
    private final Texture2D depthStencilAttachment;
    private final Framebuffer framebuffer;

    public FramebufferRenderer(final int width, final int height) {
        this.colorAttachment = new Texture2D(Texture2D.InternalFormat.RGBA8, width, height);
        this.depthStencilAttachment = new Texture2D(Texture2D.InternalFormat.DEPTH32_STENCIL8, width, height);
        this.framebuffer = new Framebuffer(this.colorAttachment, this.depthStencilAttachment);
    }

    public void begin() {
        GLStateTracker.pushFramebuffer();
        this.framebuffer.clear();
        this.framebuffer.bind(true);
        ThinGL.getImplementation().pushProjectionMatrix(new Matrix4f().setOrtho(0F, this.framebuffer.getWidth(), this.framebuffer.getHeight(), 0F, -5000F, 5000F));
        ThinGL.getImplementation().pushViewMatrix(GlobalObjects.IDENTITY_MATRIX);
    }

    public void end() {
        ThinGL.getImplementation().popViewMatrix();
        ThinGL.getImplementation().popProjectionMatrix();
        GLStateTracker.popFramebuffer(true);
    }

    public Framebuffer getFramebuffer() {
        return this.framebuffer;
    }

    public int getGlId() {
        return this.colorAttachment.getGlId();
    }

    public void delete() {
        this.framebuffer.delete();
        this.colorAttachment.delete();
        this.depthStencilAttachment.delete();
    }

}
