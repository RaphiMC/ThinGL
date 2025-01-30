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

import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.lenni0451.commons.arrays.ArrayUtils;
import net.raphimc.thingl.drawbuilder.DrawBatch;
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import net.raphimc.thingl.drawbuilder.builder.BufferRenderer;
import net.raphimc.thingl.drawbuilder.builder.BuiltBuffer;
import net.raphimc.thingl.drawbuilder.builder.PreparedBuffer;
import org.joml.Matrix4f;

import java.util.Map;

public class PersistentMultiDrawBatchDataHolder extends MultiDrawBatchDataHolder {

    private final Reference2ObjectMap<DrawBatch, PreparedBuffer> preparedDrawBatches = new Reference2ObjectLinkedOpenHashMap<>();
    private final Reference2ObjectMap<DrawBatch, BuiltBuffer> builtDrawBatches = new Reference2ObjectLinkedOpenHashMap<>();

    public PersistentMultiDrawBatchDataHolder() {
    }

    public PersistentMultiDrawBatchDataHolder(final DrawBatch[] firstOrderedDrawBatches, final DrawBatch[] lastOrderedDrawBatches) {
        super(firstOrderedDrawBatches, lastOrderedDrawBatches);
    }

    public void optimize() {
        this.deletePreparedBatches();
        for (Map.Entry<DrawBatch, DrawBatchDataHolder> entry : this.drawBatches.entrySet()) {
            this.preparedDrawBatches.put(entry.getKey(), BufferRenderer.prepareBuffer(entry.getKey(), entry.getValue(), true));
        }
    }

    public void build() {
        this.deleteBuiltBatches();
        for (Map.Entry<DrawBatch, DrawBatchDataHolder> entry : this.drawBatches.entrySet()) {
            final PreparedBuffer preparedBuffer;
            if (this.preparedDrawBatches.containsKey(entry.getKey())) {
                preparedBuffer = this.preparedDrawBatches.remove(entry.getKey());
            } else {
                preparedBuffer = BufferRenderer.prepareBuffer(entry.getKey(), entry.getValue(), false);
            }
            this.builtDrawBatches.put(entry.getKey(), BufferRenderer.buildPersistentBuffer(preparedBuffer));
        }
        this.drawBatches.clear();
        this.invalidateCache();
    }

    @Override
    public void draw(final Matrix4f modelMatrix) {
        if (this.hasDrawBatches()) {
            for (DrawBatch drawBatch : this.firstOrderedDrawBatches) {
                this.draw(drawBatch, modelMatrix);
            }
            for (DrawBatch drawBatch : this.builtDrawBatches.keySet()) {
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
        final BuiltBuffer builtBuffer = this.builtDrawBatches.get(drawBatch);
        if (builtBuffer != null) {
            BufferRenderer.render(builtBuffer, modelMatrix);
        }
    }

    public BuiltBuffer getBuiltBuffer(final DrawBatch drawBatch) {
        return this.builtDrawBatches.get(drawBatch);
    }

    public Reference2ObjectMap<DrawBatch, BuiltBuffer> getBuiltDrawBatches() {
        return this.builtDrawBatches;
    }

    public void delete() {
        super.delete();
        this.deletePreparedBatches();
        this.deleteBuiltBatches();
    }

    @Override
    public boolean hasDrawBatches() {
        return !this.builtDrawBatches.isEmpty();
    }

    @Override
    protected DrawBatchDataHolder createDrawBatchDataHolder(final DrawBatch drawBatch) {
        return new DrawBatchDataHolder(BufferBuilder::new, BufferBuilder::close);
    }

    private void deletePreparedBatches() {
        this.preparedDrawBatches.values().forEach(PreparedBuffer::delete);
        this.preparedDrawBatches.clear();
    }

    private void deleteBuiltBatches() {
        this.builtDrawBatches.values().forEach(BuiltBuffer::delete);
        this.builtDrawBatches.clear();
    }

}
