package minicraft.level.mapGeneration;

import minicraft.level.LevelGen;
import minicraft.level.mapGeneration.mapVerification.VoidMapVerifier;
import minicraft.level.tile.Tiles;
import minicraft.screen.LoadingDisplay;


public class VoidMapGeneration extends Map {
    public VoidMapGeneration(int w, int h, long worldSeed){
        super(w, h, worldSeed);
        createAndValidateMap();
    }

    @Override
    public void createAndValidateMap() {
        LoadingDisplay.setMessage("Generating the Void!");

        VoidMapVerifier voidMapVerifier = new VoidMapVerifier(this);
        voidMapVerifier.validateMap();
    }

    @Override
    public void createMap() {
        map = new short[2][w*h];
        LoadingDisplay.setMessage("Checking level");
        fillMapWithBlocks();

        plantTrees();
    }

    private void fillMapWithBlocks() {
        // creates a bunch of value maps, some with small size...
        LevelGen mnoise1 = new LevelGen(w, h, 16);
        LevelGen mnoise2 = new LevelGen(w, h, 16);
        LevelGen mnoise3 = new LevelGen(w, h, 16);

        // ...and some with larger size..
        LevelGen noise1 = new LevelGen(w, h, 32);
        LevelGen noise2 = new LevelGen(w, h, 32);

        //LevelGen jnoise1 = new LevelGen(w, h, 8);
        //LevelGen jnoise2 = new LevelGen(w, h, 4);
        // LevelGen jnoise3 = new LevelGen(w, h, 8);

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

                double dist = Math.pow(Math.max(xd, yd), 8);
                val += 1 - dist * 20;

                if (val < -0.5) {
                    Tiles.addTileIdToArray(map[0],i,"Infinite Fall");
                } else if (val > 0.5 && mval < -1.0) {
                    Tiles.addTileIdToArray(map[0],i,"Rock");
                } else {
                    Tiles.addTileIdToArray(map[0],i,"Grass");
                }
            }
        }
    }

    private void plantTrees() {
        for (int i = 0; i < w * h / 200; i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            for (int j = 0; j < 200; j++) {
                int xx = x + random.nextInt(15) - random.nextInt(14);
                int yy = y + random.nextInt(15) - random.nextInt(14);
                if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                    if (Tiles.idEqualsTile(map[0][xx + yy * w],"Grass")) {
                        map[0][xx + yy * w] = Tiles.get("Oak Tree").id;
                    }
                }
            }
        }
    }

}
