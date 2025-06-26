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

public class Blending {

    public static void noBlending() {
        ThinGL.glStateManager().setBlendFunc(GL11C.GL_ONE, GL11C.GL_ZERO);
    }

    public static void alphaBlending() {
        ThinGL.glStateManager().setBlendFunc(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void alphaWithAdditiveAlphaBlending() {
        ThinGL.glStateManager().setBlendFunc(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA, GL11C.GL_ONE, GL11C.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void premultipliedAlphaBlending() {
        ThinGL.glStateManager().setBlendFunc(GL11C.GL_ONE, GL11C.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void invertColorBlending() {
        ThinGL.glStateManager().setBlendFunc(GL11C.GL_ONE_MINUS_DST_COLOR, GL11C.GL_ONE_MINUS_SRC_COLOR, GL11C.GL_ONE, GL11C.GL_ZERO);
    }

}
