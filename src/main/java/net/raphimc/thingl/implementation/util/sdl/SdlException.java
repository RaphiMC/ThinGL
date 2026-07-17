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

import net.raphimc.thingl.util.DetailedMessageBuilder;
import org.lwjgl.sdl.SDLError;

public class SdlException extends RuntimeException {

    public static void check(final boolean result) {
        check(result, "Failed to execute SDL operation");
    }

    public static void check(final boolean result, final String message) {
        if (!result) {
            throw new SdlException(message);
        }
    }

    public static int check(final int result, final String message) {
        if (result == 0) {
            throw new SdlException(message);
        }
        return result;
    }

    public static long check(final long result, final String message) {
        if (result == 0L) {
            throw new SdlException(message);
        }
        return result;
    }

    private SdlException(final String message) {
        final DetailedMessageBuilder messageBuilder = new DetailedMessageBuilder(message);
        messageBuilder.appendField("Error", SDLError.SDL_GetError());
        super(messageBuilder.build());
    }

}
