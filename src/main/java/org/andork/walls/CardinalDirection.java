package org.andork.walls;
import org.andork.unit.Angle;
import org.andork.unit.UnitizedDouble;

public enum CardinalDirection {
	NORTH(new UnitizedDouble<>(0, Angle.degrees)), EAST(new UnitizedDouble<>(90, Angle.degrees)), SOUTH(
			new UnitizedDouble<>(180, Angle.degrees)), WEST(new UnitizedDouble<>(270, Angle.degrees));

	private static final UnitizedDouble<Angle> THREESIXTY = new UnitizedDouble<>(360, Angle.degrees);

	public final UnitizedDouble<Angle> angle;

	private CardinalDirection(UnitizedDouble<Angle> angle) {
		this.angle = angle;
	}

	private UnitizedDouble<Angle> nonnormQuadrant(CardinalDirection to, UnitizedDouble<Angle> rotation) {
		if (to.ordinal() == (ordinal() + 1) % 4) {
			return angle.add(rotation);
		} else if (ordinal() == (to.ordinal() + 1) % 4) {
			return angle.sub(rotation);
		}
		throw new IllegalArgumentException("invalid from/to combination");
	}

	public UnitizedDouble<Angle> quadrant(CardinalDirection to, UnitizedDouble<Angle> rotation) {
		UnitizedDouble<Angle> result = nonnormQuadrant(to, rotation).mod(THREESIXTY);
		return result.doubleValue(result.unit) < 0
				? result.add(THREESIXTY)
				: result;
	}
}
