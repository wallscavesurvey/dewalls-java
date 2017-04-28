package org.andork.walls;

import java.util.Date;
import java.util.List;

public class AbstractWallsVisitor implements WallsVisitor {
	@Override
	public void parsedVector(Vector parsedVector) {
	}

	@Override
	public void parsedFixStation(FixedStation station) {
	}

	@Override
	public void parsedComment(String parsedComment) {
	}

	@Override
	public void parsedNote(String station, String parsedNote) {
	}

	@Override
	public void parsedDate(Date date) {
	}

	@Override
	public void parsedFlag(List<String> stations, String flag) {
	}

	@Override
	public void willParseUnits() {
	}

	@Override
	public void parsedUnits() {
	}

	@Override
	public void parsedSegment(String segment) {
	}

	@Override
	public void message(WallsMessage message) {
	}
}
