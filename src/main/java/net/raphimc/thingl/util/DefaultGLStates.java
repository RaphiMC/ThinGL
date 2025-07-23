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
import net.raphimc.thingl.wrapper.Blending;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;

public class DefaultGLStates {

    public static void push() {
        ThinGL.glStateStack().push();
        ThinGL.glStateStack().enable(GL11C.GL_BLEND);
        ThinGL.glStateStack().enable(GL11C.GL_DEPTH_TEST);
        ThinGL.glStateStack().enable(GL11C.GL_CULL_FACE);
        ThinGL.glStateStack().disable(GL11C.GL_SCISSOR_TEST);
        ThinGL.glStateStack().disable(GL11C.GL_COLOR_LOGIC_OP);
        ThinGL.glStateStack().disable(GL11C.GL_POLYGON_OFFSET_FILL);
        ThinGL.glStateStack().pushCullFace();
        ThinGL.glStateManager().setCullFace(GL11C.GL_BACK);
        ThinGL.glStateStack().pushFrontFace();
        ThinGL.glStateManager().setFrontFace(GL11C.GL_CCW);
        ThinGL.glStateStack().pushBlendEquation();
        ThinGL.glStateManager().setBlendEquation(GL14C.GL_FUNC_ADD);
        ThinGL.glStateStack().pushBlendFunc();
        Blending.alphaBlending();
        ThinGL.glStateStack().pushDepthFunc();
        ThinGL.glStateManager().setDepthFunc(GL11C.GL_LEQUAL);
        ThinGL.glStateStack().pushColorMask();
        ThinGL.glStateManager().setColorMask(true, true, true, true);
        ThinGL.glStateStack().pushDepthMask();
        ThinGL.glStateManager().setDepthMask(true);
        ThinGL.glStateStack().pushStencilMask();
        ThinGL.glStateManager().setStencilMask(0xFFFFFFFF);
    }

    public static void pop() {
        ThinGL.glStateStack().popStencilMask();
        ThinGL.glStateStack().popDepthMask();
        ThinGL.glStateStack().popColorMask();
        ThinGL.glStateStack().popDepthFunc();
        ThinGL.glStateStack().popBlendFunc();
        ThinGL.glStateStack().popBlendEquation();
        ThinGL.glStateStack().popFrontFace();
        ThinGL.glStateStack().popCullFace();
        ThinGL.glStateStack().pop();
    }

}
