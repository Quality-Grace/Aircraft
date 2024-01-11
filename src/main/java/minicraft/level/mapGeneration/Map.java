package minicraft.level.mapGeneration;

import java.util.Random;

public abstract class Map {
    protected short[][] map;
    protected final int w;
    protected final int h;
    protected long worldSeed;
    protected final Random random;
    public Map(int w, int h, long worldSeed){
        this.w = w;
        this.h = h;
        this.worldSeed = worldSeed;
        random = new Random(worldSeed);
    }

    // Returns an array with the amounts of every tile
    public int[] countTiles(){
        int[] count = new int[256];

        for (int i = 0; i < w * h; i++) {
            count[map[0][i] & 0xffff]++;
        }

        return count;
    }

    public short[][] getMap(){
        return this.map;
    }

    // Creates the map using createMap() and checks if everything was generated correctly
    public abstract void createAndValidateMap();

    public abstract void createMap();

    // Returns the w property divided by a value
    // If that value is 0 then it returns just w
    public int getDividedW(int numberToDivideBy) {
        if(numberToDivideBy!=0) {
            return this.w/numberToDivideBy;
        } else {
            System.out.println("Cannot divide number with 0");
            return this.w;
        }
    }
}
