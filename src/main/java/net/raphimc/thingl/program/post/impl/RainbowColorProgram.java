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
package net.raphimc.thingl.program.post.impl;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.program.post.AuxInputPostProcessingProgram;
import net.raphimc.thingl.resource.shader.Shader;
import net.raphimc.thingl.wrapper.Blending;

public class RainbowColorProgram extends AuxInputPostProcessingProgram {

    private final long startTime = System.currentTimeMillis();

    public RainbowColorProgram(final Shader vertexShader, final Shader fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public void configureParameters() {
        this.configureParameters(10, 10, Direction.DOWN);
    }

    public void configureParameters(final int speedDivider, final int rainbowDivider, final Direction direction) {
        this.configureParameters(speedDivider, rainbowDivider, 0, direction);
    }

    public void configureParameters(final int speedDivider, final int rainbowDivider, final int offset, final Direction direction) {
        this.setUniformFloat("u_SpeedDivider", speedDivider);
        this.setUniformFloat("u_RainbowDivider", rainbowDivider);
        this.setUniformFloat("u_Offset", offset / 1000F);
        this.setUniformVector2f("u_Direction", direction.x, direction.y);
    }

    @Override
    public void bind() {
        super.bind();
        this.setUniformFloat("u_Time", (System.currentTimeMillis() - this.startTime) / 1_000F);
    }

    @Override
    protected void prepareAndRenderInternal(final float xtl, final float ytl, final float xbr, final float ybr) {
        ThinGL.glStateStack().pushBlendFunc();
        Blending.premultipliedAlphaBlending();
        super.prepareAndRenderInternal(xtl, ytl, xbr, ybr);
        ThinGL.glStateStack().popBlendFunc();
    }

    public enum Direction {
        NONE(0F, 0F),
        RIGHT(1F, 0F),
        LEFT(-1F, 0F),
        UP(0F, 1F),
        DOWN(0F, -1F),
        RIGHT_UP(1F, 1F),
        RIGHT_DOWN(1F, -1F),
        LEFT_UP(-1F, 1F),
        LEFT_DOWN(-1F, -1F),
        ;

        private final float x;
        private final float y;

        Direction(final float x, final float y) {
            this.x = x;
            this.y = y;
        }
    }

}
