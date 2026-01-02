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
package net.raphimc.thingl.implementation.gl.impl;

import io.github.ocelot.glslprocessor.api.GlslParser;
import io.github.ocelot.glslprocessor.api.grammar.*;
import io.github.ocelot.glslprocessor.api.node.GlslNode;
import io.github.ocelot.glslprocessor.api.node.GlslNodeList;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.api.node.branch.GlslReturnNode;
import io.github.ocelot.glslprocessor.api.node.constant.GlslIntConstantNode;
import io.github.ocelot.glslprocessor.api.node.expression.GlslAssignmentNode;
import io.github.ocelot.glslprocessor.api.node.expression.GlslOperationNode;
import io.github.ocelot.glslprocessor.api.node.function.GlslFunctionNode;
import io.github.ocelot.glslprocessor.api.node.function.GlslInvokeFunctionNode;
import io.github.ocelot.glslprocessor.api.node.variable.*;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.gl.GLBackend;
import net.raphimc.thingl.rendering.command.impl.DrawArraysCommand;
import net.raphimc.thingl.rendering.command.impl.DrawElementsCommand;
import net.raphimc.thingl.resource.image.Image;
import net.raphimc.thingl.util.glsl.GlslNodeMutator;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GL41Backend implements GLBackend {

    private static final int SHADER_STORAGE_BUFFER_TEXTURE_UNIT_OFFSET = 16;

    private static int getTextureQuery(final int target) {
        return switch (target) {
            case GL11C.GL_TEXTURE_1D -> GL11C.GL_TEXTURE_BINDING_1D;
            case GL30C.GL_TEXTURE_1D_ARRAY -> GL30C.GL_TEXTURE_BINDING_1D_ARRAY;
            case GL11C.GL_TEXTURE_2D -> GL11C.GL_TEXTURE_BINDING_2D;
            case GL30C.GL_TEXTURE_2D_ARRAY -> GL30C.GL_TEXTURE_BINDING_2D_ARRAY;
            case GL32C.GL_TEXTURE_2D_MULTISAMPLE -> GL32C.GL_TEXTURE_BINDING_2D_MULTISAMPLE;
            case GL32C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY -> GL32C.GL_TEXTURE_BINDING_2D_MULTISAMPLE_ARRAY;
            case GL12C.GL_TEXTURE_3D -> GL12C.GL_TEXTURE_BINDING_3D;
            case GL13C.GL_TEXTURE_CUBE_MAP -> GL13C.GL_TEXTURE_BINDING_CUBE_MAP;
            case GL40C.GL_TEXTURE_CUBE_MAP_ARRAY -> GL40C.GL_TEXTURE_BINDING_CUBE_MAP_ARRAY;
            case GL31C.GL_TEXTURE_BUFFER -> GL31C.GL_TEXTURE_BINDING_BUFFER;
            default -> throw new IllegalArgumentException("Unsupported texture target: " + target);
        };
    }

    private static int getTextureFormat(final int internalFormat) {
        return switch (internalFormat) {
            case GL11C.GL_RGBA8 -> GL11C.GL_RGBA;
            case GL11C.GL_RGB8 -> GL11C.GL_RGB;
            case GL30C.GL_RG8 -> GL30C.GL_RG;
            case GL30C.GL_R8 -> GL11C.GL_RED;
            case GL30C.GL_DEPTH_COMPONENT32F, GL14C.GL_DEPTH_COMPONENT32, GL14C.GL_DEPTH_COMPONENT24, GL14C.GL_DEPTH_COMPONENT16 -> GL11C.GL_DEPTH_COMPONENT;
            case GL30C.GL_DEPTH32F_STENCIL8, GL30C.GL_DEPTH24_STENCIL8 -> GL30C.GL_DEPTH_STENCIL;
            default -> throw new IllegalArgumentException("Unknown texture internal format: " + internalFormat);
        };
    }

    private static int getTextureType(final int internalFormat) {
        return switch (internalFormat) {
            case GL11C.GL_RGBA8, GL11C.GL_RGB8, GL30C.GL_RG8, GL30C.GL_R8 -> GL11C.GL_UNSIGNED_BYTE;
            case GL30C.GL_DEPTH_COMPONENT32F -> GL11C.GL_FLOAT;
            case GL14C.GL_DEPTH_COMPONENT32, GL14C.GL_DEPTH_COMPONENT24 -> GL11C.GL_UNSIGNED_INT;
            case GL14C.GL_DEPTH_COMPONENT16 -> GL11C.GL_UNSIGNED_SHORT;
            case GL30C.GL_DEPTH32F_STENCIL8 -> GL30C.GL_FLOAT_32_UNSIGNED_INT_24_8_REV;
            case GL30C.GL_DEPTH24_STENCIL8 -> GL30C.GL_UNSIGNED_INT_24_8;
            default -> throw new IllegalArgumentException("Unknown texture internal format: " + internalFormat);
        };
    }

    private static int getDebugLabelObjectTypeEXT(final int identifier) {
        return switch (identifier) {
            case GL43C.GL_BUFFER -> EXTDebugLabel.GL_BUFFER_OBJECT_EXT;
            case GL43C.GL_SHADER -> EXTDebugLabel.GL_SHADER_OBJECT_EXT;
            case GL43C.GL_PROGRAM -> EXTDebugLabel.GL_PROGRAM_OBJECT_EXT;
            case GL11C.GL_VERTEX_ARRAY -> EXTDebugLabel.GL_VERTEX_ARRAY_OBJECT_EXT;
            case GL43C.GL_QUERY -> EXTDebugLabel.GL_QUERY_OBJECT_EXT;
            case GL43C.GL_PROGRAM_PIPELINE -> EXTDebugLabel.GL_PROGRAM_PIPELINE_OBJECT_EXT;
            default -> identifier;
        };
    }

    private final GLCapabilities capabilities = GL.getCapabilities();
    private final Int2IntMap textureTargets = new Int2IntOpenHashMap();
    private final Int2IntMap queryTargets = new Int2IntOpenHashMap();
    private final Int2ObjectMap<VertexArrayObject> vertexArrayObjects = new Int2ObjectOpenHashMap<>();

    private final boolean supportsShaderStorageBuffers = this.capabilities.OpenGL43;
    private final Int2IntMap shaderStorageBufferTextures = new Int2IntOpenHashMap();

    @Override
    public void blendFunc(final int sfactor, final int dfactor) {
        GL11C.glBlendFunc(sfactor, dfactor);
    }

    @Override
    public void colorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha) {
        GL11C.glColorMask(red, green, blue, alpha);
    }

    @Override
    public void cullFace(final int mode) {
        GL11C.glCullFace(mode);
    }

    @Override
    public void deleteTextures(final int texture) {
        this.textureTargets.remove(texture);
        GL11C.glDeleteTextures(texture);
    }

    @Override
    public void depthFunc(final int func) {
        GL11C.glDepthFunc(func);
    }

    @Override
    public void depthMask(final boolean flag) {
        GL11C.glDepthMask(flag);
    }

    @Override
    public void disable(final int cap) {
        GL11C.glDisable(cap);
    }

    @Override
    public void drawArrays(final int mode, final int first, final int count) {
        GL11C.glDrawArrays(mode, first, count);
    }

    @Override
    public void drawElements(final int mode, final int count, final int type, final long indices) {
        GL11C.glDrawElements(mode, count, type, indices);
    }

    @Override
    public void enable(final int cap) {
        GL11C.glEnable(cap);
    }

    @Override
    public void frontFace(final int dir) {
        GL11C.glFrontFace(dir);
    }

    @Override
    public boolean getBoolean(final int pname) {
        return GL11C.glGetBoolean(pname);
    }

    @Override
    public float getFloat(final int pname) {
        return GL11C.glGetFloat(pname);
    }

    @Override
    public int getInteger(final int pname) {
        return GL11C.glGetInteger(pname);
    }

    @Override
    public void getIntegerv(final int pname, final int[] params) {
        GL11C.glGetIntegerv(pname, params);
    }

    @Override
    public String getString(final int name) {
        return GL11C.glGetString(name);
    }

    @Override
    public boolean isEnabled(final int cap) {
        return GL11C.glIsEnabled(cap);
    }

    @Override
    public boolean isTexture(final int texture) {
        return GL11C.glIsTexture(texture);
    }

    @Override
    public void logicOp(final int opcode) {
        GL11C.glLogicOp(opcode);
    }

    @Override
    public void pixelStorei(final int pname, final int param) {
        GL11C.glPixelStorei(pname, param);
    }

    @Override
    public void polygonOffset(final float factor, final float units) {
        GL11C.glPolygonOffset(factor, units);
    }

    @Override
    public void scissor(final int x, final int y, final int width, final int height) {
        GL11C.glScissor(x, y, width, height);
    }

    @Override
    public void stencilFunc(final int func, final int ref, final int mask) {
        GL11C.glStencilFunc(func, ref, mask);
    }

    @Override
    public void stencilMask(final int mask) {
        GL11C.glStencilMask(mask);
    }

    @Override
    public void stencilOp(final int sfail, final int dpfail, final int dppass) {
        GL11C.glStencilOp(sfail, dpfail, dppass);
    }

    @Override
    public void viewport(final int x, final int y, final int width, final int height) {
        GL11C.glViewport(x, y, width, height);
    }

    @Override
    public void blendEquation(final int mode) {
        GL14C.glBlendEquation(mode);
    }

    @Override
    public void blendFuncSeparate(final int srcRGB, final int dstRGB, final int srcAlpha, final int dstAlpha) {
        GL14C.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    @Override
    public void beginQuery(final int target, final int id) {
        GL15C.glBeginQuery(target, id);
    }

    @Override
    public void bindBuffer(final int target, final int buffer) {
        GL15C.glBindBuffer(target, buffer);
    }

    @Override
    public void deleteBuffers(final int buffer) {
        GL15C.glDeleteBuffers(buffer);
    }

    @Override
    public void deleteQueries(final int id) {
        this.queryTargets.remove(id);
        GL15C.glDeleteQueries(id);
    }

    @Override
    public void endQuery(final int target) {
        GL15C.glEndQuery(target);
    }

    @Override
    public int getQueryObjecti(final int id, final int pname) {
        if (pname == GL45C.GL_QUERY_TARGET && !this.capabilities.OpenGL45 && !this.capabilities.GL_ARB_direct_state_access) {
            return this.queryTargets.get(id);
        }

        return GL15C.glGetQueryObjecti(id, pname);
    }

    @Override
    public boolean isBuffer(final int buffer) {
        return GL15C.glIsBuffer(buffer);
    }

    @Override
    public boolean isQuery(final int id) {
        return GL15C.glIsQuery(id);
    }

    @Override
    public void attachShader(final int program, final int shader) {
        GL20C.glAttachShader(program, shader);
    }

    @Override
    public void compileShader(final int shader) {
        GL20C.glCompileShader(shader);
    }

    @Override
    public int createProgram() {
        return GL20C.glCreateProgram();
    }

    @Override
    public int createShader(final int type) {
        return GL20C.glCreateShader(type);
    }

    @Override
    public void deleteProgram(final int program) {
        GL20C.glDeleteProgram(program);
    }

    @Override
    public void deleteShader(final int shader) {
        GL20C.glDeleteShader(shader);
    }

    @Override
    public void detachShader(final int program, final int shader) {
        GL20C.glDetachShader(program, shader);
    }

    @Override
    public void getAttachedShaders(final int program, final int[] count, final int[] shaders) {
        GL20C.glGetAttachedShaders(program, count, shaders);
    }

    @Override
    public String getProgramInfoLog(final int program) {
        return GL20C.glGetProgramInfoLog(program);
    }

    @Override
    public int getProgrami(final int program, final int pname) {
        return GL20C.glGetProgrami(program, pname);
    }

    @Override
    public String getShaderInfoLog(final int shader) {
        return GL20C.glGetShaderInfoLog(shader);
    }

    @Override
    public String getShaderSource(final int shader) {
        return GL20C.glGetShaderSource(shader);
    }

    @Override
    public int getShaderi(final int shader, final int pname) {
        return GL20C.glGetShaderi(shader, pname);
    }

    @Override
    public int getUniformLocation(final int program, final CharSequence name) {
        return GL20C.glGetUniformLocation(program, name);
    }

    @Override
    public boolean isProgram(final int program) {
        return GL20C.glIsProgram(program);
    }

    @Override
    public boolean isShader(final int shader) {
        return GL20C.glIsShader(shader);
    }

    @Override
    public void linkProgram(final int program) {
        GL20C.glLinkProgram(program);
    }

    @Override
    public void shaderSource(final int shader, CharSequence string) {
        try {
            final GlslTree glslTree = GlslParser.parse(string.toString());

            boolean modified = false;
            if (!this.supportsShaderStorageBuffers) {
                if (ShaderStorageBufferShaderRewriter.modify(glslTree)) {
                    modified = true;
                }
            }

            final String shadingLanguageVersion = this.getString(GL20C.GL_SHADING_LANGUAGE_VERSION);
            final int driverVersion = Integer.parseInt(shadingLanguageVersion.split(" ")[0].replace(".", ""));
            if (glslTree.getVersionStatement().getVersion() > driverVersion) {
                glslTree.getVersionStatement().setVersion(driverVersion);
                modified = true;
            }

            if (modified) {
                string = glslTree.toSourceString();
            }
        } catch (Throwable e) {
            ThinGL.LOGGER.warn("Failed to rewrite shader source, using original source", e);
        }

        GL20C.glShaderSource(shader, string);
    }

    @Override
    public void stencilMaskSeparate(final int face, final int mask) {
        GL20C.glStencilMaskSeparate(face, mask);
    }

    @Override
    public void useProgram(final int program) {
        GL20C.glUseProgram(program);
    }

    @Override
    public void validateProgram(final int program) {
        GL20C.glValidateProgram(program);
    }

    @Override
    public void bindBufferBase(final int target, final int index, final int buffer) {
        if (target == GL43C.GL_SHADER_STORAGE_BUFFER && !this.supportsShaderStorageBuffers) {
            final int bufferTexture = this.shaderStorageBufferTextures.computeIfAbsent(index, k -> this.createTextures(GL31C.GL_TEXTURE_BUFFER));
            this.textureBuffer(bufferTexture, GL30C.GL_R32F, buffer);
            this.bindTextureUnit(SHADER_STORAGE_BUFFER_TEXTURE_UNIT_OFFSET + index, bufferTexture);
            GL33C.glBindSampler(SHADER_STORAGE_BUFFER_TEXTURE_UNIT_OFFSET + index, 0);
            return;
        }

        GL30C.glBindBufferBase(target, index, buffer);
    }

    @Override
    public void bindFramebuffer(final int target, final int framebuffer) {
        GL30C.glBindFramebuffer(target, framebuffer);
    }

    @Override
    public void bindVertexArray(final int array) {
        GL30C.glBindVertexArray(array);
    }

    @Override
    public void clearBufferiv(final int buffer, final int drawbuffer, final int[] value) {
        GL30C.glClearBufferiv(buffer, drawbuffer, value);
    }

    @Override
    public void deleteFramebuffers(final int framebuffer) {
        GL30C.glDeleteFramebuffers(framebuffer);
    }

    @Override
    public void deleteRenderbuffers(final int renderbuffer) {
        GL30C.glDeleteRenderbuffers(renderbuffer);
    }

    @Override
    public void deleteVertexArrays(final int array) {
        this.vertexArrayObjects.remove(array);
        GL30C.glDeleteVertexArrays(array);
    }

    @Override
    public boolean isFramebuffer(final int framebuffer) {
        return GL30C.glIsFramebuffer(framebuffer);
    }

    @Override
    public boolean isRenderbuffer(final int renderbuffer) {
        return GL30C.glIsRenderbuffer(renderbuffer);
    }

    @Override
    public boolean isVertexArray(final int array) {
        return GL30C.glIsVertexArray(array);
    }

    @Override
    public void uniformBlockBinding(final int program, final int uniformBlockIndex, final int uniformBlockBinding) {
        GL31C.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    }

    @Override
    public int clientWaitSync(final long sync, final int flags, final long timeout) {
        return GL32C.glClientWaitSync(sync, flags, timeout);
    }

    @Override
    public void deleteSync(final long sync) {
        GL32C.glDeleteSync(sync);
    }

    @Override
    public long fenceSync(final int condition, final int flags) {
        return GL32C.glFenceSync(condition, flags);
    }

    @Override
    public int getSynci(final long sync, final int pname) {
        return GL32C.glGetSynci(sync, pname, null);
    }

    @Override
    public boolean isSync(final long sync) {
        return GL32C.glIsSync(sync);
    }

    @Override
    public void waitSync(final long sync, final int flags, final long timeout) {
        GL32C.glWaitSync(sync, flags, timeout);
    }

    @Override
    public void bindSampler(final int unit, final int sampler) {
        GL33C.glBindSampler(unit, sampler);
    }

    @Override
    public long getQueryObjecti64(final int id, final int pname) {
        return GL33C.glGetQueryObjecti64(id, pname);
    }

    @Override
    public void drawArraysIndirect(final int mode, final long indirect) {
        GL40C.glDrawArraysIndirect(mode, indirect);
    }

    @Override
    public void drawElementsIndirect(final int mode, final int type, final long indirect) {
        GL40C.glDrawElementsIndirect(mode, type, indirect);
    }

    @Override
    public void programUniform1f(final int program, final int location, final float v0) {
        GL41C.glProgramUniform1f(program, location, v0);
    }

    @Override
    public void programUniform1i(final int program, final int location, final int v0) {
        GL41C.glProgramUniform1i(program, location, v0);
    }

    @Override
    public void programUniform1iv(final int program, final int location, final int[] value) {
        GL41C.glProgramUniform1iv(program, location, value);
    }

    @Override
    public void programUniform2f(final int program, final int location, final float v0, final float v1) {
        GL41C.glProgramUniform2f(program, location, v0, v1);
    }

    @Override
    public void programUniform3f(final int program, final int location, final float v0, final float v1, final float v2) {
        GL41C.glProgramUniform3f(program, location, v0, v1, v2);
    }

    @Override
    public void programUniform4f(final int program, final int location, final float v0, final float v1, final float v2, final float v3) {
        GL41C.glProgramUniform4f(program, location, v0, v1, v2, v3);
    }

    @Override
    public void programUniformMatrix3fv(final int program, final int location, final int count, final boolean transpose, final long value) {
        GL41C.nglProgramUniformMatrix3fv(program, location, count, transpose, value);
    }

    @Override
    public void programUniformMatrix4fv(final int program, final int location, final int count, final boolean transpose, final long value) {
        GL41C.nglProgramUniformMatrix4fv(program, location, count, transpose, value);
    }

    @Override
    public void bindImageTexture(final int unit, final int texture, final int level, final boolean layered, final int layer, final int access, final int format) {
        if (this.capabilities.glBindImageTexture != 0L) {
            GL42C.glBindImageTexture(unit, texture, level, layered, layer, access, format);
        } else if (this.capabilities.glBindImageTextureEXT != 0L) {
            EXTShaderImageLoadStore.glBindImageTextureEXT(unit, texture, level, layered, layer, access, format);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void drawArraysInstancedBaseInstance(final int mode, final int first, final int count, final int primcount, final int baseinstance) {
        if (this.capabilities.glDrawArraysInstancedBaseInstance != 0L) {
            GL42C.glDrawArraysInstancedBaseInstance(mode, first, count, primcount, baseinstance);
        } else {
            if (baseinstance == 0) {
                GL31C.glDrawArraysInstanced(mode, first, count, primcount);
            } else {
                throw new UnsupportedOperationException("Non-zero base instance is not supported");
            }
        }
    }

    @Override
    public void drawElementsInstancedBaseVertexBaseInstance(final int mode, final int count, final int type, final long indices, final int primcount, final int basevertex, final int baseinstance) {
        if (this.capabilities.glDrawElementsInstancedBaseVertexBaseInstance != 0L) {
            GL42C.glDrawElementsInstancedBaseVertexBaseInstance(mode, count, type, indices, primcount, basevertex, baseinstance);
        } else {
            if (baseinstance == 0) {
                GL32C.glDrawElementsInstancedBaseVertex(mode, count, type, indices, primcount, basevertex);
            } else {
                throw new UnsupportedOperationException("Non-zero base instance is not supported");
            }
        }
    }

    @Override
    public void copyImageSubData(final int srcName, final int srcTarget, final int srcLevel, final int srcX, final int srcY, final int srcZ, final int dstName, final int dstTarget, final int dstLevel, final int dstX, final int dstY, final int dstZ, final int srcWidth, final int srcHeight, final int srcDepth) {
        if (this.capabilities.glCopyImageSubData != 0L) {
            GL43C.glCopyImageSubData(srcName, srcTarget, srcLevel, srcX, srcY, srcZ, dstName, dstTarget, dstLevel, dstX, dstY, dstZ, srcWidth, srcHeight, srcDepth);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String getObjectLabel(final int identifier, final int name) {
        if (this.capabilities.glGetObjectLabel != 0L) {
            return GL43C.glGetObjectLabel(identifier, name);
        } else if (this.capabilities.glGetObjectLabelEXT != 0L) {
            return EXTDebugLabel.glGetObjectLabelEXT(getDebugLabelObjectTypeEXT(identifier), name, 255);
        } else {
            return ""; // Unsupported
        }
    }

    @Override
    public int getProgramResourceIndex(final int program, final int programInterface, final CharSequence name) {
        if (programInterface == GL43C.GL_SHADER_STORAGE_BLOCK && !this.supportsShaderStorageBuffers) {
            return GL20C.glGetUniformLocation(program, name);
        }

        if (this.capabilities.glGetProgramResourceIndex != 0L) {
            return GL43C.glGetProgramResourceIndex(program, programInterface, name);
        } else {
            return switch (programInterface) {
                case GL43C.GL_UNIFORM -> {
                    final int uniformCount = GL20C.glGetProgrami(program, GL20C.GL_ACTIVE_UNIFORMS);
                    for (int i = 0; i < uniformCount; i++) {
                        final String uniformName = GL31C.glGetActiveUniformName(program, i);
                        if (uniformName.contentEquals(name)) {
                            yield i;
                        }
                    }
                    yield GL31C.GL_INVALID_INDEX;
                }
                case GL43C.GL_UNIFORM_BLOCK -> {
                    final int uniformBlockCount = GL20C.glGetProgrami(program, GL31C.GL_ACTIVE_UNIFORM_BLOCKS);
                    for (int i = 0; i < uniformBlockCount; i++) {
                        final String uniformBlockName = GL31C.glGetActiveUniformBlockName(program, i);
                        if (uniformBlockName.contentEquals(name)) {
                            yield i;
                        }
                    }
                    yield GL31C.GL_INVALID_INDEX;
                }
                default -> throw new UnsupportedOperationException("Program interface " + programInterface + " is not supported");
            };
        }
    }

    @Override
    public void multiDrawArraysIndirect(final int mode, long indirect, final int drawcount, int stride) {
        if (this.capabilities.glMultiDrawArraysIndirect != 0L) {
            GL43C.glMultiDrawArraysIndirect(mode, indirect, drawcount, stride);
        } else {
            if (stride == 0) {
                stride = DrawArraysCommand.BYTES;
            }
            for (int i = 0; i < drawcount; i++) {
                GL40C.glDrawArraysIndirect(mode, indirect);
                indirect += stride;
            }
        }
    }

    @Override
    public void multiDrawElementsIndirect(final int mode, final int type, long indirect, final int drawcount, int stride) {
        if (this.capabilities.glMultiDrawElementsIndirect != 0L) {
            GL43C.glMultiDrawElementsIndirect(mode, type, indirect, drawcount, stride);
        } else {
            if (stride == 0) {
                stride = DrawElementsCommand.BYTES;
            }
            for (int i = 0; i < drawcount; i++) {
                GL40C.glDrawElementsIndirect(mode, type, indirect);
                indirect += stride;
            }
        }
    }

    @Override
    public void objectLabel(final int identifier, final int name, final CharSequence label) {
        if (this.capabilities.glObjectLabel != 0L) {
            GL43C.glObjectLabel(identifier, name, label);
        } else if (this.capabilities.glLabelObjectEXT != 0L) {
            EXTDebugLabel.glLabelObjectEXT(getDebugLabelObjectTypeEXT(identifier), name, label);
        } else {
            // Unsupported
        }
    }

    @Override
    public void shaderStorageBlockBinding(final int program, final int storageBlockIndex, final int storageBlockBinding) {
        if (this.capabilities.glShaderStorageBlockBinding != 0L && this.supportsShaderStorageBuffers) {
            GL43C.glShaderStorageBlockBinding(program, storageBlockIndex, storageBlockBinding);
        } else {
            GL41C.glProgramUniform1i(program, storageBlockIndex, SHADER_STORAGE_BUFFER_TEXTURE_UNIT_OFFSET + storageBlockBinding);
        }
    }

    @Override
    public void bindSamplers(final int first, final int[] samplers) {
        if (this.capabilities.glBindSamplers != 0L) {
            GL44C.glBindSamplers(first, samplers);
        } else {
            for (int i = 0; i < samplers.length; i++) {
                GL33C.glBindSampler(first + i, samplers[i]);
            }
        }
    }

    @Override
    public void bindTextures(final int first, final int[] textures) {
        if (this.capabilities.glBindTextures != 0L) {
            GL44C.glBindTextures(first, textures);
        } else {
            for (int i = 0; i < textures.length; i++) {
                this.bindTextureUnit(first + i, textures[i]);
            }
        }
    }

    @Override
    public void clearTexImage(final int texture, final int level, final int format, final int type, final float[] data) {
        if (this.capabilities.glClearTexImage != 0L) {
            GL44C.glClearTexImage(texture, level, format, type, data);
        } else {
            final int textureWidth = this.getTextureLevelParameteri(texture, level, GL11C.GL_TEXTURE_WIDTH);
            final int textureHeight = this.getTextureLevelParameteri(texture, level, GL11C.GL_TEXTURE_HEIGHT);
            final int textureDepth = this.getTextureLevelParameteri(texture, level, GL12C.GL_TEXTURE_DEPTH);
            this.clearTexSubImage(texture, level, 0, 0, 0, textureWidth, textureHeight, textureDepth, format, type, data);
        }
    }

    @Override
    public void clearTexSubImage(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final float[] data) {
        if (this.capabilities.glClearTexSubImage != 0L) {
            GL44C.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
        } else {
            ThinGL.glStateStack().pushPixelStore();
            ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_ALIGNMENT, 1);
            ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_SKIP_PIXELS, 0);
            ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_SKIP_ROWS, 0);
            ThinGL.glStateStack().pixelStore(GL11C.GL_UNPACK_ROW_LENGTH, 0);
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            try {
                if (data != null) {
                    throw new UnsupportedOperationException("Clearing texture with user data is not supported");
                } else {
                    final Image image = new Image(width, height, depth, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE);
                    try {
                        image.getPixels().clear();
                        switch (target) {
                            case GL11C.GL_TEXTURE_1D -> {
                                if (height == 1 && depth == 1) {
                                    this.textureSubImage1D(texture, level, xoffset, width, image.getPixelFormat(), image.getPixelDataType(), image.getPixels().getAddress());
                                } else {
                                    throw new IllegalArgumentException("Height and depth must be 1 for 1D textures");
                                }
                            }
                            case GL30C.GL_TEXTURE_1D_ARRAY, GL11C.GL_TEXTURE_2D -> {
                                if (depth == 1) {
                                    this.textureSubImage2D(texture, level, xoffset, yoffset, width, height, image.getPixelFormat(), image.getPixelDataType(), image.getPixels().getAddress());
                                } else {
                                    throw new IllegalArgumentException("Depth must be 1 for 2D textures");
                                }
                            }
                            case GL12C.GL_TEXTURE_3D, GL30C.GL_TEXTURE_2D_ARRAY -> {
                                this.textureSubImage3D(texture, level, xoffset, yoffset, zoffset, width, height, depth, image.getPixelFormat(), image.getPixelDataType(), image.getPixels().getAddress());
                            }
                            default -> throw new IllegalArgumentException("Unsupported texture target: " + target);
                        }
                    } finally {
                        image.free();
                    }
                }
            } finally {
                ThinGL.glStateStack().popPixelStore();
            }
        }
    }

    @Override
    public void bindTextureUnit(final int unit, final int texture) {
        if (this.capabilities.glBindTextureUnit != 0L) {
            GL45C.glBindTextureUnit(unit, texture);
        } else {
            final int previousActiveTexture = GL11C.glGetInteger(GL13C.GL_ACTIVE_TEXTURE);
            GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + unit);
            if (texture != 0) {
                GL11C.glBindTexture(this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), texture);
            } else {
                GL11C.glBindTexture(GL11C.GL_TEXTURE_1D, 0);
                GL11C.glBindTexture(GL30C.GL_TEXTURE_1D_ARRAY, 0);
                GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, 0);
                GL11C.glBindTexture(GL30C.GL_TEXTURE_2D_ARRAY, 0);
                GL11C.glBindTexture(GL32C.GL_TEXTURE_2D_MULTISAMPLE, 0);
                GL11C.glBindTexture(GL32C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY, 0);
                GL11C.glBindTexture(GL12C.GL_TEXTURE_3D, 0);
                GL11C.glBindTexture(GL13C.GL_TEXTURE_CUBE_MAP, 0);
                GL11C.glBindTexture(GL40C.GL_TEXTURE_CUBE_MAP_ARRAY, 0);
                GL11C.glBindTexture(GL31C.GL_TEXTURE_BUFFER, 0);
            }
            GL13C.glActiveTexture(previousActiveTexture);
        }
    }

    @Override
    public void blitNamedFramebuffer(final int readFramebuffer, final int drawFramebuffer, final int srcX0, final int srcY0, final int srcX1, final int srcY1, final int dstX0, final int dstY0, final int dstX1, final int dstY1, final int mask, final int filter) {
        if (this.capabilities.glBlitNamedFramebuffer != 0L) {
            GL45C.glBlitNamedFramebuffer(readFramebuffer, drawFramebuffer, srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
        } else {
            final int previousReadFramebuffer = GL11C.glGetInteger(GL30C.GL_READ_FRAMEBUFFER_BINDING);
            final int previousDrawFramebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, readFramebuffer);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
            GL30C.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
            GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, previousReadFramebuffer);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousDrawFramebuffer);
        }
    }

    @Override
    public int checkNamedFramebufferStatus(final int framebuffer, final int target) {
        if (this.capabilities.glCheckNamedFramebufferStatus != 0L) {
            return GL45C.glCheckNamedFramebufferStatus(framebuffer, target);
        } else if (this.capabilities.glCheckNamedFramebufferStatusEXT != 0L) {
            return EXTDirectStateAccess.glCheckNamedFramebufferStatusEXT(framebuffer, target);
        } else {
            final int previousFramebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            final int status = GL30C.glCheckFramebufferStatus(target);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
            return status;
        }
    }

    @Override
    public void clearNamedFramebufferfi(final int framebuffer, final int buffer, final int drawbuffer, final float depth, final int stencil) {
        if (this.capabilities.glClearNamedFramebufferfi != 0L) {
            GL45C.glClearNamedFramebufferfi(framebuffer, buffer, drawbuffer, depth, stencil);
        } else {
            final int previousFramebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            GL30C.glClearBufferfi(buffer, drawbuffer, depth, stencil);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
        }
    }

    @Override
    public void clearNamedFramebufferfv(final int framebuffer, final int buffer, final int drawbuffer, final float[] value) {
        if (this.capabilities.glClearNamedFramebufferfv != 0L) {
            GL45C.glClearNamedFramebufferfv(framebuffer, buffer, drawbuffer, value);
        } else {
            final int previousFramebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            GL30C.glClearBufferfv(buffer, drawbuffer, value);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
        }
    }

    @Override
    public void clearNamedFramebufferiv(final int framebuffer, final int buffer, final int drawbuffer, final int[] value) {
        if (this.capabilities.glClearNamedFramebufferiv != 0L) {
            GL45C.glClearNamedFramebufferiv(framebuffer, buffer, drawbuffer, value);
        } else {
            final int previousFramebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            GL30C.glClearBufferiv(buffer, drawbuffer, value);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
        }
    }

    @Override
    public void copyNamedBufferSubData(final int readBuffer, final int writeBuffer, final long readOffset, final long writeOffset, final long size) {
        if (this.capabilities.glCopyNamedBufferSubData != 0L) {
            GL45C.glCopyNamedBufferSubData(readBuffer, writeBuffer, readOffset, writeOffset, size);
        } else if (this.capabilities.glNamedCopyBufferSubDataEXT != 0L) {
            EXTDirectStateAccess.glNamedCopyBufferSubDataEXT(readBuffer, writeBuffer, readOffset, writeOffset, size);
        } else {
            final int previousReadBuffer = GL11C.glGetInteger(GL31C.GL_COPY_READ_BUFFER);
            final int previousWriteBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_READ_BUFFER, readBuffer);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, writeBuffer);
            GL31C.glCopyBufferSubData(GL31C.GL_COPY_READ_BUFFER, GL31C.GL_COPY_WRITE_BUFFER, readOffset, writeOffset, size);
            GL15C.glBindBuffer(GL31C.GL_COPY_READ_BUFFER, previousReadBuffer);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousWriteBuffer);
        }
    }

    @Override
    public int createBuffers() {
        if (this.capabilities.glCreateBuffers != 0L) {
            return GL45C.glCreateBuffers();
        } else {
            final int buffer = GL15C.glGenBuffers();
            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
            return buffer;
        }
    }

    @Override
    public int createFramebuffers() {
        if (this.capabilities.glCreateFramebuffers != 0L) {
            return GL45C.glCreateFramebuffers();
        } else {
            final int framebuffer = GL30C.glGenFramebuffers();
            final int previousFramebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
            return framebuffer;
        }
    }

    @Override
    public int createQueries(final int target) {
        final int query;
        if (this.capabilities.glCreateQueries != 0L) {
            query = GL45C.glCreateQueries(target);
        } else {
            query = GL15C.glGenQueries();
        }
        this.queryTargets.put(query, target);
        return query;
    }

    @Override
    public int createRenderbuffers() {
        if (this.capabilities.glCreateRenderbuffers != 0L) {
            return GL45C.glCreateRenderbuffers();
        } else {
            final int renderBuffer = GL30C.glGenRenderbuffers();
            final int previousRenderBuffer = GL11C.glGetInteger(GL30C.GL_RENDERBUFFER_BINDING);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, renderBuffer);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, previousRenderBuffer);
            return renderBuffer;
        }
    }

    @Override
    public int createTextures(final int target) {
        final int texture;
        if (this.capabilities.glCreateTextures != 0L) {
            texture = GL45C.glCreateTextures(target);
        } else {
            texture = GL11C.glGenTextures();
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glBindTexture(target, previousTexture);
        }
        this.textureTargets.put(texture, target);
        return texture;
    }

    @Override
    public int createVertexArrays() {
        if (this.capabilities.glCreateVertexArrays != 0L) {
            return GL45C.glCreateVertexArrays();
        } else {
            final int vertexArray = GL30C.glGenVertexArrays();
            final int previousVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            GL30C.glBindVertexArray(vertexArray);
            GL30C.glBindVertexArray(previousVertexArray);
            return vertexArray;
        }
    }

    @Override
    public void enableVertexArrayAttrib(final int vaobj, final int index) {
        if (this.capabilities.glEnableVertexArrayAttrib != 0L) {
            GL45C.glEnableVertexArrayAttrib(vaobj, index);
        } else if (this.capabilities.glEnableVertexArrayAttribEXT != 0L) {
            EXTDirectStateAccess.glEnableVertexArrayAttribEXT(vaobj, index);
        } else {
            final int previousVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            GL30C.glBindVertexArray(vaobj);
            GL20C.glEnableVertexAttribArray(index);
            GL30C.glBindVertexArray(previousVertexArray);
        }
    }

    @Override
    public void flushMappedNamedBufferRange(final int buffer, final long offset, final long length) {
        if (this.capabilities.glFlushMappedNamedBufferRange != 0L) {
            GL45C.glFlushMappedNamedBufferRange(buffer, offset, length);
        } else if (this.capabilities.glFlushMappedNamedBufferRangeEXT != 0L) {
            EXTDirectStateAccess.glFlushMappedNamedBufferRangeEXT(buffer, offset, length);
        } else {
            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL30C.glFlushMappedBufferRange(GL31C.GL_COPY_WRITE_BUFFER, offset, length);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public void generateTextureMipmap(final int texture) {
        if (this.capabilities.glGenerateTextureMipmap != 0L) {
            GL45C.glGenerateTextureMipmap(texture);
        } else if (this.capabilities.glGenerateTextureMipmapEXT != 0L) {
            EXTDirectStateAccess.glGenerateTextureMipmapEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D));
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL30C.glGenerateMipmap(target);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public int getNamedBufferParameteri(final int buffer, final int pname) {
        if (this.capabilities.glGetNamedBufferParameteriv != 0L) {
            return GL45C.glGetNamedBufferParameteri(buffer, pname);
        } else if (this.capabilities.glGetNamedBufferParameterivEXT != 0L) {
            return EXTDirectStateAccess.glGetNamedBufferParameteriEXT(buffer, pname);
        } else {
            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            final int parameter = GL15C.glGetBufferParameteri(GL31C.GL_COPY_WRITE_BUFFER, pname);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
            return parameter;
        }
    }

    @Override
    public long getNamedBufferParameteri64(final int buffer, final int pname) {
        if (this.capabilities.glGetNamedBufferParameteri64v != 0L) {
            return GL45C.glGetNamedBufferParameteri64(buffer, pname);
        } else {
            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            final long parameter = GL32C.glGetBufferParameteri64(GL31C.GL_COPY_WRITE_BUFFER, pname);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
            return parameter;
        }
    }

    @Override
    public void getNamedBufferSubData(final int buffer, final long offset, final long size, final long data) {
        if (this.capabilities.glGetNamedBufferSubData != 0L) {
            GL45C.nglGetNamedBufferSubData(buffer, offset, size, data);
        } else if (this.capabilities.glGetNamedBufferSubDataEXT != 0L) {
            EXTDirectStateAccess.nglGetNamedBufferSubDataEXT(buffer, offset, size, data);
        } else {
            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL15C.nglGetBufferSubData(GL31C.GL_COPY_WRITE_BUFFER, offset, size, data);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public int getNamedFramebufferAttachmentParameteri(final int framebuffer, final int attachment, final int pname) {
        if (this.capabilities.glGetNamedFramebufferAttachmentParameteriv != 0L) {
            return GL45C.glGetNamedFramebufferAttachmentParameteri(framebuffer, attachment, pname);
        } else if (this.capabilities.glGetNamedFramebufferAttachmentParameterivEXT != 0L) {
            return EXTDirectStateAccess.glGetNamedFramebufferAttachmentParameteriEXT(framebuffer, attachment, pname);
        } else {
            final int previousFramebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            final int parameter = GL30C.glGetFramebufferAttachmentParameteri(GL30C.GL_DRAW_FRAMEBUFFER, attachment, pname);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
            return parameter;
        }
    }

    @Override
    public int getNamedRenderbufferParameteri(final int renderbuffer, final int pname) {
        if (this.capabilities.glGetNamedRenderbufferParameteriv != 0L) {
            return GL45C.glGetNamedRenderbufferParameteri(renderbuffer, pname);
        } else if (this.capabilities.glGetNamedRenderbufferParameterivEXT != 0L) {
            return EXTDirectStateAccess.glGetNamedRenderbufferParameteriEXT(renderbuffer, pname);
        } else {
            final int previousRenderbuffer = GL11C.glGetInteger(GL30C.GL_RENDERBUFFER_BINDING);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, renderbuffer);
            final int parameter = GL30C.glGetRenderbufferParameteri(GL30C.GL_RENDERBUFFER, pname);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, previousRenderbuffer);
            return parameter;
        }
    }

    @Override
    public int getTextureLevelParameteri(final int texture, final int level, final int pname) {
        if (this.capabilities.glGetTextureLevelParameteriv != 0L) {
            return GL45C.glGetTextureLevelParameteri(texture, level, pname);
        } else if (this.capabilities.glGetTextureLevelParameterivEXT != 0L) {
            return EXTDirectStateAccess.glGetTextureLevelParameteriEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), level, pname);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            final int parameter = GL11C.glGetTexLevelParameteri(target, level, pname);
            GL11C.glBindTexture(target, previousTexture);
            return parameter;
        }
    }

    @Override
    public float getTextureParameterf(final int texture, final int pname) {
        if (this.capabilities.glGetTextureParameterfv != 0L) {
            return GL45C.glGetTextureParameterf(texture, pname);
        } else if (this.capabilities.glGetTextureParameterfvEXT != 0L) {
            return EXTDirectStateAccess.glGetTextureParameterfEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            final float parameter = GL11C.glGetTexParameterf(target, pname);
            GL11C.glBindTexture(target, previousTexture);
            return parameter;
        }
    }

    @Override
    public void getTextureParameterfv(final int texture, final int pname, final float[] params) {
        if (this.capabilities.glGetTextureParameterfv != 0L) {
            GL45C.glGetTextureParameterfv(texture, pname, params);
        } else if (this.capabilities.glGetTextureParameterfvEXT != 0L) {
            EXTDirectStateAccess.glGetTextureParameterfvEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname, params);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glGetTexParameterfv(target, pname, params);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public int getTextureParameteri(final int texture, final int pname) {
        if (this.capabilities.glGetTextureParameteriv != 0L) {
            if (pname == GL45C.GL_TEXTURE_TARGET && ThinGL.workarounds().isGetTextureParameterTextureTargetBroken()) {
                return this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            }

            return GL45C.glGetTextureParameteri(texture, pname);
        } else if (this.capabilities.glGetTextureParameterivEXT != 0L) {
            if (pname == GL45C.GL_TEXTURE_TARGET) {
                return this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            }

            return EXTDirectStateAccess.glGetTextureParameteriEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname);
        } else {
            if (pname == GL45C.GL_TEXTURE_TARGET) {
                return this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            }

            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            final int parameter = GL11C.glGetTexParameteri(target, pname);
            GL11C.glBindTexture(target, previousTexture);
            return parameter;
        }
    }

    @Override
    public void getTextureParameteriv(final int texture, final int pname, final int[] params) {
        if (this.capabilities.glGetTextureParameteriv != 0L) {
            GL45C.glGetTextureParameteriv(texture, pname, params);
        } else if (this.capabilities.glGetTextureParameterivEXT != 0L) {
            EXTDirectStateAccess.glGetTextureParameterivEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname, params);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glGetTexParameteriv(target, pname, params);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void getTextureSubImage(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final int bufSize, final long pixels) {
        if (this.capabilities.glGetTextureSubImage != 0L) {
            GL45C.glGetTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, bufSize, pixels);
        } else {
            final int textureWidth = this.getTextureLevelParameteri(texture, level, GL11C.GL_TEXTURE_WIDTH);
            final int textureHeight = this.getTextureLevelParameteri(texture, level, GL11C.GL_TEXTURE_HEIGHT);
            final int textureDepth = this.getTextureLevelParameteri(texture, level, GL12C.GL_TEXTURE_DEPTH);
            if (xoffset == 0 && yoffset == 0 && zoffset == 0 && width == textureWidth && height == textureHeight && depth == textureDepth) {
                final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
                final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
                GL11C.glBindTexture(target, texture);
                GL11C.glGetTexImage(target, level, format, type, pixels);
                GL11C.glBindTexture(target, previousTexture);
            } else {
                throw new UnsupportedOperationException("Partial texture reads are not supported");
            }
        }
    }

    @Override
    public long mapNamedBuffer(final int buffer, final int access) {
        if (this.capabilities.glMapNamedBuffer != 0L) {
            return GL45C.nglMapNamedBuffer(buffer, access);
        } else if (this.capabilities.glMapNamedBufferEXT != 0L) {
            return EXTDirectStateAccess.nglMapNamedBufferEXT(buffer, access);
        } else {
            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            final long address = GL15C.nglMapBuffer(GL31C.GL_COPY_WRITE_BUFFER, access);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
            return address;
        }
    }

    @Override
    public long mapNamedBufferRange(final int buffer, final long offset, final long length, final int access) {
        if (this.capabilities.glMapNamedBufferRange != 0L) {
            return GL45C.nglMapNamedBufferRange(buffer, offset, length, access);
        } else if (this.capabilities.glMapNamedBufferRangeEXT != 0L) {
            return EXTDirectStateAccess.nglMapNamedBufferRangeEXT(buffer, offset, length, access);
        } else {
            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            final long address = GL30C.nglMapBufferRange(GL31C.GL_COPY_WRITE_BUFFER, offset, length, access);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
            return address;
        }
    }

    @Override
    public void namedBufferData(final int buffer, final long size, final int usage) {
        if (this.capabilities.glNamedBufferData != 0L) {
            GL45C.glNamedBufferData(buffer, size, usage);
        } else if (this.capabilities.glNamedBufferDataEXT != 0L) {
            EXTDirectStateAccess.glNamedBufferDataEXT(buffer, size, usage);
        } else {
            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL15C.glBufferData(GL31C.GL_COPY_WRITE_BUFFER, size, usage);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public void namedBufferData(final int buffer, final long size, final long data, final int usage) {
        if (this.capabilities.glNamedBufferData != 0L) {
            GL45C.nglNamedBufferData(buffer, size, data, usage);
        } else if (this.capabilities.glNamedBufferDataEXT != 0L) {
            EXTDirectStateAccess.nglNamedBufferDataEXT(buffer, size, data, usage);
        } else {
            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL15C.nglBufferData(GL31C.GL_COPY_WRITE_BUFFER, size, data, usage);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public void namedBufferStorage(final int buffer, final long size, final int flags) {
        if (this.capabilities.glNamedBufferStorage != 0L) {
            GL45C.glNamedBufferStorage(buffer, size, flags);
        } else if (this.capabilities.glNamedBufferStorageEXT != 0L) {
            ARBBufferStorage.glNamedBufferStorageEXT(buffer, size, flags);
        } else if (this.capabilities.glBufferStorage != 0L) {
            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL44C.glBufferStorage(GL31C.GL_COPY_WRITE_BUFFER, size, flags);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        } else {
            final int usage;
            if ((flags & GL44C.GL_DYNAMIC_STORAGE_BIT) != 0) {
                usage = GL15C.GL_DYNAMIC_DRAW;
            } else {
                usage = GL15C.GL_STATIC_DRAW;
            }

            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL15C.glBufferData(GL31C.GL_COPY_WRITE_BUFFER, size, usage);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public void namedBufferStorage(final int buffer, final long size, final long data, final int flags) {
        if (this.capabilities.glNamedBufferStorage != 0L) {
            GL45C.nglNamedBufferStorage(buffer, size, data, flags);
        } else if (this.capabilities.glNamedBufferStorageEXT != 0L) {
            ARBBufferStorage.nglNamedBufferStorageEXT(buffer, size, data, flags);
        } else if (this.capabilities.glBufferStorage != 0L) {
            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL44C.nglBufferStorage(GL31C.GL_COPY_WRITE_BUFFER, size, data, flags);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        } else {
            final int usage;
            if ((flags & GL44C.GL_DYNAMIC_STORAGE_BIT) != 0) {
                usage = GL15C.GL_DYNAMIC_DRAW;
            } else {
                usage = GL15C.GL_STATIC_DRAW;
            }

            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL15C.nglBufferData(GL31C.GL_COPY_WRITE_BUFFER, size, data, usage);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public void namedBufferSubData(final int buffer, final long offset, final long size, final long data) {
        if (this.capabilities.glNamedBufferSubData != 0L) {
            GL45C.nglNamedBufferSubData(buffer, offset, size, data);
        } else if (this.capabilities.glNamedBufferSubDataEXT != 0L) {
            EXTDirectStateAccess.nglNamedBufferSubDataEXT(buffer, offset, size, data);
        } else {
            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL15C.nglBufferSubData(GL31C.GL_COPY_WRITE_BUFFER, offset, size, data);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public void namedFramebufferRenderbuffer(final int framebuffer, final int attachment, final int renderbuffertarget, final int renderbuffer) {
        if (this.capabilities.glNamedFramebufferRenderbuffer != 0L) {
            GL45C.glNamedFramebufferRenderbuffer(framebuffer, attachment, renderbuffertarget, renderbuffer);
        } else if (this.capabilities.glNamedFramebufferRenderbufferEXT != 0L) {
            EXTDirectStateAccess.glNamedFramebufferRenderbufferEXT(framebuffer, attachment, renderbuffertarget, renderbuffer);
        } else {
            final int previousFramebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            GL30C.glFramebufferRenderbuffer(GL30C.GL_DRAW_FRAMEBUFFER, attachment, renderbuffertarget, renderbuffer);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
        }
    }

    @Override
    public void namedFramebufferTexture(final int framebuffer, final int attachment, final int texture, final int level) {
        if (this.capabilities.glNamedFramebufferTexture != 0L) {
            GL45C.glNamedFramebufferTexture(framebuffer, attachment, texture, level);
        } else if (this.capabilities.glNamedFramebufferTextureEXT != 0L) {
            EXTDirectStateAccess.glNamedFramebufferTextureEXT(framebuffer, attachment, texture, level);
        } else {
            final int previousFramebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            GL30C.glFramebufferTexture2D(GL30C.GL_DRAW_FRAMEBUFFER, attachment, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), texture, level);
            GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
        }
    }

    @Override
    public void namedRenderbufferStorage(final int renderbuffer, final int internalformat, final int width, final int height) {
        if (this.capabilities.glNamedRenderbufferStorage != 0L) {
            GL45C.glNamedRenderbufferStorage(renderbuffer, internalformat, width, height);
        } else if (this.capabilities.glNamedRenderbufferStorageEXT != 0L) {
            EXTDirectStateAccess.glNamedRenderbufferStorageEXT(renderbuffer, internalformat, width, height);
        } else {
            final int previousRenderbuffer = GL11C.glGetInteger(GL30C.GL_RENDERBUFFER_BINDING);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, renderbuffer);
            GL30C.glRenderbufferStorage(GL30C.GL_RENDERBUFFER, internalformat, width, height);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, previousRenderbuffer);
        }
    }

    @Override
    public void namedRenderbufferStorageMultisample(final int renderbuffer, final int samples, final int internalformat, final int width, final int height) {
        if (this.capabilities.glNamedRenderbufferStorageMultisample != 0L) {
            GL45C.glNamedRenderbufferStorageMultisample(renderbuffer, samples, internalformat, width, height);
        } else if (this.capabilities.glNamedRenderbufferStorageMultisampleEXT != 0L) {
            EXTDirectStateAccess.glNamedRenderbufferStorageMultisampleEXT(renderbuffer, samples, internalformat, width, height);
        } else {
            final int previousRenderbuffer = GL11C.glGetInteger(GL30C.GL_RENDERBUFFER_BINDING);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, renderbuffer);
            GL30C.glRenderbufferStorageMultisample(GL30C.GL_RENDERBUFFER, samples, internalformat, width, height);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, previousRenderbuffer);
        }
    }

    @Override
    public void textureBuffer(final int texture, final int internalformat, final int buffer) {
        if (this.capabilities.glTextureBuffer != 0L) {
            GL45C.glTextureBuffer(texture, internalformat, buffer);
        } else if (this.capabilities.glTextureBufferEXT != 0L) {
            EXTDirectStateAccess.glTextureBufferEXT(texture, this.textureTargets.getOrDefault(texture, GL31C.GL_TEXTURE_BUFFER), internalformat, buffer);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL31C.GL_TEXTURE_BUFFER);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL31C.glTexBuffer(target, internalformat, buffer);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureParameterf(final int texture, final int pname, final float param) {
        if (this.capabilities.glTextureParameterf != 0L) {
            GL45C.glTextureParameterf(texture, pname, param);
        } else if (this.capabilities.glTextureParameterfEXT != 0L) {
            EXTDirectStateAccess.glTextureParameterfEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname, param);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glTexParameterf(target, pname, param);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureParameterfv(final int texture, final int pname, final float[] params) {
        if (this.capabilities.glTextureParameterfv != 0L) {
            GL45C.glTextureParameterfv(texture, pname, params);
        } else if (this.capabilities.glTextureParameterfvEXT != 0L) {
            EXTDirectStateAccess.glTextureParameterfvEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname, params);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glTexParameterfv(target, pname, params);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureParameteri(final int texture, final int pname, final int param) {
        if (this.capabilities.glTextureParameteri != 0L) {
            GL45C.glTextureParameteri(texture, pname, param);
        } else if (this.capabilities.glTextureParameteriEXT != 0L) {
            EXTDirectStateAccess.glTextureParameteriEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname, param);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glTexParameteri(target, pname, param);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureParameteriv(final int texture, final int pname, final int[] params) {
        if (this.capabilities.glTextureParameteriv != 0L) {
            GL45C.glTextureParameteriv(texture, pname, params);
        } else if (this.capabilities.glTextureParameterivEXT != 0L) {
            EXTDirectStateAccess.glTextureParameterivEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname, params);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glTexParameteriv(target, pname, params);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureStorage1D(final int texture, final int levels, final int internalformat, final int width) {
        if (this.capabilities.glTextureStorage1D != 0L) {
            GL45C.glTextureStorage1D(texture, levels, internalformat, width);
        } else if (this.capabilities.glTextureStorage1DEXT != 0L) {
            ARBTextureStorage.glTextureStorage1DEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_1D), levels, internalformat, width);
        } else if (this.capabilities.glTexStorage1D != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_1D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL42C.glTexStorage1D(target, levels, internalformat, width);
            GL11C.glBindTexture(target, previousTexture);
        } else if (this.capabilities.glTexStorage1DEXT != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_1D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            EXTTextureStorage.glTexStorage1DEXT(target, levels, internalformat, width);
            GL11C.glBindTexture(target, previousTexture);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            for (int level = 0; level < levels; level++) {
                final int levelWidth = Math.max(1, width >> level);
                GL11C.glTexImage1D(target, level, internalformat, levelWidth, 0, getTextureFormat(internalformat), getTextureType(internalformat), 0L);
            }
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureStorage2D(final int texture, final int levels, final int internalformat, final int width, final int height) {
        if (this.capabilities.glTextureStorage2D != 0L) {
            GL45C.glTextureStorage2D(texture, levels, internalformat, width, height);
        } else if (this.capabilities.glTextureStorage2DEXT != 0L) {
            ARBTextureStorage.glTextureStorage2DEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), levels, internalformat, width, height);
        } else if (this.capabilities.glTexStorage2D != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL42C.glTexStorage2D(target, levels, internalformat, width, height);
            GL11C.glBindTexture(target, previousTexture);
        } else if (this.capabilities.glTexStorage2DEXT != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            EXTTextureStorage.glTexStorage2DEXT(target, levels, internalformat, width, height);
            GL11C.glBindTexture(target, previousTexture);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            for (int level = 0; level < levels; level++) {
                final int levelWidth = Math.max(1, width >> level);
                final int levelHeight = Math.max(1, height >> level);
                GL11C.glTexImage2D(target, level, internalformat, levelWidth, levelHeight, 0, getTextureFormat(internalformat), getTextureType(internalformat), 0L);
            }
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureStorage2DMultisample(final int texture, final int samples, final int internalformat, final int width, final int height, final boolean fixedsamplelocations) {
        if (this.capabilities.glTextureStorage2DMultisample != 0L) {
            GL45C.glTextureStorage2DMultisample(texture, samples, internalformat, width, height, fixedsamplelocations);
        } else if (this.capabilities.glTextureStorage2DMultisampleEXT != 0L) {
            ARBTextureStorageMultisample.glTextureStorage2DMultisampleEXT(texture, this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE), samples, internalformat, width, height, fixedsamplelocations);
        } else if (this.capabilities.glTexStorage2DMultisample != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL43C.glTexStorage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
            GL11C.glBindTexture(target, previousTexture);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL32C.glTexImage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureStorage3D(final int texture, final int levels, final int internalformat, final int width, final int height, final int depth) {
        if (this.capabilities.glTextureStorage3D != 0L) {
            GL45C.glTextureStorage3D(texture, levels, internalformat, width, height, depth);
        } else if (this.capabilities.glTextureStorage3DEXT != 0L) {
            ARBTextureStorage.glTextureStorage3DEXT(texture, this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D), levels, internalformat, width, height, depth);
        } else if (this.capabilities.glTexStorage3D != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL42C.glTexStorage3D(target, levels, internalformat, width, height, depth);
            GL11C.glBindTexture(target, previousTexture);
        } else if (this.capabilities.glTexStorage3DEXT != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            EXTTextureStorage.glTexStorage3DEXT(target, levels, internalformat, width, height, depth);
            GL11C.glBindTexture(target, previousTexture);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            for (int level = 0; level < levels; level++) {
                final int levelWidth = Math.max(1, width >> level);
                final int levelHeight = Math.max(1, height >> level);
                final int levelDepth = Math.max(1, depth >> level);
                GL12C.glTexImage3D(target, level, internalformat, levelWidth, levelHeight, levelDepth, 0, getTextureFormat(internalformat), getTextureType(internalformat), 0L);
            }
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureStorage3DMultisample(final int texture, final int samples, final int internalformat, final int width, final int height, final int depth, final boolean fixedsamplelocations) {
        if (this.capabilities.glTextureStorage3DMultisample != 0L) {
            GL45C.glTextureStorage3DMultisample(texture, samples, internalformat, width, height, depth, fixedsamplelocations);
        } else if (this.capabilities.glTextureStorage3DMultisampleEXT != 0L) {
            ARBTextureStorageMultisample.glTextureStorage3DMultisampleEXT(texture, this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY), samples, internalformat, width, height, depth, fixedsamplelocations);
        } else if (this.capabilities.glTexStorage3DMultisample != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL43C.glTexStorage3DMultisample(target, samples, internalformat, width, height, depth, fixedsamplelocations);
            GL11C.glBindTexture(target, previousTexture);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL32C.glTexImage3DMultisample(target, samples, internalformat, width, height, depth, fixedsamplelocations);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureSubImage1D(final int texture, final int level, final int xoffset, final int width, final int format, final int type, final long pixels) {
        if (this.capabilities.glTextureSubImage1D != 0L) {
            GL45C.nglTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
        } else if (this.capabilities.glTextureSubImage1DEXT != 0L) {
            EXTDirectStateAccess.nglTextureSubImage1DEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_1D), level, xoffset, width, format, type, pixels);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_1D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.nglTexSubImage1D(target, level, xoffset, width, format, type, pixels);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureSubImage2D(final int texture, final int level, final int xoffset, final int yoffset, final int width, final int height, final int format, final int type, final long pixels) {
        if (this.capabilities.glTextureSubImage2D != 0L) {
            GL45C.nglTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
        } else if (this.capabilities.glTextureSubImage2DEXT != 0L) {
            EXTDirectStateAccess.nglTextureSubImage2DEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), level, xoffset, yoffset, width, height, format, type, pixels);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.nglTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureSubImage3D(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final long pixels) {
        if (this.capabilities.glTextureSubImage3D != 0L) {
            GL45C.nglTextureSubImage3D(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
        } else if (this.capabilities.glTextureSubImage3DEXT != 0L) {
            EXTDirectStateAccess.nglTextureSubImage3DEXT(texture, this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D), level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D);
            final int previousTexture = GL11C.glGetInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL12C.nglTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public boolean unmapNamedBuffer(final int buffer) {
        if (this.capabilities.glUnmapNamedBuffer != 0L) {
            return GL45C.glUnmapNamedBuffer(buffer);
        } else if (this.capabilities.glUnmapNamedBufferEXT != 0L) {
            return EXTDirectStateAccess.glUnmapNamedBufferEXT(buffer);
        } else {
            final int previousBuffer = GL11C.glGetInteger(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            final boolean result = GL15C.glUnmapBuffer(GL31C.GL_COPY_WRITE_BUFFER);
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
            return result;
        }
    }

    @Override
    public void vertexArrayAttribBinding(final int vaobj, final int attribindex, final int bindingindex) {
        final VertexArrayObject vertexArrayObject = this.vertexArrayObjects.computeIfAbsent(vaobj, VertexArrayObject::new);
        final VertexArrayObject.VertexAttribute vertexAttribute = vertexArrayObject.attributes.computeIfAbsent(attribindex, VertexArrayObject.VertexAttribute::new);
        vertexAttribute.bindingindex = bindingindex;

        if (this.capabilities.glVertexArrayAttribBinding != 0L) {
            GL45C.glVertexArrayAttribBinding(vaobj, attribindex, bindingindex);
        } else if (this.capabilities.glVertexArrayVertexAttribBindingEXT != 0L) {
            ARBVertexAttribBinding.glVertexArrayVertexAttribBindingEXT(vaobj, attribindex, bindingindex);
        } else if (this.capabilities.glVertexAttribBinding != 0L) {
            final int previousVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            GL30C.glBindVertexArray(vaobj);
            GL43C.glVertexAttribBinding(attribindex, bindingindex);
            GL30C.glBindVertexArray(previousVertexArray);
        } else {
            vertexArrayObject.applyAttribute(attribindex);
        }
    }

    @Override
    public void vertexArrayAttribFormat(final int vaobj, final int attribindex, final int size, final int type, final boolean normalized, final int relativeoffset) {
        final VertexArrayObject vertexArrayObject = this.vertexArrayObjects.computeIfAbsent(vaobj, VertexArrayObject::new);
        final VertexArrayObject.VertexAttribute vertexAttribute = vertexArrayObject.attributes.computeIfAbsent(attribindex, VertexArrayObject.VertexAttribute::new);
        vertexAttribute.format = new VertexArrayObject.VertexAttribFFormat(size, type, normalized, relativeoffset);

        if (this.capabilities.glVertexArrayAttribFormat != 0L) {
            GL45C.glVertexArrayAttribFormat(vaobj, attribindex, size, type, normalized, relativeoffset);
        } else if (this.capabilities.glVertexArrayVertexAttribFormatEXT != 0L) {
            ARBVertexAttribBinding.glVertexArrayVertexAttribFormatEXT(vaobj, attribindex, size, type, normalized, relativeoffset);
        } else if (this.capabilities.glVertexAttribFormat != 0L) {
            final int previousVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            GL30C.glBindVertexArray(vaobj);
            GL43C.glVertexAttribFormat(attribindex, size, type, normalized, relativeoffset);
            GL30C.glBindVertexArray(previousVertexArray);
        } else {
            vertexArrayObject.applyAttribute(attribindex);
        }
    }

    @Override
    public void vertexArrayAttribIFormat(final int vaobj, final int attribindex, final int size, final int type, final int relativeoffset) {
        final VertexArrayObject vertexArrayObject = this.vertexArrayObjects.computeIfAbsent(vaobj, VertexArrayObject::new);
        final VertexArrayObject.VertexAttribute vertexAttribute = vertexArrayObject.attributes.computeIfAbsent(attribindex, VertexArrayObject.VertexAttribute::new);
        vertexAttribute.format = new VertexArrayObject.VertexAttribIFormat(size, type, relativeoffset);

        if (this.capabilities.glVertexArrayAttribIFormat != 0L) {
            GL45C.glVertexArrayAttribIFormat(vaobj, attribindex, size, type, relativeoffset);
        } else if (this.capabilities.glVertexArrayVertexAttribIFormatEXT != 0L) {
            ARBVertexAttribBinding.glVertexArrayVertexAttribIFormatEXT(vaobj, attribindex, size, type, relativeoffset);
        } else if (this.capabilities.glVertexAttribIFormat != 0L) {
            final int previousVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            GL30C.glBindVertexArray(vaobj);
            GL43C.glVertexAttribIFormat(attribindex, size, type, relativeoffset);
            GL30C.glBindVertexArray(previousVertexArray);
        } else {
            vertexArrayObject.applyAttribute(attribindex);
        }
    }

    @Override
    public void vertexArrayAttribLFormat(final int vaobj, final int attribindex, final int size, final int type, final int relativeoffset) {
        final VertexArrayObject vertexArrayObject = this.vertexArrayObjects.computeIfAbsent(vaobj, VertexArrayObject::new);
        final VertexArrayObject.VertexAttribute vertexAttribute = vertexArrayObject.attributes.computeIfAbsent(attribindex, VertexArrayObject.VertexAttribute::new);
        vertexAttribute.format = new VertexArrayObject.VertexAttribLFormat(size, type, relativeoffset);

        if (this.capabilities.glVertexArrayAttribLFormat != 0L) {
            GL45C.glVertexArrayAttribLFormat(vaobj, attribindex, size, type, relativeoffset);
        } else if (this.capabilities.glVertexArrayVertexAttribLFormatEXT != 0L) {
            ARBVertexAttribBinding.glVertexArrayVertexAttribLFormatEXT(vaobj, attribindex, size, type, relativeoffset);
        } else if (this.capabilities.glVertexAttribLFormat != 0L) {
            final int previousVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            GL30C.glBindVertexArray(vaobj);
            GL43C.glVertexAttribLFormat(attribindex, size, type, relativeoffset);
            GL30C.glBindVertexArray(previousVertexArray);
        } else {
            vertexArrayObject.applyAttribute(attribindex);
        }
    }

    @Override
    public void vertexArrayBindingDivisor(final int vaobj, final int bindingindex, final int divisor) {
        final VertexArrayObject vertexArrayObject = this.vertexArrayObjects.computeIfAbsent(vaobj, VertexArrayObject::new);
        final VertexArrayObject.VertexBufferBinding vertexBufferBinding = vertexArrayObject.vertexBufferBinding.computeIfAbsent(bindingindex, VertexArrayObject.VertexBufferBinding::new);
        vertexBufferBinding.divisor = divisor;

        if (this.capabilities.glVertexArrayBindingDivisor != 0L) {
            GL45C.glVertexArrayBindingDivisor(vaobj, bindingindex, divisor);
        } else if (this.capabilities.glVertexArrayVertexBindingDivisorEXT != 0L) {
            ARBVertexAttribBinding.glVertexArrayVertexBindingDivisorEXT(vaobj, bindingindex, divisor);
        } else if (this.capabilities.glVertexBindingDivisor != 0L) {
            final int previousVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            GL30C.glBindVertexArray(vaobj);
            GL43C.glVertexBindingDivisor(bindingindex, divisor);
            GL30C.glBindVertexArray(previousVertexArray);
        } else {
            vertexArrayObject.applyVertexBufferBinding(bindingindex);
        }
    }

    @Override
    public void vertexArrayElementBuffer(final int vaobj, final int buffer) {
        if (this.capabilities.glVertexArrayElementBuffer != 0L && !(buffer == 0 && ThinGL.workarounds().isDsaVertexArrayElementBufferUnbindBroken())) {
            GL45C.glVertexArrayElementBuffer(vaobj, buffer);
        } else {
            final int previousVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            GL30C.glBindVertexArray(vaobj);
            GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, buffer);
            GL30C.glBindVertexArray(previousVertexArray);
        }
    }

    @Override
    public void vertexArrayVertexBuffer(final int vaobj, final int bindingindex, final int buffer, final long offset, final int stride) {
        final VertexArrayObject vertexArrayObject = this.vertexArrayObjects.computeIfAbsent(vaobj, VertexArrayObject::new);
        final VertexArrayObject.VertexBufferBinding vertexBufferBinding = vertexArrayObject.vertexBufferBinding.computeIfAbsent(bindingindex, VertexArrayObject.VertexBufferBinding::new);
        vertexBufferBinding.buffer = buffer;
        vertexBufferBinding.offset = offset;
        vertexBufferBinding.stride = stride;

        if (this.capabilities.glVertexArrayVertexBuffer != 0L) {
            GL45C.glVertexArrayVertexBuffer(vaobj, bindingindex, buffer, offset, stride);
        } else if (this.capabilities.glVertexArrayBindVertexBufferEXT != 0L) {
            ARBVertexAttribBinding.glVertexArrayBindVertexBufferEXT(vaobj, bindingindex, buffer, offset, stride);
        } else if (this.capabilities.glBindVertexBuffer != 0L) {
            final int previousVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            GL30C.glBindVertexArray(vaobj);
            GL43C.glBindVertexBuffer(bindingindex, buffer, offset, stride);
            GL30C.glBindVertexArray(previousVertexArray);
        } else {
            vertexArrayObject.applyVertexBufferBinding(bindingindex);
        }
    }

    public void putTextureTarget(final int texture, final int target) {
        this.textureTargets.put(texture, target);
    }

    public void putQueryTarget(final int query, final int target) {
        this.queryTargets.put(query, target);
    }

    private static class VertexArrayObject {

        private final int id;
        private final Int2ObjectMap<VertexBufferBinding> vertexBufferBinding = new Int2ObjectOpenHashMap<>();
        private final Int2ObjectMap<VertexAttribute> attributes = new Int2ObjectOpenHashMap<>();

        private VertexArrayObject(final int id) {
            this.id = id;
        }

        public void applyVertexBufferBinding(final int index) {
            final VertexBufferBinding binding = this.vertexBufferBinding.get(index);
            if (binding == null) {
                return;
            }

            for (Int2ObjectMap.Entry<VertexAttribute> entry : this.attributes.int2ObjectEntrySet()) {
                if (entry.getValue().bindingindex == index) {
                    this.applyAttribute(entry.getIntKey());
                }
            }
        }

        public void applyAttribute(final int index) {
            final VertexAttribute attribute = this.attributes.get(index);
            if (attribute == null || attribute.format == null) {
                return;
            }
            final VertexBufferBinding binding = this.vertexBufferBinding.get(attribute.bindingindex);
            if (binding == null) {
                return;
            }

            final int previousVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            final int previousBuffer = GL11C.glGetInteger(GL15C.GL_ARRAY_BUFFER_BINDING);
            GL30C.glBindVertexArray(this.id);
            GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, binding.buffer);

            if (attribute.format instanceof VertexAttribFFormat fformat) {
                GL20C.glVertexAttribPointer(index, fformat.size(), fformat.type(), fformat.normalized(), binding.stride, binding.offset + fformat.relativeoffset());
            } else if (attribute.format instanceof VertexAttribIFormat iformat) {
                GL30C.glVertexAttribIPointer(index, iformat.size(), iformat.type(), binding.stride, binding.offset + iformat.relativeoffset());
            } else if (attribute.format instanceof VertexAttribLFormat lformat) {
                GL41C.glVertexAttribLPointer(index, lformat.size(), lformat.type(), binding.stride, binding.offset + lformat.relativeoffset());
            } else {
                throw new IllegalStateException("Unknown VertexAttribFormat");
            }
            GL33C.glVertexAttribDivisor(index, binding.divisor);

            GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, previousBuffer);
            GL30C.glBindVertexArray(previousVertexArray);
        }

        private static class VertexBufferBinding {

            private int buffer;
            private long offset;
            private int stride;
            private int divisor;

            private VertexBufferBinding(final int index) {
            }

        }

        private static class VertexAttribute {

            private int bindingindex;
            private VertexAttribFormat format;

            private VertexAttribute(final int index) {
            }

        }

        private sealed interface VertexAttribFormat permits VertexAttribFFormat, VertexAttribIFormat, VertexAttribLFormat {
        }

        private record VertexAttribFFormat(int size, int type, boolean normalized, int relativeoffset) implements VertexAttribFormat {
        }

        private record VertexAttribIFormat(int size, int type, int relativeoffset) implements VertexAttribFormat {
        }

        private record VertexAttribLFormat(int size, int type, int relativeoffset) implements VertexAttribFormat {
        }

    }

    private static class ShaderStorageBufferShaderRewriter {

        private static boolean modify(final GlslTree tree) {
            final List<GlslStructDeclarationNode> bufferStructs = getBufferStructs(tree);
            for (GlslStructDeclarationNode bufferStruct : bufferStructs) {
                final int index = tree.getBody().indexOf(bufferStruct);
                { // Replace buffer struct with samplerBuffer uniform
                    final GlslSpecifiedType type = new GlslSpecifiedType(GlslTypeSpecifier.BuiltinType.SAMPLERBUFFER, GlslTypeQualifier.StorageType.UNIFORM);
                    final GlslNewFieldNode uniformNode = new GlslNewFieldNode(type, bufferStruct.getName(), null);
                    tree.getBody().set(index, uniformNode);
                }
                final Map<String, String> fieldReplacements = new HashMap<>();
                { // Generate the getter functions for each field in the buffer struct
                    final List<GlslStructField> fields = bufferStruct.getStructSpecifier().getFields();
                    int offset = 0;
                    for (int i = 0; i < fields.size(); i++) {
                        final GlslStructField field = fields.get(i);
                        final GlslFunctionNode getterFunctionNode = generateGetter(tree, offset, bufferStruct.getName(), field);
                        fieldReplacements.put(field.getName(), getterFunctionNode.getName());
                        tree.getBody().add(index + 1, getterFunctionNode);
                        if (i < fields.size() - 1) {
                            offset += calculateSize(tree, field.getType().getSpecifier());
                        }
                    }
                }

                // Replace all accesses to the buffer struct fields with calls to the generated getter functions
                GlslNodeMutator.mutate(tree.getBody(), node -> {
                    if (node instanceof GlslVariableNode variableNode) {
                        final String replacement = fieldReplacements.get(variableNode.getName());
                        if (replacement != null) {
                            return new GlslInvokeFunctionNode(new GlslVariableNode(replacement), new ArrayList<>());
                        }
                    } else if (node instanceof GlslGetArrayNode getArrayNode) {
                        if (getArrayNode.getExpression() instanceof GlslInvokeFunctionNode invokeFunctionNode) {
                            invokeFunctionNode.getParameters().add(new GlslInvokeFunctionNode(new GlslVariableNode("int"), List.of(getArrayNode.getIndex())));
                            return invokeFunctionNode;
                        }
                    }
                    return node;
                });
            }
            return !bufferStructs.isEmpty();
        }

        private static GlslFunctionNode generateGetter(final GlslTree tree, final int offset, final String bufferStructName, final GlslStructField field) {
            final String functionName = "get_" + bufferStructName + "_" + field.getName();
            final String varName = "var";
            GlslFunctionNode functionNode;
            if (field.getType().getSpecifier() instanceof GlslTypeSpecifier.Array arraySpecifier) {
                // If the buffer struct field is an array, the getter doesn't return the entire array, instead it returns only the element at the given index.
                // This prevents a lot of unnecessary converting of data.
                final int typeSize = calculateSize(tree, arraySpecifier.getSpecifier());
                functionNode = new GlslFunctionNode(
                        new GlslFunctionHeader(functionName, arraySpecifier.getSpecifier(), List.of(new GlslParameterDeclaration(GlslTypeSpecifier.BuiltinType.INT, "index"))),
                        List.of()
                );
                functionNode.getBody().add(new GlslNewFieldNode(arraySpecifier.getSpecifier(), varName, null));
                functionNode.getBody().addAll(generateConstructor(
                        tree,
                        bufferStructName,
                        new GlslOperationNode(
                                GlslNode.intConstant(offset),
                                new GlslOperationNode(new GlslVariableNode("index"), GlslNode.intConstant(typeSize), GlslOperationNode.Operand.MULTIPLY),
                                GlslOperationNode.Operand.ADD
                        ),
                        new GlslVariableNode(varName),
                        arraySpecifier.getSpecifier())
                );
            } else {
                // If the buffer struct field is not an array, the getter simply reads and returns the value.
                functionNode = new GlslFunctionNode(new GlslFunctionHeader(functionName, field.getType(), List.of()), List.of());
                functionNode.getBody().add(new GlslNewFieldNode(field.getType().getSpecifier(), varName, null));
                functionNode.getBody().addAll(generateConstructor(tree, bufferStructName, GlslNode.intConstant(offset), new GlslVariableNode(varName), field.getType().getSpecifier()));
            }
            functionNode.getBody().add(new GlslReturnNode(new GlslVariableNode(varName)));
            return functionNode;
        }

        private static GlslNodeList generateConstructor(final GlslTree tree, final String arrayName, final GlslNode offset, final GlslNode target, final GlslTypeSpecifier type) {
            final GlslNodeList nodeList = new GlslNodeList();
            if (type.equals(GlslTypeSpecifier.BuiltinType.UINT)) {
                final GlslInvokeFunctionNode texelFetch = new GlslInvokeFunctionNode(new GlslVariableNode("texelFetch"), List.of(new GlslVariableNode(arrayName), offset));
                final GlslGetFieldNode x = new GlslGetFieldNode(texelFetch, "x");
                final GlslInvokeFunctionNode floatBitsToUint = new GlslInvokeFunctionNode(new GlslVariableNode("floatBitsToUint"), List.of(x));
                nodeList.add(new GlslAssignmentNode(target, floatBitsToUint, GlslAssignmentNode.Operand.EQUAL));
            } else if (type instanceof GlslTypeSpecifier.Name) {
                int currentOffset = 0;
                final GlslStructDeclarationNode structNode = getStruct(tree, type.getName());
                final List<GlslStructField> fields = structNode.getStructSpecifier().getFields();
                for (int i = 0; i < fields.size(); i++) {
                    final GlslStructField field = fields.get(i);
                    nodeList.addAll(generateConstructor(
                            tree,
                            arrayName,
                            new GlslOperationNode(offset, GlslNode.intConstant(currentOffset), GlslOperationNode.Operand.ADD),
                            new GlslGetFieldNode(target, field.getName()),
                            field.getType().getSpecifier()
                    ));
                    if (i < fields.size() - 1) {
                        currentOffset += calculateSize(tree, field.getType().getSpecifier());
                    }
                }
            }
            if (nodeList.isEmpty()) {
                throw new IllegalArgumentException("Cannot generate constructor for type: " + type + " (" + type.getClass().getName() + ")");
            }
            return nodeList;
        }

        /**
         * Calculate the size of the given type specified in number of float components.
         */
        private static int calculateSize(final GlslTree tree, final GlslTypeSpecifier type) {
            if (type.equals(GlslTypeSpecifier.BuiltinType.UINT)) {
                return 1;
            } else if (type instanceof GlslStructSpecifier structSpecifier) {
                int size = 0;
                for (GlslStructField field : structSpecifier.getFields()) {
                    size += calculateSize(tree, field.getType().getSpecifier());
                }
                return size;
            } else if (type instanceof GlslTypeSpecifier.Array arraySpecifier) {
                if (arraySpecifier.getSize() == null) {
                    throw new IllegalArgumentException("Cannot calculate size of unsized array: " + type + " (" + type.getClass().getName() + ")");
                }
                if (!(arraySpecifier.getSize() instanceof GlslIntConstantNode intConstantNode)) {
                    throw new IllegalArgumentException("Cannot calculate size of dynamically sized array: " + type + " (" + type.getClass().getName() + ")");
                }
                return calculateSize(tree, arraySpecifier.getSpecifier()) * intConstantNode.intValue();
            } else if (type instanceof GlslTypeSpecifier.Name) {
                int size = 0;
                final GlslStructDeclarationNode structNode = getStruct(tree, type.getName());
                for (GlslStructField field : structNode.getStructSpecifier().getFields()) {
                    size += calculateSize(tree, field.getType().getSpecifier());
                }
                return size;
            } else {
                throw new IllegalArgumentException("Cannot calculate size of type: " + type + " (" + type.getClass().getName() + ")");
            }
        }

        private static List<GlslStructDeclarationNode> getBufferStructs(final GlslTree tree) {
            final List<GlslStructDeclarationNode> bufferStructs = new ArrayList<>();
            for (GlslNode node : tree.getBody()) {
                if (node instanceof GlslStructDeclarationNode structNode) {
                    final GlslSpecifiedType structType = structNode.getSpecifiedType();
                    if (structType.getQualifiers().contains(GlslTypeQualifier.StorageType.BUFFER) && structType.getQualifiers().contains(GlslTypeQualifier.StorageType.READONLY)) {
                        bufferStructs.add(structNode);
                    }
                }
            }
            return bufferStructs;
        }

        private static GlslStructDeclarationNode getStruct(final GlslTree tree, final String name) {
            for (GlslNode node : tree.getBody()) {
                if (node instanceof GlslStructDeclarationNode structNode) {
                    if (structNode.getName().equals(name)) {
                        return structNode;
                    }
                }
            }
            throw new IllegalArgumentException("Struct not found: " + name);
        }

    }

}
