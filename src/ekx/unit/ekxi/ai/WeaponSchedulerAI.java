package ekx.unit.ekxi.ai;

import arc.math.Mathf;
import arc.math.geom.Vec2;
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

    public WeaponSchedulerAI() {
        weapons = new AttackAI[]{
            new BasicLaserAI(),
            new EndAttackAI()
        };
    }

    @Override
    public void updateUnit() {
        if (unit == null || unit.dead) return;

        // 1. 寻找最有价值的目标（追杀/支援）
        updateTargeting();
        updateMovement();

        // 2. 初始化武器
        if (weapons[currentWeaponIndex].unit() != unit) {
            weapons[currentWeaponIndex].unit(unit);
            weapons[currentWeaponIndex].reset();
        }

        AttackAI current = weapons[currentWeaponIndex];
        current.updateUnit();

        // 3. 切换武器
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

            // 扫描全图敌人，计算威胁值（血量越高、距离越近，分越高）
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
                        if (b instanceof TurretBuild) score += 500f; // 炮塔优先度更高
                        if (score > maxScore) {
                            maxScore = score;
                            strongest = b;
                        }
                    });
                }
            }
            scanTime = 30f; // 每半秒扫描一次
        }
    }

    private void updateMovement() {
        if (strongest != null) {
            float dst = unit.dst(strongest);
            // 如果离目标太远（大于攻击距离 700），就主动飞过去支援
            if (dst > 700f) {
                unit.movePref(Vec2.trns(unit.angleTo(strongest), unit.type.speed));
                unit.lookAt(strongest);
            } else if (dst < 400f) {
                // 如果贴得太近，就拉扯一下距离
                unit.movePref(Vec2.trns(unit.angleTo(strongest) + 180f, unit.type.speed));
                unit.lookAt(strongest);
            } else {
                // 在射程内，悬停
                unit.lookAt(strongest);
            }
        }
    }

    @Override public void updateWeapon(Teamc target) {}
    @Override public void reset() {}
            }
