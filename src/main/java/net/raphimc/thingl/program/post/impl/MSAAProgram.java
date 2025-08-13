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
package net.raphimc.thingl.program.post.impl;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.framebuffer.impl.MSAATextureFramebuffer;
import net.raphimc.thingl.program.PostProcessingProgram;
import net.raphimc.thingl.resource.shader.Shader;
import net.raphimc.thingl.wrapper.Blending;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;

public class MSAAProgram extends PostProcessingProgram {

    private final int samples;
    protected MSAATextureFramebuffer inputFramebuffer;

    public MSAAProgram(final Shader vertexShader, final Shader fragmentShader, final int samples) {
        super(vertexShader, fragmentShader);

        this.samples = samples;
    }

    public void bindInput() {
        if (this.inputFramebuffer == null) {
            this.inputFramebuffer = new MSAATextureFramebuffer(this.samples);
        }
        ThinGL.glStateStack().push();
        ThinGL.glStateStack().disable(GL11C.GL_DEPTH_TEST);
        ThinGL.glStateStack().disable(GL11C.GL_STENCIL_TEST);
        ThinGL.glStateStack().enable(GL13C.GL_MULTISAMPLE);
        ThinGL.glStateStack().pushBlendFunc();
        Blending.alphaWithAdditiveAlphaBlending();
        ThinGL.glStateStack().pushFramebuffer();
        this.inputFramebuffer.bind();
    }

    public void unbindInput() {
        ThinGL.glStateStack().popFramebuffer();
        ThinGL.glStateStack().popBlendFunc();
        ThinGL.glStateStack().pop();
    }

    public void clearInput() {
        if (this.inputFramebuffer != null) {
            this.inputFramebuffer.clear();
        }
    }

    @Override
    public void bind() {
        super.bind();
        this.setUniformSampler("u_Input", this.inputFramebuffer);
    }

    @Override
    protected void prepareAndRenderInternal(final float xtl, final float ytl, final float xbr, final float ybr) {
        ThinGL.glStateStack().pushViewport();
        this.inputFramebuffer.configureViewport();
        ThinGL.glStateStack().pushBlendFunc();
        Blending.premultipliedAlphaBlending();
        super.prepareAndRenderInternal(xtl, ytl, xbr, ybr);
        ThinGL.glStateStack().popBlendFunc();
        ThinGL.glStateStack().popViewport();
    }

    @Override
    protected void free0() {
        if (this.inputFramebuffer != null) {
            this.inputFramebuffer.freeFully();
        }
        super.free0();
    }

}
