package minicraft.level.mapGeneration.mapVerification;

import minicraft.level.mapGeneration.Map;

import java.util.HashMap;

public abstract class MapVerifier {
    protected Map map;
    protected HashMap<Integer, Integer> amountsLessThan;
    protected HashMap<Integer, Integer> amountsEquals;


    public MapVerifier(Map map){
        this.map = map;
        initializeAmountsLessThanToVerify();
        initializeAmountsEqualsToVerify();
    }

    public void validateMap() {
        do {
            map.createMap();
        } while(checkAmounts());
    }

    // Checks the amounts of certain tiles
    // The tiles and their amounts are stored in hash maps
    // Returns true if all the tiles have correct amounts
    // Returns false if at least one has a wrong amount
    public boolean checkAmounts(){
        int[] count = map.countTiles();

        return !compareLessThan(count) || !compareEquals(count);
    }

    // Checks if the amount of a tile is less than a limit set in the amountsLessThan hash map
    public boolean compareLessThan(int[] count){
        if(!amountsLessThan.isEmpty()) {
            for (Integer tile : amountsLessThan.keySet()) {
                if (count[tile] < amountsLessThan.get(tile)) return false;
            }
        }
        return true;
    }

    // Checks if the amount of a tile is equal to a limit set in the amountsEquals hash map
    public boolean compareEquals(int[] count){
        if(!amountsEquals.isEmpty()){
            for(Integer tile : amountsEquals.keySet()){
                if(count[tile] == amountsEquals.get(tile)) return false;
            }
        }
        return true;
    }

    public void initializeAmountsLessThanToVerify(){
        amountsLessThan = new HashMap<>();
    }
    public void initializeAmountsEqualsToVerify(){
        amountsEquals = new HashMap<>();
    }
}
