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

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.gl.GLBackend;
import net.raphimc.thingl.rendering.command.impl.DrawArraysCommand;
import net.raphimc.thingl.rendering.command.impl.DrawElementsCommand;
import net.raphimc.thingl.resource.image.Image;
import org.lwjgl.opengl.*;

public class GL41Backend implements GLBackend {

    private static final Int2IntMap TEXTURE_QUERIES = new Int2IntOpenHashMap();
    private static final Int2IntMap TEXTURE_FORMATS = new Int2IntOpenHashMap();
    private static final Int2IntMap TEXTURE_TYPES = new Int2IntOpenHashMap();
    private static final Int2IntMap DEBUG_LABEL_OBJECT_TYPE_MAP = new Int2IntOpenHashMap();

    static {
        TEXTURE_QUERIES.put(GL11C.GL_TEXTURE_1D, GL11C.GL_TEXTURE_BINDING_1D);
        TEXTURE_QUERIES.put(GL30C.GL_TEXTURE_1D_ARRAY, GL30C.GL_TEXTURE_BINDING_1D_ARRAY);
        TEXTURE_QUERIES.put(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_BINDING_2D);
        TEXTURE_QUERIES.put(GL30C.GL_TEXTURE_2D_ARRAY, GL30C.GL_TEXTURE_BINDING_2D_ARRAY);
        TEXTURE_QUERIES.put(GL32C.GL_TEXTURE_2D_MULTISAMPLE, GL32C.GL_TEXTURE_BINDING_2D_MULTISAMPLE);
        TEXTURE_QUERIES.put(GL32C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY, GL32C.GL_TEXTURE_BINDING_2D_MULTISAMPLE_ARRAY);
        TEXTURE_QUERIES.put(GL12C.GL_TEXTURE_3D, GL12C.GL_TEXTURE_BINDING_3D);
        TEXTURE_QUERIES.put(GL13C.GL_TEXTURE_CUBE_MAP, GL13C.GL_TEXTURE_BINDING_CUBE_MAP);
        TEXTURE_QUERIES.put(GL40C.GL_TEXTURE_CUBE_MAP_ARRAY, GL40C.GL_TEXTURE_BINDING_CUBE_MAP_ARRAY);
        TEXTURE_QUERIES.put(GL31C.GL_TEXTURE_BUFFER, GL31C.GL_TEXTURE_BINDING_BUFFER);

        TEXTURE_FORMATS.put(GL11C.GL_RGBA8, GL11C.GL_RGBA);
        TEXTURE_FORMATS.put(GL11C.GL_RGB8, GL11C.GL_RGB);
        TEXTURE_FORMATS.put(GL30C.GL_RG8, GL30C.GL_RG);
        TEXTURE_FORMATS.put(GL30C.GL_R8, GL11C.GL_RED);
        TEXTURE_FORMATS.put(GL30C.GL_DEPTH_COMPONENT32F, GL11C.GL_DEPTH_COMPONENT);
        TEXTURE_FORMATS.put(GL14C.GL_DEPTH_COMPONENT32, GL11C.GL_DEPTH_COMPONENT);
        TEXTURE_FORMATS.put(GL14C.GL_DEPTH_COMPONENT24, GL11C.GL_DEPTH_COMPONENT);
        TEXTURE_FORMATS.put(GL14C.GL_DEPTH_COMPONENT16, GL11C.GL_DEPTH_COMPONENT);
        TEXTURE_FORMATS.put(GL30C.GL_DEPTH32F_STENCIL8, GL30C.GL_DEPTH_STENCIL);
        TEXTURE_FORMATS.put(GL30C.GL_DEPTH24_STENCIL8, GL30C.GL_DEPTH_STENCIL);

        TEXTURE_TYPES.put(GL11C.GL_RGBA8, GL11C.GL_UNSIGNED_BYTE);
        TEXTURE_TYPES.put(GL11C.GL_RGB8, GL11C.GL_UNSIGNED_BYTE);
        TEXTURE_TYPES.put(GL30C.GL_RG8, GL11C.GL_UNSIGNED_BYTE);
        TEXTURE_TYPES.put(GL30C.GL_R8, GL11C.GL_UNSIGNED_BYTE);
        TEXTURE_TYPES.put(GL30C.GL_DEPTH_COMPONENT32F, GL11C.GL_FLOAT);
        TEXTURE_TYPES.put(GL14C.GL_DEPTH_COMPONENT32, GL11C.GL_UNSIGNED_INT);
        TEXTURE_TYPES.put(GL14C.GL_DEPTH_COMPONENT24, GL11C.GL_UNSIGNED_INT);
        TEXTURE_TYPES.put(GL14C.GL_DEPTH_COMPONENT16, GL11C.GL_UNSIGNED_SHORT);
        TEXTURE_TYPES.put(GL30C.GL_DEPTH32F_STENCIL8, GL30C.GL_FLOAT_32_UNSIGNED_INT_24_8_REV);
        TEXTURE_TYPES.put(GL30C.GL_DEPTH24_STENCIL8, GL30C.GL_UNSIGNED_INT_24_8);

        DEBUG_LABEL_OBJECT_TYPE_MAP.put(GL43C.GL_BUFFER, EXTDebugLabel.GL_BUFFER_OBJECT_EXT);
        DEBUG_LABEL_OBJECT_TYPE_MAP.put(GL43C.GL_SHADER, EXTDebugLabel.GL_SHADER_OBJECT_EXT);
        DEBUG_LABEL_OBJECT_TYPE_MAP.put(GL43C.GL_PROGRAM, EXTDebugLabel.GL_PROGRAM_OBJECT_EXT);
        DEBUG_LABEL_OBJECT_TYPE_MAP.put(GL11C.GL_VERTEX_ARRAY, EXTDebugLabel.GL_VERTEX_ARRAY_OBJECT_EXT);
        DEBUG_LABEL_OBJECT_TYPE_MAP.put(GL43C.GL_QUERY, EXTDebugLabel.GL_QUERY_OBJECT_EXT);
        DEBUG_LABEL_OBJECT_TYPE_MAP.put(GL43C.GL_PROGRAM_PIPELINE, EXTDebugLabel.GL_PROGRAM_PIPELINE_OBJECT_EXT);
    }

    private static int getTextureQuery(final int target) {
        final int query = TEXTURE_QUERIES.get(target);
        if (query != 0) {
            return query;
        } else {
            throw new IllegalArgumentException("Unknown texture target: " + target);
        }
    }

    private static int getTextureFormat(final int internalFormat) {
        final int format = TEXTURE_FORMATS.get(internalFormat);
        if (format != 0) {
            return format;
        } else {
            throw new IllegalArgumentException("Unknown texture internal format: " + internalFormat);
        }
    }

    private static int getTextureType(final int internalFormat) {
        final int type = TEXTURE_TYPES.get(internalFormat);
        if (type != 0) {
            return type;
        } else {
            throw new IllegalArgumentException("Unknown texture internal format: " + internalFormat);
        }
    }

    private final GLCapabilities capabilities = GL.getCapabilities();
    private final Int2IntMap textureTargets = new Int2IntOpenHashMap();
    private final Int2ObjectMap<VertexArrayObject> vertexArrayObjects = new Int2ObjectOpenHashMap<>();

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
        GL15C.glDeleteQueries(id);
    }

    @Override
    public void endQuery(final int target) {
        GL15C.glEndQuery(target);
    }

    @Override
    public int getQueryObjecti(final int id, final int pname) {
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
    public void shaderSource(final int shader, final CharSequence string) {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawArraysInstancedBaseInstance(final int mode, final int first, final int count, final int primcount, final int baseinstance) {
        if (baseinstance == 0) {
            GL31C.glDrawArraysInstanced(mode, first, count, primcount);
        } else {
            throw new UnsupportedOperationException("Non-zero base instance is not supported");
        }
    }

    @Override
    public void drawElementsInstancedBaseVertexBaseInstance(final int mode, final int count, final int type, final long indices, final int primcount, final int basevertex, final int baseinstance) {
        if (baseinstance == 0) {
            GL32C.glDrawElementsInstancedBaseVertex(mode, count, type, indices, primcount, basevertex);
        } else {
            throw new UnsupportedOperationException("Non-zero base instance is not supported");
        }
    }

    @Override
    public void copyImageSubData(final int srcName, final int srcTarget, final int srcLevel, final int srcX, final int srcY, final int srcZ, final int dstName, final int dstTarget, final int dstLevel, final int dstX, final int dstY, final int dstZ, final int srcWidth, final int srcHeight, final int srcDepth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getObjectLabel(final int identifier, final int name) {
        if (capabilities.GL_EXT_debug_label) {
            return EXTDebugLabel.glGetObjectLabelEXT(DEBUG_LABEL_OBJECT_TYPE_MAP.getOrDefault(identifier, identifier), name, 255);
        } else {
            return ""; // no op
        }
    }

    @Override
    public int getProgramResourceIndex(final int program, final int programInterface, final CharSequence name) {
        final int blockCount = this.getProgrami(program, GL31C.GL_ACTIVE_UNIFORM_BLOCKS);
        for (int i = 0; i < blockCount; i++) {
            final String blockName = GL31C.glGetActiveUniformBlockName(program, i);
            if (blockName.contentEquals(name)) {
                return i;
            }
        }
        return GL31C.GL_INVALID_INDEX;
    }

    @Override
    public void multiDrawArraysIndirect(final int mode, long indirect, final int drawcount, final int stride) {
        for (int i = 0; i < drawcount; i++) {
            this.drawArraysIndirect(mode, indirect);
            if (stride == 0) {
                indirect += DrawArraysCommand.BYTES;
            } else {
                indirect += stride;
            }
        }
    }

    @Override
    public void multiDrawElementsIndirect(final int mode, final int type, long indirect, final int drawcount, final int stride) {
        for (int i = 0; i < drawcount; i++) {
            this.drawElementsIndirect(mode, type, indirect);
            if (stride == 0) {
                indirect += DrawElementsCommand.BYTES;
            } else {
                indirect += stride;
            }
        }
    }

    @Override
    public void objectLabel(final int identifier, final int name, final CharSequence label) {
        if (capabilities.GL_EXT_debug_label) {
            EXTDebugLabel.glLabelObjectEXT(DEBUG_LABEL_OBJECT_TYPE_MAP.getOrDefault(identifier, identifier), name, label);
        } else {
            // no op
        }
    }

    @Override
    public void shaderStorageBlockBinding(final int program, final int storageBlockIndex, final int storageBlockBinding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bindSamplers(final int first, final int[] samplers) {
        for (int i = 0; i < samplers.length; i++) {
            this.bindSampler(first + i, samplers[i]);
        }
    }

    @Override
    public void bindTextures(final int first, final int[] textures) {
        for (int i = 0; i < textures.length; i++) {
            this.bindTextureUnit(first + i, textures[i]);
        }
    }

    @Override
    public void clearTexImage(final int texture, final int level, final int format, final int type, final float[] data) {
        final int textureWidth = this.getTextureLevelParameteri(texture, level, GL11C.GL_TEXTURE_WIDTH);
        final int textureHeight = this.getTextureLevelParameteri(texture, level, GL11C.GL_TEXTURE_HEIGHT);
        final int textureDepth = this.getTextureLevelParameteri(texture, level, GL12C.GL_TEXTURE_DEPTH);
        this.clearTexSubImage(texture, level, 0, 0, 0, textureWidth, textureHeight, textureDepth, format, type, data);
    }

    @Override
    public void clearTexSubImage(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final float[] data) {
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

    @Override
    public void bindTextureUnit(final int unit, final int texture) {
        final int previousActiveTexture = this.getInteger(GL13C.GL_ACTIVE_TEXTURE);
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + unit);
        if (texture != 0) {
            GL11C.glBindTexture(this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), texture);
        } else {
            for (int target : TEXTURE_QUERIES.keySet()) {
                GL11C.glBindTexture(target, 0);
            }
        }
        GL13C.glActiveTexture(previousActiveTexture);
    }

    @Override
    public void blitNamedFramebuffer(final int readFramebuffer, final int drawFramebuffer, final int srcX0, final int srcY0, final int srcX1, final int srcY1, final int dstX0, final int dstY0, final int dstX1, final int dstY1, final int mask, final int filter) {
        final int previousReadFramebuffer = this.getInteger(GL30C.GL_READ_FRAMEBUFFER_BINDING);
        final int previousDrawFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
        GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, readFramebuffer);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
        GL30C.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
        GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, previousReadFramebuffer);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousDrawFramebuffer);
    }

    @Override
    public int checkNamedFramebufferStatus(final int framebuffer, final int target) {
        final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
        final int status = GL30C.glCheckFramebufferStatus(target);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
        return status;
    }

    @Override
    public void clearNamedFramebufferfi(final int framebuffer, final int buffer, final int drawbuffer, final float depth, final int stencil) {
        final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
        GL30C.glClearBufferfi(buffer, drawbuffer, depth, stencil);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
    }

    @Override
    public void clearNamedFramebufferfv(final int framebuffer, final int buffer, final int drawbuffer, final float[] value) {
        final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
        GL30C.glClearBufferfv(buffer, drawbuffer, value);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
    }

    @Override
    public void clearNamedFramebufferiv(final int framebuffer, final int buffer, final int drawbuffer, final int[] value) {
        final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
        GL30C.glClearBufferiv(buffer, drawbuffer, value);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
    }

    @Override
    public void copyNamedBufferSubData(final int readBuffer, final int writeBuffer, final long readOffset, final long writeOffset, final long size) {
        final int previousReadBuffer = this.getInteger(GL31C.GL_COPY_READ_BUFFER);
        final int previousWriteBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_READ_BUFFER, readBuffer);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, writeBuffer);
        GL31C.glCopyBufferSubData(GL31C.GL_COPY_READ_BUFFER, GL31C.GL_COPY_WRITE_BUFFER, readOffset, writeOffset, size);
        GL15C.glBindBuffer(GL31C.GL_COPY_READ_BUFFER, previousReadBuffer);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousWriteBuffer);
    }

    @Override
    public int createBuffers() {
        return GL15C.glGenBuffers();
    }

    @Override
    public int createFramebuffers() {
        return GL30C.glGenFramebuffers();
    }

    @Override
    public int createQueries(final int target) {
        return GL15C.glGenQueries();
    }

    @Override
    public int createRenderbuffers() {
        return GL30C.glGenRenderbuffers();
    }

    @Override
    public int createTextures(final int target) {
        final int texture = GL11C.glGenTextures();
        this.textureTargets.put(texture, target);
        return texture;
    }

    @Override
    public int createVertexArrays() {
        return GL30C.glGenVertexArrays();
    }

    @Override
    public void enableVertexArrayAttrib(final int vaobj, final int index) {
        final int previousVertexArray = this.getInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
        this.bindVertexArray(vaobj);
        GL20C.glEnableVertexAttribArray(index);
        this.bindVertexArray(previousVertexArray);
    }

    @Override
    public void flushMappedNamedBufferRange(final int buffer, final long offset, final long length) {
        final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
        GL30C.glFlushMappedBufferRange(GL31C.GL_COPY_WRITE_BUFFER, offset, length);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
    }

    @Override
    public void generateTextureMipmap(final int texture) {
        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        GL30C.glGenerateMipmap(target);
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public int getNamedBufferParameteri(final int buffer, final int pname) {
        final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
        try {
            return GL15C.glGetBufferParameteri(GL31C.GL_COPY_WRITE_BUFFER, pname);
        } finally {
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public long getNamedBufferParameteri64(final int buffer, final int pname) {
        final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
        try {
            return GL32C.glGetBufferParameteri64(GL31C.GL_COPY_WRITE_BUFFER, pname);
        } finally {
            GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public void getNamedBufferSubData(final int buffer, final long offset, final long size, final long data) {
        final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
        GL15C.nglGetBufferSubData(GL31C.GL_COPY_WRITE_BUFFER, offset, size, data);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
    }

    @Override
    public int getNamedFramebufferAttachmentParameteri(final int framebuffer, final int attachment, final int pname) {
        final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
        final int parameter = GL30C.glGetFramebufferAttachmentParameteri(GL30C.GL_DRAW_FRAMEBUFFER, attachment, pname);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
        return parameter;
    }

    @Override
    public int getNamedRenderbufferParameteri(final int renderbuffer, final int pname) {
        final int previousRenderbuffer = this.getInteger(GL30C.GL_RENDERBUFFER_BINDING);
        GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, renderbuffer);
        final int parameter = GL30C.glGetRenderbufferParameteri(GL30C.GL_RENDERBUFFER, pname);
        GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, previousRenderbuffer);
        return parameter;
    }

    @Override
    public int getTextureLevelParameteri(final int texture, final int level, final int pname) {
        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        final int parameter = GL11C.glGetTexLevelParameteri(target, level, pname);
        GL11C.glBindTexture(target, previousTexture);
        return parameter;
    }

    @Override
    public float getTextureParameterf(final int texture, final int pname) {
        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        final float parameter = GL11C.glGetTexParameterf(target, pname);
        GL11C.glBindTexture(target, previousTexture);
        return parameter;
    }

    @Override
    public void getTextureParameterfv(final int texture, final int pname, final float[] params) {
        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        GL11C.glGetTexParameterfv(target, pname, params);
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public int getTextureParameteri(final int texture, final int pname) {
        if (pname == GL45C.GL_TEXTURE_TARGET) {
            return this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
        }

        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        final int parameter = GL11C.glGetTexParameteri(target, pname);
        GL11C.glBindTexture(target, previousTexture);
        return parameter;
    }

    @Override
    public void getTextureParameteriv(final int texture, final int pname, final int[] params) {
        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        GL11C.glGetTexParameteriv(target, pname, params);
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public void getTextureSubImage(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final int bufSize, final long pixels) {
        final int textureWidth = this.getTextureLevelParameteri(texture, level, GL11C.GL_TEXTURE_WIDTH);
        final int textureHeight = this.getTextureLevelParameteri(texture, level, GL11C.GL_TEXTURE_HEIGHT);
        final int textureDepth = this.getTextureLevelParameteri(texture, level, GL12C.GL_TEXTURE_DEPTH);
        if (xoffset == 0 && yoffset == 0 && zoffset == 0 && width == textureWidth && height == textureHeight && depth == textureDepth) {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glGetTexImage(target, level, format, type, pixels);
            GL11C.glBindTexture(target, previousTexture);
        } else {
            throw new UnsupportedOperationException("Partial texture reads are not supported");
        }
    }

    @Override
    public long mapNamedBuffer(final int buffer, final int access) {
        final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
        final long address = GL15C.nglMapBuffer(GL31C.GL_COPY_WRITE_BUFFER, access);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        return address;
    }

    @Override
    public long mapNamedBufferRange(final int buffer, final long offset, final long length, final int access) {
        final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
        final long address = GL30C.nglMapBufferRange(GL31C.GL_COPY_WRITE_BUFFER, offset, length, access);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        return address;
    }

    @Override
    public void namedBufferData(final int buffer, final long size, final int usage) {
        final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
        GL15C.glBufferData(GL31C.GL_COPY_WRITE_BUFFER, size, usage);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
    }

    @Override
    public void namedBufferData(final int buffer, final long size, final long data, final int usage) {
        final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
        GL15C.nglBufferData(GL31C.GL_COPY_WRITE_BUFFER, size, data, usage);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
    }

    @Override
    public void namedBufferStorage(final int buffer, final long size, final int flags) {
        final int usage;
        if ((flags & GL44C.GL_DYNAMIC_STORAGE_BIT) != 0) {
            usage = GL15C.GL_DYNAMIC_DRAW;
        } else {
            usage = GL15C.GL_STATIC_DRAW;
        }

        final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
        GL15C.glBufferData(GL31C.GL_COPY_WRITE_BUFFER, size, usage);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
    }

    @Override
    public void namedBufferStorage(final int buffer, final long size, final long data, final int flags) {
        final int usage;
        if ((flags & GL44C.GL_DYNAMIC_STORAGE_BIT) != 0) {
            usage = GL15C.GL_DYNAMIC_DRAW;
        } else {
            usage = GL15C.GL_STATIC_DRAW;
        }

        final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
        GL15C.nglBufferData(GL31C.GL_COPY_WRITE_BUFFER, size, data, usage);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
    }

    @Override
    public void namedBufferSubData(final int buffer, final long offset, final long size, final long data) {
        final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
        GL15C.nglBufferSubData(GL31C.GL_COPY_WRITE_BUFFER, offset, size, data);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
    }

    @Override
    public void namedFramebufferRenderbuffer(final int framebuffer, final int attachment, final int renderbuffertarget, final int renderbuffer) {
        final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
        GL30C.glFramebufferRenderbuffer(GL30C.GL_DRAW_FRAMEBUFFER, attachment, renderbuffertarget, renderbuffer);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
    }

    @Override
    public void namedFramebufferTexture(final int framebuffer, final int attachment, final int texture, final int level) {
        final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
        GL30C.glFramebufferTexture2D(GL30C.GL_DRAW_FRAMEBUFFER, attachment, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), texture, level);
        GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
    }

    @Override
    public void namedRenderbufferStorage(final int renderbuffer, final int internalformat, final int width, final int height) {
        final int previousRenderbuffer = this.getInteger(GL30C.GL_RENDERBUFFER_BINDING);
        GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, renderbuffer);
        GL30C.glRenderbufferStorage(GL30C.GL_RENDERBUFFER, internalformat, width, height);
        GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, previousRenderbuffer);
    }

    @Override
    public void namedRenderbufferStorageMultisample(final int renderbuffer, final int samples, final int internalformat, final int width, final int height) {
        final int previousRenderbuffer = this.getInteger(GL30C.GL_RENDERBUFFER_BINDING);
        GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, renderbuffer);
        GL30C.glRenderbufferStorageMultisample(GL30C.GL_RENDERBUFFER, samples, internalformat, width, height);
        GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, previousRenderbuffer);
    }

    @Override
    public void textureBuffer(final int texture, final int internalformat, final int buffer) {
        final int target = this.textureTargets.getOrDefault(texture, GL31C.GL_TEXTURE_BUFFER);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        GL31C.glTexBuffer(target, internalformat, buffer);
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public void textureParameterf(final int texture, final int pname, final float param) {
        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        GL11C.glTexParameterf(target, pname, param);
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public void textureParameterfv(final int texture, final int pname, final float[] params) {
        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        GL11C.glTexParameterfv(target, pname, params);
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public void textureParameteri(final int texture, final int pname, final int param) {
        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        GL11C.glTexParameteri(target, pname, param);
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public void textureParameteriv(final int texture, final int pname, final int[] params) {
        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        GL11C.glTexParameteriv(target, pname, params);
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public void textureStorage1D(final int texture, final int levels, final int internalformat, final int width) {
        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_1D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        if (this.capabilities.GL_ARB_texture_storage) {
            ARBTextureStorage.glTexStorage1D(target, levels, internalformat, width);
        } else {
            throw new UnsupportedOperationException("GL_ARB_texture_storage is not supported");
        }
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public void textureStorage2D(final int texture, final int levels, final int internalformat, final int width, final int height) {
        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        if (this.capabilities.GL_ARB_texture_storage) {
            ARBTextureStorage.glTexStorage2D(target, levels, internalformat, width, height);
        } else {
            throw new UnsupportedOperationException("GL_ARB_texture_storage is not supported");
        }
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public void textureStorage2DMultisample(final int texture, final int samples, final int internalformat, final int width, final int height, final boolean fixedsamplelocations) {
        final int target = this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        GL32C.glTexImage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public void textureStorage3D(final int texture, final int levels, final int internalformat, final int width, final int height, final int depth) {
        final int target = this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        if (this.capabilities.GL_ARB_texture_storage) {
            ARBTextureStorage.glTexStorage3D(target, levels, internalformat, width, height, depth);
        } else {
            throw new UnsupportedOperationException("GL_ARB_texture_storage is not supported");
        }
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public void textureStorage3DMultisample(final int texture, final int samples, final int internalformat, final int width, final int height, final int depth, final boolean fixedsamplelocations) {
        final int target = this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        GL32C.glTexImage3DMultisample(target, samples, internalformat, width, height, depth, fixedsamplelocations);
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public void textureSubImage1D(final int texture, final int level, final int xoffset, final int width, final int format, final int type, final long pixels) {
        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_1D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        GL11C.nglTexSubImage1D(target, level, xoffset, width, format, type, pixels);
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public void textureSubImage2D(final int texture, final int level, final int xoffset, final int yoffset, final int width, final int height, final int format, final int type, final long pixels) {
        final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        GL11C.nglTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public void textureSubImage3D(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final long pixels) {
        final int target = this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D);
        final int previousTexture = this.getInteger(getTextureQuery(target));
        GL11C.glBindTexture(target, texture);
        GL12C.nglTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
        GL11C.glBindTexture(target, previousTexture);
    }

    @Override
    public boolean unmapNamedBuffer(final int buffer) {
        final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
        final boolean result = GL15C.glUnmapBuffer(GL31C.GL_COPY_WRITE_BUFFER);
        GL15C.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        return result;
    }

    @Override
    public void vertexArrayAttribBinding(final int vaobj, final int attribindex, final int bindingindex) {
        final VertexArrayObject vertexArrayObject = this.vertexArrayObjects.computeIfAbsent(vaobj, VertexArrayObject::new);
        final VertexArrayObject.VertexAttribute vertexAttribute = vertexArrayObject.attributes.computeIfAbsent(attribindex, VertexArrayObject.VertexAttribute::new);
        vertexAttribute.bindingindex = bindingindex;
        vertexArrayObject.applyAttribute(attribindex);
    }

    @Override
    public void vertexArrayAttribFormat(final int vaobj, final int attribindex, final int size, final int type, final boolean normalized, final int relativeoffset) {
        final VertexArrayObject vertexArrayObject = this.vertexArrayObjects.computeIfAbsent(vaobj, VertexArrayObject::new);
        final VertexArrayObject.VertexAttribute vertexAttribute = vertexArrayObject.attributes.computeIfAbsent(attribindex, VertexArrayObject.VertexAttribute::new);
        vertexAttribute.format = new VertexArrayObject.VertexAttribFFormat(size, type, normalized, relativeoffset);
        vertexArrayObject.applyAttribute(attribindex);
    }

    @Override
    public void vertexArrayAttribIFormat(final int vaobj, final int attribindex, final int size, final int type, final int relativeoffset) {
        final VertexArrayObject vertexArrayObject = this.vertexArrayObjects.computeIfAbsent(vaobj, VertexArrayObject::new);
        final VertexArrayObject.VertexAttribute vertexAttribute = vertexArrayObject.attributes.computeIfAbsent(attribindex, VertexArrayObject.VertexAttribute::new);
        vertexAttribute.format = new VertexArrayObject.VertexAttribIFormat(size, type, relativeoffset);
        vertexArrayObject.applyAttribute(attribindex);
    }

    @Override
    public void vertexArrayAttribLFormat(final int vaobj, final int attribindex, final int size, final int type, final int relativeoffset) {
        final VertexArrayObject vertexArrayObject = this.vertexArrayObjects.computeIfAbsent(vaobj, VertexArrayObject::new);
        final VertexArrayObject.VertexAttribute vertexAttribute = vertexArrayObject.attributes.computeIfAbsent(attribindex, VertexArrayObject.VertexAttribute::new);
        vertexAttribute.format = new VertexArrayObject.VertexAttribLFormat(size, type, relativeoffset);
        vertexArrayObject.applyAttribute(attribindex);
    }

    @Override
    public void vertexArrayBindingDivisor(final int vaobj, final int bindingindex, final int divisor) {
        final VertexArrayObject vertexArrayObject = this.vertexArrayObjects.computeIfAbsent(vaobj, VertexArrayObject::new);
        final VertexArrayObject.VertexBufferBinding vertexBufferBinding = vertexArrayObject.vertexBufferBinding.computeIfAbsent(bindingindex, VertexArrayObject.VertexBufferBinding::new);
        vertexBufferBinding.divisor = divisor;
        vertexArrayObject.applyVertexBufferBinding(bindingindex);
    }

    @Override
    public void vertexArrayElementBuffer(final int vaobj, final int buffer) {
        final int previousVertexArray = this.getInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
        this.bindVertexArray(vaobj);
        GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, buffer);
        this.bindVertexArray(previousVertexArray);
    }

    @Override
    public void vertexArrayVertexBuffer(final int vaobj, final int bindingindex, final int buffer, final long offset, final int stride) {
        final VertexArrayObject vertexArrayObject = this.vertexArrayObjects.computeIfAbsent(vaobj, VertexArrayObject::new);
        final VertexArrayObject.VertexBufferBinding vertexBufferBinding = vertexArrayObject.vertexBufferBinding.computeIfAbsent(bindingindex, VertexArrayObject.VertexBufferBinding::new);
        vertexBufferBinding.buffer = buffer;
        vertexBufferBinding.offset = offset;
        vertexBufferBinding.stride = stride;
        vertexArrayObject.applyVertexBufferBinding(bindingindex);
    }

    public void putTextureTarget(final int texture, final int target) {
        this.textureTargets.put(texture, target);
    }

    private class VertexArrayObject {

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

            final int previousVertexArray = GL41Backend.this.getInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            final int previousBuffer = GL41Backend.this.getInteger(GL15C.GL_ARRAY_BUFFER_BINDING);
            GL41Backend.this.bindVertexArray(this.id);
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
            GL41Backend.this.bindVertexArray(previousVertexArray);
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

}
