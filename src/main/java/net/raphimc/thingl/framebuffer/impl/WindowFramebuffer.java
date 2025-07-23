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
package net.raphimc.thingl.framebuffer.impl;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import net.raphimc.thingl.resource.image.ImageStorage;

public class WindowFramebuffer extends Framebuffer {

    public static final WindowFramebuffer INSTANCE = new WindowFramebuffer();

    private WindowFramebuffer() {
        super(0);
    }

    @Override
    public void checkStatus() {
    }

    @Override
    public void unbind() {
    }

    @Override
    public void free() {
    }

    @Override
    protected void freeContainingObjects() {
    }

    @Override
    public boolean isAllocated() {
        return true;
    }

    @Override
    public void setAttachment(final int attachmentPoint, final ImageStorage attachment) {
        throw new UnsupportedOperationException("Cannot set attachments on the window framebuffer");
    }

    @Override
    public int getWidth() {
        return ThinGL.windowInterface().getFramebufferWidth();
    }

    @Override
    public int getHeight() {
        return ThinGL.windowInterface().getFramebufferHeight();
    }

}
