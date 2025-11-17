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
package net.raphimc.thingl.gl.rendering.dataholder;

import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.lenni0451.commons.arrays.ArrayUtils;
import net.raphimc.thingl.gl.rendering.DrawBatchRenderer;
import net.raphimc.thingl.gl.rendering.upload.DrawBatchDataUploader;
import net.raphimc.thingl.gl.rendering.upload.UploadedDrawBatchData;
import net.raphimc.thingl.memory.MemoryBuffer;
import net.raphimc.thingl.rendering.DrawBatch;
import net.raphimc.thingl.rendering.dataholder.DrawBatchDataHolder;
import net.raphimc.thingl.rendering.dataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.rendering.preparation.DrawBatchDataOptimizer;
import net.raphimc.thingl.rendering.preparation.DrawBatchDataPreparer;
import net.raphimc.thingl.rendering.preparation.PreparedDrawBatchData;
import org.joml.Matrix4f;

import java.util.Map;

public class PersistentMultiDrawBatchDataHolder extends MultiDrawBatchDataHolder {

    private final Reference2ObjectMap<DrawBatch, PreparedDrawBatchData> preparedDrawBatches = new Reference2ObjectLinkedOpenHashMap<>();
    private final Reference2ObjectMap<DrawBatch, UploadedDrawBatchData> uploadedDrawBatches = new Reference2ObjectLinkedOpenHashMap<>();

    public PersistentMultiDrawBatchDataHolder() {
    }

    public PersistentMultiDrawBatchDataHolder(final DrawBatch[] firstOrderedDrawBatches, final DrawBatch[] lastOrderedDrawBatches) {
        super(firstOrderedDrawBatches, lastOrderedDrawBatches);
    }

    public void build() {
        this.prepare();
        this.upload();
    }

    public void prepare() {
        this.freePreparedBatches();
        for (Map.Entry<DrawBatch, DrawBatchDataHolder> entry : this.drawBatches.entrySet()) {
            this.preparedDrawBatches.put(entry.getKey(), DrawBatchDataPreparer.prepareDrawBatchData(entry.getValue()));
        }
        this.drawBatches.clear();
        this.invalidateCache();
    }

    public void optimize() {
        this.preparedDrawBatches.replaceAll((drawBatch, preparedDrawBatchData) -> DrawBatchDataOptimizer.optimize(preparedDrawBatchData));
    }

    public void upload() {
        this.freeUploadedBatches();
        for (Map.Entry<DrawBatch, PreparedDrawBatchData> entry : this.preparedDrawBatches.entrySet()) {
            this.uploadedDrawBatches.put(entry.getKey(), DrawBatchDataUploader.uploadPersistent(entry.getValue()));
            DrawBatchDataPreparer.freePreparedDrawBatchData(entry.getValue());
        }
        this.preparedDrawBatches.clear();
    }

    @Override
    public void draw(final Matrix4f modelMatrix) {
        if (this.hasDrawBatches()) {
            for (DrawBatch drawBatch : this.firstOrderedDrawBatches) {
                this.draw(drawBatch, modelMatrix);
            }
            for (DrawBatch drawBatch : this.uploadedDrawBatches.keySet()) {
                if (!ArrayUtils.contains(this.firstOrderedDrawBatches, drawBatch) && !ArrayUtils.contains(this.lastOrderedDrawBatches, drawBatch)) {
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
        final UploadedDrawBatchData uploadedDrawBatchData = this.uploadedDrawBatches.get(drawBatch);
        if (uploadedDrawBatchData != null) {
            DrawBatchRenderer.render(uploadedDrawBatchData, modelMatrix);
        }
    }

    public PreparedDrawBatchData getPreparedDrawBatch(final DrawBatch drawBatch) {
        return this.preparedDrawBatches.get(drawBatch);
    }

    public Map<DrawBatch, PreparedDrawBatchData> getPreparedDrawBatches() {
        return this.preparedDrawBatches;
    }

    public UploadedDrawBatchData getUploadedDrawBatch(final DrawBatch drawBatch) {
        return this.uploadedDrawBatches.get(drawBatch);
    }

    public Map<DrawBatch, UploadedDrawBatchData> getUploadedDrawBatches() {
        return this.uploadedDrawBatches;
    }

    public void free() {
        super.free();
        this.freePreparedBatches();
        this.freeUploadedBatches();
    }

    @Override
    public boolean hasDrawBatches() {
        return !this.uploadedDrawBatches.isEmpty();
    }

    @Override
    protected DrawBatchDataHolder createDrawBatchDataHolder(final DrawBatch drawBatch) {
        return new DrawBatchDataHolder(drawBatch, MemoryBuffer::new, MemoryBuffer::free);
    }

    private void freePreparedBatches() {
        this.preparedDrawBatches.values().forEach(DrawBatchDataPreparer::freePreparedDrawBatchData);
        this.preparedDrawBatches.clear();
    }

    private void freeUploadedBatches() {
        this.uploadedDrawBatches.values().forEach(DrawBatchDataUploader::freePersistentData);
        this.uploadedDrawBatches.clear();
    }

}
