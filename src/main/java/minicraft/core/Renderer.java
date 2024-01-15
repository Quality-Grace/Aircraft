package minicraft.core;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.tinylog.Logger;

import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Bed;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.EyeQueen;
import minicraft.entity.mob.Player;
import minicraft.graphic.Color;
import minicraft.graphic.Ellipsis;
import minicraft.graphic.Ellipsis.DotUpdater.TickUpdater;
import minicraft.graphic.Ellipsis.SmoothEllipsis;
import minicraft.graphic.Font;
import minicraft.graphic.FontStyle;
import minicraft.graphic.Rectangle;
import minicraft.graphic.Screen;
import minicraft.graphic.Sprite;
import minicraft.graphic.SpriteSheet;
import minicraft.item.Items;
import minicraft.item.PotionType;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.screen.InfoDisplay;
import minicraft.screen.LoadingDisplay;
import minicraft.screen.RelPos;
import minicraft.util.TimeData;
import minicraft.util.Utils;

/*
 * Make the game display logic
 */
public class Renderer extends Game {
	protected Renderer() {}

	public static final int HEIGHT = 288; // This is the height of the game * scale
	public static final int WIDTH = 432; // This is the width of the game * scale

	protected static float SCALE = 2; // Scales the window

	public static Screen screen; // Creates the main screen

	protected static final Canvas canvas = new Canvas();

	private static BufferedImage image; // Creates an image to be displayed on the screen.
	public static Screen lightScreen; // Creates a front screen to render the darkness in caves (Fog of war).
	public static boolean readyToRenderGameplay = false;
	public static boolean showDebugInfo = false;
	public static boolean renderRain = false;

	protected static final Ellipsis ellipsis = (Ellipsis) new SmoothEllipsis(new TickUpdater());

	public static SpriteSheet[] loadDefaultTextures() {
		final String[] SHEETS_PATHS = {
				"/resources/textures/items.png",
				"/resources/textures/tiles.png",
				"/resources/textures/entities.png",
				"/resources/textures/gui.png",
				"/resources/textures/font.png",
				"/resources/textures/background.png"
		};

		ArrayList<SpriteSheet> sheets = new ArrayList<>();

		for (String path : SHEETS_PATHS) {
			try {
				if (debug) Logger.debug("Loading sprite '{}', for default textures ...", path);
				sheets.add(new SpriteSheet(ImageIO.read(Objects.requireNonNull(Game.class.getResourceAsStream(path)))));
			} catch (NullPointerException | IllegalArgumentException | IOException exception) {
				Logger.error("Sprites failure, default textures are unable to load an sprite sheet!");
				exception.printStackTrace();
				System.exit(-1);
				return null;
			}
		}

		return sheets.toArray(new SpriteSheet[0]);
	}

	public static SpriteSheet[] loadLegacyTextures() {
		final String[] SHEETS_PATHS = {
				"/resources/textures/legacy/items.png",
				"/resources/textures/legacy/tiles.png",
				"/resources/textures/legacy/entities.png",
				"/resources/textures/legacy/gui.png",
				"/resources/textures/legacy/font.png",
				"/resources/textures/legacy/background.png"
		};

		ArrayList<SpriteSheet> sheets = new ArrayList<>();

		for (String path : SHEETS_PATHS) {
			try {
				if (debug) Logger.debug("Loading sprite '{}', for legacy textures ...", path);
				sheets.add(new SpriteSheet(ImageIO.read(Objects.requireNonNull(Game.class.getResourceAsStream(path)))));
			} catch (NullPointerException | IllegalArgumentException | IOException exception) {
				Logger.error("Sprites failure, legacy textures are unable to load an sprite sheet!");
				exception.printStackTrace();
				System.exit(-1);
				return null;
			}
		}

		return sheets.toArray(new SpriteSheet[0]);
	}

	static void initScreen() {
		Logger.debug("Initializing game display ...");

		SpriteSheet[] sheets = loadDefaultTextures();

		screen = new Screen(sheets[0], sheets[1], sheets[2], sheets[3], sheets[4], sheets[5]);
		lightScreen = new Screen(sheets[0], sheets[1], sheets[2], sheets[3], sheets[4], sheets[5]);

		Font.updateCharAdvances(sheets[4]);

		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		screen.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		Initializer.startCanvasRendering();
		canvas.createBufferStrategy(3);
		canvas.requestFocus();
	}

	/** Renders the current screen. Called in game loop, a bit after tick(). */
	public static void render() {
		if (screen == null) {
			return; // No point in this if there's no gui... :P
		}
		if (readyToRenderGameplay) {
			GameRenderer.renderGamePlay();
		}

		if (display != null) { // Renders menu, if present.
			display.render(screen);
		}

		if(!canvas.hasFocus()) {
			renderFocusNagger();
		}

		renderCanvas();
	}

	private static void renderCanvas(){
		BufferStrategy bufferStrategy = canvas.getBufferStrategy();// Creates a buffer strategy to determine how the graphics should be buffered.
		if(bufferStrategy == null){
			return;
		}

		Graphics graphics = bufferStrategy.getDrawGraphics(); // Gets the graphics in which java draws the picture
		graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight()); // draws a rect to fill the whole window (to cover last?)
		drawImage(graphics);
		graphics.dispose(); // Releases any system items that are using this method. (so we don't have crappy framerates)

		bufferStrategy.show(); // Makes the picture visible. (probably)
	}

	private static void drawImage(Graphics graphics) {
		// Scale the pixels.
		int windowWidth = getWindowSize().width;
		int windowHeight = getWindowSize().height;

		// Gets the image offset.
		int xo = (canvas.getWidth() - windowWidth) / 2 + canvas.getParent().getInsets().left;
		int yo = (canvas.getHeight() - windowHeight) / 2 + canvas.getParent().getInsets().top;
		graphics.drawImage(image, xo, yo, windowWidth, windowHeight, null); // Draws the image on the window
	}

	/** Renders the "Click to focus" box when you click off the screen. */
	private static void renderFocusNagger() {
		String msg = "Click to focus!"; // the message when you click off the screen.
		Updater.paused = true; // perhaps paused is only used for this.

		int x = (Screen.w - Font.textWidth(msg)) / 2;
		int y = (HEIGHT - 8) / 2;

		Font.drawBox(screen, x, y, Font.textWidth(msg) / 8, 1);

		// Renders the focus nagger text with a flash effect...
		if ((Updater.tickCount / 20) % 2 == 0) { // ...medium yellow color
			Font.draw(msg, screen, x, y, Color.get(1, 153));
		} else { // ...bright yellow color
			Font.draw(msg, screen, x, y, Color.get(5, 255));
		}
	}

	public static java.awt.Dimension getWindowSize() {
		return new java.awt.Dimension((int) (WIDTH * SCALE), (int) (HEIGHT * SCALE));
	}
}