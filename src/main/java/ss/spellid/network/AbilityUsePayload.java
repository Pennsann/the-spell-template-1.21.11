package ss.spellid.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ss.spellid.TheSpell;

public record AbilityUsePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AbilityUsePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "ability_use"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityUsePayload> CODEC =
            StreamCodec.unit(new AbilityUsePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}