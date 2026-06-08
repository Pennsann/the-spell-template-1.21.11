package ss.spellid.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ss.spellid.TheSpell;

public record ChannelStartPayload(int slot) implements CustomPacketPayload {
    public static final Type<ChannelStartPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "channel_start"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelStartPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeInt(payload.slot()),
                    buf -> new ChannelStartPayload(buf.readInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}