package ss.spellid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import ss.spellid.network.ChannelStartPayload;
import ss.spellid.network.ChannelStopPayload;

public class TheSpellClient implements ClientModInitializer {
	private static KeyMapping dormantKey;
	private static KeyMapping awakenedKey;
	private static KeyMapping ascendedKey;
	private static final KeyMapping.Category ABILITY_CATEGORY =
			new KeyMapping.Category(Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "aspects"));

	private static boolean lastDormantPressed = false;
	private static boolean lastAwakenedPressed = false;
	private static boolean lastAscendedPressed = false;

	@Override
	public void onInitializeClient() {
		dormantKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.the-spell.dormant_ability",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				ABILITY_CATEGORY
		));
		awakenedKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.the-spell.awakened_ability",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_G,
				ABILITY_CATEGORY
		));
		ascendedKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.the-spell.ascended_ability",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_T,
				ABILITY_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			// Dormant (slot 0)
			boolean dormantPressed = dormantKey.isDown();
			if (dormantPressed && !lastDormantPressed) {
				ClientPlayNetworking.send(new ChannelStartPayload(0));
			} else if (!dormantPressed && lastDormantPressed) {
				ClientPlayNetworking.send(new ChannelStopPayload());
			}
			lastDormantPressed = dormantPressed;

			// Awakened (slot 1)
			boolean awakenedPressed = awakenedKey.isDown();
			if (awakenedPressed && !lastAwakenedPressed) {
				ClientPlayNetworking.send(new ChannelStartPayload(1));
			} else if (!awakenedPressed && lastAwakenedPressed) {
				ClientPlayNetworking.send(new ChannelStopPayload());
			}
			lastAwakenedPressed = awakenedPressed;

			// Ascended (slot 2)
			boolean ascendedPressed = ascendedKey.isDown();
			if (ascendedPressed && !lastAscendedPressed) {
				ClientPlayNetworking.send(new ChannelStartPayload(2));
			} else if (!ascendedPressed && lastAscendedPressed) {
				ClientPlayNetworking.send(new ChannelStopPayload());
			}
			lastAscendedPressed = ascendedPressed;
		});
	}
}