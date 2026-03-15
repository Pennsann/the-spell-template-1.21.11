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

    // Identifiers for SLEEPER modifiers (unique per modifier)
    private static final Identifier SLEEPER_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "sleeper_health");
    private static final Identifier SLEEPER_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "sleeper_speed");
    private static final Identifier SLEEPER_ATTACK_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "sleeper_attack");

    public RankComponentImpl(Player player) {
        this.player = player;
    }

    @Override
    public Ranks getRank() {
        return rank;
    }

    @Override
    public void setRank(Ranks newRank) {
        Ranks oldRank = this.rank;
        this.rank = (newRank != null) ? newRank : Ranks.PLAYER;

        // Apply/remove modifiers only if rank actually changed
        if (oldRank != this.rank) {
            applyRankModifiers(oldRank, this.rank);
        }
    }

    /**
     * Removes modifiers of the old rank and applies modifiers of the new rank.
     */
    private void applyRankModifiers(Ranks oldRank, Ranks newRank) {
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);

        if (healthAttr == null || speedAttr == null || attackAttr == null) {
            TheSpell.LOGGER.warn("Player attributes not available for modifier application");
            return;
        }

        // Remove all known rank modifiers (clean slate)
        removeAllRankModifiers(healthAttr, speedAttr, attackAttr);

        // Apply new rank's modifiers
        switch (newRank) {
            case SLEEPER:
                // +4 health points = +2 hearts
                healthAttr.addPermanentModifier(new AttributeModifier(
                        SLEEPER_HEALTH_MODIFIER_ID,
                        4.0,
                        AttributeModifier.Operation.ADD_VALUE
                ));
                // +5% movement speed (base 0.1 -> +0.005)
                speedAttr.addPermanentModifier(new AttributeModifier(
                        SLEEPER_SPEED_MODIFIER_ID,
                        0.005,
                        AttributeModifier.Operation.ADD_VALUE
                ));
                // +1 attack damage (base 1.0 -> 2.0)
                attackAttr.addPermanentModifier(new AttributeModifier(
                        SLEEPER_ATTACK_MODIFIER_ID,
                        1.0,
                        AttributeModifier.Operation.ADD_VALUE
                ));
                TheSpell.LOGGER.info("Applied SLEEPER modifiers to player {}", player.getName().getString());
                break;
            // Add cases for higher ranks later
            default:
                break;
        }

        // Ensure current health does not exceed new max health
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    /**
     * Removes all known rank modifiers (to start fresh when rank changes).
     */
    private void removeAllRankModifiers(AttributeInstance healthAttr, AttributeInstance speedAttr, AttributeInstance attackAttr) {
        healthAttr.removeModifier(SLEEPER_HEALTH_MODIFIER_ID);
        speedAttr.removeModifier(SLEEPER_SPEED_MODIFIER_ID);
        attackAttr.removeModifier(SLEEPER_ATTACK_MODIFIER_ID);
        // Add more identifiers for future ranks
    }

    @Override
    public void readData(ValueInput input) {
        TheSpell.LOGGER.info("Loading Rank data from ValueInput");

        String rankName = input.getString("Rank").orElse("");
        try {
            Ranks loadedRank = Ranks.valueOf(rankName);
            // Use setRank to trigger modifier application (only if different)
            setRank(loadedRank);
        } catch (IllegalArgumentException e) {
            TheSpell.LOGGER.info("Invalid rank name, defaulting to PLAYER");
            setRank(Ranks.PLAYER);
        }
    }

    @Override
    public void writeData(ValueOutput output) {
        TheSpell.LOGGER.info("Saving Rank to ValueOutput: " + rank);
        output.putString("Rank", rank.name());
    }
}