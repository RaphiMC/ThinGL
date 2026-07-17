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
package net.raphimc.thingl.text.util.freetype;

import net.raphimc.thingl.implementation.Capabilities;
import net.raphimc.thingl.util.DetailedMessageBuilder;
import org.lwjgl.util.freetype.FreeType;

public class FreeTypeException extends RuntimeException {

    static {
        Capabilities.assertFreeTypeAvailable();
    }

    public static void check(final int result, final String message) {
        if (result != FreeType.FT_Err_Ok) {
            throw new FreeTypeException(result, message);
        }
    }

    private FreeTypeException(final int errorCode, final String message) {
        final DetailedMessageBuilder messageBuilder = new DetailedMessageBuilder(message);
        messageBuilder.appendField("Error code", String.format("0x%x (%d)", errorCode, errorCode));
        messageBuilder.appendField("Error string", FreeType.FT_Error_String(errorCode));
        super(messageBuilder.build());
    }

}
