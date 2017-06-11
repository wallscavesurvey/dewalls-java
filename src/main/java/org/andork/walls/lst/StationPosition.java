package org.andork.walls.lst;

import org.andork.segment.Segment;

public class StationPosition {
	Segment prefix;
	Segment name;
	double east = Double.NaN;
	double north = Double.NaN;
	double up = Double.NaN;
	Segment note;

	public String getNameWithPrefix() {
		if (name != null && prefix != null && !prefix.isEmpty()) {
			return prefix + ":" + name;
		}
		return name == null ? null : name.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StationPosition [prefix=").append(prefix).append(", name=").append(name).append(", east=")
				.append(east).append(", north=").append(north).append(", up=").append(up).append(", note=").append(note)
				.append("]");
		return builder.toString();
	}
}
