/**
 * Generated from {@code WallsUnits.record.js} by java-record-generator on 4/28/2017, 5:09:04 PM.
 * {@link https://github.com/jedwards1211/java-record-generator#readme}
 */
 
package org.andork.walls;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import org.andork.unit.Unit;
import org.andork.unit.Angle;
import org.andork.unit.Length;
import org.andork.unit.UnitizedDouble;
import org.andork.walls.WallsUnits.Data;

/**
 * The mutable version of {@link WallsUnits}.
 */
public final class MutableWallsUnits {
	private volatile boolean frozen = true;
	private volatile Data data;
	
	MutableWallsUnits(Data data) {
		this.data = data;
	}
	
	public MutableWallsUnits() {
		this(Data.initial);
	}
 
	public void detach() {
		if (frozen) {
			data = data.clone();
			frozen = false;
		}
	}
	
	/**
	 * @return an immutable copy of this {@code MutableWallsUnits}.
	 */
	public WallsUnits toImmutable() {
		frozen = true;
		return new WallsUnits(data);
	} 
	
	
	/**
	 * @return vector type.
	 */
	public VectorType getVectorType() {
		return data.vectorType;
	}
	
	/**
	 * @return order of compass and tape measurements.
	 */
	public List<CtMeasurement> getCtOrder() {
		return data.ctOrder;
	}
	
	/**
	 * @return order of rectangular measurements.
	 */
	public List<RectMeasurement> getRectOrder() {
		return data.rectOrder;
	}
	
	/**
	 * @return distance unit.
	 */
	public Unit<Length> getDUnit() {
		return data.dUnit;
	}
	
	/**
	 * @return LRUD unit.
	 */
	public Unit<Length> getSUnit() {
		return data.sUnit;
	}
	
	/**
	 * @return frontsight azimuth unit.
	 */
	public Unit<Angle> getAUnit() {
		return data.aUnit;
	}
	
	/**
	 * @return backsight azimuth unit.
	 */
	public Unit<Angle> getAbUnit() {
		return data.abUnit;
	}
	
	/**
	 * @return frontsight inclination unit.
	 */
	public Unit<Angle> getVUnit() {
		return data.vUnit;
	}
	
	/**
	 * @return backsight inclination unit.
	 */
	public Unit<Angle> getVbUnit() {
		return data.vbUnit;
	}
	
	/**
	 * @return declination.
	 */
	public UnitizedDouble<Angle> getDecl() {
		return data.decl;
	}
	
	/**
	 * @return UTM grid correction.
	 */
	public UnitizedDouble<Angle> getGrid() {
		return data.grid;
	}
	
	/**
	 * @return RECT correction.
	 */
	public UnitizedDouble<Angle> getRect() {
		return data.rect;
	}
	
	/**
	 * @return distance correction.
	 */
	public UnitizedDouble<Length> getIncd() {
		return data.incd;
	}
	
	/**
	 * @return frontsight azimuth correction.
	 */
	public UnitizedDouble<Angle> getInca() {
		return data.inca;
	}
	
	/**
	 * @return backsight azimuth correction.
	 */
	public UnitizedDouble<Angle> getIncab() {
		return data.incab;
	}
	
	/**
	 * @return frontsight inclination correction.
	 */
	public UnitizedDouble<Angle> getIncv() {
		return data.incv;
	}
	
	/**
	 * @return backsight inclination correction.
	 */
	public UnitizedDouble<Angle> getIncvb() {
		return data.incvb;
	}
	
	/**
	 * @return LRUD measurement correction.
	 */
	public UnitizedDouble<Length> getIncs() {
		return data.incs;
	}
	
	/**
	 * @return vertical offset correction.
	 */
	public UnitizedDouble<Length> getInch() {
		return data.inch;
	}
	
	/**
	 * @return whether backsight azimuths are corrected.
	 */
	public boolean isTypeabCorrected() {
		return data.typeabCorrected;
	}
	
	/**
	 * @return allowed frontsight/backsight azimuth disagreement.
	 */
	public UnitizedDouble<Angle> getTypeabTolerance() {
		return data.typeabTolerance;
	}
	
	/**
	 * @return whether to average the frontsight and backsight azimuth, or just use frontsight.
	 */
	public boolean isTypeabNoAverage() {
		return data.typeabNoAverage;
	}
	
	/**
	 * @return whether backsight inclinations are corrected.
	 */
	public boolean isTypevbCorrected() {
		return data.typevbCorrected;
	}
	
	/**
	 * @return allowed frontsight/backsight inclination disagreement.
	 */
	public UnitizedDouble<Angle> getTypevbTolerance() {
		return data.typevbTolerance;
	}
	
	/**
	 * @return whether to average the frontsight and backsight inclination, or just use frontsight.
	 */
	public boolean isTypevbNoAverage() {
		return data.typevbNoAverage;
	}
	
	/**
	 * @return how to change case of station names.
	 */
	public CaseType getCase_() {
		return data.case_;
	}
	
	/**
	 * @return type of LRUDs.
	 */
	public LrudType getLrud() {
		return data.lrud;
	}
	
	/**
	 * @return order of LRUD measurements.
	 */
	public List<LrudMeasurement> getLrudOrder() {
		return data.lrudOrder;
	}
	
	/**
	 * @return order of taping method measurements.
	 */
	public List<TapingMethodMeasurement> getTape() {
		return data.tape;
	}
	
	/**
	 * @return station flag.
	 */
	public String getFlag() {
		return data.flag;
	}
	
	/**
	 * @return station name prefixes.
	 */
	public List<String> getPrefix() {
		return data.prefix;
	}
	
	/**
	 * @return horizontal variance.
	 */
	public double getUvh() {
		return data.uvh;
	}
	
	/**
	 * @return vertical variance.
	 */
	public double getUvv() {
		return data.uvv;
	}
	
	
	/**
	 * Sets vector type.
	 *
	 * @param vectorType - the new value for vector type
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setVectorType(VectorType vectorType) {
		if (data.vectorType == vectorType) return this;
		detach();
		data.vectorType = vectorType;
		return this;
	}
	
	/**
	 * Sets order of compass and tape measurements.
	 *
	 * @param ctOrder - the new value for order of compass and tape measurements
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setCtOrder(List<CtMeasurement> ctOrder) {
		if (Objects.equals(data.ctOrder, ctOrder)) return this;
		detach();
		data.ctOrder = ctOrder;
		return this;
	}
	
	/**
	 * Sets order of rectangular measurements.
	 *
	 * @param rectOrder - the new value for order of rectangular measurements
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setRectOrder(List<RectMeasurement> rectOrder) {
		if (Objects.equals(data.rectOrder, rectOrder)) return this;
		detach();
		data.rectOrder = rectOrder;
		return this;
	}
	
	/**
	 * Sets distance unit.
	 *
	 * @param dUnit - the new value for distance unit
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setDUnit(Unit<Length> dUnit) {
		if (data.dUnit == dUnit) return this;
		detach();
		data.dUnit = dUnit;
		return this;
	}
	
	/**
	 * Sets LRUD unit.
	 *
	 * @param sUnit - the new value for LRUD unit
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setSUnit(Unit<Length> sUnit) {
		if (data.sUnit == sUnit) return this;
		detach();
		data.sUnit = sUnit;
		return this;
	}
	
	/**
	 * Sets frontsight azimuth unit.
	 *
	 * @param aUnit - the new value for frontsight azimuth unit
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setAUnit(Unit<Angle> aUnit) {
		if (data.aUnit == aUnit) return this;
		detach();
		data.aUnit = aUnit;
		return this;
	}
	
	/**
	 * Sets backsight azimuth unit.
	 *
	 * @param abUnit - the new value for backsight azimuth unit
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setAbUnit(Unit<Angle> abUnit) {
		if (data.abUnit == abUnit) return this;
		detach();
		data.abUnit = abUnit;
		return this;
	}
	
	/**
	 * Sets frontsight inclination unit.
	 *
	 * @param vUnit - the new value for frontsight inclination unit
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setVUnit(Unit<Angle> vUnit) {
		if (data.vUnit == vUnit) return this;
		detach();
		data.vUnit = vUnit;
		return this;
	}
	
	/**
	 * Sets backsight inclination unit.
	 *
	 * @param vbUnit - the new value for backsight inclination unit
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setVbUnit(Unit<Angle> vbUnit) {
		if (data.vbUnit == vbUnit) return this;
		detach();
		data.vbUnit = vbUnit;
		return this;
	}
	
	/**
	 * Sets declination.
	 *
	 * @param decl - the new value for declination
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setDecl(UnitizedDouble<Angle> decl) {
		if (Objects.equals(data.decl, decl)) return this;
		detach();
		data.decl = decl;
		return this;
	}
	
	/**
	 * Sets UTM grid correction.
	 *
	 * @param grid - the new value for UTM grid correction
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setGrid(UnitizedDouble<Angle> grid) {
		if (Objects.equals(data.grid, grid)) return this;
		detach();
		data.grid = grid;
		return this;
	}
	
	/**
	 * Sets RECT correction.
	 *
	 * @param rect - the new value for RECT correction
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setRect(UnitizedDouble<Angle> rect) {
		if (Objects.equals(data.rect, rect)) return this;
		detach();
		data.rect = rect;
		return this;
	}
	
	/**
	 * Sets distance correction.
	 *
	 * @param incd - the new value for distance correction
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setIncd(UnitizedDouble<Length> incd) {
		if (Objects.equals(data.incd, incd)) return this;
		detach();
		data.incd = incd;
		return this;
	}
	
	/**
	 * Sets frontsight azimuth correction.
	 *
	 * @param inca - the new value for frontsight azimuth correction
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setInca(UnitizedDouble<Angle> inca) {
		if (Objects.equals(data.inca, inca)) return this;
		detach();
		data.inca = inca;
		return this;
	}
	
	/**
	 * Sets backsight azimuth correction.
	 *
	 * @param incab - the new value for backsight azimuth correction
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setIncab(UnitizedDouble<Angle> incab) {
		if (Objects.equals(data.incab, incab)) return this;
		detach();
		data.incab = incab;
		return this;
	}
	
	/**
	 * Sets frontsight inclination correction.
	 *
	 * @param incv - the new value for frontsight inclination correction
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setIncv(UnitizedDouble<Angle> incv) {
		if (Objects.equals(data.incv, incv)) return this;
		detach();
		data.incv = incv;
		return this;
	}
	
	/**
	 * Sets backsight inclination correction.
	 *
	 * @param incvb - the new value for backsight inclination correction
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setIncvb(UnitizedDouble<Angle> incvb) {
		if (Objects.equals(data.incvb, incvb)) return this;
		detach();
		data.incvb = incvb;
		return this;
	}
	
	/**
	 * Sets LRUD measurement correction.
	 *
	 * @param incs - the new value for LRUD measurement correction
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setIncs(UnitizedDouble<Length> incs) {
		if (Objects.equals(data.incs, incs)) return this;
		detach();
		data.incs = incs;
		return this;
	}
	
	/**
	 * Sets vertical offset correction.
	 *
	 * @param inch - the new value for vertical offset correction
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setInch(UnitizedDouble<Length> inch) {
		if (Objects.equals(data.inch, inch)) return this;
		detach();
		data.inch = inch;
		return this;
	}
	
	/**
	 * Sets whether backsight azimuths are corrected.
	 *
	 * @param typeabCorrected - the new value for whether backsight azimuths are corrected
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setTypeabCorrected(boolean typeabCorrected) {
		if (data.typeabCorrected == typeabCorrected) return this;
		detach();
		data.typeabCorrected = typeabCorrected;
		return this;
	}
	
	/**
	 * Sets allowed frontsight/backsight azimuth disagreement.
	 *
	 * @param typeabTolerance - the new value for allowed frontsight/backsight azimuth disagreement
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setTypeabTolerance(UnitizedDouble<Angle> typeabTolerance) {
		if (Objects.equals(data.typeabTolerance, typeabTolerance)) return this;
		detach();
		data.typeabTolerance = typeabTolerance;
		return this;
	}
	
	/**
	 * Sets whether to average the frontsight and backsight azimuth, or just use frontsight.
	 *
	 * @param typeabNoAverage - the new value for whether to average the frontsight and backsight azimuth, or just use frontsight
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setTypeabNoAverage(boolean typeabNoAverage) {
		if (data.typeabNoAverage == typeabNoAverage) return this;
		detach();
		data.typeabNoAverage = typeabNoAverage;
		return this;
	}
	
	/**
	 * Sets whether backsight inclinations are corrected.
	 *
	 * @param typevbCorrected - the new value for whether backsight inclinations are corrected
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setTypevbCorrected(boolean typevbCorrected) {
		if (data.typevbCorrected == typevbCorrected) return this;
		detach();
		data.typevbCorrected = typevbCorrected;
		return this;
	}
	
	/**
	 * Sets allowed frontsight/backsight inclination disagreement.
	 *
	 * @param typevbTolerance - the new value for allowed frontsight/backsight inclination disagreement
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setTypevbTolerance(UnitizedDouble<Angle> typevbTolerance) {
		if (Objects.equals(data.typevbTolerance, typevbTolerance)) return this;
		detach();
		data.typevbTolerance = typevbTolerance;
		return this;
	}
	
	/**
	 * Sets whether to average the frontsight and backsight inclination, or just use frontsight.
	 *
	 * @param typevbNoAverage - the new value for whether to average the frontsight and backsight inclination, or just use frontsight
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setTypevbNoAverage(boolean typevbNoAverage) {
		if (data.typevbNoAverage == typevbNoAverage) return this;
		detach();
		data.typevbNoAverage = typevbNoAverage;
		return this;
	}
	
	/**
	 * Sets how to change case of station names.
	 *
	 * @param case_ - the new value for how to change case of station names
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setCase_(CaseType case_) {
		if (data.case_ == case_) return this;
		detach();
		data.case_ = case_;
		return this;
	}
	
	/**
	 * Sets type of LRUDs.
	 *
	 * @param lrud - the new value for type of LRUDs
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setLrud(LrudType lrud) {
		if (data.lrud == lrud) return this;
		detach();
		data.lrud = lrud;
		return this;
	}
	
	/**
	 * Sets order of LRUD measurements.
	 *
	 * @param lrudOrder - the new value for order of LRUD measurements
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setLrudOrder(List<LrudMeasurement> lrudOrder) {
		if (Objects.equals(data.lrudOrder, lrudOrder)) return this;
		detach();
		data.lrudOrder = lrudOrder;
		return this;
	}
	
	/**
	 * Sets order of taping method measurements.
	 *
	 * @param tape - the new value for order of taping method measurements
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setTape(List<TapingMethodMeasurement> tape) {
		if (Objects.equals(data.tape, tape)) return this;
		detach();
		data.tape = tape;
		return this;
	}
	
	/**
	 * Sets station flag.
	 *
	 * @param flag - the new value for station flag
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setFlag(String flag) {
		if (Objects.equals(data.flag, flag)) return this;
		detach();
		data.flag = flag;
		return this;
	}
	
	/**
	 * Sets station name prefixes.
	 *
	 * @param prefix - the new value for station name prefixes
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setPrefix(List<String> prefix) {
		if (Objects.equals(data.prefix, prefix)) return this;
		detach();
		data.prefix = prefix;
		return this;
	}
	
	/**
	 * Sets horizontal variance.
	 *
	 * @param uvh - the new value for horizontal variance
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setUvh(double uvh) {
		if (Double.doubleToLongBits(data.uvh) == Double.doubleToLongBits(uvh)) return this;
		detach();
		data.uvh = uvh;
		return this;
	}
	
	/**
	 * Sets vertical variance.
	 *
	 * @param uvv - the new value for vertical variance
	 * 
	 * @return this {@code MutableWallsUnits}.
	 */
	public MutableWallsUnits setUvv(double uvv) {
		if (Double.doubleToLongBits(data.uvv) == Double.doubleToLongBits(uvv)) return this;
		detach();
		data.uvv = uvv;
		return this;
	}
	
	
	
	@Override
	public int hashCode() {
		return data.hashCode();
	}

	boolean dataIs(Data data) {
		return this.data == data;
	}

	boolean dataEquals(Data data) {
		return data.equals(data);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj instanceof WallsUnits) return ((WallsUnits) obj).dataEquals(data);
		if (obj instanceof MutableWallsUnits) return ((MutableWallsUnits) obj).dataEquals(data);
		return false;
	}
	
	
	public MutableWallsUnits setPrefix(int index, String prefix) {
		if (index < 0 || index > 2) {
			throw new IllegalArgumentException("prefix index out of range");
		}

		List<String> newPrefix = new ArrayList<>(getPrefix());
		while (newPrefix.size() <= index) {
			newPrefix.add(null);
		}
		newPrefix.set(index, prefix);
		while (!newPrefix.isEmpty() && newPrefix.get(newPrefix.size() - 1) == null) {
			newPrefix.remove(newPrefix.size() - 1);
		}
		return setPrefix(newPrefix);
	}
	
	public String processStationName(String name) {
		if (name == null) {
			return name;
		}
		name = getCase_().apply(name);
		int explicitPrefixCount = name.length() - name.replace(":", "").length();
		List<String> prefix = getPrefix();
		for (int i = explicitPrefixCount; i < prefix.size(); i++) {
			String p = prefix.get(i);
			name = (p == null ? "" : p) + ":" + name;
		}
		return name.replaceFirst("^:+", "");
	}
	
	public UnitizedDouble<Angle> averageInclination(UnitizedDouble<Angle> fs, UnitizedDouble<Angle> bs) {
		if (bs != null && !isTypevbCorrected()) {
			bs = bs.negate();
		}
		if (fs == null) {
			return bs;
		}
		if (bs == null) {
			return fs;
		}
		return fs.add(bs).mul(0.5);
	}
	
	public String lrudOrderString() {
		StringBuffer result = new StringBuffer();
		for (LrudMeasurement elem : getLrudOrder()) {
			result.append(elem.name().charAt(0));
		}
		return result.toString();
	}
	
}
