package ekx.unit.ekxi.ai;

import mindustry.entities.units.UnitController;
import mindustry.entities.Units; // 源码里在这个包下
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

public abstract class AttackAI implements UnitController {
    public Unit unit;
    public float time = 0f;
    public boolean ended = false;

    @Override
    public void unit(Unit unit) {
        this.unit = unit;
    }

    @Override
    public Unit unit() {
        return unit;
    }

    @Override
    public void updateUnit() {
        if (unit == null || unit.dead) return;

        // 用你源码里的 Units.closestTarget 找敌人
        Teamc target = Units.closestTarget(unit.team, unit.x, unit.y, 800f);
        if (target != null) {
            unit.lookAt(target);
        }

        updateWeapon(target);
    }

    public abstract void updateWeapon(Teamc target);

    public void reset() {
        time = 0f;
        ended = false;
    }
}
