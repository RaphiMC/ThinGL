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
package net.raphimc.thingl.implementation;

import net.raphimc.thingl.ThinGL;
import org.lwjgl.opengl.GL11C;

public class Workarounds {

    private final boolean isIntelGpu;

    public Workarounds() {
        final String gpuVendor = ThinGL.glBackend().getString(GL11C.GL_VENDOR);
        this.isIntelGpu = gpuVendor != null && gpuVendor.equalsIgnoreCase("Intel");
    }

    /**
     * The Intel OpenGL driver has a bug, where calling glVertexArrayElementBuffer(vaoId, 0); generates an error.<br>
     * Confirmed broken on Intel UHD Graphics 630 (Driver version: 31.0.101.2134)
     *
     * @return Whether ThinGL should use the non DSA method to unbind the element buffer.
     */
    public boolean isDsaVertexArrayElementBufferUnbindBroken() {
        return this.isIntelGpu;
    }

    /**
     * The Intel OpenGL driver doesn't support glGetTextureParameteri(texId, GL_TEXTURE_TARGET); even tho it's required by the OpenGL 4.5 specification.<br>
     * Confirmed broken on Intel UHD Graphics 630 (Driver version: 31.0.101.2134)
     *
     * @return Whether ThinGL should use an alternative way to determine the texture type.
     */
    public boolean isGetTextureParameterTextureTargetBroken() {
        return this.isIntelGpu;
    }

}
