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

package net.raphimc.thingl.drawbuilder.databuilder.holder;

import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import net.raphimc.thingl.drawbuilder.databuilder.writer.BufferDataWriter;

public class ShaderDataHolder extends BufferDataWriter<ShaderDataHolder> {

    private int alignment = 1;
    private int structSize = 0;

    public ShaderDataHolder(final BufferBuilder bufferBuilder) {
        super(bufferBuilder);
    }

    @Override
    public ShaderDataHolder rawInt(final int i) {
        this.alignment = Math.max(this.alignment, 4);
        this.structSize += 4;
        return super.rawInt(i);
    }

    @Override
    public ShaderDataHolder rawFloat(final float f) {
        this.alignment = Math.max(this.alignment, 4);
        this.structSize += 4;
        return super.rawFloat(f);
    }

    @Override
    public ShaderDataHolder rawDouble(final double d) {
        this.alignment = Math.max(this.alignment, 8);
        this.structSize += 8;
        return super.rawDouble(d);
    }

    public void end() {
        this.bufferBuilder.align(this.alignment);
        this.alignment = 1;
        this.structSize = 0;
    }

    public int endAndGetArrayIndex() {
        if (this.structSize == 0) {
            throw new IllegalStateException("No data was written");
        }

        final int structSize = this.structSize;
        this.end();
        return (this.bufferBuilder.getPosition() / structSize) - 1;
    }

}
