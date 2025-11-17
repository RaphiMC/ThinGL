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

import net.raphimc.thingl.image.animated.AnimatedImage;
import net.raphimc.thingl.image.io.impl.awt.AwtImageIO;
import net.raphimc.thingl.resource.image.impl.ByteImage2D;
import org.lwjgl.opengl.GL12C;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;

public class AwtGifImage extends AnimatedImage {

    private final InputStream imageStream;
    private final ImageReader gifReader;
    private final ImageInputStream imageInputStream;
    private final ByteImage2D compositeImage;
    private int currentFrameIndex = -1;

    public AwtGifImage(final byte[] imageBytes) throws IOException {
        this(new ByteArrayInputStream(imageBytes));
    }

    public AwtGifImage(final InputStream imageStream) throws IOException {
        this(InitialInput.create(imageStream));
    }

    private AwtGifImage(final InitialInput initialInput) throws IOException {
        super(initialInput.gifReader.getWidth(0), initialInput.gifReader.getHeight(0), initialInput.gifReader.getNumImages(true), GL12C.GL_BGRA);
        this.imageStream = initialInput.imageStream;
        this.gifReader = initialInput.gifReader;
        this.imageInputStream = initialInput.imageInputStream;
        this.compositeImage = new ByteImage2D(this.getWidth(), this.getHeight(), GL12C.GL_BGRA);
        this.compositeImage.clear();
    }

    @Override
    public int loadNextFrame() {
        this.currentFrameIndex++;

        try {
            final BufferedImage frame = this.gifReader.read(this.currentFrameIndex);
            final IIOMetadata metadata = this.gifReader.getImageMetadata(this.currentFrameIndex);
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

            this.compositeImage.getPixels().copyTo(this.getPixels());
            final ByteImage2D image = AwtImageIO.INSTANCE.createByteImage2D(frame);
            try {
                this.drawImage(image, imageLeftPosition, imageTopPosition, TransparencyMode.MASKED);
            } finally {
                image.free();
            }
            switch (disposalMethod) {
                case "none", "doNotDispose" -> this.getPixels().copyTo(this.compositeImage.getPixels());
                case "restoreToBackgroundColor" -> this.compositeImage.clear(imageLeftPosition, imageTopPosition, frame.getWidth(), frame.getHeight());
                case "restoreToPrevious" -> {
                }
                default -> throw new UnsupportedOperationException("Unsupported disposal method: " + disposalMethod);
            }

            return delayTime;
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
        this.gifReader.dispose();
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

    private record InitialInput(InputStream imageStream, ImageReader gifReader, ImageInputStream imageInputStream) {

        public static InitialInput create(final InputStream imageStream) throws IOException {
            final Iterator<ImageReader> gifReaders = ImageIO.getImageReadersByFormatName("gif");
            if (!gifReaders.hasNext()) {
                throw new RuntimeException("No GIF reader available");
            }
            final ImageReader gifReader = gifReaders.next();
            final ImageInputStream imageInputStream = ImageIO.createImageInputStream(imageStream);
            gifReader.setInput(imageInputStream);
            return new InitialInput(imageStream, gifReader, imageInputStream);
        }

    }

}
