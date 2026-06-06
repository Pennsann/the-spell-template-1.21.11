package ss.spellid.aspect;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import ss.spellid.aspect.ability.AspectAbility;
import ss.spellid.ranks.Ranks;

import java.util.ArrayList;
import java.util.List;

public class Aspect {
    private final Identifier id;
    private final Component displayName;
    private final Component description;
    private final ItemStack icon;
    private final List<AspectPower> powers;
    private final List<AspectAbility> abilitiesBySlot; // index 0,1,2

    // New constructor: list of 3 abilities (can be null for missing slots)
    public Aspect(Identifier id, Component displayName, Component description, ItemStack icon,
                  List<AspectPower> powers, List<AspectAbility> abilitiesBySlot) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.powers = powers;
        this.abilitiesBySlot = abilitiesBySlot != null ? abilitiesBySlot : new ArrayList<>();
        // Ensure size at least 3 by padding with nulls
        while (this.abilitiesBySlot.size() < 3) this.abilitiesBySlot.add(null);
    }

    // Convenience for aspects with no abilities
    public Aspect(Identifier id, Component displayName, Component description, ItemStack icon, List<AspectPower> powers) {
        this(id, displayName, description, icon, powers, List.of());
    }

    public Identifier getId() { return id; }
    public Component getDisplayName() { return displayName; }
    public Component getDescription() { return description; }
    public ItemStack getIcon() { return icon; }
    public List<AspectPower> getPowers() { return powers; }

    /**
     * Returns the ability for the given slot (0 = Dormant, 1 = Awakened, 2 = Ascended), or null if none.
     */
    public AspectAbility getAbilityForSlot(int slot) {
        if (slot < 0 || slot >= abilitiesBySlot.size()) return null;
        return abilitiesBySlot.get(slot);
    }

    /**
     * Legacy method for single‑ability aspects (returns the first non‑null ability).
     */
    public AspectAbility getAbility() {
        return abilitiesBySlot.stream().filter(a -> a != null).findFirst().orElse(null);
    }

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