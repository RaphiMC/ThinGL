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
package net.raphimc.thingl.resource.image.texture;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.GLObject;
import org.lwjgl.opengl.*;

public abstract class Texture extends GLObject {

    protected final Int2ObjectMap<Int2IntMap> levelParameters = new Int2ObjectOpenHashMap<>();

    public Texture(final int target) {
        super(GL45C.glCreateTextures(target));
    }

    protected Texture(final int glId, final Object unused) {
        super(glId);
    }

    public static Texture fromGlId(final int glId) {
        if (!GL11C.glIsTexture(glId)) {
            throw new IllegalArgumentException("Not a texture object");
        }
        final int target = getTextureTarget(glId);
        return switch (target) {
            case GL11C.GL_TEXTURE_1D -> new Texture1D(glId);
            case GL30C.GL_TEXTURE_1D_ARRAY -> new Texture1DArray(glId);
            case GL11C.GL_TEXTURE_2D -> new Texture2D(glId);
            case GL30C.GL_TEXTURE_2D_ARRAY -> new Texture2DArray(glId);
            case GL32C.GL_TEXTURE_2D_MULTISAMPLE -> new MultisampleTexture2D(glId);
            case GL32C.GL_TEXTURE_2D_MULTISAMPLE_ARRAY -> new MultisampleTexture2DArray(glId);
            case GL12C.GL_TEXTURE_3D -> new Texture3D(glId);
            case GL13C.GL_TEXTURE_CUBE_MAP -> new CubeMapTexture(glId);
            case GL40C.GL_TEXTURE_CUBE_MAP_ARRAY -> new CubeMapArrayTexture(glId);
            case GL31C.GL_TEXTURE_BUFFER -> new BufferTexture(glId);
            default -> throw new IllegalArgumentException("Unsupported texture target: " + target);
        };
    }

    @Override
    protected void free0() {
        GL11C.glDeleteTextures(this.getGlId());
    }

    @Override
    public final int getGlType() {
        return GL11C.GL_TEXTURE;
    }

    public int getLevelParameterInt(final int level, final int parameter) {
        Int2IntMap parameters = this.levelParameters.get(level);
        if (parameters == null) {
            parameters = new Int2IntOpenHashMap();
            this.levelParameters.put(level, parameters);
        }
        if (!parameters.containsKey(parameter)) {
            parameters.put(parameter, GL45C.glGetTextureLevelParameteri(this.getGlId(), level, parameter));
        }
        return parameters.get(parameter);
    }

    public abstract int getTarget();

    public int getInternalFormat() {
        return this.getInternalFormat(0);
    }

    public int getInternalFormat(final int level) {
        return this.getLevelParameterInt(level, GL11C.GL_TEXTURE_INTERNAL_FORMAT);
    }

    public int getWidth() {
        return this.getWidth(0);
    }

    public int getWidth(final int level) {
        return this.getLevelParameterInt(level, GL11C.GL_TEXTURE_WIDTH);
    }

    public int getHeight() {
        return this.getHeight(0);
    }

    public int getHeight(final int level) {
        return this.getLevelParameterInt(level, GL11C.GL_TEXTURE_HEIGHT);
    }

    public int getDepth() {
        return this.getDepth(0);
    }

    public int getDepth(final int level) {
        return this.getLevelParameterInt(level, GL12C.GL_TEXTURE_DEPTH);
    }


    protected static int getTextureTarget(final int glId) {
        if (!ThinGL.workarounds().isGetTextureParameterTextureTargetBroken()) {
            return GL45C.glGetTextureParameteri(glId, GL45C.GL_TEXTURE_TARGET);
        } else {
            final int depth = GL45C.glGetTextureLevelParameteri(glId, 0, GL12C.GL_TEXTURE_DEPTH);
            final int samples = GL45C.glGetTextureLevelParameteri(glId, 0, GL32C.GL_TEXTURE_SAMPLES);
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
    }

}
