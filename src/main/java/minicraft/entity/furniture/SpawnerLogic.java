//SpawnerLogic
package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.mob.EnemyMob;
import minicraft.entity.mob.MobAi;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.FireParticle;
import minicraft.graphic.Point;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.core.io.Settings;


import java.util.ArrayList;

public class SpawnerLogic {


    public static void spawnMob(Spawner spawner, Player player, Level level) {
        MobAi newMob = createNewMob(spawner);
        if (newMob == null)
            return;

        Point spawnPos = findValidSpawnPosition(newMob, spawner, level);
        if (spawnPos == null)
            return;

        performMobSpawn(newMob, spawnPos, spawner, level);
    }

    private static MobAi createNewMob(Spawner spawner) {
        try {
            return (spawner.mob instanceof EnemyMob)
                    ? spawner.mob.getClass().getConstructor(int.class).newInstance(spawner.lvl)
                    : spawner.mob.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static Point findValidSpawnPosition(MobAi newMob, Spawner spawner, Level level) {
        Point position = new Point(spawner.x >> 4, spawner.y >> 4);
        Point[] areaPositions = level.getAreaTilePositions(position.x, position.y, 1);
        ArrayList<Point> validPositions = getValidSpawnPositions(newMob, spawner, areaPositions, level);

        return validPositions.isEmpty() ? null : validPositions.get(Game.random.nextInt(validPositions.size()));
    }

    private static ArrayList<Point> getValidSpawnPositions(MobAi newMob, Spawner spawner, Point[] areaPositions, Level level) {
        ArrayList<Point> validPositions = new ArrayList<>();
        for (Point point : areaPositions) {
            Tile tile = level.getTile(point.x, point.y);
            if (isValidSpawnPosition(newMob, tile, point.x, point.y, spawner, level)) {
                validPositions.add(point);
            }
        }
        return validPositions;
    }

    private static boolean isValidSpawnPosition(MobAi newMob, Tile tile, int tileX, int tileY, Spawner spawner, Level level) {
        return !(tile.mayPass(level, tileX, tileY, newMob) || (spawner.mob instanceof EnemyMob && tile.getLightRadius(level, tileX, tileY) > 0));
    }

    private static void performMobSpawn(MobAi newMob, Point spawnPos, Spawner spawner, Level level) {
        newMob.x = spawnPos.x << 4;
        newMob.y = spawnPos.y << 4;

        if (Game.debug) level.printLevelLoc("Spawning new " + spawner.mob, (newMob.x >> 4), (newMob.y >> 4), "...");

        level.add(newMob);
        Sound.Furniture_spawner_spawn.playOnLevel(spawner.x, spawner.y);

        spawnParticles(spawner, level);
    }

    private static void spawnParticles(Spawner spawner, Level level) {
        if (Settings.get("particles").equals(true)) {
            for (int i = 0; i < 3; i++) {
                level.add(new FireParticle(spawner.x - 8 + Game.random.nextInt(16), spawner.y - 6 + Game.random.nextInt(12)));
            }
        }
    }

    public static void handleEntityDeath(Spawner spawner, Player player) {
        spawner.getLevel().remove(spawner);

        switch (Game.random.nextInt(4)) {
            //case 0: Sound.Furniture_spawner_hurt.playOnLevel(spawner.x, spawner.y); break;
            case 1: Sound.Furniture_spawner_destroy.playOnLevel(spawner.x, spawner.y); break;
            case 2: Sound.Furniture_spawner_destroy_2.playOnLevel(spawner.x, spawner.y); break;
            case 3: Sound.Furniture_spawner_destroy_3.playOnLevel(spawner.x, spawner.y); break;
            default: Sound.Furniture_spawner_hurt.playOnLevel(spawner.x, spawner.y); break;
        }

        player.addScore(500);
    }
}
