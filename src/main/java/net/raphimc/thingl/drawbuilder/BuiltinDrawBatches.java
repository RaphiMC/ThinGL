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
import net.raphimc.thingl.drawbuilder.vertex.TargetDataType;
import net.raphimc.thingl.drawbuilder.vertex.VertexDataLayout;
import net.raphimc.thingl.drawbuilder.vertex.VertexDataLayoutElement;
import org.lwjgl.opengl.GL11C;

public class BuiltinDrawBatches {

    private static final Runnable PUSH_ENABLE_BLEND = () -> {
        ThinGL.glStateStack().push();
        ThinGL.glStateStack().enable(GL11C.GL_BLEND);
    };

    private static final Runnable POP = () -> ThinGL.glStateStack().pop();

    // === Vertex Data Layout Elements ===

    public static final VertexDataLayoutElement POSITION_ELEMENT = new VertexDataLayoutElement(DataType.FLOAT, 3);
    public static final VertexDataLayoutElement COLOR_ELEMENT = new VertexDataLayoutElement(DataType.UNSIGNED_BYTE, 4, TargetDataType.FLOAT_NORMALIZED);
    public static final VertexDataLayoutElement TEXTURE_ELEMENT = new VertexDataLayoutElement(DataType.FLOAT, 2);

    // === Vertex Data Layouts ===

    public static final VertexDataLayout POSITION_LAYOUT = new VertexDataLayout(POSITION_ELEMENT);
    public static final VertexDataLayout POSITION_COLOR_LAYOUT = new VertexDataLayout(POSITION_ELEMENT, COLOR_ELEMENT);
    public static final VertexDataLayout POSITION_TEXTURE_LAYOUT = new VertexDataLayout(POSITION_ELEMENT, TEXTURE_ELEMENT);
    public static final VertexDataLayout POSITION_TEXTURE_ARRAY_LAYER_LAYOUT = new VertexDataLayout(POSITION_ELEMENT, TEXTURE_ELEMENT, new VertexDataLayoutElement(DataType.SHORT, 1));
    public static final VertexDataLayout POSITION_COLOR_TEXTURE_LAYOUT = new VertexDataLayout(POSITION_ELEMENT, COLOR_ELEMENT, TEXTURE_ELEMENT);
    public static final VertexDataLayout LINE_LAYOUT = new VertexDataLayout(POSITION_ELEMENT, COLOR_ELEMENT, new VertexDataLayoutElement(DataType.FLOAT, 1));

    // === Draw Batches ===

    public static final DrawBatch COLOR_QUAD = new DrawBatch(() -> ThinGL.programs().getColor(), DrawMode.QUADS, POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);
    public static final DrawBatch COLOR_TRIANGLE = new DrawBatch(() -> ThinGL.programs().getColor(), DrawMode.TRIANGLES, POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);
    public static final DrawBatch INDEXED_COLOR_TRIANGLE = new DrawBatch(() -> ThinGL.programs().getColor(), DrawMode.INDEXED_TRIANGLES, POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);
    public static final DrawBatch COLOR_GL_LINE = new DrawBatch(() -> ThinGL.programs().getColor(), DrawMode.LINES, POSITION_COLOR_LAYOUT, () -> {
        PUSH_ENABLE_BLEND.run();
        ThinGL.glStateStack().enable(GL11C.GL_LINE_SMOOTH);
        GL11C.glHint(GL11C.GL_LINE_SMOOTH_HINT, GL11C.GL_NICEST);
    }, POP);
    public static final DrawBatch COLOR_LINE = new DrawBatch(() -> ThinGL.programs().getLine(), DrawMode.LINES, LINE_LAYOUT, () -> {
        PUSH_ENABLE_BLEND.run();
        ThinGL.glStateStack().disable(GL11C.GL_CULL_FACE);
    }, POP);
    public static final DrawBatch COLOR_TRIANGLE_FAN = new DrawBatch(() -> ThinGL.programs().getColor(), DrawMode.TRIANGLE_FAN, POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);
    public static final DrawBatch COLOR_TRIANGLE_STRIP = new DrawBatch(() -> ThinGL.programs().getColor(), DrawMode.TRIANGLE_STRIP, POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);

    // === Instanced Draw Batches ===

    public static final DrawBatch INSTANCED_COLOR_QUAD = new DrawBatch(() -> ThinGL.programs().getInstancedColor(), DrawMode.QUADS, POSITION_COLOR_LAYOUT, POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);
    public static final DrawBatch INSTANCED_COLOR_TRIANGLE = new DrawBatch(() -> ThinGL.programs().getInstancedColor(), DrawMode.TRIANGLES, POSITION_COLOR_LAYOUT, POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);
    public static final DrawBatch INSTANCED_COLOR_GL_LINE = new DrawBatch(() -> ThinGL.programs().getInstancedColor(), DrawMode.LINES, POSITION_COLOR_LAYOUT, POSITION_COLOR_LAYOUT, () -> {
        PUSH_ENABLE_BLEND.run();
        ThinGL.glStateStack().enable(GL11C.GL_LINE_SMOOTH);
        GL11C.glHint(GL11C.GL_LINE_SMOOTH_HINT, GL11C.GL_NICEST);
    }, POP);
    public static final DrawBatch INSTANCED_COLOR_TRIANGLE_FAN = new DrawBatch(() -> ThinGL.programs().getInstancedColor(), DrawMode.TRIANGLE_FAN, POSITION_COLOR_LAYOUT, POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);
    public static final DrawBatch INSTANCED_COLOR_TRIANGLE_STRIP = new DrawBatch(() -> ThinGL.programs().getInstancedColor(), DrawMode.TRIANGLE_STRIP, POSITION_COLOR_LAYOUT, POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);

    // === Multidraw Draw Batches ===

    public static final DrawBatch MULTIDRAW_COLOR_QUAD = new DrawBatch(() -> ThinGL.programs().getMultidrawColor(), DrawMode.QUADS, POSITION_COLOR_LAYOUT, PUSH_ENABLE_BLEND, POP);
    public static final DrawBatch MULTIDRAW_COLOR_LINE = new DrawBatch(() -> ThinGL.programs().getMultidrawLine(), DrawMode.LINES, LINE_LAYOUT, () -> {
        PUSH_ENABLE_BLEND.run();
        ThinGL.glStateStack().disable(GL11C.GL_CULL_FACE);
    }, POP);

}
