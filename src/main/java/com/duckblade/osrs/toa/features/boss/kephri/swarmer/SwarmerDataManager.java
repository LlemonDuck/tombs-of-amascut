package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import com.duckblade.osrs.toa.TombsOfAmascutPlugin;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SwarmerDataManager implements PluginLifecycleComponent
{

	private static final int MAX_RECENT_RAIDS = 10;
	public static final Path SWARMS_DIRECTORY = new File(TombsOfAmascutPlugin.TOA_FOLDER, "kephri-swarms").toPath();

	private final Gson gson;

	private ExecutorService executor;

	@Override
	public void startUp()
	{
		executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("ToA-SwarmerDataManager-%d").build());
	}

	@Override
	public void shutDown()
	{
		executor.shutdown();
	}

	public CompletableFuture<List<String>> getRaidList()
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try
			{
				if (!Files.exists(SWARMS_DIRECTORY))
				{
					return Collections.emptyList();
				}

				try (Stream<Path> files = Files.list(SWARMS_DIRECTORY))
				{
					return files.filter(f -> f.getFileName().toString().endsWith(".json"))
						.sorted(Comparator.reverseOrder())
						.limit(MAX_RECENT_RAIDS)
						.map(f -> f.getFileName().toString().replace(".json", ""))
						.map(s -> s.replace('_', ':'))
						.collect(Collectors.toList());
				}
			}
			catch (Exception ignored)
			{
			}

			return Collections.emptyList();
		}, executor);
	}

	public CompletableFuture<List<SwarmerRoomData>> getRaidData(String raidUnsafe)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			String raid = raidUnsafe.replace(':', '_');

			if (!Files.exists(SWARMS_DIRECTORY))
			{
				return Collections.emptyList();
			}
			if (!Files.exists(SWARMS_DIRECTORY.resolve(raid + ".json")))
			{
				return Collections.emptyList();
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
				return Collections.emptyList();
			}
		}, executor);
	}

	public CompletableFuture<Void> saveRaidData(List<SwarmerRoomData> raidDataList)
	{
		return CompletableFuture.runAsync(() ->
		{
			String raidName = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss").format(new Date());
			try
			{
				if (!Files.exists(SWARMS_DIRECTORY))
				{
					Files.createDirectories(SWARMS_DIRECTORY);
				}
				Files.writeString(
					SWARMS_DIRECTORY.resolve(raidName + ".json"),
					gson.toJson(raidDataList),
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING
				);
			}
			catch (Exception ignored)
			{
			}
		});
	}
}