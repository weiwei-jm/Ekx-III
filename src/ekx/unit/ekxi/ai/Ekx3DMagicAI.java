package ekx.unit.ekxi.ai;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
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
            // 终极大爆炸
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
            nearest
