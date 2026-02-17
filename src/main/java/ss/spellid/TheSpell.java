package ss.spellid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ss.spellid.item.ModItems;

public class TheSpell implements ModInitializer {
	public static final String MOD_ID = "the-spell";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.init();
	}
}