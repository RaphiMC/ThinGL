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
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.PersistentMultiDrawBatchDataHolder;
import net.raphimc.thingl.implementation.application.StandaloneApplicationRunner;
import net.raphimc.thingl.util.RenderMathUtil;
import org.joml.Matrix4fStack;

public class RetainedRenderingExample extends StandaloneApplicationRunner {

    public static void main(String[] args) {
        new RetainedRenderingExample().launch();
    }

    public RetainedRenderingExample() {
        super(new Configuration().setWindowTitle("ThinGL Example - Retained rendering").setExtendedDebugMode(true));
    }

    private final PersistentMultiDrawBatchDataHolder persistentDrawBatch = new PersistentMultiDrawBatchDataHolder();

    @Override
    protected void init() {
        super.init();
        ThinGL.renderer2D().beginBuffering(persistentDrawBatch); // Renderer2D now renders everything into persistentDrawBatch
        for (int i = 0; i < 10; i++) {
            final int x = i * 10;
            final int y = i * 10;
            final int width = 10;
            final int height = 10;
            ThinGL.renderer2D().filledRectangle(RenderMathUtil.getIdentityMatrix(), x, y, x + width, y + height, Color.fromRGBA(i * 25, 0, 0, 255));
        }
        ThinGL.renderer2D().endBuffering(); // Renderer2D now renders everything immediately again

        persistentDrawBatch.build(); // Build the buffers
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        persistentDrawBatch.draw(positionMatrix); // Draw the built contents of the drawBatch

        // persistentDrawBatch.free(); // Free up resources when done
    }

}
