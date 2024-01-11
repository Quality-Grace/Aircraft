package minicraft.level.mapGeneration.mapVerification;

import minicraft.level.mapGeneration.VoidMapGeneration;
import minicraft.level.tile.Tiles;

public class VoidMapVerifier extends MapVerifier{
    public VoidMapVerifier(VoidMapGeneration voidMap){
        super(voidMap);
    }

    @Override
    public void initializeAmountsLessThanToVerify() {
        super.initializeAmountsLessThanToVerify();
        amountsLessThan.put(Tiles.get("Rock").id & 0xffff, 100);
        amountsLessThan.put(Tiles.get("Grass").id & 0xffff, 100);
        amountsLessThan.put(Tiles.get("Oak Tree").id & 0xffff, 100);
    }
}
