package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import com.duckblade.osrs.toa.TombsOfAmascutPlugin;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SwarmerDataManager
{

	private static final int MAX_RECENT_RAIDS = 10;
	public static final Path SWARMS_DIRECTORY = new File(TombsOfAmascutPlugin.TOA_FOLDER, "kephri-swarms").toPath();

	private final Gson gson;

	public List<String> getRaidList()
	{
		try
		{
			if (!Files.exists(SWARMS_DIRECTORY))
			{
				Files.createDirectories(SWARMS_DIRECTORY);
				return new ArrayList<>();
			}
		}
		catch (Exception ignored)
		{
			return new ArrayList<>();
		}

		ArrayList<String> raids = new ArrayList<>();
		try (Stream<Path> files = Files.list(SWARMS_DIRECTORY))
		{
			for (Path file : files.collect(toList()))
			{
				if (file.getFileName().toString().endsWith(".json"))
				{
					String raidName = file.getFileName().toString().replace(".json", "");
					raids.add(raidName);
				}
			}
		}
		catch (Exception ignored)
		{
			return new ArrayList<>();
		}

		raids.sort(Comparator.comparingLong(Long::parseLong));
		return raids.size() > MAX_RECENT_RAIDS ? raids.subList(raids.size() - MAX_RECENT_RAIDS, raids.size()) : raids;
	}

	public List<SwarmerRoomData> getRaidData(String raid)
	{
		raid = String.valueOf(java.sql.Timestamp.valueOf(raid).getTime() / 1000);
		try
		{
			if (!Files.exists(SWARMS_DIRECTORY))
			{
				Files.createDirectories(SWARMS_DIRECTORY);
				return new ArrayList<>();
			}
		}
		catch (Exception ignored)
		{
			return new ArrayList<>();
		}
		if (!Files.exists(SWARMS_DIRECTORY.resolve(raid + ".json")))
		{
			return new ArrayList<>();
		}
		try (FileReader reader = new FileReader(SWARMS_DIRECTORY.resolve(raid + ".json").toFile()))
		{
			Type listType = new TypeToken<List<SwarmerRoomData>>()
			{
			}.getType();
			return gson.fromJson(reader, listType);
		}
		catch (Exception ignored)
		{
		}
		return new ArrayList<>();
	}

	public void saveRaidData(List<SwarmerRoomData> raidDataList)
	{
		String raidName = String.valueOf((int) (System.currentTimeMillis() / 1000));
		try
		{
			if (!Files.exists(SWARMS_DIRECTORY))
			{
				Files.createDirectories(SWARMS_DIRECTORY);
			}
			if (!Files.exists(SWARMS_DIRECTORY.resolve(raidName + ".json")))
			{
				Files.createFile(SWARMS_DIRECTORY.resolve(raidName + ".json"));
			}
			Files.writeString(SWARMS_DIRECTORY.resolve(raidName + ".json"), gson.toJson(raidDataList));
		}
		catch (Exception ignored)
		{
		}
	}

}