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

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.ImmediateMultiDrawBatchDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.implementation.application.StandaloneApplicationRunner;
import org.joml.Matrix4fStack;

public class DrawCallBatchingExample extends StandaloneApplicationRunner {

    public static void main(String[] args) {
        new DrawCallBatchingExample().launch();
    }

    public DrawCallBatchingExample() {
        super(new Configuration().setWindowTitle("ThinGL Example - Draw call batching").setExtendedDebugMode(true));
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        final MultiDrawBatchDataHolder drawBatch = new ImmediateMultiDrawBatchDataHolder();

        ThinGL.renderer2D().beginBuffering(drawBatch); // Renderer2D now renders everything into drawBatch
        // ThinGL.renderer2D().beginGlobalBuffering(); // Alternatively render everything into a global buffer
        for (int i = 0; i < 10; i++) {
            final int x = i * 10;
            final int y = i * 10;
            final int width = 10;
            final int height = 10;
            ThinGL.renderer2D().filledRectangle(positionMatrix, x, y, x + width, y + height, Color.fromRGBA(i * 25, 0, 0, 255));
        }
        ThinGL.renderer2D().endBuffering(); // Renderer2D now renders everything immediately again
        // ThinGL.renderer2D().endBuffering().draw(); // Same as line above, but also render the contents of the global buffer

        drawBatch.draw(); // Draw everything in drawBatch
        drawBatch.free(); // Free up resources
    }

}
