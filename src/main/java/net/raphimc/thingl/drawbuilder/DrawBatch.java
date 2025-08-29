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
package net.raphimc.thingl.drawbuilder;

import net.raphimc.thingl.drawbuilder.vertex.VertexDataLayout;
import net.raphimc.thingl.resource.program.Program;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record DrawBatch(Supplier<Program> program, DrawMode drawMode, VertexDataLayout vertexDataLayout, VertexDataLayout instanceVertexDataLayout, Runnable setupAction, Runnable cleanupAction) {

    public static final DrawBatch[] EMPTY_ARRAY = new DrawBatch[0];

    public record Snippet(Supplier<Program> program, DrawMode drawMode, VertexDataLayout vertexDataLayout, VertexDataLayout instanceVertexDataLayout, Runnable setupAction, Runnable cleanupAction) {
    }

    public static class Builder {

        private Supplier<Program> program;
        private DrawMode drawMode;
        private VertexDataLayout vertexDataLayout;
        private VertexDataLayout instanceVertexDataLayout;
        private Runnable setupAction = () -> {
        };
        private Runnable cleanupAction = () -> {
        };

        public Builder() {
        }

        public Builder(final Snippet snippet) {
            this.program = snippet.program;
            this.drawMode = snippet.drawMode;
            this.vertexDataLayout = snippet.vertexDataLayout;
            this.instanceVertexDataLayout = snippet.instanceVertexDataLayout;
            this.setupAction = snippet.setupAction;
            this.cleanupAction = snippet.cleanupAction;
        }

        public Builder(final DrawBatch drawBatch) {
            this.program = drawBatch.program;
            this.drawMode = drawBatch.drawMode;
            this.vertexDataLayout = drawBatch.vertexDataLayout;
            this.instanceVertexDataLayout = drawBatch.instanceVertexDataLayout;
            this.setupAction = drawBatch.setupAction;
            this.cleanupAction = drawBatch.cleanupAction;
        }

        public Builder program(final Supplier<Program> program) {
            this.program = program;
            return this;
        }

        public Builder drawMode(final DrawMode drawMode) {
            this.drawMode = drawMode;
            return this;
        }

        public Builder vertexDataLayout(final VertexDataLayout vertexDataLayout) {
            this.vertexDataLayout = vertexDataLayout;
            return this;
        }

        public Builder instanceVertexDataLayout(final VertexDataLayout instanceVertexDataLayout) {
            this.instanceVertexDataLayout = instanceVertexDataLayout;
            return this;
        }

        public Builder setupAction(final Consumer<Program> setupAction) {
            return this.setupAction(() -> setupAction.accept(this.program.get()));
        }

        public Builder setupAction(final Runnable setupAction) {
            this.setupAction = setupAction;
            return this;
        }

        public Builder cleanupAction(final Consumer<Program> cleanupAction) {
            return this.cleanupAction(() -> cleanupAction.accept(this.program.get()));
        }

        public Builder cleanupAction(final Runnable cleanupAction) {
            this.cleanupAction = cleanupAction;
            return this;
        }

        public Builder prependSetupAction(final Consumer<Program> additionalSetupAction) {
            return this.prependSetupAction(() -> additionalSetupAction.accept(this.program.get()));
        }

        public Builder prependSetupAction(final Runnable additionalSetupAction) {
            final Runnable previousSetupAction = this.setupAction;
            this.setupAction = () -> {
                additionalSetupAction.run();
                previousSetupAction.run();
            };
            return this;
        }

        public Builder prependCleanupAction(final Consumer<Program> additionalCleanupAction) {
            return this.prependCleanupAction(() -> additionalCleanupAction.accept(this.program.get()));
        }

        public Builder prependCleanupAction(final Runnable additionalCleanupAction) {
            final Runnable previousCleanupAction = this.cleanupAction;
            this.cleanupAction = () -> {
                additionalCleanupAction.run();
                previousCleanupAction.run();
            };
            return this;
        }

        public Builder appendSetupAction(final Consumer<Program> additionalSetupAction) {
            return this.appendSetupAction(() -> additionalSetupAction.accept(this.program.get()));
        }

        public Builder appendSetupAction(final Runnable additionalSetupAction) {
            final Runnable previousSetupAction = this.setupAction;
            this.setupAction = () -> {
                previousSetupAction.run();
                additionalSetupAction.run();
            };
            return this;
        }

        public Builder appendCleanupAction(final Consumer<Program> additionalCleanupAction) {
            return this.appendCleanupAction(() -> additionalCleanupAction.accept(this.program.get()));
        }

        public Builder appendCleanupAction(final Runnable additionalCleanupAction) {
            final Runnable previousCleanupAction = this.cleanupAction;
            this.cleanupAction = () -> {
                previousCleanupAction.run();
                additionalCleanupAction.run();
            };
            return this;
        }

        public Builder fromSnippet(final Snippet snippet) {
            if (snippet.program != null) {
                this.program = snippet.program;
            }
            if (snippet.drawMode != null) {
                this.drawMode = snippet.drawMode;
            }
            if (snippet.vertexDataLayout != null) {
                this.vertexDataLayout = snippet.vertexDataLayout;
            }
            if (snippet.instanceVertexDataLayout != null) {
                this.instanceVertexDataLayout = snippet.instanceVertexDataLayout;
            }
            this.appendSetupAction(snippet.setupAction);
            this.prependCleanupAction(snippet.cleanupAction);
            return this;
        }

        public DrawBatch build() {
            if (this.program == null) {
                throw new IllegalStateException("Program must be set");
            }
            if (this.drawMode == null) {
                throw new IllegalStateException("Draw mode must be set");
            }
            if (this.vertexDataLayout == null) {
                throw new IllegalStateException("Vertex data layout must be set");
            }
            return new DrawBatch(this.program, this.drawMode, this.vertexDataLayout, this.instanceVertexDataLayout, this.setupAction, this.cleanupAction);
        }

        public Snippet buildSnippet() {
            return new Snippet(this.program, this.drawMode, this.vertexDataLayout, this.instanceVertexDataLayout, this.setupAction, this.cleanupAction);
        }

    }

}
