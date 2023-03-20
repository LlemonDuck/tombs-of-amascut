package com.duckblade.osrs.toa.features.het.solver;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HetSolutionResult implements Comparable<HetSolutionResult>
{

	private static final int MAX_MIRRORS = 3;
	private static final Comparator<HetSolutionResult> COMPARATOR =
		Comparator.<HetSolutionResult>comparingInt(hs -> hs.completable ? Integer.MIN_VALUE : Integer.MAX_VALUE)
			.thenComparingInt(HetSolutionResult::getMirrorsToMove)
			.thenComparingInt(HetSolutionResult::getWallsToBreak)
			.thenComparingInt(HetSolutionResult::getMirrorsToAlter);

	private boolean completable;
	private int mirrorsToMove;
	private int mirrorsToAlter; // rotate or clean
	private int wallsToBreak;

	private List<HetTileState> incorrectStates;

	public HetSolutionResult(boolean completable, int mirrorsToMove, int mirrorsToAlter, int wallsToBreak, HetTileState wrongState)
	{
		this(
			completable,
			mirrorsToMove,
			mirrorsToAlter,
			wallsToBreak,
			wrongState != null ? Collections.singletonList(wrongState) : Collections.emptyList()
		);
	}

	public void add(HetSolutionResult other)
	{
		this.mirrorsToMove += other.mirrorsToMove;
		this.mirrorsToAlter += other.mirrorsToAlter;
		this.wallsToBreak += other.wallsToBreak;
		this.completable = this.completable && other.completable && (this.mirrorsToMove <= MAX_MIRRORS);
		this.incorrectStates.addAll(other.incorrectStates);
	}

	@Override
	public int compareTo(HetSolutionResult o)
	{
		return COMPARATOR.compare(this, o);
	}

	@VisibleForTesting
	public String getOverlayString()
	{
		if (!this.completable)
		{
			if (this.incorrectStates.isEmpty())
			{
				return " FAIL ???";
			}

			HetTileState lastBadState = this.incorrectStates.get(this.incorrectStates.size() - 1);
			return " FAIL " + lastBadState.getX() + ", " + lastBadState.getY();
		}
		return "M" + mirrorsToMove + " / W" + wallsToBreak + " / A" + mirrorsToAlter;
	}
}
