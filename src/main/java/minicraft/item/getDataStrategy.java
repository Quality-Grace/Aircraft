package minicraft.item;

import java.util.ArrayList;
import java.util.List;

public class getDataStrategy implements Strategy {

    @Override
    public Object execute(Object[] params, List<Item> items) {

        String itemdata = "";
        for (Item item : items) {
            itemdata += item.getData() + ":";
        }

        if (itemdata.length() > 0) {
            itemdata = itemdata.substring(0, itemdata.length() - 1); // remove extra ",".
        }

        return itemdata;
    }

}




