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
package net.raphimc.thingl.resource.image.texture;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.resource.image.MultisampleImageStorage2D;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL45C;

public class MultisampleTexture2D extends MultisampleTexture implements MultisampleImageStorage2D {

    public MultisampleTexture2D(final int internalFormat, final int width, final int height, final int samples) {
        super(GL32C.GL_TEXTURE_2D_MULTISAMPLE);
        GL45C.glTextureStorage2DMultisample(this.getGlId(), samples, internalFormat, width, height, true);
    }

    protected MultisampleTexture2D(final int glId) {
        super(glId, null);
    }

    public void clear(final int x, final int y, final int width, final int height, final Color color) {
        this.clear(0, x, y, 0, width, height, 1, color);
    }

}
