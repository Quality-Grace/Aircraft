//Spawner

package minicraft.entity.furniture;


import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.EnemyMob;
import minicraft.entity.mob.MobAi;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.FireParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.graphic.Color;
import minicraft.graphic.Sprite;
import minicraft.item.FurnitureItem;
import minicraft.item.Item;
import minicraft.item.PotionType;
import minicraft.item.PowerGloveItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;


public class Spawner extends Furniture {

	private static int frame = 0;

	private static final int ACTIVE_RADIUS = 8 * 16;
	private static final int minSpawnInterval = 200;
	private static final int maxSpawnInterval = 400;
	private static final int minMobSpawnChance = 10;

	public MobAi mob;
	private int health;
	int lvl;
    private int spawnTick;
	private int tickTime;

	public Spawner(MobAi m) {
		super(getClassName(m.getClass()) + " Spawner", new Sprite(0 + frame, 32, 2, 2, 2), 7, 2);
		health = 100;
		initMob(m);
		resetSpawnInterval();
	}

	private static String getClassName(Class<?> c) {
		String fullName = c.getCanonicalName();
		return fullName.substring(fullName.lastIndexOf(".") + 1);
	}

	private void initMob(MobAi m) {
		mob = m;
		sprite.color = color = m.getColor();

        int maxMobLevel;
        if (m instanceof EnemyMob) {
			lvl = ((EnemyMob) m).getLvl();
			maxMobLevel = m.getMaxLevel();
		} else {
			lvl = 1;
			maxMobLevel = 1;
		}

		if (lvl > maxMobLevel) {
			lvl = maxMobLevel;
		}
	}


	@Override
	public void tick() {
		super.tick();

		tickTime++;

		spawnTick--;
		if (spawnTick <= 0) {
			int chance = (int) (minMobSpawnChance * Math.pow(level.mobCount, 2) / Math.pow(level.maxMobCount, 2));
			if (chance <= 0 || random.nextInt(chance) == 0) trySpawn();
			resetSpawnInterval();
		}

		// Fire particles
		if (tickTime / 2 % 8 == 0) {
			if (Settings.get("particles").equals(true)) {
				level.add(new FireParticle(x - 10 + random.nextInt(14), y - 8 + random.nextInt(12)));
			}
		} else {
			frame = random.nextInt(3) * 2;
		}

		if (Settings.get("diff").equals("Peaceful")) {
			resetSpawnInterval();
		}
	}

	private void resetSpawnInterval() {
		spawnTick = random.nextInt(maxSpawnInterval - minSpawnInterval + 1) + minSpawnInterval;
	}

	private void trySpawn() {
		if (shouldNotSpawn())
			return;

		Player player = getClosestPlayer();
		if (player == null)
			return;

		if (isOutsideActiveRadius(player))
			return;

		SpawnerLogic.spawnMob(this, player, level);
	}

	private boolean shouldNotSpawn() {
		return level == null || level.mobCount >= level.maxMobCount || (mob instanceof EnemyMob && shouldNotSpawnEnemyMob());
	}

	private boolean shouldNotSpawnEnemyMob() {
		return (level.depth >= 0 && Updater.tickCount > Updater.sleepEndTime && Updater.tickCount < Updater.sleepStartTime)
				|| level.isLight(x >> 4, y >> 4);
	}

	private boolean isOutsideActiveRadius(Player player) {
		int xd = player.x - x;
		int yd = player.y - y;
		return xd * xd + yd * yd > ACTIVE_RADIUS * ACTIVE_RADIUS;
	}

	@Override
	public boolean interact(Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			handleToolInteraction(player, (ToolItem) item);
			return true;
		}

		if (item instanceof PowerGloveItem && Game.isMode("Creative")) {
			return handleCreativePowerGloveInteraction(player);
		}

		if (item == null) {
			return use(player);
		}

		return false;
	}

	private void handleToolInteraction(Player player, ToolItem tool) {
		Sound.genericHurt.playOnLevel(this.x, this.y);

		int toolDamage = calculateToolDamage(tool, player);

		health -= toolDamage;
		displayToolDamageText(toolDamage);

		if (health <= 0) {
			SpawnerLogic.handleEntityDeath(this, player);
		}
	}

	private int calculateToolDamage(ToolItem tool, Player player) {
		if (Game.isMode("Creative")) {
			return health;
		} else {
			int toolDamage = tool.level + random.nextInt(2);

			if (tool.type == ToolType.Pickaxe) {
				toolDamage += random.nextInt(5) + 2;
			}

			if (player.potionEffects.containsKey(PotionType.Haste)) {
				toolDamage *= 2;
			}

			return toolDamage;
		}
	}

	private void displayToolDamageText(int toolDamage) {
		level.add(new TextParticle("" + toolDamage, x, y, Color.get(-1, 200, 300, 400)));
	}

	private boolean handleCreativePowerGloveInteraction(Player player) {
		level.remove(this);
		if (!(player.activeItem instanceof PowerGloveItem)) {
			player.getInventory().add(0, player.activeItem);
		}
		player.activeItem = new FurnitureItem(this);
		return true;
	}

}