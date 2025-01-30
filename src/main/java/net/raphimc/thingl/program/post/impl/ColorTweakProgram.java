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

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.program.BuiltinPrograms;
import net.raphimc.thingl.program.post.SinglePassPostProcessingProgram;
import net.raphimc.thingl.resource.shader.Shader;

public class ColorTweakProgram extends SinglePassPostProcessingProgram<ColorTweakProgram> {

    private Color color = Color.WHITE;

    public ColorTweakProgram() {
        super(BuiltinPrograms.getShader("post/post_processing", Shader.Type.VERTEX), BuiltinPrograms.getShader("post/color_tweak", Shader.Type.FRAGMENT), s -> {
            s.setUniform("u_Color", s.color.getRed() / 255F, s.color.getGreen() / 255F, s.color.getBlue() / 255F, s.color.getAlpha() / 255F);
        });
    }

    public void configureParameters(final Color color) {
        this.color = color;
    }

}
