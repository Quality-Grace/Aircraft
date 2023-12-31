package minicraft.level.mapGeneration;

import minicraft.level.LevelGen;
import minicraft.level.tile.Tiles;
import minicraft.screen.LoadingDisplay;

public class SkyMapGeneration extends Map{
    private final int heavenThreshold = w * h / 2800;
    public SkyMapGeneration(int w, int h, long worldSeed) {
        super(w, h, worldSeed);
        createAndValidateMap();
    }

    @Override
    public void createAndValidateMap() {
        LoadingDisplay.setMessage("Generating the Heaven!");

        int[] count;
        do {
            map = new short[2][w*h];
            createMap();

            count = countTiles();
        } while (count[Tiles.get("Cloud").id & 0xffff] < 2000
                || count[Tiles.get("Stairs Down").id & 0xffff] < w / 64);
    }

    // Sky dimension generation
    @Override
    public void createMap() {
        LoadingDisplay.setMessage("Checking for noise");
        generateClouds();

        // Generate skygrass in cloud tile
        LoadingDisplay.setMessage("Generating Heaven Island");
        generateTilesInRegion(22,22,90, 190, 42, 21,8,"Cloud","Sky Grass");

        // Make the central island
        // Logger.debug("Generating central island ...");
        LoadingDisplay.setMessage("Generating mountains");
        generateTilesInRegion(22,22,60,90,40,20,10,"Infinite Fall", "Holy Rock");

        // Generate the ferrosite edge for the central island
        LoadingDisplay.setMessage("Generating ferrosite");
        generateTilesInRegion(38,40,90,190,80,30,12,"Cloud","Ferrosite");

        // Generate sky lawn in Sky grass
        LoadingDisplay.setMessage("Adding some flowers");
        generateSkyLawn();

        LoadingDisplay.setMessage("Adding some trees");
        generateTrees((w * h / 400),42,"Skyroot tree");
        generateTrees((w * h / 200),36,"Bluroot tree");
        generateTrees((w*h/400),24,"Goldroot tree");

        generateCacti();

        // Avoid the connection between the Sky grass and Infinite Fall tiles
        LoadingDisplay.setMessage("Generating Heaven Edges");
        generateHeaveEdges();

        LoadingDisplay.setMessage("Generating Sky Stairs");
        generateSkyStairs();
    }

    private void generateSkyStairs() {
        int stairsCount = 0;
        int stairsRadius = 15;

        stairsLoop:
        for (int i = 0; i < w * h; i++) {
            int x = random.nextInt(w - 2) + 1;
            int y = random.nextInt(h - 2) + 1;

            for (int yy = y - 1; yy <= y + 1; yy++) {
                for (int xx = x - 1; xx <= x + 1; xx++) {
                    if (!Tiles.idEqualsTile(map[0][xx + yy * w],"Cloud")) {
                        continue stairsLoop;
                    }
                }
            }

            // This should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
            for (int yy = Math.max(0, y - stairsRadius); yy <= Math.min(h - 1, y + stairsRadius); yy++) {
                for (int xx = Math.max(0, x - stairsRadius); xx <= Math.min(w - 1, x + stairsRadius); xx++) {
                    if (Tiles.idEqualsTile(map[0][xx + yy * w],"Stairs Down")) {
                        continue stairsLoop;
                    }
                }
            }

            map[0][x + y * w] = Tiles.get("Stairs Down").id;

            stairsCount++;
            if (stairsCount >= w / 64) {
                break;
            }
        }
    }

    private void generateHeaveEdges() {
        for (int j = 0; j < h; j++) {
            for (int x = 0; x < w; x++) {
                // Check if the current tile is a "sky" tile
                if (!Tiles.idEqualsTile(map[0][x + j * w], "Infinite fall")
                        && (Tiles.idEqualsTile(map[0][x + j * w], "Holy Rock")
                        || Tiles.idEqualsTile(map[0][x + j * w], "Sky fern"))) {

                    // Check the surrounding tiles within the specified thickness to see if any of them are "Infinite fall" tiles
                    int edgesThickness = 2;
                    for (int tx = x - edgesThickness; tx <= x + edgesThickness; tx++) {
                        for (int ty = j - edgesThickness; ty <= j + edgesThickness; ty++) {
                            // If any surrounding tiles are "Infinite fall" tiles, replace the current tile with a "Sky grass" tile
                            if (tx >= 0 && ty >= 0 && tx < w && ty < h && (tx != x || ty != j) &&
                                    Tiles.idEqualsTile(map[0][tx + ty * w], "Infinite fall")) {
                                Tiles.addTileIdToArray(map[0], x+j*w, "Sky grass");
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateCacti() {
        for (int i = 0; i < (w * h / 150); i++) {
            int xx = random.nextInt(w);
            int yy = random.nextInt(h);

            if (xx < w && yy < h) {
                if (Tiles.idEqualsTile(map[0][xx + yy * w], "Ferrosite")) {
                    Tiles.addTileIdToArray(map[0], xx+yy*w, "Cloud cactus");
                }
            }
        }
    }

    private void generateTrees(int outerLoopAmount, int innerLoopAmount, String treeType) {
        for (int i = 0; i < outerLoopAmount; i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);

            for (int j = 0; j < innerLoopAmount; j++) {
                int xx = x + random.nextInt(14) - random.nextInt(12) + random.nextInt(4);
                int yy = y + random.nextInt(14) - random.nextInt(12) + random.nextInt(4);

                if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                    if (map[0][xx + yy * w] == Tiles.get("Sky grass").id) {
                        map[0][xx + yy * w] = Tiles.get(treeType).id;
                    }
                }
            }
        }
    }

    private void generateSkyLawn() {
        for (int i = 0; i < (w * h / 800); i++) {
            int x = (w / 2 - random.nextInt(32)) + random.nextInt(32);
            int y = (w / 2 - random.nextInt(32)) + random.nextInt(32);
            int pos = random.nextInt(4);

            for (int j = 0; j < 16; j++) {
                int xx = x + random.nextInt(6) - random.nextInt(6);
                int yy = y + random.nextInt(6) - random.nextInt(6);

                if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                    if (Tiles.idEqualsTile(map[0][xx+yy*w],"Sky Grass")) {
                        Tiles.addTileIdToArray(map[0], xx+yy*w, "Sky lawn");
                        map[1][xx + yy * w] = (short) (pos + random.nextInt(4) * 16);
                    }
                }
            }
        }
    }

    private void generateTilesInRegion(int xCenter, int yCeneter, int kMax, int jMax, int firstRandomRange, int secondRandomRange, int thirdRandomRange, String tileToCheck, String tileToAdd) {
        for (int i = 0; i < heavenThreshold; i++) {
            int xs = w / 2 - xCenter; // divide the 60 (down) by 2 -> 30 to center
            int ys = h / 2 - yCeneter;

            for (int k = 0; k < kMax; k++) {
                int x = xs + random.nextInt(28) - random.nextInt(10);
                int y = ys + random.nextInt(28) - random.nextInt(10);

                for (int j = 0; j < jMax; j++) {
                    int xo = x + random.nextInt(firstRandomRange) - random.nextInt(secondRandomRange) + random.nextInt(thirdRandomRange);
                    int yo = y + random.nextInt(firstRandomRange) - random.nextInt(secondRandomRange) + random.nextInt(thirdRandomRange);

                    for (int yy = yo - 1; yy <= yo + 1; yy++) {
                        for (int xx = xo - 1; xx <= xo + 1; xx++) {
                            if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                                if (Tiles.idEqualsTile(map[0][xx+yy*w],tileToCheck)) {
                                    Tiles.addTileIdToArray(map[0],xx+yy*w,tileToAdd);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private void generateClouds() {
        LevelGen noise1 = new LevelGen(w, h, 8);
        LevelGen noise2 = new LevelGen(w, h, 8);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = x + y * w;

                double val = Math.abs(noise1.getValue(i) - noise2.getValue(i)) * 3 - 2;
                double xd = x / (w - 1.0) * 2 - 1;
                double yd = y / (h - 1.0) * 2 - 1;

                if (xd < 0) xd = -xd;
                if (yd < 0) yd = -yd;

                double dist = Math.max(xd, yd);
                dist = dist * dist * dist * dist;
                dist = dist * dist * dist * dist;

                val = -val * 1 - 2.2;
                val += 1 - dist * 20;

                if (val < -0.27) {
                    Tiles.addTileIdToArray(map[0], i, "Infinite Fall");
                } else {
                    Tiles.addTileIdToArray(map[0], i, "Cloud");
                }
            }
        }
    }
}
