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
import net.raphimc.thingl.awt.texture.frameprovider.AwtGifFrameProvider;
import net.raphimc.thingl.implementation.application.GLFWApplicationRunner;
import net.raphimc.thingl.texture.animated.SequencedTexture;
import org.joml.Matrix4fStack;

import java.io.IOException;

public class AnimatedImageRenderingExample extends GLFWApplicationRunner {

    public static void main(String[] args) {
        new AnimatedImageRenderingExample().launch();
    }

    public AnimatedImageRenderingExample() {
        super(new Configuration().setWindowTitle("ThinGL Example - Animated Image rendering").setExtendedDebugMode(true));
    }

    private SequencedTexture image;
    private long startTime;

    @Override
    protected void init() {
        super.init();
        try {
            final byte[] imageBytes = AnimatedImageRenderingExample.class.getResourceAsStream("/images/hand.gif").readAllBytes();
            this.image = new SequencedTexture(new AwtGifFrameProvider(imageBytes));
            // this.image = new SequencedTexture(new GifFrameProvider(imageBytes)); // Alternative method which uses a library. This is faster than using AWT
            // this.image = new SequencedTexture(new AwtWebpFrameProvider(imageBytes)); // WebP is also supported, but requires a library
            this.startTime = System.currentTimeMillis();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        final int time = (int) (System.currentTimeMillis() - this.startTime);
        ThinGL.renderer2D().textureArrayLayer(positionMatrix, this.image, this.image.getFrameIndex(time), 50, 50);
    }

}
