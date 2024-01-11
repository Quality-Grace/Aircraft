package minicraft.level.mapGeneration.mapVerification;

import minicraft.level.mapGeneration.DungeonMapGeneration;
import minicraft.level.tile.Tiles;

public class DungeonMapVerifier extends MapVerifier{
    public DungeonMapVerifier(DungeonMapGeneration dungeonMap){
        super(dungeonMap);
    }

    @Override
    public void initializeAmountsLessThanToVerify(){
        super.initializeAmountsLessThanToVerify();
        this.amountsLessThan.put(Tiles.get("Obsidian").id & 0xffff, 100);
        this.amountsLessThan.put(Tiles.get("Obsidian Wall").id & 0xffff, 100);
        this.amountsLessThan.put(Tiles.get("Raw Obsidian").id & 0xffff, 100);
    }
}
