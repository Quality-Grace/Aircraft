package minicraft.entity;

import java.util.List;

import minicraft.core.Updater;
import minicraft.entity.mob.Player;
import minicraft.graphic.Rectangle;

public class MovementHandler {

    public boolean move(Entity entity, int xd, int yd) {
        if (Updater.saving || (xd == 0 && yd == 0)) return true;

        boolean stopped = !move2(entity, xd, 0);

        if (move2(entity, 0, yd)) stopped = false;

        if (!stopped) {
            int xt = entity.x >> 4;
            int yt = entity.y >> 4;
            entity.getLevel().getTile(xt, yt).steppedOn(entity.getLevel(), xt, yt, entity);
        }

        return !stopped;
    }

    public boolean move2(Entity entity, int xd, int yd) {
        if (xd == 0 && yd == 0) {
            return true;
        }

        boolean interact = true;

        // Calculate original and new tile coordinates
        int xto0 = (entity.x - entity.xr) >> 4;
        int yto0 = (entity.y - entity.yr) >> 4;
        int xto1 = (entity.x + entity.xr) >> 4;
        int yto1 = (entity.y + entity.yr) >> 4;

        int xt0 = (entity.x + xd - entity.xr) >> 4;
        int yt0 = (entity.y + yd - entity.yr) >> 4;
        int xt1 = (entity.x + xd + entity.xr) >> 4;
        int yt1 = (entity.y + yd + entity.yr) >> 4;

        // Extracted methods for better readability
        if (!checkTilesAndInteract(entity, xd, yd, interact, xto0, yto0, xto1, yto1, xt0, yt0, xt1, yt1)) {
            return false;
        }

        List<Entity> wasInside = entity.getLevel().getEntitiesInRect(entity.getBounds());

        List<Entity> isInside = entity.getLevel().getEntitiesInRect(
                new Rectangle(entity.x + xd, entity.y + yd, entity.xr * 2, entity.yr * 2, Rectangle.CENTER_DIMS));

        // Extracted methods for better readability
        processInteractions(entity, interact, isInside, wasInside);

        isInside.removeAll(wasInside);

        // Extracted methods for better readability
        if (!checkBlockages(entity, isInside)) {
            return false;
        }

        // Move the entity
        entity.x += xd;
        entity.y += yd;

        return true;
    }

    // Extracted method for better readability
    private boolean checkTilesAndInteract(Entity entity, int xd, int yd, boolean interact,
                                          int xto0, int yto0, int xto1, int yto1,
                                          int xt0, int yt0, int xt1, int yt1) {
        for (int yt = yt0; yt <= yt1; yt++) {
            for (int xt = xt0; xt <= xt1; xt++) {
                if (xt >= xto0 && xt <= xto1 && yt >= yto0 && yt <= yto1) {
                    continue;
                }

                entity.getLevel().getTile(xt, yt).bumpedInto(entity.getLevel(), xt, yt, entity);

                if (!entity.getLevel().getTile(xt, yt).mayPass(entity.getLevel(), xt, yt, entity)) {
                    return false;
                }
            }
        }
        return true;
    }

    // Extracted method for better readability
    private void processInteractions(Entity entity, boolean interact, List<Entity> isInside, List<Entity> wasInside) {
        if (interact) {
            for (Entity e : isInside) {
                if (e == entity) {
                    continue;
                }

                if (e instanceof Player && !(entity instanceof Player)) {
                    entity.touchedBy(e);
                } else {
                    e.touchedBy(entity);
                }
            }
        }
    }

    // Extracted method for better readability
    private boolean checkBlockages(Entity entity, List<Entity> isInside) {
        for (Entity e : isInside) {
            if (e == entity) {
                continue;
            }

            if (e.blocks(entity)) {
                return false;
            }
        }
        return true;
    }

}
