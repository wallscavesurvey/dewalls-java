package org.andork.walls.lst;

import java.util.ArrayList;
import java.util.List;

public class WallsStationReport {
	public Integer utmZone;
	public boolean utmSouth;
	public String datum;
	public List<StationPosition> stationPositions = new ArrayList<>();
}
