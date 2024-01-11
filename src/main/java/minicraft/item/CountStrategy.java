package minicraft.item;

import java.util.List;

public class CountStrategy implements Strategy{


    /** Returns the how many of an item you have in the inventory. */
    @Override
    public Object execute(Object[] params, List<Item> items) {

        Item given = (Item) params[0];

        if (given == null) {
            return 0; // null requests get no items. :)
        }

        int found = 0; // initialize counting var
        int itemStackSize = items.size();

        for (int itemIndex = 0; itemIndex < itemStackSize; itemIndex++) { // loop though items in inventory
            Item currentItem = items.get(itemIndex); // assign current item

            // if the item can be a stack...
            if (currentItem instanceof StackableItem && ((StackableItem) currentItem).stacksWith(given)) {
                found += ((StackableItem) currentItem).count; // add however many items are in the stack.
            } else if (currentItem.equals(given)) {
                found++; // otherwise, just add 1 to the found count.
            }
        }

        return found;
    }
}
