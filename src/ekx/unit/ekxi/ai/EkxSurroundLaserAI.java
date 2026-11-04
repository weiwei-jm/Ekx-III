package ekx.unit.ekxi.ai;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import ekx.EkxPal;
import mindustry.entities.Damage;
import mindustry.gen.Teamc;
import mindustry.graphics.Layer;

public class EkxSurroundLaserAI extends AttackAI {
    static int[] main = {0x4FB8E6FF, 0x8FE7FFFF, 0xB8F2FFFF, 0xFFFFFFff, 0x8FE7FFFF, 0x2A7A99FF};
    static int[][] colors = {
            {0x2A7A9980, 0x4FB8E6FF, 0x8FE7FFFF, 0xFFFFFFFF, 0xB8F2FFFF, 0x6FD0F0FF, 0x2A7A9980},
            {0x2A7A9980, 0x4FB8E6FF, 0x8FE7FFFF, 0xFFFFFFFF, 0xB8F2FFFF, 0x6FD0F0FF, 0x2A7A9980},
            {0x2A7A9980, 0x4FB8E6FF, 0x8FE7FFFF, 0xFFFFFFFF, 0xB8F2FFFF, 0x6FD0F0FF, 0x2A7A9980},
            {0x2A7A9980, 0x4FB8E6FF, 0x8FE7FFFF, 0xFFFFFFFF, 0xB8F2FFFF, 0x6FD0F0FF, 0x2A7A9980},
            {0x2A7A9980, 0x4FB8E6FF, 0x8FE7FFFF, 0xFFFFFFFF, 0xB8F2FFFF, 0x6FD0F0FF, 0x2A7A9980},
            {0x2A7A9980, 0x4FB8E6FF, 0x8FE7FFFF, 0xFFFFFFFF, 0xB8F2FFFF, 0x6FD0F0FF, 0x2A7A9980},
            {0x2A7A9980, 0x4FB8E6FF, 0x8FE7FFFF, 0xFFFFFFFF, 0xB8F2FFFF, 0x6FD0F0FF, 0x2A7A9980},
            {0x2A7A9980, 0x4FB8E6FF, 0x8FE7FFFF, 0xFFFFFFFF, 0xB8F2FFFF, 0x6FD0F0FF, 0x2A7A9980}
    };
    static Color tmpColor = new Color();

    float time = 0f;
    float laserTime = 0f;
    boolean firing = false;

    @Override
    public void reset() {
        super.reset();
        time = 0f; laserTime = 0f; firing = false;
    }

    @Override
    public void updateWeapon(Teamc target) {
        time += Time.delta;

        if (time >= 90f && !firing) {
            firing = true;
            Sounds.beamHeal.at(unit.x, unit.y);
            mindustry.Vars.renderer.shake(15f, 30f);
        }

        if (firing) {
            float lfout = Mathf.clamp((360f - time) / 60f);
            if (lfout > 0f) {
                if (laserTime <= 0f) {
                    int a = colors.length;
                    for (int i = 0; i < a; i++) {
                        float ang1 = (360f / a) * i - 90f;
                        Damage.collideLine(null, unit.team, unit.x, unit.y, ang1, 800f, false, true, -1);
                    }
                    laserTime = 10f;
                }
                laserTime -= Time.delta;
            }
        }

        if (time >= 420f) ended = true;
    }

    float laserWidth(float dst) {
        float start = 250f, end = 700f, length = 3000f;
        return Interp.circleOut.apply(Mathf.clamp(dst / start) * Mathf.clamp((length - dst) / end)) * 30f;
    }

    void drawLaser(float x, float y, float angle, float fade, int[] colorSet) {
        int len = colorSet.length;
        for (int i = 0; i < len; i++) {
            int col = colorSet[i];
            float f1 = (i / (float) len - 0.5f) * 2f, f2 = ((i + 1f) / len - 0.5f) * 2f;
            Draw.color(tmpColor.rgba8888(col));
            for (int j = 0; j < 24; j++) {
                float dst1 = Interp.pow2In.apply(j / 24f) * 250f;
                float dst2 = Interp.pow2In.apply((j + 1f) / 24f) * 250f;
                float wd1 = laserWidth(dst1) * fade, wd2 = laserWidth(dst2) * fade;
                Vec2 q1 = Tmp.v1.trns(angle, dst1, wd1 * f1).add(x, y);
                Vec2 q2 = Tmp.v2.trns(angle, dst2, wd2 * f1).add(x, y);
                Vec2 q3 = Tmp.v3.trns(angle, dst2, wd2 * f2).add(x, y);
                Vec2 q4 = Tmp.v4.trns(angle, dst1, wd1 * f2).add(x, y);
                Fill.quad(q1.x, q1.y, q2.x, q2.y, q3.x, q3.y, q4.x, q4.y);
            }
            Lines.stroke(30f * fade * (1 - Math.abs(f1)));
            Lines.lineAngle(x, y, angle, 3000f, false);
        }
    }

    public void draw() {
        int a = colors.length;
        float fin1 = Interp.pow2.apply(Mathf.clamp(time / 60f));
        float fin2 = Interp.pow2.apply(Mathf.clamp((time - 90f) / 40f)), fout2 = 1f - fin2;
        float fin2I = Interp.pow2Out.apply(fin2), fout2I = 1f - fin2I;
        float fin4 = Mathf.clamp((time - 130f) / 6f);
        float fout = 1f - Mathf.clamp((time - 160f) / 30f);
        float lfout = Mathf.clamp((420f - time) / 60f);

        if (fout > 0.0001f) {
            for (int i = 0; i < a; i++) {
                float deg = (360f / a);
                float ang1 = (deg * i * fin1) - 90f + 180f * (1 - fin1);
                Vec2 npos = Tmp.v5.trns(ang1, Mathf.lerp(150f, 80f, Interp.pow2.apply(Interp.pow3In.apply(fin2)))).scl(fin2I);
                for (int j = 0; j < 32; j++) {
                    float ang2A = (deg / 32f) * fin1 * j * fout2 + ang1, ang2B = (deg / 32f) * fin1 * (j + 1f) * fout2 + ang1;
                    float ang3A = (j / 32f - 0.5f) * 360f * fin2, ang3B = ((j + 1f) / 32f - 0.5f) * 360f * fin2;
                    Vec2 vA = Tmp.v1.trns(ang2A + ang3A, 150f * fout2I).add(npos).add(unit);
                    Vec2 vB = Tmp.v2.trns(ang2B + ang3B, 150f * fout2I).add(npos).add(unit);
                    float x1 = vA.x, y1 = vA.y, x2 = vB.x, y2 = vB.y;
                    vA.trns(ang2A + ang3A, 20f * fout);
                    vB.trns(ang2B + ang3B, 20f * fout);
                    for (int k = 0; k < main.length; k++) {
                        int col = main[(main.length - 1) - k];
                        float f1 = k / (float) main.length, f2 = (k + 1f) / main.length;
                        Draw.color(tmpColor.rgba8888(col));
                        Vec2 q1 = Tmp.v1.setZero().lerp(vA, f1).add(x1, y1);
                        Vec2 q2 = Tmp.v2.setZero().lerp(vA, f2).add(x1, y1);
                        Vec2 q3 = Tmp.v3.setZero().lerp(vB, f2).add(x2, y2);
                        Vec2 q4 = Tmp.v4.setZero().lerp(vB, f1).add(x2, y2);
                        Fill.quad(q1.x, q1.y, q2.x, q2.y, q3.x, q3.y, q4.x, q4.y);
                    }
                }
            }
        }
        if (fin4 > 0f) {
            Draw.z(Layer.bullet + 1f);
            for (int i = 0; i < a; i++) {
                drawLaser(unit.x, unit.y, ((360f / a) * i) - 90f, fin4 * lfout, colors[i]);
            }
        }
        Draw.reset();
    }
}
