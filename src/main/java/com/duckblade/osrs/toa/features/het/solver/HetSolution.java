package com.duckblade.osrs.toa.features.het.solver;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum HetSolution
{

	// basically we trace a line through the room checking for all these specific states
	SOLUTION_1(new HetTileState[]{
		new HetTileState(5, 10, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_EAST),
		new HetTileState(5, 11, HetTileState.STATE_EMPTY),
		new HetTileState(5, 12, HetTileState.STATE_EMPTY),
		new HetTileState(5, 13, HetTileState.STATE_EMPTY),
		new HetTileState(5, 14, HetTileState.STATE_EMPTY),
		new HetTileState(5, 15, HetTileState.STATE_EMPTY),
		new HetTileState(5, 16, HetTileState.STATE_EMPTY),
		new HetTileState(5, 17, HetTileState.STATE_EMPTY),
		new HetTileState(5, 18, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_EAST),
		new HetTileState(6, 18, HetTileState.STATE_EMPTY),
		new HetTileState(7, 18, HetTileState.STATE_EMPTY),
		new HetTileState(8, 18, HetTileState.STATE_EMPTY),
		new HetTileState(9, 18, HetTileState.STATE_EMPTY),
		new HetTileState(10, 18, HetTileState.STATE_EMPTY),
		new HetTileState(11, 18, HetTileState.STATE_EMPTY),
		new HetTileState(12, 18, HetTileState.STATE_EMPTY),
		new HetTileState(13, 18, HetTileState.STATE_EMPTY),
		new HetTileState(14, 18, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_WEST),
		new HetTileState(14, 17, HetTileState.STATE_EMPTY),
		new HetTileState(14, 16, HetTileState.STATE_EMPTY),
		new HetTileState(14, 15, HetTileState.STATE_EMPTY),
		new HetTileState(14, 14, HetTileState.STATE_EMPTY),
		new HetTileState(14, 13, HetTileState.STATE_EMPTY),
		new HetTileState(14, 12, HetTileState.STATE_EMPTY),
	}),
	SOLUTION_2(new HetTileState[]{
		new HetTileState(5, 10, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_EAST),
		new HetTileState(5, 9, HetTileState.STATE_EMPTY),
		new HetTileState(5, 8, HetTileState.STATE_EMPTY),
		new HetTileState(5, 7, HetTileState.STATE_EMPTY),
		new HetTileState(5, 6, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_EAST),
		new HetTileState(6, 6, HetTileState.STATE_EMPTY),
		new HetTileState(7, 6, HetTileState.STATE_EMPTY),
		new HetTileState(8, 6, HetTileState.STATE_EMPTY),
		new HetTileState(9, 6, HetTileState.STATE_EMPTY),
		new HetTileState(10, 6, HetTileState.STATE_EMPTY),
		new HetTileState(11, 6, HetTileState.STATE_EMPTY),
		new HetTileState(12, 6, HetTileState.STATE_EMPTY),
		new HetTileState(13, 6, HetTileState.STATE_EMPTY),
		new HetTileState(14, 6, HetTileState.STATE_EMPTY),
		new HetTileState(15, 6, HetTileState.STATE_EMPTY),
		new HetTileState(16, 6, HetTileState.STATE_EMPTY),
		new HetTileState(17, 6, HetTileState.STATE_EMPTY),
		new HetTileState(18, 6, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_WEST),
		new HetTileState(18, 7, HetTileState.STATE_EMPTY),
		new HetTileState(18, 8, HetTileState.STATE_EMPTY),
		new HetTileState(18, 9, HetTileState.STATE_EMPTY),
		new HetTileState(18, 10, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_WEST),
		new HetTileState(17, 10, HetTileState.STATE_EMPTY),
		new HetTileState(16, 10, HetTileState.STATE_EMPTY),
		new HetTileState(15, 10, HetTileState.STATE_EMPTY),
	}),
	SOLUTION_3(new HetTileState[]{
		new HetTileState(5, 10, HetTileState.STATE_EMPTY),
		new HetTileState(4, 10, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_EAST),
		new HetTileState(4, 9, HetTileState.STATE_EMPTY),
		new HetTileState(4, 8, HetTileState.STATE_EMPTY),
		new HetTileState(4, 7, HetTileState.STATE_EMPTY),
		new HetTileState(4, 6, HetTileState.STATE_EMPTY),
		new HetTileState(4, 5, HetTileState.STATE_EMPTY),
		new HetTileState(4, 4, HetTileState.STATE_EMPTY),
		new HetTileState(4, 3, HetTileState.STATE_EMPTY),
		new HetTileState(4, 2, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_EAST),
		new HetTileState(5, 2, HetTileState.STATE_EMPTY),
		new HetTileState(6, 2, HetTileState.STATE_EMPTY),
		new HetTileState(7, 2, HetTileState.STATE_EMPTY),
		new HetTileState(8, 2, HetTileState.STATE_EMPTY),
		new HetTileState(9, 2, HetTileState.STATE_EMPTY),
		new HetTileState(10, 2, HetTileState.STATE_EMPTY),
		new HetTileState(11, 2, HetTileState.STATE_EMPTY),
		new HetTileState(12, 2, HetTileState.STATE_EMPTY),
		new HetTileState(13, 2, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_WEST),
		new HetTileState(13, 3, HetTileState.STATE_EMPTY),
		new HetTileState(13, 4, HetTileState.STATE_EMPTY),
		new HetTileState(13, 5, HetTileState.STATE_EMPTY),
		new HetTileState(13, 6, HetTileState.STATE_EMPTY),
		new HetTileState(13, 7, HetTileState.STATE_EMPTY),
		new HetTileState(13, 8, HetTileState.STATE_EMPTY),
	}),
	SOLUTION_4(new HetTileState[]{
		new HetTileState(5, 10, HetTileState.STATE_EMPTY),
		new HetTileState(4, 10, HetTileState.STATE_EMPTY),
		new HetTileState(3, 10, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_EAST),
		new HetTileState(3, 9, HetTileState.STATE_EMPTY),
		new HetTileState(3, 8, HetTileState.STATE_EMPTY),
		new HetTileState(3, 7, HetTileState.STATE_EMPTY),
		new HetTileState(3, 6, HetTileState.STATE_EMPTY),
		new HetTileState(3, 5, HetTileState.STATE_EMPTY),
		new HetTileState(3, 4, HetTileState.STATE_EMPTY),
		new HetTileState(3, 3, HetTileState.STATE_EMPTY),
		new HetTileState(3, 2, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_EAST),
		new HetTileState(4, 2, HetTileState.STATE_EMPTY),
		new HetTileState(5, 2, HetTileState.STATE_EMPTY),
		new HetTileState(6, 2, HetTileState.STATE_EMPTY),
		new HetTileState(7, 2, HetTileState.STATE_EMPTY),
		new HetTileState(8, 2, HetTileState.STATE_EMPTY),
		new HetTileState(9, 2, HetTileState.STATE_EMPTY),
		new HetTileState(10, 2, HetTileState.STATE_EMPTY),
		new HetTileState(11, 2, HetTileState.STATE_EMPTY),
		new HetTileState(12, 2, HetTileState.STATE_EMPTY),
		new HetTileState(13, 2, HetTileState.STATE_EMPTY),
		new HetTileState(14, 2, HetTileState.STATE_EMPTY),
		new HetTileState(15, 2, HetTileState.STATE_EMPTY),
		new HetTileState(16, 2, HetTileState.STATE_EMPTY),
		new HetTileState(17, 2, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_WEST),
		new HetTileState(17, 3, HetTileState.STATE_EMPTY),
		new HetTileState(17, 4, HetTileState.STATE_EMPTY),
		new HetTileState(17, 5, HetTileState.STATE_EMPTY),
		new HetTileState(17, 6, HetTileState.STATE_EMPTY),
		new HetTileState(17, 7, HetTileState.STATE_EMPTY),
		new HetTileState(17, 8, HetTileState.STATE_EMPTY),
		new HetTileState(17, 9, HetTileState.STATE_EMPTY),
		new HetTileState(17, 10, HetTileState.STATE_EMPTY),
		new HetTileState(17, 11, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_WEST),
		new HetTileState(16, 11, HetTileState.STATE_EMPTY),
		new HetTileState(15, 11, HetTileState.STATE_EMPTY),
	}),
	SOLUTION_5(new HetTileState[]{
		new HetTileState(5, 10, HetTileState.STATE_EMPTY),
		new HetTileState(4, 10, HetTileState.STATE_EMPTY),
		new HetTileState(3, 10, HetTileState.STATE_EMPTY),
		new HetTileState(2, 10, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_EAST),
		new HetTileState(2, 11, HetTileState.STATE_EMPTY),
		new HetTileState(2, 12, HetTileState.STATE_EMPTY),
		new HetTileState(2, 13, HetTileState.STATE_EMPTY),
		new HetTileState(2, 14, HetTileState.STATE_EMPTY),
		new HetTileState(2, 15, HetTileState.STATE_EMPTY),
		new HetTileState(2, 16, HetTileState.STATE_EMPTY),
		new HetTileState(2, 17, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_EAST),
		new HetTileState(3, 17, HetTileState.STATE_EMPTY),
		new HetTileState(4, 17, HetTileState.STATE_EMPTY),
		new HetTileState(5, 17, HetTileState.STATE_EMPTY),
		new HetTileState(6, 17, HetTileState.STATE_EMPTY),
		new HetTileState(7, 17, HetTileState.STATE_EMPTY),
		new HetTileState(8, 17, HetTileState.STATE_EMPTY),
		new HetTileState(9, 17, HetTileState.STATE_EMPTY),
		new HetTileState(10, 17, HetTileState.STATE_EMPTY),
		new HetTileState(11, 17, HetTileState.STATE_EMPTY),
		new HetTileState(12, 17, HetTileState.STATE_EMPTY),
		new HetTileState(13, 17, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_WEST),
		new HetTileState(13, 16, HetTileState.STATE_EMPTY),
		new HetTileState(13, 15, HetTileState.STATE_EMPTY),
		new HetTileState(13, 14, HetTileState.STATE_EMPTY),
		new HetTileState(13, 13, HetTileState.STATE_EMPTY),
		new HetTileState(13, 12, HetTileState.STATE_EMPTY),
	}),
	SOLUTION_6(new HetTileState[]{
		new HetTileState(5, 10, HetTileState.STATE_EMPTY),
		new HetTileState(4, 10, HetTileState.STATE_EMPTY),
		new HetTileState(3, 10, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_EAST),
		new HetTileState(3, 11, HetTileState.STATE_EMPTY),
		new HetTileState(3, 12, HetTileState.STATE_EMPTY),
		new HetTileState(3, 13, HetTileState.STATE_EMPTY),
		new HetTileState(3, 14, HetTileState.STATE_EMPTY),
		new HetTileState(3, 15, HetTileState.STATE_EMPTY),
		new HetTileState(3, 16, HetTileState.STATE_EMPTY),
		new HetTileState(3, 17, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_EAST),
		new HetTileState(4, 17, HetTileState.STATE_EMPTY),
		new HetTileState(5, 17, HetTileState.STATE_EMPTY),
		new HetTileState(6, 17, HetTileState.STATE_EMPTY),
		new HetTileState(7, 17, HetTileState.STATE_EMPTY),
		new HetTileState(8, 17, HetTileState.STATE_EMPTY),
		new HetTileState(9, 17, HetTileState.STATE_EMPTY),
		new HetTileState(10, 17, HetTileState.STATE_EMPTY),
		new HetTileState(11, 17, HetTileState.STATE_EMPTY),
		new HetTileState(12, 17, HetTileState.STATE_EMPTY),
		new HetTileState(13, 17, HetTileState.STATE_EMPTY),
		new HetTileState(14, 17, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_WEST),
		new HetTileState(14, 16, HetTileState.STATE_EMPTY),
		new HetTileState(14, 15, HetTileState.STATE_EMPTY),
		new HetTileState(14, 14, HetTileState.STATE_EMPTY),
		new HetTileState(14, 13, HetTileState.STATE_EMPTY),
		new HetTileState(14, 12, HetTileState.STATE_EMPTY),
	}),
	SOLUTION_7(new HetTileState[]{
		new HetTileState(5, 10, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_EAST),
		new HetTileState(5, 11, HetTileState.STATE_EMPTY),
		new HetTileState(5, 12, HetTileState.STATE_EMPTY),
		new HetTileState(5, 13, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_EAST),
		new HetTileState(6, 13, HetTileState.STATE_EMPTY),
		new HetTileState(7, 13, HetTileState.STATE_EMPTY),
		new HetTileState(8, 13, HetTileState.STATE_EMPTY),
		new HetTileState(9, 13, HetTileState.STATE_EMPTY),
		new HetTileState(10, 13, HetTileState.STATE_EMPTY),
		new HetTileState(11, 13, HetTileState.STATE_EMPTY),
		new HetTileState(12, 13, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_WEST),
		new HetTileState(12, 12, HetTileState.STATE_EMPTY),
	}),
	SOLUTION_8(new HetTileState[]{
		new HetTileState(5, 10, HetTileState.STATE_EMPTY),
		new HetTileState(4, 10, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_EAST),
		new HetTileState(4, 9, HetTileState.STATE_EMPTY),
		new HetTileState(4, 8, HetTileState.STATE_EMPTY),
		new HetTileState(4, 7, HetTileState.STATE_EMPTY),
		new HetTileState(4, 6, HetTileState.STATE_EMPTY),
		new HetTileState(4, 5, HetTileState.STATE_EMPTY),
		new HetTileState(4, 4, HetTileState.STATE_EMPTY),
		new HetTileState(4, 3, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_EAST),
		new HetTileState(5, 3, HetTileState.STATE_EMPTY),
		new HetTileState(6, 3, HetTileState.STATE_EMPTY),
		new HetTileState(7, 3, HetTileState.STATE_EMPTY),
		new HetTileState(8, 3, HetTileState.STATE_EMPTY),
		new HetTileState(9, 3, HetTileState.STATE_EMPTY),
		new HetTileState(10, 3, HetTileState.STATE_EMPTY),
		new HetTileState(11, 3, HetTileState.STATE_EMPTY),
		new HetTileState(12, 3, HetTileState.STATE_EMPTY),
		new HetTileState(13, 3, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_WEST),
		new HetTileState(13, 4, HetTileState.STATE_EMPTY),
		new HetTileState(13, 5, HetTileState.STATE_EMPTY),
		new HetTileState(13, 6, HetTileState.STATE_EMPTY),
		new HetTileState(13, 7, HetTileState.STATE_EMPTY),
		new HetTileState(13, 8, HetTileState.STATE_EMPTY),
	}),
	SOLUTION_9(new HetTileState[]{
		new HetTileState(5, 10, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_SOUTH_EAST),
		new HetTileState(5, 9, HetTileState.STATE_EMPTY),
		new HetTileState(5, 8, HetTileState.STATE_EMPTY),
		new HetTileState(5, 7, HetTileState.STATE_EMPTY),
		new HetTileState(5, 6, HetTileState.STATE_EMPTY),
		new HetTileState(5, 5, HetTileState.STATE_EMPTY),
		new HetTileState(5, 4, HetTileState.STATE_EMPTY),
		new HetTileState(5, 3, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_EAST),
		new HetTileState(6, 3, HetTileState.STATE_EMPTY),
		new HetTileState(7, 3, HetTileState.STATE_EMPTY),
		new HetTileState(8, 3, HetTileState.STATE_EMPTY),
		new HetTileState(9, 3, HetTileState.STATE_EMPTY),
		new HetTileState(10, 3, HetTileState.STATE_EMPTY),
		new HetTileState(11, 3, HetTileState.STATE_EMPTY),
		new HetTileState(12, 3, HetTileState.STATE_EMPTY),
		new HetTileState(13, 3, HetTileState.STATE_MIRROR_MOVABLE, HetTileState.ORIENTATION_NORTH_WEST),
		new HetTileState(13, 4, HetTileState.STATE_EMPTY),
		new HetTileState(13, 5, HetTileState.STATE_EMPTY),
		new HetTileState(13, 6, HetTileState.STATE_EMPTY),
		new HetTileState(13, 7, HetTileState.STATE_EMPTY),
		new HetTileState(13, 8, HetTileState.STATE_EMPTY),
	}),
	;

	private final HetTileState[] solutionStates;

	@Getter(onMethod_ = @VisibleForTesting)
	private HetSolutionResult lastScore;

	// sum of all conversion scores (number of actions) required
	public HetSolutionResult getScore(HetTileState[][] roomStates)
	{
		HetSolutionResult acc = new HetSolutionResult(true, 0, 0, 0, new ArrayList<>());
		for (HetTileState solutionState : solutionStates)
		{
			// find the matching state
			HetTileState roomState = roomStates[solutionState.getX()][solutionState.getY()];
			if (roomState == null)
			{
				// if the plugin didn't populate this entry then there was no object on that tile
				roomState = new HetTileState(solutionState.getX(), solutionState.getY(), HetTileState.STATE_EMPTY);
			}

			// sanity check the result here, MAX_VALUE indicates it is impossible
			HetSolutionResult score = roomState.conversionScoreTo(solutionState);
			if (!score.isCompletable())
			{
				return (this.lastScore = new HetSolutionResult(false, solutionState.getX(), solutionState.getY(), 0, roomState));
			}

			acc.add(score);
		}

		return (this.lastScore = acc);
	}

}
