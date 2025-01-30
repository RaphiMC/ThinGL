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

package net.raphimc.thingl.framebuffer.impl;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.framebuffer.ResizingFramebuffer;
import net.raphimc.thingl.renderer.impl.Renderer2D;
import net.raphimc.thingl.resource.texture.AbstractTexture;
import net.raphimc.thingl.resource.texture.Texture2D;
import net.raphimc.thingl.util.GlobalObjects;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL45C;

public class TextureFramebuffer extends ResizingFramebuffer {

    public TextureFramebuffer() {
        this(true);
    }

    public TextureFramebuffer(final boolean addDepthAttachment) {
        this(addDepthAttachment, GL11C.GL_LINEAR);
    }

    public TextureFramebuffer(final int textureFilter) {
        this(true, textureFilter);
    }

    public TextureFramebuffer(final boolean addDepthAttachment, final int textureFilter) {
        super((width, height) -> {
            final Texture2D texture = new Texture2D(AbstractTexture.InternalFormat.RGBA8, width, height);
            texture.setFilter(textureFilter);
            return texture;
        }, addDepthAttachment ? (width, height) -> {
            final Texture2D texture = new Texture2D(AbstractTexture.InternalFormat.DEPTH32_STENCIL8, width, height);
            texture.setFilter(textureFilter);
            GL45C.glTextureParameteri(texture.getGlId(), GL14C.GL_TEXTURE_COMPARE_MODE, GL11C.GL_NONE);
            return texture;
        } : null);
        this.init();
    }

    public void render(final Matrix4f positionMatrix, final float x, final float y, final float width, final float height) {
        ThinGL.getImplementation().pushProjectionMatrix(new Matrix4f().setOrtho(0F, this.getWidth(), this.getHeight(), 0F, -5000F, 5000F));
        ThinGL.getImplementation().pushViewMatrix(GlobalObjects.IDENTITY_MATRIX);
        Renderer2D.INSTANCE.texture(positionMatrix, this.getColorAttachment().getGlId(), x, y, width, height, 0F, height, width, -height, width, height);
        ThinGL.getImplementation().popProjectionMatrix();
        ThinGL.getImplementation().popViewMatrix();
    }

    @Override
    public Texture2D getColorAttachment() {
        return (Texture2D) super.getColorAttachment();
    }

    @Override
    public Texture2D getDepthAttachment() {
        return (Texture2D) super.getDepthAttachment();
    }

}
