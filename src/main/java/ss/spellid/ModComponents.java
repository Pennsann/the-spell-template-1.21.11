package ss.spellid;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import ss.spellid.components.EssenceComponent;
import ss.spellid.components.EssenceComponentImpl;
import ss.spellid.components.RankComponent;
import ss.spellid.components.RankComponentImpl;

public class ModComponents implements EntityComponentInitializer {

    public static final ComponentKey<RankComponent> RANK = ComponentRegistryV3.INSTANCE
            .getOrCreate( Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "rank"), RankComponent.class);

    public static final ComponentKey<EssenceComponent> ESSENCE = ComponentRegistryV3.INSTANCE
            .getOrCreate(Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "essence"), EssenceComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(RANK, player -> new RankComponentImpl(), RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(ESSENCE, player ->  new EssenceComponentImpl(player), RespawnCopyStrategy.ALWAYS_COPY);
        // hostiles go here too
    }
}