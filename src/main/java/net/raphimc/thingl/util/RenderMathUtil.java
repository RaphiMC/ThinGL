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
import org.joml.Vector2f;
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

    public static Rectanglei getScreenRect(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2) {
        final Vector3f start = new Vector3f(x1, y1, 0);
        final Vector3f end = new Vector3f(x2, y2, 0);
        if (positionMatrix != null && (positionMatrix.properties() & Matrix4fc.PROPERTY_IDENTITY) == 0) {
            positionMatrix.transformPosition(start);
            positionMatrix.transformPosition(end);
        }

        final Vector2f scale = ThinGL.applicationInterface().get2DScaleFactor();
        return new Rectanglei(
                MathUtils.floorInt(start.x * scale.x),
                MathUtils.floorInt((MathUtils.ceilInt(ThinGL.applicationInterface().getCurrentFramebuffer().getHeight() / scale.y) - end.y) * scale.y),
                MathUtils.ceilInt(end.x * scale.x),
                MathUtils.ceilInt((MathUtils.ceilInt(ThinGL.applicationInterface().getCurrentFramebuffer().getHeight() / scale.y) - start.y) * scale.y)
        );
    }

}
