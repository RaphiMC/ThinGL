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
package net.raphimc.thingl.implementation.application;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.framebuffer.impl.WindowFramebuffer;
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class StandaloneApplicationInterface extends ApplicationInterface {

    protected final Matrix4fStack projectionMatrixStack = new Matrix4fStack(16);
    protected final Matrix4fStack viewMatrixStack = new Matrix4fStack(16);
    private Framebuffer framebuffer = WindowFramebuffer.INSTANCE;

    public StandaloneApplicationInterface() {
        ThinGL.windowInterface().addFramebufferResizeCallback(this::loadProjectionMatrix);
        this.loadProjectionMatrix(ThinGL.windowInterface().getFramebufferWidth(), ThinGL.windowInterface().getFramebufferHeight());
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrixStack;
    }

    @Override
    public void pushProjectionMatrix(final Matrix4f projectionMatrix) {
        this.projectionMatrixStack.pushMatrix().set(projectionMatrix);
    }

    @Override
    public void popProjectionMatrix() {
        this.projectionMatrixStack.popMatrix();
    }

    @Override
    public Matrix4f getViewMatrix() {
        return this.viewMatrixStack;
    }

    @Override
    public void pushViewMatrix(final Matrix4f viewMatrix) {
        this.viewMatrixStack.pushMatrix().set(viewMatrix);
    }

    @Override
    public void popViewMatrix() {
        this.viewMatrixStack.popMatrix();
    }

    @Override
    public Framebuffer getCurrentFramebuffer() {
        return this.framebuffer;
    }

    @Override
    public void setCurrentFramebuffer(final Framebuffer framebuffer) {
        this.framebuffer = framebuffer;
    }

    protected void loadProjectionMatrix(final float width, final float height) {
        this.projectionMatrixStack.setOrtho(0F, width, height, 0F, -1000F, 1000F);
    }

}
