package org.andork.walls.lst;

import org.andork.segment.Segment;

public class StationPosition {
	public Segment prefix;
	public Segment name;
	public double east = Double.NaN;
	public double north = Double.NaN;
	public double up = Double.NaN;
	public Segment note;

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
