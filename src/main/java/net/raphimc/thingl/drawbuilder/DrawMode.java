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
package net.raphimc.thingl.drawbuilder;

import org.lwjgl.opengl.GL11C;

public enum DrawMode {

    POINTS(GL11C.GL_POINTS, "Points", false, false),
    LINES(GL11C.GL_LINES, "Lines", false, false),
    LINE_STRIP(GL11C.GL_LINE_STRIP, "Line Strip", true, false),
    LINE_LOOP(GL11C.GL_LINE_LOOP, "Line Loop", true, false),
    TRIANGLES(GL11C.GL_TRIANGLES, "Triangles", false, false),
    TRIANGLE_STRIP(GL11C.GL_TRIANGLE_STRIP, "Triangle Strip", true, false),
    TRIANGLE_FAN(GL11C.GL_TRIANGLE_FAN, "Triangle Fan", true, false),
    QUADS(GL11C.GL_TRIANGLES, "Quads", false, true),
    INDEXED_LINES(GL11C.GL_LINES, "Indexed Lines", false, true),
    INDEXED_TRIANGLES(GL11C.GL_TRIANGLES, "Indexed Triangles", false, true),
    ;

    public static DrawMode fromGlMode(final int glMode) {
        for (DrawMode type : values()) {
            if (type.glMode == glMode) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown draw mode: " + glMode);
    }

    private final int glMode;
    private final String displayName;
    private final boolean connectedPrimitives;
    private final boolean indexed;

    DrawMode(final int glMode, final String displayName, final boolean connectedPrimitives, final boolean indexed) {
        this.glMode = glMode;
        this.displayName = displayName;
        this.connectedPrimitives = connectedPrimitives;
        this.indexed = indexed;
    }

    public int getGlMode() {
        return this.glMode;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean usesConnectedPrimitives() {
        return this.connectedPrimitives;
    }

    public boolean isIndexed() {
        return this.indexed;
    }

}
