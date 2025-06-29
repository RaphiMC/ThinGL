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
import net.raphimc.thingl.program.post.MultiPassAuxInputPostProcessingProgram;
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import net.raphimc.thingl.resource.shader.Shader;

public class KawaseBlurProgram extends MultiPassAuxInputPostProcessingProgram {

    public KawaseBlurProgram(final Shader vertexShader, final Shader fragmentShader) {
        super(vertexShader, fragmentShader, 4, true);
    }

    public void configureParameters() {
        this.configureParameters(3F);
    }

    public void configureParameters(final float offset) {
        this.setUniformFloat("u_Offset", offset);
    }

    @Override
    protected void renderQuad0(final float x1, final float y1, final float x2, final float y2) {
        final Framebuffer currentFramebuffer = ThinGL.applicationInterface().getCurrentFramebuffer();
        if (x1 == 0 && y1 == 0 && x2 == currentFramebuffer.getWidth() && y2 == currentFramebuffer.getHeight()) {
            super.renderQuad0(x1, y1, x2, y2);
        } else {
            throw new UnsupportedOperationException("KawaseBlurProgram does not support rendering a sub-rectangle of the framebuffer. Call renderFullscreenQuad instead.");
        }
    }

}
