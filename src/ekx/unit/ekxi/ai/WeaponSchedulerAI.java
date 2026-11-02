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

    // 全局唯一的武器实例（只创建一次，避免内存泄漏）
    private final AttackAI[] allWeapons = {
        new BasicLaserAI(),
        new EndAttackAI()
    };

    // 当前激活的武器
    private AttackAI activeWeapon = null;

    public WeaponSchedulerAI() {
        // 确保所有武器在生成时绑定好宿主
        for (AttackAI w : allWeapons) {
            w.unit(this.unit);
        }
    }

    @Override
    public void updateUnit() {
        if (unit == null || unit.dead) return;

        // 1. 找目标
        updateTargeting();

        if (strongest == null) {
            // 没目标，停火
            return;
        }

        // 朝向目标
        unit.lookAt(strongest);

        // 2. 判断目标血量，决定切换哪个武器
        float targetHealth = (strongest instanceof mindustry.gen.Healthc h) ? h.health() : 1000f;
        AttackAI targetWeapon;

        if (targetHealth > 10000f) {
            targetWeapon = allWeapons[1]; // 终极攻击
        } else {
            targetWeapon = allWeapons[0]; // 普通激光
        }

        // 3. 如果武器需要切换，执行切换流程
        if (activeWeapon != targetWeapon) {
            if (activeWeapon != null) {
                activeWeapon.reset(); // 结束上一个武器
            }
            activeWeapon = targetWeapon;
            activeWeapon.unit(unit); // 绑定单位
            activeWeapon.reset();    // 启动新武器
        }

        // 4. 移动逻辑
        float dst = unit.dst(strongest);
        if (dst > 600f) {
            Vec2 v = Tmp.v1.trns(unit.angleTo(strongest), unit.type.speed);
            unit.movePref(v);
        }

        // 5. 执行当前选中的武器逻辑
        if (activeWeapon != null) {
            activeWeapon.updateUnit();
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
        if (activeWeapon != null) {
            activeWeapon.reset();
            activeWeapon = null;
        }
    }
}
