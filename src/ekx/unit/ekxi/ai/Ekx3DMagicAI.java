package ekx.unit.ekxi.ai;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.struct.FloatSeq;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import ekx.ExkPal;
import mindustry.content.Fx;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;

public class Ekx3DMagicAI extends AttackAI {
    static Vec2[] ringPos = {new Vec2(44.25f, 18f), new Vec2(30.5f, 0f), new Vec2(19.5f, 7.5f)};
    static float[] ringMoveSpeed = {2f, 4f, 8f}, ringReloadTimes = {12f, 8f, 5f};
    static float[][] rotations3D = {
            {0.0625f, 1f, 0.125f},
            {0f, 0.25f, 1f},
            {1f, 1f, 0.1f}
    };
    
    float time = 0f;
    float[] ringRotations = new float[3], ringRotations3D = new float[3 * 3];
    float[] ringReloads = new float[3 * 8];
    float[] eyeLasers = new float[3 * 8 * 3];
    int[] ringIdxs = new int[3];

    Seq<Teamc> nearests = new Seq<>(true, 3 * 8 + 1);
    FloatSeq nearestScore = new FloatSeq(true, 3 * 8 + 1);
    float targetingUpdate = 0f;

    @Override
    public void reset() {
        super.reset();
        time = 0f;
        java.util.Arrays.fill(ringRotations, 0f);
        java.util.Arrays.fill(ringRotations3D, 0f);
        java.util.Arrays.fill(ringReloads, 0f);
    }

@Override
public void updateWeapon(Teamc target) {
    updateTargeting();

    time += Time.delta;
    float duration = 15f * 60;

    if (time < duration) {
        float speed = Mathf.pow(Mathf.clamp(time / 700f) * 3f, 0.5f);
        float speed2 = Mathf.pow(Mathf.clamp(time / duration), 0.5f);
        float out = 1f - Interp.pow2.apply(Mathf.clamp((time - 10f * 60f) / (4.5f * 60)));

        for (int i = 0; i < 3; i++) {
            ringRotations[i] += ringMoveSpeed[i] * speed * Time.delta * 0.5f * out;
            if (time < 12 * 60f) {
                for (int j = 0; j < 3; j++) {
                    int k = i * 3 + j;
                    ringRotations3D[k] += speed2 * Time.delta * 2f * (rotations3D[i][j]);
                    ringRotations3D[k] %= 360f;
                }
            } else {
                float f = Mathf.clamp((time - 12 * 60f) / (3f * 60));
                for (int j = 0; j < 3; j++) {
                    int k = i * 3 + j;
                    float rs = rotations3D[i][j];
                    if (ringRotations3D[k] > 0f) {
                        float anr = 45f;
                        float rr = (1f - Mathf.clamp((ringRotations3D[k] - (360 - anr)) / anr)) * 0.99f + 0.01f;
                        ringRotations3D[k] += speed2 * Time.delta * 2f * (rs != 0f ? Mathf.lerp(rs, Mathf.sqrt(1f / rs), f) : 0f) * rr;
                        if (ringRotations3D[k] >= 360f) {
                            ringRotations3D[k] = 0f;
                        }
                    }
                }
            }
        }
    } else if (time >= duration) {
        ended = true;
        for (Unit u : Groups.unit) {
            if (u.team != unit.team && unit.within(u, 500f)) {
                u.damagePierce(u.maxHealth + 1000f);
                Fx.shockwave.at(u.x, u.y);
            }
        }
        Fx.shockwave.at(unit.x, unit.y);
        return;
    }

    updateRings();
}

void updateRings() {
    float duration = 15f * 60;

    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 8; j++) {
            int idx = (i * 8 + j);
            int idx2 = idx * 3;
            eyeLasers[idx2 + 2] -= Time.delta;
        }
    }
    if (!nearests.isEmpty() && time < (duration - 30f)) {
        for (int i = 0; i < 3; i++) {
            if (ringReloads[i] <= 0f) {
                int j = ringIdxs[i];
                int idx = (i * 8 + j);
                int idx2 = idx * 3;
                Teamc tar = nearests.get(idx % nearests.size);

                if (tar != null) {
                    if (tar instanceof mindustry.gen.Healthc h) {
                        h.damagePierce(10000f);
                    }
                    eyeLasers[idx2] = tar.x();
                    eyeLasers[idx2 + 1] = tar.y();
                    eyeLasers[idx2 + 2] = 30f;
                }
                ringReloads[i] = ringReloadTimes[i];
                ringIdxs[i] = Mathf.mod(ringIdxs[i] + ((i % 2) == 0 ? 1 : -1), 8);
            }
        }
    }
    for (int i = 0; i < 3; i++) {
        ringReloads[i] -= Time.delta;
    }
}

void updateTargeting() {
    if ((targetingUpdate -= Time.delta) <= 0f) {
        nearests.clear();
        nearestScore.clear();
        for (Unit u : Groups.unit) {
            if (u.team != unit.team) {
                float dst = u.dst(unit);
                if (nearestScore.isEmpty()) {
                    nearests.add(u);
                    nearestScore.add(dst);
                } else {
                    for (int i = nearests.size - 1; i >= 0; i--) {
                        float sdst = nearestScore.items[i];
                        if (dst <= sdst) {
                            nearests.insert(i + 1, u);
                            nearestScore.insert(i + 1, dst);
                            if (nearestScore.size > (3 * 8)) {
                                nearests.remove(0);
                                nearestScore.removeIndex(0);
                            }
                            break;
                        }
                    }
                }
            }
        }
        targetingUpdate = 5f;
    }
                        }

private void drawCircle3D(float x, float y, float rx, float ry, float rz, float radius, float rotation, int sides) {
        Lines.beginLine();
        for (int i = 0; i <= sides; i++) {
            float ang = i * (360f / sides) + rotation;
            Vec3 v3 = Tmp.v31.set(Mathf.cosDeg(ang) * radius, Mathf.sinDeg(ang) * radius, 0f)
                    .rotate(Vec3.Y, ry).rotate(Vec3.X, rx).rotate(Vec3.Z, rz);
            float sz1 = 700f / (700f - v3.z);
            Lines.linePoint(x + v3.x * sz1, y + v3.y * sz1);
        }
        Lines.endLine();
    }

    @Override
    public void draw() {
        Color darkC = Tmp.c1.set(ExkPal.empathyDark).lerp(ExkPal.empathy, Mathf.absin(2f, 1f));
        Color col1 = Tmp.c2.set(ExkPal.empathy).lerp(Color.white, Mathf.absin(2f, 1f));

        float fin = Mathf.clamp(time / 40f);
        Draw.color(darkC, fin * 0.5f);
        
        for (int i = 0; i < 3; i++) {
            Draw.color(darkC, fin);
            int k = i * 3;
            drawCircle3D(unit.x, unit.y, ringRotations3D[k], ringRotations3D[k + 1], ringRotations3D[k + 2], 80f + i * 40f, ringRotations[i], 64);
            
            Draw.color(Color.white);
            for (int j = 0; j < 8; j++) {
                Vec2 p = eyePos(i, j).scl(2f);
                Tmp.v2.set(p).setLength(120f);
                float ll = p.len() / Tmp.v2.len();
                
                Vec3 v3 = Tmp.v31.set(Tmp.v2.x, Tmp.v2.y, 0f).rotate(Vec3.Y, ringRotations3D[k + 1]).rotate(Vec3.X, ringRotations3D[k]).rotate(Vec3.Z, ringRotations3D[k + 2]);
                float sz1 = 700f / (700f - v3.z);
                v3.x *= sz1;
                v3.y *= sz1;

                Fill.circle(v3.x * ll + unit.x, v3.y * ll + unit.y, 2f * fin);
            }
        }

        float z = Draw.z();
        Draw.z(Layer.flyingUnit + 1f);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                int idx = (i * 8 + j) * 3;
                float x = eyeLasers[idx];
                float y = eyeLasers[idx + 1];
                float time = eyeLasers[idx + 2];
                if (time <= 0.0001f) continue;

                Vec2 p = eyePos3D(i, j, 120f);
                float f = time / 30f;
                float s = 5.25f * f;

                Draw.z(z);
                Draw.color(col1);
                Lines.stroke(s);
                Lines.line(p.x, p.y, x, y, false);
                Fill.circle(p.x, p.y, s);
                Fill.circle(x, y, s);
                Draw.z(z + 0.01f);
                Draw.color(Color.white);
                Lines.stroke(s * 0.5f);
                Lines.line(p.x, p.y, x, y, false);
                Fill.circle(p.x, p.y, s * 0.5f);
                Fill.circle(x, y, s * 0.5f);
            }
        }
        Draw.z(z);
        Draw.color();
    }

    Vec2 eyePos3D(int ring, int idx, float size) {
        Vec2 p = eyePos(ring, idx).scl(2f);
        Tmp.v2.set(p).setLength(size);
        float ll = p.len() / Tmp.v2.len();
        int k = ring * 3;

        Vec3 v3 = Tmp.v31.set(Tmp.v2.x, Tmp.v2.y, 0f).rotate(Vec3.Y, ringRotations3D[k + 1]).rotate(Vec3.X, ringRotations3D[k]).rotate(Vec3.Z, ringRotations3D[k + 2]);
        float sz1 = 700f / (700f - v3.z);
        v3.x *= sz1;
        v3.y *= sz1;

        return Tmp.v3.set(v3.x * ll + unit.x, v3.y * ll + unit.y);
    }

    Vec2 eyePos(int ring, int idx) {
        if (ringPos[ring].y == 0) {
            return Tmp.v1.trns(ringRotations[ring] + idx * 45f, ringPos[ring].x);
        }
        int rot = (idx / 2);
        float side = Mathf.signs[idx % 2] * ringPos[ring].y;
        return Tmp.v1.trns(ringRotations[ring] + rot * 90f, ringPos[ring].x, side);
    }
}
