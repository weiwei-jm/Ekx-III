package ekx.unit.ekxi;

import arc.util.Time;
import mindustry.content.Fx;
import mindustry.gen.UnitEntity;
import mindustry.type.StatusEffect;

public class EkxIUnit extends UnitEntity {
    
    public boolean isReviving = false;
    public float reviveTimer = 0f;
    public final float reviveDuration = 300f; // 5秒复活读条
    public boolean castingSkill = false;      // 释放技能无敌

    @Override
    public void update() {
        super.update();
        
        if (isReviving) {
            reviveTimer += Time.delta;
            this.vel.setZero(); // 复活期间锁定移动
            
            if (reviveTimer >= reviveDuration) {
                isReviving = false;
                reviveTimer = 0f;
                this.health = this.maxHealth; // 读条完成，满血复活
                Fx.massiveExplosion.at(this.x, this.y);
            }
        }
    }

    @Override
    public void apply(StatusEffect effect, float duration) {
        // 免疫所有负面效果
        if (effect.disadvantageous()) return;
        super.apply(effect, duration);
    }

    @Override
    public void damage(float amount) {
        // 1. 放技能期间绝对无敌
        if (castingSkill) return;
        
        // 2. 如果处于“复活读条”期间，跟原版普通单位一模一样，正常挨打，不减免！
        if (isReviving) {
            super.damage(amount);
            return;
        }

        // 3. 正常存活状态下，所有伤害减半
        super.damage(amount * 0.5f);
    }

    @Override
    public void kill() {
        if (!isReviving) {
            // 第一次死亡：进入读条状态
            isReviving = true;
            reviveTimer = 0f;
            this.health = 0; 
            Fx.massiveExplosion.at(this.x, this.y);
        } else {
            // 如果复活读条期间又被别人打死了，那就彻底死透，无法复活
            super.kill();
        }
    }
}
