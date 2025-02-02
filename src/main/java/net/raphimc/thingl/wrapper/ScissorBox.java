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

package net.raphimc.thingl.wrapper;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.util.RenderMathUtil;
import org.joml.Matrix4f;
import org.joml.primitives.Rectanglei;
import org.lwjgl.opengl.GL11C;

import java.util.Stack;

public class ScissorBox {

    private static final Stack<Rectanglei> SCISSOR_STACK = new Stack<>();

    static {
        ThinGL.registerEndFrameCallback(() -> {
            if (!SCISSOR_STACK.isEmpty()) {
                while (!SCISSOR_STACK.isEmpty()) pop();
                ThinGL.LOGGER.warn("ScissorBox SCISSOR_STACK was not empty after rendering one frame!");
            }
        });
    }

    public static void pushOverwrite(final float x1, final float y1, final float x2, final float y2) {
        pushOverwrite(null, x1, y1, x2, y2);
    }

    public static void pushOverwrite(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2) {
        if (SCISSOR_STACK.isEmpty()) {
            GLStateTracker.push();
            GLStateTracker.enable(GL11C.GL_SCISSOR_TEST);
        }

        final Rectanglei rectangle = SCISSOR_STACK.push(RenderMathUtil.getScreenRect(positionMatrix, x1, y1, x2, y2));
        GL11C.glScissor(rectangle.minX, rectangle.minY, rectangle.lengthX(), rectangle.lengthY());
    }

    public static void pushIntersection(final float x1, final float y1, final float x2, final float y2) {
        pushIntersection(null, x1, y1, x2, y2);
    }

    public static void pushIntersection(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2) {
        if (SCISSOR_STACK.isEmpty()) {
            pushOverwrite(positionMatrix, x1, y1, x2, y2);
            return;
        }

        final Rectanglei rectangle = SCISSOR_STACK.push(SCISSOR_STACK.peek().intersection(RenderMathUtil.getScreenRect(positionMatrix, x1, y1, x2, y2)));
        GL11C.glScissor(rectangle.minX, rectangle.minY, rectangle.lengthX(), rectangle.lengthY());
    }

    public static void pop() {
        SCISSOR_STACK.pop();
        if (SCISSOR_STACK.isEmpty()) {
            GLStateTracker.pop();
        } else {
            final Rectanglei rectangle = SCISSOR_STACK.peek();
            GL11C.glScissor(rectangle.minX, rectangle.minY, rectangle.lengthX(), rectangle.lengthY());
        }
    }

    public static boolean isAnyPointInside(final float x1, final float y1, final float x2, final float y2) {
        return isAnyPointInside(null, x1, y1, x2, y2);
    }

    public static boolean isAnyPointInside(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2) {
        if (SCISSOR_STACK.isEmpty()) return true;

        final Rectanglei rectangle = RenderMathUtil.getScreenRect(positionMatrix, x1, y1, x2, y2);
        final Rectanglei intersection = SCISSOR_STACK.peek().intersection(rectangle, rectangle);
        return intersection.lengthX() > 0 && intersection.lengthY() > 0;
    }

    public static boolean isFullyInside(final float x1, final float y1, final float x2, final float y2) {
        return isFullyInside(null, x1, y1, x2, y2);
    }

    public static boolean isFullyInside(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2) {
        if (SCISSOR_STACK.isEmpty()) return true;

        final Rectanglei rectangle = RenderMathUtil.getScreenRect(positionMatrix, x1, y1, x2, y2);
        final Rectanglei intersection = SCISSOR_STACK.peek().intersection(rectangle, rectangle);
        return rectangle.lengthX() == intersection.lengthX() && rectangle.lengthY() == intersection.lengthY();
    }

}
