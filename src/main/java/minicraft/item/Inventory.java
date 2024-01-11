
package minicraft.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import minicraft.entity.furniture.Furniture;

public class Inventory {

    Strategy strategy;
	
//	/** Random values used only for inventory **/
//    private final Random random = new Random();
    
    private final List<Item> items = new ArrayList<>(); // The list of items that is in the inventory.

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    
    public int size() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }


    /**
     * Get one item in this inventory.
     * 
     * @param index The index of the item in the inventory's item array.
     * @return The specified item.
     */
    public Item get(int index) {
        return items.get(index);
    }


    public void setStrategy(Strategy str) {
        this.strategy = str;
    }

    public Object executeStrategy(Object[] params) {
        return strategy.execute(params, items);
    }
}
