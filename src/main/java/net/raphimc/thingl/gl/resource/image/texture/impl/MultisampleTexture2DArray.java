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
package net.raphimc.thingl.gl.resource.image.texture.impl;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.image.MultisampleImageStorage3D;
import net.raphimc.thingl.gl.resource.image.texture.MultisampleTexture;
import org.lwjgl.opengl.GL32C;

public class MultisampleTexture2DArray extends MultisampleTexture implements MultisampleImageStorage3D {

    public MultisampleTexture2DArray(final int internalFormat, final int width, final int height, final int layers, final int samples) {
        super(GL32C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY);
        ThinGL.glBackend().textureStorage3DMultisample(this.getGlId(), samples, internalFormat, width, height, layers, true);
    }

    protected MultisampleTexture2DArray(final int glId) {
        super(glId, null);
    }

    public static MultisampleTexture2DArray fromGlIdUnsafe(final int glId) {
        return new MultisampleTexture2DArray(glId);
    }

    public void clear(final int x, final int y, final int z, final int width, final int height, final int depth, final Color color) {
        this.clear(0, x, y, z, width, height, depth, color);
    }

}
