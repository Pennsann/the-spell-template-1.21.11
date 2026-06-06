package ss.spellid.components;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;

public class FlawComponentImpl implements FlawComponent {
    private final Map<String, Integer> ints = new HashMap<>();
    private final Map<String, Long> longs = new HashMap<>();

    @Override
    public void setInt(String key, int value) {
        ints.put(key, value);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return ints.getOrDefault(key, defaultValue);
    }

    @Override
    public void setLong(String key, long value) {
        longs.put(key, value);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return longs.getOrDefault(key, defaultValue);
    }

    @Override
    public void writeData(ValueOutput output) {
        // Write ints
        output.putInt("IntCount", ints.size());
        int i = 0;
        for (Map.Entry<String, Integer> entry : ints.entrySet()) {
            output.putString("IntKey_" + i, entry.getKey());
            output.putInt("IntVal_" + i, entry.getValue());
            i++;
        }
        // Write longs
        output.putInt("LongCount", longs.size());
        i = 0;
        for (Map.Entry<String, Long> entry : longs.entrySet()) {
            output.putString("LongKey_" + i, entry.getKey());
            output.putLong("LongVal_" + i, entry.getValue());
            i++;
        }
    }

    @Override
    public void readData(ValueInput input) {
        ints.clear();
        longs.clear();
        int intCount = input.getInt("IntCount").orElse(0);
        for (int i = 0; i < intCount; i++) {
            String key = input.getString("IntKey_" + i).orElse("");
            int value = input.getInt("IntVal_" + i).orElse(0);
            if (!key.isEmpty()) ints.put(key, value);
        }
        int longCount = input.getInt("LongCount").orElse(0);
        for (int i = 0; i < longCount; i++) {
            String key = input.getString("LongKey_" + i).orElse("");
            long value = input.getLong("LongVal_" + i).orElse(0L);
            if (!key.isEmpty()) longs.put(key, value);
        }
    }
}