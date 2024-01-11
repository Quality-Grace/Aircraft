package minicraft.level.mapGeneration.mapVerification;

import minicraft.level.mapGeneration.TopMapGeneration;
import minicraft.level.tile.Tiles;

public class TopMapVerifier extends MapVerifier {
    public TopMapVerifier(TopMapGeneration topMap){
        super(topMap);
    }

    @Override
    public void initializeAmountsLessThanToVerify() {
        super.initializeAmountsLessThanToVerify();
        this.amountsLessThan.put(Tiles.get("Rock").id & 0xffff, 100);
        this.amountsLessThan.put(Tiles.get("Sand").id & 0xffff, 100);
        this.amountsLessThan.put(Tiles.get("Grass").id & 0xffff, 100);
        this.amountsLessThan.put(Tiles.get("Oak Tree").id & 0xffff, 100);
        this.amountsLessThan.put(Tiles.get("Daisy").id & 0xffff, 100);
    }

    @Override
    public void initializeAmountsEqualsToVerify() {
        super.initializeAmountsEqualsToVerify();
        this.amountsEquals.put(Tiles.get("Stairs Down").id & 0xffff, 0);
    }
}
