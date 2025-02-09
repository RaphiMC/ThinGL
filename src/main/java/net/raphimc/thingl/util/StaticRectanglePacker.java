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
package net.raphimc.thingl.util;

import org.lwjgl.stb.STBRPContext;
import org.lwjgl.stb.STBRPNode;
import org.lwjgl.stb.STBRPRect;
import org.lwjgl.stb.STBRectPack;
import org.lwjgl.system.MemoryStack;

public class StaticRectanglePacker {

    private final STBRPContext rectPackContext;
    private final STBRPNode.Buffer rectPackNodes;

    public StaticRectanglePacker(final int width, final int height) {
        this.rectPackContext = STBRPContext.create();
        this.rectPackNodes = STBRPNode.malloc(width);
        STBRectPack.stbrp_init_target(this.rectPackContext, width, height, this.rectPackNodes);
    }

    public Slot pack(final int width, final int height) {
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
            return new Slot(x, y, width, height, u1, v1, u2, v2);
        }
    }

    public int getWidth() {
        return this.rectPackContext.width();
    }

    public int getHeight() {
        return this.rectPackContext.height();
    }

    public void delete() {
        this.rectPackNodes.free();
    }

    public record Slot(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
    }

}
