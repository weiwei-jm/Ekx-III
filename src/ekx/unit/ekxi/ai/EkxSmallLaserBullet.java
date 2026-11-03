package ekx.unit.ekxi.ai;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import ekx.EkxPal; // 修正了这里
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class EkxSmallLaserBullet extends BulletType {
    float length = 600f;
    float width = 8f;

    Color[] colors = {EkxPal.empathy.cpy().a(0.5f), EkxPal.empathy, EkxPal.empathyAdd, Color.white}; // 修正了这里
    float[] widths = {1.65f, 1f, 0.66f, 0.5f};

    public EkxSmallLaserBullet() {
        speed = 0f;
        collides = collidesTiles = false;
        absorbable = false;
        hittable = false;
        keepVelocity = false;
        pierce = true;
        pierceArmor = true;
        pierceCap = -1;
        impact = true;
        lifetime = 20f; 
        despawnEffect = Fx.none;
        shootEffect = Fx.none;
        hitEffect = Fx.hitLaserBlast;
        status = StatusEffects.disarmed;
        statusDuration = 60f;
        damage = 2000f;
        drawSize = 600f;
    }

    @Override
    public void update(Bullet b) {
        if (b.timer(0, 5f)) {
            mindustry.entities.Damage.collideLaser(b, length, false, true, -1);
        }
    }

    private void drawMagicCircle(float x, float y, float rotZ, float radius, float time) {
        Draw.z(Layer.flyingUnit - 1f);
        Draw.color(EkxPal.empathyAdd, 0.8f); // 修正了这里
        Lines.stroke(2f);
        Lines.beginLine();
        for (int i = 0; i <= 24; i++) {
            float ang = i * 15f + time * 2f;
            Lines.linePoint(x + Mathf.cosDeg(ang) * radius, y + Mathf.sinDeg(ang) * radius * 0.4f);
        }
        Lines.endLine();

        Draw.color(Color.white, 0.9f);
        Lines.stroke(2.5f);
        Lines.beginLine();
        for (int i = 0; i <= 5; i++) {
            float ang = rotZ + (i * 144f) + time * 3f; 
            float px = x + Mathf.cosDeg(ang) * radius * 0.8f;
            float py = y + Mathf.sinDeg(ang) * radius * 0.8f * 0.4f;
            Lines.linePoint(px, py);
        }
        Lines.endLine();
    }

    @Override
    public void draw(Bullet b) {
        drawMagicCircle(b.x, b.y, b.rotation(), 45f, Time.time + b.id);
        float fc = Mathf.clamp(b.time / 4f);
        float mwidth = Mathf.clamp((b.lifetime - b.time) / 16f) * width * 1.25f;
        float l = fc * length;
        Vec2 v = Tmp.v1.trns(b.rotation(), l).add(b);
        float sin = (Mathf.absin(5f, 0.1f) + 1f);

        Draw.z(Layer.bullet + 1f);
        for (int i = 0; i < 4; i++) {
            Draw.color(colors[i]);
            Lines.stroke(widths[i] * mwidth * sin);
            Lines.line(b.x, b.y, v.x, v.y, false);
            Drawf.tri(v.x, v.y, Lines.getStroke(), mwidth * 3 + width / 1.5f, b.rotation());
            Drawf.tri(v.x, v.y, Lines.getStroke(), mwidth * 2 + width / 2f, b.rotation() + 180f);

            for (int s : Mathf.signs) {
                Drawf.tri(b.x, b.y, Lines.getStroke() / 2f, (mwidth * widths[i] * 2 + 120f) * sin * fc, b.rotation() + 90f * s);
            }
            Fill.circle(b.x, b.y, mwidth * 0.7f * widths[i] * sin);
        }
        Draw.reset();
        v = Tmp.v1.trns(b.rotation(), l + width / 2f).add(b);
        Drawf.light(b.x, b.y, v.x, v.y, mwidth * 2.75f, colors[0], 0.6f);
    }
}
