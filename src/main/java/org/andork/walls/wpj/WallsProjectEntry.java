package org.andork.walls.wpj;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.andork.segment.Segment;
import org.andork.unit.Length;
import org.andork.unit.Unit;

public class WallsProjectEntry {
	final WallsProjectBook parent;
	final String title;
	Segment name;
	Path path;
	int status;
	Segment options;
	GeoReference reference;

	public static enum LaunchOptions {
		Properties, Edit, Open;
	};

	public static enum View {
		NorthOrEast, NorthOrWest, North, East, West;
	};

	// status BITS
	//  2^0 : Type = Book
	//  2^1 : ?
	//  2^2 : ?
	//  2^3 : name defines segment
	//  2^4 : 1 = Feet, 0 = Meters
	//  2^5 : ?
	//  2^6 : reference unspecified
	//  2^7 : ?
	//Derive decl from date:
	//  2^8 : 1 = no
	//  2^9 : 1 = yes
	//UTM/GPS Grid-relative:
	//  2^10: 1 = no
	//  2^11: 1 = yes
	//Preserve vertical shot orientation:
	//  2^12: 1 = no
	//  2^13: 1 = yes
	//Preserve vertical shot length:
	//  2^14: 1 = no
	//  2^15: 1 = yes
	//Other type
	//  2^16: 1 = type is other
	//  2^17: edit on launch
	//  2^18: open on launch
	//Default view after compilation (bits 21-19):
	//     1: North or East
	//	   10: North or West
	//    11: North
	//   100: East
	//   101: West

	public static final int NameDefinesSegmentBit = 1 << 3;
	public static final int FeetBit = 1 << 4;
	public static final int ReferenceUnspecifiedBit = 1 << 6;
	public static final int DontDeriveDeclBit = 1 << 8;
	public static final int DeriveDeclBit = 1 << 9;
	public static final int NotGridRelativeBit = 1 << 10;
	public static final int GridRelativeBit = 1 << 11;
	public static final int DontPreserveVertShotOrientationBit = 1 << 12;
	public static final int PreserveVertShotOrientationBit = 1 << 13;
	public static final int DontPreserveVertShotLengthBit = 1 << 14;
	public static final int PreserveVertShotLengthBit = 1 << 15;
	public static final int OtherTypeBit = 1 << 16;
	public static final int EditOnLaunchBit = 1 << 17;
	public static final int OpenOnLaunchBit = 1 << 18;
	public static final int DefaultViewAfterCompilationMask = 7 << 19;
	public static final int NorthOrEastViewBits = 1 << 19;
	public static final int NorthOrWestViewBits = 2 << 19;
	public static final int NorthViewBits = 3 << 19;
	public static final int EastViewBits = 4 << 19;
	public static final int WestViewBits = 5 << 19;

	public WallsProjectEntry(WallsProjectBook parent, String title) {
		super();
		this.parent = parent;
		this.title = title;
	}

	public boolean isOther() {
		return (status & OtherTypeBit) != 0;
	}

	public boolean isSurvey() {
		return !isOther();
	}

	public boolean nameDefinesSegment() {
		return (status & NameDefinesSegmentBit) != 0;
	}

	public GeoReference reference() {
		if ((status & ReferenceUnspecifiedBit) != 0) {
			return new GeoReference();
		}
		if (reference != null) {
			return reference;
		}
		if (parent != null) {
			return parent.reference();
		}
		return new GeoReference();
	}

	public Unit<Length> reviewUnits() {
		return (status & FeetBit) != 0 ? Length.feet : Length.meters;
	}

	public boolean deriveDeclFromDate() {
		if ((status & DontDeriveDeclBit) != 0) {
			return false;
		}
		if ((status & DeriveDeclBit) != 0) {
			return true;
		}
		if (parent != null) {
			return parent.deriveDeclFromDate();
		}
		return false;
	}

	public boolean gridRelative() {
		if ((status & NotGridRelativeBit) != 0) {
			return false;
		}
		if ((status & GridRelativeBit) != 0) {
			return true;
		}
		if (parent != null) {
			return parent.gridRelative();
		}
		return false;
	}

	public boolean preserveVertShotOrientation() {
		if ((status & DontPreserveVertShotOrientationBit) != 0) {
			return false;
		}
		if ((status & PreserveVertShotOrientationBit) != 0) {
			return true;
		}
		if (parent != null) {
			return parent.preserveVertShotOrientation();
		}
		return false;
	}

	public boolean preserveVertShotLength() {
		if ((status & DontPreserveVertShotLengthBit) != 0) {
			return false;
		}
		if ((status & PreserveVertShotLengthBit) != 0) {
			return true;
		}
		if (parent != null) {
			return parent.preserveVertShotLength();
		}
		return false;
	}

	public LaunchOptions launchOptions() {
		if ((status & EditOnLaunchBit) != 0) {
			return LaunchOptions.Edit;
		}
		if ((status & OpenOnLaunchBit) != 0) {
			return LaunchOptions.Open;
		}
		return LaunchOptions.Properties;
	}

	public View defaultViewAfterCompilation() {
		switch (status & DefaultViewAfterCompilationMask) {
		case NorthOrEastViewBits:
			return View.NorthOrEast;
		case NorthOrWestViewBits:
			return View.NorthOrWest;
		case NorthViewBits:
			return View.North;
		case EastViewBits:
			return View.East;
		case WestViewBits:
			return View.West;
		default:
			if (parent != null) {
				return parent.defaultViewAfterCompilation();
			}
			return View.NorthOrEast;
		}
	}

	public Path dir() {
		if (parent == null || path != null && path.isAbsolute()) {
			return path;
		}
		if (path != null) {
			return parent.dir().resolve(path).normalize();
		}
		return parent.dir();
	}

	public Path absolutePath() {
		String name = this.name == null ? null : this.name.toString();
		if (name == null || name.isEmpty()) {
			return null;
		}
		if (isSurvey() && !name.matches("\\.[sS][rR][vV]$")) {
			if (Files.exists(dir().resolve(name + ".SRV").toAbsolutePath().normalize())) {
				name += ".SRV";
			} else if (Files.exists(dir().resolve(name + ".srv").toAbsolutePath().normalize())) {
				name += ".srv";
			} else {
				name += ".SRV";
			}
		}
		return dir().resolve(name).toAbsolutePath().normalize();
	}

	public List<Segment> allOptions() {
		List<Segment> options = new ArrayList<>();
		if (parent != null) {
			options = parent.allOptions();
		}
		if (this.options != null && !this.options.isEmpty()) {
			options.add(this.options);
		}
		return options;
	}

	public List<String> segment() {
		List<String> result = new ArrayList<>();
		if (parent != null) {
			result = parent.segment();
		}
		if (nameDefinesSegment() && !name.isEmpty()) {
			result.add(name.toString());
		}
		return result;
	}

}
