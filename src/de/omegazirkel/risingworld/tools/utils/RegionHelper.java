package de.omegazirkel.risingworld.tools.utils;

import java.util.HashMap;
import java.util.Map;

import net.risingworld.api.World;
import net.risingworld.api.objects.Player;
import net.risingworld.api.objects.world.Chunk;
import net.risingworld.api.utils.Vector3i;

public final class RegionHelper {

    private RegionHelper() {}

    private static final int SECTOR_SCAN_RADIUS_CHUNKS = 8;
    private static final int SAMPLE_Y = 6;

    public static String getBiomeName(Player player) {
        byte biomeCode = getStableBiomeCode(player);
        return getBiomeName(biomeCode);
    }

    // Alias, falls du bewusst "Biom" statt "Biome" verwenden willst
    public static String getBiomName(Player player) {
        return getBiomeName(player);
    }

    public static String getRegionName(Player player) {
        byte biomeCode = getStableBiomeCode(player);
        return getRegionName(biomeCode);
    }

    public static String getBiomeName(byte biomeCode) {
        String name = BIOME_NAMES.get(biomeCode);
        return name != null ? name : "Unknown";
    }

    public static String getRegionName(byte biomeCode) {
        RegionType region = BIOME_REGIONS.get(biomeCode);
        return region != null ? region.name : RegionType.UNKNOWN.name;
    }

    public static byte getStableBiomeCode(Player player) {
        if (player == null) {
            return -1;
        }

        Vector3i playerChunk = player.getChunkPosition();
        byte playerBiome = getBiomeCodeAtPlayer(player);
        if (isStableRegionBiome(playerBiome)) {
            return playerBiome;
        }

        Byte landBiome = findLandBiomeInSector(playerChunk.x, playerChunk.z);
        if (landBiome != null) {
            return landBiome;
        }

        return playerBiome;
    }

    private static byte getBiomeCodeAtPlayer(Player player) {
        Chunk chunk = World.getChunk(
                player.getChunkPosition().x,
                player.getChunkPosition().z
        );

        if (chunk == null) {
            return -1;
        }

        byte[] rawLOD = chunk.getRawLODTerrain();

        if (rawLOD == null) {
            return -1;
        }

        int localX = Math.floorMod(player.getBlockPosition().x, Chunk.SIZE_X);
        int localZ = Math.floorMod(player.getBlockPosition().z, Chunk.SIZE_Z);

        int index = Chunk.getRawTerrainIndex(SAMPLE_Y, localX, localZ);

        if (index < 0 || index >= rawLOD.length) {
            return -1;
        }

        return rawLOD[index];
    }

    private static Byte findLandBiomeInSector(int centerChunkX, int centerChunkZ) {
        Map<Byte, Integer> biomeCounter = new HashMap<>();

        for (int radius = 0; radius <= SECTOR_SCAN_RADIUS_CHUNKS; radius++) {
            scanRing(centerChunkX, centerChunkZ, radius, biomeCounter);

            if (!biomeCounter.isEmpty()) {
                return getDominantBiome(biomeCounter);
            }
        }

        return null;
    }

    private static void scanRing(
            int centerChunkX,
            int centerChunkZ,
            int radius,
            Map<Byte, Integer> biomeCounter
    ) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {

                if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                    continue;
                }

                Chunk chunk = World.getChunk(centerChunkX + dx, centerChunkZ + dz);

                if (chunk == null) {
                    continue;
                }

                sampleLandBiome(chunk, biomeCounter);
            }
        }
    }

    private static void sampleLandBiome(Chunk chunk, Map<Byte, Integer> biomeCounter) {
        byte[] rawLOD = chunk.getRawLODTerrain();

        if (rawLOD == null) {
            return;
        }

        int[][] samplePoints = {
                {8, 8},
                {4, 4},
                {4, 12},
                {12, 4},
                {12, 12}
        };

        for (int[] point : samplePoints) {
            int index = Chunk.getRawTerrainIndex(SAMPLE_Y, point[0], point[1]);

            if (index < 0 || index >= rawLOD.length) {
                continue;
            }

            byte biomeCode = rawLOD[index];

            if (!isStableRegionBiome(biomeCode)) {
                continue;
            }

            biomeCounter.merge(biomeCode, 1, Integer::sum);
        }
    }

    private static boolean isLandBiome(byte biomeCode) {
        return BIOME_REGIONS.containsKey(biomeCode);
    }

    private static boolean isStableRegionBiome(byte biomeCode) {
        return isLandBiome(biomeCode) && biomeCode != 5;
    }

    private static byte getDominantBiome(Map<Byte, Integer> biomeCounter) {
        byte result = -1;
        int bestCount = -1;

        for (Map.Entry<Byte, Integer> entry : biomeCounter.entrySet()) {
            if (entry.getValue() > bestCount) {
                result = entry.getKey();
                bestCount = entry.getValue();
            }
        }

        return result;
    }

    private static final Map<Byte, String> BIOME_NAMES = new HashMap<>();
    private static final Map<Byte, RegionType> BIOME_REGIONS = new HashMap<>();

    static {
        add((byte) 1, "Grass", RegionType.FOREST);
        add((byte) 2, "FlowerMeadow", RegionType.FOREST);
        add((byte) 3, "ForestDeciduous", RegionType.FOREST);
        add((byte) 4, "ForestConiferous", RegionType.FOREST);
        add((byte) 5, "StoneField", RegionType.FOREST);
        add((byte) 6, "ForestDead", RegionType.FOREST);
        add((byte) 7, "FernMeadow", RegionType.FOREST);
        add((byte) 8, "FlowerMeadow2", RegionType.FOREST);
        add((byte) 9, "BushMeadow", RegionType.FOREST);

        add((byte) 50, "Tundra", RegionType.ARCTIC);
        add((byte) 51, "Taiga", RegionType.ARCTIC);
        add((byte) 52, "Taiga2", RegionType.ARCTIC);
        add((byte) 53, "TaigaSnow", RegionType.ARCTIC);
        add((byte) 54, "TaigaDead", RegionType.ARCTIC);
        add((byte) 60, "FrozenField", RegionType.ARCTIC);
        add((byte) 61, "SnowField", RegionType.ARCTIC);

        add((byte) 100, "Arid", RegionType.DESERT);
        add((byte) 101, "AridForestPine", RegionType.DESERT);
        add((byte) 102, "Savannah", RegionType.DESERT);
        add((byte) 103, "Desert", RegionType.DESERT);
        add((byte) 104, "LushDesert", RegionType.DESERT);
        add((byte) 105, "DryField", RegionType.DESERT);
        add((byte) 106, "CottonField", RegionType.DESERT);
    }

    private static void add(byte code, String biomeName, RegionType region) {
        BIOME_NAMES.put(code, biomeName);
        BIOME_REGIONS.put(code, region);
    }
}
