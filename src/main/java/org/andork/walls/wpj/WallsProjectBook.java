package org.andork.walls.wpj;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WallsProjectBook extends WallsProjectEntry {
	public WallsProjectBook(WallsProjectBook parent, String title) {
		super(parent, title);
	}

	final List<WallsProjectEntry> children = new ArrayList<>();
	final List<WallsProjectEntry> unmodifiableChildren = new ArrayList<>(children);
	
	public List<WallsProjectEntry> children() {
		return unmodifiableChildren;
	}

	public Path absolutePath() {
		return dir().toAbsolutePath().normalize();
	}

	public boolean isSurvey() {
		return false;
	}
}
