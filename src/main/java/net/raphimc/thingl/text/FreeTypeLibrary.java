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
package net.raphimc.thingl.text;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.text.renderer.SDFTextRenderer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FreeType;

import java.nio.ByteBuffer;

public class FreeTypeLibrary {

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

    private final long pointer;

    public FreeTypeLibrary() {
        ThinGL.get().getCapabilities().ensureFreeTypePresent();
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            final PointerBuffer instanceBuffer = memoryStack.mallocPointer(1);
            checkError(FreeType.FT_Init_FreeType(instanceBuffer), "Failed to initialize FreeType library");
            this.pointer = instanceBuffer.get();

            final ByteBuffer propertyBuffer = memoryStack.malloc(Integer.BYTES);
            propertyBuffer.putInt(0, SDFTextRenderer.DF_PX_RANGE);
            checkError(FreeType.FT_Property_Set(this.pointer, "sdf", "spread", propertyBuffer), "Failed to set SDF spread property");
            checkError(FreeType.FT_Property_Set(this.pointer, "bsdf", "spread", propertyBuffer), "Failed to set BSDF spread property");
        }
    }

    public long getPointer() {
        return this.pointer;
    }

    public void free() {
        checkError(FreeType.FT_Done_FreeType(this.pointer), "Failed to free FreeType library");
    }

}
