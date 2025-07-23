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
package net.raphimc.thingl.drawbuilder.multidraw;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.lenni0451.commons.arrays.ArrayUtils;
import net.lenni0451.commons.math.MathUtils;
import net.raphimc.thingl.drawbuilder.DrawBatch;
import net.raphimc.thingl.drawbuilder.builder.BufferRenderer;
import net.raphimc.thingl.drawbuilder.builder.BuiltBuffer;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.PersistentMultiDrawBatchDataHolder;
import net.raphimc.thingl.resource.buffer.Buffer;
import net.raphimc.thingl.util.RenderMathUtil;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiDrawRenderer {

    private final DrawBatch[] firstOrderedDrawBatches;
    private final DrawBatch[] lastOrderedDrawBatches;
    private final Reference2ObjectMap<DrawBatch, MultiDrawBuilder> drawBatches = new Reference2ObjectLinkedOpenHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger();
    private final Int2ObjectMap<Reference2IntMap<DrawBatch>> drawBatchBuffers = new Int2ObjectOpenHashMap<>();

    public MultiDrawRenderer() {
        this(DrawBatch.EMPTY_ARRAY, DrawBatch.EMPTY_ARRAY);
    }

    public MultiDrawRenderer(final DrawBatch[] firstOrderedDrawBatches, final DrawBatch[] lastOrderedDrawBatches) {
        this.firstOrderedDrawBatches = firstOrderedDrawBatches;
        this.lastOrderedDrawBatches = lastOrderedDrawBatches;
    }

    public int uploadDrawBatchBuffers(final PersistentMultiDrawBatchDataHolder multiDrawBatchDataHolder) {
        final int id = this.idGenerator.getAndIncrement();
        final Reference2IntMap<DrawBatch> multiDrawIds = new Reference2IntOpenHashMap<>();
        for (Map.Entry<DrawBatch, BuiltBuffer> entry : multiDrawBatchDataHolder.getBuiltDrawBatches().entrySet()) {
            final MultiDrawBuilder multiDrawBuilder = this.drawBatches.computeIfAbsent(entry.getKey(), drawBatch -> new MultiDrawBuilder((DrawBatch) drawBatch));
            multiDrawIds.put(entry.getKey(), multiDrawBuilder.uploadBuffer(entry.getValue()));
        }
        this.drawBatchBuffers.put(id, multiDrawIds);
        return id;
    }

    public void removeDrawBatchBuffers(final int id) {
        if (!this.drawBatchBuffers.containsKey(id)) {
            throw new IllegalArgumentException("DrawBatch is not uploaded");
        }
        this.removeFromRenderList(id);
        final Reference2IntMap<DrawBatch> multiDrawIds = this.drawBatchBuffers.remove(id);
        for (Reference2IntMap.Entry<DrawBatch> entry : multiDrawIds.reference2IntEntrySet()) {
            final MultiDrawBuilder multiDrawBuilder = this.drawBatches.get(entry.getKey());
            multiDrawBuilder.removeBuffer(entry.getIntValue());
        }
    }

    public void clearDrawBatchBuffers() {
        for (int id : this.drawBatchBuffers.keySet().toIntArray()) {
            this.removeDrawBatchBuffers(id);
        }
        this.idGenerator.set(0);
    }

    public void addToRenderList(final int id) {
        if (!this.drawBatchBuffers.containsKey(id)) {
            throw new IllegalArgumentException("DrawBatch is not uploaded");
        }
        final Reference2IntMap<DrawBatch> multiDrawIds = this.drawBatchBuffers.get(id);
        for (Reference2IntMap.Entry<DrawBatch> entry : multiDrawIds.reference2IntEntrySet()) {
            final MultiDrawBuilder multiDrawBuilder = this.drawBatches.get(entry.getKey());
            multiDrawBuilder.addToRenderList(entry.getIntValue());
        }
    }

    public void removeFromRenderList(final int id) {
        if (!this.drawBatchBuffers.containsKey(id)) {
            throw new IllegalArgumentException("DrawBatch is not uploaded");
        }
        final Reference2IntMap<DrawBatch> multiDrawIds = this.drawBatchBuffers.get(id);
        for (Reference2IntMap.Entry<DrawBatch> entry : multiDrawIds.reference2IntEntrySet()) {
            final MultiDrawBuilder multiDrawBuilder = this.drawBatches.get(entry.getKey());
            multiDrawBuilder.removeFromRenderList(entry.getIntValue());
        }
    }

    public void clearRenderList() {
        for (MultiDrawBuilder multiDrawBuilder : this.drawBatches.values()) {
            multiDrawBuilder.clearRenderList();
        }
    }

    public void rebuildCommandBuffer() {
        for (MultiDrawBuilder multiDrawBuilder : this.drawBatches.values()) {
            multiDrawBuilder.rebuildCommandBuffer();
        }
    }

    public void draw() {
        this.draw(RenderMathUtil.getIdentityMatrix());
    }

    public void draw(final Matrix4f modelMatrix) {
        if (this.hasDrawBatches()) {
            for (DrawBatch drawBatch : this.firstOrderedDrawBatches) {
                this.draw(drawBatch, modelMatrix);
            }
            for (DrawBatch drawBatch : this.drawBatches.keySet()) {
                if (!ArrayUtils.contains(this.firstOrderedDrawBatches, drawBatch) && !ArrayUtils.contains(this.lastOrderedDrawBatches, drawBatch)) {
                    this.draw(drawBatch, modelMatrix);
                }
            }
            for (DrawBatch drawBatch : this.lastOrderedDrawBatches) {
                this.draw(drawBatch, modelMatrix);
            }
        }
    }

    public void draw(final Matrix4f modelMatrix, final Buffer drawDataBuffer) {
        if (this.hasDrawBatches()) {
            for (DrawBatch drawBatch : this.firstOrderedDrawBatches) {
                this.draw(drawBatch, modelMatrix, drawDataBuffer);
            }
            for (DrawBatch drawBatch : this.drawBatches.keySet()) {
                if (!ArrayUtils.contains(this.firstOrderedDrawBatches, drawBatch) && !ArrayUtils.contains(this.lastOrderedDrawBatches, drawBatch)) {
                    this.draw(drawBatch, modelMatrix, drawDataBuffer);
                }
            }
            for (DrawBatch drawBatch : this.lastOrderedDrawBatches) {
                this.draw(drawBatch, modelMatrix, drawDataBuffer);
            }
        }
    }

    public void draw(final DrawBatch drawBatch, final Matrix4f modelMatrix) {
        this.draw(drawBatch, modelMatrix, null);
    }

    public void draw(final DrawBatch drawBatch, final Matrix4f modelMatrix, final Buffer drawDataBuffer) {
        final MultiDrawBuilder multiDrawBuilder = this.drawBatches.get(drawBatch);
        if (multiDrawBuilder != null) {
            multiDrawBuilder.getBuiltBuffer().shaderDataBuffers().put("ssbo_DrawData", drawDataBuffer);
            BufferRenderer.render(multiDrawBuilder.getBuiltBuffer(), modelMatrix);
            multiDrawBuilder.getBuiltBuffer().shaderDataBuffers().remove("ssbo_DrawData");
        }
    }

    public String getMemoryAllocationString() {
        long vertexUsedMemory = 0;
        long indexUsedMemory = 0;
        for (MultiDrawBuilder multiDrawBuilder : this.drawBatches.values()) {
            vertexUsedMemory += multiDrawBuilder.getVertexAllocator().getUsedMemory();
            indexUsedMemory += multiDrawBuilder.getIndexAllocator().getUsedMemory();
        }
        return "V Mem: " + MathUtils.formatBytes(vertexUsedMemory) + ", I Mem: " + MathUtils.formatBytes(indexUsedMemory);
    }

    public void free() {
        this.drawBatches.values().forEach(MultiDrawBuilder::free);
        this.drawBatches.clear();
    }

    public boolean hasDrawBatches() {
        return !this.drawBatches.isEmpty();
    }

}
