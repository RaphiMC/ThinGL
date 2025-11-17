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
package net.raphimc.thingl.gl.resource.query;

import net.raphimc.thingl.gl.resource.GLObject;
import org.lwjgl.opengl.*;

public class Query extends GLObject {

    private Integer target;

    public Query(final int target) {
        super(GL45C.glCreateQueries(target));
        this.target = target;
    }

    protected Query(final int glId, final Object unused) {
        super(glId);
    }

    public static Query fromGlId(final int glId) {
        if (!GL15C.glIsQuery(glId)) {
            throw new IllegalArgumentException("Not a query object");
        }
        return fromGlIdUnsafe(glId);
    }

    public static Query fromGlIdUnsafe(final int glId) {
        return new Query(glId, null);
    }

    public void begin() {
        GL15C.glBeginQuery(this.getTarget(), this.getGlId());
    }

    public void end() {
        GL15C.glEndQuery(this.getTarget());
    }

    public boolean isResultAvailable() {
        return GL15C.glGetQueryObjecti(this.getGlId(), GL15C.GL_QUERY_RESULT_AVAILABLE) == GL11C.GL_TRUE;
    }

    public boolean getResultBoolean() {
        return GL15C.glGetQueryObjecti(this.getGlId(), GL15C.GL_QUERY_RESULT) == GL11C.GL_TRUE;
    }

    public int getResultInt() {
        return GL15C.glGetQueryObjecti(this.getGlId(), GL15C.GL_QUERY_RESULT);
    }

    public long getResultLong() {
        return GL33C.glGetQueryObjecti64(this.getGlId(), GL15C.GL_QUERY_RESULT);
    }

    @Override
    protected void free0() {
        GL15C.glDeleteQueries(this.getGlId());
    }

    @Override
    public final int getGlType() {
        return GL43C.GL_QUERY;
    }

    public int getTarget() {
        if (this.target == null) {
            this.target = GL15C.glGetQueryObjecti(this.getGlId(), GL45C.GL_QUERY_TARGET);
        }
        return this.target;
    }

}
