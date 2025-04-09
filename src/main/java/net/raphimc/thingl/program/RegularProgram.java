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

package net.raphimc.thingl.program;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import net.raphimc.thingl.resource.program.Program;
import net.raphimc.thingl.resource.shader.Shader;
import net.raphimc.thingl.util.RenderMathUtil;
import org.jetbrains.annotations.ApiStatus;

public class RegularProgram extends Program {

    public RegularProgram(final Shader... shaders) {
        super(shaders);
    }

    @ApiStatus.Internal
    protected RegularProgram(final int glId) {
        super(glId);
    }

    @Override
    public void bind() {
        super.bind();
        final Framebuffer currentFramebuffer = ThinGL.applicationInterface().getCurrentFramebuffer();
        this.setUniform("u_ProjectionMatrix", ThinGL.applicationInterface().getProjectionMatrix());
        this.setUniform("u_ViewMatrix", ThinGL.applicationInterface().getViewMatrix());
        this.setUniform("u_ModelMatrix", RenderMathUtil.getIdentityMatrix());
        this.setUniform("u_Viewport", currentFramebuffer.getWidth(), currentFramebuffer.getHeight());
        this.setUniform("u_ColorModifier", Color.WHITE);
    }

}
