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
import net.raphimc.thingl.program.BuiltinPrograms;
import net.raphimc.thingl.program.post.MultiPassPostProcessingProgram;
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import net.raphimc.thingl.resource.shader.Shader;

public class KawaseBlurProgram extends MultiPassPostProcessingProgram<KawaseBlurProgram> {

    private float offset = 3F;

    public KawaseBlurProgram() {
        super(BuiltinPrograms.getShader("post/post_processing", Shader.Type.VERTEX), BuiltinPrograms.getShader("post/kawase_blur", Shader.Type.FRAGMENT), s -> {
            s.setUniform("u_Pass", 0);
            s.setUniform("u_Offset", s.offset);
        }, s -> {
            s.setUniform("u_Pass", 1);
            s.setUniform("u_Offset", s.offset);
        }, s -> {
            s.setUniform("u_Pass", 2);
            s.setUniform("u_Offset", s.offset);
        }, s -> {
            s.setUniform("u_Pass", 3);
            s.setUniform("u_Offset", s.offset);
        });
    }

    public void configureParameters(final float offset) {
        this.offset = offset;
    }

    @Override
    protected void renderQuad0(final float x1, final float y1, final float x2, final float y2) {
        final Framebuffer currentFramebuffer = ThinGL.getImplementation().getCurrentFramebuffer();
        if (x1 == 0 && y1 == 0 && x2 == currentFramebuffer.getWidth() && y2 == currentFramebuffer.getHeight()) {
            super.renderQuad0(x1, y1, x2, y2);
        } else {
            throw new UnsupportedOperationException("KawaseBlurProgram does not support rendering a sub-rectangle of the framebuffer. Call renderFullscreenQuad instead.");
        }
    }

}
