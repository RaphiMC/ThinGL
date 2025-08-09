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
package net.raphimc.thingl.implementation;

import net.lenni0451.commons.color.Color;
import org.joml.Matrix4fStack;

public class GlobalUniforms {

    private final Matrix4fStack projectionMatrixStack = new Matrix4fStack(16);
    private final Matrix4fStack viewMatrixStack = new Matrix4fStack(16);
    private Color colorModifier = Color.WHITE;

    public Matrix4fStack getProjectionMatrix() {
        return this.projectionMatrixStack;
    }

    public Matrix4fStack getViewMatrix() {
        return this.viewMatrixStack;
    }

    public Color getColorModifier() {
        return this.colorModifier;
    }

    public void setColorModifier(final Color colorModifier) {
        this.colorModifier = colorModifier;
    }

}
