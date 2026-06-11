package ss.spellid.aspect.definition;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ss.spellid.TheSpell;
import ss.spellid.aspect.Aspect;
import ss.spellid.aspect.ability.frost.FrigidTorrentAbility;
import ss.spellid.aspect.ability.frost.PermafrostAuraPower;
import ss.spellid.aspect.ability.frost.PermafrostTouchAbility;
import java.util.List;

public class FrostOfTheLonelyPeakAspect {
    public static final Aspect INSTANCE = new Aspect(
            Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "frost_of_the_lonely_peak"),
            Component.literal("Frost of the Lonely Peak"),
            Component.literal("The cold of the highest mountain, where even thoughts freeze."),
            new ItemStack(Items.PACKED_ICE),
            List.of(new PermafrostAuraPower()),
            List.of(
                    new PermafrostTouchAbility(),   // slot 0 - Sleeper
                    new FrigidTorrentAbility(),      // slot 1 - Awakened
                    null                             // slot 2 - Ascended (not yet implemented)
            )
    );
}