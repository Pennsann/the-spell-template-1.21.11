package ss.spellid.Power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

public record PlayerPowerData(Map<String, Integer> powerLevels) {

    //Codec for saving
    public static final Codec<PlayerPowerData>
            CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf
                            ("PowerLevels").forGetter(PlayerPowerData::powerLevels)
            ).apply(instance, PlayerPowerData::new));
    //Default empty data :D
    public static final PlayerPowerData EMPTY = new PlayerPowerData(new HashMap<>());

    //Helper for rank accending
    //REMEBER 0 IS PLAYER and 1 is awakened and so on WO WOWOWO
    public Integer getLevel(String powerId) {
        return powerLevels.getOrDefault(powerId, 0);
    }

    //Set/update level for a powa
    public PlayerPowerData withLevel(String powerId, int level) {
        Map<String,Integer> newLevels = new HashMap<>(powerLevels);
        newLevels.put(powerId, level);
        return new PlayerPowerData(newLevels);
    }

}
