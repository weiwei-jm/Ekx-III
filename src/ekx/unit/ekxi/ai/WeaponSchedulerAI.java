package ekx.unit.ekxi.ai;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;

public class WeaponSchedulerAI extends AttackAI {
    private Teamc strongest;
    private float scanTime = 0f;
    private float reload = 0f;

    private final Ekx3DMagicAI magicAI = new Ekx3DMagicAI();
    private boolean useMagic = false;

    @Override
    public void updateUnit() {
        if (unit == null || unit.dead) return;

        updateTargeting();

        if (strongest == null) return;

        unit.lookAt(strongest);
        reload -= Time.delta;

        float targetHealth = (strongest instanceof mindustry.gen.Healthc h) ? h.health() : 1000f;

        if (targetHealth > 10000f) {
            if (!useMagic) {
                if (reload <= 0f) {
                    useMagic = Mathf.chance(0.5f);
                    reload = 300f;
                    
                    if (useMagic) {
                        magicAI.unit(unit);
                        magicAI.reset();
                    } else {
                        // 找不到大激光类，直接用原版激光炮特效代替
                        mindustry.content.Fx.hitLaserBlast.at(unit.x, unit.y, unit.angleTo(strongest));
                        if(strongest instanceof mindustry.gen.Healthc h) h.damagePierce(20000f);
                        mindustry.Vars.renderer.shake(15f, 10f);
                    }
                }
            } else {
                magicAI.updateUnit();
                if (magicAI.ended) {
                    useMagic = false;
                    reload = 120f;
                }
            }
        } else {
            useMagic = false;
            if (reload <= 0f) {
                new EkxSmallLaserBullet().create(unit, unit.team, unit.x, unit.y, unit.angleTo(strongest));
                reload = 15f; 
            }
        }

        float dst = unit.dst(strongest);
        if (dst > 500f) {
            Vec2 v = Tmp.v1.trns(unit.angleTo(strongest), unit.type.speed);
            unit.movePref(v);
        }
    }

    private void updateTargeting() {
        if (scanTime > 0) scanTime -= Time.delta;
        
        if (strongest != null && (!strongest.isAdded() || (strongest instanceof mindustry.gen.Healthc h && h.dead()))) {
            strongest = null;
        }

        if (scanTime <= 0 || strongest == null) {
            strongest = null;
            final float[] maxScore = {0f};
            final Teamc[] bestTarget = {null};

            for (Unit u : mindustry.gen.Groups.unit) {
                if (u.team != unit.team && u.isValid() && unit.dst(u) < 1000f) {
                    float score = u.maxHealth / 100f - unit.dst(u) / 1000f;
                    if (score > maxScore[0]) {
                        maxScore[0] = score;
                        bestTarget[0] = u;
                    }
                }
            }
            
            for (mindustry.game.Teams.TeamData data : mindustry.Vars.state.teams.present) {
                if (data.team != unit.team && data.turretTree != null) {
                    data.turretTree.intersect(unit.x - 1000f, unit.y - 1000f, 2000f, 2000f, b -> {
                        float score = b.maxHealth / 60f - unit.dst(b) / 1000f;
                        if (b instanceof TurretBuild) score += 500f; 
                        if (score > maxScore[0]) {
                            maxScore[0] = score;
                            bestTarget[0] = b;
                        }
                    });
                }
            }
            
            strongest = bestTarget[0];
            scanTime = 15f; 
        }
    }

    @Override public void updateWeapon(Teamc target) {}
    @Override public void reset() { 
        reload = 0f; 
        useMagic = false;
        if (magicAI != null) magicAI.reset();
    }
}
