package ss.spellid.components;

import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import net.minecraft.resources.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import ss.spellid.TheSpell;

public class RankComponentInitializer implements EntityComponentInitializer {
    public static final ComponentKey<RankComponent> RANK_KEY =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                    Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "rank"),
                    RankComponent.class
            );

    public static final ComponentKey<EssenceComponent> ESSENCE =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                    Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "essence"),
                    EssenceComponent.class
            );

    // New nightmare instance component
    public static final ComponentKey<NightmareInstance> NIGHTMARE_INSTANCE =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                    Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "nightmare_instance"),
                    NightmareInstance.class
            );

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(
                RANK_KEY,
                player -> new RankComponentImpl(player),
                RespawnCopyStrategy.ALWAYS_COPY
        );

        registry.registerForPlayers(
                ESSENCE,
                player -> new EssenceComponentImpl(player),
                RespawnCopyStrategy.ALWAYS_COPY
        );

        // Register nightmare instance – not copied on death/respawn
        registry.registerForPlayers(
                NIGHTMARE_INSTANCE,
                player -> new NightmareInstanceImpl(),
                RespawnCopyStrategy.NEVER_COPY
        );
    }
}