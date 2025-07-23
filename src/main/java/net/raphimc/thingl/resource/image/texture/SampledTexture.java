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

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL45C;

public abstract class SampledTexture extends ImageTexture {

    public SampledTexture(final int target) {
        super(target);
        this.setFilter(GL11C.GL_LINEAR);
        this.setWrap(GL12C.GL_CLAMP_TO_EDGE);
    }

    protected SampledTexture(final int glId, final Object unused) {
        super(glId, unused);
    }

    public void generateMipmaps() {
        GL45C.glGenerateTextureMipmap(this.getGlId());
    }

    public void setFilter(final int filter) {
        this.setMinificationFilter(filter);
        this.setMagnificationFilter(filter);
    }

    public int getMinificationFilter() {
        return this.getParameterInt(GL11C.GL_TEXTURE_MIN_FILTER);
    }

    public void setMinificationFilter(final int minificationFilter) {
        this.setParameterInt(GL11C.GL_TEXTURE_MIN_FILTER, minificationFilter);
    }

    public int getMagnificationFilter() {
        return this.getParameterInt(GL11C.GL_TEXTURE_MAG_FILTER);
    }

    public void setMagnificationFilter(final int magnificationFilter) {
        this.setParameterInt(GL11C.GL_TEXTURE_MAG_FILTER, magnificationFilter);
    }

    public void setWrap(final int wrap) {
        this.setWrapS(wrap);
        this.setWrapT(wrap);
        this.setWrapR(wrap);
    }

    public int getWrapS() {
        return this.getParameterInt(GL11C.GL_TEXTURE_WRAP_S);
    }

    public void setWrapS(final int wrapS) {
        this.setParameterInt(GL11C.GL_TEXTURE_WRAP_S, wrapS);
    }

    public int getWrapT() {
        return this.getParameterInt(GL11C.GL_TEXTURE_WRAP_T);
    }

    public void setWrapT(final int wrapT) {
        this.setParameterInt(GL11C.GL_TEXTURE_WRAP_T, wrapT);
    }

    public int getWrapR() {
        return this.getParameterInt(GL12C.GL_TEXTURE_WRAP_R);
    }

    public void setWrapR(final int wrapR) {
        this.setParameterInt(GL12C.GL_TEXTURE_WRAP_R, wrapR);
    }

}
