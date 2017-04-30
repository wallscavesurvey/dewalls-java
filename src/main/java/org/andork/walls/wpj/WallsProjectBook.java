package org.andork.walls.wpj;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WallsProjectBook extends WallsProjectEntry {
	public WallsProjectBook(WallsProjectBook parent, String title) {
		super(parent, title);
	}

	final List<WallsProjectEntry> children = new ArrayList<>();

	public Path absolutePath() {
		return dir().toAbsolutePath().normalize();
	}

	public boolean isSurvey() {
		return false;
	}
}
