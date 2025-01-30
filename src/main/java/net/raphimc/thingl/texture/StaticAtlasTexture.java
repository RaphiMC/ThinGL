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
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL44C;
import org.lwjgl.stb.STBRPContext;
import org.lwjgl.stb.STBRPNode;
import org.lwjgl.stb.STBRPRect;
import org.lwjgl.stb.STBRectPack;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public class StaticAtlasTexture extends Texture2D {

    private final STBRPContext rectPackContext;
    private final STBRPNode.Buffer rectPackNodes;

    public StaticAtlasTexture(final InternalFormat internalFormat, final int width, final int height) {
        super(internalFormat, width, height);
        this.setWrap(GL13C.GL_CLAMP_TO_BORDER);
        this.setFilter(GL11C.GL_LINEAR);
        GL44C.glClearTexImage(this.getGlId(), 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, (ByteBuffer) null);

        this.rectPackContext = STBRPContext.create();
        this.rectPackNodes = STBRPNode.malloc(width);
        STBRectPack.stbrp_init_target(this.rectPackContext, width, height, this.rectPackNodes);
    }

    public Slot addSlot(final int width, final int height, final PixelFormat pixelFormat, final ByteBuffer pixels) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            final STBRPRect.Buffer rect = STBRPRect.malloc(1, memoryStack).w(width + 1).h(height + 1);
            STBRectPack.stbrp_pack_rects(this.rectPackContext, rect);
            if (!rect.was_packed()) {
                return null;
            }

            final int x = rect.x();
            final int y = rect.y();
            final float u1 = x / (float) this.getWidth();
            final float v1 = y / (float) this.getHeight();
            final float u2 = (x + width) / (float) this.getWidth();
            final float v2 = (y + height) / (float) this.getHeight();

            this.uploadPixels(x, y, width, height, pixelFormat, pixels);
            return new Slot(x, y, width, height, u1, v1, u2, v2);
        }
    }

    @Override
    protected void delete0() {
        super.delete0();
        this.rectPackNodes.free();
    }

    public record Slot(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
    }

}
