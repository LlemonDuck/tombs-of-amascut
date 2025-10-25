package com.duckblade.osrs.toa.features.pointstracker;

import java.io.FileWriter;
import java.io.PrintWriter;
import net.runelite.client.eventbus.EventBus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestPurpleWeightingGrapher
{

	@Mock
	PointsTracker pointsTracker;

	@Mock
	EventBus eventBus;

	@InjectMocks
	PurpleWeightingManager purpleWeightingManager;

	/**
	 * dumps a table of data for import into google sheets/excel
	 */
	@Test
	@Disabled
	public void dumpPurpleWeightTables() throws Exception
	{
		when(pointsTracker.getUniqueChance()).thenReturn(0.0);

		// CHECKSTYLE:OFF
		try (PrintWriter weightsWriter = new PrintWriter(new FileWriter("purple_weights.tsv"));
			 PrintWriter percentsWriter = new PrintWriter(new FileWriter("purple_percents.tsv")))
		// CHECKSTYLE:ON
		{
			weightsWriter.print("Raid Level\t");
			percentsWriter.print("Raid Level\t");
			for (Purple p : Purple.values())
			{
				weightsWriter.print(p.getItemName() + "\t");
				percentsWriter.print(p.getItemName() + "\t");
			}
			weightsWriter.println();
			percentsWriter.println();

			for (int raidLevel = 0; raidLevel <= 600; raidLevel += 5)
			{
				purpleWeightingManager.raidLevel = raidLevel;
				purpleWeightingManager.reweight();

				weightsWriter.print(raidLevel + "\t");
				percentsWriter.print(raidLevel + "\t");
				for (Purple p : Purple.values())
				{
					PurpleWeightingManager.PurpleWeighting weighting = purpleWeightingManager.getWeighting(p);
					weightsWriter.print(weighting.getWeight() + "\t");
					percentsWriter.print(String.format("%.3f\t", weighting.getPurplePercent()));
				}
				weightsWriter.println();
				percentsWriter.println();
			}
		}
	}

}
