package ekx.unit.ekxi;

import ekx.unit.NullAI;
import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.meta.Env;

public class EkxIUnitType extends UnitType {
    public EkxIUnitType(String name) {
        super(name);
        flying = true;
        hitSize = 16f;
        drag = 0.07f;
        health = 10000f;
        speed = 2f;

        outlines = false;
        drawCell = false;
        bounded = false;
        createScorch = false;
        hidden = false; 

        engineSize = -1f;
        
        /*aiController = mindustry.ai.types.FollowAI::new;*/
        controller = u -> new NullAI();
        constructor = EkxIUnit::new; // 改成了 EkxIUnit

        envEnabled = Env.any;
        envDisabled = 0;
    }

    @Override
    public void init() {
        super.init();
        for (StatusEffect s : Vars.content.statusEffects()) {
            immunities.add(s);
        }
    }

    @Override
    public void update(Unit unit) {
        if (!(unit instanceof EkxIUnit)) { // 改成了 EkxIUnit
            unit.destroy();
            return;
        }
        super.update(unit);
    }
}
