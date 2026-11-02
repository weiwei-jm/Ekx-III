package ekx.unit.ekxi.ai;

import ekx.unit.ekxi.EkxIDamage;
import mindustry.content.Fx;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

public class EndAttackAI extends AttackAI {

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public void updateWeapon(Teamc target) {
        // 持续 10 秒钟（600帧）
        time += mindustry.core.Time.delta;
        if (time > 600f) {
            ended = true;
            return;
        }

        // 每隔 60 帧（1秒）爆发出一次全屏伤害和蓝色特效
        if (time % 60f < mindustry.core.Time.delta) {
            float radius = 400f; // 技能范围
            
            // 播放爆炸特效（用原版特效代替，你可以后续换成你的蓝色法阵）
            Fx.healWaveDynamic.at(unit.x, unit.y, radius);
            Fx.shockwave.at(unit.x, unit.y);
            
            // 对范围内所有敌人造成巨额真实伤害
            Groups.unit.intersect(unit.x - radius, unit.y - radius, radius * 2, radius * 2, u -> {
                if (u.team != unit.team) {
                    // 此处就是触发无敌血量的反伤逻辑，如果打不死，也会造成巨量伤害
                    u.damagePierce(10000f);
                }
            });
        }
    }
}
