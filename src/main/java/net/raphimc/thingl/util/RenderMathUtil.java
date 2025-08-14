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

import net.lenni0451.commons.math.MathUtils;
import net.raphimc.thingl.ThinGL;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.primitives.Rectanglei;

public class RenderMathUtil {

    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();

    public static int getMaxMipMapLevels(final int width, final int height) {
        return getMaxMipMapLevels(Math.max(width, height));
    }

    public static int getMaxMipMapLevels(final int width, final int height, final int depth) {
        return getMaxMipMapLevels(Math.max(Math.max(width, height), depth));
    }

    public static int getMaxMipMapLevels(final int maxDim) {
        return (int) (Math.floor(Math.log(maxDim) / Math.log(2))) + 1;
    }

    public static Matrix4f getIdentityMatrix() {
        if ((IDENTITY_MATRIX.properties() & Matrix4fc.PROPERTY_IDENTITY) == 0) {
            IDENTITY_MATRIX.identity();
            ThinGL.LOGGER.warn("IDENTITY_MATRIX was modified");
        }
        return IDENTITY_MATRIX;
    }

    public static Matrix4f getMvpMatrix(final Matrix4f positionMatrix) {
        return new Matrix4f().mul(ThinGL.globalUniforms().getProjectionMatrix()).mul(ThinGL.globalUniforms().getViewMatrix()).mul(positionMatrix);
    }

    public static Rectanglei getWindowRectangle(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr) {
        return getWindowRectangle(positionMatrix, xtl, ytl, xbr, ybr, false);
    }

    public static Rectanglei getWindowRectangle(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr, final boolean flipY) {
        final int[] viewport = ThinGL.glStateManager().getViewport().toArray();
        final Matrix4f mvpMatrix = RenderMathUtil.getMvpMatrix(positionMatrix);
        final Vector3f topLeft = new Vector3f(xtl, ytl, 0F);
        final Vector3f bottomRight = new Vector3f(xbr, ybr, 0F);

        mvpMatrix.project(topLeft, viewport, topLeft);
        mvpMatrix.project(bottomRight, viewport, bottomRight);

        if (flipY) {
            return new Rectanglei(MathUtils.floorInt(topLeft.x), MathUtils.floorInt(viewport[3] - topLeft.y), MathUtils.ceilInt(bottomRight.x), MathUtils.ceilInt(viewport[3] - bottomRight.y));
        } else {
            return new Rectanglei(MathUtils.floorInt(topLeft.x), MathUtils.floorInt(bottomRight.y), MathUtils.ceilInt(bottomRight.x), MathUtils.ceilInt(topLeft.y));
        }
    }

    public static Vector3f projectToWindowCoordinates(final Matrix4f positionMatrix, final Vector3f position) {
        final int[] viewport = ThinGL.glStateManager().getViewport().toArray();
        final Matrix4f mvpMatrix = RenderMathUtil.getMvpMatrix(positionMatrix);
        return mvpMatrix.project(position, viewport, new Vector3f());
    }

    public static Vector3f unprojectFromWindowCoordinates(final Matrix4f positionMatrix, final Vector3f windowPosition) {
        final int[] viewport = ThinGL.glStateManager().getViewport().toArray();
        final Matrix4f mvpMatrix = RenderMathUtil.getMvpMatrix(positionMatrix);
        return mvpMatrix.unproject(windowPosition, viewport, new Vector3f());
    }

}
