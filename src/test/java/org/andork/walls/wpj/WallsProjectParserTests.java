package org.andork.walls.wpj;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Function;

import org.andork.segment.SegmentParseException;
import org.andork.unit.Angle;
import org.andork.unit.Length;
import org.junit.Assert;
import org.junit.Test;

public class WallsProjectParserTests {

	@Test
	public void testParseKauaNorthMaze() throws Exception {
		WallsProjectParser parser = new WallsProjectParser();

		WallsProjectBook projectRoot = parser.parseFile(getClass().getResourceAsStream("Kaua North Maze.wpj"),
				"Kaua North Maze.wpj");

		Assert.assertTrue(parser.getMessages().isEmpty());

		Assert.assertNotNull(projectRoot);
		Assert.assertEquals("Actun Kaua - North Maze", projectRoot.title);
		Assert.assertEquals("KAUA-NM", Objects.toString(projectRoot.name, null));
		Assert.assertEquals(WallsProjectEntry.View.NorthOrEast, projectRoot.defaultViewAfterCompilation());
		Assert.assertFalse(projectRoot.preserveVertShotLength());
		Assert.assertFalse(projectRoot.preserveVertShotOrientation());
		Assert.assertTrue(projectRoot.deriveDeclFromDate());
		Assert.assertTrue(projectRoot.gridRelative());

		Assert.assertNotNull(projectRoot.reference());
		Assert.assertEquals(Length.meters(2308521.655), projectRoot.reference().northing);
		Assert.assertEquals(Length.meters(324341.706), projectRoot.reference().easting);
		Assert.assertEquals(16, projectRoot.reference().zone);
		Assert.assertEquals(Angle.degrees(-0.602), projectRoot.reference().gridConvergence);
		Assert.assertEquals(Length.meters(27), projectRoot.reference().elevation);

		Assert.assertEquals(5, projectRoot.children.size());

		WallsProjectEntry child0 = projectRoot.children.get(0);
		Assert.assertEquals("SVG Map Info - Please Read First!", child0.title);
		Assert.assertEquals("SVGINFO.TXT", child0.name.toString());
		Assert.assertTrue(child0.isOther());
		Assert.assertEquals(WallsProjectEntry.LaunchOptions.Edit, child0.launchOptions());

		WallsProjectEntry child1 = projectRoot.children.get(1);
		Assert.assertEquals("Flags and Notes", child1.title);
		Assert.assertEquals("NOTES", child1.name.toString());
		Assert.assertTrue(child1.isSurvey());

		WallsProjectEntry child2 = projectRoot.children.get(2);
		Assert.assertEquals("SVG Sources", child2.title);
		Assert.assertNull(child2.name);
		Assert.assertTrue(child2 instanceof WallsProjectBook);

		WallsProjectEntry child3 = projectRoot.children.get(3);
		Assert.assertEquals("Fixed Points", child3.title);
		Assert.assertNull(child3.name);
		Assert.assertTrue(child3 instanceof WallsProjectBook);

		WallsProjectEntry child4 = projectRoot.children.get(4);
		Assert.assertEquals("North Maze Surveys", child4.title);
		Assert.assertEquals("NORTH", child4.name.toString());
		Assert.assertTrue(child4 instanceof WallsProjectBook);
	}

	//
	//	namespace dewalls {
	//	namespace PathTests {
	//
	WallsProjectEntry entryAt(WallsProjectBook book, String titlePath) {
		String[] titleParts = titlePath.split("/");
		for (int i = 0; i < titleParts.length && book != null; i++) {
			WallsProjectEntry nextChild = null;
			for (WallsProjectEntry child : book.children) {
				if (titleParts[i].equals(child.title)) {
					nextChild = child;
					break;
				}
			}
			if (i == titleParts.length - 1) {
				return nextChild;
			} else if (nextChild != null && nextChild instanceof WallsProjectBook) {
				book = (WallsProjectBook) nextChild;
			} else {
				return null;
			}
		}
		return null;
	}

	String absolutePath(WallsProjectEntry entry) {
		return entry == null || entry.absolutePath() == null ? null : entry.absolutePath().toString();
	}

	void checkEntryPath(WallsProjectEntry entry, String path) {
		Assert.assertNotNull(entry);
		Assert.assertEquals(Paths.get(path).normalize(), entry.absolutePath());
	}

	//	} // namespace PathTests
	//	} // namespace dewalls
	//
	@Test
	public void testHandlesPathsCorrectly() throws SegmentParseException {
		WallsProjectParser parser = new WallsProjectParser();

		parser.parseLine(".book root");
		parser.parseLine(".path /rootdir");
		parser.parseLine(".book a");
		parser.parseLine(".book b");
		parser.parseLine(".path bdir");
		parser.parseLine(".book c");
		parser.parseLine(".endbook");
		parser.parseLine(".book d");
		parser.parseLine(".path ddir");
		parser.parseLine(".book e");
		parser.parseLine(".endbook"); //e
		parser.parseLine(".endbook"); //d
		parser.parseLine(".endbook"); //b
		parser.parseLine(".book f");
		parser.parseLine(".path fdir");
		parser.parseLine(".survey g");
		parser.parseLine(".name gsurvey");
		parser.parseLine(".endbook"); // f
		parser.parseLine(".book h");
		parser.parseLine(".path /hdir");
		parser.parseLine(".endbook"); // h
		parser.parseLine(".survey i");
		parser.parseLine(".endbook"); // a
		parser.parseLine(".endbook"); // root

		WallsProjectBook root = parser.result();
		
		String os = System.getProperty("os.name");
		Function<String, String> normalize = str -> str;
		if (os != null && os.toLowerCase().contains("win")) {
			normalize = str -> "C:" + str.replaceAll("/", "\\\\");
		}
				

		Assert.assertNotNull(root);
		Assert.assertEquals(normalize.apply("/rootdir"), absolutePath(root));
		Assert.assertEquals(normalize.apply("/rootdir"), absolutePath(entryAt(root, "a")));
		Assert.assertEquals(normalize.apply("/rootdir/bdir"), absolutePath(entryAt(root, "a/b")));
		Assert.assertEquals(normalize.apply("/rootdir/bdir"), absolutePath(entryAt(root, "a/b/c")));
		Assert.assertEquals(normalize.apply("/rootdir/bdir/ddir"), absolutePath(entryAt(root, "a/b/d")));
		Assert.assertEquals(normalize.apply("/rootdir/bdir/ddir"), absolutePath(entryAt(root, "a/b/d/e")));
		Assert.assertEquals(normalize.apply("/rootdir/fdir"), absolutePath(entryAt(root, "a/f")));
		Assert.assertEquals(normalize.apply("/rootdir/fdir/gsurvey.SRV"), absolutePath(entryAt(root, "a/f/g")));
		Assert.assertEquals(normalize.apply("/hdir"), absolutePath(entryAt(root, "a/h")));
		Assert.assertNull(absolutePath(entryAt(root, "a/i")));
	}
}
