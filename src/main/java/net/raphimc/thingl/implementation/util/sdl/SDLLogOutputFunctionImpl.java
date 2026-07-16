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
package net.raphimc.thingl.implementation.util.sdl;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.util.DetailedMessageBuilder;
import org.lwjgl.sdl.SDLLog;
import org.lwjgl.sdl.SDL_LogOutputFunction;
import org.lwjgl.system.MemoryUtil;

import java.util.Map;

public class SDLLogOutputFunctionImpl extends SDL_LogOutputFunction {

    private static final Map<Integer, String> CATEGORY = Map.of(
            SDLLog.SDL_LOG_CATEGORY_APPLICATION, "Application",
            SDLLog.SDL_LOG_CATEGORY_ERROR, "Error",
            SDLLog.SDL_LOG_CATEGORY_ASSERT, "Assert",
            SDLLog.SDL_LOG_CATEGORY_SYSTEM, "System",
            SDLLog.SDL_LOG_CATEGORY_AUDIO, "Audio",
            SDLLog.SDL_LOG_CATEGORY_VIDEO, "Video",
            SDLLog.SDL_LOG_CATEGORY_RENDER, "Render",
            SDLLog.SDL_LOG_CATEGORY_INPUT, "Input",
            SDLLog.SDL_LOG_CATEGORY_TEST, "Test",
            SDLLog.SDL_LOG_CATEGORY_GPU, "GPU"
    );

    private static final Map<Integer, String> PRIORITY = Map.of(
            SDLLog.SDL_LOG_PRIORITY_INVALID, "Invalid",
            SDLLog.SDL_LOG_PRIORITY_TRACE, "Trace",
            SDLLog.SDL_LOG_PRIORITY_VERBOSE, "Verbose",
            SDLLog.SDL_LOG_PRIORITY_DEBUG, "Debug",
            SDLLog.SDL_LOG_PRIORITY_INFO, "Info",
            SDLLog.SDL_LOG_PRIORITY_WARN, "Warn",
            SDLLog.SDL_LOG_PRIORITY_ERROR, "Error",
            SDLLog.SDL_LOG_PRIORITY_CRITICAL, "Critical"
    );

    @Override
    public void invoke(final long userdata, final int category, final int priority, final long message) {
        final DetailedMessageBuilder messageBuilder = new DetailedMessageBuilder("SDL log message");
        messageBuilder.appendField("Category", CATEGORY.getOrDefault(category, "Unknown (" + category + ")"));
        messageBuilder.appendField("Priority", PRIORITY.getOrDefault(priority, "Unknown (" + priority + ")"));
        messageBuilder.appendField("Message", MemoryUtil.memUTF8(message));
        if ((priority & SDLLog.SDL_LOG_PRIORITY_ERROR) != 0 || (priority & SDLLog.SDL_LOG_PRIORITY_CRITICAL) != 0) {
            ThinGL.LOGGER.error(messageBuilder.build(), new RuntimeException());
        } else if ((priority & SDLLog.SDL_LOG_PRIORITY_WARN) != 0) {
            ThinGL.LOGGER.warn(messageBuilder.build(), new RuntimeException());
        } else {
            ThinGL.LOGGER.info(messageBuilder.build(), new RuntimeException());
        }
    }

}
