package org.andork.walls;

import org.andork.segment.Segment;
import org.andork.segment.SegmentParseException;

public class WallsMessage {
	public final String severity;
	public final String message;
	public final Segment segment;

	public WallsMessage(String severity, String message, Segment segment) {
		this.severity = severity;
		this.message = message;
		this.segment = segment;
	}

	public WallsMessage(SegmentParseException ex) {
		this("error", ex.getLocalizedMessage(), ex.getSegment());
	}
}
