package exk;

import mindustry.mod.*;
import exk.unit.ExkUnitTypes;

public class EkxMod extends Mod {
    @Override
    public void loadContent() {
        ExkUnitTypes.load();
    }
}
