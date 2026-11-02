package ekx.unit.ekxi.ai;

import arc.math.geom.Vec2;
import arc.util.Time;
import ekx.unit.NullAI;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;

public class WeaponSchedulerAI extends AttackAI {
    private final AttackAI[] weapons;
    private int currentWeaponIndex = 0;

    private Teamc strongest;
    private float scanTime = 0f;

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

        updateTargeting();

        // 如果没有目标，交给 NullAI
        if (strongest == null) {
            if (idleAI.unit() != unit) {
                idleAI.unit(unit);
            }
            idleAI.updateUnit();
            return; 
        }

        updateMovement();

        if (weapons[currentWeaponIndex].unit() != unit) {
            weapons[currentWeaponIndex].unit(unit);
            weapons[currentWeaponIndex].reset();
        }

        AttackAI current = weapons[currentWeaponIndex];
        current.updateUnit();

        if (current.ended) {
            currentWeaponIndex = (currentWeaponIndex + 1) % weapons.length;
        }
    }

    private void updateTargeting() {
        if (scanTime > 0) scanTime -= Time.delta;
        
        if (strongest != null && (!strongest.isAdded() || (strongest instanceof mindustry.gen.Healthc h && h.dead()))) {
            strongest = null;
        }

        if (scanTime <= 0 || strongest == null) {
            strongest = null;
            float maxScore = 0f;

            for (Unit u : mindustry.gen.Groups.unit) {
                if (u.team != unit.team && u.isValid()) {
                    float score = u.maxHealth / 100f - unit.dst(u) / 1000f;
                    if (score > maxScore) {
                        maxScore = score;
                        strongest = u;
                    }
                }
            }
            
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
            scanTime = 30f;
        }
    }

    private void updateMovement() {
        if (strongest != null) {
            float dst = unit.dst(strongest);
            if (dst > 700f) {
                // 直接用我们之前用过很多次的 Tmp.v1 向量
                arc.math.geom.Vec2 v = arc.util.Tmp.v1.trns(unit.angleTo(strongest), unit.type.speed);
                unit.movePref(v);
                unit.lookAt(strongest);
            } else if (dst < 400f) {
                arc.math.geom.Vec2 v = arc.util.Tmp.v1.trns(unit.angleTo(strongest) + 180f, unit.type.speed);
                unit.movePref(v);
                unit.lookAt(strongest);
            } else {
                unit.lookAt(strongest);
            }
        }
    }

    @Override public void updateWeapon(Teamc target) {}
    @Override public void reset() {}
}
