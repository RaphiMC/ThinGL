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
package net.raphimc.thingl.renderer.impl;

import net.lenni0451.commons.color.Color;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.BuiltinDrawBatches;
import net.raphimc.thingl.drawbuilder.DrawBatch;
import net.raphimc.thingl.drawbuilder.DrawMode;
import net.raphimc.thingl.drawbuilder.databuilder.holder.VertexDataHolder;
import net.raphimc.thingl.renderer.Primitives;
import net.raphimc.thingl.renderer.Renderer;
import net.raphimc.thingl.resource.image.texture.CubeMapTexture;
import net.raphimc.thingl.util.CacheUtil;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBf;
import org.joml.primitives.AABBi;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL32C;

import java.util.function.IntFunction;

public class Renderer3D extends Renderer {

    public static final byte FACE_DOWN = 0b00000001;
    public static final byte FACE_UP = 0b00000010;
    public static final byte FACE_NORTH = 0b00000100;
    public static final byte FACE_SOUTH = 0b00001000;
    public static final byte FACE_WEST = 0b00010000;
    public static final byte FACE_EAST = 0b00100000;
    private static final byte ALL_FACES = 0b00111111;

    protected final IntFunction<DrawBatch> skyBox = CacheUtil.memoizeInt(textureId -> new DrawBatch(() -> ThinGL.programs().getSkyBox(), DrawMode.QUADS, BuiltinDrawBatches.POSITION_LAYOUT, () -> {
        ThinGL.glStateStack().push();
        ThinGL.glStateStack().enable(GL11C.GL_BLEND);
        ThinGL.glStateStack().enable(GL32C.GL_TEXTURE_CUBE_MAP_SEAMLESS);
        ThinGL.glStateStack().pushDepthMask();
        ThinGL.glStateManager().setDepthMask(false);
        ThinGL.programs().getSkyBox().setUniformSampler("u_Texture", textureId);
    }, () -> {
        ThinGL.glStateStack().popDepthMask();
        ThinGL.glStateStack().pop();
    }));

    public void filledBox(final Matrix4f positionMatrix, final AABBd aabb, final Color color) {
        this.filledBox(positionMatrix, (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ, color);
    }

    public void filledBox(final Matrix4f positionMatrix, final AABBf aabb, final Color color) {
        this.filledBox(positionMatrix, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, color);
    }

    public void filledBox(final Matrix4f positionMatrix, final AABBi aabb, final Color color) {
        this.filledBox(positionMatrix, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, color);
    }

    public void filledBox(final Matrix4f positionMatrix, final float minX, float minY, final float minZ, final float maxX, final float maxY, final float maxZ, final Color color) {
        this.filledBox(positionMatrix, minX, minY, minZ, maxX, maxY, maxZ, color, (byte) 0);
    }

    public void filledBox(final Matrix4f positionMatrix, final AABBd aabb, final Color color, final byte excludedFaces) {
        this.filledBox(positionMatrix, (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ, color, excludedFaces);
    }

    public void filledBox(final Matrix4f positionMatrix, final AABBf aabb, final Color color, final byte excludedFaces) {
        this.filledBox(positionMatrix, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, color, excludedFaces);
    }

    public void filledBox(final Matrix4f positionMatrix, final AABBi aabb, final Color color, final byte excludedFaces) {
        this.filledBox(positionMatrix, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, color, excludedFaces);
    }

    public void filledBox(final Matrix4f positionMatrix, final float minX, float minY, final float minZ, final float maxX, final float maxY, final float maxZ, final Color color, final byte excludedFaces) {
        if (excludedFaces == ALL_FACES) return;
        final int abgrColor = color.toABGR();

        if ((excludedFaces & FACE_DOWN) == 0) {
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, abgrColor);
        }
        if ((excludedFaces & FACE_UP) == 0) {
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, minX, maxY, maxZ, abgrColor);
        }
        if ((excludedFaces & FACE_NORTH) == 0) {
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, abgrColor);
        }
        if ((excludedFaces & FACE_SOUTH) == 0) {
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, abgrColor);
        }
        if ((excludedFaces & FACE_WEST) == 0) {
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, abgrColor);
        }
        if ((excludedFaces & FACE_EAST) == 0) {
            Primitives.filledRectangle(positionMatrix, this.targetMultiDrawBatchDataHolder, maxX, maxY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, abgrColor);
        }

        this.drawIfNotBuffering();
    }

    public void outlineBox(final Matrix4f positionMatrix, final AABBd aabb, final float lineWidth, final Color color) {
        this.outlineBox(positionMatrix, (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ, lineWidth, color);
    }

    public void outlineBox(final Matrix4f positionMatrix, final AABBf aabb, final float lineWidth, final Color color) {
        this.outlineBox(positionMatrix, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, lineWidth, color);
    }

    public void outlineBox(final Matrix4f positionMatrix, final AABBi aabb, final float lineWidth, final Color color) {
        this.outlineBox(positionMatrix, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, lineWidth, color);
    }

    public void outlineBox(final Matrix4f positionMatrix, final float minX, float minY, final float minZ, final float maxX, final float maxY, final float maxZ, final float lineWidth, final Color color) {
        this.outlineBox(positionMatrix, minX, minY, minZ, maxX, maxY, maxZ, lineWidth, color, (byte) 0);
    }

    public void outlineBox(final Matrix4f positionMatrix, final AABBd aabb, final float lineWidth, final Color color, final byte excludedFaces) {
        this.outlineBox(positionMatrix, (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ, lineWidth, color, excludedFaces);
    }

    public void outlineBox(final Matrix4f positionMatrix, final AABBf aabb, final float lineWidth, final Color color, final byte excludedFaces) {
        this.outlineBox(positionMatrix, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, lineWidth, color, excludedFaces);
    }

    public void outlineBox(final Matrix4f positionMatrix, final AABBi aabb, final float lineWidth, final Color color, final byte excludedFaces) {
        this.outlineBox(positionMatrix, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, lineWidth, color, excludedFaces);
    }

    public void outlineBox(final Matrix4f positionMatrix, final float minX, float minY, final float minZ, final float maxX, final float maxY, final float maxZ, final float lineWidth, final Color color, final byte excludedFaces) {
        if (excludedFaces == ALL_FACES) return;
        final int abgrColor = color.toABGR();

        if ((excludedFaces & FACE_DOWN) == 0) {
            if ((excludedFaces & FACE_NORTH) == 0) {
                Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, minX, minY, minZ, maxX, minY, minZ, lineWidth, abgrColor);
            }
            if ((excludedFaces & FACE_EAST) == 0) {
                Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, maxX, minY, minZ, maxX, minY, maxZ, lineWidth, abgrColor);
            }
            if ((excludedFaces & FACE_SOUTH) == 0) {
                Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, minX, minY, maxZ, maxX, minY, maxZ, lineWidth, abgrColor);
            }
            if ((excludedFaces & FACE_WEST) == 0) {
                Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, minX, minY, minZ, minX, minY, maxZ, lineWidth, abgrColor);
            }
        }
        if ((excludedFaces & FACE_UP) == 0) {
            if ((excludedFaces & FACE_NORTH) == 0) {
                Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, minX, maxY, minZ, maxX, maxY, minZ, lineWidth, abgrColor);
            }
            if ((excludedFaces & FACE_EAST) == 0) {
                Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, maxX, maxY, minZ, maxX, maxY, maxZ, lineWidth, abgrColor);
            }
            if ((excludedFaces & FACE_SOUTH) == 0) {
                Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, minX, maxY, maxZ, maxX, maxY, maxZ, lineWidth, abgrColor);
            }
            if ((excludedFaces & FACE_WEST) == 0) {
                Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, minX, maxY, minZ, minX, maxY, maxZ, lineWidth, abgrColor);
            }
        }
        if ((excludedFaces & FACE_NORTH) == 0) {
            if ((excludedFaces & FACE_WEST) == 0) {
                Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, minX, minY, minZ, minX, maxY, minZ, lineWidth, abgrColor);
            }
            if ((excludedFaces & FACE_EAST) == 0) {
                Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, maxX, minY, minZ, maxX, maxY, minZ, lineWidth, abgrColor);
            }
        }
        if ((excludedFaces & FACE_SOUTH) == 0) {
            if ((excludedFaces & FACE_WEST) == 0) {
                Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, minX, minY, maxZ, minX, maxY, maxZ, lineWidth, abgrColor);
            }
            if ((excludedFaces & FACE_EAST) == 0) {
                Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, maxX, minY, maxZ, maxX, maxY, maxZ, lineWidth, abgrColor);
            }
        }

        this.drawIfNotBuffering();
    }

    public void line(final Matrix4f positionMatrix, final Vector3d start, final Vector3d end, final float width, final Color color) {
        this.line(positionMatrix, (float) start.x, (float) start.y, (float) start.z, (float) end.x, (float) end.y, (float) end.z, width, color);
    }

    public void line(final Matrix4f positionMatrix, final Vector3f start, final Vector3f end, final float width, final Color color) {
        this.line(positionMatrix, start.x, start.y, start.z, end.x, end.y, end.z, width, color);
    }

    public void line(final Matrix4f positionMatrix, final Vector3i start, final Vector3i end, final float width, final Color color) {
        this.line(positionMatrix, start.x, start.y, start.z, end.x, end.y, end.z, width, color);
    }

    public void line(final Matrix4f positionMatrix, final float x1, final float y1, final float z1, final float x2, final float y2, final float z2, final float width, final Color color) {
        Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, z1, x2, y2, z2, width, color.toABGR());
        this.drawIfNotBuffering();
    }

    public void line(final Matrix4f positionMatrix, final Vector3d start, final Vector3d end, final float width, final Color startColor, final Color endColor) {
        this.line(positionMatrix, (float) start.x, (float) start.y, (float) start.z, (float) end.x, (float) end.y, (float) end.z, width, startColor, endColor);
    }

    public void line(final Matrix4f positionMatrix, final Vector3f start, final Vector3f end, final float width, final Color startColor, final Color endColor) {
        this.line(positionMatrix, start.x, start.y, start.z, end.x, end.y, end.z, width, startColor, endColor);
    }

    public void line(final Matrix4f positionMatrix, final Vector3i start, final Vector3i end, final float width, final Color startColor, final Color endColor) {
        this.line(positionMatrix, start.x, start.y, start.z, end.x, end.y, end.z, width, startColor, endColor);
    }

    public void line(final Matrix4f positionMatrix, final float x1, final float y1, final float z1, final float x2, final float y2, final float z2, final float width, final Color startColor, final Color endColor) {
        Primitives.line(positionMatrix, this.targetMultiDrawBatchDataHolder, x1, y1, z1, x2, y2, z2, width, startColor.toABGR(), endColor.toABGR());
        this.drawIfNotBuffering();
    }

    public void skyBox(final CubeMapTexture cubeMapTexture) {
        final VertexDataHolder vertexDataHolder = this.targetMultiDrawBatchDataHolder.getVertexDataHolder(this.skyBox.apply(cubeMapTexture.getGlId()));

        // Right face (+X)
        vertexDataHolder.putVector3f(1F, 1F, 1F).endVertex();
        vertexDataHolder.putVector3f(1F, 1F, -1F).endVertex();
        vertexDataHolder.putVector3f(1F, -1F, -1F).endVertex();
        vertexDataHolder.putVector3f(1F, -1F, 1F).endVertex();

        // Left face (-X)
        vertexDataHolder.putVector3f(-1F, 1F, -1F).endVertex();
        vertexDataHolder.putVector3f(-1F, 1F, 1F).endVertex();
        vertexDataHolder.putVector3f(-1F, -1F, 1F).endVertex();
        vertexDataHolder.putVector3f(-1F, -1F, -1F).endVertex();

        // Top face (+Y)
        vertexDataHolder.putVector3f(-1F, 1F, -1F).endVertex();
        vertexDataHolder.putVector3f(1F, 1F, -1F).endVertex();
        vertexDataHolder.putVector3f(1F, 1F, 1F).endVertex();
        vertexDataHolder.putVector3f(-1F, 1F, 1F).endVertex();

        // Bottom face (-Y)
        vertexDataHolder.putVector3f(-1F, -1F, 1F).endVertex();
        vertexDataHolder.putVector3f(1F, -1F, 1F).endVertex();
        vertexDataHolder.putVector3f(1F, -1F, -1F).endVertex();
        vertexDataHolder.putVector3f(-1F, -1F, -1F).endVertex();

        // Front face (+Z)
        vertexDataHolder.putVector3f(-1F, 1F, 1F).endVertex();
        vertexDataHolder.putVector3f(1F, 1F, 1F).endVertex();
        vertexDataHolder.putVector3f(1F, -1F, 1F).endVertex();
        vertexDataHolder.putVector3f(-1F, -1F, 1F).endVertex();

        // Back face (-Z)
        vertexDataHolder.putVector3f(1F, 1F, -1F).endVertex();
        vertexDataHolder.putVector3f(-1F, 1F, -1F).endVertex();
        vertexDataHolder.putVector3f(-1F, -1F, -1F).endVertex();
        vertexDataHolder.putVector3f(1F, -1F, -1F).endVertex();

        this.drawIfNotBuffering();
    }

}
