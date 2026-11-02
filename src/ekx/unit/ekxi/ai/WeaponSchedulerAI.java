package ekx.unit.ekxi.ai;

import mindustry.gen.Teamc;

public class WeaponSchedulerAI extends AttackAI {
    private final AttackAI[] weapons;
    private int currentWeaponIndex = 0;

    public WeaponSchedulerAI() {
        weapons = new AttackAI[]{
            new BasicLaserAI(),
            new EndAttackAI()
        };
    }

    @Override
    public void updateUnit() {
        if (unit == null || unit.dead) return;

        // 初始化武器
        if (weapons[currentWeaponIndex].unit() != unit) {
            weapons[currentWeaponIndex].unit(unit);
            weapons[currentWeaponIndex].reset();
        }

        AttackAI current = weapons[currentWeaponIndex];
        current.updateUnit();

        // 如果当前武器打完了，切换下一个
        if (current.ended) {
            currentWeaponIndex = (currentWeaponIndex + 1) % weapons.length;
        }
    }

    @Override public void updateWeapon(Teamc target) {}
    @Override public void reset() {}
}
