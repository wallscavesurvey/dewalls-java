package org.andork.walls.lst;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.andork.segment.Segment;
import org.andork.segment.SegmentMatcher;

public class WallsStationReportParser {
	private WallsStationReport report;

	public WallsStationReportParser() {
		this(new WallsStationReport());
	}

	public WallsStationReportParser(WallsStationReport report) {
		this.report = report;
	}
	
	public WallsStationReport getReport() {
		return report;
	}

	private static final Pattern crsLineRx = Pattern.compile(
			"^\\s*UTM\\s+(\\d+)([NS])\\s+Grid Conv:\\s*([0-9.]+)\\s+Datum:\\s*(.+)\\s*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern stationPositionLineRx = Pattern.compile(
			"^([^\t]*)\t([^\t]+)\t([-+]?\\d+(\\.\\d*)?|\\.\\d+)\t([-+]?\\d+(\\.\\d*)?|\\.\\d+)\t([-+]?\\d+(\\.\\d*)?|\\.\\d+)(\t(.*))?");

	public void parseLine(Segment line) {
		SegmentMatcher matcher = new SegmentMatcher(line, stationPositionLineRx);
		if (matcher.find()) {
			StationPosition station = new StationPosition();
			station.prefix = matcher.group(1);
			station.name = matcher.group(2);
			station.east = Double.parseDouble(matcher.group(3).toString());
			station.north = Double.parseDouble(matcher.group(5).toString());
			station.up = Double.parseDouble(matcher.group(7).toString());
			station.note = matcher.group(10);
			report.stationPositions.add(station);
			return;
		}
		matcher = new SegmentMatcher(line, crsLineRx);
		if (matcher.find()) {
			report.utmZone = Integer.valueOf(matcher.group(1).toString());
			report.utmSouth = "s".equalsIgnoreCase(matcher.group(2).toString());
			report.datum = matcher.group(4).toString();
			if (report.datum.toUpperCase().matches("WGS\\s*(19)?84")) {
				report.datum = "WGS84";
			}
		}
	}
	
	public void parse(InputStream in, Object source) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			int lineNumber = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				parseLine(new Segment(line, source, lineNumber++, 0));
			}
		}		
	}

	public void parseFile(Path path) throws IOException {
		try (FileInputStream in = new FileInputStream(path.toFile())) {
			parse(in, path);
		}
	}

	public void parseFile(File file) throws IOException {
		try (FileInputStream in = new FileInputStream(file)) {
			parse(in, file);
		}
	}
}
