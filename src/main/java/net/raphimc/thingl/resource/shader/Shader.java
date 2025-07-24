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
package net.raphimc.thingl.resource.shader;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.GLObject;
import org.lwjgl.opengl.*;

public class Shader extends GLObject {

    private Integer type;
    private String source;

    public Shader(final Type type, final String source) {
        super(GL20C.glCreateShader(type.getGlType()));
        this.type = type.getGlType();
        try {
            this.setSource(source);
            this.compile();
        } catch (Throwable e) {
            this.free();
            throw e;
        }
    }

    protected Shader(final int glId) {
        super(glId);
    }

    public static Shader fromGlId(final int glId) {
        if (!GL20C.glIsShader(glId)) {
            throw new IllegalArgumentException("Not a shader object");
        }
        return fromGlIdUnsafe(glId);
    }

    public static Shader fromGlIdUnsafe(final int glId) {
        return new Shader(glId);
    }

    public void compile() {
        GL20C.glCompileShader(this.getGlId());
        final String compileLog = GL20C.glGetShaderInfoLog(this.getGlId());
        if (GL20C.glGetShaderi(this.getGlId(), GL20C.GL_COMPILE_STATUS) == GL11C.GL_FALSE) {
            throw new IllegalStateException("Error compiling shader: " + compileLog);
        } else if (!compileLog.isBlank()) {
            ThinGL.LOGGER.warn("Shader compile log: " + compileLog);
        }
    }

    @Override
    protected void free0() {
        GL20C.glDeleteShader(this.getGlId());
    }

    @Override
    public final int getGlType() {
        return GL43C.GL_SHADER;
    }

    public int getType() {
        if (this.type == null) {
            this.type = GL20C.glGetShaderi(this.getGlId(), GL20C.GL_SHADER_TYPE);
        }
        return this.type;
    }

    public Type getTypeEnum() {
        return Type.fromGlType(this.getType());
    }

    public String getSource() {
        if (this.source == null) {
            this.source = GL20C.glGetShaderSource(this.getGlId());
        }
        return this.source;
    }

    public void setSource(final String source) {
        this.source = source;
        GL20C.glShaderSource(this.getGlId(), source);
    }

    public enum Type {

        VERTEX(GL20C.GL_VERTEX_SHADER, "Vertex Shader", "vert"),
        FRAGMENT(GL20C.GL_FRAGMENT_SHADER, "Fragment Shader", "frag"),
        GEOMETRY(GL32C.GL_GEOMETRY_SHADER, "Geometry Shader", "geom"),
        TESSELLATION_CONTROL(GL40C.GL_TESS_CONTROL_SHADER, "Tessellation control Shader", "tesc"),
        TESSELLATION_EVALUATION(GL40C.GL_TESS_EVALUATION_SHADER, "Tessellation evaluation Shader", "tese"),
        COMPUTE(GL43C.GL_COMPUTE_SHADER, "Compute Shader", "comp"),
        NV_MESH(NVMeshShader.GL_MESH_SHADER_NV, "NVIDIA Mesh Shader", "mesh"),
        NV_TASK(NVMeshShader.GL_TASK_SHADER_NV, "NVIDIA Task Shader", "task"),
        ;

        public static Type fromGlType(final int glType) {
            for (Type type : values()) {
                if (type.glType == glType) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Unknown shader type: " + glType);
        }

        private final int glType;
        private final String displayName;
        private final String fileExtension;

        Type(final int glType, final String displayName, final String fileExtension) {
            this.glType = glType;
            this.displayName = displayName;
            this.fileExtension = fileExtension;
        }

        public int getGlType() {
            return this.glType;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getFileExtension() {
            return this.fileExtension;
        }

    }

}
