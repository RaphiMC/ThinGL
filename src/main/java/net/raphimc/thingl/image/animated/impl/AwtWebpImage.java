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
package net.raphimc.thingl.image.animated.impl;

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;
import net.raphimc.thingl.image.animated.AnimatedImage;
import net.raphimc.thingl.image.io.impl.awt.AwtImageIO;
import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.resource.image.impl.ByteImage2D;
import net.raphimc.thingl.util.ReflectionUtil;
import org.lwjgl.opengl.GL12C;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Function;

public class AwtWebpImage extends AnimatedImage {

    static {
        Capabilities.assertTwelveMonkeysWebpReaderAvailable();
    }

    private final InputStream imageStream;
    private final ImageReader webpReader;
    private final ImageInputStream imageInputStream;
    private final List<?> frames;
    private final Function<Object, Object> frameBoundsGetter;
    private final Function<Object, Object> frameDurationGetter;
    private final Function<Object, Object> frameBlendGetter;
    private final Function<Object, Object> frameDisposeGetter;
    private final ByteImage2D compositeImage;
    private int currentFrameIndex = -1;

    public AwtWebpImage(final byte[] imageBytes) throws IOException {
        this(new ByteArrayInputStream(imageBytes));
    }

    public AwtWebpImage(final InputStream imageStream) throws IOException {
        this(InitialInput.create(imageStream));
    }

    private AwtWebpImage(final InitialInput initialInput) throws IOException {
        super(initialInput.width, initialInput.height, initialInput.webpReader.getNumImages(true), GL12C.GL_BGRA);
        this.imageStream = initialInput.imageStream;
        this.webpReader = initialInput.webpReader;
        this.imageInputStream = initialInput.imageInputStream;
        this.frames = (List<?>) ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.WebPImageReader", "frames").apply(this.webpReader);
        this.frameBoundsGetter = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame", "bounds");
        this.frameDurationGetter = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame", "duration");
        this.frameBlendGetter = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame", "blend");
        this.frameDisposeGetter = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame", "dispose");
        this.compositeImage = new ByteImage2D(this.getWidth(), this.getHeight(), GL12C.GL_BGRA);
        this.compositeImage.clear();
    }

    @Override
    public int loadNextFrame() {
        this.currentFrameIndex++;

        try {
            final BufferedImage frame = this.webpReader.read(this.currentFrameIndex);
            final Object animationFrame = this.frames.get(this.currentFrameIndex);
            final Rectangle bounds = (Rectangle) this.frameBoundsGetter.apply(animationFrame);
            final int duration = (int) this.frameDurationGetter.apply(animationFrame);
            final boolean blend = (boolean) this.frameBlendGetter.apply(animationFrame);
            final boolean dispose = (boolean) this.frameDisposeGetter.apply(animationFrame);

            this.compositeImage.getPixels().copyTo(this.getPixels());
            final ByteImage2D image = AwtImageIO.INSTANCE.createByteImage2D(frame);
            try {
                this.drawImage(image, bounds.x, bounds.y, blend ? TransparencyMode.ALPHA_BLENDED : TransparencyMode.OPAQUE);
            } finally {
                image.free();
            }
            if (dispose) {
                this.compositeImage.clear(bounds.x, bounds.y, frame.getWidth(), frame.getHeight());
            } else {
                this.getPixels().copyTo(this.compositeImage.getPixels());
            }

            return duration;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean hasMoreFrames() {
        return this.currentFrameIndex + 1 < this.getFrameCount();
    }

    @Override
    protected void free0() {
        this.compositeImage.free();
        this.webpReader.dispose();
        try {
            this.imageInputStream.close();
        } catch (IOException ignored) {
        }
        try {
            this.imageStream.close();
        } catch (IOException ignored) {
        }
        super.free0();
    }

    private record InitialInput(InputStream imageStream, ImageReader webpReader, ImageInputStream imageInputStream, int width, int height) {

        public static InitialInput create(final InputStream imageStream) throws IOException {
            final ImageReader webpReader = new WebPImageReaderSpi().createReaderInstance();
            final ImageInputStream imageInputStream = ImageIO.createImageInputStream(imageStream);
            webpReader.setInput(imageInputStream);
            webpReader.getNumImages(false); // Force read the header
            final Object header = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.WebPImageReader", "header").apply(webpReader);
            final int width = (int) ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.VP8xChunk", "width").apply(header);
            final int height = (int) ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.VP8xChunk", "height").apply(header);
            final boolean isAnimated = (boolean) ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.VP8xChunk", "containsANIM").apply(header);
            if (!isAnimated) {
                throw new UnsupportedOperationException("WebP image is not animated");
            }
            return new InitialInput(imageStream, webpReader, imageInputStream, width, height);
        }

    }

}
