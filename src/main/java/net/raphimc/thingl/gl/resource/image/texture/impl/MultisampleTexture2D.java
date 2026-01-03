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
package net.raphimc.thingl.gl.resource.image.texture.impl;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.image.MultisampleImageStorage2D;
import net.raphimc.thingl.gl.resource.image.texture.MultisampleTexture;
import org.lwjgl.opengl.GL32C;

public class MultisampleTexture2D extends MultisampleTexture implements MultisampleImageStorage2D {

    public MultisampleTexture2D(final int internalFormat, final int width, final int height, final int samples) {
        super(GL32C.GL_TEXTURE_2D_MULTISAMPLE);
        ThinGL.glBackend().textureStorage2DMultisample(this.getGlId(), samples, internalFormat, width, height, true);
    }

    protected MultisampleTexture2D(final int glId) {
        super(glId, null);
    }

    public static MultisampleTexture2D fromGlIdUnsafe(final int glId) {
        return new MultisampleTexture2D(glId);
    }

    public void clear(final int x, final int y, final int width, final int height, final Color color) {
        this.clear(0, x, y, 0, width, height, 1, color);
    }

}
