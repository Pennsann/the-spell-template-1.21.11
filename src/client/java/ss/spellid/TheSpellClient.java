package ss.spellid;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;

public class TheSpellClient implements ClientModInitializer {

	public static KeyMapping openSpellKey;

	@Override
	public void onInitializeClient() {
		System.out.println("Category translation key: key.category." + TheSpell.MOD_ID + ".keys");
		KeyMapping.Category SPELL_CATEGORY = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "keys")
		);

		openSpellKey = KeyBindingHelper.registerKeyBinding(
				new KeyMapping(
						"The spell",
						InputConstants.Type.KEYSYM,
						GLFW.GLFW_KEY_O,
						SPELL_CATEGORY
				)
		);


			}
	}