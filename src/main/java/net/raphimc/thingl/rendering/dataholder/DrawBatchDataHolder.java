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
package net.raphimc.thingl.rendering.dataholder;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.raphimc.thingl.memory.MemoryBuffer;
import net.raphimc.thingl.rendering.bufferbuilder.ShaderBufferBuilder;
import net.raphimc.thingl.rendering.bufferbuilder.impl.IndexBufferBuilder;
import net.raphimc.thingl.rendering.bufferbuilder.impl.VertexBufferBuilder;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DrawBatchDataHolder {

    private final Supplier<MemoryBuffer> memoryBufferSupplier;
    private final Consumer<MemoryBuffer> memoryBufferDisposer;
    private VertexBufferBuilder vertexBufferBuilder;
    private VertexBufferBuilder instanceVertexBufferBuilder;
    private IndexBufferBuilder indexBufferBuilder;
    private final Object2ObjectMap<String, ShaderBufferBuilder> uniformBufferBuilders = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, ShaderBufferBuilder> shaderStorageBufferBuilders = new Object2ObjectOpenHashMap<>();

    public DrawBatchDataHolder(final Supplier<MemoryBuffer> memoryBufferSupplier, final Consumer<MemoryBuffer> memoryBufferDisposer) {
        this.memoryBufferSupplier = memoryBufferSupplier;
        this.memoryBufferDisposer = memoryBufferDisposer;
    }

    public VertexBufferBuilder getVertexBufferBuilder() {
        if (this.vertexBufferBuilder == null) {
            this.vertexBufferBuilder = new VertexBufferBuilder(this.memoryBufferSupplier.get());
        }
        return this.vertexBufferBuilder;
    }

    public VertexBufferBuilder getInstanceVertexBufferBuilder() {
        if (this.instanceVertexBufferBuilder == null) {
            this.instanceVertexBufferBuilder = new VertexBufferBuilder(this.memoryBufferSupplier.get());
        }
        return this.instanceVertexBufferBuilder;
    }

    public IndexBufferBuilder getIndexBufferBuilder() {
        if (this.indexBufferBuilder == null) {
            this.indexBufferBuilder = new IndexBufferBuilder(this.memoryBufferSupplier.get());
        }
        if (this.vertexBufferBuilder != null) {
            this.indexBufferBuilder.applyVertexOffset(this.vertexBufferBuilder);
        }
        return this.indexBufferBuilder;
    }

    public ShaderBufferBuilder getUniformBufferBuilder(final String name, final Function<MemoryBuffer, ? extends ShaderBufferBuilder> uniformBufferBuilderSupplier) {
        return this.uniformBufferBuilders.computeIfAbsent(name, key -> uniformBufferBuilderSupplier.apply(this.memoryBufferSupplier.get()));
    }

    public ShaderBufferBuilder getShaderStorageBufferBuilder(final String name, final Function<MemoryBuffer, ? extends ShaderBufferBuilder> shaderStorageBufferBuilderSupplier) {
        return this.shaderStorageBufferBuilders.computeIfAbsent(name, key -> shaderStorageBufferBuilderSupplier.apply(this.memoryBufferSupplier.get()));
    }

    public boolean hasVertexData() {
        return this.vertexBufferBuilder != null;
    }

    public boolean hasInstanceVertexData() {
        return this.instanceVertexBufferBuilder != null;
    }

    public boolean hasIndexData() {
        return this.indexBufferBuilder != null;
    }

    public boolean hasUniformData(final String name) {
        return this.uniformBufferBuilders.containsKey(name);
    }

    public boolean hasShaderStorageData(final String name) {
        return this.shaderStorageBufferBuilders.containsKey(name);
    }

    public Map<String, ShaderBufferBuilder> getUniformBufferBuilders() {
        return this.uniformBufferBuilders;
    }

    public Map<String, ShaderBufferBuilder> getShaderStorageBufferBuilders() {
        return this.shaderStorageBufferBuilders;
    }

    public void free() {
        if (this.vertexBufferBuilder != null) {
            this.memoryBufferDisposer.accept(this.vertexBufferBuilder.getMemoryBuffer());
        }
        if (this.instanceVertexBufferBuilder != null) {
            this.memoryBufferDisposer.accept(this.instanceVertexBufferBuilder.getMemoryBuffer());
        }
        if (this.indexBufferBuilder != null) {
            this.memoryBufferDisposer.accept(this.indexBufferBuilder.getMemoryBuffer());
        }
        for (ShaderBufferBuilder uniformBufferBuilder : this.uniformBufferBuilders.values()) {
            this.memoryBufferDisposer.accept(uniformBufferBuilder.getMemoryBuffer());
        }
        for (ShaderBufferBuilder shaderStorageBufferBuilder : this.shaderStorageBufferBuilders.values()) {
            this.memoryBufferDisposer.accept(shaderStorageBufferBuilder.getMemoryBuffer());
        }
    }

}
