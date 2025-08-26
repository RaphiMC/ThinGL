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

import java.util.function.Function;

public class Std430ShaderDataHolder extends StdShaderDataHolder {

    public static final Function<BufferBuilder, Std430ShaderDataHolder> SUPPLIER = Std430ShaderDataHolder::new;

    public Std430ShaderDataHolder(final BufferBuilder bufferBuilder) {
        super(bufferBuilder);
    }

    @Override
    protected int getStructAlignment(final int maxMemberAlignment) {
        return maxMemberAlignment;
    }

}
