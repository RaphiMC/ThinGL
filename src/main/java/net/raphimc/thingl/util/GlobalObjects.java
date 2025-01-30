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

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.ImmediateMultiDrawBatchDataHolder;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class GlobalObjects {

    public static final Matrix4f IDENTITY_MATRIX = new Matrix4f();
    public static final ImmediateMultiDrawBatchDataHolder GLOBAL_BATCH = new ImmediateMultiDrawBatchDataHolder();
    public static final Matrix4f MATRIX4F = new Matrix4f();
    public static final Vector3f VECTOR3F = new Vector3f();
    public static final Vector2f VECTOR2F = new Vector2f();

    static {
        ThinGL.registerEndFrameCallback(() -> {
            if ((IDENTITY_MATRIX.properties() & Matrix4fc.PROPERTY_IDENTITY) == 0) {
                IDENTITY_MATRIX.identity();
                ThinGL.LOGGER.warn("IDENTITY_MATRIX was modified");
            }
            if (GLOBAL_BATCH.hasDrawBatches()) {
                GLOBAL_BATCH.delete();
                ThinGL.LOGGER.warn("GLOBAL_BATCH was not empty after rendering one frame!");
            }
        });
    }

}
