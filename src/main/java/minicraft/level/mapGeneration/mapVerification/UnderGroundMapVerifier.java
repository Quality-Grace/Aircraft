package minicraft.level.mapGeneration.mapVerification;

import minicraft.level.mapGeneration.UndergroundMapGeneration;
import minicraft.level.tile.Tiles;

public class UnderGroundMapVerifier extends MapVerifier {
    public UnderGroundMapVerifier(UndergroundMapGeneration undergroundMap) {
        super(undergroundMap);
    }

    public boolean compareLessThan(int[] count){
        if(count[Tiles.get("Stairs Down").id & 0xffff] < map.getDividedW(32) && ((UndergroundMapGeneration)map).getDepth() < 3) return false;
        return super.compareLessThan(count);
    }

    @Override
    public void initializeAmountsLessThanToVerify() {
        super.initializeAmountsLessThanToVerify();
        amountsLessThan.put(Tiles.get("Rock").id & 0xffff, 100);
        amountsLessThan.put(Tiles.get("Dirt").id & 0xffff, 100);
        amountsLessThan.put((Tiles.get("Iron Ore").id & 0xffff) + ((UndergroundMapGeneration)map).getDepth() - 1, 20);
    }
}