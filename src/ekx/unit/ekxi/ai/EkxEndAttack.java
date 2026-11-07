package ekx.unit.ekxi.ai;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.struct.FloatSeq;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import ekx.unit.ekxi.EkxGraphicUtils;
import ekx.unit.ekxi.EkxIUnit;
import ekx.unit.ekxi.EkxRegions;
import mindustry.Vars;
import mindustry.entities.Units;
import mindustry.game.Teams.TeamData;
import mindustry.gen.Building;
import mindustry.gen.Healthc;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

public class EkxEndAttack extends AttackAI {
    static Vec2[] ringPos = {new Vec2(44.25f, 18f), new Vec2(30.5f, 0f), new Vec2(19.5f, 7.5f)};
    static float[] ringMoveSpeed = {2f, 4f, 8f}, ringReloadTimes = {12f, 8f, 5f};
    static float[][] rotations3D = {{0.0625f, 1f, 0.125f}, {0f, 0.25f, 1f}, {1f, 1f, 0.1f}};
    static Seq<Building> tmpBuildings = new Seq<>();

    public static final Color customColor = Color.valueOf("6C9FFFFF");
    public static final Sound laserSound = mindustry.gen.Sounds.beamPlasma;

    float[] ringRotations = new float[3], ringRotations3D = new float[9];
    float[] ringReloads = new float[24];
    float[] eyeLasers = new float[72];
    int[] ringIdxs = new int[3];

    Seq<Healthc> nearests = new Seq<>(true, 25);
    FloatSeq nearestScore = new FloatSeq(true, 25);
    FloatSeq crosses = new FloatSeq();
    float targetingUpdate = 0f;
    boolean flash = false;
    boolean ending = false;
    float endTime = 0f, endScanTimer = 0f;

    @Override
    public void reset() {
        super.reset();
        java.util.Arrays.fill(ringRotations, 0f);
        java.util.Arrays.fill(ringRotations3D, 0f);
        java.util.Arrays.fill(ringReloads, 0f);
        flash = false; ending = false; endTime = 0f;
    }

    @Override
    public void updateWeapon(Teamc target) {
        if (unit instanceof EkxIUnit execUnit) {
            if (!execUnit.isReviving) execUnit.castingSkill = true; 
        }

        updateTargeting();
        time += Time.delta;
        float duration = 15f * 60;

        if (time < duration) {
            float speed = Mathf.pow(Mathf.clamp(time / 700f) * 3f, 0.5f);
            float out = 1f - Interp.pow2.apply(Mathf.clamp((time - 10f * 60f) / (4.5f * 60)));
            for (int i = 0; i < 3; i++) {
                ringRotations[i] += ringMoveSpeed[i] * speed * Time.delta * 0.5f * out;
                if (time < 12 * 60f) {
                    for (int j = 0; j < 3; j++) {
                        int k = i * 3 + j;
                        ringRotations3D[k] += speed * Time.delta * 2f * (rotations3D[i][j]);
                        ringRotations3D[k] %= 360f;
                    }
                }
            }
        } else if (!flash) {
            flash = true;
        }

        if (time > duration + 20f && !ending) {
            ending = true;
            crosses.clear();
            int size = Math.min((int) ((Vars.world.width() * Vars.world.height()) / ((200 * 200) / 8f)), 1000);
            for (int i = 0; i < size; i++) {
                float cx = Mathf.random(Vars.world.unitWidth());
                float cy = Mathf.random(Vars.world.unitHeight());
                float cr = Mathf.random(360);
                float cs = Mathf.random(1.7f, 4.25f);
                crosses.add(cx, cy, cr, cs, 0f);
            }
        }
        if (ending) updateEnd();
        updateRings();

        if (time >= duration + 20f + 3 * 60f) {
            ended = true;
            if (unit instanceof EkxIUnit execUnit) execUnit.castingSkill = false;
        }
    }

    void updateRings() {
        float duration = 15f * 60;
        for (int i = 0; i < 72; i += 3) eyeLasers[i + 2] -= Time.delta;

        if (!nearests.isEmpty() && time < (duration - 30f)) {
            for (int i = 0; i < 3; i++) {
                if (ringReloads[i] <= 0f) {
                    int j = ringIdxs[i];
                    int idx = (i * 8 + j);
                    int idx2 = idx * 3;
                    Healthc tar = nearests.get(idx % nearests.size);

                    if (tar != null) {
                        laserSound.at(tar.x(), tar.y(), 1f);
                        if (tar instanceof Unit u) {
                            float d = Mathf.pow(u.health / 100000000f, 2) * u.maxHealth;
                            u.damage(10000f + d);
                        } else if (tar instanceof Building b) {
                            float d = Mathf.pow(b.health / 100000000f, 2) * b.maxHealth;
                            b.damage(10000f + d);
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
        for (int i = 0; i < 3; i++) ringReloads[i] -= Time.delta;
    }

    void updateTargeting() {
        if ((targetingUpdate -= Time.delta) <= 0f) {
            nearests.clear(); nearestScore.clear();
            for (TeamData data : Vars.state.teams.present) {
                if (data.team != unit.team) {
                    for (Unit u : data.units) { nearests.add(u); nearestScore.add(u.dst(unit)); }
                    for (Building b : data.buildings) { nearests.add(b); nearestScore.add(b.dst(unit)); }
                }
            }
            if (nearestScore.size > 24) { nearests.setSize(24); nearestScore.setSize(24); }
            targetingUpdate = 5f;
        }
    }

    void updateEnd() {
        endTime += Time.delta;
        float size = Mathf.pow(Mathf.clamp(endTime / (1.75f * 60f)), 5) * 10000f;

        if (endScanTimer <= 0f && endTime < (1.75f * 60f)) {
            Units.nearby(unit.x - size, unit.y - size, size * 2, size * 2, u -> { 
                if (u.team != unit.team && u.within(unit.x, unit.y, size)) {
                    u.damage(u.maxHealth + 1000f); 
                }
            });
            tmpBuildings.clear();
            for (TeamData data : Vars.state.teams.present) {
                if (data.team != unit.team) {
                    for (Building b : data.buildings) if (unit.within(b, size) && !(b instanceof CoreBuild)) tmpBuildings.add(b);
                }
            }
            for (Building b : tmpBuildings) b.damage(b.maxHealth + 1000f);
            endScanTimer = 3;
        }
        endScanTimer -= Time.delta;
        if (endTime >= 3 * 60f) { time = 0f; flash = false; endTime = 0f; ending = false; }
    }

    @Override
    public void draw() {
        if (ending) {
            float size = Mathf.pow(Mathf.clamp(endTime / (1.75f * 60f)), 5) * 10000f;
            Draw.z(Layer.blockOver);
            Draw.color(customColor);
            Fill.poly(unit.x, unit.y, 64, size);
            Draw.color();
            return;
        }

        Draw.z(Layer.flyingUnit);
        float fin = Mathf.clamp(time / 40f);
        Draw.blend(arc.graphics.Blending.additive);
        Draw.color(customColor, fin * 0.5f);

        for (int i = 0; i < 3; i++) {
            TextureRegion reg = EkxRegions.endAPI[i];
            int k = i * 3;
            EkxGraphicUtils.circle3D(reg, unit.x, unit.y, ringRotations3D[k], ringRotations3D[k + 1], ringRotations3D[k + 2], reg.width * Draw.scl, ringRotations[i], 64);
        }
        Draw.blend();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                int idx = (i * 8 + j) * 3;
                float x = eyeLasers[idx], y = eyeLasers[idx + 1], time = eyeLasers[idx + 2];
                if (time <= 0.0001f) continue;
                Vec2 p = eyePos3D(i, j, 20f);
                float f = time / 30f, s = 5.25f * f;

                Draw.color(customColor);
                Lines.stroke(s);
                Lines.line(p.x, p.y, x, y, false);
                Fill.circle(p.x, p.y, s);
                Fill.circle(x, y, s);
                Draw.color(Color.white);
                Lines.stroke(s * 0.5f);
                Lines.line(p.x, p.y, x, y, false);
            }
        }
        Draw.reset();
    }

    Vec2 eyePos3D(int ring, int idx, float size) {
        Vec2 p = eyePos(ring, idx).scl(2f);
        Tmp.v2.set(p).setLength(size);
        float ll = p.len() / Tmp.v2.len();
        int k = ring * 3;
        Vec3 v3 = Tmp.v31.set(Tmp.v2.x, Tmp.v2.y, 0f).rotate(Vec3.Y, ringRotations3D[k + 1]).rotate(Vec3.X, ringRotations3D[k]).rotate(Vec3.Z, ringRotations3D[k + 2]);
        float sz1 = 700f / (700f - v3.z);
        v3.x *= sz1; v3.y *= sz1;
        return Tmp.v3.set(v3.x * ll + unit.x, v3.y * ll + unit.y);
    }

    Vec2 eyePos(int ring, int idx) {
        if (ringPos[ring].y == 0) return Tmp.v1.trns(ringRotations[ring] + idx * 45f, ringPos[ring].x);
        int rot = (idx / 2);
        float side = Mathf.signs[idx % 2] * ringPos[ring].y;
        return Tmp.v1.trns(ringRotations[ring] + rot * 90f, ringPos[ring].x, side);
    }
}
