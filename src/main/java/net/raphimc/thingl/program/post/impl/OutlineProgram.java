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

public class OutlineProgram extends MultiPassPostProcessingProgram<OutlineProgram> {

    public static final int STYLE_OUTER_BIT = 1 << 0;
    public static final int STYLE_INNER_BIT = 1 << 1;
    public static final int STYLE_SHARP_CORNERS_BIT = 1 << 2;

    private int styleFlags = STYLE_OUTER_BIT;
    private int width = 1;

    public OutlineProgram(final Shader vertexShader, final Shader fragmentShader) {
        super(vertexShader, fragmentShader, s -> {
            s.setUniformBoolean("u_FinalPass", false);
            s.setUniformInt("u_StyleFlags", s.styleFlags);
            s.setUniformInt("u_Width", s.width);
        }, s -> {
            s.setUniformBoolean("u_FinalPass", true);
            s.setUniformInt("u_StyleFlags", s.styleFlags);
            s.setUniformInt("u_Width", s.width);
        });
    }

    public void configureParameters(final int styleFlags, final int width) {
        this.styleFlags = styleFlags;
        this.width = width;
    }

}
