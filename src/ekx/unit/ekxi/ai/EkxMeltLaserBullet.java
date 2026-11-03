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
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class EkxMeltLaserBullet extends BulletType {
    float width = 160f;
    float length = 800f;
    float end = 300f;
    Interp dwidth = Interp.circleOut;
    
    // 替换为你的蓝光配色
    Color[] colors = {EkxPal.empathy.cpy().a(0.4f), EkxPal.empathy, Color.white};
    float[] widths = {1.5f, 1f, 0.8f};

    TextureRegion hcircle;

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

        // 总持续时间 4.5 秒 (270 帧)。前 2.3 秒蓄力变粗，后 2.2 秒保持
        lifetime = 4.5f * 60f;

        despawnEffect = Fx.none;
        shootEffect = Fx.none;
        hitEffect = Fx.hitLaserBlast;
        status = StatusEffects.melting;
        statusDuration = 120f;
        damage = 5000f;
        drawSize = length * 2.1f;
        knockback = 60f;
        layer = (Layer.bullet + Layer.effect) / 2;
    }

    @Override
    public void load() {
        hcircle = Core.atlas.find("hcircle");
    }

    float getLaserWidth(float dst) {
        return dwidth.apply(Mathf.clamp(dst / end));
    }

    float timeWidth(Bullet b) {
        float w1 = 5f;
        // 前 140 帧变大，最后 80 帧变细消失
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
        Tmp.v1.trns(b.rotation(), 40f).add(b.x, b.y);
        // 简单的开火震屏
        Effect.shake(10f, 10f, b.x, b.y);
        Sounds.laserblast.at(b.x, b.y, 2f, 1.5f);
    }

    @Override
    public void update(Bullet b) {
        if (b.timer(0, 5f)) {
            Vec2 v = Tmp.v1.trns(b.rotation(), length).add(b);
            float vx = v.x;
            float vy = v.y;
            float dw = timeWidth(b);

            // 用原版最暴力的线段判定
            Damage.collideLine(b, b.team, b.x, b.y, b.rotation(), length, false, true, -1);
            
            // 额外处理击退和特效
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
        Vec2 v = Tmp.v1.trns(b.rotation(), end).add(b.x, b.y);
        Vec2 v2 = Tmp.v2.trns(b.rotation(), length - end).add(v.x, v.y);
        float w = timeWidth(b);

        for (int i = 0; i < 3; i++) {
            float dw = w * widths[i] * (1 + Mathf.absin(8f, 0.1f));

            Draw.color(colors[i]);
            Lines.stroke(dw);
            // 画激光柱
            Draw.rect(hcircle, v.x, v.y, end * 2, Lines.getStroke(), b.rotation() + 180f);
            Lines.lineAngle(v.x, v.y, b.rotation(),
            length - end, false);
            Drawf.tri(v2.x, v2.y, Lines.getStroke(), width * 2 + dw, b.rotation());
        }
        
        if (b.time > 140f) {
            float scl = Interp.pow2Out.apply(Mathf.clamp((b.time - 140f) / 5)) * Interp.pow3In.apply(Mathf.clamp((lifetime - b.time) / 80f));
            Rand r = Utils.rand, r2 = Utils.rand2;
            r.setSeed(b.id + 1236);

            for (int i = 0; i < 75; i++) {
                float d = r.random(20f, 40f);
                float time = (Time.time + r.random(d));
                float dtime = (time % d) / d;
                float slope = Interp.pow2Out.apply(Mathf.curve(dtime, 0f, 0.05f) * Mathf.curve(1 - dtime, 0f, 0.05f));

                int seed = (int) (time / d) + r.nextInt();
                r2.setSeed(seed);

                float dh = r2.random(120f, 220f);
                float dw = dh * r2.random(0.05f, 0.15f) * scl * slope;
                float rx = r2.range(Math.max(w - dw, 0f)) / 2f;
                float arx = Math.abs(rx);
                float xp = rx * getLaserWidth(dtime * (length - dh) + dh);
                float yp = dtime * ((length - (end + this.width / 2)) + (w - arx) * 4.5f) + dh;
                Color rc = r2.chance(0.1f + Interp.pow3In.apply((w - arx) / w) * 0.7f) ? Color.white : EkxPal.empathy;

                Draw.color(rc);
                v.trns(b.rotation(), (yp + end / 1.5f) - (w - arx) * 1.5f, xp).add(b.x, b.y);
                
                Fill.circle(v.x, v.y, dw / 2f);
            }
        }
        Drawf.light(b.x, b.y, v2.x, v2.y, w * 2 + 20f, colors[0], 0.75f);
        Draw.reset();
    }
}
