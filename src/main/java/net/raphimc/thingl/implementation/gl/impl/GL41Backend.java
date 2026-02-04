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
import io.github.ocelot.glslprocessor.api.node.function.GlslPrimitiveConstructorNode;
import io.github.ocelot.glslprocessor.api.node.variable.*;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.rendering.command.impl.DrawArraysCommand;
import net.raphimc.thingl.rendering.command.impl.DrawElementsCommand;
import net.raphimc.thingl.resource.image.Image;
import net.raphimc.thingl.util.glsl.GlslNodeMutator;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GL41Backend extends GL45Backend {

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
    public void deleteTexture(final int texture) {
        this.textureTargets.remove(texture);
        super.deleteTexture(texture);
    }

    @Override
    public void deleteQuery(final int id) {
        this.queryTargets.remove(id);
        super.deleteQuery(id);
    }

    @Override
    public int getQueryObjecti(final int id, final int pname) {
        if (pname == GL45C.GL_QUERY_TARGET && !this.capabilities.OpenGL45 && !this.capabilities.GL_ARB_direct_state_access) {
            return this.queryTargets.get(id);
        }
        return super.getQueryObjecti(id, pname);
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
        super.shaderSource(shader, string);
    }

    @Override
    public void bindBufferBase(final int target, final int index, final int buffer) {
        if (target == GL43C.GL_SHADER_STORAGE_BUFFER && !this.supportsShaderStorageBuffers) {
            final int bufferTexture = this.shaderStorageBufferTextures.computeIfAbsent(index, _ -> this.createTexture(GL31C.GL_TEXTURE_BUFFER));
            this.textureBuffer(bufferTexture, GL30C.GL_R32F, buffer);
            this.bindTextureUnit(SHADER_STORAGE_BUFFER_TEXTURE_UNIT_OFFSET + index, bufferTexture);
            this.bindSampler(SHADER_STORAGE_BUFFER_TEXTURE_UNIT_OFFSET + index, 0);
            return;
        }
        super.bindBufferBase(target, index, buffer);
    }

    @Override
    public void deleteVertexArray(final int array) {
        this.vertexArrayObjects.remove(array);
        super.deleteVertexArray(array);
    }

    @Override
    public void bindImageTexture(final int unit, final int texture, final int level, final boolean layered, final int layer, final int access, final int format) {
        if (this.capabilities.glBindImageTexture != 0L) {
            super.bindImageTexture(unit, texture, level, layered, layer, access, format);
        } else if (this.capabilities.glBindImageTextureEXT != 0L) {
            EXTShaderImageLoadStore.glBindImageTextureEXT(unit, texture, level, layered, layer, access, format);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void drawArraysInstancedBaseInstance(final int mode, final int first, final int count, final int primcount, final int baseinstance) {
        if (this.capabilities.glDrawArraysInstancedBaseInstance != 0L) {
            super.drawArraysInstancedBaseInstance(mode, first, count, primcount, baseinstance);
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
            super.drawElementsInstancedBaseVertexBaseInstance(mode, count, type, indices, primcount, basevertex, baseinstance);
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
            super.copyImageSubData(srcName, srcTarget, srcLevel, srcX, srcY, srcZ, dstName, dstTarget, dstLevel, dstX, dstY, dstZ, srcWidth, srcHeight, srcDepth);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String getObjectLabel(final int identifier, final int name) {
        if (this.capabilities.glGetObjectLabel != 0L) {
            return super.getObjectLabel(identifier, name);
        } else if (this.capabilities.glGetObjectLabelEXT != 0L) {
            return EXTDebugLabel.glGetObjectLabelEXT(getDebugLabelObjectTypeEXT(identifier), name, 255);
        } else {
            return ""; // Unsupported
        }
    }

    @Override
    public int getProgramResourceIndex(final int program, final int programInterface, final CharSequence name) {
        if (programInterface == GL43C.GL_SHADER_STORAGE_BLOCK && !this.supportsShaderStorageBuffers) {
            return this.getUniformLocation(program, name);
        }

        if (this.capabilities.glGetProgramResourceIndex != 0L) {
            return super.getProgramResourceIndex(program, programInterface, name);
        } else {
            return switch (programInterface) {
                case GL43C.GL_UNIFORM -> {
                    final int uniformCount = this.getProgrami(program, GL20C.GL_ACTIVE_UNIFORMS);
                    for (int i = 0; i < uniformCount; i++) {
                        final String uniformName = GL31C.glGetActiveUniformName(program, i);
                        if (uniformName.contentEquals(name)) {
                            yield i;
                        }
                    }
                    yield GL31C.GL_INVALID_INDEX;
                }
                case GL43C.GL_UNIFORM_BLOCK -> {
                    final int uniformBlockCount = this.getProgrami(program, GL31C.GL_ACTIVE_UNIFORM_BLOCKS);
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
            super.multiDrawArraysIndirect(mode, indirect, drawcount, stride);
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
            super.multiDrawElementsIndirect(mode, type, indirect, drawcount, stride);
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
            super.objectLabel(identifier, name, label);
        } else if (this.capabilities.glLabelObjectEXT != 0L) {
            EXTDebugLabel.glLabelObjectEXT(getDebugLabelObjectTypeEXT(identifier), name, label);
        } else {
            // Unsupported
        }
    }

    @Override
    public void shaderStorageBlockBinding(final int program, final int storageBlockIndex, final int storageBlockBinding) {
        if (this.capabilities.glShaderStorageBlockBinding != 0L && this.supportsShaderStorageBuffers) {
            super.shaderStorageBlockBinding(program, storageBlockIndex, storageBlockBinding);
        } else {
            GL41C.glProgramUniform1i(program, storageBlockIndex, SHADER_STORAGE_BUFFER_TEXTURE_UNIT_OFFSET + storageBlockBinding);
        }
    }

    @Override
    public void bindSamplers(final int first, final int[] samplers) {
        if (this.capabilities.glBindSamplers != 0L) {
            super.bindSamplers(first, samplers);
        } else {
            for (int i = 0; i < samplers.length; i++) {
                this.bindSampler(first + i, samplers[i]);
            }
        }
    }

    @Override
    public void bindTextures(final int first, final int[] textures) {
        if (this.capabilities.glBindTextures != 0L) {
            super.bindTextures(first, textures);
        } else {
            for (int i = 0; i < textures.length; i++) {
                this.bindTextureUnit(first + i, textures[i]);
            }
        }
    }

    @Override
    public void clearTexImage(final int texture, final int level, final int format, final int type, final float[] data) {
        if (this.capabilities.glClearTexImage != 0L) {
            super.clearTexImage(texture, level, format, type, data);
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
            super.clearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
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
            super.bindTextureUnit(unit, texture);
        } else {
            final int previousActiveTexture = this.getInteger(GL13C.GL_ACTIVE_TEXTURE);
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
            super.blitNamedFramebuffer(readFramebuffer, drawFramebuffer, srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
        } else {
            final int previousReadFramebuffer = this.getInteger(GL30C.GL_READ_FRAMEBUFFER_BINDING);
            final int previousDrawFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            this.bindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, readFramebuffer);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
            GL30C.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
            this.bindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, previousReadFramebuffer);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousDrawFramebuffer);
        }
    }

    @Override
    public int checkNamedFramebufferStatus(final int framebuffer, final int target) {
        if (this.capabilities.glCheckNamedFramebufferStatus != 0L) {
            return super.checkNamedFramebufferStatus(framebuffer, target);
        } else if (this.capabilities.glCheckNamedFramebufferStatusEXT != 0L) {
            return EXTDirectStateAccess.glCheckNamedFramebufferStatusEXT(framebuffer, target);
        } else {
            final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            final int status = GL30C.glCheckFramebufferStatus(target);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
            return status;
        }
    }

    @Override
    public void clearNamedFramebufferfi(final int framebuffer, final int buffer, final int drawbuffer, final float depth, final int stencil) {
        if (this.capabilities.glClearNamedFramebufferfi != 0L) {
            super.clearNamedFramebufferfi(framebuffer, buffer, drawbuffer, depth, stencil);
        } else {
            final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            GL30C.glClearBufferfi(buffer, drawbuffer, depth, stencil);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
        }
    }

    @Override
    public void clearNamedFramebufferfv(final int framebuffer, final int buffer, final int drawbuffer, final float[] value) {
        if (this.capabilities.glClearNamedFramebufferfv != 0L) {
            super.clearNamedFramebufferfv(framebuffer, buffer, drawbuffer, value);
        } else {
            final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            GL30C.glClearBufferfv(buffer, drawbuffer, value);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
        }
    }

    @Override
    public void clearNamedFramebufferiv(final int framebuffer, final int buffer, final int drawbuffer, final int[] value) {
        if (this.capabilities.glClearNamedFramebufferiv != 0L) {
            super.clearNamedFramebufferiv(framebuffer, buffer, drawbuffer, value);
        } else {
            final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            GL30C.glClearBufferiv(buffer, drawbuffer, value);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
        }
    }

    @Override
    public void copyNamedBufferSubData(final int readBuffer, final int writeBuffer, final long readOffset, final long writeOffset, final long size) {
        if (this.capabilities.glCopyNamedBufferSubData != 0L) {
            super.copyNamedBufferSubData(readBuffer, writeBuffer, readOffset, writeOffset, size);
        } else if (this.capabilities.glNamedCopyBufferSubDataEXT != 0L) {
            EXTDirectStateAccess.glNamedCopyBufferSubDataEXT(readBuffer, writeBuffer, readOffset, writeOffset, size);
        } else {
            final int previousReadBuffer = this.getInteger(GL31C.GL_COPY_READ_BUFFER);
            final int previousWriteBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_READ_BUFFER, readBuffer);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, writeBuffer);
            GL31C.glCopyBufferSubData(GL31C.GL_COPY_READ_BUFFER, GL31C.GL_COPY_WRITE_BUFFER, readOffset, writeOffset, size);
            this.bindBuffer(GL31C.GL_COPY_READ_BUFFER, previousReadBuffer);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousWriteBuffer);
        }
    }

    @Override
    public int createBuffer() {
        if (this.capabilities.glCreateBuffers != 0L) {
            return super.createBuffer();
        } else {
            final int buffer = GL15C.glGenBuffers();
            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
            return buffer;
        }
    }

    @Override
    public int createFramebuffer() {
        if (this.capabilities.glCreateFramebuffers != 0L) {
            return super.createFramebuffer();
        } else {
            final int framebuffer = GL30C.glGenFramebuffers();
            final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
            return framebuffer;
        }
    }

    @Override
    public int createQuery(final int target) {
        final int query;
        if (this.capabilities.glCreateQueries != 0L) {
            query = super.createQuery(target);
        } else {
            query = GL15C.glGenQueries();
        }
        this.queryTargets.put(query, target);
        return query;
    }

    @Override
    public int createRenderbuffer() {
        if (this.capabilities.glCreateRenderbuffers != 0L) {
            return super.createRenderbuffer();
        } else {
            final int renderBuffer = GL30C.glGenRenderbuffers();
            final int previousRenderBuffer = this.getInteger(GL30C.GL_RENDERBUFFER_BINDING);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, renderBuffer);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, previousRenderBuffer);
            return renderBuffer;
        }
    }

    @Override
    public int createSampler() {
        if (this.capabilities.glCreateSamplers != 0L) {
            return super.createSampler();
        } else {
            return GL33C.glGenSamplers();
        }
    }

    @Override
    public int createTexture(final int target) {
        final int texture;
        if (this.capabilities.glCreateTextures != 0L) {
            texture = super.createTexture(target);
        } else {
            texture = GL11C.glGenTextures();
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glBindTexture(target, previousTexture);
        }
        this.textureTargets.put(texture, target);
        return texture;
    }

    @Override
    public int createVertexArray() {
        if (this.capabilities.glCreateVertexArrays != 0L) {
            return super.createVertexArray();
        } else {
            final int vertexArray = GL30C.glGenVertexArrays();
            final int previousVertexArray = this.getInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            this.bindVertexArray(vertexArray);
            this.bindVertexArray(previousVertexArray);
            return vertexArray;
        }
    }

    @Override
    public void enableVertexArrayAttrib(final int vaobj, final int index) {
        if (this.capabilities.glEnableVertexArrayAttrib != 0L) {
            super.enableVertexArrayAttrib(vaobj, index);
        } else if (this.capabilities.glEnableVertexArrayAttribEXT != 0L) {
            EXTDirectStateAccess.glEnableVertexArrayAttribEXT(vaobj, index);
        } else {
            final int previousVertexArray = this.getInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            this.bindVertexArray(vaobj);
            GL20C.glEnableVertexAttribArray(index);
            this.bindVertexArray(previousVertexArray);
        }
    }

    @Override
    public void flushMappedNamedBufferRange(final int buffer, final long offset, final long length) {
        if (this.capabilities.glFlushMappedNamedBufferRange != 0L) {
            super.flushMappedNamedBufferRange(buffer, offset, length);
        } else if (this.capabilities.glFlushMappedNamedBufferRangeEXT != 0L) {
            EXTDirectStateAccess.glFlushMappedNamedBufferRangeEXT(buffer, offset, length);
        } else {
            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL30C.glFlushMappedBufferRange(GL31C.GL_COPY_WRITE_BUFFER, offset, length);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public void generateTextureMipmap(final int texture) {
        if (this.capabilities.glGenerateTextureMipmap != 0L) {
            super.generateTextureMipmap(texture);
        } else if (this.capabilities.glGenerateTextureMipmapEXT != 0L) {
            EXTDirectStateAccess.glGenerateTextureMipmapEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D));
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL30C.glGenerateMipmap(target);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public int getNamedBufferParameteri(final int buffer, final int pname) {
        if (this.capabilities.glGetNamedBufferParameteriv != 0L) {
            return super.getNamedBufferParameteri(buffer, pname);
        } else if (this.capabilities.glGetNamedBufferParameterivEXT != 0L) {
            return EXTDirectStateAccess.glGetNamedBufferParameteriEXT(buffer, pname);
        } else {
            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            final int parameter = GL15C.glGetBufferParameteri(GL31C.GL_COPY_WRITE_BUFFER, pname);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
            return parameter;
        }
    }

    @Override
    public long getNamedBufferParameteri64(final int buffer, final int pname) {
        if (this.capabilities.glGetNamedBufferParameteri64v != 0L) {
            return super.getNamedBufferParameteri64(buffer, pname);
        } else {
            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            final long parameter = GL32C.glGetBufferParameteri64(GL31C.GL_COPY_WRITE_BUFFER, pname);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
            return parameter;
        }
    }

    @Override
    public void getNamedBufferSubData(final int buffer, final long offset, final long size, final long data) {
        if (this.capabilities.glGetNamedBufferSubData != 0L) {
            super.getNamedBufferSubData(buffer, offset, size, data);
        } else if (this.capabilities.glGetNamedBufferSubDataEXT != 0L) {
            EXTDirectStateAccess.nglGetNamedBufferSubDataEXT(buffer, offset, size, data);
        } else {
            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL15C.nglGetBufferSubData(GL31C.GL_COPY_WRITE_BUFFER, offset, size, data);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public int getNamedFramebufferAttachmentParameteri(final int framebuffer, final int attachment, final int pname) {
        if (this.capabilities.glGetNamedFramebufferAttachmentParameteriv != 0L) {
            return super.getNamedFramebufferAttachmentParameteri(framebuffer, attachment, pname);
        } else if (this.capabilities.glGetNamedFramebufferAttachmentParameterivEXT != 0L) {
            return EXTDirectStateAccess.glGetNamedFramebufferAttachmentParameteriEXT(framebuffer, attachment, pname);
        } else {
            final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            final int parameter = GL30C.glGetFramebufferAttachmentParameteri(GL30C.GL_DRAW_FRAMEBUFFER, attachment, pname);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
            return parameter;
        }
    }

    @Override
    public int getNamedRenderbufferParameteri(final int renderbuffer, final int pname) {
        if (this.capabilities.glGetNamedRenderbufferParameteriv != 0L) {
            return super.getNamedRenderbufferParameteri(renderbuffer, pname);
        } else if (this.capabilities.glGetNamedRenderbufferParameterivEXT != 0L) {
            return EXTDirectStateAccess.glGetNamedRenderbufferParameteriEXT(renderbuffer, pname);
        } else {
            final int previousRenderbuffer = this.getInteger(GL30C.GL_RENDERBUFFER_BINDING);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, renderbuffer);
            final int parameter = GL30C.glGetRenderbufferParameteri(GL30C.GL_RENDERBUFFER, pname);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, previousRenderbuffer);
            return parameter;
        }
    }

    @Override
    public int getTextureLevelParameteri(final int texture, final int level, final int pname) {
        if (this.capabilities.glGetTextureLevelParameteriv != 0L) {
            return super.getTextureLevelParameteri(texture, level, pname);
        } else if (this.capabilities.glGetTextureLevelParameterivEXT != 0L) {
            return EXTDirectStateAccess.glGetTextureLevelParameteriEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), level, pname);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            final int parameter = GL11C.glGetTexLevelParameteri(target, level, pname);
            GL11C.glBindTexture(target, previousTexture);
            return parameter;
        }
    }

    @Override
    public float getTextureParameterf(final int texture, final int pname) {
        if (this.capabilities.glGetTextureParameterfv != 0L) {
            return super.getTextureParameterf(texture, pname);
        } else if (this.capabilities.glGetTextureParameterfvEXT != 0L) {
            return EXTDirectStateAccess.glGetTextureParameterfEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            final float parameter = GL11C.glGetTexParameterf(target, pname);
            GL11C.glBindTexture(target, previousTexture);
            return parameter;
        }
    }

    @Override
    public void getTextureParameterfv(final int texture, final int pname, final float[] params) {
        if (this.capabilities.glGetTextureParameterfv != 0L) {
            super.getTextureParameterfv(texture, pname, params);
        } else if (this.capabilities.glGetTextureParameterfvEXT != 0L) {
            EXTDirectStateAccess.glGetTextureParameterfvEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname, params);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glGetTexParameterfv(target, pname, params);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public int getTextureParameteri(final int texture, final int pname) {
        if (this.capabilities.glGetTextureParameteriv != 0L) {
            return super.getTextureParameteri(texture, pname);
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
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            final int parameter = GL11C.glGetTexParameteri(target, pname);
            GL11C.glBindTexture(target, previousTexture);
            return parameter;
        }
    }

    @Override
    public void getTextureParameteriv(final int texture, final int pname, final int[] params) {
        if (this.capabilities.glGetTextureParameteriv != 0L) {
            super.getTextureParameteriv(texture, pname, params);
        } else if (this.capabilities.glGetTextureParameterivEXT != 0L) {
            EXTDirectStateAccess.glGetTextureParameterivEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname, params);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glGetTexParameteriv(target, pname, params);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void getTextureSubImage(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final int bufSize, final long pixels) {
        if (this.capabilities.glGetTextureSubImage != 0L) {
            super.getTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, bufSize, pixels);
        } else {
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
    }

    @Override
    public long mapNamedBuffer(final int buffer, final int access) {
        if (this.capabilities.glMapNamedBuffer != 0L) {
            return super.mapNamedBuffer(buffer, access);
        } else if (this.capabilities.glMapNamedBufferEXT != 0L) {
            return EXTDirectStateAccess.nglMapNamedBufferEXT(buffer, access);
        } else {
            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            final long address = GL15C.nglMapBuffer(GL31C.GL_COPY_WRITE_BUFFER, access);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
            return address;
        }
    }

    @Override
    public long mapNamedBufferRange(final int buffer, final long offset, final long length, final int access) {
        if (this.capabilities.glMapNamedBufferRange != 0L) {
            return super.mapNamedBufferRange(buffer, offset, length, access);
        } else if (this.capabilities.glMapNamedBufferRangeEXT != 0L) {
            return EXTDirectStateAccess.nglMapNamedBufferRangeEXT(buffer, offset, length, access);
        } else {
            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            final long address = GL30C.nglMapBufferRange(GL31C.GL_COPY_WRITE_BUFFER, offset, length, access);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
            return address;
        }
    }

    @Override
    public void namedBufferData(final int buffer, final long size, final int usage) {
        if (this.capabilities.glNamedBufferData != 0L) {
            super.namedBufferData(buffer, size, usage);
        } else if (this.capabilities.glNamedBufferDataEXT != 0L) {
            EXTDirectStateAccess.glNamedBufferDataEXT(buffer, size, usage);
        } else {
            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL15C.glBufferData(GL31C.GL_COPY_WRITE_BUFFER, size, usage);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public void namedBufferData(final int buffer, final long size, final long data, final int usage) {
        if (this.capabilities.glNamedBufferData != 0L) {
            super.namedBufferData(buffer, size, data, usage);
        } else if (this.capabilities.glNamedBufferDataEXT != 0L) {
            EXTDirectStateAccess.nglNamedBufferDataEXT(buffer, size, data, usage);
        } else {
            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL15C.nglBufferData(GL31C.GL_COPY_WRITE_BUFFER, size, data, usage);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public void namedBufferStorage(final int buffer, final long size, final int flags) {
        if (this.capabilities.glNamedBufferStorage != 0L) {
            super.namedBufferStorage(buffer, size, flags);
        } else if (this.capabilities.glNamedBufferStorageEXT != 0L) {
            ARBBufferStorage.glNamedBufferStorageEXT(buffer, size, flags);
        } else if (this.capabilities.glBufferStorage != 0L) {
            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL44C.glBufferStorage(GL31C.GL_COPY_WRITE_BUFFER, size, flags);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        } else {
            final int usage;
            if ((flags & GL44C.GL_DYNAMIC_STORAGE_BIT) != 0) {
                usage = GL15C.GL_DYNAMIC_DRAW;
            } else {
                usage = GL15C.GL_STATIC_DRAW;
            }

            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL15C.glBufferData(GL31C.GL_COPY_WRITE_BUFFER, size, usage);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public void namedBufferStorage(final int buffer, final long size, final long data, final int flags) {
        if (this.capabilities.glNamedBufferStorage != 0L) {
            super.namedBufferStorage(buffer, size, data, flags);
        } else if (this.capabilities.glNamedBufferStorageEXT != 0L) {
            ARBBufferStorage.nglNamedBufferStorageEXT(buffer, size, data, flags);
        } else if (this.capabilities.glBufferStorage != 0L) {
            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL44C.nglBufferStorage(GL31C.GL_COPY_WRITE_BUFFER, size, data, flags);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        } else {
            final int usage;
            if ((flags & GL44C.GL_DYNAMIC_STORAGE_BIT) != 0) {
                usage = GL15C.GL_DYNAMIC_DRAW;
            } else {
                usage = GL15C.GL_STATIC_DRAW;
            }

            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL15C.nglBufferData(GL31C.GL_COPY_WRITE_BUFFER, size, data, usage);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public void namedBufferSubData(final int buffer, final long offset, final long size, final long data) {
        if (this.capabilities.glNamedBufferSubData != 0L) {
            super.namedBufferSubData(buffer, offset, size, data);
        } else if (this.capabilities.glNamedBufferSubDataEXT != 0L) {
            EXTDirectStateAccess.nglNamedBufferSubDataEXT(buffer, offset, size, data);
        } else {
            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            GL15C.nglBufferSubData(GL31C.GL_COPY_WRITE_BUFFER, offset, size, data);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
        }
    }

    @Override
    public void namedFramebufferRenderbuffer(final int framebuffer, final int attachment, final int renderbuffertarget, final int renderbuffer) {
        if (this.capabilities.glNamedFramebufferRenderbuffer != 0L) {
            super.namedFramebufferRenderbuffer(framebuffer, attachment, renderbuffertarget, renderbuffer);
        } else if (this.capabilities.glNamedFramebufferRenderbufferEXT != 0L) {
            EXTDirectStateAccess.glNamedFramebufferRenderbufferEXT(framebuffer, attachment, renderbuffertarget, renderbuffer);
        } else {
            final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            GL30C.glFramebufferRenderbuffer(GL30C.GL_DRAW_FRAMEBUFFER, attachment, renderbuffertarget, renderbuffer);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
        }
    }

    @Override
    public void namedFramebufferTexture(final int framebuffer, final int attachment, final int texture, final int level) {
        if (this.capabilities.glNamedFramebufferTexture != 0L) {
            super.namedFramebufferTexture(framebuffer, attachment, texture, level);
        } else if (this.capabilities.glNamedFramebufferTextureEXT != 0L) {
            EXTDirectStateAccess.glNamedFramebufferTextureEXT(framebuffer, attachment, texture, level);
        } else {
            final int previousFramebuffer = this.getInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
            GL30C.glFramebufferTexture2D(GL30C.GL_DRAW_FRAMEBUFFER, attachment, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), texture, level);
            this.bindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
        }
    }

    @Override
    public void namedRenderbufferStorage(final int renderbuffer, final int internalformat, final int width, final int height) {
        if (this.capabilities.glNamedRenderbufferStorage != 0L) {
            super.namedRenderbufferStorage(renderbuffer, internalformat, width, height);
        } else if (this.capabilities.glNamedRenderbufferStorageEXT != 0L) {
            EXTDirectStateAccess.glNamedRenderbufferStorageEXT(renderbuffer, internalformat, width, height);
        } else {
            final int previousRenderbuffer = this.getInteger(GL30C.GL_RENDERBUFFER_BINDING);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, renderbuffer);
            GL30C.glRenderbufferStorage(GL30C.GL_RENDERBUFFER, internalformat, width, height);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, previousRenderbuffer);
        }
    }

    @Override
    public void namedRenderbufferStorageMultisample(final int renderbuffer, final int samples, final int internalformat, final int width, final int height) {
        if (this.capabilities.glNamedRenderbufferStorageMultisample != 0L) {
            super.namedRenderbufferStorageMultisample(renderbuffer, samples, internalformat, width, height);
        } else if (this.capabilities.glNamedRenderbufferStorageMultisampleEXT != 0L) {
            EXTDirectStateAccess.glNamedRenderbufferStorageMultisampleEXT(renderbuffer, samples, internalformat, width, height);
        } else {
            final int previousRenderbuffer = this.getInteger(GL30C.GL_RENDERBUFFER_BINDING);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, renderbuffer);
            GL30C.glRenderbufferStorageMultisample(GL30C.GL_RENDERBUFFER, samples, internalformat, width, height);
            GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, previousRenderbuffer);
        }
    }

    @Override
    public void textureBuffer(final int texture, final int internalformat, final int buffer) {
        if (this.capabilities.glTextureBuffer != 0L) {
            super.textureBuffer(texture, internalformat, buffer);
        } else if (this.capabilities.glTextureBufferEXT != 0L) {
            EXTDirectStateAccess.glTextureBufferEXT(texture, this.textureTargets.getOrDefault(texture, GL31C.GL_TEXTURE_BUFFER), internalformat, buffer);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL31C.GL_TEXTURE_BUFFER);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL31C.glTexBuffer(target, internalformat, buffer);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureParameterf(final int texture, final int pname, final float param) {
        if (this.capabilities.glTextureParameterf != 0L) {
            super.textureParameterf(texture, pname, param);
        } else if (this.capabilities.glTextureParameterfEXT != 0L) {
            EXTDirectStateAccess.glTextureParameterfEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname, param);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glTexParameterf(target, pname, param);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureParameterfv(final int texture, final int pname, final float[] params) {
        if (this.capabilities.glTextureParameterfv != 0L) {
            super.textureParameterfv(texture, pname, params);
        } else if (this.capabilities.glTextureParameterfvEXT != 0L) {
            EXTDirectStateAccess.glTextureParameterfvEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname, params);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glTexParameterfv(target, pname, params);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureParameteri(final int texture, final int pname, final int param) {
        if (this.capabilities.glTextureParameteri != 0L) {
            super.textureParameteri(texture, pname, param);
        } else if (this.capabilities.glTextureParameteriEXT != 0L) {
            EXTDirectStateAccess.glTextureParameteriEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname, param);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glTexParameteri(target, pname, param);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureParameteriv(final int texture, final int pname, final int[] params) {
        if (this.capabilities.glTextureParameteriv != 0L) {
            super.textureParameteriv(texture, pname, params);
        } else if (this.capabilities.glTextureParameterivEXT != 0L) {
            EXTDirectStateAccess.glTextureParameterivEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), pname, params);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.glTexParameteriv(target, pname, params);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureStorage1D(final int texture, final int levels, final int internalformat, final int width) {
        if (this.capabilities.glTextureStorage1D != 0L) {
            super.textureStorage1D(texture, levels, internalformat, width);
        } else if (this.capabilities.glTextureStorage1DEXT != 0L) {
            ARBTextureStorage.glTextureStorage1DEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_1D), levels, internalformat, width);
        } else if (this.capabilities.glTexStorage1D != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_1D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL42C.glTexStorage1D(target, levels, internalformat, width);
            GL11C.glBindTexture(target, previousTexture);
        } else if (this.capabilities.glTexStorage1DEXT != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_1D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            EXTTextureStorage.glTexStorage1DEXT(target, levels, internalformat, width);
            GL11C.glBindTexture(target, previousTexture);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
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
            super.textureStorage2D(texture, levels, internalformat, width, height);
        } else if (this.capabilities.glTextureStorage2DEXT != 0L) {
            ARBTextureStorage.glTextureStorage2DEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), levels, internalformat, width, height);
        } else if (this.capabilities.glTexStorage2D != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL42C.glTexStorage2D(target, levels, internalformat, width, height);
            GL11C.glBindTexture(target, previousTexture);
        } else if (this.capabilities.glTexStorage2DEXT != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            EXTTextureStorage.glTexStorage2DEXT(target, levels, internalformat, width, height);
            GL11C.glBindTexture(target, previousTexture);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
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
            super.textureStorage2DMultisample(texture, samples, internalformat, width, height, fixedsamplelocations);
        } else if (this.capabilities.glTextureStorage2DMultisampleEXT != 0L) {
            ARBTextureStorageMultisample.glTextureStorage2DMultisampleEXT(texture, this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE), samples, internalformat, width, height, fixedsamplelocations);
        } else if (this.capabilities.glTexStorage2DMultisample != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL43C.glTexStorage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
            GL11C.glBindTexture(target, previousTexture);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL32C.glTexImage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureStorage3D(final int texture, final int levels, final int internalformat, final int width, final int height, final int depth) {
        if (this.capabilities.glTextureStorage3D != 0L) {
            super.textureStorage3D(texture, levels, internalformat, width, height, depth);
        } else if (this.capabilities.glTextureStorage3DEXT != 0L) {
            ARBTextureStorage.glTextureStorage3DEXT(texture, this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D), levels, internalformat, width, height, depth);
        } else if (this.capabilities.glTexStorage3D != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL42C.glTexStorage3D(target, levels, internalformat, width, height, depth);
            GL11C.glBindTexture(target, previousTexture);
        } else if (this.capabilities.glTexStorage3DEXT != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            EXTTextureStorage.glTexStorage3DEXT(target, levels, internalformat, width, height, depth);
            GL11C.glBindTexture(target, previousTexture);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
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
            super.textureStorage3DMultisample(texture, samples, internalformat, width, height, depth, fixedsamplelocations);
        } else if (this.capabilities.glTextureStorage3DMultisampleEXT != 0L) {
            ARBTextureStorageMultisample.glTextureStorage3DMultisampleEXT(texture, this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY), samples, internalformat, width, height, depth, fixedsamplelocations);
        } else if (this.capabilities.glTexStorage3DMultisample != 0L) {
            final int target = this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL43C.glTexStorage3DMultisample(target, samples, internalformat, width, height, depth, fixedsamplelocations);
            GL11C.glBindTexture(target, previousTexture);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL32C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL32C.glTexImage3DMultisample(target, samples, internalformat, width, height, depth, fixedsamplelocations);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureSubImage1D(final int texture, final int level, final int xoffset, final int width, final int format, final int type, final long pixels) {
        if (this.capabilities.glTextureSubImage1D != 0L) {
            super.textureSubImage1D(texture, level, xoffset, width, format, type, pixels);
        } else if (this.capabilities.glTextureSubImage1DEXT != 0L) {
            EXTDirectStateAccess.nglTextureSubImage1DEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_1D), level, xoffset, width, format, type, pixels);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_1D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.nglTexSubImage1D(target, level, xoffset, width, format, type, pixels);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureSubImage2D(final int texture, final int level, final int xoffset, final int yoffset, final int width, final int height, final int format, final int type, final long pixels) {
        if (this.capabilities.glTextureSubImage2D != 0L) {
            super.textureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
        } else if (this.capabilities.glTextureSubImage2DEXT != 0L) {
            EXTDirectStateAccess.nglTextureSubImage2DEXT(texture, this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D), level, xoffset, yoffset, width, height, format, type, pixels);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL11C.GL_TEXTURE_2D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL11C.nglTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public void textureSubImage3D(final int texture, final int level, final int xoffset, final int yoffset, final int zoffset, final int width, final int height, final int depth, final int format, final int type, final long pixels) {
        if (this.capabilities.glTextureSubImage3D != 0L) {
            super.textureSubImage3D(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
        } else if (this.capabilities.glTextureSubImage3DEXT != 0L) {
            EXTDirectStateAccess.nglTextureSubImage3DEXT(texture, this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D), level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
        } else {
            final int target = this.textureTargets.getOrDefault(texture, GL12C.GL_TEXTURE_3D);
            final int previousTexture = this.getInteger(getTextureQuery(target));
            GL11C.glBindTexture(target, texture);
            GL12C.nglTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
            GL11C.glBindTexture(target, previousTexture);
        }
    }

    @Override
    public boolean unmapNamedBuffer(final int buffer) {
        if (this.capabilities.glUnmapNamedBuffer != 0L) {
            return super.unmapNamedBuffer(buffer);
        } else if (this.capabilities.glUnmapNamedBufferEXT != 0L) {
            return EXTDirectStateAccess.glUnmapNamedBufferEXT(buffer);
        } else {
            final int previousBuffer = this.getInteger(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, buffer);
            final boolean result = GL15C.glUnmapBuffer(GL31C.GL_COPY_WRITE_BUFFER);
            this.bindBuffer(GL31C.GL_COPY_WRITE_BUFFER, previousBuffer);
            return result;
        }
    }

    @Override
    public void vertexArrayAttribBinding(final int vaobj, final int attribindex, final int bindingindex) {
        final VertexArrayObject vertexArrayObject = this.vertexArrayObjects.computeIfAbsent(vaobj, VertexArrayObject::new);
        final VertexArrayObject.VertexAttribute vertexAttribute = vertexArrayObject.attributes.computeIfAbsent(attribindex, VertexArrayObject.VertexAttribute::new);
        vertexAttribute.bindingindex = bindingindex;

        if (this.capabilities.glVertexArrayAttribBinding != 0L) {
            super.vertexArrayAttribBinding(vaobj, attribindex, bindingindex);
        } else if (this.capabilities.glVertexArrayVertexAttribBindingEXT != 0L) {
            ARBVertexAttribBinding.glVertexArrayVertexAttribBindingEXT(vaobj, attribindex, bindingindex);
        } else if (this.capabilities.glVertexAttribBinding != 0L) {
            final int previousVertexArray = this.getInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            this.bindVertexArray(vaobj);
            GL43C.glVertexAttribBinding(attribindex, bindingindex);
            this.bindVertexArray(previousVertexArray);
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
            super.vertexArrayAttribFormat(vaobj, attribindex, size, type, normalized, relativeoffset);
        } else if (this.capabilities.glVertexArrayVertexAttribFormatEXT != 0L) {
            ARBVertexAttribBinding.glVertexArrayVertexAttribFormatEXT(vaobj, attribindex, size, type, normalized, relativeoffset);
        } else if (this.capabilities.glVertexAttribFormat != 0L) {
            final int previousVertexArray = this.getInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            this.bindVertexArray(vaobj);
            GL43C.glVertexAttribFormat(attribindex, size, type, normalized, relativeoffset);
            this.bindVertexArray(previousVertexArray);
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
            super.vertexArrayAttribIFormat(vaobj, attribindex, size, type, relativeoffset);
        } else if (this.capabilities.glVertexArrayVertexAttribIFormatEXT != 0L) {
            ARBVertexAttribBinding.glVertexArrayVertexAttribIFormatEXT(vaobj, attribindex, size, type, relativeoffset);
        } else if (this.capabilities.glVertexAttribIFormat != 0L) {
            final int previousVertexArray = this.getInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            this.bindVertexArray(vaobj);
            GL43C.glVertexAttribIFormat(attribindex, size, type, relativeoffset);
            this.bindVertexArray(previousVertexArray);
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
            super.vertexArrayAttribLFormat(vaobj, attribindex, size, type, relativeoffset);
        } else if (this.capabilities.glVertexArrayVertexAttribLFormatEXT != 0L) {
            ARBVertexAttribBinding.glVertexArrayVertexAttribLFormatEXT(vaobj, attribindex, size, type, relativeoffset);
        } else if (this.capabilities.glVertexAttribLFormat != 0L) {
            final int previousVertexArray = this.getInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            this.bindVertexArray(vaobj);
            GL43C.glVertexAttribLFormat(attribindex, size, type, relativeoffset);
            this.bindVertexArray(previousVertexArray);
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
            super.vertexArrayBindingDivisor(vaobj, bindingindex, divisor);
        } else if (this.capabilities.glVertexArrayVertexBindingDivisorEXT != 0L) {
            ARBVertexAttribBinding.glVertexArrayVertexBindingDivisorEXT(vaobj, bindingindex, divisor);
        } else if (this.capabilities.glVertexBindingDivisor != 0L) {
            final int previousVertexArray = this.getInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            this.bindVertexArray(vaobj);
            GL43C.glVertexBindingDivisor(bindingindex, divisor);
            this.bindVertexArray(previousVertexArray);
        } else {
            vertexArrayObject.applyVertexBufferBinding(bindingindex);
        }
    }

    @Override
    public void vertexArrayElementBuffer(final int vaobj, final int buffer) {
        if (this.capabilities.glVertexArrayElementBuffer != 0L) {
            super.vertexArrayElementBuffer(vaobj, buffer);
        } else {
            final int previousVertexArray = this.getInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            this.bindVertexArray(vaobj);
            this.bindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, buffer);
            this.bindVertexArray(previousVertexArray);
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
            super.vertexArrayVertexBuffer(vaobj, bindingindex, buffer, offset, stride);
        } else if (this.capabilities.glVertexArrayBindVertexBufferEXT != 0L) {
            ARBVertexAttribBinding.glVertexArrayBindVertexBufferEXT(vaobj, bindingindex, buffer, offset, stride);
        } else if (this.capabilities.glBindVertexBuffer != 0L) {
            final int previousVertexArray = this.getInteger(GL30C.GL_VERTEX_ARRAY_BINDING);
            this.bindVertexArray(vaobj);
            GL43C.glBindVertexBuffer(bindingindex, buffer, offset, stride);
            this.bindVertexArray(previousVertexArray);
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

            switch (attribute.format) {
                case VertexAttribFFormat fformat -> GL20C.glVertexAttribPointer(index, fformat.size(), fformat.type(), fformat.normalized(), binding.stride, binding.offset + fformat.relativeoffset());
                case VertexAttribIFormat iformat -> GL30C.glVertexAttribIPointer(index, iformat.size(), iformat.type(), binding.stride, binding.offset + iformat.relativeoffset());
                case VertexAttribLFormat lformat -> GL41C.glVertexAttribLPointer(index, lformat.size(), lformat.type(), binding.stride, binding.offset + lformat.relativeoffset());
                case null, default -> throw new IllegalStateException("Unknown VertexAttribFormat");
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
                            return new GlslInvokeFunctionNode(new GlslVariableNode(replacement), List.of());
                        }
                    } else if (node instanceof GlslGetArrayNode getArrayNode) {
                        if (getArrayNode.getExpression() instanceof GlslInvokeFunctionNode invokeFunctionNode) {
                            invokeFunctionNode.getParameters().add(new GlslInvokeFunctionNode(new GlslPrimitiveConstructorNode(GlslTypeSpecifier.BuiltinType.INT), List.of(getArrayNode.getIndex())));
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
                    final GlslSpecifiedType structType = structNode.getType();
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
