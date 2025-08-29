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
package net.raphimc.thingl.awt.texture.frameprovider;

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.awt.AwtUtil;
import net.raphimc.thingl.framebuffer.FramebufferRenderer;
import net.raphimc.thingl.resource.image.texture.Texture2D;
import net.raphimc.thingl.texture.animated.frameprovider.FrameProvider;
import net.raphimc.thingl.util.DefaultGLStates;
import net.raphimc.thingl.util.ReflectionUtil;
import net.raphimc.thingl.util.RenderMathUtil;
import org.lwjgl.opengl.GL11C;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AwtWebpFrameProvider implements FrameProvider {

    private final ThinGL thinGL;
    private final ImageReader webpReader;
    private final ImageInputStream imageInputStream;
    private final int width;
    private final int height;
    private final FramebufferRenderer frameBuilder;
    private final Texture2D partialTexture;
    private final List<?> frames;
    private final Function<Object, Object> frameBoundsGetter;
    private final Function<Object, Object> frameDurationGetter;
    private final Function<Object, Object> frameBlendGetter;
    private final Function<Object, Object> frameDisposeGetter;
    private int frameCount = -1;
    private int currentFrame = -1;

    public AwtWebpFrameProvider(final byte[] imageBytes) throws IOException {
        this(new ByteArrayInputStream(imageBytes));
    }

    public AwtWebpFrameProvider(final InputStream imageStream) throws IOException {
        ThinGL.capabilities().ensureTwelveMonkeysWebpReaderPresent();
        this.thinGL = ThinGL.get();
        this.webpReader = new WebPImageReaderSpi().createReaderInstance();
        this.imageInputStream = ImageIO.createImageInputStream(imageStream);
        this.webpReader.setInput(this.imageInputStream);

        try {
            this.webpReader.getNumImages(false); // Force read the header
            final Object header = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.WebPImageReader", "header").apply(this.webpReader);
            this.width = (int) ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.VP8xChunk", "width").apply(header);
            this.height = (int) ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.VP8xChunk", "height").apply(header);
            final boolean isAnimated = (boolean) ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.VP8xChunk", "containsANIM").apply(header);
            if (!isAnimated) {
                throw new UnsupportedOperationException("WebP image is not animated");
            }

            this.frames = (List<?>) ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.WebPImageReader", "frames").apply(this.webpReader);
            this.frameBoundsGetter = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame", "bounds");
            this.frameDurationGetter = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame", "duration");
            this.frameBlendGetter = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame", "blend");
            this.frameDisposeGetter = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame", "dispose");

            this.frameBuilder = new FramebufferRenderer(this.width, this.height, false);
            this.partialTexture = new Texture2D(GL11C.GL_RGBA8, this.width, this.height);
        } catch (Throwable e) {
            this.free();
            throw e;
        }
    }

    @Override
    public int loadNextFrame(final Texture2D target) throws IOException {
        if (this.currentFrame + 1 >= this.getFrameCount()) {
            return -1;
        }
        this.currentFrame++;

        final BufferedImage frame = this.webpReader.read(this.currentFrame);
        final Object animationFrame = this.frames.get(this.currentFrame);
        final Rectangle bounds = (Rectangle) this.frameBoundsGetter.apply(animationFrame);
        final int duration = (int) this.frameDurationGetter.apply(animationFrame);
        final boolean blend = (boolean) this.frameBlendGetter.apply(animationFrame);
        final boolean dispose = (boolean) this.frameDisposeGetter.apply(animationFrame);

        if (!this.thinGL.isAllocated()) { // If ThinGL was freed while the image was loading
            return -1;
        }

        final CompletableFuture<Void> uploadFuture = new CompletableFuture<>();
        this.thinGL.runOnRenderThread(() -> {
            try {
                this.frameBuilder.begin();
                DefaultGLStates.push();
                try {
                    final Texture2D frameBuilderTexture = this.frameBuilder.getColorAttachment();
                    AwtUtil.uploadBufferedImageToTexture2D(this.partialTexture, 0, 0, frame);
                    if (blend) {
                        ThinGL.renderer2D().texture(RenderMathUtil.getIdentityMatrix(), this.partialTexture, bounds.x, bounds.y, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight());
                    } else {
                        for (int y = 0; y < frame.getHeight(); y++) { // Copy to the frame builder while flipping it vertically
                            this.partialTexture.copyTo(frameBuilderTexture, 0, y, bounds.x, frameBuilderTexture.getHeight() - 1 - bounds.y - y, frame.getWidth(), 1);
                        }
                    }
                    for (int y = 0; y < frameBuilderTexture.getHeight(); y++) { // Copy to the image while flipping it vertically
                        frameBuilderTexture.copyTo(target, 0, frameBuilderTexture.getHeight() - 1 - y, 0, y, frameBuilderTexture.getWidth(), 1);
                    }
                    if (dispose) {
                        this.frameBuilder.clear();
                    }
                } finally {
                    DefaultGLStates.pop();
                    this.frameBuilder.end();
                }
            } finally {
                uploadFuture.complete(null);
            }
        });
        uploadFuture.join();

        return duration;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getFrameCount() {
        if (this.frameCount == -1) {
            try {
                this.frameCount = this.webpReader.getNumImages(true);
            } catch (IOException e) {
                this.frameCount = 0;
            }
        }
        return this.frameCount;
    }

    @Override
    public void free() {
        if (this.partialTexture != null) {
            this.partialTexture.free();
        }
        if (this.frameBuilder != null) {
            this.frameBuilder.free();
        }
        this.webpReader.dispose();
        try {
            this.imageInputStream.close();
        } catch (IOException ignored) {
        }
    }

}
