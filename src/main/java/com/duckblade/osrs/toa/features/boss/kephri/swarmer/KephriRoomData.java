package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Getter
@Setter
public class KephriRoomData {
    private static final Logger log = LoggerFactory.getLogger(KephriRoomData.class);
    private int down;
    private int wave;
    private int leaks;

    public KephriRoomData(int down, int wave, int leaks) {
        this.down = down;
        this.wave = wave;
        this.leaks = leaks;
    }

    private static final int MAX_RECENT_RAIDS = 10;

    public static final String PLUGIN_DIRECTORY = System.getProperty("user.home").replace("\\", "/") + "/.runelite/tombs-of-amascut/swarmer";

    public static List<String> getRaidList() {
        try {
            if (!Files.exists(Path.of(PLUGIN_DIRECTORY))) {
                Files.createDirectories(Path.of(PLUGIN_DIRECTORY));
                return new ArrayList<>();
            }
        } catch (Exception ignored) {
            return new ArrayList<>();
        }

        ArrayList<String> raids = new ArrayList<>();
        try (Stream<Path> files = Files.list(Path.of(PLUGIN_DIRECTORY))) {
            for (Path file : files.collect(toList())) {
                if (file.getFileName().toString().endsWith(".json")) {
                    String raidName = file.getFileName().toString().replace(".json", "");
                    raids.add(raidName);
                }
            }
        }
        catch (Exception ignored) {
            return new ArrayList<>();
        }

        raids.sort(Comparator.comparingLong(Long::parseLong));
        return raids.size() > MAX_RECENT_RAIDS ? raids.subList(raids.size() - MAX_RECENT_RAIDS, raids.size()) : raids;

    }

    public static List<KephriRoomData> getRaidData(String raid) {
        raid = String.valueOf(java.sql.Timestamp.valueOf(raid).getTime() / 1000);
        try {
            if (!Files.exists(Path.of(PLUGIN_DIRECTORY))) {
                Files.createDirectories(Path.of(PLUGIN_DIRECTORY));
                return new ArrayList<>();
            }
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
        if (!Files.exists(Path.of(PLUGIN_DIRECTORY, raid+".json"))) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(Path.of(PLUGIN_DIRECTORY, raid+".json").toFile())) {
            Type listType = new TypeToken<List<KephriRoomData>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception ignored) {}
        return new ArrayList<>();
    }

    public static void saveRaidData(List<KephriRoomData> raidDataList) {
        String raidName = String.valueOf((int)(System.currentTimeMillis()/1000));
        Gson gson = new Gson();
        try {
            if (!Files.exists(Path.of(PLUGIN_DIRECTORY))) {
                Files.createDirectories(Path.of(PLUGIN_DIRECTORY));
            }
            if (!Files.exists(Path.of(PLUGIN_DIRECTORY, raidName + ".json"))) {
                Files.createFile(Path.of(PLUGIN_DIRECTORY, raidName + ".json"));
            }
            Files.writeString(Path.of(PLUGIN_DIRECTORY, raidName + ".json"), gson.toJson(raidDataList));
        } catch (Exception ignored) {}
    }

}