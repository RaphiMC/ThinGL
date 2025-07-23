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

package net.raphimc.thingl.resource.framebuffer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.framebuffer.impl.WindowFramebuffer;
import net.raphimc.thingl.resource.GLContainerObject;
import net.raphimc.thingl.resource.image.ImageStorage;
import net.raphimc.thingl.resource.image.renderbuffer.RenderBuffer;
import net.raphimc.thingl.resource.image.texture.ImageTexture;
import net.raphimc.thingl.resource.image.texture.Texture;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL45C;

public class Framebuffer extends GLContainerObject {

    private final Int2ObjectMap<ImageStorage> attachments = new Int2ObjectOpenHashMap<>();

    private Color clearColor = Color.TRANSPARENT;
    private float clearDepth = 1F;
    private int clearStencil = 0;

    public Framebuffer() {
        super(GL45C.glCreateFramebuffers());
        for (int attachment : this.getValidAttachmentPoints()) {
            this.attachments.put(attachment, null);
        }
    }

    public Framebuffer(final ImageStorage colorAttachment) {
        this(colorAttachment, null, null);
    }

    public Framebuffer(final ImageStorage colorAttachment, final ImageStorage depthAttachment) {
        this(colorAttachment, depthAttachment, null);
    }

    public Framebuffer(final ImageStorage colorAttachment, final ImageStorage depthAttachment, final ImageStorage stencilAttachment) {
        super(GL45C.glCreateFramebuffers());
        for (int attachment : this.getValidAttachmentPoints()) {
            this.attachments.put(attachment, null);
        }
        try {
            if (colorAttachment != null) {
                this.setAttachment(GL30C.GL_COLOR_ATTACHMENT0, colorAttachment);
            }
            if (depthAttachment == stencilAttachment && depthAttachment != null) {
                this.setAttachment(GL30C.GL_DEPTH_STENCIL_ATTACHMENT, depthAttachment);
            } else {
                if (depthAttachment != null) {
                    this.setAttachment(GL30C.GL_DEPTH_ATTACHMENT, depthAttachment);
                }
                if (stencilAttachment != null) {
                    this.setAttachment(GL30C.GL_STENCIL_ATTACHMENT, stencilAttachment);
                }
            }
            this.checkStatus();
            this.clear();
        } catch (Throwable e) {
            this.free();
            throw e;
        }
    }

    protected Framebuffer(final int glId) {
        super(glId);
    }

    public static Framebuffer fromGlId(final int glId) {
        if (glId == 0) {
            return WindowFramebuffer.INSTANCE;
        }
        if (!GL30C.glIsFramebuffer(glId)) {
            throw new IllegalArgumentException("Not a framebuffer object");
        }
        return new Framebuffer(glId);
    }

    public void checkStatus() {
        final int status = GL45C.glCheckNamedFramebufferStatus(this.getGlId(), GL30C.GL_FRAMEBUFFER);
        switch (status) {
            case GL30C.GL_FRAMEBUFFER_COMPLETE:
                break;
            case GL30C.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                throw new IllegalStateException("Framebuffer is not complete: GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
            case GL30C.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                throw new IllegalStateException("Framebuffer is not complete: GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
            case GL30C.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                throw new IllegalStateException("Framebuffer is not complete: GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
            case GL30C.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                throw new IllegalStateException("Framebuffer is not complete: GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
            case GL30C.GL_FRAMEBUFFER_UNSUPPORTED:
                throw new IllegalStateException("Framebuffer is not complete: GL_FRAMEBUFFER_UNSUPPORTED");
            case GL30C.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
                throw new IllegalStateException("Framebuffer is not complete: GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
            case GL32C.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS:
                throw new IllegalStateException("Framebuffer is not complete: GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
            case GL11C.GL_OUT_OF_MEMORY:
                throw new IllegalStateException("Framebuffer is not complete: GL_OUT_OF_MEMORY");
            default:
                throw new IllegalStateException("Framebuffer is not complete: " + status);
        }
    }

    public void bind() {
        this.bind(false);
    }

    public void bind(final boolean setViewport) {
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, this.getGlId());
        ThinGL.applicationInterface().setCurrentFramebuffer(this);
        if (setViewport) {
            ThinGL.glStateManager().setViewport(0, 0, this.getWidth(), this.getHeight());
        }
    }

    public void unbind() {
        WindowFramebuffer.INSTANCE.bind(true);
    }

    public void clear() {
        this.clearColor();
        this.clearDepthAndStencil();
    }

    public void clear(final int x, final int y, final int width, final int height) {
        this.clearColor(x, y, width, height);
        this.clearDepthAndStencil(x, y, width, height);
    }

    public void clearColor() {
        this.clearColor(this.clearColor);
    }

    public void clearColor(final int x, final int y, final int width, final int height) {
        this.clearColor(x, y, width, height, this.clearColor);
    }

    public void clearColor(final Color color) {
        this.clearColor(color, true, true, true, true);
    }

    public void clearColor(final int x, final int y, final int width, final int height, final Color color) {
        this.clearColor(x, y, width, height, color, true, true, true, true);
    }

    public void clearColor(final Color color, final boolean red, final boolean green, final boolean blue, final boolean alpha) {
        if (this.getColorAttachment(0) != null) {
            ThinGL.glStateStack().push();
            ThinGL.glStateStack().disable(GL11C.GL_SCISSOR_TEST);
            ThinGL.glStateStack().pushColorMask();
            ThinGL.glStateManager().setColorMask(red, green, blue, alpha);
            GL45C.glClearNamedFramebufferfv(this.getGlId(), GL11C.GL_COLOR, 0, color.toRGBAF());
            ThinGL.glStateStack().popColorMask();
            ThinGL.glStateStack().pop();
        }
    }

    public void clearColor(final int x, final int y, final int width, final int height, final Color color, final boolean red, final boolean green, final boolean blue, final boolean alpha) {
        if (this.getColorAttachment(0) != null) {
            ThinGL.glStateStack().push();
            ThinGL.glStateStack().enable(GL11C.GL_SCISSOR_TEST);
            ThinGL.glStateStack().pushScissor();
            ThinGL.glStateManager().setScissor(x, y, width, height);
            ThinGL.glStateStack().pushColorMask();
            ThinGL.glStateManager().setColorMask(red, green, blue, alpha);
            GL45C.glClearNamedFramebufferfv(this.getGlId(), GL11C.GL_COLOR, 0, color.toRGBAF());
            ThinGL.glStateStack().popColorMask();
            ThinGL.glStateStack().popScissor();
            ThinGL.glStateStack().pop();
        }
    }

    public void clearDepth() {
        this.clearDepth(this.clearDepth);
    }

    public void clearDepth(final int x, final int y, final int width, final int height) {
        this.clearDepth(x, y, width, height, this.clearDepth);
    }

    public void clearDepth(final float depth) {
        if (this.getDepthAttachment() != null) {
            ThinGL.glStateStack().push();
            ThinGL.glStateStack().disable(GL11C.GL_SCISSOR_TEST);
            ThinGL.glStateStack().pushDepthMask();
            ThinGL.glStateManager().setDepthMask(true);
            GL45C.glClearNamedFramebufferfv(this.getGlId(), GL11C.GL_DEPTH, 0, new float[]{depth});
            ThinGL.glStateStack().popDepthMask();
            ThinGL.glStateStack().pop();
        }
    }

    public void clearDepth(final int x, final int y, final int width, final int height, final float depth) {
        if (this.getDepthAttachment() != null) {
            ThinGL.glStateStack().push();
            ThinGL.glStateStack().enable(GL11C.GL_SCISSOR_TEST);
            ThinGL.glStateStack().pushScissor();
            ThinGL.glStateManager().setScissor(x, y, width, height);
            ThinGL.glStateStack().pushDepthMask();
            ThinGL.glStateManager().setDepthMask(true);
            GL45C.glClearNamedFramebufferfv(this.getGlId(), GL11C.GL_DEPTH, 0, new float[]{depth});
            ThinGL.glStateStack().popDepthMask();
            ThinGL.glStateStack().popScissor();
            ThinGL.glStateStack().pop();
        }
    }

    public void clearStencil() {
        this.clearStencil(this.clearStencil);
    }

    public void clearStencil(final int x, final int y, final int width, final int height) {
        this.clearStencil(x, y, width, height, this.clearStencil);
    }

    public void clearStencil(final int stencil) {
        this.clearStencil(stencil, 0xFFFFFFFF);
    }

    public void clearStencil(final int x, final int y, final int width, final int height, final int stencil) {
        this.clearStencil(x, y, width, height, stencil, 0xFFFFFFFF);
    }

    public void clearStencil(final int stencil, final int mask) {
        if (this.getStencilAttachment() != null) {
            ThinGL.glStateStack().push();
            ThinGL.glStateStack().disable(GL11C.GL_SCISSOR_TEST);
            ThinGL.glStateStack().pushStencilMask();
            ThinGL.glStateManager().setStencilMask(mask);
            GL45C.glClearNamedFramebufferiv(this.getGlId(), GL11C.GL_STENCIL, 0, new int[]{stencil});
            ThinGL.glStateStack().popStencilMask();
            ThinGL.glStateStack().pop();
        }
    }

    public void clearStencil(final int x, final int y, final int width, final int height, final int stencil, final int mask) {
        if (this.getStencilAttachment() != null) {
            ThinGL.glStateStack().push();
            ThinGL.glStateStack().enable(GL11C.GL_SCISSOR_TEST);
            ThinGL.glStateStack().pushScissor();
            ThinGL.glStateManager().setScissor(x, y, width, height);
            ThinGL.glStateStack().pushStencilMask();
            ThinGL.glStateManager().setStencilMask(mask);
            GL45C.glClearNamedFramebufferiv(this.getGlId(), GL11C.GL_STENCIL, 0, new int[]{stencil});
            ThinGL.glStateStack().popStencilMask();
            ThinGL.glStateStack().popScissor();
            ThinGL.glStateStack().pop();
        }
    }

    public void clearDepthAndStencil() {
        this.clearDepthAndStencil(this.clearDepth, this.clearStencil);
    }

    public void clearDepthAndStencil(final int x, final int y, final int width, final int height) {
        this.clearDepthAndStencil(x, y, width, height, this.clearDepth, this.clearStencil);
    }

    public void clearDepthAndStencil(final float depth, final int stencil) {
        if (this.getDepthAttachment() != null && this.getStencilAttachment() != null) {
            ThinGL.glStateStack().push();
            ThinGL.glStateStack().disable(GL11C.GL_SCISSOR_TEST);
            ThinGL.glStateStack().pushDepthMask();
            ThinGL.glStateManager().setDepthMask(true);
            ThinGL.glStateStack().pushStencilMask();
            ThinGL.glStateManager().setStencilMask(0xFFFFFFFF);
            GL45C.glClearNamedFramebufferfi(this.getGlId(), GL30C.GL_DEPTH_STENCIL, 0, depth, stencil);
            ThinGL.glStateStack().popStencilMask();
            ThinGL.glStateStack().popDepthMask();
            ThinGL.glStateStack().pop();
        } else if (this.getDepthAttachment() != null) {
            this.clearDepth(depth);
        } else if (this.getStencilAttachment() != null) {
            this.clearStencil(stencil);
        }
    }

    public void clearDepthAndStencil(final int x, final int y, final int width, final int height, final float depth, final int stencil) {
        if (this.getDepthAttachment() != null && this.getStencilAttachment() != null) {
            ThinGL.glStateStack().push();
            ThinGL.glStateStack().enable(GL11C.GL_SCISSOR_TEST);
            ThinGL.glStateStack().pushScissor();
            ThinGL.glStateManager().setScissor(x, y, width, height);
            ThinGL.glStateStack().pushDepthMask();
            ThinGL.glStateManager().setDepthMask(true);
            ThinGL.glStateStack().pushStencilMask();
            ThinGL.glStateManager().setStencilMask(0xFFFFFFFF);
            GL45C.glClearNamedFramebufferfi(this.getGlId(), GL30C.GL_DEPTH_STENCIL, 0, depth, stencil);
            ThinGL.glStateStack().popStencilMask();
            ThinGL.glStateStack().popDepthMask();
            ThinGL.glStateStack().popScissor();
            ThinGL.glStateStack().pop();
        } else if (this.getDepthAttachment() != null) {
            this.clearDepth(x, y, width, height, depth);
        } else if (this.getStencilAttachment() != null) {
            this.clearStencil(x, y, width, height, stencil);
        }
    }

    public void blitTo(final Framebuffer target, final boolean color, final boolean depth, final boolean stencil) {
        int mask = 0;
        if (color) {
            mask |= GL11C.GL_COLOR_BUFFER_BIT;
        }
        if (depth) {
            mask |= GL11C.GL_DEPTH_BUFFER_BIT;
        }
        if (stencil) {
            mask |= GL11C.GL_STENCIL_BUFFER_BIT;
        }
        GL45C.glBlitNamedFramebuffer(this.getGlId(), target.getGlId(), 0, 0, this.getWidth(), this.getHeight(), 0, 0, target.getWidth(), target.getHeight(), mask, GL11C.GL_NEAREST);
    }

    @Override
    protected void free0() {
        GL30C.glDeleteFramebuffers(this.getGlId());
    }

    @Override
    protected void freeContainingObjects() {
        for (int attachmentId : this.getValidAttachmentPoints()) {
            final ImageStorage attachment = this.getAttachment(attachmentId);
            if (attachment != null) {
                attachment.free();
            }
        }
    }

    @Override
    public final int getGlType() {
        return GL30C.GL_FRAMEBUFFER;
    }

    public void setClearColor(final Color color) {
        this.clearColor = color;
    }

    public void setClearDepth(final float depth) {
        this.clearDepth = depth;
    }

    public void setClearStencil(final int stencil) {
        this.clearStencil = stencil;
    }

    public ImageStorage getColorAttachment(final int index) {
        return this.getAttachment(GL30C.GL_COLOR_ATTACHMENT0 + index);
    }

    public ImageStorage getDepthAttachment() {
        return this.getAttachment(GL30C.GL_DEPTH_ATTACHMENT);
    }

    public ImageStorage getStencilAttachment() {
        return this.getAttachment(GL30C.GL_STENCIL_ATTACHMENT);
    }

    public ImageStorage getAttachment(final int attachmentPoint) {
        if (!this.attachments.containsKey(attachmentPoint)) {
            final int attachmentType = GL45C.glGetNamedFramebufferAttachmentParameteri(this.getGlId(), attachmentPoint, GL30C.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
            if (attachmentType == GL11C.GL_NONE) {
                this.attachments.put(attachmentPoint, null);
                return null;
            }

            final int attachmentGlId = GL45C.glGetNamedFramebufferAttachmentParameteri(this.getGlId(), attachmentPoint, GL30C.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);
            final ImageStorage attachment = switch (attachmentType) {
                case GL11C.GL_TEXTURE -> (ImageTexture) Texture.fromGlId(attachmentGlId);
                case GL30C.GL_RENDERBUFFER -> RenderBuffer.fromGlId(attachmentGlId);
                default -> throw new IllegalArgumentException("Unsupported framebuffer attachment type: " + attachmentType);
            };
            this.attachments.put(attachmentPoint, attachment);
            return attachment;
        }
        return this.attachments.get(attachmentPoint);
    }

    public void setAttachment(final int attachmentPoint, final ImageStorage attachment) {
        if (attachmentPoint == GL30C.GL_DEPTH_STENCIL_ATTACHMENT) {
            this.attachments.put(GL30C.GL_DEPTH_ATTACHMENT, attachment);
            this.attachments.put(GL30C.GL_STENCIL_ATTACHMENT, attachment);
        } else {
            this.attachments.put(attachmentPoint, attachment);
        }

        if (attachment instanceof ImageTexture) {
            GL45C.glNamedFramebufferTexture(this.getGlId(), attachmentPoint, attachment.getGlId(), 0);
        } else if (attachment instanceof RenderBuffer) {
            GL45C.glNamedFramebufferRenderbuffer(this.getGlId(), attachmentPoint, attachment.getTarget(), attachment.getGlId());
        } else {
            throw new IllegalArgumentException("Unsupported framebuffer attachment class: " + attachment.getClass().getName());
        }
    }

    public int getWidth() {
        return this.getAnyNonNullAttachment().getWidth();
    }

    public int getHeight() {
        return this.getAnyNonNullAttachment().getHeight();
    }

    private ImageStorage getAnyNonNullAttachment() {
        final ImageStorage colorAttachment0 = this.getColorAttachment(0);
        if (colorAttachment0 != null) {
            return colorAttachment0;
        }
        for (int attachmentPoint : this.getValidAttachmentPoints()) {
            final ImageStorage attachment = this.getAttachment(attachmentPoint);
            if (attachment != null) {
                return attachment;
            }
        }
        throw new IllegalStateException("No attachments");
    }

    private int[] getValidAttachmentPoints() {
        final int maxColorAttachments = ThinGL.capabilities().getMaxColorAttachments();
        final int[] attachmentPoints = new int[maxColorAttachments + 2];
        for (int i = 0; i < maxColorAttachments; i++) {
            attachmentPoints[i] = GL30C.GL_COLOR_ATTACHMENT0 + i;
        }
        attachmentPoints[maxColorAttachments] = GL30C.GL_DEPTH_ATTACHMENT;
        attachmentPoints[maxColorAttachments + 1] = GL30C.GL_STENCIL_ATTACHMENT;
        return attachmentPoints;
    }

}
