package ekx.unit.ekxi.ai;

import arc.util.Time;
import mindustry.content.Fx;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;

public class EndAttackAI extends AttackAI {
    @Override
    public void updateWeapon(Teamc target) {
        time += Time.delta;
        if (time > 600f) {
            ended = true;
            return;
        }

        if (time % 60f < Time.delta) {
            float radius = 400f;
            
            Fx.healWaveDynamic.at(unit.x, unit.y, radius);
            Fx.shockwave.at(unit.x, unit.y);
            
            Groups.unit.intersect(unit.x - radius, unit.y - radius, radius * 2, radius * 2, u -> {
                if (u.team != unit.team) {
                    u.damagePierce(10000f);
                }
            });
        }
    }
}
