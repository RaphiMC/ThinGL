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
import net.raphimc.thingl.drawbuilder.databuilder.holder.ShaderDataHolder;
import net.raphimc.thingl.drawbuilder.databuilder.holder.VertexDataHolder;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DrawBatchDataHolder {

    private final Supplier<BufferBuilder> bufferBuilderSupplier;
    private final Consumer<BufferBuilder> bufferBuilderDisposer;
    private VertexDataHolder vertexDataHolder;
    private VertexDataHolder instanceVertexDataHolder;
    private IndexDataHolder indexDataHolder;
    private final Object2ObjectMap<String, ShaderDataHolder> uniformDataHolders = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, ShaderDataHolder> shaderStorageDataHolders = new Object2ObjectOpenHashMap<>();

    public DrawBatchDataHolder(final Supplier<BufferBuilder> bufferBuilderSupplier, final Consumer<BufferBuilder> bufferBuilderDisposer) {
        this.bufferBuilderSupplier = bufferBuilderSupplier;
        this.bufferBuilderDisposer = bufferBuilderDisposer;
    }

    public void free() {
        if (this.vertexDataHolder != null) {
            this.bufferBuilderDisposer.accept(this.vertexDataHolder.getBufferBuilder());
        }
        if (this.instanceVertexDataHolder != null) {
            this.bufferBuilderDisposer.accept(this.instanceVertexDataHolder.getBufferBuilder());
        }
        if (this.indexDataHolder != null) {
            this.bufferBuilderDisposer.accept(this.indexDataHolder.getBufferBuilder());
        }
        for (ShaderDataHolder uniformDataHolder : this.uniformDataHolders.values()) {
            this.bufferBuilderDisposer.accept(uniformDataHolder.getBufferBuilder());
        }
        for (ShaderDataHolder shaderStorageDataHolder : this.shaderStorageDataHolders.values()) {
            this.bufferBuilderDisposer.accept(shaderStorageDataHolder.getBufferBuilder());
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

    public boolean hasInstanceVertexDataHolder() {
        return this.instanceVertexDataHolder != null;
    }

    public VertexDataHolder getInstanceVertexDataHolder() {
        if (this.instanceVertexDataHolder == null) {
            this.instanceVertexDataHolder = new VertexDataHolder(this.bufferBuilderSupplier.get());
        }
        return this.instanceVertexDataHolder;
    }

    public boolean hasIndexDataHolder() {
        return this.indexDataHolder != null;
    }

    public IndexDataHolder getIndexDataHolder() {
        if (this.indexDataHolder == null) {
            this.indexDataHolder = new IndexDataHolder(this.bufferBuilderSupplier.get());
        }
        if (this.vertexDataHolder != null) {
            this.indexDataHolder.applyVertexOffset(this.vertexDataHolder);
        }
        return this.indexDataHolder;
    }

    public boolean hasUniformDataHolder(final String name) {
        return this.uniformDataHolders.containsKey(name);
    }

    public ShaderDataHolder getUniformDataHolder(final String name, final Function<BufferBuilder, ? extends ShaderDataHolder> uniformDataHolderSupplier) {
        return this.uniformDataHolders.computeIfAbsent(name, key -> uniformDataHolderSupplier.apply(this.bufferBuilderSupplier.get()));
    }

    public Map<String, ShaderDataHolder> getUniformDataHolders() {
        return this.uniformDataHolders;
    }

    public boolean hasShaderStorageDataHolder(final String name) {
        return this.shaderStorageDataHolders.containsKey(name);
    }

    public ShaderDataHolder getShaderStorageDataHolder(final String name, final Function<BufferBuilder, ? extends ShaderDataHolder> shaderStorageDataHolderSupplier) {
        return this.shaderStorageDataHolders.computeIfAbsent(name, key -> shaderStorageDataHolderSupplier.apply(this.bufferBuilderSupplier.get()));
    }

    public Map<String, ShaderDataHolder> getShaderStorageDataHolders() {
        return this.shaderStorageDataHolders;
    }

}
