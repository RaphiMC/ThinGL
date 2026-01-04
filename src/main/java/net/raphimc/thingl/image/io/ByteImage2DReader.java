/*
 * This file is part of ThinGL - https://github.com/RaphiMC/ThinGL
 * Copyright (C) 2024-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.thingl.image.io;

import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.resource.image.impl.ByteImage2D;
import net.raphimc.thingl.resource.memory.Memory;

import java.nio.ByteBuffer;

public interface ByteImage2DReader {

    default ByteImage2D readByteImage2D(final ByteBuffer imageBuffer) {
        return this.readByteImage2D(imageBuffer, true);
    }

    default ByteImage2D readByteImage2D(final ByteBuffer imageBuffer, final boolean forceColor) {
        return this.readByteImage2D(imageBuffer, forceColor, true);
    }

    default ByteImage2D readByteImage2D(final ByteBuffer imageBuffer, final boolean forceColor, final boolean freeImageBuffer) {
        if (imageBuffer.isDirect()) {
            return this.readByteImage2D(MemoryAllocator.wrapMemory(imageBuffer), forceColor, freeImageBuffer);
        } else {
            final byte[] imageBytes = new byte[imageBuffer.remaining()];
            imageBuffer.get(imageBytes);
            return this.readByteImage2D(imageBytes, forceColor);
        }
    }

    default ByteImage2D readByteImage2D(final byte[] imageBytes) {
        return this.readByteImage2D(imageBytes, true);
    }

    default ByteImage2D readByteImage2D(final byte[] imageBytes, final boolean forceColor) {
        return this.readByteImage2D(MemoryAllocator.allocateMemory(imageBytes), forceColor);
    }

    default ByteImage2D readByteImage2D(final Memory imageData) {
        return this.readByteImage2D(imageData, true);
    }

    default ByteImage2D readByteImage2D(final Memory imageData, final boolean forceColor) {
        return this.readByteImage2D(imageData, forceColor, true);
    }

    ByteImage2D readByteImage2D(final Memory imageData, final boolean forceColor, final boolean freeImageData);

}
