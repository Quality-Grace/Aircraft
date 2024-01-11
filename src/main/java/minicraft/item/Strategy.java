package minicraft.item;

import java.util.List;

public interface Strategy {

    Object execute(Object[] params, List<Item> items);

}
