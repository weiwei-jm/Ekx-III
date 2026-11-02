package ekx.unit.ekxi.ai;

import arc.math.geom.Vec2;
import ekx.unit.NullAI;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;

public class WeaponSchedulerAI extends AttackAI {
    private final AttackAI[] weapons;
    private int currentWeaponIndex = 0;

    private Teamc strongest;
    private float scanTime = 0f;

    // 备用挂机 AI
    private final NullAI idleAI = new NullAI();

    public WeaponSchedulerAI() {
        weapons = new AttackAI[]{
            new BasicLaserAI(),
            new EndAttackAI()
        };
    }

    @Override
    public void updateUnit() {
        if (unit == null || unit.dead) return;

        // 1. 寻找目标
        updateTargeting();

        // 2. 如果没有目标，切换回 NullAI 挂机
        if (strongest == null) {
            // 确保 idleAI 绑定到了当前单位
            if (idleAI.unit() != unit) {
                idleAI.unit(unit);
            }
            // 让原版的空 AI 接管（可以停下来或者执行原版基础行为）
            idleAI.updateUnit();
            return; // 没敌人，直接返回，不执行下面的武器逻辑
        }

        // 3. 有目标，执行移动和攻击
        updateMovement();

        // 4. 初始化武器
        if (weapons[currentWeaponIndex].unit() != unit) {
            weapons[currentWeaponIndex].unit(unit);
            weapons[currentWeaponIndex].reset();
        }

        AttackAI current = weapons[currentWeaponIndex];
        current.updateUnit();

        // 5. 切换武器
        if (current.ended) {
            currentWeaponIndex = (currentWeaponIndex + 1) % weapons.length;
        }
    }

    private void updateTargeting() {
        if (scanTime > 0) scanTime -= mindustry.core.Time.delta;
        
        if (strongest != null && (!strongest.isAdded() || (strongest instanceof mindustry.gen.Healthc h && h.dead()))) {
            strongest = null;
        }

        if (scanTime <= 0 || strongest == null) {
            strongest = null;
            float maxScore = 0f;

            // 扫描单位
            for (Unit u : mindustry.gen.Groups.unit) {
                if (u.team != unit.team && u.isValid()) {
                    float score = u.maxHealth / 100f - unit.dst(u) / 1000f;
                    if (score > maxScore) {
                        maxScore = score;
                        strongest = u;
                    }
                }
            }
            
            // 扫描炮塔
            for (mindustry.game.Teams.TeamData data : mindustry.Vars.state.teams.present) {
                if (data.team != unit.team && data.turretTree != null) {
                    data.turretTree.intersect(unit.x - 1500f, unit.y - 1500f, 3000f, 3000f, b -> {
                        float score = b.maxHealth / 60f - unit.dst(b) / 1000f;
                        if (b instanceof TurretBuild) score += 500f; 
                        if (score > maxScore) {
                            maxScore = score;
                            strongest = b;
                        }
                    });
                }
            }
            scanTime = 10f;
        }
    }

    private void updateMovement() {
        if (strongest != null) {
            float dst = unit.dst(strongest);
            if (dst > 700f) {
                unit.movePref(Vec2.trns(unit.angleTo(strongest), unit.type.speed));
                unit.lookAt(strongest);
            } else if (dst < 400f) {
                unit.movePref(Vec2.trns(unit.angleTo(strongest) + 180f, unit.type.speed));
                unit.lookAt(strongest);
            } else {
                unit.lookAt(strongest);
            }
        }
    }

    @Override public void updateWeapon(Teamc target) {}
    @Override public void reset() {}
}
