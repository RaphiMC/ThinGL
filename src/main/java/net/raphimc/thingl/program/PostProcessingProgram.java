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
import net.raphimc.thingl.resource.program.Program;
import net.raphimc.thingl.resource.shader.Shader;
import net.raphimc.thingl.util.RenderMathUtil;
import net.raphimc.thingl.wrapper.GLStateManager;
import org.joml.Matrix4f;
import org.joml.primitives.Rectanglei;
import org.lwjgl.opengl.GL11C;

public class PostProcessingProgram extends Program {

    public PostProcessingProgram(final Shader vertexShader, final Shader fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    @Override
    public void bind() {
        super.bind();
        final GLStateManager.Viewport viewport = ThinGL.glStateManager().getViewport();
        this.setUniformMatrix4f("u_ProjectionMatrix", new Matrix4f().setOrtho(0F, viewport.width(), viewport.height(), 0F, -1000F, 1000F));
        this.setUniformVector2f("u_Viewport", viewport.width(), viewport.height());
    }

    public final void renderFullscreen() {
        final GLStateManager.Viewport viewport = ThinGL.glStateManager().getViewport();
        this.prepareAndRenderInternal(0F, 0F, viewport.width(), viewport.height());
    }

    public final void render(final float xtl, final float ytl, final float xbr, final float ybr) {
        this.render(RenderMathUtil.getIdentityMatrix(), xtl, ytl, xbr, ybr);
    }

    public final void render(final Matrix4f positionMatrix, final float xtl, final float ytl, final float xbr, final float ybr) {
        final Rectanglei rectangle = RenderMathUtil.getWindowRectangle(positionMatrix, xtl, ytl, xbr, ybr, true);
        final GLStateManager.Viewport viewport = ThinGL.glStateManager().getViewport();
        rectangle.translate(-viewport.x(), viewport.y());
        this.prepareAndRenderInternal(rectangle.minX, rectangle.minY, rectangle.maxX, rectangle.maxY);
    }

    protected void prepareAndRenderInternal(final float xtl, final float ytl, final float xbr, final float ybr) {
        this.bind();
        ThinGL.glStateStack().push();
        ThinGL.glStateStack().enable(GL11C.GL_BLEND);
        ThinGL.glStateStack().disable(GL11C.GL_DEPTH_TEST);
        ThinGL.glStateStack().pushDepthMask();
        ThinGL.glStateManager().setDepthMask(false);
        this.renderInternal(xtl, ytl, xbr, ybr);
        ThinGL.glStateStack().popDepthMask();
        ThinGL.glStateStack().pop();
        this.unbind();
    }

    protected void renderInternal(final float xtl, final float ytl, final float xbr, final float ybr) {
        this.setUniformVector4f("u_Rectangle", xtl, ytl, xbr, ybr);
        ThinGL.immediateVertexArrays().getPostProcessingVao().drawArrays(DrawMode.TRIANGLES, 6, 0);
    }

}
