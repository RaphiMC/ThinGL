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

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.application.GLFWApplicationRunner;
import net.raphimc.thingl.resource.image.texture.Texture2D;
import org.joml.Matrix4fStack;

import java.io.IOException;

public class ImageRenderingExample extends GLFWApplicationRunner {

    public static void main(String[] args) {
        new ImageRenderingExample().launch();
    }

    public ImageRenderingExample() {
        super(new Configuration().setWindowTitle("ThinGL Example - Image rendering").setExtendedDebugMode(true));
    }

    private Texture2D image;

    @Override
    protected void init() {
        super.init();
        try {
            final byte[] imageBytes = ImageRenderingExample.class.getResourceAsStream("/images/triangles-1430105_640.png").readAllBytes();
            this.image = Texture2D.fromImage(imageBytes); // Standard image formats like PNG, JPEG, BMP, and GIF are supported
            // this.image = AWTUtil.createTexture2DFromBufferedImage(...); // Its also possible to create a Texture2D from a BufferedImage directly
            // this.image = AWTUtil.createTexture2DFromSvg(...); // SVG is also supported, but requires a library
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        ThinGL.renderer2D().texture(positionMatrix, this.image, 50, 50);
    }

}
