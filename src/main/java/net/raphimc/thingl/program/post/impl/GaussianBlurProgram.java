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

import net.raphimc.thingl.program.post.MultiPassPostProcessingProgram;
import net.raphimc.thingl.resource.shader.Shader;

public class GaussianBlurProgram extends MultiPassPostProcessingProgram<GaussianBlurProgram> {

    private int radius = 10;
    private float sigma = 10F;

    public GaussianBlurProgram(final Shader vertexShader, final Shader fragmentShader) {
        super(vertexShader, fragmentShader, s -> {
            s.setUniform("u_FinalPass", false);
            s.setUniform("u_Radius", s.radius);
            s.setUniform("u_Sigma", s.sigma);
        }, s -> {
            s.setUniform("u_FinalPass", true);
            s.setUniform("u_Radius", s.radius);
            s.setUniform("u_Sigma", s.sigma);
        });
    }

    public void configureParameters(final int strength) {
        this.radius = strength;
        this.sigma = strength;
    }

    public void configureParameters(final int radius, final float sigma) {
        this.radius = radius;
        this.sigma = sigma;
    }

}
