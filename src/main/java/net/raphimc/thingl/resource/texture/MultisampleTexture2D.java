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

package net.raphimc.thingl.resource.texture;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL45C;

public class MultisampleTexture2D extends AbstractTexture {

    private final int width;
    private final int height;
    private final int samples;

    public MultisampleTexture2D(final InternalFormat internalFormat, final int width, final int height, final int samples) {
        super(Type.TEX_2D_MULTISAMPLE, internalFormat);
        this.width = width;
        this.height = height;
        this.samples = samples;
        GL45C.glTextureStorage2DMultisample(this.getGlId(), samples, internalFormat.getGlFormat(), width, height, true);
    }

    protected MultisampleTexture2D(final int glId) {
        super(glId, Type.TEX_2D_MULTISAMPLE);
        this.width = GL45C.glGetTextureLevelParameteri(glId, 0, GL11C.GL_TEXTURE_WIDTH);
        this.height = GL45C.glGetTextureLevelParameteri(glId, 0, GL11C.GL_TEXTURE_HEIGHT);
        this.samples = GL45C.glGetTextureLevelParameteri(glId, 0, GL32C.GL_TEXTURE_SAMPLES);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getSamples() {
        return this.samples;
    }

}
