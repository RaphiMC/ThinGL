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

package net.raphimc.thingl.framebuffer;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import net.raphimc.thingl.resource.framebuffer.FramebufferAttachment;
import org.lwjgl.opengl.GL30C;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public abstract class ResizingFramebuffer extends Framebuffer {

    private final BiFunction<Integer, Integer, FramebufferAttachment> colorAttachmentSupplier;
    private final BiFunction<Integer, Integer, FramebufferAttachment> depthAttachmentSupplier;
    private final BiFunction<Integer, Integer, FramebufferAttachment> stencilAttachmentSupplier;
    private final BiConsumer<Integer, Integer> framebufferResizeCallback = this::resize;

    public ResizingFramebuffer(final BiFunction<Integer, Integer, FramebufferAttachment> colorAttachmentSupplier) {
        this(colorAttachmentSupplier, null, null);
    }

    public ResizingFramebuffer(final BiFunction<Integer, Integer, FramebufferAttachment> colorAttachmentSupplier, final BiFunction<Integer, Integer, FramebufferAttachment> depthStencilAttachmentSupplier) {
        this(colorAttachmentSupplier, depthStencilAttachmentSupplier, depthStencilAttachmentSupplier);
    }

    public ResizingFramebuffer(final BiFunction<Integer, Integer, FramebufferAttachment> colorAttachmentSupplier, final BiFunction<Integer, Integer, FramebufferAttachment> depthAttachmentSupplier, BiFunction<Integer, Integer, FramebufferAttachment> stencilAttachmentSupplier) {
        super(null);
        this.colorAttachmentSupplier = colorAttachmentSupplier;
        this.depthAttachmentSupplier = depthAttachmentSupplier;
        this.stencilAttachmentSupplier = stencilAttachmentSupplier;
        ThinGL.windowInterface().addFramebufferResizeCallback(this.framebufferResizeCallback);
    }

    protected void init() {
        this.resize(ThinGL.windowInterface().getFramebufferWidth(), ThinGL.windowInterface().getFramebufferHeight());
    }

    @Override
    protected void free0() {
        ThinGL.windowInterface().removeFramebufferResizeCallback(this.framebufferResizeCallback);
        super.free0();
        this.freeContainingObjects();
    }

    private void resize(final int width, final int height) {
        try {
            this.freeContainingObjects();
            this.setAttachment(GL30C.GL_COLOR_ATTACHMENT0, this.colorAttachmentSupplier.apply(width, height));
            if (this.stencilAttachmentSupplier == this.depthAttachmentSupplier && this.depthAttachmentSupplier != null) {
                this.setAttachment(GL30C.GL_DEPTH_STENCIL_ATTACHMENT, this.depthAttachmentSupplier.apply(width, height));
            } else if (this.depthAttachmentSupplier != null) {
                this.setAttachment(GL30C.GL_DEPTH_ATTACHMENT, this.depthAttachmentSupplier.apply(width, height));
            } else if (this.stencilAttachmentSupplier != null) {
                this.setAttachment(GL30C.GL_STENCIL_ATTACHMENT, this.stencilAttachmentSupplier.apply(width, height));
            }
            this.checkFramebufferStatus();
            this.clear();
        } catch (Throwable e) {
            this.freeFully();
            throw e;
        }
    }

}
