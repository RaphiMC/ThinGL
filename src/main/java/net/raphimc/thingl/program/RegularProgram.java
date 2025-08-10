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

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.program.Program;
import net.raphimc.thingl.resource.shader.Shader;
import net.raphimc.thingl.wrapper.GLStateManager;
import org.joml.Matrix4f;

public class RegularProgram extends Program {

    public RegularProgram(final Shader... shaders) {
        super(shaders);
    }

    @Override
    public void bind() {
        super.bind();
        final GLStateManager.Viewport viewport = ThinGL.glStateManager().getViewport();
        this.setUniformMatrix4f("u_ProjectionMatrix", ThinGL.globalUniforms().getProjectionMatrix());
        this.setUniformMatrix4f("u_ViewMatrix", ThinGL.globalUniforms().getViewMatrix());
        this.setUniformVector4f("u_ColorModifier", ThinGL.globalUniforms().getColorModifier());
        this.setUniformVector2f("u_Viewport", viewport.width(), viewport.height());
    }

    public void configureParameters(final Matrix4f modelMatrix) {
        this.setUniformMatrix4f("u_ModelMatrix", modelMatrix);
    }

}
