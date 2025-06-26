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

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.framebuffer.impl.TextureFramebuffer;
import net.raphimc.thingl.program.PostProcessingProgram;
import net.raphimc.thingl.resource.shader.Shader;
import net.raphimc.thingl.util.RenderMathUtil;
import net.raphimc.thingl.wrapper.Blending;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;

public abstract class MaskablePostProcessingProgram extends PostProcessingProgram {

    protected TextureFramebuffer maskFramebuffer;

    public MaskablePostProcessingProgram(final Shader vertexShader, final Shader fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public void bindMask() {
        ThinGL.glStateStack().push();
        ThinGL.glStateStack().disable(GL11C.GL_DEPTH_TEST);
        ThinGL.glStateStack().disable(GL11C.GL_STENCIL_TEST);
        ThinGL.glStateStack().pushBlendFunc();
        Blending.alphaWithAdditiveAlphaBlending();
        ThinGL.glStateStack().pushFramebuffer();
        if (this.maskFramebuffer == null) {
            this.maskFramebuffer = ThinGL.framebufferPool().borrowFramebuffer(GL11C.GL_LINEAR);
        }
        this.maskFramebuffer.bind();
    }

    public void unbindMask() {
        ThinGL.glStateStack().popFramebuffer();
        ThinGL.glStateStack().popBlendFunc();
        ThinGL.glStateStack().pop();
    }

    public void renderMask() {
        this.renderMask(RenderMathUtil.getIdentityMatrix());
    }

    public void renderMask(final Matrix4f positionMatrix) {
        ThinGL.glStateStack().pushBlendFunc();
        Blending.premultipliedAlphaBlending();
        this.maskFramebuffer.render(positionMatrix, 0, 0, this.maskFramebuffer.getWidth(), this.maskFramebuffer.getHeight());
        ThinGL.glStateStack().popBlendFunc();
    }

    public void clearMask() {
        if (this.maskFramebuffer != null) {
            ThinGL.framebufferPool().returnFramebuffer(this.maskFramebuffer);
            this.maskFramebuffer = null;
        }
    }

    @Override
    public void bind() {
        super.bind();
        this.setUniformSampler("u_Mask", this.maskFramebuffer);
    }

}
