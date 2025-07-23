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

public class ScissorStack {

    private final Stack<Rectanglei> stack = new Stack<>();

    public ScissorStack() {
        ThinGL.get().addFinishFrameCallback(() -> {
            if (!this.stack.isEmpty()) {
                while (!this.stack.isEmpty()) this.pop();
                ThinGL.LOGGER.warn("ScissorStack was not empty at the end of the frame!");
            }
        });
    }

    public void pushOverwrite(final float x1, final float y1, final float x2, final float y2) {
        this.pushOverwrite(null, x1, y1, x2, y2);
    }

    public void pushOverwrite(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2) {
        if (this.stack.isEmpty()) {
            ThinGL.glStateStack().push();
            ThinGL.glStateStack().enable(GL11C.GL_SCISSOR_TEST);
        }

        final Rectanglei rectangle = this.stack.push(RenderMathUtil.getScreenRect(positionMatrix, x1, y1, x2, y2));
        ThinGL.glStateManager().setScissor(rectangle.minX, rectangle.minY, rectangle.lengthX(), rectangle.lengthY());
    }

    public void pushIntersection(final float x1, final float y1, final float x2, final float y2) {
        this.pushIntersection(null, x1, y1, x2, y2);
    }

    public void pushIntersection(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2) {
        if (this.stack.isEmpty()) {
            pushOverwrite(positionMatrix, x1, y1, x2, y2);
            return;
        }

        final Rectanglei rectangle = this.stack.push(this.stack.peek().intersection(RenderMathUtil.getScreenRect(positionMatrix, x1, y1, x2, y2)));
        ThinGL.glStateManager().setScissor(rectangle.minX, rectangle.minY, rectangle.lengthX(), rectangle.lengthY());
    }

    public void pop() {
        this.stack.pop();
        if (this.stack.isEmpty()) {
            ThinGL.glStateStack().pop();
        } else {
            final Rectanglei rectangle = this.stack.peek();
            ThinGL.glStateManager().setScissor(rectangle.minX, rectangle.minY, rectangle.lengthX(), rectangle.lengthY());
        }
    }

    public boolean isAnyPointInside(final float x1, final float y1, final float x2, final float y2) {
        return this.isAnyPointInside(null, x1, y1, x2, y2);
    }

    public boolean isAnyPointInside(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2) {
        if (this.stack.isEmpty()) return true;

        final Rectanglei rectangle = RenderMathUtil.getScreenRect(positionMatrix, x1, y1, x2, y2);
        final Rectanglei intersection = this.stack.peek().intersection(rectangle, rectangle);
        return intersection.lengthX() > 0 && intersection.lengthY() > 0;
    }

    public boolean isFullyInside(final float x1, final float y1, final float x2, final float y2) {
        return this.isFullyInside(null, x1, y1, x2, y2);
    }

    public boolean isFullyInside(final Matrix4f positionMatrix, final float x1, final float y1, final float x2, final float y2) {
        if (this.stack.isEmpty()) return true;

        final Rectanglei rectangle = RenderMathUtil.getScreenRect(positionMatrix, x1, y1, x2, y2);
        final Rectanglei intersection = this.stack.peek().intersection(rectangle, rectangle);
        return rectangle.lengthX() == intersection.lengthX() && rectangle.lengthY() == intersection.lengthY();
    }

}
