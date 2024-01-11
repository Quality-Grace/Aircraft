package minicraft.item;

import org.tinylog.Logger;

import java.util.List;

public class RemoveStrategy implements Strategy{

    private List<Item> items;
    @Override
    public Object execute(Object[] params, List<Item> items) {

        this.items = items;

        if(params[0] instanceof Integer && params[1] == null) {
            return remove((int) params[0]);
        } else if(params[0] instanceof StackableItem && params[1] instanceof Integer){
            return removeFromStack((StackableItem) params[0], (int) params[1]);
        } else if(params[0] instanceof Item && params[1] == null) {
            return removeItem((Item) params[0]);
        }else if(params[0] instanceof Item && params[1] instanceof Integer) {
            return removeItems((Item) params[0], (int) params[1]);
        }

        return null;
    }

    /**
     * Remove an item in this inventory.
     *
     * @param index The index of the item in the inventory's item array.
     * @return The removed item.
     */
    public Item remove(int index) {
        return items.remove(index);
    }

    /**
     * Removes items from your inventory; looks for stacks, and removes from each
     * until reached count. returns amount removed.
     */
    private int removeFromStack(StackableItem given, int count) {
        int removed = 0; // to keep track of amount removed.
        int itemStackSize = items.size();

        for (int item = 0; item < itemStackSize; item++) {
            if (!(items.get(item) instanceof StackableItem)) {
                continue;
            }

            StackableItem currentItem = (StackableItem) items.get(item);
            if (!currentItem.stacksWith(given)) {
                continue; // can't do equals, becuase that includes the stack size.
            }

            // equals; and current item is stackable.
            int amountRemoving = Math.min(count - removed, currentItem.count); // this is the number of items that are being removed from the stack this run-through.

            currentItem.count -= amountRemoving;
            if (currentItem.count == 0) { // remove the item from the inventory if its stack is empty.
                remove(item);
                item--;
            }

            removed += amountRemoving;
            if (removed == count) {
                break;
            }

            if (removed > count) { // just in case...
                System.out.println("SCREW UP while removing items from stack: " + (removed - count) + " too many.");
                break;
            }
            // if not all have been removed, look for another stack.
        }

        if (removed < count) {
            System.out.println("Inventory: could not remove all items; " + (count - removed) + " left.");
        }
        return removed;
    }

    /**
     * Removes the item from the inventory entirely, whether it's a stack, or a lone
     * item.
     */
    public Object removeItem(Item item) {
        // if (Game.debug) System.out.println("original item: " + i);
        if (item instanceof StackableItem) {
            removeItems(item.clone(), ((StackableItem) item).count);
        } else {
            removeItems(item.clone(), 1);
        }

        return null;
    }

    /**
     * Removes items from this inventory. Note, if passed a stackable item, this
     * will only remove a max of count from the stack.
     *
     * @param given Item to remove.
     * @param count Max amount of the item to remove.
     */
    public Object removeItems(Item given, int count) {
        if (given instanceof StackableItem) {
            count -= removeFromStack((StackableItem) given, count);
        } else {
            int itemStackSize = items.size();
            for (int itemIndex = 0; itemIndex < itemStackSize; itemIndex++) {
                Item currentItem = items.get(itemIndex);
                if (currentItem.equals(given)) {
                    remove(itemIndex);
                    count--;
                    if (count == 0) {
                        break;
                    }
                }
            }
        }

        if (count > 0) Logger.warn("Could not remove " + count + " " + given + (count > 1 ? "s" : "") + " from inventory");

        return null;
    }
}
