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

package net.raphimc.thingl.program.post;

import net.raphimc.thingl.framebuffer.impl.TextureFramebuffer;
import net.raphimc.thingl.program.PostProcessingProgram;
import net.raphimc.thingl.resource.shader.Shader;
import net.raphimc.thingl.util.GlobalObjects;
import net.raphimc.thingl.util.pool.FramebufferPool;
import net.raphimc.thingl.wrapper.GLStateTracker;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;

public abstract class MaskablePostProcessingProgram extends PostProcessingProgram {

    protected TextureFramebuffer maskFramebuffer;

    public MaskablePostProcessingProgram(final Shader vertexShader, final Shader fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public void bindMask() {
        GLStateTracker.push();
        GLStateTracker.disable(GL11C.GL_DEPTH_TEST);
        GLStateTracker.disable(GL11C.GL_STENCIL_TEST);
        GLStateTracker.pushBlendFunc();
        GL14C.glBlendFuncSeparate(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA, GL11C.GL_ONE, GL11C.GL_ONE_MINUS_SRC_ALPHA);
        GLStateTracker.pushFramebuffer();
        if (this.maskFramebuffer == null) {
            this.maskFramebuffer = FramebufferPool.borrowFramebuffer(GL11C.GL_LINEAR);
        }
        this.maskFramebuffer.bind();
    }

    public void unbindMask() {
        GLStateTracker.popFramebuffer();
        GLStateTracker.popBlendFunc();
        GLStateTracker.pop();
    }

    public void renderMask() {
        this.renderMask(GlobalObjects.IDENTITY_MATRIX);
    }

    public void renderMask(final Matrix4f positionMatrix) {
        GLStateTracker.push();
        GLStateTracker.enable(GL11C.GL_BLEND);
        GLStateTracker.pushBlendFunc();
        GL11C.glBlendFunc(GL11C.GL_ONE, GL11C.GL_ONE_MINUS_SRC_ALPHA);
        this.maskFramebuffer.render(positionMatrix, 0, 0, this.maskFramebuffer.getWidth(), this.maskFramebuffer.getHeight());
        GLStateTracker.popBlendFunc();
        GLStateTracker.pop();
    }

    public void clearMask() {
        if (this.maskFramebuffer != null) {
            FramebufferPool.returnFramebuffer(this.maskFramebuffer);
            this.maskFramebuffer = null;
        }
    }

    @Override
    public void bind() {
        super.bind();
        this.setUniformTexture("u_Mask", this.maskFramebuffer);
    }

}
