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
import net.raphimc.thingl.resource.image.texture.Texture2D;
import org.joml.Matrix4fStack;

import java.io.IOException;

public class BlurExample extends StandaloneApplicationRunner {

    public static void main(String[] args) {
        new BlurExample().launch();
    }

    public BlurExample() {
        super(new Configuration().setWindowTitle("ThinGL Example - Blur").setExtendedDebugMode(true));
    }

    private Texture2D image;

    @Override
    protected void init() {
        super.init();
        try {
            final byte[] imageBytes = BlurExample.class.getResourceAsStream("/images/triangles-1430105_640.png").readAllBytes();
            this.image = Texture2D.fromImage(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        ThinGL.renderer2D().texture(positionMatrix, this.image.getGlId(), 50, 50, this.image.getWidth(), this.image.getHeight());

        final float x = (System.currentTimeMillis() % 5000) / 5000F * (this.image.getWidth() * 1.25F);
        ThinGL.programs().getGaussianBlur().bindInput();
        ThinGL.renderer2D().filledCircle(positionMatrix, x, 200, 75, Color.RED);
        ThinGL.programs().getGaussianBlur().unbindInput();
        ThinGL.programs().getGaussianBlur().configureParameters(10); // Configure the blur radius
        ThinGL.programs().getGaussianBlur().renderScaledQuad(50, 50, 50 + this.image.getWidth(), 50 + this.image.getHeight());
        ThinGL.programs().getGaussianBlur().clearInput();
    }

}
