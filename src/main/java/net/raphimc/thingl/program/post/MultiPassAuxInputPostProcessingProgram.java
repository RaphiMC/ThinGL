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
import net.raphimc.thingl.resource.image.texture.Texture2D;
import net.raphimc.thingl.resource.shader.Shader;
import org.lwjgl.opengl.GL11C;

public abstract class MultiPassAuxInputPostProcessingProgram extends AuxInputPostProcessingProgram {

    private final int passes;
    private final boolean needsSourceFramebufferRead;

    public MultiPassAuxInputPostProcessingProgram(final Shader vertexShader, final Shader fragmentShader, final int passes) {
        this(vertexShader, fragmentShader, passes, false);
    }

    public MultiPassAuxInputPostProcessingProgram(final Shader vertexShader, final Shader fragmentShader, final int passes, final boolean needsSourceFramebufferRead) {
        super(vertexShader, fragmentShader);
        this.passes = passes;
        this.needsSourceFramebufferRead = needsSourceFramebufferRead;

        if (passes < 1) {
            throw new IllegalArgumentException("Pass count must be at least 1");
        }
        if (passes == 1 && !needsSourceFramebufferRead) {
            throw new IllegalArgumentException("Single pass programs must use source framebuffer read (Or else use " + AuxInputPostProcessingProgram.class.getSimpleName() + " instead)");
        }
    }

    @Override
    protected void renderInternal(final float xtl, final float ytl, final float xbr, final float ybr) {
        final Framebuffer sourceFramebuffer = ThinGL.glStateManager().getDrawFramebuffer();
        if (this.passes == 1) { // Special case for single pass with source framebuffer read support
            final TextureFramebuffer sourceFramebufferCopy = ThinGL.framebufferPool().borrowFramebuffer(GL11C.GL_LINEAR);
            sourceFramebuffer.blitTo(sourceFramebufferCopy, true, false, false);
            this.renderPass(0, sourceFramebufferCopy, xtl, ytl, xbr, ybr);
            ThinGL.framebufferPool().returnFramebuffer(sourceFramebufferCopy);
        } else {
            final TextureFramebuffer[] framebuffers = new TextureFramebuffer[this.passes - 1];
            for (int i = 0; i < framebuffers.length; i++) {
                framebuffers[i] = ThinGL.framebufferPool().borrowFramebuffer(GL11C.GL_LINEAR);
            }

            ThinGL.glStateStack().push();
            ThinGL.glStateStack().disable(GL11C.GL_BLEND);
            ThinGL.glStateStack().disable(GL11C.GL_DEPTH_TEST);
            ThinGL.glStateStack().disable(GL11C.GL_STENCIL_TEST);
            ThinGL.glStateStack().pushFramebuffer();

            framebuffers[0].bind();
            if (this.needsSourceFramebufferRead) {
                if (sourceFramebuffer.getColorAttachment(0) instanceof Texture2D) {
                    this.renderPass(0, sourceFramebuffer, xtl, ytl, xbr, ybr);
                } else { // Temp copy to ensure the source framebuffer color attachment is a Texture2D
                    final TextureFramebuffer sourceFramebufferCopy = ThinGL.framebufferPool().borrowFramebuffer(GL11C.GL_LINEAR);
                    sourceFramebuffer.blitTo(sourceFramebufferCopy, true, false, false);
                    this.renderPass(0, sourceFramebufferCopy, xtl, ytl, xbr, ybr);
                    ThinGL.framebufferPool().returnFramebuffer(sourceFramebufferCopy);
                }
            } else {
                this.renderPass(0, null, xtl, ytl, xbr, ybr);
            }
            for (int i = 1; i < this.passes - 1; i++) {
                framebuffers[i].bind();
                this.renderPass(i, framebuffers[i - 1], xtl, ytl, xbr, ybr);
            }

            ThinGL.glStateStack().popFramebuffer();
            ThinGL.glStateStack().pop();

            this.renderPass(this.passes - 1, framebuffers[framebuffers.length - 1], xtl, ytl, xbr, ybr);

            for (TextureFramebuffer framebuffer : framebuffers) {
                ThinGL.framebufferPool().returnFramebuffer(framebuffer);
            }
        }
    }

    protected void renderPass(final int pass, final Framebuffer sourceFramebuffer, final float xtl, final float ytl, final float xbr, final float ybr) {
        this.setUniformInt("u_Pass", pass);
        this.setUniformSampler("u_Source", sourceFramebuffer);
        super.renderInternal(xtl, ytl, xbr, ybr);
    }

}
