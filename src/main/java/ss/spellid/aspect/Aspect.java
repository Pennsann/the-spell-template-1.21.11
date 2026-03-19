package ss.spellid.aspect;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import ss.spellid.aspect.ability.AspectAbility;

import java.util.List;

public class Aspect {
    private final Identifier id;
    private final Component displayName;
    private final Component description;
    private final ItemStack icon;
    private final List<AspectPower> powers;
    private final AspectAbility ability;

    public Aspect(Identifier id, Component displayName, Component description, ItemStack icon, List<AspectPower> powers, AspectAbility ability) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.powers = powers;
        this.ability = ability;
    }

    public Identifier getId() { return id; }
    public Component getDisplayName() { return displayName; }
    public Component getDescription() { return description; }
    public ItemStack getIcon() { return icon; }
    public List<AspectPower> getPowers() { return powers; }
    public AspectAbility getAbility() { return ability; }

    public void applyTo(Player player) {
        for (AspectPower power : powers) {
            power.apply(player);
        }
    }

    public void removeFrom(Player player) {
        for (AspectPower power : powers) {
            power.remove(player);
        }
    }
}