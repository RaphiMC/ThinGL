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

package net.raphimc.thingl.drawbuilder;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.vertex.DataType;
import net.raphimc.thingl.drawbuilder.vertex.VertexDataLayout;
import net.raphimc.thingl.drawbuilder.vertex.VertexDataLayoutElement;
import org.lwjgl.opengl.GL11C;

public class BuiltinDrawBatches {

    private static final Runnable PUSH_ENABLE_BLEND = () -> {
        ThinGL.glStateTracker().push();
        ThinGL.glStateTracker().enable(GL11C.GL_BLEND);
    };

    private static final Runnable POP = () -> ThinGL.glStateTracker().pop();

    private static final VertexDataLayoutElement POSITION_ELEMENT = new VertexDataLayoutElement(DataType.FLOAT, 3);
    private static final VertexDataLayoutElement HALF_POSITION_ELEMENT = new VertexDataLayoutElement(DataType.HALF_FLOAT, 3);
    private static final VertexDataLayoutElement COLOR_ELEMENT = new VertexDataLayoutElement(DataType.UNSIGNED_BYTE, 4, true);
    private static final VertexDataLayoutElement TEXTURE_ELEMENT = new VertexDataLayoutElement(DataType.FLOAT, 2);

    public static final VertexDataLayout POSITION_COLOR_LAYOUT = new VertexDataLayout(POSITION_ELEMENT, COLOR_ELEMENT);
    public static final VertexDataLayout LINE_LAYOUT = new VertexDataLayout(POSITION_ELEMENT, COLOR_ELEMENT, new VertexDataLayoutElement(DataType.FLOAT, 1));
    public static final VertexDataLayout POSITION_TEXTURE_LAYOUT = new VertexDataLayout(POSITION_ELEMENT, TEXTURE_ELEMENT);
    public static final VertexDataLayout POSITION_COLOR_TEXTURE_LAYOUT = new VertexDataLayout(POSITION_ELEMENT, COLOR_ELEMENT, TEXTURE_ELEMENT);
    public static final VertexDataLayout HALF_POSITION_COLOR_LAYOUT = new VertexDataLayout(HALF_POSITION_ELEMENT, COLOR_ELEMENT);

    public static final DrawBatch COLORED_QUAD = new DrawBatch(() -> ThinGL.programs().getPositionColor(), () -> ThinGL.programs().getInstancedPositionColor(), () -> ThinGL.programs().getMultidrawPositionColor(), DrawMode.QUADS, POSITION_COLOR_LAYOUT, HALF_POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);

    public static final DrawBatch COLORED_TRIANGLE = new DrawBatch(() -> ThinGL.programs().getPositionColor(), () -> ThinGL.programs().getInstancedPositionColor(), () -> ThinGL.programs().getMultidrawPositionColor(), DrawMode.TRIANGLES, POSITION_COLOR_LAYOUT, HALF_POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);

    public static final DrawBatch INDEXED_COLORED_TRIANGLE = new DrawBatch(() -> ThinGL.programs().getPositionColor(), () -> ThinGL.programs().getInstancedPositionColor(), () -> ThinGL.programs().getMultidrawPositionColor(), DrawMode.TRIANGLES, POSITION_COLOR_LAYOUT, HALF_POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);

    public static final DrawBatch COLORED_GL_LINE = new DrawBatch(() -> ThinGL.programs().getPositionColor(), () -> ThinGL.programs().getInstancedPositionColor(), () -> ThinGL.programs().getMultidrawPositionColor(), DrawMode.LINES, POSITION_COLOR_LAYOUT, HALF_POSITION_COLOR_LAYOUT, () -> {
        PUSH_ENABLE_BLEND.run();
        ThinGL.glStateTracker().enable(GL11C.GL_LINE_SMOOTH);
        GL11C.glHint(GL11C.GL_LINE_SMOOTH_HINT, GL11C.GL_NICEST);
    }, POP);

    public static final DrawBatch COLORED_LINE = new DrawBatch(() -> ThinGL.programs().getLine(), () -> ThinGL.programs().getMultidrawLine(), DrawMode.LINES, LINE_LAYOUT, () -> {
        PUSH_ENABLE_BLEND.run();
        ThinGL.glStateTracker().disable(GL11C.GL_CULL_FACE);
    }, POP);

    public static final DrawBatch COLORED_TRIANGLE_FAN = new DrawBatch(() -> ThinGL.programs().getPositionColor(), () -> ThinGL.programs().getInstancedPositionColor(), DrawMode.TRIANGLE_FAN, POSITION_COLOR_LAYOUT, HALF_POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);

    public static final DrawBatch COLORED_TRIANGLE_STRIP = new DrawBatch(() -> ThinGL.programs().getPositionColor(), DrawMode.TRIANGLE_STRIP, POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);

}
