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

    public void pushOverwrite(final float xtl, final float ytl, final float xbr, final float ybr) {
        this.pushOverwrite(RenderMathUtil.getIdentityMatrix(), xtl, ytl, xbr, ybr);
    }

    public void pushOverwrite(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr) {
        if (this.stack.isEmpty()) {
            ThinGL.glStateStack().push();
            ThinGL.glStateStack().enable(GL11C.GL_SCISSOR_TEST);
        }

        final Rectanglei rectangle = this.stack.push(RenderMathUtil.getWindowRectangle(positionMatrix, xtl, ytl, xbr, ybr));
        ThinGL.glStateManager().setScissor(rectangle);
    }

    public void pushIntersection(final float xtl, final float ytl, final float xbr, final float ybr) {
        this.pushIntersection(RenderMathUtil.getIdentityMatrix(), xtl, ytl, xbr, ybr);
    }

    public void pushIntersection(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr) {
        if (this.stack.isEmpty()) {
            this.pushOverwrite(positionMatrix, xtl, ytl, xbr, ybr);
            return;
        }

        final Rectanglei rectangle = RenderMathUtil.getWindowRectangle(positionMatrix, xtl, ytl, xbr, ybr);
        final Rectanglei intersection = this.stack.push(this.stack.peek().intersection(rectangle, rectangle));
        ThinGL.glStateManager().setScissor(intersection);
    }

    public void pop() {
        this.stack.pop();
        if (this.stack.isEmpty()) {
            ThinGL.glStateStack().pop();
        } else {
            ThinGL.glStateManager().setScissor(this.stack.peek());
        }
    }

    public boolean intersectsRectangle(final float xtl, final float ytl, final float xbr, final float ybr) {
        return this.intersectsRectangle(RenderMathUtil.getIdentityMatrix(), xtl, ytl, xbr, ybr);
    }

    public boolean intersectsRectangle(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr) {
        if (this.stack.isEmpty()) return true;

        final Rectanglei rectangle = RenderMathUtil.getWindowRectangle(positionMatrix, xtl, ytl, xbr, ybr);
        return this.stack.peek().intersectsRectangle(rectangle);
    }

    public boolean containsRectangle(final float xtl, final float ytl, final float xbr, final float ybr) {
        return this.containsRectangle(RenderMathUtil.getIdentityMatrix(), xtl, ytl, xbr, ybr);
    }

    public boolean containsRectangle(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr) {
        if (this.stack.isEmpty()) return true;

        final Rectanglei rectangle = RenderMathUtil.getWindowRectangle(positionMatrix, xtl, ytl, xbr, ybr);
        return this.stack.peek().containsRectangle(rectangle);
    }

}
