package ekx.unit.ekxi;

import ekx.unit.ekxi.ai.WeaponSchedulerAI;
import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.meta.Env;

public class EkxIUnitType extends UnitType {
    public EkxIUnitType(String name) {
        super(name);
        flying = true;
        lowAltitude = true;
        hitSize = 24f;
        drag = 0.07f;
        health = 1f;
        speed = 3f;
        range = 800f;

        outlines = false;
        drawCell = false;
        bounded = false;
        createScorch = false;
        hidden = false; 

        engineSize = -1f;
        
        // 核心：直接将 AI 指向最高统帅调度器
        controller = u -> new WeaponSchedulerAI();
        constructor = EkxIUnit::new;

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
        if (!(unit instanceof EkxIUnit)) {
            unit.destroy();
            return;
        }
        super.update(unit);
    }
}
