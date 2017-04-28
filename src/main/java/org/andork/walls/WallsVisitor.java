package org.andork.walls;

import java.util.Date;
import java.util.List;

public interface WallsVisitor {
    void parsedVector(Vector parsedVector);
    void parsedFixStation(FixedStation station);
    void parsedComment(String parsedComment);
    void parsedNote(String station, String parsedNote);
    void parsedDate(Date date);
    void parsedFlag(List<String> stations, String flag);
    void willParseUnits();
    void parsedUnits();
    void parsedSegment(String segment);
    void message(WallsMessage message);
}
