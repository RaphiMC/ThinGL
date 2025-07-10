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

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.framebuffer.FramebufferRenderer;
import net.raphimc.thingl.resource.texture.AbstractTexture;
import net.raphimc.thingl.resource.texture.Texture2D;
import net.raphimc.thingl.texture.SequencedTexture;
import org.lwjgl.opengl.GL45C;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class AWTUtil {

    public static Color convertColor(final java.awt.Color color) {
        return Color.fromARGB(color.getRGB());
    }

    public static Texture2D createTextureFromBufferedImage(final BufferedImage bufferedImage) {
        return createTextureFromBufferedImage(AbstractTexture.InternalFormat.RGBA8, bufferedImage);
    }

    public static Texture2D createTextureFromBufferedImage(final AbstractTexture.InternalFormat internalFormat, final BufferedImage bufferedImage) {
        final Texture2D texture = new Texture2D(internalFormat, bufferedImage.getWidth(), bufferedImage.getHeight());
        uploadBufferedImageToTexture2D(texture, 0, 0, bufferedImage);
        return texture;
    }

    public static void uploadBufferedImageToTexture2D(final Texture2D texture, final int x, final int y, final BufferedImage bufferedImage) {
        final int[] pixels = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), pixels, 0, bufferedImage.getWidth());
        texture.uploadPixels(x, y, bufferedImage.getWidth(), bufferedImage.getHeight(), AbstractTexture.PixelFormat.BGRA, pixels, false);
    }

    public static SequencedTexture createSequencedTextureFromGif(final byte[] imageData) throws IOException {
        return createSequencedTextureFromGif(new ByteArrayInputStream(imageData));
    }

    public static SequencedTexture createSequencedTextureFromGif(final InputStream imageDataStream) throws IOException {
        final Iterator<ImageReader> gifReaders = ImageIO.getImageReadersByFormatName("gif");
        if (!gifReaders.hasNext()) {
            throw new RuntimeException("No GIF reader available");
        }
        final ImageReader gifReader = gifReaders.next();
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(imageDataStream)) {
            gifReader.setInput(imageInputStream);
            int frameCount = gifReader.getNumImages(true);
            if (frameCount > ThinGL.capabilities().getMaxArrayTextureLayers()) {
                ThinGL.LOGGER.warn("GIF has more frames (" + frameCount + ") than the maximum supported by the GPU (" + ThinGL.capabilities().getMaxArrayTextureLayers() + "). Using the maximum supported frames.");
                frameCount = ThinGL.capabilities().getMaxArrayTextureLayers();
            }
            final int width = gifReader.getWidth(0);
            final int height = gifReader.getHeight(0);

            final SequencedTexture sequencedTexture = new SequencedTexture(AbstractTexture.InternalFormat.RGBA8, width, height, frameCount);
            final Texture2D partialTexture = new Texture2D(AbstractTexture.InternalFormat.RGBA8, width, height);
            final FramebufferRenderer frameBuilder = new FramebufferRenderer(width, height, false);
            frameBuilder.begin();
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
                    ThinGL.renderer2D().texture(RenderMathUtil.getIdentityMatrix(), partialTexture.getGlId(), imageLeftPosition, imageTopPosition, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight(), partialTexture.getWidth(), partialTexture.getHeight());
                    for (int y = 0; y < sequencedTexture.getHeight(); y++) { // Copy to the image while flipping it vertically
                        GL45C.glCopyTextureSubImage3D(sequencedTexture.getGlId(), 0, 0, y, frameIndex, 0, sequencedTexture.getHeight() - 1 - y, sequencedTexture.getWidth(), 1);
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
                frameBuilder.free();
                partialTexture.free();
            }
            return sequencedTexture;
        } finally {
            gifReader.dispose();
        }
    }

}
