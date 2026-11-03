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
import ekx.EkxPal;
import mindustry.content.Fx;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;

public class Ekx3DMagicAI extends AttackAI {
    static Vec2[] ringPos = {new Vec2(44.25f, 18f), new Vec2(30.5f, 0f), new Vec2(19.5f, 7.5f)};
    static float[] ringMoveSpeed = {2f, 4f, 8f};
    static float[][] rotations3D = {{0.0625f, 1f, 0.125f}, {0f, 0.25f, 1f}, {1f, 1f, 0.1f}};
    
    float time = 0f;
    float[] ringRotations = new float[3], ringRotations3D = new float[3 * 3];
    
    // 动画时间参数
    final float DRAW_TIME = 120f; // 2秒画阵
    final float TURN_TIME = 30f;  // 0.5秒转身
    final float LASER_TIME = 240f; // 4秒激光
    final float FADE_TIME = 30f;  // 0.5秒消失
    final float TOTAL_TIME = DRAW_TIME + TURN_TIME + LASER_TIME + FADE_TIME;
    
    float startRotation = 0f;
    float targetRotation = 0f;
    boolean laserFired = false;

    @Override
    public void reset() {
        super.reset();
        time = 0f;
        laserFired = false;
        java.util.Arrays.fill(ringRotations, 0f);
        java.util.Arrays.fill(ringRotations3D, 0f);
    }

    @Override
    public void updateWeapon(Teamc target) {
        time += Time.delta;

        // 1. 画阵期间（2秒），缓慢旋转
        if (time < DRAW_TIME) {
            float speed = Interp.pow2Out.apply(time / DRAW_TIME);
            for (int i = 0; i < 3; i++) {
                ringRotations[i] += ringMoveSpeed[i] * speed * Time.delta * 0.2f;
                for (int j = 0; j < 3; j++) {
                    int k = i * 3 + j;
                    ringRotations3D[k] += speed * Time.delta * 1.5f * rotations3D[i][j];
                    ringRotations3D[k] %= 360f;
                }
            }
        } 
        // 2. 转身期间（0.5秒）
        else if (time < DRAW_TIME + TURN_TIME) {
            if (startRotation == 0f && target != null) {
                startRotation = unit.rotation;
                targetRotation = unit.angleTo(target);
            }
            // 平滑滑向目标
            float turnProgress = (time - DRAW_TIME) / TURN_TIME;
            unit.rotation = Mathf.lerp(startRotation, targetRotation, Interp.pow2Out.apply(turnProgress));
        } 
        // 3. 激光开火期间（4秒）
        else if (time < DRAW_TIME + TURN_TIME + LASER_TIME) {
            if (!laserFired && target != null) {
                // 发射大光柱
                new EkxMeltLaserBullet().create(unit, unit.team, unit.x, unit.y, unit.rotation);
                laserFired = true;
                mindustry.Vars.renderer.shake(15f, 60f);
            }
            // 开火时法阵缓慢自转
            for (int i = 0; i < 3; i++) {
                ringRotations[i] += ringMoveSpeed[i] * Time.delta * 0.05f;
            }
        }
        // 4. 结束判定
        else if (time >= TOTAL_TIME) {
            ended = true;
        }
    }
    
    public void draw() {
        // 计算全局透明度（在最后0.5秒慢慢消失）
        float fadeAlpha = 1f;
        if (time > DRAW_TIME + TURN_TIME + LASER_TIME) {
            fadeAlpha = 1f - ((time - (DRAW_TIME + TURN_TIME + LASER_TIME)) / FADE_TIME);
        }
        fadeAlpha = Mathf.clamp(fadeAlpha);

        Color darkC = Tmp.c1.set(EkxPal.empathyDark).lerp(EkxPal.empathy, Mathf.absin(2f, 1f));
        Color col1 = Tmp.c2.set(EkxPal.empathy).lerp(Color.white, Mathf.absin(2f, 1f));

        float z = Draw.z();
        Draw.z(Layer.flyingUnit + 1f);

        // 计算描绘进度（0 到 1）
        float drawProgress = Mathf.clamp(time / DRAW_TIME);
        // 为了让线条变粗，我们预留了不同的层级
        for (int i = 0; i < 3; i++) {
            int k = i * 3;
            // 根据进度，半径逐渐展开，线条逐渐变粗
            float rad = (80f + i * 40f) * Interp.pow2Out.apply(drawProgress);
            float lineWidth = 5f * Interp.pow2Out.apply(drawProgress) * fadeAlpha;

            // 绘制自发光外圈
            Draw.color(darkC.cpy().a(0.4f * fadeAlpha));
            Lines.stroke(lineWidth * 2.5f);
            drawCircle3D(unit.x, unit.y, ringRotations3D[k], ringRotations3D[k + 1], ringRotations3D[k + 2], rad, ringRotations[i], (int)(64 * drawProgress));

            // 绘制实体主线条
            Draw.color(col1.cpy().a(fadeAlpha));
            Lines.stroke(lineWidth);
            drawCircle3D(unit.x, unit.y, ringRotations3D[k], ringRotations3D[k + 1], ringRotations3D[k + 2], rad, ringRotations[i], (int)(64 * drawProgress));
            
            // 绘制白色高亮
            Draw.color(Color.white, fadeAlpha);
            Lines.stroke(lineWidth * 0.4f);
            drawCircle3D(unit.x, unit.y, ringRotations3D[k], ringRotations3D[k + 1], ringRotations3D[k + 2], rad, ringRotations[i], (int)(64 * drawProgress));
        }

        Draw.z(z);
        Draw.color();
    }

    private void drawCircle3D(float x, float y, float rx, float ry, float rz, float radius, float rotation, int sides) {
        if (sides <= 2) return;
        Lines.beginLine();
        for (int i = 0; i <= sides; i++) {
            float ang = (i * (360f / 64f)) + rotation;
            Vec3 v3 = Tmp.v31.set(Mathf.cosDeg(ang) * radius, Mathf.sinDeg(ang) * radius, 0f)
                    .rotate(Vec3.Y, ry).rotate(Vec3.X, rx).rotate(Vec3.Z, rz);
            float sz1 = 700f / (700f - v3.z);
            Lines.linePoint(x + v3.x * sz1, y + v3.y * sz1);
        }
        Lines.endLine();
    }
}
