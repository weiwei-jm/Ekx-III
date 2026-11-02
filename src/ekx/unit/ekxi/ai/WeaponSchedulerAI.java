package ekx.unit.ekxi.ai;

import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;

public class WeaponSchedulerAI extends AttackAI {
    private Teamc strongest;
    private float scanTime = 0f;

    // 实例化我们的两个武器
    private final BasicLaserAI laserAI = new BasicLaserAI();
    private final EndAttackAI endAI = new EndAttackAI();

    @Override
    public void updateUnit() {
        if (unit == null || unit.dead) return;

        // 寻找目标
        updateTargeting();

        // 如果没目标，就原地悬停待命
        if (strongest == null) {
            return;
        }

        // 朝向目标
        unit.lookAt(strongest);

        // 核心机制：根据目标血量选择武器
        float targetHealth = (strongest instanceof mindustry.gen.Healthc h) ? h.health() : 1000f;
        
        AttackAI activeAI;

        if (targetHealth > 10000f) {
            // 如果敌人血量超过 1万，使用终极攻击
            activeAI = endAI;
            // 如果刚切到终极攻击，重置一下状态
            if (laserAI.unit() == unit) {
                laserAI.reset();
                endAI.unit(unit);
                endAI.reset();
            }
        } else {
            // 如果敌人血量较少，使用普通激光
            activeAI = laserAI;
            // 如果刚从终极切回普通，也重置一下
            if (endAI.unit() == unit) {
                endAI.reset();
                laserAI.unit(unit);
                laserAI.reset();
            }
        }

        // 确保 AI 绑定了当前单位
        if (activeAI.unit() != unit) {
            activeAI.unit(unit);
            activeAI.reset();
        }

        // 如果目标太远，就飞过去
        float dst = unit.dst(strongest);
        if (dst > 600f) {
            Vec2 v = Tmp.v1.trns(unit.angleTo(strongest), unit.type.speed);
            unit.movePref(v);
        }

        // 执行当前选中的武器逻辑
        activeAI.updateUnit();
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

            // 扫描周围 1000 范围内的敌人
            for (Unit u : mindustry.gen.Groups.unit) {
                if (u.team != unit.team && u.isValid() && unit.dst(u) < 1000f) {
                    float score = u.maxHealth / 100f - unit.dst(u) / 1000f;
                    if (score > maxScore[0]) {
                        maxScore[0] = score;
                        bestTarget[0] = u;
                    }
                }
            }
            
            // 扫描炮塔
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
    @Override public void reset() {}
}
