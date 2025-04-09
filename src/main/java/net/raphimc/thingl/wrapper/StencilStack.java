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
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL11C;

import java.util.Stack;

public class StencilStack {

    private final Stack<Mode> stack = new Stack<>();

    @ApiStatus.Internal
    public StencilStack(final ThinGL thinGL) {
        thinGL.addEndFrameCallback(() -> {
            if (!this.stack.isEmpty()) {
                while (!this.stack.isEmpty()) this.pop();
                ThinGL.LOGGER.warn("StencilStack was not empty at the end of the frame!");
            }
        });
    }

    public void push(final Mode mode) {
        if (this.stack.isEmpty()) {
            this.clear();
            ThinGL.glStateTracker().push();
            ThinGL.glStateTracker().enable(GL11C.GL_STENCIL_TEST);
        }
        GL11C.glColorMask(false, false, false, false);
        this.stack.push(mode).begin(this.stack.size());
    }

    public void set() {
        GL11C.glColorMask(true, true, true, true);
        this.stack.peek().end(this.stack.size());
    }

    public void pop() {
        this.stack.pop();
        if (this.stack.isEmpty()) {
            ThinGL.glStateTracker().pop();
            this.clear();
        } else {
            this.set();
        }
    }

    private void clear() {
        GL11C.glClearStencil(0);
        GL11C.glClear(GL11C.GL_STENCIL_BUFFER_BIT);
    }

    public enum Mode {
        EQUAL_INTERSECTION {
            @Override
            protected void begin(final int stackSize) {
                GL11C.glStencilFunc(GL11C.GL_EQUAL, stackSize - 1, 0xFF);
                GL11C.glStencilOp(GL11C.GL_KEEP, GL11C.GL_KEEP, GL11C.GL_INCR);
            }

            @Override
            protected void end(final int stackSize) {
                GL11C.glStencilFunc(GL11C.GL_LEQUAL, stackSize, 0xFF);
                GL11C.glStencilOp(GL11C.GL_KEEP, GL11C.GL_KEEP, GL11C.GL_KEEP);
            }
        },
        NOT_EQUAL {
            @Override
            protected void begin(final int stackSize) {
                GL11C.glStencilFunc(GL11C.GL_ALWAYS, 1, 0xFF);
                GL11C.glStencilOp(GL11C.GL_REPLACE, GL11C.GL_REPLACE, GL11C.GL_REPLACE);
            }

            @Override
            protected void end(final int stackSize) {
                GL11C.glStencilFunc(GL11C.GL_EQUAL, 0, 0xFF);
                GL11C.glStencilOp(GL11C.GL_KEEP, GL11C.GL_KEEP, GL11C.GL_KEEP);
            }
        },
        OVERLAP {
            @Override
            protected void begin(final int stackSize) {
                GL11C.glStencilFunc(GL11C.GL_NEVER, 0, 0xFF);
                GL11C.glStencilOp(GL11C.GL_INCR, GL11C.GL_INCR, GL11C.GL_INCR);
            }

            @Override
            protected void end(final int stackSize) {
                GL11C.glStencilFunc(GL11C.GL_LEQUAL, stackSize + 1, 0xFF);
                GL11C.glStencilOp(GL11C.GL_KEEP, GL11C.GL_KEEP, GL11C.GL_KEEP);
            }
        },
        ;

        protected abstract void begin(final int stackSize);

        protected abstract void end(final int stackSize);
    }

}
