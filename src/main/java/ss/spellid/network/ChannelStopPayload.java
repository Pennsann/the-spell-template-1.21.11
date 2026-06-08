package ss.spellid.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ss.spellid.TheSpell;

public record ChannelStopPayload() implements CustomPacketPayload {
    public static final Type<ChannelStopPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "channel_stop"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelStopPayload> CODEC = StreamCodec.unit(new ChannelStopPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}