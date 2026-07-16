/*
 * This file is part of ThinGL - https://github.com/RaphiMC/ThinGL
 * Copyright (C) 2024-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.thingl.implementation.util.glfw;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.util.DetailedMessageBuilder;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.MemoryUtil;

import java.util.Map;

public class GLFWErrorCallbackImpl extends GLFWErrorCallback {

    private static final Map<Integer, String> ERRORS = APIUtil.apiClassTokens((_, value) -> 0x10000 < value && value < 0x20000, null, GLFW.class);

    @Override
    public void invoke(final int error, final long description) {
        final DetailedMessageBuilder messageBuilder = new DetailedMessageBuilder("GLFW error message");
        messageBuilder.appendField("Error", ERRORS.getOrDefault(error, "Unknown (" + error + ")"));
        messageBuilder.appendField("Description", MemoryUtil.memUTF8(description));
        ThinGL.LOGGER.error(messageBuilder.build(), new RuntimeException());
    }

}
