package ekx.unit.ekxi;

import ekx.unit.NullAI;
import ekx.unit.ekxi.ai.WeaponSchedulerAI; // 引入调度器
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
        health = 1f; // 锁死 1 血
        speed = 2f;
        range = 800f;

        outlines = false;
        drawCell = false;
        bounded = false;
        createScorch = false;
        hidden = false; 

        engineSize = -1f;
        
        // 【关键】把 AI 换成我们写的武器调度器
        aiController = WeaponSchedulerAI::new;
        controller = u -> new NullAI();
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
