package minicraft.entity;
import org.tinylog.Logger;

import minicraft.core.Game;
import minicraft.level.Level;
import minicraft.network.Network;

public class LifecycleManager {

    public void die(Entity entity) {
        entity.remove();
    }

    public void remove(Entity entity) {
        entity.setRemoved(true);

        if (entity.getLevel() != null) {
            entity.getLevel().remove(entity);
        }
    }

    public void remove(Entity entity, Level level) {
        if (level != entity.getLevel()) {
            if (Game.debug) {
                Logger.info("Tried to remove entity " + entity + " from level it is not in: " + level +
                        "; in level " + entity.getLevel());
            }
        } else {
            entity.setRemoved(true);
            entity.setLevel(null);
        }
    }

    public void setLevel(Entity entity, Level level, int x, int y) {
        if (level == null) {
            Logger.warn("Tried to set level of entity " + entity + " to a null level; Should use remove(level)");
            return;
        }

        entity.setLevel(level);
        entity.setRemoved(false);
        entity.setX(x);
        entity.setY(y);

        if (entity.getEid() < 0) {
            entity.setEid(Network.generateUniqueEntityId());
        }
    }
}

