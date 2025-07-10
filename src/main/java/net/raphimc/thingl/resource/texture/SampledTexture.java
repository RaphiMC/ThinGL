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
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL45C;

public abstract class SampledTexture extends AbstractTexture {

    private final int mipMapLevels;
    private int minificationFilter;
    private int magnificationFilter;

    public SampledTexture(final Type type, final InternalFormat internalFormat, final int mipMapLevels) {
        super(type, internalFormat);
        this.mipMapLevels = mipMapLevels;
    }

    protected SampledTexture(final int glId, final Type type) {
        super(glId, type);
        this.mipMapLevels = GL45C.glGetTextureParameteri(glId, GL43C.GL_TEXTURE_IMMUTABLE_LEVELS);
    }

    @Override
    public void refreshCachedData() {
        this.minificationFilter = GL45C.glGetTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_MIN_FILTER);
        this.magnificationFilter = GL45C.glGetTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_MAG_FILTER);
    }

    public int getMipMapLevels() {
        return this.mipMapLevels;
    }

    public int getMinificationFilter() {
        return this.minificationFilter;
    }

    public void setMinificationFilter(final int minificationFilter) {
        this.minificationFilter = minificationFilter;
        GL45C.glTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_MIN_FILTER, minificationFilter);
    }

    public int getMagnificationFilter() {
        return this.magnificationFilter;
    }

    public void setMagnificationFilter(final int magnificationFilter) {
        this.magnificationFilter = magnificationFilter;
        GL45C.glTextureParameteri(this.getGlId(), GL11C.GL_TEXTURE_MAG_FILTER, magnificationFilter);
    }

    public void setFilter(final int filter) {
        this.setMinificationFilter(filter);
        this.setMagnificationFilter(filter);
    }

}
