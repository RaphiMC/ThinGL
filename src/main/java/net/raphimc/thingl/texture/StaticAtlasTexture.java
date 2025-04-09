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

import net.raphimc.thingl.resource.texture.Texture2D;
import net.raphimc.thingl.util.rectpack.Slot;
import net.raphimc.thingl.util.rectpack.StaticRectanglePacker;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL44C;
import org.lwjgl.opengl.GL45C;

import java.nio.ByteBuffer;

public class StaticAtlasTexture extends Texture2D {

    private final StaticRectanglePacker rectanglePacker;

    public StaticAtlasTexture(final InternalFormat internalFormat, final int width, final int height) {
        super(internalFormat, width, height);
        this.setWrap(GL13C.GL_CLAMP_TO_BORDER);
        GL45C.glTextureParameterfv(this.getGlId(), GL11C.GL_TEXTURE_BORDER_COLOR, new float[4]);
        GL44C.glClearTexImage(this.getGlId(), 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        this.rectanglePacker = new StaticRectanglePacker(width, height);
    }

    public Slot addSlot(final int width, final int height, final PixelFormat pixelFormat, final ByteBuffer pixels) {
        final Slot slot = this.rectanglePacker.pack(width, height);
        if (slot == null) {
            return null;
        }

        this.uploadPixels(slot.x(), slot.y(), width, height, pixelFormat, pixels);
        return slot;
    }

    @Override
    protected void free0() {
        super.free0();
        this.rectanglePacker.free();
    }

}
