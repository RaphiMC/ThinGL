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

package net.raphimc.thingl.drawbuilder.vertex;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

public enum DataType {

    BYTE(GL11C.GL_BYTE, "Byte", 1, true),
    UNSIGNED_BYTE(GL11C.GL_UNSIGNED_BYTE, "Unsigned Byte", 1, true),
    SHORT(GL11C.GL_SHORT, "Short", 2, true),
    UNSIGNED_SHORT(GL11C.GL_UNSIGNED_SHORT, "Unsigned Short", 2, true),
    INT(GL11C.GL_INT, "Int", 4, true),
    UNSIGNED_INT(GL11C.GL_UNSIGNED_INT, "Unsigned Int", 4, true),
    FLOAT(GL11C.GL_FLOAT, "Float", 4, false),
    HALF_FLOAT(GL30C.GL_HALF_FLOAT, "Half Float", 2, false),
    ;

    public static DataType fromGlType(final int glType) {
        for (DataType type : values()) {
            if (type.glType == glType) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown data type: " + glType);
    }

    private final int glType;
    private final String displayName;
    private final int size;
    private final boolean isInteger;

    DataType(final int glType, final String displayName, final int size, final boolean isInteger) {
        this.glType = glType;
        this.displayName = displayName;
        this.size = size;
        this.isInteger = isInteger;
    }

    public int getGlType() {
        return this.glType;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getSize() {
        return this.size;
    }

    public boolean isInteger() {
        return this.isInteger;
    }

}
