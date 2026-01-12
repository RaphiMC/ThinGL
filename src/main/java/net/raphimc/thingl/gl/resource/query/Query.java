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
package net.raphimc.thingl.gl.resource.query;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.GLObject;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL45C;

public class Query extends GLObject {

    private Integer target;

    public Query(final int target) {
        super(ThinGL.glBackend().createQuery(target));
        this.target = target;
    }

    protected Query(final int glId, final Object unused) {
        super(glId);
    }

    public static Query fromGlId(final int glId) {
        if (!ThinGL.glBackend().isQuery(glId)) {
            throw new IllegalArgumentException("Not a query object");
        }
        return fromGlIdUnsafe(glId);
    }

    public static Query fromGlIdUnsafe(final int glId) {
        return new Query(glId, null);
    }

    public void begin() {
        ThinGL.glBackend().beginQuery(this.getTarget(), this.getGlId());
    }

    public void end() {
        ThinGL.glBackend().endQuery(this.getTarget());
    }

    public boolean isResultAvailable() {
        return ThinGL.glBackend().getQueryObjecti(this.getGlId(), GL15C.GL_QUERY_RESULT_AVAILABLE) == GL11C.GL_TRUE;
    }

    public boolean getResultBoolean() {
        return ThinGL.glBackend().getQueryObjecti(this.getGlId(), GL15C.GL_QUERY_RESULT) == GL11C.GL_TRUE;
    }

    public int getResultInt() {
        return ThinGL.glBackend().getQueryObjecti(this.getGlId(), GL15C.GL_QUERY_RESULT);
    }

    public long getResultLong() {
        return ThinGL.glBackend().getQueryObjecti64(this.getGlId(), GL15C.GL_QUERY_RESULT);
    }

    @Override
    protected void free0() {
        ThinGL.glBackend().deleteQuery(this.getGlId());
    }

    @Override
    public final int getGlType() {
        return GL43C.GL_QUERY;
    }

    public int getTarget() {
        if (this.target == null) {
            this.target = ThinGL.glBackend().getQueryObjecti(this.getGlId(), GL45C.GL_QUERY_TARGET);
        }
        return this.target;
    }

}
