package minicraft.level;

import minicraft.core.Game;
import minicraft.level.tile.Tiles;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class LevelGenPreview {
    private static final int w = 256;
    private static final int h = 256;
    private static final int mapScale = 512/w;

    // Prints the seed and the elapsed time
    public static void debugTimeLevel(long startNanoTime, int lvl, long worldSeed){
        long endSecondsTime = (System.nanoTime() - startNanoTime) >> 30;
        String finalGenTime = "took " + endSecondsTime + "s";

        Logger.debug("Generated level {}, with seed {}, {}", lvl, worldSeed, finalGenTime);
    }

    public static int[] initializeLevelMap(String[] args){
        if (args.length > 0) {
            int[] mapLevels = new int[args.length];
            for (int i = 0; i < args.length; i++) {
                try {
                    int levelNum = Integer.parseInt(args[i]);
                    mapLevels[i] = levelNum;
                } catch (NumberFormatException exception) {
                    break;
                }
            }
            return mapLevels;
        } else {
            return new int[]{0};
        }
    }

    @NotNull
    private static BufferedImage initializeMapImage(short[] map) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB); // Creates an image
        int[] pixels = new int[w * h]; // The pixels in the image. (an integer array, the size is Width * height)

        for (int y = 0; y < h; y++) { // Loops through the height of the map
            for (int x = 0; x < w; x++) { // (inner-loop)Loops through the entire width of the map
                int i = x + y * w; // Current tile of the map.

				/*The colors used in the pixels are hexadecimal (0xRRGGBB) and stored in the TilePixelColourEnum.
				  0xff0000 would be fully red
				  and 0xffffff would be white etc.

				  This static method takes the pixels and map array and also the current index.
				  For every tile in the map array it will add their colour to the pixels array.
				 */
                Tiles.addColourToArray(pixels, i, map[i]);
            }
        }
        img.setRGB(0, 0, w, h, pixels, 0, w); // Sets the pixels into the image
        return img;
    }

    public static int debugJOptionPaneInitializer(BufferedImage img){
        // Name of the buttons used for the window.
        String[] options = {
                "Another",
                "Quit"
        };

        return JOptionPane.showOptionDialog(
                null, null,
                "Map Generator",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                new ImageIcon(img.getScaledInstance(w * mapScale, h * mapScale, Image.SCALE_SMOOTH)),
                options, null
        );
    }
    public static void main(String[] args) {
        /*
         * This is used to see seeds without having to run the game I mean, this is a
         * world viewer that uses the same method as above using perlin noise, to
         * generate a world, and be able to see it in a JPane according to the size of
         * the world generated
         */
        Random random = new Random(0x100);
        long worldSeed = random.nextLong();
        // Fixes to get this method to work
        // AirWizard needs this in constructor
        Game.gameDir = "";

        // Initialize the tiles
        Tiles.initialize();

        // End of fixes

        int idx = -1;

        int[] mapLevels = initializeLevelMap(args);

        int generator = 0;
        do { // stop the loop and close the program

            long startNanoTime = System.nanoTime();

            int lvl = mapLevels[idx++ % mapLevels.length];
            if (lvl > 2 || lvl < -4) continue;

            short[][] fullMap = LevelGen.createAndValidateMap(w, h, 0, worldSeed);

            if (fullMap == null) continue;

            // Create the map image
            BufferedImage img = initializeMapImage(fullMap[0]); // Creates an image

            // Prints the seed and the elapsed time
            debugTimeLevel(startNanoTime, lvl, worldSeed);

            worldSeed = random.nextLong();

            // Generates the JOptionPane
            generator = debugJOptionPaneInitializer(img);

			/* Now you noticed that we made the dialog an integer. This is because when you click a button it will return a number.
               Since we passed in 'options', the window will return 0 if you press "Another" and it will return 1 when you press "Quit".
               If you press the red "x" close mark, the window will return -1
			 */
            // If the dialog returns -1 (red "x" button) or 1 ("Quit" button) then...
        } while(generator != -1 && generator != 1);
    }
}
