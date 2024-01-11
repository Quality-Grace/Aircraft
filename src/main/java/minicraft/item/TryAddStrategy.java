package minicraft.item;

import minicraft.entity.furniture.Furniture;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class TryAddStrategy implements Strategy{

    /** Random values used only for inventory **/
    private final Random random = new Random();
    private AdditionStrategy a;
    @Override
    public Object execute(Object[] params, List<Item> items) {

       // this.items = items;

        if(params[0] instanceof Integer && params[1] instanceof Item && params[2] instanceof Integer && params[3] instanceof Boolean) {
            return tryAdd((int) params[0], (Item) params[1], (int) params[2], (boolean) params[3]);
        } else if(params[0] instanceof Integer && params[1] instanceof Item && params[2] instanceof Integer){
            return tryAdd((int) params[0], (Item) params[1], (int) params[2]);
        } else if(params[0] instanceof Integer && params[1] instanceof Item) {
            return tryAdd((int) params[0], (Item) params[1]);
        }else if(params[0] instanceof Integer && params[1] instanceof ToolType && params[2] instanceof Integer) {
            return tryAdd((int) params[0], (ToolType) params[1], (int) params[2]);
        } else if(params[0] instanceof Integer && params[1] instanceof Furniture) {
            return tryAdd((int) params[0], (Furniture) params[1]);
        }

        return null;
    }

    /**
     * Tries to add an item to the inventory.
     *
     * @param chance       Chance for the item to be added.
     * @param item         Item to be added.
     * @param num          How many of the item.
     * @param allOrNothing if true, either all items will be added or none, if false
     *                     its possible to add between 0-num items.
     */
    //tryadd1
    public Object tryAdd(int chance, Item item, int num, boolean allOrNothing) {
        if (!allOrNothing || random.nextInt(chance) == 0) {
            for (int i = 0; i < num; i++) {
                if (allOrNothing || random.nextInt(chance) == 0) {
//                    AdditionStrategy a=null;
                    a.add(item.clone());
                }
            }
        }
        return null;
    }

    //tryadd2
    public Object tryAdd(int chance, @Nullable Item item, int num) {
        if (item == null) {
            return null;
        }

        if (item instanceof StackableItem) {
            ((StackableItem) item).count *= num;
            tryAdd(chance, item, 1, true);
        } else {
            tryAdd(chance, item, num, false);
        }
        return null;
    }

    //tryadd3
    public Object tryAdd(int chance, @Nullable Item item) {
        tryAdd(chance, item, 1);
        return null;
    }

    //tryadd4
    public Object tryAdd(int chance, ToolType type, int lvl) {
        tryAdd(chance, new ToolItem(type, lvl));
        return null;
    }

    /**
     * Tries to add an Furniture to the inventory.
     *
     * @param chance Chance for the item to be added.
     * @param type   Type of furniture to add.
     */
    //tryadd5
    public Object tryAdd(int chance, Furniture type) {
        tryAdd(chance, new FurnitureItem(type));
        return null;
    }
}
