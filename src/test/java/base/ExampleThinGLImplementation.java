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
package base;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.StandaloneThinGLImplementation;
import org.lwjgl.opengl.GL11C;

public class ExampleThinGLImplementation extends StandaloneThinGLImplementation {

    public ExampleThinGLImplementation() {
        ThinGL.registerWindowFramebufferResizeCallback(this::createProjectionMatrix);
    }

    private void createProjectionMatrix(final int width, final int height) {
        this.projectionMatrixStack.setOrtho(0F, width, height, 0F, -5000F, 5000F);
        GL11C.glViewport(0, 0, width, height);
    }

}
