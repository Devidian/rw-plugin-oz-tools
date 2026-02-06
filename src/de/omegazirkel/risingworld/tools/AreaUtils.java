package de.omegazirkel.risingworld.tools;

import java.util.List;

import net.risingworld.api.Server;
import net.risingworld.api.objects.Area;
import net.risingworld.api.utils.Vector3f;
import net.risingworld.api.utils.Vector3i;

public class AreaUtils {
    /**
     * Converts a chunk coordinate into an Area
     */
    public static Area getVirtualAreaFromChunkVector(Vector3i chunkPosition) {
        return chunksToArea(List.of(chunkPosition));
    }

    /**
     * transform list of chunks into area
     * 
     * @param chunks
     * @return
     */
    public static Area chunksToArea(List<Vector3i> chunks) {
        Vector3i start = chunks.get(0).copy();
        Vector3i end = chunks.get(0).copy();

        Boolean negX = false;
        Boolean negZ = false;
        Boolean negY = false;
        // check if any x/z is negative
        for (Vector3i chunk : chunks) {
            if (chunk.x < 0)
                negX = true;
            if (chunk.z < 0)
                negZ = true;
            if (chunk.y < 0)
                negY = true;
        }

        if (chunks.size() > 1)
            for (Vector3i chunk : chunks) {
                // X
                if (!negX) {
                    if (chunk.x < start.x)
                        start.x = chunk.x;
                    if (chunk.x > end.x)
                        end.x = chunk.x;
                } else {
                    if (chunk.x > start.x)
                        start.x = chunk.x;
                    if (chunk.x < end.x)
                        end.x = chunk.x;
                }
                // Z
                if (!negZ) {
                    if (chunk.z < start.z)
                        start.z = chunk.z;
                    if (chunk.z > end.z)
                        end.z = chunk.z;
                } else {
                    if (chunk.z > start.z)
                        start.z = chunk.z;
                    if (chunk.z < end.z)
                        end.z = chunk.z;
                }
                if (!negY) {
                    // Y
                    if (chunk.y < start.y)
                        start.y = chunk.y;
                    if (chunk.y > end.y)
                        end.y = chunk.y;
                } else {
                    if (chunk.y > start.y)
                        start.y = chunk.y;
                    if (chunk.y < end.y)
                        end.y = chunk.y;
                }
            }
        float startX = negX ? (start.x + 1) * 32 - 0.01f : (start.x * 32);
        float startZ = negZ ? (start.z + 1) * 32 - 0.01f : (start.z * 32);
        float startY = negY ? (start.y + 1) * 64 - 0.01f : (start.y * 64);
        float endX = negX ? end.x * 32 + 0.001f : (end.x + 1) * 32 - 0.001f;
        float endZ = negZ ? end.z * 32 + 0.001f : (end.z + 1) * 32 - 0.001f;
        float endY = negY ? end.y * 64 + 0.001f : (end.y + 1) * 64 - 0.001f;
        Vector3f areaStart = new Vector3f(startX, startY, startZ);
        Vector3f areaEnd = new Vector3f(endX, endY, endZ);
        Area area = new Area(areaStart, areaEnd);
        return area;
    }

    public static Area isAreaIntersecting(Area area) {
        for (Area a : Server.getAllAreas()) {
            if (a != null && a.intersects(area)) {
                return a;
            }
        }
        return null;
    }
}
