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

package net.raphimc.thingl.util.font;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FreeType;

import java.nio.ByteBuffer;

public class FreeTypeInstance {

    private static long INSTANCE = 0L;

    public static long get() {
        if (INSTANCE == 0L) {
            try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                final PointerBuffer instanceBuffer = memoryStack.mallocPointer(1);
                checkError(FreeType.FT_Init_FreeType(instanceBuffer), "Failed to initialize FreeType library");
                INSTANCE = instanceBuffer.get();

                final ByteBuffer propertyBuffer = memoryStack.malloc(Integer.BYTES);
                propertyBuffer.putInt(0, 3);
                checkError(FreeType.FT_Property_Set(INSTANCE, "sdf", "spread", propertyBuffer), "Failed to set SDF spread property");
                checkError(FreeType.FT_Property_Set(INSTANCE, "bsdf", "spread", propertyBuffer), "Failed to set BSDF spread property");
            }
        }

        return INSTANCE;
    }

    public static void free() {
        if (INSTANCE != 0L) {
            checkError(FreeType.FT_Done_FreeType(INSTANCE), "Failed to free FreeType library");
            INSTANCE = 0L;
        }
    }

    public static void checkError(final int result, final String message, final int... allowedErrors) {
        if (result != FreeType.FT_Err_Ok) {
            for (int ignoreError : allowedErrors) {
                if (result == ignoreError) {
                    return;
                }
            }

            final String errorString = FreeType.FT_Error_String(result);
            throw new RuntimeException("FreeType error: " + message + " (" + (errorString != null ? errorString : result) + ")");
        }
    }

}
