package minicraft.item;

import java.util.List;

public class UpdateStrategy implements Strategy{

    /**
     * Replaces all the items in the inventory with the items in the string.
     *
     * @param items String representation of an inventory.
     */
    private AdditionStrategy a;
    @Override
    public Object execute(Object[] params, List<Item> items) {
        items.clear();
        String strItems = (String) params[0];

        if (items.isEmpty()) {
            return null; // there are no items to add.
        }

        for (String item : strItems.split(":")) { // this still generates a 1-item array when "items" is blank... [""]
            a.add(Items.get(item));
        }

        return null;
    }
}
