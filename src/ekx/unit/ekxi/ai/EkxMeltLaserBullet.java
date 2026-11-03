package ekx.unit.ekxi.ai;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import ekx.EkxPal;
import mindustry.content.*;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.bullet.*;
import mindustry.entities.units.WeaponMount; // 修复
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild; // 修复

public class EkxMeltLaserBullet extends BulletType {
    float width = 160f;
    float length = 10000f;
    float end = 300f;
    Interp dwidth = Interp.circleOut;
    
    Color[] colors = {EkxPal.empathy.cpy().a(0.4f), EkxPal.empathy, Color.white};
    float[] widths = {1.5f, 1f, 0.8f};

    public EkxMeltLaserBullet() {
        speed = 0f;
        collides = collidesTiles = false;
        absorbable = false;
        hittable = false;
        keepVelocity = false;

        pierce = true;
        pierceArmor = true;
        pierceCap = -1;
        impact = true;

        lifetime = 4.5f * 60f;
        despawnEffect = Fx.none;
        shootEffect = Fx.none;
        hitEffect = Fx.hitLaserBlast;
        status = StatusEffects.melting;
        statusDuration = 120f;
        damage = 9000f;
        drawSize = length * 2.1f;
        knockback = 60f;
        layer = (Layer.bullet + Layer.effect) / 2;
    }

    float getLaserWidth(float dst) {
        return dwidth.apply(Mathf.clamp(dst / end));
    }

    float timeWidth(Bullet b) {
        float w1 = 5f;
        return (w1 * Mathf.clamp(b.time / 60f) + (width - w1) * Interp.pow2Out.apply(Mathf.clamp((b.time - 140f) / 5))) * Interp.pow3In.apply(Mathf.clamp((lifetime - b.time) / 80f));
    }

    void handleDamage(Healthc e, Bullet b) {
        if (e instanceof Unit u) {
            float sss = Interp.pow2In.apply(Mathf.clamp(1 - b.dst(u) / length)) * length;
            Tmp.v3.set(u).sub(b).nor().scl((knockback + sss / (2 * (1.3157894f))) * 80f);
            if (impact) Tmp.v3.setAngle(b.rotation() + (knockback < 0 ? 180f : 0f));

            if (b.time >= 140f) {
                u.impulse(Tmp.v3);
                u.vel.limit(Mathf.pow(24f, 1 + u.drag));
            }

            u.clearStatuses();
            u.apply(StatusEffects.disarmed, 90f);
            u.apply(StatusEffects.melting, 60f);

            for (WeaponMount m : u.mounts) {
                m.reload = m.weapon.reload;
            }
        }
        if (e instanceof Building bl) {
            bl.applyHealSuppression(120f);
            bl.applySlowdown(0.1f, 120f);
            if (e instanceof TurretBuild tr) {
                tr.reloadCounter = Math.max(0, tr.reloadCounter - 4f * Time.delta);
            }
        }

        if (b.time >= 140f) {
            float tscl = Mathf.clamp((lifetime - b.time) / 80f);
            float dam = Math.max(Math.max(b.damage, damage), (e.maxHealth() * 10f) / 50f) * tscl;
            e.damagePierce(dam);
        }
    }

    @Override
    public void init(Bullet b) {
        Sounds.beamHeal.at(b.x, b.y); // 去掉变调参数，原音播放
        Effect.shake(10f, 10f, b.x, b.y);
    }

    @Override
    public void update(Bullet b) {
        if (b.time > 140f && b.time - Time.delta <= 140f) {            Sounds.beamHeal.at(b.x, b.y); // 同样原音播放
        Effect.shake(20f, 20f, b.x, b.y);

            float dw = timeWidth(b);

            Damage.collideLine(b, b.team, b.x, b.y, b.rotation(), length, false, true, -1);
            
            for (mindustry.game.Teams.TeamData data : mindustry.Vars.state.teams.present) {
                if (data.team != b.team) {
                    if (data.unitTree != null) {
                        data.unitTree.intersect(Tmp.r1.setCentered(b.x, b.y, length * 2), u -> {
                            if (u.within(b.x, b.y, length + u.hitSize / 2f)) {
                                Vec2 nearest = Intersector.nearestSegmentPoint(b.x, b.y, vx, vy, u.x, u.y, Tmp.v2);
                                if (u.dst(nearest) <= dw + u.hitSize / 2f) {
                                    handleDamage(u, b);
                                }
                            }
                        });
                    }
                }
            }
        }
    }

@Override
public void draw(Bullet b) {
    float w = timeWidth(b);
    
    // 1. 脚下画法阵
    drawMagicCircle(b.x, b.y, w * 0.6f + 30f, Time.time);

    // 2. 绘制激光
    Vec2 v = Tmp.v1.trns(b.rotation(), end).add(b.x, b.y);
    Vec2 v2 = Tmp.v2.trns(b.rotation(), length - end).add(v.x, v.y);

    for (int i = 0; i < 3; i++) {
        float dw = w * widths[i] * (1 + Mathf.absin(8f, 0.1f));
        Draw.color(colors[i]);
        Lines.stroke(dw);
        
        // 这里加了 Fill.circle，让起点有一个光晕，从法阵中心喷射
        Fill.circle(b.x, b.y, Lines.getStroke() / 2f); 
        Lines.lineAngle(b.x, b.y, b.rotation(), length, false);
        
        Drawf.tri(v2.x, v2.y, Lines.getStroke(), width * 2 + dw, b.rotation());
    }
        }
        Drawf.light(b.x, b.y, v2.x, v2.y, w * 2 + 20f, colors[0], 0.75f);
        Draw.reset();
    }
                }
