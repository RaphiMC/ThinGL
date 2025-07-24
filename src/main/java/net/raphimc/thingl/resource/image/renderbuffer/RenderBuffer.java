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
package net.raphimc.thingl.resource.image.renderbuffer;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.raphimc.thingl.resource.GLObject;
import net.raphimc.thingl.resource.image.ImageStorage;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL45C;

public abstract class RenderBuffer extends GLObject implements ImageStorage {

    protected final Int2IntMap parameters = new Int2IntOpenHashMap();

    public RenderBuffer() {
        super(GL45C.glCreateRenderbuffers());
    }

    protected RenderBuffer(final int glId) {
        super(glId);
    }

    public static RenderBuffer fromGlId(final int glId) {
        if (!GL30C.glIsRenderbuffer(glId)) {
            throw new IllegalArgumentException("Not a renderbuffer object");
        }
        return fromGlIdUnsafe(glId);
    }

    public static RenderBuffer fromGlIdUnsafe(final int glId) {
        final int samples = GL45C.glGetNamedRenderbufferParameteri(glId, GL30C.GL_RENDERBUFFER_SAMPLES);
        if (samples == 0) {
            return StandardRenderBuffer.fromGlIdUnsafe(glId);
        } else {
            return MultisampleRenderBuffer.fromGlIdUnsafe(glId);
        }
    }

    @Override
    public void copyTo(final ImageStorage target, final int srcLevel, final int srcX, final int srcY, final int srcZ, final int dstLevel, final int dstX, final int dstY, final int dstZ, final int width, final int height, final int depth) {
        GL43C.glCopyImageSubData(this.getGlId(), this.getTarget(), srcLevel, srcX, srcY, srcZ, target.getGlId(), target.getTarget(), dstLevel, dstX, dstY, dstZ, width, height, depth);
    }

    @Override
    protected void free0() {
        GL30C.glDeleteRenderbuffers(this.getGlId());
    }

    @Override
    public final int getGlType() {
        return GL30C.GL_RENDERBUFFER;
    }

    @Override
    public int getTarget() {
        return GL30C.GL_RENDERBUFFER;
    }

    public int getParameterInt(final int parameter) {
        if (!this.parameters.containsKey(parameter)) {
            this.parameters.put(parameter, GL45C.glGetNamedRenderbufferParameteri(this.getGlId(), parameter));
        }
        return this.parameters.get(parameter);
    }

    @Override
    public int getInternalFormat() {
        return this.getParameterInt(GL30C.GL_RENDERBUFFER_INTERNAL_FORMAT);
    }

    @Override
    public int getWidth() {
        return this.getParameterInt(GL30C.GL_RENDERBUFFER_WIDTH);
    }

    @Override
    public int getHeight() {
        return this.getParameterInt(GL30C.GL_RENDERBUFFER_HEIGHT);
    }

}
