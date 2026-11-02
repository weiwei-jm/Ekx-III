package ekx.unit;

import mindustry.type.UnitType;
import ekx.unit.ekxi.EkxIUnitType; // 改成了 EkxIUnitType

public class EkxUnitTypes {
    public static UnitType ekx1; // 变量名顺便改成了 ekx1

    public static void load() {
        ekx1 = new EkxIUnitType("ekx1");
        ekx1.localizedName = "Ekx-I";
    }
}
