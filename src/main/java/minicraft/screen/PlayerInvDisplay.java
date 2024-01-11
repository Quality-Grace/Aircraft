package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.entity.mob.Player;
import minicraft.graphic.Color;
import minicraft.graphic.Font;
import minicraft.graphic.FontStyle;
import minicraft.graphic.Screen;
import minicraft.item.Item;
import minicraft.item.RemoveStrategy;

public class PlayerInvDisplay extends Display {

	private final Player player;

	private Object[] params;

	public PlayerInvDisplay(Player player) {
		super(new InventoryMenu(player, player.getInventory(), "Inventory"));
		this.player = player;
	}

	@Override
	public void tick(InputHandler input) {
		super.tick(input);

		if (!menus[0].searcherBarActive && input.getKey("menu").clicked) {
			Game.exitDisplay();
			return;
		}

		if (input.getKey("attack").clicked && menus[0].getNumOptions() > 0) {
			player.getInventory().setStrategy(new RemoveStrategy());
			params[0] = menus[0].getSelection();

			player.activeItem = (Item)player.getInventory().executeStrategy(params);
			Game.exitDisplay();
		}
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);
		
		if (!menus[0].searcherBarActive) {
			String text = "(" + Game.input.getMapping("SEARCHER-BAR") + ") " + Localization.getLocalized("to search.");
			FontStyle style = new FontStyle(Color.WHITE).setShadowType(Color.BLACK, true).setXPos(Screen.w / 4 - 75 - text.length()).setYPos(Screen.h / 2 - 46);
			Font.drawParagraph(text, screen, style, 2);
		}
	}
}
