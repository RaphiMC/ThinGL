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
package net.raphimc.thingl.drawbuilder.vertex;

public record VertexDataLayoutElement(DataType dataType, int count, TargetDataType targetDataType, int padding) {

    public VertexDataLayoutElement {
        switch (dataType.getTargetDataType()) {
            case INT -> {
                if (targetDataType != TargetDataType.INT && targetDataType != TargetDataType.FLOAT && targetDataType != TargetDataType.FLOAT_NORMALIZED) {
                    throw new IllegalArgumentException("Invalid target data type for INT data type: " + targetDataType);
                }
            }
            case FLOAT -> {
                if (targetDataType != TargetDataType.FLOAT) {
                    throw new IllegalArgumentException("Invalid target data type for FLOAT data type: " + targetDataType);
                }
            }
            case DOUBLE -> {
                if (targetDataType != TargetDataType.DOUBLE && targetDataType != TargetDataType.FLOAT) {
                    throw new IllegalArgumentException("Invalid target data type for DOUBLE data type: " + targetDataType);
                }
            }
        }
    }

    public VertexDataLayoutElement(final DataType type, final int count) {
        this(type, count, type.getTargetDataType());
    }

    public VertexDataLayoutElement(final DataType type, final int count, final TargetDataType targetDataType) {
        this(type, count, targetDataType, 0);
    }

}
