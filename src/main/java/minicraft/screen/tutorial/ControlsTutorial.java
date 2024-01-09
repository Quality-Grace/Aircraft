package minicraft.screen.tutorial;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.graphic.Color;
import minicraft.graphic.Font;
import minicraft.graphic.Screen;
import minicraft.graphic.StaticColorsVars;
import minicraft.screen.Display;
import minicraft.screen.Menu;
import minicraft.screen.RelPos;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.StringEntry;

public class ControlsTutorial extends Display {

    public ControlsTutorial() {
        super(true,
                new Menu.Builder(false, 6, RelPos.LEFT,
                        new StringEntry("       " + Localization.getLocalized("Moving the character."), StaticColorsVars.ORANGE),
                        new BlankEntry(),
                        new StringEntry(Localization.getLocalized("You can move your character through"), StaticColorsVars.WHITE),
                        new StringEntry(Localization.getLocalized("the keyboard, unfortunately"), StaticColorsVars.WHITE),
                        new StringEntry(Localization.getLocalized("there is no mouse :("), StaticColorsVars.WHITE),
                        new BlankEntry(), new StringEntry(Localization.getLocalized("You can move with:"), StaticColorsVars.WHITE),
                        new BlankEntry(), new BlankEntry(),
                        new StringEntry(Localization.getLocalized("You can also change the controls to"), StaticColorsVars.WHITE),
                        new StringEntry(Localization.getLocalized("your liking in:"), StaticColorsVars.WHITE), new BlankEntry(),
                        new BlankEntry()).setTitle("Tutorial", StaticColorsVars.YELLOW).createMenu());
    }

    @Override
    public void render(Screen screen) {
        super.render(screen);

        // Font.drawCentered(Settings.getEntry("mode")+"", screen, Screen.h - 190,
        // Color.GRAY);
        Font.drawCentered(
                Game.input.getMapping("MOVE-DOWN") + ", " + Game.input.getMapping("MOVE-UP") + ", "
                        + Game.input.getMapping("MOVE-LEFT") + ", " + Game.input.getMapping("MOVE-RIGHT"),
                screen, Screen.h - 128, StaticColorsVars.GRAY); // Controls
        Font.drawCentered(Localization.getLocalized("Options > Change Key bindings"), screen, Screen.h - 70,
                StaticColorsVars.GRAY); // Option location
    }
}
