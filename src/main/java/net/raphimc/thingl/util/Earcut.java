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
package net.raphimc.thingl.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import org.joml.Vector2f;

import java.util.Collection;
import java.util.List;

/**
 * Based on <a href="https://github.com/maplibre/earcut4j/blob/abc69f30e10134936a29cb7c4e1175636062d946/src/main/java/org/maplibre/earcut4j/Earcut.java">earcut4j</a>.<br>
 * Original licensed under the <a href="https://github.com/maplibre/earcut4j/blob/abc69f30e10134936a29cb7c4e1175636062d946/LICENSE">ISC</a> license.
 */
public class Earcut {

    public static IntList earcut(final List<Vector2f> points) {
        final Node startNode = buildLinkedRing(points, true);
        if (startNode == null || startNode.next == startNode.prev) {
            return IntLists.emptyList();
        }
        final PolygonBounds bounds = points.size() > 75 ? new PolygonBounds(points) : null; // if the shape is not too simple, we'll use z-order curve hash later
        final IntList triangles = new IntArrayList();
        triangulateLinked(startNode, triangles, bounds, 0);
        return triangles;
    }

    private static Node buildLinkedRing(final List<Vector2f> points, final boolean clockwise) {
        float signedArea = 0F;
        Vector2f previousPoint = points.getLast();
        for (Vector2f point : points) {
            signedArea += (previousPoint.x - point.x) * (point.y + previousPoint.y);
            previousPoint = point;
        }

        Node last = null;
        if (clockwise == signedArea > 0F) {
            for (int i = 0; i < points.size(); i++) {
                last = new Node(points.get(i), i, last);
            }
        } else {
            for (int i = points.size() - 1; i >= 0; i--) {
                last = new Node(points.get(i), i, last);
            }
        }
        if (last != null && last.equals(last.next)) {
            last.remove();
            last = last.next;
        }
        return last;
    }

    private static void triangulateLinked(Node ear, final IntList triangles, final PolygonBounds bounds, final int pass) {
        if (ear == null) {
            return;
        }

        if (bounds != null && pass == 0) { // interlink polygon nodes in z-order
            indexByZOrder(ear, bounds);
        }

        Node stop = ear;
        while (ear.prev != ear.next) { // iterate through ears, slicing them one by one
            final Node prev = ear.prev;
            final Node next = ear.next;

            if (bounds != null ? isEarHashed(ear, bounds) : isEar(ear)) {
                // cut off the triangle
                triangles.add(prev.i);
                triangles.add(ear.i);
                triangles.add(next.i);
                ear.remove();

                // skipping the next vertex leads to less sliver triangles
                ear = next.next;
                stop = next.next;
                continue;
            }

            ear = next;

            if (ear == stop) { // if we looped through the whole remaining polygon and can't find any more ears
                if (pass == 0) {
                    // try filtering points and slicing again
                    triangulateLinked(removeRedundantPoints(ear, null), triangles, bounds, 1);
                } else if (pass == 1) {
                    // if this didn't work, try curing all small self-intersections locally
                    triangulateLinked(resolveLocalIntersections(removeRedundantPoints(ear, null), triangles), triangles, bounds, 2);
                } else if (pass == 2) {
                    // as a last resort, try splitting the remaining polygon into two
                    splitAndTriangulate(ear, triangles, bounds);
                }
                break;
            }
        }
    }

    private static boolean isEar(final Node ear) {
        if (orientedTriangleArea(ear.prev, ear, ear.next) >= 0F) {
            return false; // reflex, can't be an ear
        }

        // now make sure we don't have other points inside the potential ear
        Node p = ear.next.next;
        while (p != ear.prev) {
            if (isPointInTriangleExcludingFirstVertex(ear.prev.x, ear.prev.y, ear.x, ear.y, ear.next.x, ear.next.y, p.x, p.y) && orientedTriangleArea(p.prev, p, p.next) >= 0F) {
                return false;
            }
            p = p.next;
        }
        return true;
    }

    private static boolean isEarHashed(final Node ear, final PolygonBounds bounds) {
        if (orientedTriangleArea(ear.prev, ear, ear.next) >= 0F) {
            return false; // reflex, can't be an ear
        }

        // triangle bbox; min & max are calculated like this for speed
        final float minTX = Math.min(ear.prev.x, Math.min(ear.x, ear.next.x));
        final float minTY = Math.min(ear.prev.y, Math.min(ear.y, ear.next.y));
        final float maxTX = Math.max(ear.prev.x, Math.max(ear.x, ear.next.x));
        final float maxTY = Math.max(ear.prev.y, Math.max(ear.y, ear.next.y));

        // z-order range for the current triangle bbox;
        final int minZ = computeZOrder(minTX, minTY, bounds);
        final int maxZ = computeZOrder(maxTX, maxTY, bounds);

        Node p = ear.prevZ;
        Node n = ear.nextZ;
        while (p != null && p.z >= minZ && n != null && n.z <= maxZ) { // first look for points inside the triangle in increasing z-order
            if (p != ear.prev && p != ear.next && isPointInTriangleExcludingFirstVertex(ear.prev.x, ear.prev.y, ear.x, ear.y, ear.next.x, ear.next.y, p.x, p.y) && orientedTriangleArea(p.prev, p, p.next) >= 0F) {
                return false;
            }
            p = p.prevZ;

            if (n != ear.prev && n != ear.next && isPointInTriangleExcludingFirstVertex(ear.prev.x, ear.prev.y, ear.x, ear.y, ear.next.x, ear.next.y, n.x, n.y) && orientedTriangleArea(n.prev, n, n.next) >= 0F) {
                return false;
            }
            n = n.nextZ;
        }

        while (p != null && p.z >= minZ) { // look for remaining points in decreasing z-order
            if (p != ear.prev && p != ear.next && isPointInTriangleExcludingFirstVertex(ear.prev.x, ear.prev.y, ear.x, ear.y, ear.next.x, ear.next.y, p.x, p.y) && orientedTriangleArea(p.prev, p, p.next) >= 0F) {
                return false;
            }
            p = p.prevZ;
        }

        while (n != null && n.z <= maxZ) { // look for remaining points in increasing z-order
            if (n != ear.prev && n != ear.next && isPointInTriangleExcludingFirstVertex(ear.prev.x, ear.prev.y, ear.x, ear.y, ear.next.x, ear.next.y, n.x, n.y) && orientedTriangleArea(n.prev, n, n.next) >= 0F) {
                return false;
            }
            n = n.nextZ;
        }

        return true;
    }

    private static Node resolveLocalIntersections(Node start, final IntList triangles) {
        Node p = start;
        do {
            final Node a = p.prev;
            final Node b = p.next.next;

            if (!a.equals(b) && segmentsIntersect(a, p, p.next, b) && isLocallyInside(a, b) && isLocallyInside(b, a)) {
                triangles.add(a.i);
                triangles.add(p.i);
                triangles.add(b.i);

                // remove two nodes involved
                p.remove();
                p.next.remove();

                p = start = b;
            }
            p = p.next;
        } while (p != start);

        return removeRedundantPoints(p, null);
    }

    private static void splitAndTriangulate(final Node start, final IntList triangles, final PolygonBounds bounds) {
        // look for a valid diagonal that divides the polygon into two
        Node a = start;
        do {
            Node b = a.next.next;
            while (b != a.prev) {
                if (a.i != b.i && isValidDiagonal(a, b)) {
                    // split the polygon in two by the diagonal
                    Node c = splitPolygon(a, b);

                    // filter collinear points around the cuts
                    a = removeRedundantPoints(a, a.next);
                    c = removeRedundantPoints(c, c.next);

                    // run earcut on each half
                    triangulateLinked(a, triangles, bounds, 0);
                    triangulateLinked(c, triangles, bounds, 0);
                    return;
                }
                b = b.next;
            }
            a = a.next;
        } while (a != start);
    }

    private static boolean isValidDiagonal(final Node a, final Node b) {
        return a.next.i != b.i && a.prev.i != b.i && !intersectsAnyPolygonEdge(a, b) && // doesn't intersect other edges
                (isLocallyInside(a, b) && isLocallyInside(b, a) && isMidpointInsidePolygon(a, b) && // locally visible
                        (orientedTriangleArea(a.prev, a, b.prev) != 0F || orientedTriangleArea(a, b.prev, b) != 0F) || // does not create opposite-facing sectors
                        a.equals(b) && orientedTriangleArea(a.prev, a, a.next) > 0F && orientedTriangleArea(b.prev, b, b.next) > 0F); // special zero-length case
    }

    private static boolean isLocallyInside(final Node a, final Node b) {
        return orientedTriangleArea(a.prev, a, a.next) < 0F ? orientedTriangleArea(a, b, a.next) >= 0F && orientedTriangleArea(a, a.prev, b) >= 0F : orientedTriangleArea(a, b, a.prev) < 0F || orientedTriangleArea(a, a.next, b) < 0F;
    }

    private static boolean isMidpointInsidePolygon(final Node a, final Node b) {
        Node p = a;
        boolean inside = false;
        final float px = (a.x + b.x) / 2F;
        final float py = (a.y + b.y) / 2F;
        do {
            if (((p.y > py) != (p.next.y > py)) && p.next.y != p.y && (px < (p.next.x - p.x) * (py - p.y) / (p.next.y - p.y) + p.x)) {
                inside = !inside;
            }
            p = p.next;
        } while (p != a);
        return inside;
    }

    private static boolean intersectsAnyPolygonEdge(final Node a, final Node b) {
        Node p = a;
        do {
            if (p.i != a.i && p.next.i != a.i && p.i != b.i && p.next.i != b.i && segmentsIntersect(p, p.next, a, b)) {
                return true;
            }
            p = p.next;
        } while (p != a);
        return false;
    }

    private static boolean segmentsIntersect(final Node p1, final Node q1, final Node p2, final Node q2) {
        final float o1 = Math.signum(orientedTriangleArea(p1, q1, p2));
        final float o2 = Math.signum(orientedTriangleArea(p1, q1, q2));
        final float o3 = Math.signum(orientedTriangleArea(p2, q2, p1));
        final float o4 = Math.signum(orientedTriangleArea(p2, q2, q1));
        if (o1 != o2 && o3 != o4) {
            return true; // general case
        }
        if (o1 == 0F && isOnSegment(p1, p2, q1)) {
            return true; // p1, q1 and p2 are collinear and p2 lies on p1q1
        }
        if (o2 == 0F && isOnSegment(p1, q2, q1)) {
            return true; // p1, q1 and q2 are collinear and q2 lies on p1q1
        }
        if (o3 == 0F && isOnSegment(p2, p1, q2)) {
            return true; // p2, q2 and p1 are collinear and p1 lies on p2q2
        }
        if (o4 == 0F && isOnSegment(p2, q1, q2)) {
            return true; // p2, q2 and q1 are collinear and q1 lies on p2q2
        }
        return false;
    }

    // for collinear points p, q, r, check if point q lies on segment pr
    private static boolean isOnSegment(final Node p, final Node q, final Node r) {
        return q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x) && q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y);
    }

    private static Node removeRedundantPoints(final Node start, Node end) {
        if (end == null) {
            end = start;
        }
        Node p = start;
        boolean again;
        do {
            again = false;
            if (p.equals(p.next) || orientedTriangleArea(p.prev, p, p.next) == 0F) {
                p.remove();
                p = end = p.prev;
                if (p == p.next) {
                    break;
                }
                again = true;
            } else {
                p = p.next;
            }
        } while (again || p != end);
        return end;
    }

    private static Node splitPolygon(final Node a, final Node b) {
        final Node a2 = new Node(a, a.i);
        final Node b2 = new Node(b, b.i);
        final Node an = a.next;
        final Node bp = b.prev;

        a.next = b;
        b.prev = a;

        a2.next = an;
        an.prev = a2;

        b2.next = a2;
        a2.prev = b2;

        bp.next = b2;
        b2.prev = bp;

        return b2;
    }

    private static void indexByZOrder(final Node start, final PolygonBounds bounds) {
        Node p = start;
        do {
            if (p.z == Integer.MIN_VALUE) {
                p.z = computeZOrder(p.x, p.y, bounds);
            }
            p.prevZ = p.prev;
            p.nextZ = p.next;
            p = p.next;
        } while (p != start);
        p.prevZ.nextZ = null;
        p.prevZ = null;

        sortByZOrder(p);
    }

    // z-order of a point given coords and inverse of the longer side of data bbox
    private static int computeZOrder(final float x, final float y, final PolygonBounds bounds) {
        // coords are transformed into non-negative 15-bit integer range
        int lx = (int) ((x - bounds.minX) * bounds.invSize);
        int ly = (int) ((y - bounds.minY) * bounds.invSize);

        lx = (lx | (lx << 8)) & 0x00FF00FF;
        lx = (lx | (lx << 4)) & 0x0F0F0F0F;
        lx = (lx | (lx << 2)) & 0x33333333;
        lx = (lx | (lx << 1)) & 0x55555555;

        ly = (ly | (ly << 8)) & 0x00FF00FF;
        ly = (ly | (ly << 4)) & 0x0F0F0F0F;
        ly = (ly | (ly << 2)) & 0x33333333;
        ly = (ly | (ly << 1)) & 0x55555555;

        return lx | (ly << 1);
    }

    private static void sortByZOrder(Node list) {
        int inSize = 1;
        int numMerges;
        do {
            Node p = list;
            list = null;
            Node tail = null;
            numMerges = 0;

            while (p != null) {
                numMerges++;
                Node q = p;
                int pSize = 0;
                for (int i = 0; i < inSize; i++) {
                    pSize++;
                    q = q.nextZ;
                    if (q == null)
                        break;
                }

                int qSize = inSize;
                while (pSize > 0 || (qSize > 0 && q != null)) {
                    Node e;
                    if (pSize == 0) {
                        e = q;
                        q = q.nextZ;
                        qSize--;
                    } else if (qSize == 0 || q == null) {
                        e = p;
                        p = p.nextZ;
                        pSize--;
                    } else if (p.z <= q.z) {
                        e = p;
                        p = p.nextZ;
                        pSize--;
                    } else {
                        e = q;
                        q = q.nextZ;
                        qSize--;
                    }

                    if (tail != null) {
                        tail.nextZ = e;
                    } else {
                        list = e;
                    }

                    e.prevZ = tail;
                    tail = e;
                }

                p = q;
            }

            tail.nextZ = null;
            inSize *= 2;

        } while (numMerges > 1);
    }

    private static float orientedTriangleArea(final Node p, final Node q, final Node r) {
        return (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
    }

    private static boolean isPointInTriangle(final float ax, final float ay, final float bx, final float by, final float cx, final float cy, final float px, final float py) {
        return (cx - px) * (ay - py) >= (ax - px) * (cy - py) && (ax - px) * (by - py) >= (bx - px) * (ay - py) && (bx - px) * (cy - py) >= (cx - px) * (by - py);
    }

    private static boolean isPointInTriangleExcludingFirstVertex(final float ax, final float ay, final float bx, final float by, final float cx, final float cy, final float px, final float py) {
        return !(ax == px && ay == py) && isPointInTriangle(ax, ay, bx, by, cx, cy, px, py);
    }

    private static class Node extends Vector2f {

        private final int i; // index in the input point list
        private Node prev = this; // previous node in a polygon ring
        private Node next = this; // next node in a polygon ring
        private int z = Integer.MIN_VALUE; // z-order curve value
        private Node prevZ; // previous node in z-order
        private Node nextZ; // next node in z-order

        private Node(final Vector2f point, final int index) {
            this(point, index, null);
        }

        private Node(final Vector2f point, final int index, final Node last) {
            super(point);
            this.i = index;
            if (last != null) {
                this.next = last.next;
                this.prev = last;
                last.next.prev = this;
                last.next = this;
            }
        }

        public void remove() {
            this.next.prev = this.prev;
            this.prev.next = this.next;
            if (this.prevZ != null) {
                this.prevZ.nextZ = this.nextZ;
            }
            if (this.nextZ != null) {
                this.nextZ.prevZ = this.prevZ;
            }
        }

    }

    private record PolygonBounds(float minX, float minY, float maxX, float maxY, float invSize) {

        private PolygonBounds(final Collection<Vector2f> points) {
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            float maxY = -Float.MAX_VALUE;
            for (Vector2f point : points) {
                if (point.x < minX) {
                    minX = point.x;
                }
                if (point.y < minY) {
                    minY = point.y;
                }
                if (point.x > maxX) {
                    maxX = point.x;
                }
                if (point.y > maxY) {
                    maxY = point.y;
                }
            }
            final float size = Math.max(maxX - minX, maxY - minY);
            this(minX, minY, maxX, maxY, size != 0F ? Short.MAX_VALUE / size : 0F);
        }

    }

}
