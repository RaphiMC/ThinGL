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

import net.raphimc.thingl.resource.image.impl.ByteImage2D;
import net.raphimc.thingl.resource.memory.Memory;

public interface ByteImage2DWriter {

    default byte[] writeByteImage2DToBytes(final ByteImage2D image) {
        final Memory memory = this.writeByteImage2DToMemory(image);
        final byte[] imageBytes = memory.getBytes(0, memory.getSizeAsInt());
        memory.free();
        return imageBytes;
    }

    Memory writeByteImage2DToMemory(final ByteImage2D image);

}
