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
package net.raphimc.thingl.util;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.DocumentLimits;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.renderer.SVGRenderingHints;
import com.github.weisj.jsvg.view.ViewBox;
import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;
import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.framebuffer.FramebufferRenderer;
import net.raphimc.thingl.resource.image.texture.Texture2D;
import net.raphimc.thingl.resource.image.texture.Texture2DArray;
import net.raphimc.thingl.texture.SequencedTexture;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class AWTUtil {

    public static Color convertColor(final java.awt.Color color) {
        return Color.fromARGB(color.getRGB());
    }

    public static Texture2D createTexture2DFromBufferedImage(final BufferedImage image) {
        return createTexture2DFromBufferedImage(GL11C.GL_RGBA8, image);
    }

    public static Texture2D createTexture2DFromBufferedImage(final int internalFormat, final BufferedImage image) {
        final Texture2D texture = new Texture2D(internalFormat, image.getWidth(), image.getHeight());
        uploadBufferedImageToTexture2D(texture, 0, 0, image);
        return texture;
    }

    public static void uploadBufferedImageToTexture2D(final Texture2D texture, final int x, final int y, final BufferedImage image) {
        final int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        texture.uploadPixels(x, y, image.getWidth(), image.getHeight(), GL12C.GL_BGRA, pixels, false);
    }

    public static void uploadBufferedImageToTexture2DArray(final Texture2DArray texture, final int x, final int y, final int layer, final BufferedImage image) {
        final int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        texture.uploadPixels(x, y, layer, image.getWidth(), image.getHeight(), GL12C.GL_BGRA, pixels, false);
    }

    public static SequencedTexture createSequencedTextureFromGif(final byte[] imageBytes) throws IOException {
        return createSequencedTextureFromGif(new ByteArrayInputStream(imageBytes));
    }

    public static SequencedTexture createSequencedTextureFromGif(final InputStream imageStream) throws IOException {
        final Iterator<ImageReader> gifReaders = ImageIO.getImageReadersByFormatName("gif");
        if (!gifReaders.hasNext()) {
            throw new RuntimeException("No GIF reader available");
        }
        final ImageReader gifReader = gifReaders.next();
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(imageStream)) {
            gifReader.setInput(imageInputStream);
            int frameCount = gifReader.getNumImages(true);
            if (frameCount > ThinGL.capabilities().getMaxArrayTextureLayers()) {
                ThinGL.LOGGER.warn("GIF has more frames (" + frameCount + ") than the maximum supported by the GPU (" + ThinGL.capabilities().getMaxArrayTextureLayers() + "). Using the maximum supported frames.");
                frameCount = ThinGL.capabilities().getMaxArrayTextureLayers();
            }
            final int width = gifReader.getWidth(0);
            final int height = gifReader.getHeight(0);

            final SequencedTexture sequencedTexture = new SequencedTexture(GL11C.GL_RGBA8, width, height, frameCount);
            final Texture2D partialTexture = new Texture2D(GL11C.GL_RGBA8, width, height);
            final FramebufferRenderer frameBuilder = new FramebufferRenderer(width, height, false);
            final Texture2D frameBuilderTexture = frameBuilder.getColorAttachment();
            frameBuilder.begin();
            DefaultGLStates.push();
            try {
                int relativeTime = 0;
                for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                    final BufferedImage frame = gifReader.read(frameIndex);
                    final IIOMetadata metadata = gifReader.getImageMetadata(frameIndex);
                    final Node metadataTree = metadata.getAsTree(metadata.getNativeMetadataFormatName());
                    Node graphicControlExtensionNode = null;
                    Node imageDescriptorNode = null;
                    for (int i = 0; i < metadataTree.getChildNodes().getLength(); i++) {
                        final Node childNode = metadataTree.getChildNodes().item(i);
                        if (childNode.getNodeName().equals("GraphicControlExtension")) {
                            graphicControlExtensionNode = childNode;
                        } else if (childNode.getNodeName().equals("ImageDescriptor")) {
                            imageDescriptorNode = childNode;
                        }
                    }
                    if (graphicControlExtensionNode == null || imageDescriptorNode == null) {
                        throw new IOException("Invalid GIF metadata");
                    }
                    final int delayTime = Integer.parseInt(graphicControlExtensionNode.getAttributes().getNamedItem("delayTime").getNodeValue()) * 10;
                    final String disposalMethod = graphicControlExtensionNode.getAttributes().getNamedItem("disposalMethod").getNodeValue();
                    final int imageLeftPosition = Integer.parseInt(imageDescriptorNode.getAttributes().getNamedItem("imageLeftPosition").getNodeValue());
                    final int imageTopPosition = Integer.parseInt(imageDescriptorNode.getAttributes().getNamedItem("imageTopPosition").getNodeValue());

                    AWTUtil.uploadBufferedImageToTexture2D(partialTexture, 0, 0, frame);
                    ThinGL.renderer2D().texture(RenderMathUtil.getIdentityMatrix(), partialTexture, imageLeftPosition, imageTopPosition, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight());
                    for (int y = 0; y < sequencedTexture.getHeight(); y++) { // Copy to the image while flipping it vertically
                        frameBuilderTexture.copyTo(sequencedTexture, 0, sequencedTexture.getHeight() - 1 - y, 0, y, frameIndex, sequencedTexture.getWidth(), 1);
                    }
                    switch (disposalMethod) {
                        case "none", "doNotDispose" -> {
                        }
                        case "restoreToBackgroundColor" -> frameBuilder.clear();
                        default -> throw new UnsupportedOperationException("Unsupported disposal method: " + disposalMethod);
                    }

                    sequencedTexture.getFrameTimes().put(relativeTime, frameIndex);
                    relativeTime += delayTime;
                }
                sequencedTexture.getFrameTimes().put(relativeTime, frameCount - 1);
            } catch (Throwable e) {
                sequencedTexture.free();
                throw e;
            } finally {
                frameBuilder.end();
                DefaultGLStates.pop();
                frameBuilder.free();
                partialTexture.free();
            }
            return sequencedTexture;
        } finally {
            gifReader.dispose();
        }
    }

    public static SequencedTexture createSequencedTextureFromWebp(final byte[] imageBytes) throws IOException {
        return createSequencedTextureFromWebp(new ByteArrayInputStream(imageBytes));
    }

    public static SequencedTexture createSequencedTextureFromWebp(final InputStream imageStream) throws IOException {
        ThinGL.capabilities().ensureTwelveMonkeysWebpReaderPresent();
        final ImageReader webpReader = new WebPImageReaderSpi().createReaderInstance();
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(imageStream)) {
            webpReader.setInput(imageInputStream);
            int frameCount = webpReader.getNumImages(true);
            if (frameCount > ThinGL.capabilities().getMaxArrayTextureLayers()) {
                ThinGL.LOGGER.warn("WebP has more frames (" + frameCount + ") than the maximum supported by the GPU (" + ThinGL.capabilities().getMaxArrayTextureLayers() + "). Using the maximum supported frames.");
                frameCount = ThinGL.capabilities().getMaxArrayTextureLayers();
            }

            final Object header = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.WebPImageReader", "header").apply(webpReader);
            final int width = (int) ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.VP8xChunk", "width").apply(header);
            final int height = (int) ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.VP8xChunk", "height").apply(header);
            final boolean isAnimated = (boolean) ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.VP8xChunk", "containsANIM").apply(header);
            if (!isAnimated) {
                throw new UnsupportedOperationException("WebP image is not animated, cannot create sequenced texture");
            }

            final List<?> frames = (List<?>) ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.WebPImageReader", "frames").apply(webpReader);
            final Function<Object, Object> boundsGetter = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame", "bounds");
            final Function<Object, Object> durationGetter = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame", "duration");
            final Function<Object, Object> blendGetter = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame", "blend");
            final Function<Object, Object> disposeGetter = ReflectionUtil.createGetter("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame", "dispose");

            final SequencedTexture sequencedTexture = new SequencedTexture(GL11C.GL_RGBA8, width, height, frameCount);
            final Texture2D partialTexture = new Texture2D(GL11C.GL_RGBA8, width, height);
            final FramebufferRenderer frameBuilder = new FramebufferRenderer(width, height, false);
            final Texture2D frameBuilderTexture = frameBuilder.getColorAttachment();
            DefaultGLStates.push();
            frameBuilder.begin();
            try {
                int relativeTime = 0;
                for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                    final BufferedImage frame = webpReader.read(frameIndex);
                    final Object animationFrame = frames.get(frameIndex);
                    final Rectangle bounds = (Rectangle) boundsGetter.apply(animationFrame);
                    final boolean blend = (boolean) blendGetter.apply(animationFrame);
                    final boolean dispose = (boolean) disposeGetter.apply(animationFrame);

                    AWTUtil.uploadBufferedImageToTexture2D(partialTexture, 0, 0, frame);
                    if (blend) {
                        ThinGL.renderer2D().texture(RenderMathUtil.getIdentityMatrix(), partialTexture, bounds.x, bounds.y, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight());
                    } else {
                        for (int y = 0; y < frame.getHeight(); y++) { // Copy to the frame builder while flipping it vertically
                            partialTexture.copyTo(frameBuilderTexture, 0, y, bounds.x, frameBuilderTexture.getHeight() - 1 - bounds.y - y, frame.getWidth(), 1);
                        }
                    }
                    for (int y = 0; y < sequencedTexture.getHeight(); y++) { // Copy to the image while flipping it vertically
                        frameBuilderTexture.copyTo(sequencedTexture, 0, sequencedTexture.getHeight() - 1 - y, 0, y, frameIndex, sequencedTexture.getWidth(), 1);
                    }
                    if (dispose) {
                        frameBuilder.clear();
                    }

                    sequencedTexture.getFrameTimes().put(relativeTime, frameIndex);
                    relativeTime += (int) durationGetter.apply(animationFrame);
                }
                sequencedTexture.getFrameTimes().put(relativeTime, frameCount - 1);
            } catch (Throwable e) {
                sequencedTexture.free();
                throw e;
            } finally {
                frameBuilder.end();
                DefaultGLStates.pop();
                frameBuilder.free();
                partialTexture.free();
            }
            return sequencedTexture;
        } finally {
            webpReader.dispose();
        }
    }

    public static Texture2D createTexture2DFromSvg(final byte[] documentBytes) {
        return createTexture2DFromSvg(new ByteArrayInputStream(documentBytes));
    }

    public static Texture2D createTexture2DFromSvg(final byte[] documentBytes, final int width, final int height) {
        return createTexture2DFromSvg(new ByteArrayInputStream(documentBytes), width, height);
    }

    public static Texture2D createTexture2DFromSvg(final byte[] documentBytes, final float scale) {
        return createTexture2DFromSvg(new ByteArrayInputStream(documentBytes), scale);
    }

    public static Texture2D createTexture2DFromSvg(final InputStream documentStream) {
        return createTexture2DFromSvg(documentStream, null, null, null);
    }

    public static Texture2D createTexture2DFromSvg(final InputStream documentStream, final int width, final int height) {
        return createTexture2DFromSvg(documentStream, width, height, null);
    }

    public static Texture2D createTexture2DFromSvg(final InputStream documentStream, final float scale) {
        return createTexture2DFromSvg(documentStream, null, null, scale);
    }

    private static Texture2D createTexture2DFromSvg(final InputStream documentStream, final Integer targetWidth, final Integer targetHeight, final Float targetScale) {
        ThinGL.capabilities().ensureJsvgPresent();
        final SVGDocument document = new SVGLoader().load(documentStream, null, LoaderContext.builder().documentLimits(new DocumentLimits(DocumentLimits.DEFAULT_MAX_USE_NESTING_DEPTH, DocumentLimits.DEFAULT_MAX_NESTING_DEPTH, Integer.MAX_VALUE)).build());
        if (document == null) {
            throw new RuntimeException("Failed to load SVG document");
        }

        int width = targetWidth != null ? targetWidth : Math.round(document.size().width);
        int height = targetHeight != null ? targetHeight : Math.round(document.size().height);
        if (targetScale != null) {
            width = Math.round(width * targetScale);
            height = Math.round(height * targetScale);
        }

        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics.setRenderingHint(SVGRenderingHints.KEY_IMAGE_ANTIALIASING, SVGRenderingHints.VALUE_IMAGE_ANTIALIASING_ON);
        graphics.setRenderingHint(SVGRenderingHints.KEY_CACHE_OFFSCREEN_IMAGE, SVGRenderingHints.VALUE_NO_CACHE);
        document.render(null, graphics, new ViewBox(width, height));
        graphics.dispose();

        return createTexture2DFromBufferedImage(image);
    }

}
