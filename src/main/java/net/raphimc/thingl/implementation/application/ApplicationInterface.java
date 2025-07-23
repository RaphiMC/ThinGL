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

import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;

public abstract class ApplicationInterface {

    public abstract Matrix4f getProjectionMatrix();

    public abstract void pushProjectionMatrix(final Matrix4f projectionMatrix);

    public abstract void popProjectionMatrix();

    public abstract Matrix4f getViewMatrix();

    public abstract void pushViewMatrix(final Matrix4f viewMatrix);

    public abstract void popViewMatrix();

    public abstract Framebuffer getCurrentFramebuffer();

    public abstract void setCurrentFramebuffer(final Framebuffer framebuffer);

    public Vector2f get2DScaleFactor() {
        final Vector2f scale = new Vector2f();
        final Matrix4f projectionMatrix = this.getProjectionMatrix();
        if ((projectionMatrix.properties() & Matrix4fc.PROPERTY_AFFINE) != 0) { // If orthographic projection
            final Framebuffer currentFramebuffer = this.getCurrentFramebuffer();
            // Extract scale factor from orthographic projection matrix
            scale.x = currentFramebuffer.getWidth() / (2F / projectionMatrix.m00());
            scale.y = currentFramebuffer.getHeight() / -(2F / projectionMatrix.m11());
        } else {
            scale.set(1F);
        }
        return scale;
    }

    public boolean needsPreviousProgramRestored() {
        return false;
    }

    public boolean needsPreviousVertexArrayRestored() {
        return false;
    }

}
