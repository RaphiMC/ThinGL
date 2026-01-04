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

public class OutlineProgram extends MultiPassAuxInputPostProcessingProgram {

    public static final int STYLE_OUTER_BIT = 1 << 0;
    public static final int STYLE_INNER_BIT = 1 << 1;
    public static final int STYLE_SHARP_CORNERS_BIT = 1 << 2;

    public OutlineProgram(final Shader vertexShader, final Shader fragmentShader) {
        super(vertexShader, fragmentShader, 2);
    }

    public void configureParameters(final int width) {
        this.configureParameters(width, STYLE_OUTER_BIT);
    }

    public void configureParameters(final int width, final int styleFlags) {
        this.setUniformInt("u_Width", width);
        this.setUniformInt("u_StyleFlags", styleFlags);
    }

}
