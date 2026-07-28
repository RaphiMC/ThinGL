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
package net.raphimc.thingl.util.rectpack;

/**
 * Based on <a href="https://github.com/nothings/stb/blob/31c1ad37456438565541f4919958214b6e762fb4/stb_rect_pack.h">stb_rect_pack</a>.<br>
 */
public class StaticRectanglePacker {

    private final int width;
    private final int height;
    private final int spacing;

    private SkylineNode skylineHead;

    public StaticRectanglePacker(final int width, final int height) {
        this(width, height, 1);
    }

    public StaticRectanglePacker(final int width, final int height, final int spacing) {
        this.width = width;
        this.height = height;
        this.spacing = spacing;
        this.skylineHead = new SkylineNode(0, 0, new SkylineNode(width, Integer.MAX_VALUE, null));
    }

    public Slot pack(final int rectWidth, final int rectHeight) {
        final int paddedRectWidth = rectWidth + this.spacing;
        final int paddedRectHeight = rectHeight + this.spacing;
        if (paddedRectWidth > this.width || paddedRectHeight > this.height) {
            return null;
        }
        final Placement placement = this.findBestPlacement(paddedRectWidth, paddedRectHeight);
        if (placement == null) {
            return null;
        }
        this.updateSkyline(placement);

        final int x = placement.x();
        final int y = placement.y();
        final float u1 = x / (float) this.width;
        final float v1 = y / (float) this.height;
        final float u2 = (x + rectWidth) / (float) this.width;
        final float v2 = (y + rectHeight) / (float) this.height;
        return new Slot(x, y, rectWidth, rectHeight, u1, v1, u2, v2);
    }

    private void updateSkyline(final Placement placement) {
        SkylineNode current = placement.current();
        final int right = placement.x() + placement.width();
        while (current.next != null && current.next.x <= right) {
            current = current.next;
        }
        if (current.x < right) {
            current.x = right;
        }

        final SkylineNode newNode = new SkylineNode(placement.x(), placement.y() + placement.height(), current);
        if (newNode.y == newNode.next.y) {
            newNode.next = newNode.next.next;
        }

        final SkylineNode previous = placement.previous();
        if (previous != null) {
            if (previous.y == newNode.y) {
                previous.next = newNode.next;
            } else {
                previous.next = newNode;
            }
        } else {
            this.skylineHead = newNode;
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    private Placement findBestPlacement(final int width, final int height) {
        int bestY = Integer.MAX_VALUE;
        SkylineNode bestPrevious = null;
        SkylineNode bestCurrent = null;

        SkylineNode previous = null;
        SkylineNode current = this.skylineHead;
        while (current.x + width <= this.width) {
            int candidateY = 0;
            for (SkylineNode node = current; node.x < current.x + width; node = node.next) {
                candidateY = Math.max(candidateY, node.y);
            }
            if (candidateY < bestY) {
                bestY = candidateY;
                bestPrevious = previous;
                bestCurrent = current;
            }
            previous = current;
            current = current.next;
        }

        if (bestCurrent == null || bestY + height > this.height) {
            return null;
        }
        return new Placement(bestCurrent.x, bestY, width, height, bestPrevious, bestCurrent);
    }

    private static class SkylineNode {

        private int x;
        private int y;
        private SkylineNode next;

        private SkylineNode(final int x, final int y, final SkylineNode next) {
            this.x = x;
            this.y = y;
            this.next = next;
        }

    }

    private record Placement(int x, int y, int width, int height, SkylineNode previous, SkylineNode current) {
    }

}
