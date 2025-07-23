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

import net.raphimc.thingl.drawbuilder.builder.BufferBuilder;
import net.raphimc.thingl.implementation.application.StandaloneApplicationRunner;
import net.raphimc.thingl.resource.buffer.Buffer;
import net.raphimc.thingl.resource.buffer.ImmutableBuffer;
import net.raphimc.thingl.resource.program.Program;
import net.raphimc.thingl.resource.shader.Shader;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class ComputeShaderExample extends StandaloneApplicationRunner {

    private static final int COUNT = 10;
    private static final String SHADER_SOURCE = """
                #version 430 core

                layout (std430) buffer ssbo_Counter {
                    int counter[];
                };

                layout (local_size_x = 1, local_size_y = 1, local_size_z = 1) in;

                void main() {
                    counter[gl_GlobalInvocationID.x] += int(gl_GlobalInvocationID.x);
                }
            """;

    public static void main(String[] args) {
        new ComputeShaderExample().launch();
    }

    public ComputeShaderExample() {
        super(new Configuration().setWindowTitle("ThinGL Example - Compute Shader").setExtendedDebugMode(true));
    }

    @Override
    protected void init() {
        super.init();
        final Shader computeShader = new Shader(Shader.Type.COMPUTE, SHADER_SOURCE);
        final Program computeProgram = new Program(computeShader);

        final BufferBuilder counterBufferBuilder = new BufferBuilder();
        for (int i = 0; i < COUNT; i++) {
            counterBufferBuilder.putInt(i + 10);
        }
        final Buffer counterBuffer = new ImmutableBuffer(counterBufferBuilder.finish(), GL44C.GL_CLIENT_STORAGE_BIT);
        counterBufferBuilder.free();

        computeProgram.bind();
        computeProgram.setShaderStorageBuffer("ssbo_Counter", counterBuffer);
        GL43.glDispatchCompute(COUNT, 1, 1);
        GL42.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);
        computeProgram.unbind();

        final int[] counterResult = new int[10];
        final ByteBuffer counterResultBuffer = counterBuffer.download();
        counterResultBuffer.asIntBuffer().get(counterResult);
        MemoryUtil.memFree(counterResultBuffer);

        for (int i = 0; i < COUNT; i++) {
            System.out.println("Counter[" + i + "] = " + counterResult[i]);
        }
        System.exit(0);
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
    }

}
