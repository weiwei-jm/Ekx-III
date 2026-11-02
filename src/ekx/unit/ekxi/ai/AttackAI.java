package ekx.unit.ekxi.ai;

import mindustry.ai.types.AIController;
import mindustry.gen.Teamc;
import mindustry.gen.Units;

public abstract class AttackAI extends AIController {
    
    // 技能持续时间计时器
    public float time = 0f;
    
    // 技能释放完毕后是否结束
    public boolean ended = false;

    @Override
    public void updateUnit() {
        // 如果单位死了，停止所有技能
        if (unit.dead) return;

        // 寻找目标
        Teamc target = Units.closestTarget(unit.team, unit.x, unit.y, 800f, u -> true, b -> true);
        if (target != null) {
            unit.lookAt(target);
        }

        // 执行当前武器的逻辑
        updateWeapon(target);
    }

    // 子类需要重写这个方法来实现具体的攻击逻辑
    public abstract void updateWeapon(Teamc target);

    // 重置状态，准备下一次被调用
    public void reset() {
        time = 0f;
        ended = false;
    }
}
