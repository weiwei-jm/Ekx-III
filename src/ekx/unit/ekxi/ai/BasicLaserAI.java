package ekx.unit.ekxi.ai;

import arc.util.Time;
import mindustry.entities.Damage;
import mindustry.gen.Healthc;
import mindustry.gen.Teamc;

public class BasicLaserAI extends AttackAI {
    float reload = 0f;

    @Override
    public void reset() {
        super.reset();
        reload = 0f;
    }

    @Override
    public void updateWeapon(Teamc target) {
        time += Time.delta;
        if (time > 300f) {
            ended = true;
            return;
        }

        if (target != null && reload <= 0f) {
            float angle = unit.angleTo(target);
            float length = 600f;
            
            Damage.collideLine(unit.team, unit.x, unit.y, angle, length, 
                h -> {
                    if (h instanceof Healthc hc) {
                        hc.damagePierce(2000f);
                    }
                }, 
                h -> true, 
                (h, x, y) -> {}
            );
            reload = 15f; 
        }
        
        if (reload > 0) reload -= Time.delta;
    }
}
