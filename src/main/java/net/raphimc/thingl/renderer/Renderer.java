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
package net.raphimc.thingl.renderer;

import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.ImmediateMultiDrawBatchDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.MultiDrawBatchDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.PersistentMultiDrawBatchDataHolder;

public abstract class Renderer {

    protected final MultiDrawBatchDataHolder immediateMultiDrawBatchDataHolder;
    protected MultiDrawBatchDataHolder targetMultiDrawBatchDataHolder;

    public Renderer() {
        this(new ImmediateMultiDrawBatchDataHolder());
    }

    protected Renderer(final ImmediateMultiDrawBatchDataHolder immediateMultiDrawBatchDataHolder) {
        this.immediateMultiDrawBatchDataHolder = immediateMultiDrawBatchDataHolder;
        this.targetMultiDrawBatchDataHolder = immediateMultiDrawBatchDataHolder;
    }

    public void beginGlobalBuffering() {
        this.beginBuffering(ThinGL.globalDrawBatch());
    }

    public void beginBuffering(final MultiDrawBatchDataHolder targetMultiDrawBatchDataHolder) {
        this.targetMultiDrawBatchDataHolder = targetMultiDrawBatchDataHolder;
    }

    public MultiDrawBatchDataHolder endBuffering() {
        if (!this.isBuffering()) {
            throw new IllegalStateException("Not buffering");
        }

        final MultiDrawBatchDataHolder targetMultiDrawBatchDataHolder = this.targetMultiDrawBatchDataHolder;
        this.targetMultiDrawBatchDataHolder = this.immediateMultiDrawBatchDataHolder;
        return targetMultiDrawBatchDataHolder;
    }

    public PersistentMultiDrawBatchDataHolder endPersistentBufferingAndBuild() {
        if (this.endBuffering() instanceof PersistentMultiDrawBatchDataHolder persistentMultiDrawBatchDataHolder) {
            persistentMultiDrawBatchDataHolder.build();
            return persistentMultiDrawBatchDataHolder;
        } else {
            throw new IllegalStateException("Not buffering into a PersistentMultiDrawBatchDataHolder");
        }
    }

    public void free() {
        this.immediateMultiDrawBatchDataHolder.free();
    }

    public boolean isBuffering() {
        return this.targetMultiDrawBatchDataHolder != this.immediateMultiDrawBatchDataHolder;
    }

    public MultiDrawBatchDataHolder getTargetMultiDrawBatchDataHolder() {
        return this.targetMultiDrawBatchDataHolder;
    }

    protected void drawIfNotBuffering() {
        if (!this.isBuffering()) {
            this.immediateMultiDrawBatchDataHolder.draw();
        }
    }

}
