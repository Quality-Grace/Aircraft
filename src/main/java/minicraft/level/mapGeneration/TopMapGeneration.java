package minicraft.level.mapGeneration;

import minicraft.core.io.Settings;
import minicraft.level.LevelGen;
import minicraft.level.tile.Tiles;
import minicraft.screen.LoadingDisplay;

import java.util.Objects;

public class TopMapGeneration extends Map{
    private final String terrainType;
    public TopMapGeneration(int w, int h, long worldSeed) {
        super(w, h, worldSeed);
        terrainType = (String) Settings.get("Type");
        createAndValidateMap();
    }

    @Override
    public void createAndValidateMap() {
        LoadingDisplay.setMessage("Generating the Surface!");

        int[] count;
        do {
            map = new short[2][w*h];
            createMap();

            count = countTiles();
        } while (count[Tiles.get("Rock").id & 0xffff] < 100
                || count[Tiles.get("Sand").id & 0xffff] < 100
                || count[Tiles.get("Grass").id & 0xffff] < 100
                || count[Tiles.get("Oak Tree").id & 0xffff] < 100
                || count[Tiles.get("Daisy").id & 0xffff] < 100
                || count[Tiles.get("Stairs Down").id & 0xffff] == 0);
    }

    @Override
    public void createMap() {
        fillMapWithBlocksAndFluids();

        // DESERT GENERATION STEP
        LoadingDisplay.setMessage("Generating desert");
        desertGeneration();

        // TUNDRA GENERATION STEP
        LoadingDisplay.setMessage("Generating tundra");
        tundraGeneration();

        /// FOREST GENERATION STEP
        LoadingDisplay.setMessage("Adding some trees");
        forestGeneration();

        // VEGETATION GENERATION STEP
        LoadingDisplay.setMessage("Adding some flowers");
        flowerGeneration();

        LoadingDisplay.setMessage("Adding some plants");
        lawnGeneration();

        // add cactus to sand
        cactiGeneration();

        // add ice spikes to snow
        iceSpikeGeneration();

        // Decoration
        int beachThickness = 1;

        replaceTilesInAreaWithCondition(beachThickness,beachThickness,"Water", new String[]{"Grass", "Oak Tree", "Poppy", "Rose", "Daisy", "Birch Tree", "Lawn", "Dandelion"},"Sand");

        LoadingDisplay.setMessage("Generating mountains");
        mountainGeneration();

        // Generate the beaches (if the ice is generated in the sides)
        replaceTilesInAreaWithCondition(beachThickness,beachThickness,"Ice", new String[]{"Grass", "Oak Tree", "Poppy", "Rose", "Daisy", "Birch Tree", "Lawn", "Dandelion"},"Sand");

        LoadingDisplay.setMessage("Generating glaciers");
        generateGlaciers(beachThickness);

        // Generate the stairs inside the rock
        generateStairs();
    }

    private void fillMapWithBlocksAndFluids() {
        // creates a bunch of value maps, some with small size...
        LevelGen mnoise1 = new LevelGen(w, h, 16);
        LevelGen mnoise2 = new LevelGen(w, h, 16);
        LevelGen mnoise3 = new LevelGen(w, h, 16);

        // ...and some with larger size..
        LevelGen noise1 = new LevelGen(w, h, 32);
        LevelGen noise2 = new LevelGen(w, h, 32);

        LoadingDisplay.setMessage("Checking for theme");
        String terrainTheme = (String) Settings.get("Theme");

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = x + y * w;

                double val = Math.abs(noise1.getValue(i) - noise2.getValue(i)) * 3 - 2;
                double mval = Math.abs(mnoise1.getValue(i) - mnoise2.getValue(i));
                mval = Math.abs(mval - mnoise3.getValue(i)) * 3 - 2;

                // this calculates a sort of distance based on the current coordinate.
                double xd = x / (w - 1.0) * 2 - 1;
                double yd = y / (h - 1.0) * 2 - 1;

                if (xd < 0) xd = -xd;
                if (yd < 0) yd = -yd;

                double dist = Math.max(xd, yd);
                dist = dist * dist * dist * dist;
                dist = dist * dist * dist * dist;
                val += 1 - dist * 16;

                // World themes logic
                switch (terrainType) {
                    case "Island":
                        if (val < -0.7) {
                            String fluid = (Objects.equals(terrainTheme, "Hell") ? "Lava" : "Water");
                            Tiles.addTileIdToArray(map[0], i,fluid);
                        } else if (val > 0.9 && mval > -1.9) {
                            // Mountains
                            Tiles.addTileIdToArray(map[0], i,"Up Rock");
                        } else {
                            Tiles.addTileIdToArray(map[0], i,"Grass");
                        }
                        break;
                    case "Box":
                        if (val < -1.5) {
                            String fluid = (Objects.equals(terrainTheme, "Hell") ? "Lava" : "Water");
                            Tiles.addTileIdToArray(map[0], i,fluid);
                        } else if (val > 0.5 && mval < -1.5) {
                            Tiles.addTileIdToArray(map[0], i,"Up Rock");
                        } else {
                            Tiles.addTileIdToArray(map[0], i,"Grass");
                        }
                        break;
                    case "Mountain":
                        if (val < -0.4) {
                            Tiles.addTileIdToArray(map[0], i,"Grass");
                        } else if (val > 0.5 && mval < -1.5) {
                            String fluid = (Objects.equals(terrainTheme, "Hell") ? "Lava" : "Water");
                            Tiles.addTileIdToArray(map[0], i,fluid);
                        } else {
                            Tiles.addTileIdToArray(map[0], i,"Up Rock");
                        }
                        break;
                    case "Irregular":
                        if (val < -0.5 && mval < -0.5) {
                            String fluid = (Objects.equals(terrainTheme, "Hell") ? "Lava" : "Water");
                            Tiles.addTileIdToArray(map[0], i,fluid);
                        } else if (val > 0.5 && mval < -1.5) {
                            Tiles.addTileIdToArray(map[0], i,"Rock");
                        } else if (val < -0.5 && mval > -1.5) {
                            // Irregular beaches beaches
                            Tiles.addTileIdToArray(map[0], i,"sand");
                        } else {
                            Tiles.addTileIdToArray(map[0], i,"Grass");
                        }
                        break;
                }
            }
        }
    }

    private void generateStairs() {
        int stairsCount = 0;
        int stairsRadius = 15;

        // Logger.debug("Generating stairs for surface level...");

        stairsLoop:
        for (int i = 0; i < (w*h / 100); i++) { // loops a certain number of times, more for bigger world

            // Sizes
            int x = random.nextInt(w - 2) + 1;
            int y = random.nextInt(h - 2) + 1;

            // The first loop, which checks to make sure that a new stairs tile will be completely surrounded by rock.
            for (int yy = y - 1; yy <= y + 1; yy++) {
                for (int xx = x - 1; xx <= x + 1; xx++) {
                    if (Tiles.idEqualsTile(map[0][xx+yy*w], "Up Rock")) {
                        continue stairsLoop;
                    }
                }
            }

            // This should prevent any stairsDown tile from being within 30 tiles of any other stairsDown tile.
            for (int yy = Math.max(0, y - stairsRadius); yy <= Math.min(h - 1, y + stairsRadius); yy++) {
                for (int xx = Math.max(0, x - stairsRadius); xx <= Math.min(w - 1, x + stairsRadius); xx++) {
                    if (Tiles.idEqualsTile(map[0][xx+yy*w], "Stairs Down")) {
                        continue stairsLoop;
                    }
                }
            }

            Tiles.addTileIdToArray(map[0], x+y*w,"Stairs Down");

            stairsCount++;
            if (stairsCount >= w / 21) {
                break;
            }
        }
    }

    private void generateGlaciers(int beachThickness) {
        replaceTilesInAreaWithCondition(5 + random.nextInt(6),5 + random.nextInt(8),"Water",new String[]{"Ice"}, "Hole");
        replaceTilesInAreaWithCondition(16,16,"Water",new String[]{"Hole"}, "Water");
        replaceTilesInAreaWithCondition(beachThickness, beachThickness,"Ice", new String[]{"Snow", "Fir Tree", "Pine Tree"}, "Snow");
        replaceTilesInAreaWithCondition(beachThickness - random.nextInt(2), beachThickness - random.nextInt(1), "Grass", new String[]{"Snow", "Fir Tree", "Pine Tree"}, "Snow");
    }

    private void replaceTilesInAreaWithCondition(int randomX, int randomY, String tileToReplace, String[] validReplacementTiles, String replacementTile) {
        for (int j = 0; j < h; j++) {
            for (int x = 0; x < w; x++) {
                if (!Tiles.idEqualsTile(map[0][x + j * w], replacementTile) && Tiles.arrayContainsTileWithId(map[0][x+j*w], validReplacementTiles)) {
                    check_ocean:
                    for (int tx = x - randomX; tx <= x + randomX; tx++) {
                        for (int ty = j - randomY; ty <= j + 5 + randomY; ty++) {
                            if (tx >= 0 && ty >= 0 && tx <= w && ty <= h && (tx != x || ty != j)) {
                                if (Tiles.idEqualsTile(map[0][tx + ty * w], replacementTile)) {
                                    Tiles.addTileIdToArray(map[0], x+j*w, tileToReplace);
                                    break check_ocean;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void mountainGeneration() {
        for (int j = 0; j < h; j++) {
            for (int x = 0; x < w; x++) {
                if ( Tiles.idEqualsTile(map[0][x + j * w],"Up Rock")) {
                    check_mountains:
                    for (int tx = x - (1 + random.nextInt(2)); tx <= x + (1 + random.nextInt(2)); tx++) {
                        for (int ty = j - (1 + random.nextInt(2)); ty <= j + (1 + random.nextInt(2)); ty++) {
                            if ((tx >= 0 && ty >= 0 && tx <= w && ty <= h) && (tx != x || ty != j)) {
                                if (Tiles.arrayContainsTileWithId(map[0][tx+ty*w], new String[]{"Grass","Snow","Sand","Fir Tree","Oak Tree","Birch Tree","Pine Tree","Ice Spike", "Rose", "Lawn", "Dandelion","Poppy","Daisy"})) {
                                    Tiles.addTileIdToArray(map[0], x+j*w,"Rock");
                                    break check_mountains;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void iceSpikeGeneration() {
        for (int i = 0; i < (w*h / 100); i++) {
            int xx = random.nextInt(w);
            int yy = random.nextInt(h);
            if (xx < w && yy < h) {
                if (Tiles.idEqualsTile(map[0][xx + yy * w], "Snow")) {
                    Tiles.addTileIdToArray(map[0], xx+yy*w, "Ice Spike");
                }
            }
        }

        // same...
        for (int i = 0; i < (w*h / 100); i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            for (int j = 0; j < 20; j++) {
                int xx = x + random.nextInt(2) - random.nextInt(2);
                int yy = y + random.nextInt(2) - random.nextInt(2);
                if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                    if (Tiles.idEqualsTile(map[0][xx + yy * w], "Snow")) {
                        Tiles.addTileIdToArray(map[0], xx+yy*w, "Ice Spike");
                    }
                }
            }
        }
    }

    private void cactiGeneration() {
        for (int i = 0; i < (w*h / 100); i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);

            for (int j = 0; j < 5; j++) {
                int xx = x + random.nextInt(15) + random.nextInt(10) + random.nextInt(5);
                int yy = y + random.nextInt(15) + random.nextInt(10) + random.nextInt(5);

                if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                    if (Tiles.idEqualsTile(map[0][xx + yy * w], "Sand") && random.nextInt(6) == 3) {
                        Tiles.addTileIdToArray(map[0], xx+yy*w, "Cactus");
                    }
                }
            }
        }
    }

    private void lawnGeneration() {
        for (int i = 0; i < w*h / 400; i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            int pos = random.nextInt(4);

            for (int j = 0; j < 100; j++) {
                int xx = x + random.nextInt(5) - random.nextInt(5);
                int yy = y + random.nextInt(5) - random.nextInt(5);

                if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                    if (Tiles.idEqualsTile(map[0][xx + yy * w], "Grass")) {
                        Tiles.addTileIdToArray(map[0], xx+yy*w, "Lawn");
                        map[1][xx + yy * w] = (short) (pos + random.nextInt(4) * 16);
                    }
                }
            }
        }
    }

    private void flowerGeneration() {
        for (int i = 0; i < w*h / 400; i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            int pos = random.nextInt(4);

            for (int j = 0; j < 30; j++) {
                int xx = x + random.nextInt(5) - random.nextInt(5);
                int yy = y + random.nextInt(5) - random.nextInt(5);

                if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                    if (Tiles.idEqualsTile(map[0][xx + yy * w], "Grass")) {
                        if (random.nextBoolean()) {
                            if (random.nextBoolean()) {
                                Tiles.addTileIdToArray(map[0],xx+yy*w,"Rose");
                            } else {
                                Tiles.addTileIdToArray(map[0],xx+yy*w,"Poppy");
                            }
                        } else {
                            if (random.nextBoolean()) {
                                Tiles.addTileIdToArray(map[0],xx+yy*w,"Dandelion");
                            } else {
                                Tiles.addTileIdToArray(map[0],xx+yy*w,"Daisy");
                            }
                        }

                        map[1][xx + yy * w] = (short) (pos + random.nextInt(4) * 16);  // data determines which way the flower faces
                    }
                }
            }
        }
    }

    private void forestGeneration() {
        for (int i = 0; i < w*h / 200; i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);

            for (int j = 0; j < 100; j++) {
                int xx = (x + random.nextInt(15)) - random.nextInt(10);
                int yy = (y + random.nextInt(15)) - random.nextInt(10);

                if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                    if (Tiles.idEqualsTile(map[0][xx + yy * w], "Snow")) {
                        if (random.nextBoolean()) {
                            Tiles.addTileIdToArray(map[0],xx+yy*w,"Fir Tree");
                        } else {
                            Tiles.addTileIdToArray(map[0],xx+yy*w,"Pine Tree");
                        }
                    } else if (Tiles.idEqualsTile(map[0][xx + yy * w], "Grass")) {
                        if (random.nextBoolean()) {
                            Tiles.addTileIdToArray(map[0],xx+yy*w,"Oak Tree");
                        } else {
                            Tiles.addTileIdToArray(map[0],xx+yy*w,"Birch Tree");
                        }
                    }
                }
            }
        }
    }

    private void tundraGeneration() {
        int tundraThreshold = w*h / (terrainType.equals("Tundra") ? 600 : 2840);

        for (int i = 0; i < tundraThreshold; i++) {
            int xs = (w/2 - random.nextInt(w/2)) - 32;  // [0 0]
            int ys = (w/2 - random.nextInt(h/2)) - 32; // [0 1]

            for (int size = 0; size < (128 + random.nextInt(8)); size++) {
                int x = ((xs + random.nextInt(32)) - (16 + random.nextInt(8))) - random.nextInt(4);
                int y = ((ys + random.nextInt(32)) - (16 + random.nextInt(8))) - random.nextInt(4);

                for (int j = 0; j < (168 + random.nextInt(8)); j++) {
                    int xo = x + random.nextInt(10) - 5 + random.nextInt(4) + random.nextInt(3);
                    int yo = y + random.nextInt(10) - 5 + random.nextInt(4) + random.nextInt(3);

                    int waterThreshold = 10 * w / 128;

                    for (int yy = yo - (1 + random.nextInt(8)); yy <= yo + (1 + random.nextInt(8)); yy++) { // Height modifier
                        for (int xx = xo - (1 + random.nextInt(8)); xx <= xo + (1 + random.nextInt(4)); xx++) { // Width modifier
                            if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                                if (Tiles.arrayContainsTileWithId(map[0][xx+yy*w], new String[]{"Grass", "Sand", "Lawn"})) {
                                    Tiles.addTileIdToArray(map[0],xx+yy*w,"Snow");
                                } else if (xx > waterThreshold && xx < w - waterThreshold && yy > waterThreshold && yy < h - waterThreshold && Tiles.idEqualsTile(map[0][xx+yy*w], "Water")) {
                                    Tiles.addTileIdToArray(map[0],xx+yy*w,"Ice");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void desertGeneration() {
        int desertThreshold = w*h / (terrainType.equals("Desert") ? 600 : 2840);

        for (int i = 0; i < desertThreshold; i++) {
            // Position
            int xs = (w/2 + random.nextInt(w/2)) + 32;   // [0 0]
            int ys = (h/2 + random.nextInt(h/2)) + 32; // [0 1]

            for (int size = 0; size < 140; size++) { // Size
                int x = xs + random.nextInt(32) - 16 + random.nextInt(8) - random.nextInt(4);
                int y = ys + random.nextInt(30) - 16 + random.nextInt(8) - random.nextInt(4);

                for (int m = 0; m < 180; m++) { // Amount
                    int xo = x + random.nextInt(10) - 5 + random.nextInt(4) + random.nextInt(3);
                    int yo = y + random.nextInt(10) - 5 + random.nextInt(4) + random.nextInt(3);

                    for (int yy = yo - (1 + random.nextInt(8)); yy <= yo + (1 + random.nextInt(8)); yy++) { // Height modifier
                        for (int xx = xo - (1 + random.nextInt(4)); xx <= xo + (1 + random.nextInt(4)); xx++) { // Width modifier
                            if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                                if (Tiles.idEqualsTile(map[0][xx+yy*w],"Grass")) {
                                    Tiles.addTileIdToArray(map[0],xx+yy*w,"Sand");
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
