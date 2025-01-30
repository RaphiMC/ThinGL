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

import net.raphimc.thingl.resource.shader.Shader;

import java.util.function.Consumer;

public class SinglePassPostProcessingProgram<S extends MaskablePostProcessingProgram> extends MaskablePostProcessingProgram {

    private final Consumer<S> pass;

    public SinglePassPostProcessingProgram(final Shader vertexShader, final Shader fragmentShader, final Consumer<S> pass) {
        super(vertexShader, fragmentShader);

        this.pass = pass;
    }

    @Override
    protected void renderQuad0(final float x, final float y, final float width, final float height) {
        this.pass.accept((S) this);
        super.renderQuad0(x, y, width, height);
    }

}
