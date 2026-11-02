package ekx.unit.ekxi.ai;

import ekx.unit.ekxi.EkxIUnit;

public class WeaponSchedulerAI extends AttackAI {
    private final AttackAI[] weapons;
    private int currentWeaponIndex = 0;

    public WeaponSchedulerAI() {
        // 在这里注册你的所有武器！顺序就是释放的顺序。
        weapons = new AttackAI[]{
            new BasicLaserAI(),
            new EndAttackAI()
        };
    }

    @Override
    public void updateUnit() {
        if (unit.dead) return;

        // 初始化或切换武器
        if (weapons[currentWeaponIndex].controller() != unit) {
            weapons[currentWeaponIndex].unit(unit);
            weapons[currentWeaponIndex].reset();
        }

        AttackAI current = weapons[currentWeaponIndex];
        current.updateUnit();

        // 如果当前武器打完了（子类里会把 ended 设为 true）
        if (current.ended) {
            // 切换到下一个武器，循环往复
            currentWeaponIndex = (currentWeaponIndex + 1) % weapons.length;
        }
    }

    // 这两个方法是空的，因为具体的攻击逻辑已经在子 AI 里了
    @Override public void updateWeapon(mindustry.gen.Teamc target) {}
    @Override public void reset() {}
}
