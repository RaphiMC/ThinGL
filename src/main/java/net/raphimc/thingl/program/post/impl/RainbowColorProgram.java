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
import net.raphimc.thingl.program.post.SinglePassPostProcessingProgram;
import net.raphimc.thingl.resource.shader.Shader;
import net.raphimc.thingl.wrapper.Blending;

public class RainbowColorProgram extends SinglePassPostProcessingProgram<RainbowColorProgram> {

    private final long startTime = System.currentTimeMillis();

    private int speedDivider = 10;
    private int rainbowDivider = 10;
    private int offset = 0;
    private Direction direction = Direction.DOWN;

    public RainbowColorProgram(final Shader vertexShader, final Shader fragmentShader) {
        super(vertexShader, fragmentShader, s -> {
            s.setUniform("u_Time", (System.currentTimeMillis() - s.startTime) / 1_000F);
            s.setUniform("u_SpeedDivider", (float) s.speedDivider);
            s.setUniform("u_RainbowDivider", (float) s.rainbowDivider);
            s.setUniform("u_Offset", s.offset / 1000F);
            s.setUniform("u_Direction", s.direction.x, s.direction.y);
        });
    }

    public void configureParameters(final int speedDivider, final int rainbowDivider, final int offset, final Direction direction) {
        this.speedDivider = speedDivider;
        this.rainbowDivider = rainbowDivider;
        this.offset = offset;
        this.direction = direction;
    }

    @Override
    protected void renderQuad0(final float x1, final float y1, final float x2, final float y2) {
        ThinGL.glStateTracker().pushBlendFunc();
        Blending.additiveBlending();
        super.renderQuad0(x1, y1, x2, y2);
        ThinGL.glStateTracker().popBlendFunc();
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
