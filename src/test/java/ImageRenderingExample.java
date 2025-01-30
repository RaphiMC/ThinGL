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
import net.raphimc.thingl.renderer.impl.Renderer2D;
import net.raphimc.thingl.resource.texture.AbstractTexture;
import net.raphimc.thingl.resource.texture.Texture2D;
import org.joml.Matrix4fStack;

import java.io.IOException;

public class ImageRenderingExample extends ExampleBase {

    public static void main(String[] args) {
        new ImageRenderingExample().run();
    }

    private Texture2D image;

    @Override
    protected void init() {
        try {
            final byte[] imageData = TextRenderingExample.class.getResourceAsStream("/images/triangles-1430105_640.png").readAllBytes();
            this.image = new Texture2D(AbstractTexture.InternalFormat.RGBA8, imageData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        Renderer2D.INSTANCE.texture(positionMatrix, this.image.getGlId(), 50, 50, this.image.getWidth(), this.image.getHeight());
    }

}
