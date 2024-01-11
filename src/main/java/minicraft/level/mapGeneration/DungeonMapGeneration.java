package minicraft.level.mapGeneration;

import minicraft.level.LevelGen;
import minicraft.level.Structure;
import minicraft.level.mapGeneration.mapVerification.DungeonMapVerifier;
import minicraft.level.tile.Tiles;
import minicraft.screen.LoadingDisplay;

public class DungeonMapGeneration extends Map{
    public DungeonMapGeneration(int w, int h, long worldSeed) {
        super(w, h, worldSeed);
        createAndValidateMap();
    }

    @Override
    public void createAndValidateMap() {
        LoadingDisplay.setMessage("Generating the Dungeon!");
        DungeonMapVerifier dungeonMapVerifier = new DungeonMapVerifier(this);
        dungeonMapVerifier.validateMap();
    }

    @Override
    public void createMap() {
        map = new short[2][w*h];
        LoadingDisplay.setMessage("Checking for noise");
        fillMapWithBlocksAndFluids();

        generateLavaPool();

        generateRawObsidian();
    }

    private void generateRawObsidian() {
        for (int i = 0; i < (w*h / 100); i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            for (int j = 0; j < 20; j++) {
                int xx = x + random.nextInt(3) - random.nextInt(3);
                int yy = y + random.nextInt(3) - random.nextInt(3);
                if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                    if (Tiles.idEqualsTile(map[0][xx+yy*w],"Obsidian")) {
                        Tiles.addTileIdToArray(map[0],xx+yy*w,"Raw Obsidian");
                    }

                }
            }
        }
    }

    private void generateLavaPool() {
        lavaLoop:
        for (int i = 0; i < (w*h / 450); i++) {
            int x = random.nextInt(w - 2) + 1;
            int y = random.nextInt(h - 2) + 1;

            for (int yy = y - 1; yy <= y + 1; yy++) {
                for (int xx = x - 1; xx <= x + 1; xx++) {
                    if (Tiles.idEqualsTile(map[0][xx+yy*w],"Obsidian Wall")) {
                        continue lavaLoop;
                    }
                }
            }

            // Generate structure (lava pool)
            Structure.drawLavaPool(map[0], x, y, w);
        }
    }

    private void fillMapWithBlocksAndFluids() {
        LevelGen noise1 = new LevelGen(w, h, 8);
        LevelGen noise2 = new LevelGen(w, h, 8);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = x + y * w;

                double val = Math.abs(noise1.getValue(i) - noise2.getValue(i)) * 3 - 2;

                double xd = x / (w - 1.1) * 2 - 1;
                double yd = y / (h - 1.1) * 2 - 1;

                if (xd < 0) xd = -xd;
                if (yd < 0) yd = -yd;

                double dist = Math.max(xd, yd);
                dist = dist * dist * dist * dist;
                dist = dist * dist * dist * dist;

                val = -val * 1 - 2.2;
                val += 1 - dist * 2;

                if (val < -0.05) {
                    Tiles.addTileIdToArray(map[0],i,"Obsidian Wall");
                } else if (val>=-0.05 && val<-0.03) {
                    Tiles.addTileIdToArray(map[0],i,"Lava");
                } else {
                    Tiles.addTileIdToArray(map[0],i,"Obsidian");
                }
            }
        }
    }
}
