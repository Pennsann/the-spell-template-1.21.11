package ss.spellid.aspect.ability;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.Vec3;
import ss.spellid.TheSpell;

public class FireballAbility implements AspectAbility {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(TheSpell.MOD_ID, "fireball");
    private static final int COOLDOWN_TICKS = 40; // 2 seconds
    private static final int ESSENCE_COST = 20;

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public int getCooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    public int getEssenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public boolean canUse(ServerPlayer player) {
        return true;
    }

    @Override
    public void use(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        // Use the constructor that takes the owner and a Vec3 direction
        SmallFireball fireball = new SmallFireball(player.level(), player, look);
        // Position it slightly in front of the player's eyes
        fireball.setPos(player.getX(), player.getEyeY() - 0.2, player.getZ());
        player.level().addFreshEntity(fireball);
    }
}