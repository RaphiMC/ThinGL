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

package net.raphimc.thingl.resource.program;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.GLResource;
import net.raphimc.thingl.resource.buffer.AbstractBuffer;
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import net.raphimc.thingl.resource.shader.Shader;
import net.raphimc.thingl.resource.texture.AbstractTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Program extends GLResource {

    private final Set<Shader> shaders = new HashSet<>();
    private final Object2IntMap<String> uniformLocationCache = new Object2IntOpenHashMap<>();
    private final Object2IntMap<String> uniformBlockIndexCache = new Object2IntOpenHashMap<>();
    private final Object2IntMap<String> shaderStorageBlockIndexCache = new Object2IntOpenHashMap<>();

    private int currentTextureUnit;
    private int currentUniformBlockIndex;
    private int currentShaderStorageBufferIndex;

    public Program(final Shader... shaders) {
        super(GL43C.GL_PROGRAM, GL20C.glCreateProgram());
        try {
            for (Shader shader : shaders) {
                this.attachShader(shader);
            }
            this.linkAndValidate();
        } catch (Throwable e) {
            this.delete();
            throw e;
        }
    }

    protected Program(final int glId) {
        super(GL43C.GL_PROGRAM, glId);
        this.refreshCachedData();
    }

    public static Program fromGlId(final int glId) {
        if (!GL20C.glIsProgram(glId)) {
            throw new IllegalArgumentException("Invalid OpenGL resource");
        }
        return new Program(glId);
    }

    @Override
    public void refreshCachedData() {
        this.shaders.clear();
        final int count = GL20C.glGetProgrami(this.getGlId(), GL20C.GL_ATTACHED_SHADERS);
        final int[] shaderIds = new int[count];
        GL20C.glGetAttachedShaders(this.getGlId(), null, shaderIds);
        for (int shaderId : shaderIds) {
            this.shaders.add(Shader.fromGlId(shaderId));
        }
        this.uniformLocationCache.clear();
        this.shaderStorageBlockIndexCache.clear();
    }

    public void linkAndValidate() {
        GL20C.glLinkProgram(this.getGlId());
        final String linkLog = GL20C.glGetProgramInfoLog(this.getGlId());
        if (GL20C.glGetProgrami(this.getGlId(), GL20C.GL_LINK_STATUS) == GL11C.GL_FALSE) {
            throw new IllegalStateException("Error linking program: " + linkLog);
        } else if (!linkLog.isBlank()) {
            ThinGL.LOGGER.warn("Program linkLog: " + linkLog);
        }

        GL20C.glValidateProgram(this.getGlId());
        final String validateLog = GL20C.glGetProgramInfoLog(this.getGlId());
        if (GL20C.glGetProgrami(this.getGlId(), GL20C.GL_VALIDATE_STATUS) == GL11C.GL_FALSE) {
            throw new IllegalStateException("Error validating program: " + validateLog);
        } else if (!validateLog.isBlank()) {
            ThinGL.LOGGER.warn("Program validateLog: " + validateLog);
        }
    }

    public void setUniform(final String name, final boolean v) {
        GL41C.glProgramUniform1i(this.getGlId(), this.getUniformLocation(name), v ? GL11C.GL_TRUE : GL11C.GL_FALSE);
    }

    public void setUniform(final String name, final int v) {
        GL41C.glProgramUniform1i(this.getGlId(), this.getUniformLocation(name), v);
    }

    public void setUniform(final String name, final int... v) {
        GL41C.glProgramUniform1iv(this.getGlId(), this.getUniformLocation(name), v);
    }

    public void setUniform(final String name, final float v) {
        GL41C.glProgramUniform1f(this.getGlId(), this.getUniformLocation(name), v);
    }

    public void setUniform(final String name, final float v1, final float v2) {
        GL41C.glProgramUniform2f(this.getGlId(), this.getUniformLocation(name), v1, v2);
    }

    public void setUniform(final String name, final float v1, final float v2, final float v3) {
        GL41C.glProgramUniform3f(this.getGlId(), this.getUniformLocation(name), v1, v2, v3);
    }

    public void setUniform(final String name, final float v1, final float v2, final float v3, final float v4) {
        GL41C.glProgramUniform4f(this.getGlId(), this.getUniformLocation(name), v1, v2, v3, v4);
    }

    public void setUniform(final String name, final Color color) {
        this.setUniform(name, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
    }

    public void setUniform(final String name, final Matrix3f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final FloatBuffer buffer = stack.mallocFloat(3 * 3);
            matrix.get(buffer);
            GL41C.glProgramUniformMatrix3fv(this.getGlId(), this.getUniformLocation(name), false, buffer);
        }
    }

    public void setUniform(final String name, final Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final FloatBuffer buffer = stack.mallocFloat(4 * 4);
            matrix.get(buffer);
            GL41C.glProgramUniformMatrix4fv(this.getGlId(), this.getUniformLocation(name), false, buffer);
        }
    }

    public void setUniformTexture(final String name, final Framebuffer framebuffer) {
        if (framebuffer.getColorAttachment() instanceof AbstractTexture texture) {
            this.setUniformTexture(name, texture);
        } else {
            throw new IllegalArgumentException("Framebuffer color attachment is not a texture");
        }
    }

    public void setUniformTexture(final String name, final AbstractTexture texture) {
        this.setUniformTexture(name, texture.getGlId());
    }

    public void setUniformTexture(final String name, final int textureId) {
        GL45C.glBindTextureUnit(this.currentTextureUnit, textureId);
        this.setUniform(name, this.currentTextureUnit++);
    }

    public void setUniformTextureArray(final String name, final int... textureIds) {
        final int[] textureUnits = new int[textureIds.length];
        for (int i = 0; i < textureIds.length; i++) {
            GL45C.glBindTextureUnit(this.currentTextureUnit + i, textureIds[i]);
            textureUnits[i] = this.currentTextureUnit + i;
        }
        this.setUniform(name, textureUnits);
    }

    public void setUniformBuffer(final String name, final AbstractBuffer buffer) {
        GL31C.glUniformBlockBinding(this.getGlId(), this.getUniformBlockIndex(name), this.currentUniformBlockIndex);
        GL30C.glBindBufferBase(GL31C.GL_UNIFORM_BUFFER, this.currentUniformBlockIndex++, buffer.getGlId());
    }

    public void setShaderStorageBuffer(final String name, final AbstractBuffer buffer) {
        GL43C.glShaderStorageBlockBinding(this.getGlId(), this.getShaderStorageBlockIndex(name), this.currentShaderStorageBufferIndex);
        GL30C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, this.currentShaderStorageBufferIndex++, buffer.getGlId());
    }

    public void bind() {
        this.currentTextureUnit = 0;
        this.currentUniformBlockIndex = 0;
        this.currentShaderStorageBufferIndex = 0;
        GL20C.glUseProgram(this.getGlId());
    }

    public void unbind() {
        GL20C.glUseProgram(0);
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0);
        ThinGL.onProgramUnbind();
    }

    @Override
    protected void delete0() {
        GL20C.glDeleteProgram(this.getGlId());
    }

    public Shader getShader(final Shader.Type type) {
        for (Shader shader : this.shaders) {
            if (shader.getType() == type.getGlType()) {
                return shader;
            }
        }

        return null;
    }

    public Set<Shader> getShaders() {
        return Collections.unmodifiableSet(this.shaders);
    }

    public void attachShader(final Shader shader) {
        GL20C.glAttachShader(this.getGlId(), shader.getGlId());
        this.shaders.add(shader);
    }

    public void detachShader(final Shader shader) {
        GL20C.glDetachShader(this.getGlId(), shader.getGlId());
        this.shaders.remove(shader);
    }

    private int getUniformLocation(final String name) {
        return this.uniformLocationCache.computeIfAbsent(name, this::queryUniformLocation);
    }

    private int queryUniformLocation(final String name) {
        return GL20C.glGetUniformLocation(this.getGlId(), name);
    }

    public int getUniformBlockIndex(final String name) {
        return this.uniformBlockIndexCache.computeIfAbsent(name, this::queryUniformBlockIndex);
    }

    private int queryUniformBlockIndex(final String name) {
        return GL43C.glGetProgramResourceIndex(this.getGlId(), GL43C.GL_UNIFORM_BLOCK, name);
    }

    public int getShaderStorageBlockIndex(final String name) {
        return this.shaderStorageBlockIndexCache.computeIfAbsent(name, this::queryShaderStorageBlockIndex);
    }

    private int queryShaderStorageBlockIndex(final String name) {
        return GL43C.glGetProgramResourceIndex(this.getGlId(), GL43C.GL_SHADER_STORAGE_BLOCK, name);
    }

}
