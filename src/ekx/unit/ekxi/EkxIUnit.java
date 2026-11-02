package ekx.unit.ekxi;

import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;

public class EkxIUnit extends UnitEntity {
    
    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public void add() {
        super.add();
        if (!headless && isAdded()) {
            EkxIManager.register(this);
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (!headless) {
            EkxIManager.unregister(this);
        }
    }

    @Override
    public void update() {
        // 单位只需要处理自身的血量状态，战斗逻辑全权交给 Scheduler
        super.update();
        if (!dead) {
            health = 1f;
        }
    }

    @Override
    public void damage(float amount) { }

    @Override
    public void damagePierce(float amount, boolean withEffect) { }

    @Override
    public void collision(Hitboxc other, float x, float y) {
        super.collision(other, x, y);
        if (other instanceof Bullet b) {
            float amount = b.damage;
            Object owner = b.owner;
            if (owner instanceof Unit u) u.damagePierce(amount);
            else if (owner instanceof TurretBuild tb) tb.damagePierce(amount);
            b.remove();
        }
    }
}
