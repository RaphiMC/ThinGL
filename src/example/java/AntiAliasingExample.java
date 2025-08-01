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
import net.raphimc.thingl.implementation.application.StandaloneApplicationRunner;
import org.joml.Matrix4fStack;

public class AntiAliasingExample extends StandaloneApplicationRunner {

    public static void main(String[] args) {
        new AntiAliasingExample().launch();
    }

    public AntiAliasingExample() {
        super(new Configuration().setWindowTitle("ThinGL Example - AntiAliasing").setExtendedDebugMode(true));
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        ThinGL.programs().getMsaa().bindInput();

        final float rectX = 100;
        final float rectY = 100;
        final float rectW = 300;
        final float rectH = 200;
        final float centerX = rectX + rectW / 2F;
        final float centerY = rectY + rectH / 2F;
        final float angle = (System.currentTimeMillis() % 8000) / 8000F * (float) Math.PI * 2;

        positionMatrix.translate(centerX, centerY, 0);
        positionMatrix.rotateZ(angle);
        positionMatrix.translate(-rectW / 2F, -rectH / 2F, 0);

        ThinGL.renderer2D().filledRoundedRectangle(positionMatrix, 0, 0, rectW, rectH, 40, Color.GRAY);

        ThinGL.programs().getMsaa().unbindInput();
        ThinGL.programs().getMsaa().renderFullscreenQuad();
        ThinGL.programs().getMsaa().clearInput();
    }

}
