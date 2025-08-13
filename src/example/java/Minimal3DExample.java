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
import org.joml.primitives.AABBf;

public class Minimal3DExample extends StandaloneApplicationRunner {

    public static void main(String[] args) {
        new Minimal3DExample().launch();
    }

    public Minimal3DExample() {
        super(new Configuration().setWindowTitle("ThinGL Example - Minimal 3D Example").setExtendedDebugMode(true));
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        positionMatrix.rotateY((float) Math.toRadians((System.currentTimeMillis() % 3600L) / 10F));

        final AABBf box = new AABBf(-0.5F, -0.5F, -0.5F, 0.5F, 0.5F, 0.5F);
        ThinGL.renderer3D().outlineBox(positionMatrix, box, 2, Color.GREEN);
    }

    @Override
    protected void loadProjectionMatrix(final float width, final float height) {
        ThinGL.globalUniforms().getProjectionMatrix().setPerspective((float) Math.toRadians(90F), width / height, 0.1F, 1000F);

        ThinGL.globalUniforms().getViewMatrix().setLookAt(
                1F, 1F, 1F,
                0F, 0F, 0F,
                0F, 1F, 0F
        );
    }

}
