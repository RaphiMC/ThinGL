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
import net.raphimc.thingl.drawbuilder.DrawBatch;
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.IndexDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.ShaderDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.VertexDataHolder;
import net.raphimc.thingl.util.RenderMathUtil;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.function.Function;

public abstract class MultiDrawBatchDataHolder {

    protected final DrawBatch[] firstOrderedDrawBatches;
    protected final DrawBatch[] lastOrderedDrawBatches;
    protected final Reference2ObjectMap<DrawBatch, DrawBatchDataHolder> drawBatches = new Reference2ObjectLinkedOpenHashMap<>();
    protected DrawBatch lastDrawBatch;
    protected DrawBatchDataHolder lastDrawBatchDataHolder;

    public MultiDrawBatchDataHolder() {
        this(DrawBatch.EMPTY_ARRAY, DrawBatch.EMPTY_ARRAY);
    }

    public MultiDrawBatchDataHolder(final DrawBatch[] firstOrderedDrawBatches, final DrawBatch[] lastOrderedDrawBatches) {
        this.firstOrderedDrawBatches = firstOrderedDrawBatches;
        this.lastOrderedDrawBatches = lastOrderedDrawBatches;
    }

    public DrawBatchDataHolder getDrawBatchDataHolder(final DrawBatch drawBatch) {
        if (drawBatch != this.lastDrawBatch) {
            this.lastDrawBatch = drawBatch;
            this.lastDrawBatchDataHolder = this.drawBatches.computeIfAbsent(drawBatch, this::createDrawBatchDataHolder);
        }
        return this.lastDrawBatchDataHolder;
    }

    public VertexDataHolder getVertexDataHolder(final DrawBatch drawBatch) {
        return this.getDrawBatchDataHolder(drawBatch).getVertexDataHolder();
    }

    public VertexDataHolder getInstanceVertexDataHolder(final DrawBatch drawBatch) {
        return this.getDrawBatchDataHolder(drawBatch).getInstanceVertexDataHolder();
    }

    public IndexDataHolder getIndexDataHolder(final DrawBatch drawBatch) {
        return this.getDrawBatchDataHolder(drawBatch).getIndexDataHolder();
    }

    public ShaderDataHolder getShaderDataHolder(final DrawBatch drawBatch, final String name, final Function<BufferBuilder, ? extends ShaderDataHolder> shaderDataHolderSupplier) {
        return this.getDrawBatchDataHolder(drawBatch).getShaderDataHolder(name, shaderDataHolderSupplier);
    }

    public void draw() {
        this.draw(RenderMathUtil.getIdentityMatrix());
    }

    public abstract void draw(final Matrix4f modelMatrix);

    public abstract void draw(final DrawBatch drawBatch, final Matrix4f modelMatrix);

    public void free() {
        this.drawBatches.values().forEach(DrawBatchDataHolder::free);
        this.drawBatches.clear();
        this.invalidateCache();
    }

    public void replaceDrawBatch(final DrawBatch oldDrawBatch, final DrawBatch newDrawBatch) {
        if (oldDrawBatch == newDrawBatch) {
            return;
        }
        if (oldDrawBatch.drawMode() != newDrawBatch.drawMode()) {
            throw new IllegalArgumentException("Cannot replace draw batch because the draw mode is different");
        }
        if (oldDrawBatch.vertexDataLayout() != newDrawBatch.vertexDataLayout()) {
            throw new IllegalArgumentException("Cannot replace draw batch because the vertex data layout is different");
        }
        if (this.drawBatches.containsKey(oldDrawBatch)) {
            this.drawBatches.put(newDrawBatch, this.drawBatches.remove(oldDrawBatch));
            this.invalidateCache();
        }
    }

    public void replaceDrawBatches(final Map<DrawBatch, DrawBatch> drawBatches) {
        for (Map.Entry<DrawBatch, DrawBatch> entry : drawBatches.entrySet()) {
            this.replaceDrawBatch(entry.getKey(), entry.getValue());
        }
    }

    public boolean hasDrawBatches() {
        return !this.drawBatches.isEmpty();
    }

    protected abstract DrawBatchDataHolder createDrawBatchDataHolder(final DrawBatch drawBatch);

    protected void invalidateCache() {
        this.lastDrawBatch = null;
        this.lastDrawBatchDataHolder = null;
    }

}
