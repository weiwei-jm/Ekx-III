package ekx.unit.ekxi.ai;

import arc.math.geom.Intersector;
import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.entities.Units;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

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
            
            // 手写激光命中判定，绕开原版复杂的 Damage.collideLine 参数限制
            Vec2 p1 = new Vec2(unit.x, unit.y);
            Vec2 p2 = new Vec2(unit.x, unit.y).add(Vec2.trns(angle, length));
            
            for (Unit u : Groups.unit) {
                if (u.team != unit.team) {
                    Vec2 nearest = Intersector.nearestSegmentPoint(p1, p2, u.x, u.y, new Vec2());
                    if (nearest.dst(u.x, u.y) < (u.hitSize / 2f) + 10f) {
                        u.damagePierce(2000f);
                    }
                }
            }
            
            reload = 15f; 
        }
        
        if (reload > 0) reload -= Time.delta;
    }
}
