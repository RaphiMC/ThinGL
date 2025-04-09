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

package net.raphimc.thingl.drawbuilder.index;

import org.lwjgl.opengl.GL11C;

public enum IndexType {

    UNSIGNED_BYTE(GL11C.GL_UNSIGNED_BYTE, "Unsigned Byte", Byte.BYTES),
    UNSIGNED_SHORT(GL11C.GL_UNSIGNED_SHORT, "Unsigned Short", Short.BYTES),
    UNSIGNED_INT(GL11C.GL_UNSIGNED_INT, "Unsigned Int", Integer.BYTES),
    ;

    public static IndexType fromGlType(final int glType) {
        for (IndexType type : values()) {
            if (type.glType == glType) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown index type: " + glType);
    }

    private final int glType;
    private final String displayName;
    private final int size;

    IndexType(final int glType, final String displayName, final int size) {
        this.glType = glType;
        this.displayName = displayName;
        this.size = size;
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

}
