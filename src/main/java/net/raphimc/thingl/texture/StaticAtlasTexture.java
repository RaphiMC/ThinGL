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
package net.raphimc.thingl.texture;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.resource.image.texture.Texture2D;
import net.raphimc.thingl.util.rectpack.Slot;
import net.raphimc.thingl.util.rectpack.StaticRectanglePacker;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;

import java.nio.ByteBuffer;

public class StaticAtlasTexture extends Texture2D {

    private final StaticRectanglePacker rectanglePacker;

    public StaticAtlasTexture(final int internalFormat, final int width, final int height) {
        super(internalFormat, width, height);
        this.setWrap(GL13C.GL_CLAMP_TO_BORDER);
        this.setParameterFloatArray(GL11C.GL_TEXTURE_BORDER_COLOR, new float[4]);
        this.clear(Color.TRANSPARENT);
        this.rectanglePacker = new StaticRectanglePacker(width, height);
    }

    public Slot addSlot(final int width, final int height, final int pixelFormat, final ByteBuffer pixelBuffer) {
        final Slot slot = this.rectanglePacker.pack(width, height);
        if (slot == null) {
            return null;
        }

        this.uploadPixels(slot.x(), slot.y(), width, height, pixelFormat, pixelBuffer);
        return slot;
    }

    @Override
    protected void free0() {
        super.free0();
        this.rectanglePacker.free();
    }

}
