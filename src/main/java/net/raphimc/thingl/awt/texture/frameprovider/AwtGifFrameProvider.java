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

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.awt.AwtUtil;
import net.raphimc.thingl.framebuffer.FramebufferRenderer;
import net.raphimc.thingl.resource.image.texture.Texture2D;
import net.raphimc.thingl.texture.animated.frameprovider.FrameProvider;
import net.raphimc.thingl.util.DefaultGLStates;
import net.raphimc.thingl.util.RenderMathUtil;
import org.lwjgl.opengl.GL11C;
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
import java.util.concurrent.CompletableFuture;

public class AwtGifFrameProvider implements FrameProvider {

    private final ThinGL thinGL;
    private final ImageReader gifReader;
    private final ImageInputStream imageInputStream;
    private final int width;
    private final int height;
    private final FramebufferRenderer frameBuilder;
    private final Texture2D partialTexture;
    private int currentFrame = -1;

    public AwtGifFrameProvider(final byte[] imageBytes) throws IOException {
        this(new ByteArrayInputStream(imageBytes));
    }

    public AwtGifFrameProvider(final InputStream imageStream) throws IOException {
        this.thinGL = ThinGL.get();
        final Iterator<ImageReader> gifReaders = ImageIO.getImageReadersByFormatName("gif");
        if (!gifReaders.hasNext()) {
            throw new RuntimeException("No GIF reader available");
        }
        this.gifReader = gifReaders.next();
        this.imageInputStream = ImageIO.createImageInputStream(imageStream);
        this.gifReader.setInput(this.imageInputStream);

        try {
            this.width = this.gifReader.getWidth(0);
            this.height = this.gifReader.getHeight(0);
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

        final BufferedImage frame = this.gifReader.read(this.currentFrame);
        final IIOMetadata metadata = this.gifReader.getImageMetadata(this.currentFrame);
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
                    ThinGL.renderer2D().texture(RenderMathUtil.getIdentityMatrix(), this.partialTexture, imageLeftPosition, imageTopPosition, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight());
                    for (int y = 0; y < frameBuilderTexture.getHeight(); y++) { // Copy to the image while flipping it vertically
                        frameBuilderTexture.copyTo(target, 0, frameBuilderTexture.getHeight() - 1 - y, 0, y, frameBuilderTexture.getWidth(), 1);
                    }
                    switch (disposalMethod) {
                        case "none", "doNotDispose" -> {
                        }
                        case "restoreToBackgroundColor" -> this.frameBuilder.clear();
                        default -> throw new UnsupportedOperationException("Unsupported disposal method: " + disposalMethod);
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

        return delayTime;
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
        try {
            return this.gifReader.getNumImages(true);
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public void free() {
        if (this.partialTexture != null) {
            this.partialTexture.free();
        }
        if (this.frameBuilder != null) {
            this.frameBuilder.free();
        }
        this.gifReader.dispose();
        try {
            this.imageInputStream.close();
        } catch (IOException ignored) {
        }
    }

}
