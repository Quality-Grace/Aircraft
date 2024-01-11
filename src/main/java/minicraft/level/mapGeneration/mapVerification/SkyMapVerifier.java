package minicraft.level.mapGeneration.mapVerification;

import minicraft.level.mapGeneration.SkyMapGeneration;
import minicraft.level.tile.Tiles;

public class SkyMapVerifier extends MapVerifier{
    public SkyMapVerifier(SkyMapGeneration map){
        super(map);
    }

    @Override
    public void initializeAmountsLessThanToVerify() {
        super.initializeAmountsLessThanToVerify();
        amountsLessThan.put(Tiles.get("Cloud").id & 0xffff, 2000);
        amountsLessThan.put(Tiles.get("Stairs Down").id & 0xffff, map.getDividedW(64));
    }
}
