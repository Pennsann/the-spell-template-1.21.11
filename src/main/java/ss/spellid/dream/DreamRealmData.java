package ss.spellid.dream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import ss.spellid.TheSpell;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DreamRealmData {
    private static final String CITADELS_PATH = "data/the-spell/dream_realm/citadels.json";
    private static List<BlockPos> citadels = null;

    public static void load() {
        if (citadels != null) return;

        try (InputStream stream = DreamRealmData.class.getClassLoader().getResourceAsStream(CITADELS_PATH)) {
            if (stream == null) {
                TheSpell.LOGGER.error("Could not find citadels.json");
                citadels = new ArrayList<>();
                return;
            }

            JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));
            JsonObject root = element.getAsJsonObject();
            Type listType = new TypeToken<List<int[]>>(){}.getType();
            List<int[]> rawList = new Gson().fromJson(root.get("citadels"), listType);

            citadels = new ArrayList<>();
            for (int[] arr : rawList) {
                if (arr.length >= 3) {
                    citadels.add(new BlockPos(arr[0], arr[1], arr[2]));
                }
            }
            TheSpell.LOGGER.info("Loaded {} Dream Realm citadels", citadels.size());
        } catch (Exception e) {
            TheSpell.LOGGER.error("Failed to load citadels.json", e);
            citadels = new ArrayList<>();
        }
    }

    public static BlockPos getRandomCitadel() {
        if (citadels == null) load();
        if (citadels.isEmpty()) return BlockPos.ZERO; // fallback
        return citadels.get(new Random().nextInt(citadels.size()));
    }
}