package ekx.unit.ekxi.ai;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Blending; // 补充这个
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.Seq;       // 补充这个
import arc.struct.FloatSeq;  // 补充这个
import arc.util.*;
import ekx.unit.ekxi.*;
import mindustry.Vars;
import mindustry.game.Teams.TeamData;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.storage.CoreBlock.*;

public class EkxEndAttack extends AttackAI {
    static Vec2[] ringPos = {new Vec2(44.25f, 18f), new Vec2(30.5f, 0f), new Vec2(19.5f, 7.5f)};
    static float[] ringMoveSpeed = {2f, 4f, 8f}, ringReloadTimes = {12f, 8f, 5f};
    static float[][] rotations3D = {{0.0625f, 1f, 0.125f}, {0f, 0.25f, 1f}, {1f, 1f, 0.1f}};
    static Interp swing = new Interp.SwingOut(5f);
    static Seq<Building> tmpBuildings = new Seq<>();
    float[] ringRotations = new float[3], ringRotations3D = new float[9];
    float[] ringReloads = new float[24];
    float[] eyeLasers = new float[72];
    int[] ringIdxs = new int[3];
    Seq<Teamc> nearests = new Seq<>(true, 25);
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
                            if (ringRotations3D[k] >= 360f) ringRotations3D[k] = 0f;
                        }
                    }
                }
            }
        } else if (!flash) {
            for (int i = 0; i < 3; i++) {
                TextureRegion reg = EkxRegions.endAPI[i];
                for (int j = 0; j < 8; j++) {
                    Vec2 p = eyePos3D(i, j, reg.width * Draw.scl);
                    EkxFX.endFlash.at(p.x, p.y, 1f, unit);
                }
            }
            EkxFX.endFlash.at(unit.x, unit.y, 3f, unit);
            flash = true;
        }

    if (time > duration + 20f && !ending) {
        crosses.clear();
        int size = Math.min((int) ((Vars.world.width() * Vars.world.height()) / ((200 * 200) / 8f)), 1000);
        for (int i = 0; i < size; i++) {
            crosses.add(Mathf.random(Vars.world.unitWidth()), Mathf.random(Vars.world.unitHeight()), Mathf.random(360), Mathf.random(1.7f, 4.25f));
            crosses.add(0f);
        }
        ending = true;
    }
    if (ending) updateEnd();
    updateRings();
    if (time >= duration + 20f + 10 * 60f) {
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
                Teamc tar = nearests.get(idx % nearests.size);
                if (tar != null) {
                    if (tar instanceof Unit u) {
                        float d = Mathf.pow(u.health / 100000000f, 2) * u.maxHealth;
                        EkxIDamage.damageUnit(u, 10000f + d, true, null);
                    } else if (tar instanceof Building b) {
                        float d = Mathf.pow(b.health / 100000000f, 2) * b.maxHealth;
                        EkxIDamage.damageBuilding(b, 10000f + d, true, null);
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
    float[] ar = crosses.items;
    int s = crosses.size;
    float size = Mathf.pow(Mathf.clamp(endTime / (1.75f * 60f)), 5) * 10000f;
    float oldSize = Mathf.pow(Mathf.clamp((endTime - Time.delta) / (1.75f * 60f)), 5) * 10000f;

    if (endScanTimer <= 0f && endTime < (1.75f * 60f)) {
        for (Unit u : Groups.unit) {
            if (u.team != unit.team && unit.within(u, size)) EkxIDamage.damageUnit(u, u.maxHealth + 1000f, true, () -> EkxFX.endDeath.at(u.x, u.y, u.hitSize / 2));
        }
        tmpBuildings.clear();
        for (TeamData data : Vars.state.teams.present) {
            if (data.team != unit.team && !data.buildings.isEmpty()) {
                for (Building b : data.buildings) if (unit.within(b, size) && !(b instanceof CoreBuild)) tmpBuildings.add(b);
            }
        }
        for (Building b : tmpBuildings) EkxIDamage.damageBuilding(b, b.maxHealth + 1000f, true, () -> EkxFX.endDeath.at(b.x, b.y, b.hitSize / 2));
        tmpBuildings.clear();
        endScanTimer = 3;
    }
    if (endTime < (1.75f * 60f)) {
        for (TeamData data : Vars.state.teams.present) if (data.team != unit.team) data.plans.clear();
        double area = (size * size * Math.PI) - (oldSize * oldSize * Math.PI);
        int amount = Math.min((int) (area / (100f * 100f * Mathf.pi)), 250);
        for (int i = 0; i < amount; i++) {
            float r = Mathf.random(360);
            Vec2 v = Tmp.v2.trns(r, Mathf.random(oldSize, size)).add(unit.x, unit.y);
            if (v.x > 0 && v.y > 0 && v.x < Vars.world.unitWidth() && v.y < Vars.world.unitHeight()) EkxFX.endSplash.at(v.x, v.y, r);
        }
    }
    endScanTimer -= Time.delta;
    for (int i = 0; i < s; i += 5) {
        float x = ar[i], y = ar[i + 1];
        if (unit.within(x, y, size)) ar[i + 4] = Mathf.clamp(ar[i + 4] + Time.delta / 5f);
    }
    if (endTime >= 10 * 60f) { time = 0f; flash = false; endTime = 0f; ending = false; }
}

void drawEnd() {
        float size = Mathf.pow(Mathf.clamp(endTime / (1.75f * 60f)), 5) * 10000f;
        float fout = Mathf.clamp((10f * 60 - endTime) / 10f);
        float sfout = Mathf.clamp((10f * 60 - endTime) / 30f);
        float z = Draw.z();
        Draw.z(Layer.blockOver);
        Draw.color(Color.red);
        if (fout >= 0.999f) Fill.poly(unit.x, unit.y, 64, size);
        else Fill.rect(Core.camera.position.x, Core.camera.position.y, Core.camera.width, Core.camera.height * fout);
        Draw.blend(Blending.additive);
        Draw.color(Color.valueOf("6C9FFFFF"));
        Draw.z(Layer.flyingUnit);
        float[] ar = crosses.items;
        int s = crosses.size;
        TextureRegion flashReg = EkxRegions.flash;
        Rect r = Tmp.r1;
        Core.camera.bounds(r);
        for (int i = 0; i < s; i += 5) {
            float x = ar[i], y = ar[i + 1], rot = ar[i + 2], scl = ar[i + 3] * fout, fin = ar[i + 4];
            Tmp.r2.setCentered(x, y, flashReg.width * scl);
            if (r.overlaps(Tmp.r2) && Mathf.dst(unit.x, unit.y, x, y) < size) {
                float f = swing.apply(fin);
                Draw.rect(flashReg, x, y, flashReg.width * scl * f, flashReg.height * scl * f, rot);
            }
        }
        Draw.color();
        float f = swing.apply(Mathf.clamp((endTime - 30f) / 15f));
        float sin = 1f + Mathf.absin(2f, 0.1f);
        for (int i = 0; i < 4; i++) Drawf.tri(unit.x, unit.y, 15f * sfout, 280f * f * sfout * sin, i * 90);
        Lines.stroke(3f * Mathf.clamp((endTime - 30f) / 20f) * sfout * sin);
        Lines.circle(unit.x, unit.y, 75f);
        Draw.blend();
        Draw.color();
        Draw.z(z);
    }

    @Override
    public void draw() {
        if (ending) drawEnd();
        Color darkC = Tmp.c1.set(Color.valueOf("3A5F9FFF")).lerp(Color.valueOf("6C9FFFFF"), Mathf.absin(2f, 1f));
        Color col1 = Tmp.c2.set(Color.valueOf("6C9FFFFF")).lerp(Color.white, Mathf.absin(2f, 1f));
        float fin = Mathf.clamp(time / 40f);
        Draw.blend(Blending.additive);
        Draw.color(darkC, fin * 0.5f);
        TextureRegion mag = EkxRegions.magicCircle;
        Draw.rect(mag, unit.x, unit.y, mag.width * Draw.scl * 2f, mag.height * Draw.scl * 2f);
        for (int i = 0; i < 3; i++) {
            Draw.color(darkC, fin);
            TextureRegion reg = EkxRegions.endAPI[i];
            int k = i * 3;
            EkxGraphicUtils.circle3D(reg, unit.x, unit.y, ringRotations3D[k], ringRotations3D[k + 1], ringRotations3D[k + 2], reg.width * Draw.scl, ringRotations[i], 64);
            Draw.color(Color.white);
            for (int j = 0; j < 8; j++) {
                Vec2 p = eyePos(i, j).scl(2f);
                Tmp.v2.set(p).setLength(reg.width * Draw.scl);
                float ll = p.len() / Tmp.v2.len();
                Vec3 v3 = Tmp.v31.set(Tmp.v2.x, Tmp.v2.y, 0f).rotate(Vec3.Y, ringRotations3D[k + 1]).rotate(Vec3.X, ringRotations3D[k]).rotate(Vec3.Z, ringRotations3D[k + 2]);
                float sz1 = 700f / (700f - v3.z);
                v3.x *= sz1; v3.y *= sz1;
                Fill.circle(v3.x * ll + unit.x, v3.y * ll + unit.y, 2f * fin);
            }
        }
        Draw.blend();
        float z = Draw.z();
        for (int i = 0; i < 3; i++) {
            TextureRegion reg = EkxRegions.endAPI[i];
            float size = reg.width * Draw.scl;
            for (int j = 0; j < 8; j++) {
                int idx = (i * 8 + j) * 3;
                float x = eyeLasers[idx], y = eyeLasers[idx + 1], t = eyeLasers[idx + 2];
                if (t <= 0.0001f) continue;
                Vec2 p = eyePos3D(i, j, size);
                float f = t / 30f, s = 5.25f * f;
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
