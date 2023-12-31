package minicraft.level.mapGeneration;

import minicraft.level.LevelGen;
import minicraft.level.Structure;
import minicraft.level.tile.Tiles;
import minicraft.screen.LoadingDisplay;

public class UndergroundMapGeneration extends Map {
    private final int depth;
    public UndergroundMapGeneration(int w, int h, int depth, long worldSeed){
        super(w,h, worldSeed);
        this.depth = depth;
        createAndValidateMap();
    }

    @Override
    public void createAndValidateMap() {
        LoadingDisplay.setMessage("Generating the caves!");

        int[] count;
        do {
            map = new short[2][w*h];
            createMap();

            count = countTiles();
        } while (count[Tiles.get("Rock").id & 0xffff] < 100
                || count[Tiles.get("Dirt").id & 0xffff] < 100
                || count[(Tiles.get("Iron Ore").id & 0xffff) + depth - 1] < 20
                || depth < 3 && count[Tiles.get("Stairs Down").id & 0xffff] < w / 32);
    }

    @Override
    public void createMap() {
        /*
         * This generates the 3 levels of cave, iron, gold and gem
         */
        LoadingDisplay.setMessage("Checking for noise");
        fillMapWithBlocksAndFluids();

        if (depth == 1) {
            LoadingDisplay.setMessage("Generating mushrooms");
            generateMycelium();
            plantMushrooms("Brown Mushroom");
            plantMushrooms("Red Mushroom");
        }

        /// Generate ores
        LoadingDisplay.setMessage("Generating Ores");
        generateOres();

        if (depth > 2) {
            generateStairs();
        } else {
            generateStairsUnderground();
        }
    }

    private void generateOres() {
        for (int i = 0; i < (w*h / 400); i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);

            // Iron ore
            generateOre(x, y,25,5,3, "Iron Ore");

            // Lapizlazuli ore
            generateOre(x, y, 10, 3, 2, "Lapis");
        }
    }

    private void fillMapWithBlocksAndFluids() {
        LevelGen mnoise1 = new LevelGen(w, h, 16);
        LevelGen mnoise2 = new LevelGen(w, h, 16);
        LevelGen mnoise3 = new LevelGen(w, h, 16);

        LevelGen nnoise1 = new LevelGen(w, h, 16);
        LevelGen nnoise2 = new LevelGen(w, h, 16);
        LevelGen nnoise3 = new LevelGen(w, h, 16);

        LevelGen wnoise1 = new LevelGen(w, h, 16);
        LevelGen wnoise2 = new LevelGen(w, h, 16);
        LevelGen wnoise3 = new LevelGen(w, h, 16);

        LevelGen noise1 = new LevelGen(w, h, 32);
        LevelGen noise2 = new LevelGen(w, h, 32);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = x + y * w;
                /// for the x=0 or y=0 i's, values[i] is always between -1 and 1.
                /// so, val is between -2 and 4.
                /// the rest are between -2 and 7.
                double val = Math.abs(noise1.getValue(i) - noise2.getValue(i)) * 3 - 2;

                double mval = Math.abs(mnoise1.getValue(i) - mnoise2.getValue(i));
                mval = Math.abs(mval - mnoise3.getValue(i)) * 3 - 2;

                double nval = Math.abs(nnoise1.getValue(i) - nnoise2.getValue(i));
                nval = Math.abs(nval - nnoise3.getValue(i)) * 3 - 2;

                double wval = Math.abs(wnoise1.getValue(i) - wnoise2.getValue(i));
                wval = Math.abs(wval - wnoise3.getValue(i)) * 3 - 2;

                double xd = x / (w - 1.0) * 2 - 1;
                double yd = y / (h - 1.0) * 2 - 1;

                if (xd < 0) xd = -xd;
                if (yd < 0) yd = -yd;

                double dist = Math.max(xd, yd);
                dist = Math.pow(dist, 8);
                val += 1 - dist * 20;

                if (val > -1 && wval < -1.4 + (double) (depth) / 2 * 3 && depth == 1) {
                    Tiles.addTileIdToArray(map[0], i, "Dirt");

                    // Make level 2 and 3 caves
                } else if (val > -1 && wval < -4 + (depth) / 2.0 * 3) {
                    if (depth == 3) {
                        Tiles.addTileIdToArray(map[0], i, "Lava");
                    } else {
                        Tiles.addTileIdToArray(map[0], i, "Water");
                    }
                } else if (val > -1.5 && (mval < -1.7 || nval < -1.4)) {
                    Tiles.addTileIdToArray(map[0], i, "Dirt");
                } else {
                    Tiles.addTileIdToArray(map[0], i, "Rock");
                }
            }
        }
    }

    private void generateStairsUnderground() {
        int stairsCount = 0;
        int stairsRadius = 15;

        stairsLoop:
        for (int i = 0; i < w * h / 100; i++) {
            int x = random.nextInt(w - 20) + 10;
            int y = random.nextInt(h - 20) + 10;

            for (int yy = y - 1; yy <= y + 1; yy++) {
                for (int xx = x - 1; xx <= x + 1; xx++) {
                    if (!Tiles.idEqualsTile(map[0][xx + yy * w], "Rock")) {
                        continue stairsLoop;
                    }
                }
            }

            // This should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
            for (int yy = Math.max(0, y - stairsRadius); yy <= Math.min(h - 1, y + stairsRadius); yy++) {
                for (int xx = Math.max(0, x - stairsRadius); xx <= Math.min(w - 1, x + stairsRadius); xx++) {
                    if (Tiles.idEqualsTile(map[0][xx + yy * w], "Stairs Down")) {
                        continue stairsLoop;
                    }
                }
            }

            Tiles.addTileIdToArray(map[0], x + y * w, "Stairs Down");

            stairsCount++;
            if (stairsCount >= w / 32) {
                break;
            }
        }
    }

    private void generateStairs() {
        int stairsRadius = 1;
        int xx = w / 2;
        int yy = w / 2;
        for (int i = 0; i < w * h / 380; i++) {
            for (int j = 0; j < 10; j++) {
                if (xx < w - stairsRadius && yy < h - stairsRadius) {
                    Structure.drawDungeonLock(map[0], xx, yy, w);

                    /// The "& 0xffff" is a common way to convert a short to an unsigned int, which basically prevents negative values... except... this doesn't do anything if you flip it back to a short again...
                    map[0][xx + yy * w] = (short) (Tiles.get("Stairs Down").id & 0xffff);
                }
            }
        }
    }

    private void generateOre(int x, int y, int amount, int rangeMax, int rangeMin, String ore) {
        for (int j = 0; j < amount; j++) {
            int xx = x + random.nextInt(rangeMax) - random.nextInt(rangeMin);
            int yy = y + random.nextInt(rangeMax) - random.nextInt(rangeMin);
            int oresThreshold = 2;
            if (xx >= oresThreshold && yy >= oresThreshold && xx < w - oresThreshold && yy < h - oresThreshold) {
                if (Tiles.idEqualsTile(map[0][xx + yy * w], "Rock")) {
                    map[0][xx + yy * w] = (short) ((Tiles.get(ore).id & 0xffff) + depth - 1);
                }
            }
        }
    }

    private void generateMycelium() {
        for (int i = 0; i < (w*h / 2800); i++) {
            int xs = random.nextInt(w);
            int ys = random.nextInt(h);

            for (int k = 0; k < 64; k++) {
                int x = xs + random.nextInt(32) - 8 + random.nextInt(4);
                int y = ys + random.nextInt(32) - 8 + random.nextInt(4);

                for (int j = 0; j < 75; j++) {
                    int xo = x + random.nextInt(5) - random.nextInt(4);

                    int yo = y + random.nextInt(5) - random.nextInt(4);
                    for (int yy = yo - 1; yy <= yo + 1; yy++) {
                        for (int xx = xo - 1; xx <= xo + 1; xx++) {
                            if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                                if (Tiles.idEqualsTile(map[0][xx + yy * w], "Dirt")) {
                                    Tiles.addTileIdToArray(map[0], xx+yy*w,"Mycelium");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void plantMushrooms(String mushroomType) {
        for (int i = 0; i < w*h/100; i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            int pos = random.nextInt(4);

            for (int j = 0; j < 20; j++) {
                int xx = x + random.nextInt(4) - random.nextInt(4);
                int yy = y + random.nextInt(4) - random.nextInt(4);

                if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                    if (Tiles.idEqualsTile(map[0][xx + yy * w], "Mycelium")) {
                        Tiles.addTileIdToArray(map[0], xx+yy*w,mushroomType);
                        map[1][xx + yy * w] = (short) (pos + random.nextInt(4) * 16);
                    }
                }
            }
        }
    }
}
