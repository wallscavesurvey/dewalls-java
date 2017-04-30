package org.andork.walls.srv;

import org.andork.unit.UnitizedDouble;

public class VarianceOverride {
	public static final VarianceOverride FLOATED = new VarianceOverride() {
		@Override
		public String toString() {
			return "?";
		}
	};

	public static final VarianceOverride FLOATED_TRAVERSE = new VarianceOverride() {
		@Override
		public String toString() {
			return "*";
		}
	};

	public static class Length extends VarianceOverride {
		public final UnitizedDouble<org.andork.unit.Length> lengthOverride;

		public Length(UnitizedDouble<org.andork.unit.Length> lengthOverride) {
			super();
			this.lengthOverride = lengthOverride;
		}

		@Override
		public String toString() {
			return lengthOverride.toString();
		}
	}

	public static class RMSError extends VarianceOverride {
		public final UnitizedDouble<org.andork.unit.Length> error;

		public RMSError(UnitizedDouble<org.andork.unit.Length> error) {
			super();
			this.error = error;
		}

		@Override
		public String toString() {
			return "R" + error.toString();
		}
	}
}
