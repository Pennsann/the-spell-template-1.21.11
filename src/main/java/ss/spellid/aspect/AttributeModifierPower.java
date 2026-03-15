package ss.spellid.aspect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import ss.spellid.TheSpell;

public class AttributeModifierPower implements AspectPower {
    private final Identifier attributeId;
    private final double amount;
    private final AttributeModifier.Operation operation;
    private final Identifier modifierId;

    public AttributeModifierPower(Identifier attributeId, double amount, AttributeModifier.Operation operation, String name) {
        this.attributeId = attributeId;
        this.amount = amount;
        this.operation = operation;
        this.modifierId = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, name);
    }

    @Override
    public void apply(Player player) {
        var optionalHolder = BuiltInRegistries.ATTRIBUTE.get(attributeId);
        if (optionalHolder.isEmpty()) {
            TheSpell.LOGGER.warn("Attribute not found: {}", attributeId);
            return;
        }
        Holder<Attribute> holder = optionalHolder.get();
        AttributeInstance attr = player.getAttribute(holder);
        if (attr != null) {
            attr.addTransientModifier(new AttributeModifier(modifierId, amount, operation));
        }
    }

    @Override
    public void remove(Player player) {
        var optionalHolder = BuiltInRegistries.ATTRIBUTE.get(attributeId);
        if (optionalHolder.isEmpty()) return;
        Holder<Attribute> holder = optionalHolder.get();
        AttributeInstance attr = player.getAttribute(holder);
        if (attr != null) {
            attr.removeModifier(modifierId);
        }
    }
}