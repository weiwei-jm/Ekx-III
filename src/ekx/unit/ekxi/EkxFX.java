package ekx.unit.ekxi;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Interp;
import arc.math.Mathf;
import mindustry.entities.Effect;
import mindustry.graphics.Layer;

public class EkxFX {
    public static final Color customColor = Color.valueOf("6C9FFFFF");

    public static Effect endFlash = new Effect(15f, e -> {
        float f = Interp.pow2In.apply(Mathf.curve(e.fin(), 0f, 0.1f));
        float fo = Mathf.curve(e.fout(), 0.4f, 1f);
        float f2 = Interp.pow2Out.apply(Mathf.curve(e.fin(), 0.1f, 0.75f));
        float scl = e.rotation;

        Draw.color(customColor);
        for (int i = 0; i < 4; i++) {
            float r = i * 90f;
            Drawf.tri(e.x, e.y, 5f * fo * scl, (5f + 120f * f) * fo * scl, r);
        }
        for (int i = 0; i < 2; i++) {
            float r = i * 180f;
            Drawf.tri(e.x, e.y, 7f * e.fout() * scl, (7f + 310f * f2) * scl, r);
        }
    }).layer(Layer.flyingUnit + 0.1f);

    public static Effect endDeath = new Effect(50f, 1000f, e -> {
        float fin1 = Mathf.curve(e.fin(), 0f, 0.65f);
        float size = e.rotation;
        arc.math.Rand r = new arc.math.Rand(e.id);
        r.setSeed(e.id);
        e.lifetime = 50f + r.range(4f);
        int base = (int)((size * size) / 34f) + 2;
        int base2 = (int)((size * size) / 16f) + 4;

        Draw.color(Color.valueOf("500000"), customColor, Mathf.curve(Interp.pow2Out.apply(fin1), 0f, 0.5f));
        for (int i = 0; i < base; i++) {
            arc.math.geom.Vec2 v = arc.util.Tmp.v1.trns(r.random(360f), Mathf.sqrt(r.nextFloat()) * size + ((20f + size * 4f) * Interp.pow2Out.apply(fin1) * r.nextFloat()));
            float s = r.random(0.5f, 1.1f) * (size * 0.4f + 8f) * (1f - fin1);
            if (fin1 < 1f) Fill.circle(v.x + e.x, v.y + e.y, s);
        }
        Draw.color(Color.valueOf("500000"), customColor, Mathf.curve(Interp.pow2Out.apply(e.fin()), 0f, 0.5f));
        for (int i = 0; i < base2; i++) {
            float sin = Mathf.sin(r.random(7f, 11f), r.random(size * 2f)) * e.fin();
            arc.math.geom.Vec2 v = arc.util.Tmp.v1.trns(r.random(360f), Mathf.sqrt(r.nextFloat()) * size + ((40f + size * 8f) * Interp.pow2In.apply(e.fin()) * r.nextFloat()), sin);
            float s = r.random(0.5f, 1.1f) * (size * 0.25f + 3f) * (1f - Interp.pow4In.apply(e.fin()));
            Fill.circle(v.x + e.x, v.y + e.y, s);
        }
    });

    public static Effect endSplash = new Effect(35f, 800f, e -> {
        arc.math.Rand r = new arc.math.Rand(e.id);
        r.setSeed(e.id);
        e.lifetime = 50f + r.range(16f);
        Draw.color(Color.valueOf("500000"));
        int am = r.random(5, 9);
        for (int i = 0; i < am; i++) {
            float of = 0.3f / (am - 1f);
            float c = Mathf.curve(e.fin(), of * i, (1 - 0.3f) + (of * i));
            float ang = r.range(40f) + e.rotation;
            float scl = r.random(0.6f, 1.4f) * 200f;
            float len = r.random(350f, 900f);
            if (c > 0.0001f && c < 0.9999f) {
                arc.math.geom.Vec2 v = arc.util.Tmp.v1.trns(ang, len * Interp.pow2Out.apply(c)).add(e.x, e.y);
                Drawf.tri(v.x, v.y, scl * 0.22f * (1f - Interp.pow3In.apply(c)), scl * Interp.pow3Out.apply(Mathf.curve(c, 0f, 0.5f)) + scl * 0.5f, ang);
            }
        }
    }).layer(Layer.darkness + 1f);
        }
