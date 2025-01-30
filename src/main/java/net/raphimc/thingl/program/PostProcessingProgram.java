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

package net.raphimc.thingl.program;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.DrawMode;
import net.raphimc.thingl.resource.framebuffer.Framebuffer;
import net.raphimc.thingl.resource.program.Program;
import net.raphimc.thingl.resource.shader.Shader;
import net.raphimc.thingl.resource.vertexarray.VertexArray;
import net.raphimc.thingl.wrapper.GLStateTracker;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11C;

public class PostProcessingProgram extends Program {

    private static final VertexArray VAO = new VertexArray();

    static {
        VAO.setDebugName("Post-Processing VAO");
    }

    public PostProcessingProgram(final Shader vertexShader, final Shader fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    @Override
    public void bind() {
        super.bind();
        final Framebuffer currentFramebuffer = ThinGL.getImplementation().getCurrentFramebuffer();
        this.setUniform("u_ProjectionMatrix", new Matrix4f().setOrtho(0F, currentFramebuffer.getWidth(), currentFramebuffer.getHeight(), 0F, -5000F, 5000F));
        this.setUniform("u_Viewport", currentFramebuffer.getWidth(), currentFramebuffer.getHeight());
        this.setUniform("u_Quad", 0F, 0F, currentFramebuffer.getWidth(), currentFramebuffer.getHeight());
    }

    public final void renderScaledQuad(final float x1, final float y1, final float x2, final float y2) {
        final Vector2f scale = ThinGL.getImplementation().get2DScaleFactor();
        this.renderQuad(x1 * scale.x, y1 * scale.y, x2 * scale.x, y2 * scale.y);
    }

    public final void renderQuad(final float x1, final float y1, final float x2, final float y2) {
        this.bind();
        GLStateTracker.push();
        GLStateTracker.enable(GL11C.GL_BLEND);
        GLStateTracker.disable(GL11C.GL_DEPTH_TEST);
        GLStateTracker.pushDepthMask();
        GL11C.glDepthMask(false);
        this.renderQuad0(x1, y1, x2, y2);
        GLStateTracker.popDepthMask();
        GLStateTracker.pop();
        this.unbind();
    }

    public final void renderFullscreenQuad() {
        final Framebuffer currentFramebuffer = ThinGL.getImplementation().getCurrentFramebuffer();
        this.renderQuad(0F, 0F, currentFramebuffer.getWidth(), currentFramebuffer.getHeight());
    }

    protected void renderQuad0(final float x1, final float y1, final float x2, final float y2) {
        this.setUniform("u_Quad", x1, y1, x2, y2);
        VAO.drawArrays(DrawMode.TRIANGLES, 6, 0);
    }

}
