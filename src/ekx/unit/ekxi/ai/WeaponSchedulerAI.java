package ekx.unit.ekxi.ai;

import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

public class WeaponSchedulerAI extends AttackAI {
    private Teamc strongest;
    private float scanTime = 0f;
    private final EkxEndAttack endAttack = new EkxEndAttack();
    
    private boolean isCasting = false;
    private float castDelay = 0f;

    @Override
    public void updateUnit() {
        if (unit == null || unit.dead) return;
        
        updateTargeting();
        if (strongest == null) return;

        unit.lookAt(strongest);

        if (isCasting) {
            endAttack.updateUnit();
            if (endAttack.ended) {
                isCasting = false;
                castDelay = 60f; 
            }
        } else {
            if (castDelay > 0) {
                castDelay -= Time.delta;
            } else {
                isCasting = true;
                endAttack.unit(unit);
                endAttack.reset();
            }
        }

        float dst = unit.dst(strongest);
        if (dst > 600f && !isCasting) {
            Vec2 v = Tmp.v1.trns(unit.angleTo(strongest), unit.type.speed);
            unit.movePref(v);
        }
    }

    private void updateTargeting() {
        if (scanTime > 0) scanTime -= Time.delta;
        if (strongest != null && (!strongest.isAdded() || (strongest instanceof mindustry.gen.Healthc h && h.dead()))) strongest = null;
        
        if (scanTime <= 0 || strongest == null) {
            strongest = null;
            final float[] maxScore = {0f};
            final Teamc[] bestTarget = {null};

            for (Unit u : mindustry.gen.Groups.unit) {
                if (u.team != unit.team && u.isValid() && unit.dst(u) < 1000f) {
                    float score = u.maxHealth / 100f - unit.dst(u) / 1000f;
                    if (score > maxScore[0]) { maxScore[0] = score; bestTarget[0] = u; }
                }
            }
            strongest = bestTarget[0];
            scanTime = 15f; 
        }
    }

    @Override public void updateWeapon(Teamc target) {}
    @Override public void reset() { 
        isCasting = false;
        castDelay = 0f;
        if (endAttack != null) endAttack.reset();
    }
                    }
