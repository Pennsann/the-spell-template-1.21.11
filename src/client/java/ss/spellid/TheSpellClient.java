package ss.spellid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import ss.spellid.TheSpell;
import ss.spellid.network.AbilityUsePayload;

public class TheSpellClient implements ClientModInitializer {
	private static KeyMapping abilityKey;
	private static final KeyMapping.Category ABILITY_CATEGORY =
			new KeyMapping.Category(Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "general"));

	@Override
	public void onInitializeClient() {
		abilityKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.the-spell.ability",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				ABILITY_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (abilityKey.consumeClick()) {
				if (client.player != null) {
					ClientPlayNetworking.send(new AbilityUsePayload());
				}
			}
		});
	}
}