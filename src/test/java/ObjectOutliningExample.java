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

import base.ExampleBase;
import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.program.BuiltinPrograms;
import net.raphimc.thingl.renderer.impl.Renderer2D;
import org.joml.Matrix4fStack;

public class ObjectOutliningExample extends ExampleBase {

    public static void main(String[] args) {
        new ObjectOutliningExample().run();
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        BuiltinPrograms.SMOOTH_OUTLINE.bindMask();
        this.renderScene(positionMatrix);
        BuiltinPrograms.SMOOTH_OUTLINE.unbindMask();
        BuiltinPrograms.SMOOTH_OUTLINE.renderFullscreenQuad();
        BuiltinPrograms.SMOOTH_OUTLINE.clearMask();

        positionMatrix.translate(0, 200, 0);
        this.renderScene(positionMatrix);
    }

    private void renderScene(final Matrix4fStack positionMatrix) {
        Renderer2D.INSTANCE.filledRectangle(positionMatrix, 50, 50, 75, 75, Color.RED);
        Renderer2D.INSTANCE.filledTriangle(positionMatrix, 50, 50, 75, 110, 100, 50, Color.GREEN);
        Renderer2D.INSTANCE.filledCircle(positionMatrix, 120, 100, 50, Color.BLUE);
    }

}
