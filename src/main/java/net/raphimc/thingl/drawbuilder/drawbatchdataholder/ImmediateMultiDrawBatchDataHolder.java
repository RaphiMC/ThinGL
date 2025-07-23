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
package net.raphimc.thingl.drawbuilder.drawbatchdataholder;

import net.lenni0451.commons.arrays.ArrayUtils;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.DrawBatch;
import net.raphimc.thingl.drawbuilder.builder.BufferRenderer;
import net.raphimc.thingl.drawbuilder.builder.BuiltBuffer;
import org.joml.Matrix4f;

public class ImmediateMultiDrawBatchDataHolder extends MultiDrawBatchDataHolder {

    public ImmediateMultiDrawBatchDataHolder() {
    }

    public ImmediateMultiDrawBatchDataHolder(final DrawBatch[] firstOrderedDrawBatches, final DrawBatch[] lastOrderedDrawBatches) {
        super(firstOrderedDrawBatches, lastOrderedDrawBatches);
    }

    @Override
    public void draw(final Matrix4f modelMatrix) {
        if (this.hasDrawBatches()) {
            for (DrawBatch drawBatch : this.firstOrderedDrawBatches) {
                this.draw(drawBatch, modelMatrix);
            }
            for (DrawBatch drawBatch : this.drawBatches.keySet().toArray(DrawBatch.EMPTY_ARRAY)) {
                if (!ArrayUtils.contains(this.lastOrderedDrawBatches, drawBatch)) {
                    this.draw(drawBatch, modelMatrix);
                }
            }
            for (DrawBatch drawBatch : this.lastOrderedDrawBatches) {
                this.draw(drawBatch, modelMatrix);
            }
        }
    }

    @Override
    public void draw(final DrawBatch drawBatch, final Matrix4f modelMatrix) {
        final DrawBatchDataHolder drawBatchDataHolder = this.drawBatches.remove(drawBatch);
        if (drawBatchDataHolder != null) {
            this.invalidateCache();
            final BuiltBuffer builtBuffer = BufferRenderer.buildTemporaryBuffer(BufferRenderer.prepareBuffer(drawBatch, drawBatchDataHolder, false));
            BufferRenderer.render(builtBuffer, modelMatrix);
            BufferRenderer.freeTemporaryBuffer(builtBuffer);
        }
    }

    @Override
    protected DrawBatchDataHolder createDrawBatchDataHolder(final DrawBatch drawBatch) {
        return new DrawBatchDataHolder(ThinGL.bufferBuilderPool()::borrowBufferBuilder, ThinGL.bufferBuilderPool()::returnBufferBuilder);
    }

}
