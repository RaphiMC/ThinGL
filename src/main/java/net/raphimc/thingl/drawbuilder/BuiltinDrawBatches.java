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

    // === Snippets ===

    public static final DrawBatch.Snippet BLEND_SNIPPET = new DrawBatch.Builder()
            .setupAction(() -> {
                ThinGL.glStateStack().push();
                ThinGL.glStateStack().enable(GL11C.GL_BLEND);
            })
            .cleanupAction(() -> ThinGL.glStateStack().pop())
            .buildSnippet();

    public static final DrawBatch.Snippet COLOR_SNIPPET = new DrawBatch.Builder(BLEND_SNIPPET)
            .program(() -> ThinGL.programs().getColor())
            .drawMode(DrawMode.QUADS)
            .vertexDataLayout(POSITION_COLOR_LAYOUT)
            .buildSnippet();

    public static final DrawBatch.Snippet TEXTURE_SNIPPET = new DrawBatch.Builder(BLEND_SNIPPET)
            .program(() -> ThinGL.programs().getTexture())
            .drawMode(DrawMode.QUADS)
            .vertexDataLayout(POSITION_TEXTURE_LAYOUT)
            .buildSnippet();

    // === Draw Batches ===

    public static final DrawBatch COLOR_QUAD = new DrawBatch.Builder(COLOR_SNIPPET)
            .drawMode(DrawMode.QUADS)
            .build();

    public static final DrawBatch COLOR_TRIANGLE = new DrawBatch.Builder(COLOR_SNIPPET)
            .drawMode(DrawMode.TRIANGLES)
            .build();

    public static final DrawBatch INDEXED_COLOR_TRIANGLE = new DrawBatch.Builder(COLOR_SNIPPET)
            .drawMode(DrawMode.INDEXED_TRIANGLES)
            .build();

    public static final DrawBatch COLOR_GL_LINE = new DrawBatch.Builder(COLOR_SNIPPET)
            .drawMode(DrawMode.LINES)
            .appendSetupAction(() -> {
                ThinGL.glStateStack().enable(GL11C.GL_LINE_SMOOTH);
                GL11C.glHint(GL11C.GL_LINE_SMOOTH_HINT, GL11C.GL_NICEST);
            })
            .build();

    public static final DrawBatch COLOR_LINE = new DrawBatch.Builder(COLOR_SNIPPET)
            .program(() -> ThinGL.programs().getLine())
            .drawMode(DrawMode.LINES)
            .vertexDataLayout(LINE_LAYOUT)
            .appendSetupAction(() -> ThinGL.glStateStack().disable(GL11C.GL_CULL_FACE))
            .build();

    public static final DrawBatch COLOR_TRIANGLE_FAN = new DrawBatch.Builder(COLOR_SNIPPET)
            .drawMode(DrawMode.TRIANGLE_FAN)
            .build();

    public static final DrawBatch COLOR_TRIANGLE_STRIP = new DrawBatch.Builder(COLOR_SNIPPET)
            .drawMode(DrawMode.TRIANGLE_STRIP)
            .build();

    // === Instanced Draw Batches ===

    public static final DrawBatch INSTANCED_COLOR_QUAD = new DrawBatch.Builder(COLOR_QUAD)
            .program(() -> ThinGL.programs().getInstancedColor())
            .instanceVertexDataLayout(POSITION_COLOR_LAYOUT)
            .build();

    public static final DrawBatch INSTANCED_COLOR_TRIANGLE = new DrawBatch.Builder(COLOR_TRIANGLE)
            .program(() -> ThinGL.programs().getInstancedColor())
            .instanceVertexDataLayout(POSITION_COLOR_LAYOUT)
            .build();

    public static final DrawBatch INSTANCED_COLOR_GL_LINE = new DrawBatch.Builder(COLOR_GL_LINE)
            .program(() -> ThinGL.programs().getInstancedColor())
            .instanceVertexDataLayout(POSITION_COLOR_LAYOUT)
            .build();

    public static final DrawBatch INSTANCED_COLOR_TRIANGLE_FAN = new DrawBatch.Builder(COLOR_TRIANGLE_FAN)
            .program(() -> ThinGL.programs().getInstancedColor())
            .instanceVertexDataLayout(POSITION_COLOR_LAYOUT)
            .build();

    public static final DrawBatch INSTANCED_COLOR_TRIANGLE_STRIP = new DrawBatch.Builder(COLOR_TRIANGLE_STRIP)
            .program(() -> ThinGL.programs().getInstancedColor())
            .instanceVertexDataLayout(POSITION_COLOR_LAYOUT)
            .build();

    // === Multidraw Draw Batches ===

    public static final DrawBatch MULTIDRAW_COLOR_QUAD = new DrawBatch.Builder(COLOR_QUAD)
            .program(() -> ThinGL.programs().getMultidrawColor())
            .build();

    public static final DrawBatch MULTIDRAW_COLOR_LINE = new DrawBatch.Builder(COLOR_LINE)
            .program(() -> ThinGL.programs().getMultidrawLine())
            .build();

}
