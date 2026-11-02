package ekx.unit.ekxi;

import arc.util.Time;
import mindustry.content.Fx;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;

public class EkxIUnit extends UnitEntity {
    
    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public void update() {
        super.update();
        
        // 只要没死，就强行把血量锁在 1
        if (!dead) {
            health = 1f;
        }

        // 如果真的死了（比如被代码杀、被特殊大招秒杀），立刻触发重组
        if (dead) {
            respawn();
        }
    }

    // 拦截普通伤害，不掉血（因为我们要通过 collision 抓攻击者）
    @Override
    public void damage(float amount) {
        // 忽略，避免原版直接判定死亡
    }

    @Override
    public void damagePierce(float amount, boolean withEffect) {
        // 忽略
    }

    // 核心反伤机制：子弹碰撞拦截
    @Override
    public void collision(Hitboxc other, float x, float y) {
        super.collision(other, x, y);
        
        if (other instanceof Bullet b) {
            float amount = b.damage;
            Object owner = b.owner;

            // 1. 反伤给单位
            if (owner instanceof Unit u) {
                u.damagePierce(amount);
            } 
            // 2. 反伤给炮塔
            else if (owner instanceof TurretBuild tb) {
                tb.damagePierce(amount);
            }
            // 3. 如果是环境/出生点等，直接免疫

            // 移除这颗子弹，避免重复计算
            b.remove();
        }
    }

    // 死亡与重组特效
    public void respawn() {
        // 播放碎裂爆炸效果
        Fx.dynamicExplosion.at(x, y, hitSize / 8f);
        Fx.shockwave.at(x, y);
        
        // 瞬间满血复活
        dead = false;
        health = 1f;
        maxHealth = 1f;
        
        // 清除所有负面状态（防止复活后还在燃烧/融化）
        clearStatuses();
        
        // 播放蓝色重组特效（我用原版治疗特效替代，视觉上很契合）
        Fx.healWaveDynamic.at(x, y, hitSize);
        Fx.heal.at(x, y);
    }
}
