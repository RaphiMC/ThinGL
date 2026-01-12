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
package net.raphimc.thingl.implementation.gl;

public interface GLBackend {

    void blendFunc(final int sfactor, final int dfactor);

    void colorMask(final boolean red, final boolean green, final boolean blue, final boolean alpha);

    void cullFace(final int mode);

    void deleteTexture(final int texture);

    void depthFunc(final int func);

    void depthMask(final boolean flag);

    void disable(final int cap);

    void drawArrays(final int mode, final int first, final int count);

    void drawElements(final int mode, final int count, final int type, final long indices);

    void enable(final int cap);

    void frontFace(final int dir);

    boolean getBoolean(final int pname);

    float getFloat(final int pname);

    int getInteger(final int pname);

    void getIntegerv(final int pname, final int[] params);

    String getString(final int name);

    boolean isEnabled(final int cap);

    boolean isTexture(final int texture);

    void logicOp(final int opcode);

    void pixelStorei(final int pname, final int param);

    void polygonOffset(final float factor, final float units);

    void scissor(final int x, final int y, final int width, final int height);

    void stencilFunc(final int func, final int ref, final int mask);

    void stencilMask(final int mask);

    void stencilOp(final int sfail, final int dpfail, final int dppass);

    void viewport(final int x, final int y, final int width, final int height);

    void blendEquation(final int mode);

    void blendFuncSeparate(final int srcRGB, final int dstRGB, final int srcAlpha, final int dstAlpha);

    void beginQuery(final int target, final int id);

    void bindBuffer(final int target, final int buffer);

    void deleteBuffer(final int buffer);

    void deleteQuery(final int id);

    void endQuery(final int target);

    int getQueryObjecti(final int id, final int pname);

    boolean isBuffer(final int buffer);

    boolean isQuery(final int id);

    void attachShader(final int program, final int shader);

    void compileShader(final int shader);

    int createProgram();

    int createShader(final int type);

    void deleteProgram(final int program);

    void deleteShader(final int shader);

    void detachShader(final int program, final int shader);

    void getAttachedShaders(final int program, final int[] count, final int[] shaders);

    String getProgramInfoLog(final int program);

    int getProgrami(final int program, final int pname);

    String getShaderInfoLog(final int shader);

    String getShaderSource(final int shader);

    int getShaderi(final int shader, final int pname);

    int getUniformLocation(final int program, final CharSequence name);

    boolean isProgram(final int program);

    boolean isShader(final int shader);

    void linkProgram(final int program);

    void shaderSource(final int shader, final CharSequence string);

    void stencilMaskSeparate(final int face, final int mask);

    void useProgram(final int program);

    void validateProgram(final int program);

    void bindBufferBase(final int target, final int index, final int buffer);

    void bindFramebuffer(final int target, final int framebuffer);

    void bindVertexArray(final int array);

    void clearBufferiv(final int buffer, final int drawbuffer, final int[] value);

    void deleteFramebuffer(final int framebuffer);

    void deleteRenderbuffer(final int renderbuffer);

    void deleteVertexArray(final int array);

    boolean isFramebuffer(final int framebuffer);

    boolean isRenderbuffer(final int renderbuffer);

    boolean isVertexArray(final int array);

    void uniformBlockBinding(final int program, final int uniformBlockIndex, final int uniformBlockBinding);

    int clientWaitSync(final long sync, final int flags, final long timeout);

    void deleteSync(final long sync);

    long fenceSync(final int condition, final int flags);

    int getSynci(final long sync, final int pname);

    boolean isSync(final long sync);

    void waitSync(final long sync, final int flags, final long timeout);

    void bindSampler(final int unit, final int sampler);

    void deleteSampler(final int sampler);

    long getQueryObjecti64(final int id, final int pname);

    float getSamplerParameterf(final int sampler, final int pname);

    void getSamplerParameterfv(final int sampler, final int pname, final float[] params);

    int getSamplerParameteri(final int sampler, final int pname);

    void getSamplerParameteriv(final int sampler, final int pname, final int[] params);

    boolean isSampler(final int id);

    void samplerParameterf(final int sampler, final int pname, final float param);

    void samplerParameterfv(final int sampler, final int pname, final float[] params);

    void samplerParameteri(final int sampler, final int pname, final int param);

    void samplerParameteriv(final int sampler, final int pname, final int[] params);

    void drawArraysIndirect(final int mode, final long indirect);

    void drawElementsIndirect(final int mode, final int type, final long indirect);

    void programUniform1f(final int program, final int location, final float v0);

    void programUniform1i(final int program, final int location, final int v0);

    void programUniform1iv(final int program, final int location, final int[] value);

    void programUniform2f(final int program, final int location, final float v0, final float v1);

    void programUniform3f(final int program, final int location, final float v0, final float v1, final float v2);

    void programUniform4f(final int program, final int location, final float v0, final float v1, final float v2, final float v3);

    void programUniformMatrix3fv(final int program, final int location, final int count, final boolean transpose, final long value);

    void programUniformMatrix4fv(final int program, final int location, final int count, final boolean transpose, final long value);

    void bindImageTexture(final int unit, final int texture, final int level, final boolean layered, final int layer, final int access, final int format);

    void drawArraysInstancedBaseInstance(final int mode, final int first, final int count, final int primcount, final int baseinstance);

    void drawElementsInstancedBaseVertexBaseInstance(final int mode, final int count, final int type, final long indices, final int primcount, final int basevertex, final int baseinstance);

    void copyImageSubData(final int srcName, final int srcTarget, final int srcLevel, final int srcX, final int srcY, final int srcZ, final int dstName, final int dstTarget, final int dstLevel, final int dstX, final int dstY, final int dstZ, final int srcWidth, final int srcHeight, final int srcDepth);

    String getObjectLabel(final int identifier, final int name);

    int getProgramResourceIndex(final int program, final int programInterface, final CharSequence name);

    void multiDrawArraysIndirect(final int mode, final long indirect, final int drawcount, final int stride);

    void multiDrawElementsIndirect(final int mode, final int type, final long indirect, final int drawcount, final int stride);

    void objectLabel(final int identifier, final int name, final CharSequence label);

    void shaderStorageBlockBinding(final int program, final int storageBlockIndex, final int storageBlockBinding);

    void bindSamplers(final int first, final int[] samplers);

    void bindTextures(final int first, final int[] textures);

    void clearTexImage(final int texture, final int level, final int format, final int type, final float[] data);

    void clearTexSubImage(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final float[] data);

    void bindTextureUnit(final int unit, final int texture);

    void blitNamedFramebuffer(final int readFramebuffer, final int drawFramebuffer, final int srcX0, final int srcY0, final int srcX1, final int srcY1, final int dstX0, final int dstY0, final int dstX1, final int dstY1, final int mask, final int filter);

    int checkNamedFramebufferStatus(final int framebuffer, final int target);

    void clearNamedFramebufferfi(final int framebuffer, final int buffer, final int drawbuffer, final float depth, final int stencil);

    void clearNamedFramebufferfv(final int framebuffer, final int buffer, final int drawbuffer, final float[] value);

    void clearNamedFramebufferiv(final int framebuffer, final int buffer, final int drawbuffer, final int[] value);

    void copyNamedBufferSubData(final int readBuffer, final int writeBuffer, final long readOffset, final long writeOffset, final long size);

    int createBuffer();

    int createFramebuffer();

    int createQuery(final int target);

    int createRenderbuffer();

    int createSampler();

    int createTexture(final int target);

    int createVertexArray();

    void enableVertexArrayAttrib(final int vaobj, final int index);

    void flushMappedNamedBufferRange(final int buffer, final long offset, final long length);

    void generateTextureMipmap(final int texture);

    int getNamedBufferParameteri(final int buffer, final int pname);

    long getNamedBufferParameteri64(final int buffer, final int pname);

    void getNamedBufferSubData(final int buffer, final long offset, final long size, final long data);

    int getNamedFramebufferAttachmentParameteri(final int framebuffer, final int attachment, final int pname);

    int getNamedRenderbufferParameteri(final int renderbuffer, final int pname);

    int getTextureLevelParameteri(final int texture, final int level, final int pname);

    float getTextureParameterf(final int texture, final int pname);

    void getTextureParameterfv(final int texture, final int pname, final float[] params);

    int getTextureParameteri(final int texture, final int pname);

    void getTextureParameteriv(final int texture, final int pname, final int[] params);

    void getTextureSubImage(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final int bufSize, final long pixels);

    long mapNamedBuffer(final int buffer, final int access);

    long mapNamedBufferRange(final int buffer, final long offset, final long length, final int access);

    void namedBufferData(final int buffer, final long size, final int usage);

    void namedBufferData(final int buffer, final long size, final long data, final int usage);

    void namedBufferStorage(final int buffer, final long size, final int flags);

    void namedBufferStorage(final int buffer, final long size, final long data, final int flags);

    void namedBufferSubData(final int buffer, final long offset, final long size, final long data);

    void namedFramebufferRenderbuffer(final int framebuffer, final int attachment, final int renderbuffertarget, final int renderbuffer);

    void namedFramebufferTexture(final int framebuffer, final int attachment, final int texture, final int level);

    void namedRenderbufferStorage(final int renderbuffer, final int internalformat, final int width, final int height);

    void namedRenderbufferStorageMultisample(final int renderbuffer, final int samples, final int internalformat, final int width, final int height);

    void textureBuffer(final int texture, final int internalformat, final int buffer);

    void textureParameterf(final int texture, final int pname, final float param);

    void textureParameterfv(final int texture, final int pname, final float[] params);

    void textureParameteri(final int texture, final int pname, final int param);

    void textureParameteriv(final int texture, final int pname, final int[] params);

    void textureStorage1D(final int texture, final int levels, final int internalformat, final int width);

    void textureStorage2D(final int texture, final int levels, final int internalformat, final int width, final int height);

    void textureStorage2DMultisample(final int texture, final int samples, final int internalformat, final int width, final int height, final boolean fixedsamplelocations);

    void textureStorage3D(final int texture, final int levels, final int internalformat, final int width, final int height, final int depth);

    void textureStorage3DMultisample(final int texture, final int samples, final int internalformat, final int width, final int height, final int depth, final boolean fixedsamplelocations);

    void textureSubImage1D(final int texture, final int level, final int xoffset, final int width, final int format, final int type, final long pixels);

    void textureSubImage2D(final int texture, final int level, final int xoffset, final int yoffset, final int width, final int height, final int format, final int type, final long pixels);

    void textureSubImage3D(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final long pixels);

    boolean unmapNamedBuffer(final int buffer);

    void vertexArrayAttribBinding(final int vaobj, final int attribindex, final int bindingindex);

    void vertexArrayAttribFormat(final int vaobj, final int attribindex, final int size, final int type, final boolean normalized, final int relativeoffset);

    void vertexArrayAttribIFormat(final int vaobj, final int attribindex, final int size, final int type, final int relativeoffset);

    void vertexArrayAttribLFormat(final int vaobj, final int attribindex, final int size, final int type, final int relativeoffset);

    void vertexArrayBindingDivisor(final int vaobj, final int bindingindex, final int divisor);

    void vertexArrayElementBuffer(final int vaobj, final int buffer);

    void vertexArrayVertexBuffer(final int vaobj, final int bindingindex, final int buffer, final long offset, final int stride);

}
