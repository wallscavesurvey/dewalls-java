package org.andork.walls.srv;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.andork.unit.Angle;
import org.andork.unit.Length;
import org.andork.unit.UnitizedDouble;

public class FixedStation implements HasVarianceOverrides, HasNote, HasComment, HasInlineSegment, Cloneable {
	public String name;
	public UnitizedDouble<Length> north;
	public UnitizedDouble<Length> east;
	public UnitizedDouble<Length> elevation;
	public UnitizedDouble<Angle> latitude;
	public UnitizedDouble<Angle> longitude;
	public VarianceOverride horizontalVariance;
	public VarianceOverride verticalVariance;
	public String note;
	public List<String> segment;
	public String comment;
	public Date date;
	public WallsUnits units;

	@Override
	public void setVerticalVarianceOverride(VarianceOverride override) {
		verticalVariance = override;
	}

	@Override
	public void setHorizontalVarianceOverride(VarianceOverride override) {
		horizontalVariance = override;
	}
	
	@Override
	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public void setSegment(List<String> segment) {
		this.segment = segment;
	}
	
	public void processStationName() {
		name = units.processStationName(name);
	}
	
	@Override
	public FixedStation clone() {
		FixedStation result;
		try {
			result = (FixedStation) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		result.segment = segment == null ? null : new ArrayList<>(segment);
		result.date = date == null ? null : (Date) date.clone();
		return result;
	}
}
