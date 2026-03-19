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

    // SLEEPER modifiers
    private static final Identifier SLEEPER_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "sleeper_health");
    private static final Identifier SLEEPER_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "sleeper_speed");
    private static final Identifier SLEEPER_ATTACK_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "sleeper_attack");

    // AWAKENED modifiers
    private static final Identifier AWAKENED_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "awakened_health");
    private static final Identifier AWAKENED_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "awakened_speed");
    private static final Identifier AWAKENED_ATTACK_MODIFIER_ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "awakened_attack");

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
            TheSpell.LOGGER.info("Rank change for {}: {} -> {}", player.getName().getString(), oldRank, this.rank);
            applyRankModifiers(oldRank, this.rank);
            // If becoming Sleeper for the first time, record start time
            if (this.rank == Ranks.SLEEPER && oldRank != Ranks.SLEEPER) {
                EssenceComponent essence = RankComponentInitializer.ESSENCE.get(player);
                if (essence.getSleeperStartTime() == 0) {
                    long time = player.level().getServer().overworld().getGameTime();
                    essence.setSleeperStartTime(time);
                }
            }
        }
    }

    private void applyRankModifiers(Ranks oldRank, Ranks newRank) {
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (healthAttr == null || speedAttr == null || attackAttr == null) return;

        // Log current modifiers for debugging
        TheSpell.LOGGER.info("Before removal - Health modifiers: {}", healthAttr.getModifiers());
        TheSpell.LOGGER.info("Before removal - Speed modifiers: {}", speedAttr.getModifiers());
        TheSpell.LOGGER.info("Before removal - Attack modifiers: {}", attackAttr.getModifiers());

        // Remove all rank-based modifiers first
        removeAllRankModifiers(healthAttr, speedAttr, attackAttr);

        // Log after removal
        TheSpell.LOGGER.info("After removal - Health modifiers: {}", healthAttr.getModifiers());
        TheSpell.LOGGER.info("After removal - Speed modifiers: {}", speedAttr.getModifiers());
        TheSpell.LOGGER.info("After removal - Attack modifiers: {}", attackAttr.getModifiers());

        // Apply new rank modifiers
        switch (newRank) {
            case SLEEPER:
                healthAttr.addPermanentModifier(new AttributeModifier(SLEEPER_HEALTH_MODIFIER_ID, 4.0, AttributeModifier.Operation.ADD_VALUE));
                speedAttr.addPermanentModifier(new AttributeModifier(SLEEPER_SPEED_MODIFIER_ID, 0.005, AttributeModifier.Operation.ADD_VALUE));
                attackAttr.addPermanentModifier(new AttributeModifier(SLEEPER_ATTACK_MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_VALUE));
                break;
            case AWAKENED:
                healthAttr.addPermanentModifier(new AttributeModifier(AWAKENED_HEALTH_MODIFIER_ID, 6.0, AttributeModifier.Operation.ADD_VALUE));
                speedAttr.addPermanentModifier(new AttributeModifier(AWAKENED_SPEED_MODIFIER_ID, 0.01, AttributeModifier.Operation.ADD_VALUE));
                attackAttr.addPermanentModifier(new AttributeModifier(AWAKENED_ATTACK_MODIFIER_ID, 2.0, AttributeModifier.Operation.ADD_VALUE));
                break;
            default:
                // No modifiers for other ranks (yet)
                break;
        }

        // Clamp health if it exceeds new max
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }

        TheSpell.LOGGER.info("After adding - Health: {}, Speed: {}, Attack: {}", player.getMaxHealth(), speedAttr != null ? speedAttr.getValue() : 0, attackAttr != null ? attackAttr.getValue() : 0);
    }

    private void removeAllRankModifiers(AttributeInstance healthAttr, AttributeInstance speedAttr, AttributeInstance attackAttr) {
        // Remove SLEEPER modifiers
        healthAttr.removeModifier(SLEEPER_HEALTH_MODIFIER_ID);
        speedAttr.removeModifier(SLEEPER_SPEED_MODIFIER_ID);
        attackAttr.removeModifier(SLEEPER_ATTACK_MODIFIER_ID);
        // Remove AWAKENED modifiers
        healthAttr.removeModifier(AWAKENED_HEALTH_MODIFIER_ID);
        speedAttr.removeModifier(AWAKENED_SPEED_MODIFIER_ID);
        attackAttr.removeModifier(AWAKENED_ATTACK_MODIFIER_ID);
        // Add higher ranks here later
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
        // Apply modifiers after loading data; this will also remove old ones and add new ones
        applyRankModifiers(Ranks.PLAYER, rank);
    }
}