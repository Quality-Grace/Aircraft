package minicraft.level;

import java.util.Random;

import minicraft.level.mapGeneration.*;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

public class LevelGen {

	private static long worldSeed = 0;

	private static final Random random = new Random(worldSeed); // Initializes the random class
	private double[] values; // An array of doubles, used to help making noise for the map
	
	private final int w, h;

	/**
	 * This creates noise to create random values for level generation
	 */
	public LevelGen(int w, int h, int featureSize) {
		this.w = w;
		this.h = h;

		values = new double[w * h]; // Creates the size of the value array (width * height)

		/// to be 16 or 32, in the code below.
		for (int y = 0; y < w; y += featureSize) {
			for (int x = 0; x < w; x += featureSize) {

				// This method sets the random value from -1 to 1 at the given coordinate.
				setSample(x, y, random.nextFloat() * 2 - 1);
			}
		}

		int stepSize = featureSize;
		double scale = (1.3 / ((double) w));
		double scaleMod = 1.0;

		do {
			int halfStep = stepSize / 2;
			for (int y = 0; y < h; y += stepSize) {
				for (int x = 0; x < w; x += stepSize) { // this loops through the values again, by a given increment...

					double a = sample(x, y); // fetches the value at the coordinate set previously (it fetches the exact same ones that were just set above)
					double b = sample(x + stepSize, y); // fetches the value at the next coordinate over. This could possibly loop over at the end, and fetch the first value in the row instead.
					double c = sample(x, y + stepSize); // fetches the next value down, possibly looping back to the top of the column.
					double d = sample(x + stepSize, y + stepSize); // fetches the value one down, one right.

					/*
					 * This could probably use some explaining... Note: the number values are
					 * probably only good the first time around...
					 *
					 * This starts with taking the average of the four numbers from before (they
					 * form a little square in adjacent tiles), each of which holds a value from -1
					 * to 1. Then, it basically adds a 5th number, generated the same way as before.
					 * However, this 5th number is multiplied by a few things first... ...by
					 * stepSize, aka featureSize, and scale, which is 2/size the first time.
					 * featureSize is 16 or 32, which is a multiple of the common level size, 128.
					 * Precisely, it is 128 / 8, or 128 / 4, respectively with 16 and 32. So, the
					 * equation becomes size / const * 2 / size, or, simplified, 2 / const. For a
					 * feature size of 32, stepSize * scale = 2 / 4 = 1/2. featureSize of 16, it's 2
					 * / 8 = 1/4. Later on, this gets closer to 4 / 4 = 1, so... the 5th value may
					 * not change much at all in later iterations for a feature size of 32, which
					 * means it has an effect of 1, which is actually quite significant to the value
					 * that is set. So, it tends to decrease the 5th -1 or 1 number, sometimes
					 * making it of equal value to the other 4 numbers, sort of. It will usually
					 * change the end result by 0.5 to 0.25, perhaps; at max.
					 */
					double e = (a + b + c + d) / 4.0 + (random.nextFloat() * 2 - 1) * stepSize * scale;

					/*
					 * This sets the value that is right in the middle of the other 4 to an average
					 * of the four, plus a 5th number, which makes it slightly off, differing by
					 * about 0.25 or so on average, the first time around.
					 */
					setSample(x + halfStep, y + halfStep, e);
				}
			}

			// This loop does the same as before, but it takes into account some of the half Steps we set in the last loop.
			for (int y = 0; y < h; y += stepSize) {
				for (int x = 0; x < w; x += stepSize) {

					double a = sample(x, y); // middle (current) tile
					double b = sample(x + stepSize, y); // right tile
					double c = sample(x, y + stepSize); // bottom tile
					double d = sample(x + halfStep, y + halfStep); // mid-right, mid-bottom tile
					double e = sample(x + halfStep, y - halfStep); // mid-right, mid-top tile
					double f = sample(x - halfStep, y + halfStep); // mid-left, mid-bottom tile

					// The 0.5 at the end is because we are going by half-steps..?
					// The H is for the right and surrounding mids, and g is the bottom and
					// surrounding mids.
					double H = (a + b + d + e) / 4.0 + (random.nextFloat() * 2 - 1) * stepSize * scale * 0.5; // adds middle, right, mr-mb, mr-mt, and random.
					double g = (a + c + d + f) / 4.0 + (random.nextFloat() * 2 - 1) * stepSize * scale * 0.5; // adds middle, bottom, mr-mb, ml-mb, and random.

					setSample(x + halfStep, y, H); // Sets the H to the mid-right
					setSample(x, y + halfStep, g); // Sets the g to the mid-bottom
				}
			}

			/**
			 * THEN... this stuff is set to repeat the system all over again! The featureSize is halved, allowing access to further unset
			 * mids, and the scale changes... The scale increases the first time, x1.8, but the second time it's x1.1, and after that
			 * probably a little less than 1. So, it generally increases a bit, maybe to 4 / w at tops. This results in the 5th random value
			 * being more significant than the first 4 ones in later iterations.
			 */
			stepSize /= 2;
			scale *= (scaleMod + 0.7);
			scaleMod *= 0.3;

		} while (stepSize > 1); // This stops when the stepsize is < 1, aka 0 b/c it's an int. At this point there are no more mid values.
	}

	// This merely returns the value, like Level.getTile(x, y).
	private double sample(int x, int y) {
		return values[(x & (w - 1)) + (y & (h - 1)) * w];
	} 

	private void setSample(int x, int y, double value) {
		/**
		 * This method is short, but difficult to understand. This is what I think it does: The values array is like a 2D array, but
		 * formatted into a 1D array; so the basic "x + y * w" is used to access a given value. The value parameter is a random number,
		 * above set to be a random decimal from -1 to 1. From above, we can see that the x and y values passed in range from 0 to the
		 * width/height, and increment by a certain constant known as the "featureSize". This implies that the locations chosen from this
		 * array, to put the random value in, somehow determine the size of biomes, perhaps. The x/y value is taken and AND'ed with the
		 * size-1, which could be 127. This just caps the value at 127; however, it shouldn't be higher in the first place, so it is merely
		 * a safety measure.
		 *
		 * In other words, this is just "values[x + y * w] = value;"
		 */
		values[(x & (w - 1)) + (y & (h - 1)) * w] = value;
	}

	@Nullable
	static short[][] createAndValidateMap(int w, int h, int level, long seed) {
		worldSeed = seed;

		if (level == 1){
			SkyMapGeneration skyMap = new SkyMapGeneration(w,h,seed);
			return skyMap.getMap();
		} else if (level == 0) {
			TopMapGeneration topMap = new TopMapGeneration(w,h,seed);
			return topMap.getMap();
		} else if (level == -4) {
			DungeonMapGeneration dungeonMap = new DungeonMapGeneration(w,h,seed);
			return dungeonMap.getMap();
		}else if ((level > -4) && (level < 0)){
			UndergroundMapGeneration undergroundMap = new UndergroundMapGeneration(w,h,-level,seed);
			return undergroundMap.getMap();
		}else if(level == 2) {
			VoidMapGeneration voidMap = new VoidMapGeneration(w,h,seed);
			return voidMap.getMap();
		}

		Logger.error("Level index {} is not valid. Could not generate a level!", level);
		return null;
	}

	public double getValue(int index){
		return this.values[index];
	}
}
