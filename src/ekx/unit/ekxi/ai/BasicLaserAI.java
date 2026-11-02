package ekx.unit.ekxi.ai;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.math.geom.Vec2;
import arc.util.Time;
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
            
            // 手写激光判定，避开原版烦人的参数限制
            for (Unit u : Groups.unit) {
                if (u.team != unit.team) {
                    // 计算敌人到激光线段的距离
                    float angleToU = unit.angleTo(u);
                    float dstToU = unit.dst(u);
                    if (dstToU < length && Angles.angleDist(angle, angleToU) < Mathf.atan2(u.hitSize / 2f, dstToU)) {
                        u.damagePierce(2000f);
                    }
                }
            }
            
            reload = 15f; 
        }
        
        if (reload > 0) reload -= Time.delta;
    }
}
