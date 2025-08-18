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

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.application.GLFWApplicationRunner;
import net.raphimc.thingl.program.post.impl.OutlineProgram;
import org.joml.Matrix4fStack;

public class ObjectOutliningExample extends GLFWApplicationRunner {

    public static void main(String[] args) {
        new ObjectOutliningExample().launch();
    }

    public ObjectOutliningExample() {
        super(new Configuration().setWindowTitle("ThinGL Example - Object outlining").setExtendedDebugMode(true));
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        ThinGL.programs().getOutline().bindInput();
        this.renderScene(positionMatrix);
        ThinGL.programs().getOutline().unbindInput();
        ThinGL.programs().getOutline().configureParameters(1, OutlineProgram.STYLE_OUTER_BIT); // Configure the outline
        ThinGL.programs().getOutline().renderFullscreen();
        ThinGL.programs().getOutline().clearInput();

        positionMatrix.translate(0, 200, 0);
        this.renderScene(positionMatrix);
    }

    private void renderScene(final Matrix4fStack positionMatrix) {
        ThinGL.renderer2D().filledRectangle(positionMatrix, 50, 50, 75, 75, Color.RED);
        ThinGL.renderer2D().filledTriangle(positionMatrix, 50, 50, 75, 110, 100, 50, Color.GREEN);
        ThinGL.renderer2D().filledCircle(positionMatrix, 120, 100, 50, Color.BLUE);
    }

}
