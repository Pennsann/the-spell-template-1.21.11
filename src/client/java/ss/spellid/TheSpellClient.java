package ss.spellid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import ss.spellid.network.AbilityUsePayload;

public class TheSpellClient implements ClientModInitializer {
	private static KeyMapping dormantKey;
	private static KeyMapping awakenedKey;
	private static KeyMapping ascendedKey;
	private static final KeyMapping.Category ABILITY_CATEGORY =
			new KeyMapping.Category(Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "aspects"));

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
			while (dormantKey.consumeClick()) {
				if (client.player != null) {
					ClientPlayNetworking.send(new AbilityUsePayload(0));
				}
			}
			while (awakenedKey.consumeClick()) {
				if (client.player != null) {
					ClientPlayNetworking.send(new AbilityUsePayload(1));
				}
			}
			while (ascendedKey.consumeClick()) {
				if (client.player != null) {
					ClientPlayNetworking.send(new AbilityUsePayload(2));
				}
			}
		});
	}
}