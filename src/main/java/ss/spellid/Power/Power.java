package ss.spellid.Power;

import java.util.function.Consumer;

public record Power(String id, int level, Consumer<PlayerPowerData> onActivate) {
}
