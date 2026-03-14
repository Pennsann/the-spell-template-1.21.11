package ss.spellid.components;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.component.ComponentKey;
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

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        TheSpell.LOGGER.info("Starting registration of rank and essence components");
        TheSpell.LOGGER.info("RANK_KEY: " + RANK_KEY.getId());
        TheSpell.LOGGER.info("ESSENCE_KEY: " + ESSENCE.getId());

        registry.registerForPlayers(
                RANK_KEY,
                player -> {
                    return new RankComponentImpl(player);
                },
                RespawnCopyStrategy.ALWAYS_COPY
        );

        registry.registerForPlayers(
                ESSENCE,
                player -> {
                    return new EssenceComponentImpl(player);
                },
                RespawnCopyStrategy.ALWAYS_COPY
        );

        TheSpell.LOGGER.info("Rank and essence components successfully registered");
    }
}