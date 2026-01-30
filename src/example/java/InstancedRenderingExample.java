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

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.application.GLFWApplicationRunner;
import net.raphimc.thingl.rendering.DrawBatches;
import net.raphimc.thingl.rendering.bufferbuilder.impl.VertexBufferBuilder;
import net.raphimc.thingl.util.RenderMathUtil;
import org.joml.Matrix4fStack;

public class InstancedRenderingExample extends GLFWApplicationRunner {

    public static void main(String[] args) {
        new InstancedRenderingExample().run();
    }

    public InstancedRenderingExample() {
        super(new Configuration().setWindowTitle("ThinGL Example - Instancing").setExtendedDebugMode(true));
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        // Begin buffering the draw calls
        ThinGL.renderer2D().beginGlobalBuffering();
        { // Draw the object which should be instanced
            ThinGL.renderer2D().filledRectangle(RenderMathUtil.getIdentityMatrix(), 0, 0, 50, 50, Color.WHITE);
            ThinGL.renderer2D().filledRectangle(RenderMathUtil.getIdentityMatrix(), 50, 50, 100, 100, Color.WHITE);
            ThinGL.renderer2D().filledRectangle(RenderMathUtil.getIdentityMatrix(), 100, 100, 150, 150, Color.WHITE);
            ThinGL.renderer2D().filledRectangle(RenderMathUtil.getIdentityMatrix(), 100, 0, 150, 50, Color.WHITE);
            ThinGL.renderer2D().filledRectangle(RenderMathUtil.getIdentityMatrix(), 0, 100, 50, 150, Color.WHITE);
        }
        // Replace the used draw batch with the instanced version
        ThinGL.renderer2D().getTargetMultiDrawBatchDataHolder().replaceDrawBatch(DrawBatches.COLOR_QUAD, DrawBatches.INSTANCED_COLOR_QUAD);
        // Get the vertex buffer builder for the instance vertex data
        final VertexBufferBuilder vertexBufferBuilder = ThinGL.renderer2D().getTargetMultiDrawBatchDataHolder().getInstanceVertexBufferBuilder(DrawBatches.INSTANCED_COLOR_QUAD);
        // Each "vertex" is an instance of the object we want to draw
        vertexBufferBuilder.writeVector3f(positionMatrix, 0, 0, 0).writeColor(Color.RED).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, 150, 0, 0).writeColor(Color.GREEN).endVertex();
        vertexBufferBuilder.writeVector3f(positionMatrix, 300, 0, 0).writeColor(Color.BLUE).endVertex();

        ThinGL.renderer2D().endBuffering().draw();
    }

}
