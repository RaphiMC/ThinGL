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
package net.raphimc.thingl.gl.resource.program;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.GLContainerObject;
import net.raphimc.thingl.gl.resource.buffer.Buffer;
import net.raphimc.thingl.gl.resource.framebuffer.Framebuffer;
import net.raphimc.thingl.gl.resource.image.texture.ImageTexture;
import net.raphimc.thingl.gl.resource.image.texture.Texture;
import net.raphimc.thingl.gl.resource.shader.Shader;
import net.raphimc.thingl.memory.allocator.MemoryAllocator;
import net.raphimc.thingl.resource.memory.Memory;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL31C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Program extends GLContainerObject {

    private Set<Shader> shaders;

    private final Object2IntMap<String> uniformLocationCache = new Object2IntOpenHashMap<>();
    private final Object2IntMap<String> uniformBlockIndexCache = new Object2IntOpenHashMap<>();
    private final Object2IntMap<String> shaderStorageBlockIndexCache = new Object2IntOpenHashMap<>();

    private int currentTextureUnit;
    private int currentImageUnit;
    private int currentUniformBufferIndex;
    private int currentShaderStorageBufferIndex;

    public Program(final Shader... shaders) {
        super(ThinGL.glBackend().createProgram());
        this.shaders = new HashSet<>(shaders.length);
        try {
            for (Shader shader : shaders) {
                this.attachShader(shader);
            }
            this.link();
        } catch (Throwable e) {
            this.free();
            throw e;
        }
    }

    protected Program(final int glId) {
        super(glId);
    }

    public static Program fromGlId(final int glId) {
        if (!ThinGL.glBackend().isProgram(glId)) {
            throw new IllegalArgumentException("Not a program object");
        }
        return fromGlIdUnsafe(glId);
    }

    public static Program fromGlIdUnsafe(final int glId) {
        return new Program(glId);
    }

    public void attachShader(final Shader shader) {
        this.getShaders(); // Ensure shaders set is initialized
        ThinGL.glBackend().attachShader(this.getGlId(), shader.getGlId());
        this.shaders.add(shader);
    }

    public void detachShader(final Shader shader) {
        this.getShaders(); // Ensure shaders set is initialized
        ThinGL.glBackend().detachShader(this.getGlId(), shader.getGlId());
        this.shaders.remove(shader);
    }

    public void link() {
        this.uniformLocationCache.clear();
        this.uniformBlockIndexCache.clear();
        this.shaderStorageBlockIndexCache.clear();
        ThinGL.glBackend().linkProgram(this.getGlId());
        final String linkLog = ThinGL.glBackend().getProgramInfoLog(this.getGlId());
        if (ThinGL.glBackend().getProgrami(this.getGlId(), GL20C.GL_LINK_STATUS) == GL11C.GL_FALSE) {
            throw new IllegalStateException("Error linking program: " + linkLog);
        } else if (!linkLog.isBlank()) {
            ThinGL.LOGGER.warn("Program link log: " + linkLog);
        }
    }

    public void validate() {
        ThinGL.glBackend().validateProgram(this.getGlId());
        final String validateLog = ThinGL.glBackend().getProgramInfoLog(this.getGlId());
        if (ThinGL.glBackend().getProgrami(this.getGlId(), GL20C.GL_VALIDATE_STATUS) == GL11C.GL_FALSE) {
            throw new IllegalStateException("Error validating program: " + validateLog);
        } else if (!validateLog.isBlank()) {
            ThinGL.LOGGER.warn("Program validate log: " + validateLog);
        }
    }

    public void setUniformBoolean(final String name, final boolean v) {
        ThinGL.glBackend().programUniform1i(this.getGlId(), this.getUniformLocation(name), v ? GL11C.GL_TRUE : GL11C.GL_FALSE);
    }

    public void setUniformInt(final String name, final int v) {
        ThinGL.glBackend().programUniform1i(this.getGlId(), this.getUniformLocation(name), v);
    }

    public void setUniformIntArray(final String name, final int... v) {
        ThinGL.glBackend().programUniform1iv(this.getGlId(), this.getUniformLocation(name), v);
    }

    public void setUniformFloat(final String name, final float v) {
        ThinGL.glBackend().programUniform1f(this.getGlId(), this.getUniformLocation(name), v);
    }

    public void setUniformVector2f(final String name, final float v1, final float v2) {
        ThinGL.glBackend().programUniform2f(this.getGlId(), this.getUniformLocation(name), v1, v2);
    }

    public void setUniformVector3f(final String name, final float v1, final float v2, final float v3) {
        ThinGL.glBackend().programUniform3f(this.getGlId(), this.getUniformLocation(name), v1, v2, v3);
    }

    public void setUniformVector4f(final String name, final float v1, final float v2, final float v3, final float v4) {
        ThinGL.glBackend().programUniform4f(this.getGlId(), this.getUniformLocation(name), v1, v2, v3, v4);
    }

    public void setUniformVector4f(final String name, final Color color) {
        this.setUniformVector4f(name, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
    }

    public void setUniformMatrix3f(final String name, final Matrix3f matrix) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            final Memory memory = MemoryAllocator.wrapMemory(memoryStack.nmalloc(Memory.MATRIX3F_SIZE), Memory.MATRIX3F_SIZE);
            memory.putMatrix3f(0, matrix);
            ThinGL.glBackend().programUniformMatrix3fv(this.getGlId(), this.getUniformLocation(name), 1, false, memory.getAddress());
        }
    }

    public void setUniformMatrix4f(final String name, final Matrix4f matrix) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            final Memory memory = MemoryAllocator.wrapMemory(memoryStack.nmalloc(Memory.MATRIX4F_SIZE), Memory.MATRIX4F_SIZE);
            memory.putMatrix4f(0, matrix);
            ThinGL.glBackend().programUniformMatrix4fv(this.getGlId(), this.getUniformLocation(name), 1, false, memory.getAddress());
        }
    }

    public void setUniformSampler(final String name, final Framebuffer framebuffer) {
        if (framebuffer != null) {
            if (framebuffer.getColorAttachment(0) instanceof ImageTexture texture) {
                this.setUniformSampler(name, texture);
            } else {
                throw new IllegalArgumentException("Framebuffer color attachment is not a texture");
            }
        } else {
            this.setUniformSampler(name, 0);
        }
    }

    public void setUniformSampler(final String name, final Texture texture) {
        if (texture != null) {
            this.setUniformSampler(name, texture.getGlId());
        } else {
            this.setUniformSampler(name, 0);
        }
    }

    public void setUniformSampler(final String name, final int textureId) {
        ThinGL.glBackend().bindTextureUnit(this.currentTextureUnit, textureId);
        ThinGL.glBackend().bindSampler(this.currentTextureUnit, 0);
        this.setUniformInt(name, this.currentTextureUnit++);
    }

    public void setUniformSamplerArray(final String name, final int... textureIds) {
        ThinGL.glBackend().bindTextures(this.currentTextureUnit, textureIds);
        ThinGL.glBackend().bindSamplers(this.currentTextureUnit, new int[textureIds.length]);
        final int[] textureUnits = new int[textureIds.length];
        for (int i = 0; i < textureIds.length; i++) {
            textureUnits[i] = this.currentTextureUnit + i;
        }
        this.setUniformIntArray(name, textureUnits);
        this.currentTextureUnit += textureIds.length;
    }

    public void setUniformImage(final String name, final Framebuffer framebuffer, final int access, final int format) {
        if (framebuffer.getColorAttachment(0) instanceof ImageTexture texture) {
            this.setUniformImage(name, texture, access, format);
        } else {
            throw new IllegalArgumentException("Framebuffer color attachment is not a texture");
        }
    }

    public void setUniformImage(final String name, final Texture texture, final int access, final int format) {
        this.setUniformImage(name, texture.getGlId(), access, format);
    }

    public void setUniformImage(final String name, final int textureId, final int access, final int format) {
        ThinGL.glBackend().bindImageTexture(this.currentImageUnit, textureId, 0, false, 0, access, format);
        this.setUniformInt(name, this.currentImageUnit++);
    }

    public void setUniformBuffer(final String name, final Buffer buffer) {
        ThinGL.glBackend().uniformBlockBinding(this.getGlId(), this.getUniformBlockIndex(name), this.currentUniformBufferIndex);
        if (buffer != null) {
            ThinGL.glBackend().bindBufferBase(GL31C.GL_UNIFORM_BUFFER, this.currentUniformBufferIndex++, buffer.getGlId());
        } else {
            ThinGL.glBackend().bindBufferBase(GL31C.GL_UNIFORM_BUFFER, this.currentUniformBufferIndex++, 0);
        }
    }

    public void setShaderStorageBuffer(final String name, final Buffer buffer) {
        ThinGL.glBackend().shaderStorageBlockBinding(this.getGlId(), this.getShaderStorageBlockIndex(name), this.currentShaderStorageBufferIndex);
        if (buffer != null) {
            ThinGL.glBackend().bindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, this.currentShaderStorageBufferIndex++, buffer.getGlId());
        } else {
            ThinGL.glBackend().bindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, this.currentShaderStorageBufferIndex++, 0);
        }
    }

    public void bind() {
        this.currentTextureUnit = 0;
        this.currentImageUnit = 0;
        this.currentUniformBufferIndex = 0;
        this.currentShaderStorageBufferIndex = 0;
        if (ThinGL.config().restoreProgramBinding()) {
            ThinGL.glStateStack().pushProgram();
        }
        ThinGL.glStateManager().setProgram(this.getGlId());
    }

    public void unbind() {
        this.currentTextureUnit = 0;
        this.currentImageUnit = 0;
        this.currentUniformBufferIndex = 0;
        this.currentShaderStorageBufferIndex = 0;
        if (ThinGL.config().restoreProgramBinding()) {
            ThinGL.glStateStack().popProgram();
        }
    }

    @Override
    protected void free0() {
        ThinGL.glBackend().deleteProgram(this.getGlId());
    }

    @Override
    protected void freeContainingObjects() {
        for (Shader shader : this.getShaders()) {
            shader.free();
        }
    }

    @Override
    public final int getGlType() {
        return GL43C.GL_PROGRAM;
    }

    public Shader getShader(final Shader.Type type) {
        for (Shader shader : this.getShaders()) {
            if (shader.getType() == type.getGlType()) {
                return shader;
            }
        }

        return null;
    }

    public Set<Shader> getShaders() {
        if (this.shaders == null) {
            final int shaderCount = ThinGL.glBackend().getProgrami(this.getGlId(), GL20C.GL_ATTACHED_SHADERS);
            this.shaders = new HashSet<>(shaderCount);
            final int[] shaderGlIds = new int[shaderCount];
            ThinGL.glBackend().getAttachedShaders(this.getGlId(), null, shaderGlIds);
            for (int shaderGlId : shaderGlIds) {
                this.shaders.add(Shader.fromGlId(shaderGlId));
            }
        }
        return Collections.unmodifiableSet(this.shaders);
    }

    private int getUniformLocation(final String name) {
        return this.uniformLocationCache.computeIfAbsent(name, this::queryUniformLocation);
    }

    private int queryUniformLocation(final String name) {
        return ThinGL.glBackend().getUniformLocation(this.getGlId(), name);
    }

    public int getUniformBlockIndex(final String name) {
        return this.uniformBlockIndexCache.computeIfAbsent(name, this::queryUniformBlockIndex);
    }

    private int queryUniformBlockIndex(final String name) {
        return ThinGL.glBackend().getProgramResourceIndex(this.getGlId(), GL43C.GL_UNIFORM_BLOCK, name);
    }

    public int getShaderStorageBlockIndex(final String name) {
        return this.shaderStorageBlockIndexCache.computeIfAbsent(name, this::queryShaderStorageBlockIndex);
    }

    private int queryShaderStorageBlockIndex(final String name) {
        return ThinGL.glBackend().getProgramResourceIndex(this.getGlId(), GL43C.GL_SHADER_STORAGE_BLOCK, name);
    }

}
