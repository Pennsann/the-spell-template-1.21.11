package ss.spellid.components;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import ss.spellid.TheSpell;
import ss.spellid.ranks.Ranks;

public class RankComponentImpl implements RankComponent {
    private Ranks rank = Ranks.PLAYER;
    private final Player player;

    private static final Identifier SLEEPER_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "sleeper_health");
    private static final Identifier SLEEPER_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "sleeper_speed");
    private static final Identifier SLEEPER_ATTACK_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "sleeper_attack");

    public RankComponentImpl(Player player) {
        this.player = player;
    }

    @Override
    public Ranks getRank() { return rank; }

    @Override
    public void setRank(Ranks newRank) {
        Ranks oldRank = this.rank;
        this.rank = (newRank != null) ? newRank : Ranks.PLAYER;
        if (oldRank != this.rank) {
            applyRankModifiers(oldRank, this.rank);
        }
    }

    private void applyRankModifiers(Ranks oldRank, Ranks newRank) {
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (healthAttr == null || speedAttr == null || attackAttr == null) return;

        removeAllRankModifiers(healthAttr, speedAttr, attackAttr);

        switch (newRank) {
            case SLEEPER:
                healthAttr.addPermanentModifier(new AttributeModifier(SLEEPER_HEALTH_MODIFIER_ID, 4.0, AttributeModifier.Operation.ADD_VALUE));
                speedAttr.addPermanentModifier(new AttributeModifier(SLEEPER_SPEED_MODIFIER_ID, 0.005, AttributeModifier.Operation.ADD_VALUE));
                attackAttr.addPermanentModifier(new AttributeModifier(SLEEPER_ATTACK_MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_VALUE));
                break;
            default: break;
        }

        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private void removeAllRankModifiers(AttributeInstance healthAttr, AttributeInstance speedAttr, AttributeInstance attackAttr) {
        healthAttr.removeModifier(SLEEPER_HEALTH_MODIFIER_ID);
        speedAttr.removeModifier(SLEEPER_SPEED_MODIFIER_ID);
        attackAttr.removeModifier(SLEEPER_ATTACK_MODIFIER_ID);
    }

    @Override
    public void writeData(ValueOutput output) {
        output.putString("Rank", rank.name());
    }

    @Override
    public void readData(ValueInput input) {
        String rankName = input.getString("Rank").orElse("");
        try {
            rank = Ranks.valueOf(rankName);
        } catch (IllegalArgumentException e) {
            rank = Ranks.PLAYER;
        }
        applyRankModifiers(Ranks.PLAYER, rank);
    }
}