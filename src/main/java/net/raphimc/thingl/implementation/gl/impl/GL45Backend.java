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

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.gl.GLBackend;
import org.lwjgl.opengl.*;

public class GL45Backend implements GLBackend {

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
        GL42C.glBindImageTexture(unit, texture, level, layered, layer, access, format);
    }

    @Override
    public void drawArraysInstancedBaseInstance(final int mode, final int first, final int count, final int primcount, final int baseinstance) {
        GL42C.glDrawArraysInstancedBaseInstance(mode, first, count, primcount, baseinstance);
    }

    @Override
    public void drawElementsInstancedBaseVertexBaseInstance(final int mode, final int count, final int type, final long indices, final int primcount, final int basevertex, final int baseinstance) {
        GL42C.glDrawElementsInstancedBaseVertexBaseInstance(mode, count, type, indices, primcount, basevertex, baseinstance);
    }

    @Override
    public void copyImageSubData(final int srcName, final int srcTarget, final int srcLevel, final int srcX, final int srcY, final int srcZ, final int dstName, final int dstTarget, final int dstLevel, final int dstX, final int dstY, final int dstZ, final int srcWidth, final int srcHeight, final int srcDepth) {
        GL43C.glCopyImageSubData(srcName, srcTarget, srcLevel, srcX, srcY, srcZ, dstName, dstTarget, dstLevel, dstX, dstY, dstZ, srcWidth, srcHeight, srcDepth);
    }

    @Override
    public String getObjectLabel(final int identifier, final int name) {
        return GL43C.glGetObjectLabel(identifier, name);
    }

    @Override
    public int getProgramResourceIndex(final int program, final int programInterface, final CharSequence name) {
        return GL43C.glGetProgramResourceIndex(program, programInterface, name);
    }

    @Override
    public void multiDrawArraysIndirect(final int mode, final long indirect, final int drawcount, final int stride) {
        GL43C.glMultiDrawArraysIndirect(mode, indirect, drawcount, stride);
    }

    @Override
    public void multiDrawElementsIndirect(final int mode, final int type, final long indirect, final int drawcount, final int stride) {
        GL43C.glMultiDrawElementsIndirect(mode, type, indirect, drawcount, stride);
    }

    @Override
    public void objectLabel(final int identifier, final int name, final CharSequence label) {
        GL43C.glObjectLabel(identifier, name, label);
    }

    @Override
    public void shaderStorageBlockBinding(final int program, final int storageBlockIndex, final int storageBlockBinding) {
        GL43C.glShaderStorageBlockBinding(program, storageBlockIndex, storageBlockBinding);
    }

    @Override
    public void bindSamplers(final int first, final int[] samplers) {
        GL44C.glBindSamplers(first, samplers);
    }

    @Override
    public void bindTextures(final int first, final int[] textures) {
        GL44C.glBindTextures(first, textures);
    }

    @Override
    public void clearTexImage(final int texture, final int level, final int format, final int type, final float[] data) {
        GL44C.glClearTexImage(texture, level, format, type, data);
    }

    @Override
    public void clearTexSubImage(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final float[] data) {
        GL44C.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    @Override
    public void bindTextureUnit(final int unit, final int texture) {
        GL45C.glBindTextureUnit(unit, texture);
    }

    @Override
    public void blitNamedFramebuffer(final int readFramebuffer, final int drawFramebuffer, final int srcX0, final int srcY0, final int srcX1, final int srcY1, final int dstX0, final int dstY0, final int dstX1, final int dstY1, final int mask, final int filter) {
        GL45C.glBlitNamedFramebuffer(readFramebuffer, drawFramebuffer, srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    @Override
    public int checkNamedFramebufferStatus(final int framebuffer, final int target) {
        return GL45C.glCheckNamedFramebufferStatus(framebuffer, target);
    }

    @Override
    public void clearNamedFramebufferfi(final int framebuffer, final int buffer, final int drawbuffer, final float depth, final int stencil) {
        GL45C.glClearNamedFramebufferfi(framebuffer, buffer, drawbuffer, depth, stencil);
    }

    @Override
    public void clearNamedFramebufferfv(final int framebuffer, final int buffer, final int drawbuffer, final float[] value) {
        GL45C.glClearNamedFramebufferfv(framebuffer, buffer, drawbuffer, value);
    }

    @Override
    public void clearNamedFramebufferiv(final int framebuffer, final int buffer, final int drawbuffer, final int[] value) {
        GL45C.glClearNamedFramebufferiv(framebuffer, buffer, drawbuffer, value);
    }

    @Override
    public void copyNamedBufferSubData(final int readBuffer, final int writeBuffer, final long readOffset, final long writeOffset, final long size) {
        GL45C.glCopyNamedBufferSubData(readBuffer, writeBuffer, readOffset, writeOffset, size);
    }

    @Override
    public int createBuffers() {
        return GL45C.glCreateBuffers();
    }

    @Override
    public int createFramebuffers() {
        return GL45C.glCreateFramebuffers();
    }

    @Override
    public int createQueries(final int target) {
        return GL45C.glCreateQueries(target);
    }

    @Override
    public int createRenderbuffers() {
        return GL45C.glCreateRenderbuffers();
    }

    @Override
    public int createTextures(final int target) {
        return GL45C.glCreateTextures(target);
    }

    @Override
    public int createVertexArrays() {
        return GL45C.glCreateVertexArrays();
    }

    @Override
    public void enableVertexArrayAttrib(final int vaobj, final int index) {
        GL45C.glEnableVertexArrayAttrib(vaobj, index);
    }

    @Override
    public void flushMappedNamedBufferRange(final int buffer, final long offset, final long length) {
        GL45C.glFlushMappedNamedBufferRange(buffer, offset, length);
    }

    @Override
    public void generateTextureMipmap(final int texture) {
        GL45C.glGenerateTextureMipmap(texture);
    }

    @Override
    public int getNamedBufferParameteri(final int buffer, final int pname) {
        return GL45C.glGetNamedBufferParameteri(buffer, pname);
    }

    @Override
    public long getNamedBufferParameteri64(final int buffer, final int pname) {
        return GL45C.glGetNamedBufferParameteri64(buffer, pname);
    }

    @Override
    public void getNamedBufferSubData(final int buffer, final long offset, final long size, final long data) {
        GL45C.nglGetNamedBufferSubData(buffer, offset, size, data);
    }

    @Override
    public int getNamedFramebufferAttachmentParameteri(final int framebuffer, final int attachment, final int pname) {
        return GL45C.glGetNamedFramebufferAttachmentParameteri(framebuffer, attachment, pname);
    }

    @Override
    public int getNamedRenderbufferParameteri(final int renderbuffer, final int pname) {
        return GL45C.glGetNamedRenderbufferParameteri(renderbuffer, pname);
    }

    @Override
    public int getTextureLevelParameteri(final int texture, final int level, final int pname) {
        return GL45C.glGetTextureLevelParameteri(texture, level, pname);
    }

    @Override
    public float getTextureParameterf(final int texture, final int pname) {
        return GL45C.glGetTextureParameterf(texture, pname);
    }

    @Override
    public void getTextureParameterfv(final int texture, final int pname, final float[] params) {
        GL45C.glGetTextureParameterfv(texture, pname, params);
    }

    @Override
    public int getTextureParameteri(final int texture, final int pname) {
        if (pname == GL45C.GL_TEXTURE_TARGET && ThinGL.workarounds().isGetTextureParameterTextureTargetBroken()) {
            final int depth = GL45C.glGetTextureLevelParameteri(texture, 0, GL12C.GL_TEXTURE_DEPTH);
            final int samples = GL45C.glGetTextureLevelParameteri(texture, 0, GL32C.GL_TEXTURE_SAMPLES);
            if (samples == 0) {
                if (depth > 1) {
                    return GL12C.GL_TEXTURE_3D;
                } else {
                    return GL11C.GL_TEXTURE_2D;
                }
            } else {
                if (depth > 1) {
                    return GL32C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY;
                } else {
                    return GL32C.GL_TEXTURE_2D_MULTISAMPLE;
                }
            }
        }

        return GL45C.glGetTextureParameteri(texture, pname);
    }

    @Override
    public void getTextureParameteriv(final int texture, final int pname, final int[] params) {
        GL45C.glGetTextureParameteriv(texture, pname, params);
    }

    @Override
    public void getTextureSubImage(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final int bufSize, final long pixels) {
        GL45C.nglGetTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, bufSize, pixels);
    }

    @Override
    public long mapNamedBuffer(final int buffer, final int access) {
        return GL45C.nglMapNamedBuffer(buffer, access);
    }

    @Override
    public long mapNamedBufferRange(final int buffer, final long offset, final long length, final int access) {
        return GL45C.nglMapNamedBufferRange(buffer, offset, length, access);
    }

    @Override
    public void namedBufferData(final int buffer, final long size, final int usage) {
        GL45C.glNamedBufferData(buffer, size, usage);
    }

    @Override
    public void namedBufferData(final int buffer, final long size, final long data, final int usage) {
        GL45C.nglNamedBufferData(buffer, size, data, usage);
    }

    @Override
    public void namedBufferStorage(final int buffer, final long size, final int flags) {
        GL45C.glNamedBufferStorage(buffer, size, flags);
    }

    @Override
    public void namedBufferStorage(final int buffer, final long size, final long data, final int flags) {
        GL45C.nglNamedBufferStorage(buffer, size, data, flags);
    }

    @Override
    public void namedBufferSubData(final int buffer, final long offset, final long size, final long data) {
        GL45C.nglNamedBufferSubData(buffer, offset, size, data);
    }

    @Override
    public void namedFramebufferRenderbuffer(final int framebuffer, final int attachment, final int renderbuffertarget, final int renderbuffer) {
        GL45C.glNamedFramebufferRenderbuffer(framebuffer, attachment, renderbuffertarget, renderbuffer);
    }

    @Override
    public void namedFramebufferTexture(final int framebuffer, final int attachment, final int texture, final int level) {
        GL45C.glNamedFramebufferTexture(framebuffer, attachment, texture, level);
    }

    @Override
    public void namedRenderbufferStorage(final int renderbuffer, final int internalformat, final int width, final int height) {
        GL45C.glNamedRenderbufferStorage(renderbuffer, internalformat, width, height);
    }

    @Override
    public void namedRenderbufferStorageMultisample(final int renderbuffer, final int samples, final int internalformat, final int width, final int height) {
        GL45C.glNamedRenderbufferStorageMultisample(renderbuffer, samples, internalformat, width, height);
    }

    @Override
    public void textureBuffer(final int texture, final int internalformat, final int buffer) {
        GL45C.glTextureBuffer(texture, internalformat, buffer);
    }

    @Override
    public void textureParameterf(final int texture, final int pname, final float param) {
        GL45C.glTextureParameterf(texture, pname, param);
    }

    @Override
    public void textureParameterfv(final int texture, final int pname, final float[] params) {
        GL45C.glTextureParameterfv(texture, pname, params);
    }

    @Override
    public void textureParameteri(final int texture, final int pname, final int param) {
        GL45C.glTextureParameteri(texture, pname, param);
    }

    @Override
    public void textureParameteriv(final int texture, final int pname, final int[] params) {
        GL45C.glTextureParameteriv(texture, pname, params);
    }

    @Override
    public void textureStorage1D(final int texture, final int levels, final int internalformat, final int width) {
        GL45C.glTextureStorage1D(texture, levels, internalformat, width);
    }

    @Override
    public void textureStorage2D(final int texture, final int levels, final int internalformat, final int width, final int height) {
        GL45C.glTextureStorage2D(texture, levels, internalformat, width, height);
    }

    @Override
    public void textureStorage2DMultisample(final int texture, final int samples, final int internalformat, final int width, final int height, final boolean fixedsamplelocations) {
        GL45C.glTextureStorage2DMultisample(texture, samples, internalformat, width, height, fixedsamplelocations);
    }

    @Override
    public void textureStorage3D(final int texture, final int levels, final int internalformat, final int width, final int height, final int depth) {
        GL45C.glTextureStorage3D(texture, levels, internalformat, width, height, depth);
    }

    @Override
    public void textureStorage3DMultisample(final int texture, final int samples, final int internalformat, final int width, final int height, final int depth, final boolean fixedsamplelocations) {
        GL45C.glTextureStorage3DMultisample(texture, samples, internalformat, width, height, depth, fixedsamplelocations);
    }

    @Override
    public void textureSubImage1D(final int texture, final int level, final int xoffset, final int width, final int format, final int type, final long pixels) {
        GL45C.nglTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }

    @Override
    public void textureSubImage2D(final int texture, final int level, final int xoffset, final int yoffset, final int width, final int height, final int format, final int type, final long pixels) {
        GL45C.nglTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    @Override
    public void textureSubImage3D(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final long pixels) {
        GL45C.nglTextureSubImage3D(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    @Override
    public boolean unmapNamedBuffer(final int buffer) {
        return GL45C.glUnmapNamedBuffer(buffer);
    }

    @Override
    public void vertexArrayAttribBinding(final int vaobj, final int attribindex, final int bindingindex) {
        GL45C.glVertexArrayAttribBinding(vaobj, attribindex, bindingindex);
    }

    @Override
    public void vertexArrayAttribFormat(final int vaobj, final int attribindex, final int size, final int type, final boolean normalized, final int relativeoffset) {
        GL45C.glVertexArrayAttribFormat(vaobj, attribindex, size, type, normalized, relativeoffset);
    }

    @Override
    public void vertexArrayAttribIFormat(final int vaobj, final int attribindex, final int size, final int type, final int relativeoffset) {
        GL45C.glVertexArrayAttribIFormat(vaobj, attribindex, size, type, relativeoffset);
    }

    @Override
    public void vertexArrayAttribLFormat(final int vaobj, final int attribindex, final int size, final int type, final int relativeoffset) {
        GL45C.glVertexArrayAttribLFormat(vaobj, attribindex, size, type, relativeoffset);
    }

    @Override
    public void vertexArrayBindingDivisor(final int vaobj, final int bindingindex, final int divisor) {
        GL45C.glVertexArrayBindingDivisor(vaobj, bindingindex, divisor);
    }

    @Override
    public void vertexArrayElementBuffer(final int vaobj, final int buffer) {
        if (buffer == 0 && ThinGL.workarounds().isDsaVertexArrayElementBufferUnbindBroken()) {
            final int previousVertexArray = GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            GL30C.glBindVertexArray(vaobj);
            GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, 0);
            GL30C.glBindVertexArray(previousVertexArray);
            return;
        }

        GL45C.glVertexArrayElementBuffer(vaobj, buffer);
    }

    @Override
    public void vertexArrayVertexBuffer(final int vaobj, final int bindingindex, final int buffer, final long offset, final int stride) {
        GL45C.glVertexArrayVertexBuffer(vaobj, bindingindex, buffer, offset, stride);
    }

}
