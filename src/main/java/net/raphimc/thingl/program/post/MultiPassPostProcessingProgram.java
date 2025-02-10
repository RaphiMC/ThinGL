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
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import net.raphimc.thingl.resource.shader.Shader;
import net.raphimc.thingl.util.pool.FramebufferPool;
import net.raphimc.thingl.wrapper.GLStateTracker;
import org.lwjgl.opengl.GL11C;

import java.util.function.Consumer;

public class MultiPassPostProcessingProgram<S extends MaskablePostProcessingProgram> extends MaskablePostProcessingProgram {

    private final Consumer<S>[] passes;

    @SafeVarargs
    public MultiPassPostProcessingProgram(final Shader vertexShader, final Shader fragmentShader, final Consumer<S>... passes) {
        super(vertexShader, fragmentShader);
        this.passes = passes;

        if (this.passes.length < 2) throw new IllegalArgumentException("Pass count must be at least 2");
    }

    @Override
    protected void renderQuad0(final float x1, final float y1, final float x2, final float y2) {
        final Framebuffer sourceFramebuffer = ThinGL.getImplementation().getCurrentFramebuffer();

        final TextureFramebuffer[] framebuffers = new TextureFramebuffer[this.passes.length - 1];
        for (int i = 0; i < framebuffers.length; i++) {
            framebuffers[i] = FramebufferPool.borrowFramebuffer(GL11C.GL_LINEAR);
        }

        GLStateTracker.push();
        GLStateTracker.disable(GL11C.GL_DEPTH_TEST);
        GLStateTracker.disable(GL11C.GL_STENCIL_TEST);

        this.setUniformTexture("u_Source", sourceFramebuffer);
        framebuffers[0].bind();
        this.passes[0].accept((S) this);
        super.renderQuad0(x1, y1, x2, y2);

        for (int i = 1; i < this.passes.length - 1; i++) {
            this.setUniformTexture("u_Source", framebuffers[i - 1]);
            framebuffers[i].bind();
            this.passes[i].accept((S) this);
            super.renderQuad0(x1, y1, x2, y2);
        }

        GLStateTracker.pop();

        this.setUniformTexture("u_Source", framebuffers[this.passes.length - 2]);
        sourceFramebuffer.bind();
        this.passes[this.passes.length - 1].accept((S) this);
        super.renderQuad0(x1, y1, x2, y2);

        for (TextureFramebuffer framebuffer : framebuffers) {
            FramebufferPool.returnFramebuffer(framebuffer);
        }
    }

}
