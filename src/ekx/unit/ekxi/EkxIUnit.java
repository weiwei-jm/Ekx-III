package ekx.unit.ekxi;

import arc.util.Time;
import mindustry.content.Fx;
import mindustry.gen.UnitEntity;
import mindustry.type.StatusEffect;

public class EkxIUnit extends UnitEntity {
    public boolean isReviving = false;
    public float reviveTimer = 0f;
    public final float reviveDuration = 300f; 
    public boolean castingSkill = false; 

    @Override
    public void update() {
        super.update();
        if (isReviving) {
            reviveTimer += Time.delta;
            this.vel.setZero(); 
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
        // v160 中负面效果判断一般用这个（或者直接写死免疫燃烧/融化等）
        if (effect == mindustry.content.StatusEffects.burning || 
            effect == mindustry.content.StatusEffects.melting || 
            effect == mindustry.content.StatusEffects.disarmed) return;
        super.apply(effect, duration);
    }

    @Override
    public void damage(float amount) {
        if (castingSkill) return;
        if (isReviving) {
            super.damage(amount);
            return;
        }
        super.damage(amount * 0.5f);
    }

    @Override
    public void kill() {
        if (!isReviving) {
            isReviving = true;
            reviveTimer = 0f;
            this.health = 0; 
            Fx.massiveExplosion.at(this.x, this.y);
        } else {
            super.kill();
        }
    }
}
