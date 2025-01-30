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
import org.lwjgl.opengl.GL11C;

import java.util.Stack;

public class Stencil {

    private static final Stack<Mode> STENCIL_STACK = new Stack<>();

    static {
        ThinGL.registerEndFrameCallback(() -> {
            if (!STENCIL_STACK.isEmpty()) {
                while (!STENCIL_STACK.isEmpty()) pop();
                ThinGL.LOGGER.warn("Stencil STENCIL_STACK was not empty after rendering one frame!");
            }
        });
    }

    public static void push(final Mode mode) {
        if (STENCIL_STACK.isEmpty()) {
            clear();
            GLStateTracker.push();
            GLStateTracker.enable(GL11C.GL_STENCIL_TEST);
        }
        GL11C.glColorMask(false, false, false, false);
        STENCIL_STACK.push(mode).begin();
    }

    public static void set() {
        GL11C.glColorMask(true, true, true, true);
        STENCIL_STACK.peek().end();
    }

    public static void pop() {
        STENCIL_STACK.pop();
        if (STENCIL_STACK.isEmpty()) {
            GLStateTracker.pop();
            clear();
        } else {
            set();
        }
    }

    private static void clear() {
        GL11C.glClearStencil(0);
        GL11C.glClear(GL11C.GL_STENCIL_BUFFER_BIT);
    }

    public enum Mode {
        EQUAL_INTERSECTION {
            @Override
            protected void begin() {
                GL11C.glStencilFunc(GL11C.GL_EQUAL, STENCIL_STACK.size() - 1, 0xFF);
                GL11C.glStencilOp(GL11C.GL_KEEP, GL11C.GL_KEEP, GL11C.GL_INCR);
            }

            @Override
            protected void end() {
                GL11C.glStencilFunc(GL11C.GL_LEQUAL, STENCIL_STACK.size(), 0xFF);
                GL11C.glStencilOp(GL11C.GL_KEEP, GL11C.GL_KEEP, GL11C.GL_KEEP);
            }
        },
        NOT_EQUAL {
            @Override
            protected void begin() {
                GL11C.glStencilFunc(GL11C.GL_ALWAYS, 1, 0xFF);
                GL11C.glStencilOp(GL11C.GL_REPLACE, GL11C.GL_REPLACE, GL11C.GL_REPLACE);
            }

            @Override
            protected void end() {
                GL11C.glStencilFunc(GL11C.GL_EQUAL, 0, 0xFF);
                GL11C.glStencilOp(GL11C.GL_KEEP, GL11C.GL_KEEP, GL11C.GL_KEEP);
            }
        },
        OVERLAP {
            @Override
            protected void begin() {
                GL11C.glStencilFunc(GL11C.GL_NEVER, 0, 0xFF);
                GL11C.glStencilOp(GL11C.GL_INCR, GL11C.GL_INCR, GL11C.GL_INCR);
            }

            @Override
            protected void end() {
                GL11C.glStencilFunc(GL11C.GL_LEQUAL, STENCIL_STACK.size() + 1, 0xFF);
                GL11C.glStencilOp(GL11C.GL_KEEP, GL11C.GL_KEEP, GL11C.GL_KEEP);
            }
        },
        ;

        protected abstract void begin();

        protected abstract void end();
    }

}
