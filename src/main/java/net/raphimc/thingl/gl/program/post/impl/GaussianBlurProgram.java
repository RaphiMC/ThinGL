/*
 * This file is part of ThinGL - https://github.com/RaphiMC/ThinGL
 * Copyright (C) 2024-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.thingl.gl.program.post.impl;

import net.raphimc.thingl.gl.program.post.MultiPassAuxInputPostProcessingProgram;
import net.raphimc.thingl.gl.resource.shader.Shader;

public class GaussianBlurProgram extends MultiPassAuxInputPostProcessingProgram {

    public GaussianBlurProgram(final Shader vertexShader, final Shader fragmentShader) {
        super(vertexShader, fragmentShader, 2, true);
    }

    public void configureParameters() {
        this.configureParameters(10);
    }

    public void configureParameters(final int strength) {
        this.configureParameters(strength, strength);
    }

    public void configureParameters(final int radius, final float sigma) {
        this.setUniformInt("u_Radius", radius);
        this.setUniformFloat("u_Sigma", sigma);
    }

}
