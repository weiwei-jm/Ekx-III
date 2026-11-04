package ekx.unit.ekxi.ai;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import ekx.EkxPal;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;

public class WeaponSchedulerAI extends AttackAI {
    private Teamc strongest;
    private float scanTime = 0f;
    private float reload = 0f;

    private final Ekx3DMagicAI magicAI = new Ekx3DMagicAI();
    private final EkxSurroundLaserAI surroundAI = new EkxSurroundLaserAI();
    
    private int currentState = 0; // 0=无, 1=3D法阵, 2=环绕激光

    @Override
    public void updateUnit() {
        if (unit == null || unit.dead) return;
        updateTargeting();
        if (strongest == null) return;

        unit.lookAt(strongest);
        reload -= Time.delta;
        float targetHealth = (strongest instanceof mindustry.gen.Healthc h) ? h.health() : 1000f;

        // 状态机：如果正在释放大招，就老老实实放完
        if (currentState == 1) {
            magicAI.updateUnit();
            if (magicAI.ended) { currentState = 0; reload = 120f; }
            return;
        } else if (currentState == 2) {
            surroundAI.updateUnit();
            if (surroundAI.ended) { currentState = 0; reload = 120f; }
            return;
        }

        // 正常状态：决定用什么技能
        if (targetHealth > 10000f) {
            if (reload <= 0f) {
                // 高血量敌人：50%放3D法阵，50%放环绕激光
                float rand = Mathf.random(1f);
                if (rand < 0.5f) {
                    currentState = 1;
                    magicAI.unit(unit);
                    magicAI.reset();
                } else {
                    currentState = 2;
                    surroundAI.unit(unit);
                    surroundAI.reset();
                }
                reload = 300f;
            }
        } else {
            // 低血量敌人：使用小激光
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
        if (strongest != null && (!strongest.isAdded() || (strongest instanceof mindustry.gen.Healthc h && h.dead()))) strongest = null;
        if (scanTime <= 0 || strongest == null) {
            strongest = null;
            final float[] maxScore = {0f};
            final Teamc[] bestTarget = {null};

            for (Unit u : mindustry.gen.Groups.unit) {
                if (u.team != unit.team && u.isValid() && unit.dst(u) < 1000f) {
                    float score = u.maxHealth / 100f - unit.dst(u) / 1000f;
                    if (score > maxScore[0]) { maxScore[0] = score; bestTarget[0] = u; }
                }
            }
            for (mindustry.game.Teams.TeamData data : mindustry.Vars.state.teams.present) {
                if (data.team != unit.team && data.turretTree != null) {
                    data.turretTree.intersect(unit.x - 1000f, unit.y - 1000f, 2000f, 2000f, b -> {
                        float score = b.maxHealth / 60f - unit.dst(b) / 1000f;
                        if (b instanceof TurretBuild) score += 500f; 
                        if (score > maxScore[0]) { maxScore[0] = score; bestTarget[0] = b; }
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
        currentState = 0;
        if (magicAI != null) magicAI.reset();
        if (surroundAI != null) surroundAI.reset();
    }
}
