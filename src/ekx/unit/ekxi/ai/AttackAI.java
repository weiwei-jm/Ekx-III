package ekx.unit.ekxi.ai;

import mindustry.entities.units.UnitController;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.gen.Units;

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

    // 子类如果不重写，默认什么都不做
    @Override
    public void updateUnit() {
        if (unit == null || unit.dead) return;

        Teamc target = Units.closestTarget(unit.team, unit.x, unit.y, 800f, u -> true, b -> true);
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
