package ekx.unit.ekxi;

import arc.util.Time;
import mindustry.content.Fx;
import mindustry.gen.UnitEntity;
import mindustry.type.StatusEffect;

public class EkxIUnit extends UnitEntity {
    
    // === 原 EkxExecutorUnit 的所有机制 ===
    public boolean isReviving = false;
    public float reviveTimer = 0f;
    public final float reviveDuration = 300f; // 5秒
    public boolean castingSkill = false; // 放技能期间无敌

    @Override
    public void update() {
        super.update();
        
        if (isReviving) {
            reviveTimer += Time.delta;
            this.vel.setZero(); // 复活期间锁定移动
            
            if (reviveTimer >= reviveDuration) {
                isReviving = false;
                reviveTimer = 0f;
                this.health = this.maxHealth;
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
        // 放技能期间绝对无敌
        if (castingSkill) return;
        
        // 复活期间如果被打，直接死透
        if (isReviving) {
            super.damage(amount);
            return;
        }

        // 所有伤害减半
        super.damage(amount * 0.5f);
    }

    @Override
    public void kill() {
        if (!isReviving) {
            // 第一次死亡，进入复活状态
            isReviving = true;
            reviveTimer = 0f;
            this.health = 0; 
            Fx.massiveExplosion.at(this.x, this.y);
        } else {
            // 复活期间再次死亡，彻底死透
            super.kill();
        }
    }
}
