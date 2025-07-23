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

import net.raphimc.thingl.resource.image.MultisampleImageStorage;
import org.lwjgl.opengl.GL32C;

public abstract class MultisampleTexture extends ImageTexture implements MultisampleImageStorage {

    public MultisampleTexture(final int target) {
        super(target);
    }

    protected MultisampleTexture(final int glId, final Object unused) {
        super(glId, unused);
    }

    @Override
    public int getSamples() {
        return this.getLevelParameterInt(0, GL32C.GL_TEXTURE_SAMPLES);
    }

}
