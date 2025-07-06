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

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.framebuffer.impl.WindowFramebuffer;
import net.raphimc.thingl.resource.GLContainerObject;
import net.raphimc.thingl.resource.renderbuffer.AbstractRenderBuffer;
import net.raphimc.thingl.resource.texture.AbstractTexture;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL45C;

import java.util.Objects;

public class Framebuffer extends GLContainerObject {

    private final float[] clearColor = new float[]{0F, 0F, 0F, 0F};
    private final float[] clearDepth = new float[]{1F};
    private final int[] clearStencil = new int[]{0};

    private final FramebufferAttachment[] colorAttachments = new FramebufferAttachment[this.getMaxSupportedColorAttachmentCount()];
    private FramebufferAttachment depthAttachment;
    private FramebufferAttachment stencilAttachment;

    public Framebuffer(final FramebufferAttachment colorAttachment) {
        this(colorAttachment, null, null);
    }

    public Framebuffer(final FramebufferAttachment colorAttachment, final FramebufferAttachment depthStencilAttachment) {
        this(colorAttachment, depthStencilAttachment, depthStencilAttachment);
    }

    public Framebuffer(final FramebufferAttachment colorAttachment, final FramebufferAttachment depthAttachment, final FramebufferAttachment stencilAttachment) {
        super(GL45C.glCreateFramebuffers());
        if (colorAttachment == null && depthAttachment == null && stencilAttachment == null) { // Attachments can be added later
            return;
        }
        try {
            if (colorAttachment != null) {
                this.setAttachment(GL30C.GL_COLOR_ATTACHMENT0, colorAttachment);
            }
            if (depthAttachment != null) {
                this.setAttachment(GL30C.GL_DEPTH_ATTACHMENT, depthAttachment);
            }
            if (stencilAttachment != null) {
                this.setAttachment(GL30C.GL_STENCIL_ATTACHMENT, stencilAttachment);
            }
            this.checkFramebufferStatus();
            this.clear();
        } catch (Throwable e) {
            this.free();
            throw e;
        }
    }

    protected Framebuffer(final int glId) {
        super(glId);
        this.refreshCachedData();
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

    @Override
    public void refreshCachedData() {
        for (int i = 0; i < this.colorAttachments.length; i++) {
            this.colorAttachments[i] = FramebufferAttachment.fromGlId(this.getGlId(), GL30C.GL_COLOR_ATTACHMENT0 + i);
        }
        this.depthAttachment = FramebufferAttachment.fromGlId(this.getGlId(), GL30C.GL_DEPTH_ATTACHMENT);
        this.stencilAttachment = FramebufferAttachment.fromGlId(this.getGlId(), GL30C.GL_STENCIL_ATTACHMENT);
    }

    public void checkFramebufferStatus() {
        final int status = GL45C.glCheckNamedFramebufferStatus(this.getGlId(), GL30C.GL_FRAMEBUFFER);
        if (status != GL30C.GL_FRAMEBUFFER_COMPLETE) {
            switch (status) {
                case GL30C.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                    throw new IllegalStateException("glCheckFramebufferStatus: GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
                case GL30C.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                    throw new IllegalStateException("glCheckFramebufferStatus: GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
                case GL30C.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                    throw new IllegalStateException("glCheckFramebufferStatus: GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
                case GL30C.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                    throw new IllegalStateException("glCheckFramebufferStatus: GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
                case GL30C.GL_FRAMEBUFFER_UNSUPPORTED:
                    throw new IllegalStateException("glCheckFramebufferStatus: GL_FRAMEBUFFER_UNSUPPORTED");
                case GL30C.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
                    throw new IllegalStateException("glCheckFramebufferStatus: GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
                case GL32C.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS:
                    throw new IllegalStateException("glCheckFramebufferStatus: GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
                case GL11C.GL_OUT_OF_MEMORY:
                    throw new IllegalStateException("glCheckFramebufferStatus: GL_OUT_OF_MEMORY");
                default:
                    throw new IllegalStateException("glCheckFramebufferStatus: " + status);
            }
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

    public void clearColor() {
        GL45C.glClearNamedFramebufferfv(this.getGlId(), GL11C.GL_COLOR, 0, this.clearColor);
    }

    public void clearDepth() {
        if (this.depthAttachment != null) {
            GL45C.glClearNamedFramebufferfv(this.getGlId(), GL11C.GL_DEPTH, 0, this.clearDepth);
        }
    }

    public void clearStencil() {
        if (this.stencilAttachment != null) {
            GL45C.glClearNamedFramebufferiv(this.getGlId(), GL11C.GL_STENCIL, 0, this.clearStencil);
        }
    }

    public void clearDepthAndStencil() {
        if (this.depthAttachment != null || this.stencilAttachment != null) {
            GL45C.glClearNamedFramebufferfi(this.getGlId(), GL30C.GL_DEPTH_STENCIL, 0, this.clearDepth[0], this.clearStencil[0]);
        }
    }

    public void blitTo(final Framebuffer target, final boolean copyColor, final boolean copyDepth, final boolean copyStencil) {
        int mask = 0;
        if (copyColor) {
            mask |= GL11C.GL_COLOR_BUFFER_BIT;
        }
        if (copyDepth) {
            mask |= GL11C.GL_DEPTH_BUFFER_BIT;
        }
        if (copyStencil) {
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
        for (int i = 0; i < this.colorAttachments.length; i++) {
            if (this.colorAttachments[i] != null) {
                this.colorAttachments[i].free();
                this.colorAttachments[i] = null;
            }
        }
        if (this.depthAttachment != null) {
            this.depthAttachment.free();
            this.depthAttachment = null;
        }
        if (this.stencilAttachment != null) {
            this.stencilAttachment.free();
            this.stencilAttachment = null;
        }
    }

    @Override
    public final int getGlType() {
        return GL30C.GL_FRAMEBUFFER;
    }

    public void setClearColor(final float r, final float g, final float b, final float a) {
        this.clearColor[0] = r;
        this.clearColor[1] = g;
        this.clearColor[2] = b;
        this.clearColor[3] = a;
    }

    public void setClearDepth(final float depth) {
        this.clearDepth[0] = depth;
    }

    public void setClearStencil(final int stencil) {
        this.clearStencil[0] = stencil;
    }

    public FramebufferAttachment getColorAttachment(final int index) {
        if (index >= 0 && index < this.colorAttachments.length) {
            return this.colorAttachments[index];
        } else {
            return null;
        }
    }

    public FramebufferAttachment getDepthAttachment() {
        return this.depthAttachment;
    }

    public FramebufferAttachment getStencilAttachment() {
        return this.stencilAttachment;
    }

    public FramebufferAttachment getAttachment(final int type) {
        if (type >= GL30C.GL_COLOR_ATTACHMENT0 && type <= GL30C.GL_COLOR_ATTACHMENT31) {
            if (type - GL30C.GL_COLOR_ATTACHMENT0 < this.colorAttachments.length) {
                return this.colorAttachments[type - GL30C.GL_COLOR_ATTACHMENT0];
            } else {
                return null;
            }
        } else {
            return switch (type) {
                case GL30C.GL_DEPTH_ATTACHMENT -> this.depthAttachment;
                case GL30C.GL_STENCIL_ATTACHMENT -> this.stencilAttachment;
                case GL30C.GL_DEPTH_STENCIL_ATTACHMENT -> Objects.equals(this.depthAttachment, this.stencilAttachment) ? this.depthAttachment : null;
                default -> throw new IllegalArgumentException("Invalid attachment type: " + type);
            };
        }
    }

    public void setAttachment(final int type, final FramebufferAttachment attachment) {
        if (type >= GL30C.GL_COLOR_ATTACHMENT0 && type <= GL30C.GL_COLOR_ATTACHMENT31) {
            if (type - GL30C.GL_COLOR_ATTACHMENT0 < this.colorAttachments.length) {
                this.colorAttachments[type - GL30C.GL_COLOR_ATTACHMENT0] = attachment;
            } else {
                throw new IllegalArgumentException("Invalid color attachment index: " + (type - GL30C.GL_COLOR_ATTACHMENT0));
            }
        } else {
            switch (type) {
                case GL30C.GL_DEPTH_ATTACHMENT -> this.depthAttachment = attachment;
                case GL30C.GL_STENCIL_ATTACHMENT -> this.stencilAttachment = attachment;
                case GL30C.GL_DEPTH_STENCIL_ATTACHMENT -> {
                    this.depthAttachment = attachment;
                    this.stencilAttachment = attachment;
                }
                default -> throw new IllegalArgumentException("Invalid attachment type: " + type);
            }
        }

        if (attachment instanceof AbstractTexture) {
            GL45C.glNamedFramebufferTexture(this.getGlId(), type, attachment.getGlId(), 0);
        } else if (attachment instanceof AbstractRenderBuffer) {
            GL45C.glNamedFramebufferRenderbuffer(this.getGlId(), type, GL30C.GL_RENDERBUFFER, attachment.getGlId());
        } else {
            throw new IllegalArgumentException("Invalid attachment class: " + attachment.getClass().getName());
        }
    }

    public int getWidth() {
        return this.getAnyNonNullAttachment().getWidth();
    }

    public int getHeight() {
        return this.getAnyNonNullAttachment().getHeight();
    }

    private FramebufferAttachment getAnyNonNullAttachment() {
        if (this.colorAttachments[0] != null) {
            return this.colorAttachments[0];
        }
        if (this.depthAttachment != null) {
            return this.depthAttachment;
        }
        if (this.stencilAttachment != null) {
            return this.stencilAttachment;
        }
        for (FramebufferAttachment colorAttachment : this.colorAttachments) {
            if (colorAttachment != null) {
                return colorAttachment;
            }
        }
        throw new IllegalStateException("No attachments");
    }

    protected int getMaxSupportedColorAttachmentCount() {
        return ThinGL.capabilities().getMaxColorAttachments();
    }

}
