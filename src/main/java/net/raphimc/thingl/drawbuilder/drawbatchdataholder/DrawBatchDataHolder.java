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

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.IndexDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.InstanceDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.ShaderDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.VertexDataHolder;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DrawBatchDataHolder {

    private final Supplier<BufferBuilder> bufferBuilderSupplier;
    private final Consumer<BufferBuilder> bufferBuilderDisposer;
    private VertexDataHolder vertexDataHolder;
    private IndexDataHolder indexDataHolder;
    private InstanceDataHolder instanceDataHolder;
    private final Object2ObjectMap<String, ShaderDataHolder> shaderDataHolders = new Object2ObjectOpenHashMap<>();

    public DrawBatchDataHolder(final Supplier<BufferBuilder> bufferBuilderSupplier, final Consumer<BufferBuilder> bufferBuilderDisposer) {
        this.bufferBuilderSupplier = bufferBuilderSupplier;
        this.bufferBuilderDisposer = bufferBuilderDisposer;
    }

    public void free() {
        if (this.vertexDataHolder != null) {
            this.bufferBuilderDisposer.accept(this.vertexDataHolder.getBufferBuilder());
        }
        if (this.indexDataHolder != null) {
            this.bufferBuilderDisposer.accept(this.indexDataHolder.getBufferBuilder());
        }
        if (this.instanceDataHolder != null) {
            this.bufferBuilderDisposer.accept(this.instanceDataHolder.getBufferBuilder());
        }
        for (ShaderDataHolder bufferShaderDataHolder : this.shaderDataHolders.values()) {
            this.bufferBuilderDisposer.accept(bufferShaderDataHolder.getBufferBuilder());
        }
    }

    public boolean hasVertexDataHolder() {
        return this.vertexDataHolder != null;
    }

    public VertexDataHolder getVertexDataHolder() {
        if (this.vertexDataHolder == null) {
            this.vertexDataHolder = new VertexDataHolder(this.bufferBuilderSupplier.get());
        }
        return this.vertexDataHolder;
    }

    public boolean hasIndexDataHolder() {
        return this.indexDataHolder != null;
    }

    public IndexDataHolder getIndexDataHolder() {
        if (this.indexDataHolder == null) {
            this.indexDataHolder = new IndexDataHolder(this.bufferBuilderSupplier.get());
        }
        this.indexDataHolder.applyVertexOffset(this.vertexDataHolder);
        return this.indexDataHolder;
    }

    public boolean hasInstanceDataHolder() {
        return this.instanceDataHolder != null;
    }

    public InstanceDataHolder getInstanceDataHolder() {
        if (this.instanceDataHolder == null) {
            this.instanceDataHolder = new InstanceDataHolder(this.bufferBuilderSupplier.get());
        }
        return this.instanceDataHolder;
    }

    public boolean hasShaderDataHolder() {
        return !this.shaderDataHolders.isEmpty();
    }

    public ShaderDataHolder getShaderDataHolder(final String name) {
        return this.shaderDataHolders.computeIfAbsent(name, key -> new ShaderDataHolder(this.bufferBuilderSupplier.get()));
    }

    public Map<String, ShaderDataHolder> getShaderDataHolders() {
        return this.shaderDataHolders;
    }

}
