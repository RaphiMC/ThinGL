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

package net.raphimc.thingl.program.post;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.framebuffer.impl.TextureFramebuffer;
import net.raphimc.thingl.resource.shader.Shader;
import net.raphimc.thingl.util.pool.FramebufferPool;
import org.lwjgl.opengl.GL11C;

import java.util.function.Consumer;

public class SinglePassBufferedPostProcessingProgram<S extends SinglePassPostProcessingProgram<S>> extends SinglePassPostProcessingProgram<S> {

    public SinglePassBufferedPostProcessingProgram(final Shader vertexShader, final Shader fragmentShader, final Consumer<S> pass) {
        super(vertexShader, fragmentShader, pass);
    }

    @Override
    protected void renderQuad0(final float x1, final float y1, final float x2, final float y2) {
        final TextureFramebuffer tempFramebuffer = FramebufferPool.borrowFramebuffer(GL11C.GL_LINEAR);
        ThinGL.getImplementation().getCurrentFramebuffer().blitTo(tempFramebuffer, true, false, false);

        this.setUniformTexture("u_Source", tempFramebuffer);
        super.renderQuad0(x1, y1, x2, y2);

        FramebufferPool.returnFramebuffer(tempFramebuffer);
    }

}
