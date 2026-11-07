package ekx.unit.ekxi;

import arc.Core;
import arc.graphics.g2d.TextureRegion;

public class EkxRegions {
    public static TextureRegion[] endAPI = new TextureRegion[3];

    public static void load() {
        for (int i = 0; i < endAPI.length; i++) {
            endAPI[i] = Core.atlas.find("flameout-end-api-" + i);
            if (!endAPI[i].found()) endAPI[i] = Core.atlas.find("clear");
        }
    }
}
