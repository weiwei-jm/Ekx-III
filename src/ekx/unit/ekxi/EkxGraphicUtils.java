package ekx.unit.ekxi;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;

public class EkxGraphicUtils {
    static Vec3 v = new Vec3();
    static Vec2 v2 = new Vec2();
    public static float[] verts = new float[4 * 6];

    public static void circle3D(TextureRegion region, float x, float y, float rx, float ry, float rz, float size, float angle, int iter) {
        float color = Draw.getColor().toFloatBits();
        float mcolor = Color.clearFloatBits;

        for (int i = 0; i < iter; i++) {
            float ang1 = (360f / iter) * i;
            float ang2 = (360f / iter) * (i + 1f);

            v2.trns(ang1, 0.5f);
            float uu1 = v2.x + 0.5f;
            float vv1 = v2.y + 0.5f;
            v2.trns(ang2, 0.5f);
            float uu2 = v2.x + 0.5f;
            float vv2 = v2.y + 0.5f;

            float mu = (region.u + region.u2) / 2f;
            float mv = (region.v + region.v2) / 2f;

            float tu1 = Mathf.lerp(region.u, region.u2, uu1), tv1 = Mathf.lerp(region.v, region.v2, vv1);
            float tu2 = Mathf.lerp(region.u, region.u2, uu2), tv2 = Mathf.lerp(region.v, region.v2, vv2);

            v2.trns(ang1 + angle, size);
            v.set(v2.x, v2.y, 0f).rotate(Vec3.Y, ry).rotate(Vec3.X, rx).rotate(Vec3.Z, rz);
            float sz1 = 700f / (700f - v.z);
            v.x *= sz1;
            v.y *= sz1;

            float x1 = v.x + x, y1 = v.y + y;

            v2.trns(ang2 + angle, size);
            v.set(v2.x, v2.y, 0f).rotate(Vec3.Y, ry).rotate(Vec3.X, rx).rotate(Vec3.Z, rz);
            float sz2 = 700f / (700f - v.z);
            v.x *= sz2;
            v.y *= sz2;

            float x2 = v.x + x, y2 = v.y + y;

            verts[0] = x; verts[1] = y; verts[2] = color;
            verts[3] = mu; verts[4] = mv; verts[5] = mcolor;
            verts[6] = x; verts[7] = y; verts[8] = color;
            verts[9] = mu; verts[10] = mv; verts[11] = mcolor;
            verts[12] = x1; verts[13] = y1; verts[14] = color;
            verts[15] = tu1; verts[16] = tv1; verts[17] = mcolor;
            verts[18] = x2; verts[19] = y2; verts[20] = color;
            verts[21] = tu2; verts[22] = tv2; verts[23] = mcolor;

            Draw.vert(region.texture, verts, 0, 24);
        }
    }
}
