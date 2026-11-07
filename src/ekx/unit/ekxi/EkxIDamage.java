package ekx.unit.ekxi;

import mindustry.gen.Building;
import mindustry.gen.Unit;

public class EkxIDamage {
    public static void damageUnit(Unit u, float damage, boolean lethal, Runnable onDeath) {
        if (u.dead) return;
        if (lethal) {
            u.damage(damage);
            if (u.dead && onDeath != null) onDeath.run();
        } else {
            u.damagePierce(damage);
        }
    }
    public static void damageBuilding(Building b, float damage, boolean lethal, Runnable onDeath) {
        if (b.dead) return;
        if (lethal) {
            b.damage(damage);
            if (b.dead && onDeath != null) onDeath.run();
        } else {
            b.damagePierce(damage);
        }
    }
}
