package minicraft.item;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;

public class AdditionStrategy implements Strategy{

    private List<Item> items;

    @Override
    public Object execute(Object[] params, List<Item> items) {

        this.items = items;

        if(params[0] instanceof Inventory && params[1] == null) {
            return addAll((Inventory) params[0]);
        } else if(params[0] instanceof Item && params[1] == null){
            return add((Item) params[0]);
        } else if(params[0] instanceof Item && params[1] instanceof Integer) {
            return add((Item) params[0], (int) params[1]);
        }else if(params[0] instanceof Integer && params[1] instanceof Item) {
            return add((int) params[0], (Item) params[1]);
        }

       return null;
    }

    public Object addAll(Inventory other) {
        for (Item item : other.getItems()) {
            add(item.clone());
        }
        return null;
    }

    /** Adds an item to the inventory */
    //add1
    public Object add(@Nullable Item item) {
        if (item != null) {
            add(items.size(), item); // adds the item to the end of the inventory list
        }

        return null;
    }

    /**
     * Adds several copies of the same item to the end of the inventory.
     *
     * @param item Item to be added.
     * @param amount Amount of items to add.
     */
    //add2
    public Object add(Item item, int amount) {
        for (int items = 0; items < amount; items++) {
            add(item.clone());
        }
        return null;
    }

    /**
     * Adds an item to a specific spot in the inventory.
     *
     * @param slot Index to place item at.
     * @param item Item to be added.
     */
    //add3
    public Object add(int slot, Item item) {
        // if (Game.debug) System.out.println("adding item to an inventory: " + item);
        if (item instanceof PowerGloveItem) {
            Logger.warn("Tried to add power glove to inventory, stack trace:");
            Thread.dumpStack();
            return null; // do NOT add to inventory
        }

        if (item instanceof StackableItem) { // if the item is a item...
            StackableItem toTake = (StackableItem) item; // ...convert it into a StackableItem object.

            boolean added = false;
            int itemStackSize = items.size();

            for (int i = 0; i < itemStackSize; i++) {
                if (toTake.stacksWith(items.get(i))) {
                    // matching implies that the other item is stackable, too.
                    ((StackableItem) items.get(i)).count += toTake.count;
                    added = true;
                    break;
                }
            }

            if (!added) {
                items.add(slot, toTake);
            }
        } else {
            items.add(slot, item); // add the item to the items list
        }

        return null;
    }
}
