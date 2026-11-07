package ekx.unit.ekxi;

import arc.Core;
import arc.graphics.g2d.TextureRegion;

public class EkxRegions {
    public static TextureRegion[] endAPI = new TextureRegion[3];
    public static TextureRegion magicCircle, flash;

    public static void load() {
        for (int i = 0; i < 3; i++) {
            endAPI[i] = Core.atlas.find("ekx-end-api-" + i);
        }
        magicCircle = Core.atlas.find("ekx-magic-circle");
        flash = Core.atlas.find("ekx-flash");
    }
}
