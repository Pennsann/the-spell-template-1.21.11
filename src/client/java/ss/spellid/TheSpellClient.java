package ss.spellid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import ss.spellid.TheSpell;
import ss.spellid.components.RankComponentInitializer;

public class TheSpellClient implements ClientModInitializer {
	private static final Identifier ESSENCE_ICONS_TEXTURE =
			Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "textures/gui/essence_bar_icons.png");

	private static final int ICON_WIDTH = 7;
	private static final int ICON_HEIGHT = 9;
	private static final int ICON_COUNT = 10;
	private static final int BAR_WIDTH = ICON_WIDTH * ICON_COUNT;
	private static final int BAR_HEIGHT = ICON_HEIGHT;
	private static final int TEXTURE_WIDTH = 9;
	private static final int TEXTURE_HEIGHT = 30; // 3*9 + 2*1.5? adjust as needed
	private static final int LEFT_PAD = 1;
	private static final int V_GAP = 1;

	private static final int ROW_FULL = 0;
	private static final int ROW_HALF = 1;
	private static final int ROW_EMPTY = 2;

	@Override
	public void onInitializeClient() {
//		HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
//			Minecraft mc = Minecraft.getInstance();
//			Player player = mc.player;
//			if (player == null) return;
//
//			var essence = RankComponentInitializer.ESSENCE.get(player);
//			int current = essence.getCurrentEssence();
//			int max = 225;//essence.getMaxEssence();
//
//			int startX = (mc.getWindow().getGuiScaledWidth() - BAR_WIDTH) / 2;
//			int y = mc.getWindow().getGuiScaledHeight() - 40;
//
//			float percent = (float) current / max;
//			float iconValue = percent * ICON_COUNT;
//			int fullIcons = (int) Math.floor(iconValue);
//			float remainder = iconValue - fullIcons;
//			boolean hasHalf = remainder >= 0.5f;
//
//			for (int i = 0; i < ICON_COUNT; i++) {
//				int row;
//				if (i < fullIcons) {
//					row = ROW_FULL;
//				} else if (i == fullIcons && hasHalf) {
//					row = ROW_HALF;
//				} else {
//					row = ROW_EMPTY;
//				}
//
//				int x = startX + i * ICON_WIDTH;
//				int u = LEFT_PAD;
//				int v = row * (ICON_HEIGHT + V_GAP);
//
//				guiGraphics.blit(
//						ESSENCE_ICONS_TEXTURE,
//						x, y,
//						u, v,
//						ICON_WIDTH, ICON_HEIGHT,
//						TEXTURE_WIDTH, TEXTURE_HEIGHT
//				);
//			}
//		});
//	}
	}
}