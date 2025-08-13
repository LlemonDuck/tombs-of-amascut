package com.duckblade.osrs.toa.features.het.solver;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.runelite.api.GameObject;
import net.runelite.api.NullObjectID;
import net.runelite.api.ObjectID;
import net.runelite.api.Point;

@Data
@RequiredArgsConstructor
public class HetTileState
{

	public static final int STATE_EMPTY = 0;
	public static final int STATE_MIRROR_MOVABLE = 1;
	public static final int STATE_MIRROR = 2;
	public static final int STATE_MIRROR_DIRTY = 3;
	public static final int STATE_WALL_BROKEN = 4;
	public static final int STATE_WALL_REMOVED = 5;
	public static final int STATE_WALL_STATIC = 6;

	public static final Map<Integer, Integer> OBJECT_ID_TO_STATE = ImmutableMap.<Integer, Integer>builder()
		.put(ObjectID.MIRROR_45455, STATE_MIRROR_MOVABLE)
		.put(ObjectID.MIRROR_45456, STATE_MIRROR)
		.put(ObjectID.MIRROR_DIRTY, STATE_MIRROR_DIRTY)
		.put(ObjectID.BARRIER_45458, STATE_WALL_STATIC)
		.put(ObjectID.BARRIER_45460, STATE_WALL_STATIC)
		.put(ObjectID.BARRIER_45462, STATE_WALL_BROKEN)
		.put(ObjectID.BARRIER_45464, STATE_WALL_BROKEN)
		.put(NullObjectID.NULL_45466, STATE_WALL_REMOVED) // wall mined, but floor rubble remains
		.put(NullObjectID.NULL_29733, STATE_EMPTY) // this object is placed to "clear" previous objects (e.g. mirror picked up, wall despawned)
		.build();

	public static final int ORIENTATION_NORTH_EAST = 0b00;
	public static final int ORIENTATION_SOUTH_EAST = 0b01;
	public static final int ORIENTATION_SOUTH_WEST = 0b10;
	public static final int ORIENTATION_NORTH_WEST = 0b11;

	// x and y in "puzzle-space" where (0, 0) is the bottom left corner of a 21x21 grid encompassing the puzzle space
	private final int x;
	private final int y;
	private final int state;
	private final int orientation;

	public HetTileState(int x, int y, int state)
	{
		this(x, y, state, ORIENTATION_NORTH_EAST);
	}

	public static HetTileState fromGameObject(GameObject o, Point puzzleBase)
	{
		Point p = o.getSceneMinLocation();
		return new HetTileState(
			p.getX() - puzzleBase.getX(),
			p.getY() - puzzleBase.getY(),
			OBJECT_ID_TO_STATE.getOrDefault(o.getId(), STATE_EMPTY),
			(o.getConfig() & 0b11000000) >> 6
		);
	}

	public HetSolutionResult conversionScoreTo(HetTileState other)
	{
		assert other != null;
		assert this.x == other.x;
		assert this.y == other.y;

		if (this.state == other.state)
		{
			// minor hack, i assume `other` will never be a wall
			return this.orientation == other.orientation ? new HetSolutionResult(true, 0, 0, 0, Collections.emptyList()) : new HetSolutionResult(
				true,
				0,
				1,
				0,
				other
			);
		}

		switch (this.state)
		{
			case STATE_WALL_STATIC:
				// we can't convert static walls to anything, obviously
				return new HetSolutionResult(false, 0, 0, 0, other);


			case STATE_MIRROR_DIRTY:
			case STATE_MIRROR:
				// STATE_MIRROR can take the place of a STATE_MIRROR_MOVABLE iff orientation matches
				// and fails the solution in all other cases
				// also, dirty mirrors are never movable, so they handle the same
				HetTileState maybeClean = this.state == STATE_MIRROR_DIRTY ? other : null;
				if (other.state == STATE_MIRROR_MOVABLE && this.orientation == other.orientation)
				{
					return new HetSolutionResult(true, 0, maybeClean == null ? 0 : 1, 0, maybeClean);
				}
				return new HetSolutionResult(false, 0, 0, 0, other);

			case STATE_EMPTY:
			case STATE_WALL_REMOVED:
			case STATE_WALL_BROKEN:
				HetTileState maybeWallRemove = this.state == STATE_WALL_BROKEN ? other : null;
				if (other.state == STATE_EMPTY)
				{
					// tile needs to be clear (but may already be clear)
					return new HetSolutionResult(true, 0, 0, maybeWallRemove == null ? 0 : 1, maybeWallRemove);
				}

				if (other.state == STATE_MIRROR_MOVABLE)
				{
					// mirror needs to be placed here (and maybe a wall cleared first too)
					return new HetSolutionResult(true, 1, 0, maybeWallRemove == null ? 0 : 1, other);
				}

			case STATE_MIRROR_MOVABLE:
				// assumption that other is EMPTY
				// don't count this as a mirror move, only count the destination as one to avoid double count
				return new HetSolutionResult(true, 0, 0, 0, other);

			default:
				// all possible combinations should have now already been reached
				return new HetSolutionResult(false, 0, 0, 0, other);
		}
	}

	// whether to reset the solution state when we see a state of this
	// i.e. is this state a human-actionable state update
	public boolean stateResetsSolution()
	{
		switch (state)
		{
			case STATE_EMPTY:
			case STATE_WALL_REMOVED:
			case STATE_MIRROR_MOVABLE:
			case STATE_MIRROR_DIRTY: // black orbs hitting mirrors can make them dirty
			case STATE_MIRROR: // clean action
				return false;

			// basically just if a wall spawns
			default:
				return true;
		}
	}

}
