package ekx.unit.ekxi.ai;

import ekx.unit.ekxi.EkxIDamage;
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
        // 持续 5 秒钟（300帧）
        time += mindustry.core.Time.delta;
        if (time > 300f) {
            ended = true;
            return;
        }

        if (target != null && reload <= 0f) {
            float angle = unit.angleTo(target);
            float length = 600f;
            
            // 发射一道蓝色的激光（直接判定）
            Damage.collideLine(unit.team, unit.x, unit.y, angle, length, 
                h -> {
                    if (h instanceof Healthc hc) {
                        EkxIDamage.damageUnit((mindustry.gen.Unit)hc, 2000f, true, null);
                    }
                }, 
                h -> true, 
                (h, x, y) -> {}
            );
            
            reload = 15f; // 持续射击间隔
        }
        
        if (reload > 0) reload -= mindustry.core.Time.delta;
    }
}
