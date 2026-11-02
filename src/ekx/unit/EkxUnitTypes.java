package ekx.unit;

import mindustry.type.UnitType;
import ekx.unit.empathy.EmpathyUnitType;

public class EkxUnitTypes {
    public static UnitType empathy;

    public static void load() {
        empathy = new EmpathyUnitType("ekx1");
        empathy.localizedName = "Ekx1";
    }
}
