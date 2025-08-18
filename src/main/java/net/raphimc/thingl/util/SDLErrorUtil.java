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

import org.lwjgl.sdl.SDLError;

public class SDLErrorUtil {

    public static void checkError(final long result) {
        SDLErrorUtil.checkError(result, "SDL Error");
    }

    public static void checkError(final long result, final String message) {
        SDLErrorUtil.checkError(result != 0, message);
    }

    public static void checkError(final boolean result) {
        SDLErrorUtil.checkError(result, "SDL Error");
    }

    public static void checkError(final boolean result, final String message) {
        if (!result) {
            throw new IllegalStateException(message + ": " + SDLError.SDL_GetError());
        }
    }

}
