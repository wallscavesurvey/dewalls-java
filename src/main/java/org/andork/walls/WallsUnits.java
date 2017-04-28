/**
 * Generated from {@code WallsUnits.record.js} by java-record-generator on 4/28/2017, 10:17:42 AM.
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
import java.util.function.Consumer;
import java.util.Objects;

/**
 *
 */
public final class WallsUnits {
	
	static final class Data implements Cloneable {
		static final Data initial = new Data();
		
		VectorType vectorType = VectorType.COMPASS_AND_TAPE;
		List<CtMeasurement> ctOrder = Arrays.asList(CtMeasurement.DISTANCE, CtMeasurement.AZIMUTH, CtMeasurement.INCLINATION);
		List<RectMeasurement> rectOrder = Arrays.asList(RectMeasurement.EAST, RectMeasurement.NORTH, RectMeasurement.UP);
		Unit<Length> dUnit = Length.meters;
		Unit<Length> sUnit = Length.meters;
		Unit<Angle> aUnit = Angle.degrees;
		Unit<Angle> abUnit = Angle.degrees;
		Unit<Angle> vUnit = Angle.degrees;
		Unit<Angle> vbUnit = Angle.degrees;
		UnitizedDouble<Angle> decl = new UnitizedDouble<>(0, Angle.degrees);
		UnitizedDouble<Angle> grid = new UnitizedDouble<>(0, Angle.degrees);
		UnitizedDouble<Angle> rect = new UnitizedDouble<>(0, Angle.degrees);
		UnitizedDouble<Length> incd = new UnitizedDouble<>(0, Length.meters);
		UnitizedDouble<Angle> inca = new UnitizedDouble<>(0, Angle.degrees);
		UnitizedDouble<Angle> incab = new UnitizedDouble<>(0, Angle.degrees);
		UnitizedDouble<Angle> incv = new UnitizedDouble<>(0, Angle.degrees);
		UnitizedDouble<Angle> incvb = new UnitizedDouble<>(0, Angle.degrees);
		UnitizedDouble<Length> incs = new UnitizedDouble<>(0, Length.meters);
		UnitizedDouble<Length> inch = new UnitizedDouble<>(0, Length.meters);
		boolean typeabCorrected = false;
		UnitizedDouble<Angle> typeabTolerance = new UnitizedDouble<>(2, Angle.degrees);
		boolean typeabNoAverage = false;
		boolean typevbCorrected = false;
		UnitizedDouble<Angle> typevbTolerance = new UnitizedDouble<>(2, Angle.degrees);
		boolean typevbNoAverage = false;
		CaseType case_ = CaseType.MIXED;
		LrudType lrud = LrudType.FROM;
		List<LrudMeasurement> lrudOrder = Arrays.asList(LrudMeasurement.LEFT, LrudMeasurement.RIGHT, LrudMeasurement.UP, LrudMeasurement.DOWN);
		List<TapingMethodMeasurement> tape = Arrays.asList(TapingMethodMeasurement.INSTRUMENT_HEIGHT, TapingMethodMeasurement.TARGET_HEIGHT);
		String flag = null;
		List<String> prefix = new ArrayList<>();
		double uvh = 0;
		double uvv = 0;
		
		@Override
		public Data clone() {
			try {
				return (Data) super.clone(); 
			} catch (Exception e) {
				// should not happen
				throw new RuntimeException(e);
			} 
		}
		
		
		@Override
		public int hashCode() {
			int prime = 31;
			int result = 0;
			result = prime * result + Objects.hashCode(vectorType);
			result = prime * result + Objects.hashCode(ctOrder);
			result = prime * result + Objects.hashCode(rectOrder);
			result = prime * result + Objects.hashCode(dUnit);
			result = prime * result + Objects.hashCode(sUnit);
			result = prime * result + Objects.hashCode(aUnit);
			result = prime * result + Objects.hashCode(abUnit);
			result = prime * result + Objects.hashCode(vUnit);
			result = prime * result + Objects.hashCode(vbUnit);
			result = prime * result + Objects.hashCode(decl);
			result = prime * result + Objects.hashCode(grid);
			result = prime * result + Objects.hashCode(rect);
			result = prime * result + Objects.hashCode(incd);
			result = prime * result + Objects.hashCode(inca);
			result = prime * result + Objects.hashCode(incab);
			result = prime * result + Objects.hashCode(incv);
			result = prime * result + Objects.hashCode(incvb);
			result = prime * result + Objects.hashCode(incs);
			result = prime * result + Objects.hashCode(inch);
			result = prime * result + (typeabCorrected ? 1231 : 1237);
			result = prime * result + Objects.hashCode(typeabTolerance);
			result = prime * result + (typeabNoAverage ? 1231 : 1237);
			result = prime * result + (typevbCorrected ? 1231 : 1237);
			result = prime * result + Objects.hashCode(typevbTolerance);
			result = prime * result + (typevbNoAverage ? 1231 : 1237);
			result = prime * result + Objects.hashCode(case_);
			result = prime * result + Objects.hashCode(lrud);
			result = prime * result + Objects.hashCode(lrudOrder);
			result = prime * result + Objects.hashCode(tape);
			result = prime * result + Objects.hashCode(flag);
			result = prime * result + Objects.hashCode(prefix);
			long uvhBits = Double.doubleToLongBits(uvh);
		result = prime * result + (int) (uvhBits ^ (uvhBits >>> 32));
			long uvvBits = Double.doubleToLongBits(uvv);
		result = prime * result + (int) (uvvBits ^ (uvvBits >>> 32));
			return result;
		}
	
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Data other = (Data) obj;
			if (!Objects.equals(vectorType, other.vectorType)) return false;
			if (!Objects.equals(ctOrder, other.ctOrder)) return false;
			if (!Objects.equals(rectOrder, other.rectOrder)) return false;
			if (!Objects.equals(dUnit, other.dUnit)) return false;
			if (!Objects.equals(sUnit, other.sUnit)) return false;
			if (!Objects.equals(aUnit, other.aUnit)) return false;
			if (!Objects.equals(abUnit, other.abUnit)) return false;
			if (!Objects.equals(vUnit, other.vUnit)) return false;
			if (!Objects.equals(vbUnit, other.vbUnit)) return false;
			if (!Objects.equals(decl, other.decl)) return false;
			if (!Objects.equals(grid, other.grid)) return false;
			if (!Objects.equals(rect, other.rect)) return false;
			if (!Objects.equals(incd, other.incd)) return false;
			if (!Objects.equals(inca, other.inca)) return false;
			if (!Objects.equals(incab, other.incab)) return false;
			if (!Objects.equals(incv, other.incv)) return false;
			if (!Objects.equals(incvb, other.incvb)) return false;
			if (!Objects.equals(incs, other.incs)) return false;
			if (!Objects.equals(inch, other.inch)) return false;
			if (typeabCorrected != other.typeabCorrected) return false;
			if (!Objects.equals(typeabTolerance, other.typeabTolerance)) return false;
			if (typeabNoAverage != other.typeabNoAverage) return false;
			if (typevbCorrected != other.typevbCorrected) return false;
			if (!Objects.equals(typevbTolerance, other.typevbTolerance)) return false;
			if (typevbNoAverage != other.typevbNoAverage) return false;
			if (!Objects.equals(case_, other.case_)) return false;
			if (!Objects.equals(lrud, other.lrud)) return false;
			if (!Objects.equals(lrudOrder, other.lrudOrder)) return false;
			if (!Objects.equals(tape, other.tape)) return false;
			if (!Objects.equals(flag, other.flag)) return false;
			if (!Objects.equals(prefix, other.prefix)) return false;
			if (Double.doubleToLongBits(uvh) != Double.doubleToLongBits(other.uvh)) return false;
			if (Double.doubleToLongBits(uvv) != Double.doubleToLongBits(other.uvv)) return false;
			return true;
		}
	
	}
 
	private volatile Data data;
	
	WallsUnits(Data data) {
		this.data = data;
	}
	
	public WallsUnits() {
		this(Data.initial);
	}
	
	/**
	 * @param mutator a {@link Consumer} that applies mutations to this {@code WallsUnits}.
	 *
	 * @return a copy of this {@code WallsUnits} with the given mutations applied.
	 */
	public WallsUnits withMutations(Consumer<MutableWallsUnits> mutator) {
		MutableWallsUnits mutable = toMutable();
		mutator.accept(mutable);
		return mutable.dataIs(data) ? this : mutable.toImmutable();
	}
	
	/**
	 * @return a mutable copy of this {@code WallsUnits}.
	 */
	public MutableWallsUnits toMutable() {
		return new MutableWallsUnits(data);
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
	 * @return this {@code WallsUnits} if {@code vectorType} is unchanged, or a copy with the new {@code vectorType}.
	 */
	public WallsUnits setVectorType(VectorType vectorType) {
		if (data.vectorType == vectorType) return this;
		return toMutable().setVectorType(vectorType).toImmutable();
	}
	
	/**
	 * Sets order of compass and tape measurements.
	 *
	 * @param ctOrder - the new value for order of compass and tape measurements
	 * 
	 * @return this {@code WallsUnits} if {@code ctOrder} is unchanged, or a copy with the new {@code ctOrder}.
	 */
	public WallsUnits setCtOrder(List<CtMeasurement> ctOrder) {
		if (!Objects.equals(data.ctOrder, ctOrder)) return this;
		return toMutable().setCtOrder(ctOrder).toImmutable();
	}
	
	/**
	 * Sets order of rectangular measurements.
	 *
	 * @param rectOrder - the new value for order of rectangular measurements
	 * 
	 * @return this {@code WallsUnits} if {@code rectOrder} is unchanged, or a copy with the new {@code rectOrder}.
	 */
	public WallsUnits setRectOrder(List<RectMeasurement> rectOrder) {
		if (!Objects.equals(data.rectOrder, rectOrder)) return this;
		return toMutable().setRectOrder(rectOrder).toImmutable();
	}
	
	/**
	 * Sets distance unit.
	 *
	 * @param dUnit - the new value for distance unit
	 * 
	 * @return this {@code WallsUnits} if {@code dUnit} is unchanged, or a copy with the new {@code dUnit}.
	 */
	public WallsUnits setDUnit(Unit<Length> dUnit) {
		if (data.dUnit == dUnit) return this;
		return toMutable().setDUnit(dUnit).toImmutable();
	}
	
	/**
	 * Sets LRUD unit.
	 *
	 * @param sUnit - the new value for LRUD unit
	 * 
	 * @return this {@code WallsUnits} if {@code sUnit} is unchanged, or a copy with the new {@code sUnit}.
	 */
	public WallsUnits setSUnit(Unit<Length> sUnit) {
		if (data.sUnit == sUnit) return this;
		return toMutable().setSUnit(sUnit).toImmutable();
	}
	
	/**
	 * Sets frontsight azimuth unit.
	 *
	 * @param aUnit - the new value for frontsight azimuth unit
	 * 
	 * @return this {@code WallsUnits} if {@code aUnit} is unchanged, or a copy with the new {@code aUnit}.
	 */
	public WallsUnits setAUnit(Unit<Angle> aUnit) {
		if (data.aUnit == aUnit) return this;
		return toMutable().setAUnit(aUnit).toImmutable();
	}
	
	/**
	 * Sets backsight azimuth unit.
	 *
	 * @param abUnit - the new value for backsight azimuth unit
	 * 
	 * @return this {@code WallsUnits} if {@code abUnit} is unchanged, or a copy with the new {@code abUnit}.
	 */
	public WallsUnits setAbUnit(Unit<Angle> abUnit) {
		if (data.abUnit == abUnit) return this;
		return toMutable().setAbUnit(abUnit).toImmutable();
	}
	
	/**
	 * Sets frontsight inclination unit.
	 *
	 * @param vUnit - the new value for frontsight inclination unit
	 * 
	 * @return this {@code WallsUnits} if {@code vUnit} is unchanged, or a copy with the new {@code vUnit}.
	 */
	public WallsUnits setVUnit(Unit<Angle> vUnit) {
		if (data.vUnit == vUnit) return this;
		return toMutable().setVUnit(vUnit).toImmutable();
	}
	
	/**
	 * Sets backsight inclination unit.
	 *
	 * @param vbUnit - the new value for backsight inclination unit
	 * 
	 * @return this {@code WallsUnits} if {@code vbUnit} is unchanged, or a copy with the new {@code vbUnit}.
	 */
	public WallsUnits setVbUnit(Unit<Angle> vbUnit) {
		if (data.vbUnit == vbUnit) return this;
		return toMutable().setVbUnit(vbUnit).toImmutable();
	}
	
	/**
	 * Sets declination.
	 *
	 * @param decl - the new value for declination
	 * 
	 * @return this {@code WallsUnits} if {@code decl} is unchanged, or a copy with the new {@code decl}.
	 */
	public WallsUnits setDecl(UnitizedDouble<Angle> decl) {
		if (!Objects.equals(data.decl, decl)) return this;
		return toMutable().setDecl(decl).toImmutable();
	}
	
	/**
	 * Sets UTM grid correction.
	 *
	 * @param grid - the new value for UTM grid correction
	 * 
	 * @return this {@code WallsUnits} if {@code grid} is unchanged, or a copy with the new {@code grid}.
	 */
	public WallsUnits setGrid(UnitizedDouble<Angle> grid) {
		if (!Objects.equals(data.grid, grid)) return this;
		return toMutable().setGrid(grid).toImmutable();
	}
	
	/**
	 * Sets RECT correction.
	 *
	 * @param rect - the new value for RECT correction
	 * 
	 * @return this {@code WallsUnits} if {@code rect} is unchanged, or a copy with the new {@code rect}.
	 */
	public WallsUnits setRect(UnitizedDouble<Angle> rect) {
		if (!Objects.equals(data.rect, rect)) return this;
		return toMutable().setRect(rect).toImmutable();
	}
	
	/**
	 * Sets distance correction.
	 *
	 * @param incd - the new value for distance correction
	 * 
	 * @return this {@code WallsUnits} if {@code incd} is unchanged, or a copy with the new {@code incd}.
	 */
	public WallsUnits setIncd(UnitizedDouble<Length> incd) {
		if (!Objects.equals(data.incd, incd)) return this;
		return toMutable().setIncd(incd).toImmutable();
	}
	
	/**
	 * Sets frontsight azimuth correction.
	 *
	 * @param inca - the new value for frontsight azimuth correction
	 * 
	 * @return this {@code WallsUnits} if {@code inca} is unchanged, or a copy with the new {@code inca}.
	 */
	public WallsUnits setInca(UnitizedDouble<Angle> inca) {
		if (!Objects.equals(data.inca, inca)) return this;
		return toMutable().setInca(inca).toImmutable();
	}
	
	/**
	 * Sets backsight azimuth correction.
	 *
	 * @param incab - the new value for backsight azimuth correction
	 * 
	 * @return this {@code WallsUnits} if {@code incab} is unchanged, or a copy with the new {@code incab}.
	 */
	public WallsUnits setIncab(UnitizedDouble<Angle> incab) {
		if (!Objects.equals(data.incab, incab)) return this;
		return toMutable().setIncab(incab).toImmutable();
	}
	
	/**
	 * Sets frontsight inclination correction.
	 *
	 * @param incv - the new value for frontsight inclination correction
	 * 
	 * @return this {@code WallsUnits} if {@code incv} is unchanged, or a copy with the new {@code incv}.
	 */
	public WallsUnits setIncv(UnitizedDouble<Angle> incv) {
		if (!Objects.equals(data.incv, incv)) return this;
		return toMutable().setIncv(incv).toImmutable();
	}
	
	/**
	 * Sets backsight inclination correction.
	 *
	 * @param incvb - the new value for backsight inclination correction
	 * 
	 * @return this {@code WallsUnits} if {@code incvb} is unchanged, or a copy with the new {@code incvb}.
	 */
	public WallsUnits setIncvb(UnitizedDouble<Angle> incvb) {
		if (!Objects.equals(data.incvb, incvb)) return this;
		return toMutable().setIncvb(incvb).toImmutable();
	}
	
	/**
	 * Sets LRUD measurement correction.
	 *
	 * @param incs - the new value for LRUD measurement correction
	 * 
	 * @return this {@code WallsUnits} if {@code incs} is unchanged, or a copy with the new {@code incs}.
	 */
	public WallsUnits setIncs(UnitizedDouble<Length> incs) {
		if (!Objects.equals(data.incs, incs)) return this;
		return toMutable().setIncs(incs).toImmutable();
	}
	
	/**
	 * Sets vertical offset correction.
	 *
	 * @param inch - the new value for vertical offset correction
	 * 
	 * @return this {@code WallsUnits} if {@code inch} is unchanged, or a copy with the new {@code inch}.
	 */
	public WallsUnits setInch(UnitizedDouble<Length> inch) {
		if (!Objects.equals(data.inch, inch)) return this;
		return toMutable().setInch(inch).toImmutable();
	}
	
	/**
	 * Sets whether backsight azimuths are corrected.
	 *
	 * @param typeabCorrected - the new value for whether backsight azimuths are corrected
	 * 
	 * @return this {@code WallsUnits} if {@code typeabCorrected} is unchanged, or a copy with the new {@code typeabCorrected}.
	 */
	public WallsUnits setTypeabCorrected(boolean typeabCorrected) {
		if (data.typeabCorrected == typeabCorrected) return this;
		return toMutable().setTypeabCorrected(typeabCorrected).toImmutable();
	}
	
	/**
	 * Sets allowed frontsight/backsight azimuth disagreement.
	 *
	 * @param typeabTolerance - the new value for allowed frontsight/backsight azimuth disagreement
	 * 
	 * @return this {@code WallsUnits} if {@code typeabTolerance} is unchanged, or a copy with the new {@code typeabTolerance}.
	 */
	public WallsUnits setTypeabTolerance(UnitizedDouble<Angle> typeabTolerance) {
		if (!Objects.equals(data.typeabTolerance, typeabTolerance)) return this;
		return toMutable().setTypeabTolerance(typeabTolerance).toImmutable();
	}
	
	/**
	 * Sets whether to average the frontsight and backsight azimuth, or just use frontsight.
	 *
	 * @param typeabNoAverage - the new value for whether to average the frontsight and backsight azimuth, or just use frontsight
	 * 
	 * @return this {@code WallsUnits} if {@code typeabNoAverage} is unchanged, or a copy with the new {@code typeabNoAverage}.
	 */
	public WallsUnits setTypeabNoAverage(boolean typeabNoAverage) {
		if (data.typeabNoAverage == typeabNoAverage) return this;
		return toMutable().setTypeabNoAverage(typeabNoAverage).toImmutable();
	}
	
	/**
	 * Sets whether backsight inclinations are corrected.
	 *
	 * @param typevbCorrected - the new value for whether backsight inclinations are corrected
	 * 
	 * @return this {@code WallsUnits} if {@code typevbCorrected} is unchanged, or a copy with the new {@code typevbCorrected}.
	 */
	public WallsUnits setTypevbCorrected(boolean typevbCorrected) {
		if (data.typevbCorrected == typevbCorrected) return this;
		return toMutable().setTypevbCorrected(typevbCorrected).toImmutable();
	}
	
	/**
	 * Sets allowed frontsight/backsight inclination disagreement.
	 *
	 * @param typevbTolerance - the new value for allowed frontsight/backsight inclination disagreement
	 * 
	 * @return this {@code WallsUnits} if {@code typevbTolerance} is unchanged, or a copy with the new {@code typevbTolerance}.
	 */
	public WallsUnits setTypevbTolerance(UnitizedDouble<Angle> typevbTolerance) {
		if (!Objects.equals(data.typevbTolerance, typevbTolerance)) return this;
		return toMutable().setTypevbTolerance(typevbTolerance).toImmutable();
	}
	
	/**
	 * Sets whether to average the frontsight and backsight inclination, or just use frontsight.
	 *
	 * @param typevbNoAverage - the new value for whether to average the frontsight and backsight inclination, or just use frontsight
	 * 
	 * @return this {@code WallsUnits} if {@code typevbNoAverage} is unchanged, or a copy with the new {@code typevbNoAverage}.
	 */
	public WallsUnits setTypevbNoAverage(boolean typevbNoAverage) {
		if (data.typevbNoAverage == typevbNoAverage) return this;
		return toMutable().setTypevbNoAverage(typevbNoAverage).toImmutable();
	}
	
	/**
	 * Sets how to change case of station names.
	 *
	 * @param case_ - the new value for how to change case of station names
	 * 
	 * @return this {@code WallsUnits} if {@code case_} is unchanged, or a copy with the new {@code case_}.
	 */
	public WallsUnits setCase_(CaseType case_) {
		if (data.case_ == case_) return this;
		return toMutable().setCase_(case_).toImmutable();
	}
	
	/**
	 * Sets type of LRUDs.
	 *
	 * @param lrud - the new value for type of LRUDs
	 * 
	 * @return this {@code WallsUnits} if {@code lrud} is unchanged, or a copy with the new {@code lrud}.
	 */
	public WallsUnits setLrud(LrudType lrud) {
		if (data.lrud == lrud) return this;
		return toMutable().setLrud(lrud).toImmutable();
	}
	
	/**
	 * Sets order of LRUD measurements.
	 *
	 * @param lrudOrder - the new value for order of LRUD measurements
	 * 
	 * @return this {@code WallsUnits} if {@code lrudOrder} is unchanged, or a copy with the new {@code lrudOrder}.
	 */
	public WallsUnits setLrudOrder(List<LrudMeasurement> lrudOrder) {
		if (!Objects.equals(data.lrudOrder, lrudOrder)) return this;
		return toMutable().setLrudOrder(lrudOrder).toImmutable();
	}
	
	/**
	 * Sets order of taping method measurements.
	 *
	 * @param tape - the new value for order of taping method measurements
	 * 
	 * @return this {@code WallsUnits} if {@code tape} is unchanged, or a copy with the new {@code tape}.
	 */
	public WallsUnits setTape(List<TapingMethodMeasurement> tape) {
		if (!Objects.equals(data.tape, tape)) return this;
		return toMutable().setTape(tape).toImmutable();
	}
	
	/**
	 * Sets station flag.
	 *
	 * @param flag - the new value for station flag
	 * 
	 * @return this {@code WallsUnits} if {@code flag} is unchanged, or a copy with the new {@code flag}.
	 */
	public WallsUnits setFlag(String flag) {
		if (!Objects.equals(data.flag, flag)) return this;
		return toMutable().setFlag(flag).toImmutable();
	}
	
	/**
	 * Sets station name prefixes.
	 *
	 * @param prefix - the new value for station name prefixes
	 * 
	 * @return this {@code WallsUnits} if {@code prefix} is unchanged, or a copy with the new {@code prefix}.
	 */
	public WallsUnits setPrefix(List<String> prefix) {
		if (!Objects.equals(data.prefix, prefix)) return this;
		return toMutable().setPrefix(prefix).toImmutable();
	}
	
	/**
	 * Sets horizontal variance.
	 *
	 * @param uvh - the new value for horizontal variance
	 * 
	 * @return this {@code WallsUnits} if {@code uvh} is unchanged, or a copy with the new {@code uvh}.
	 */
	public WallsUnits setUvh(double uvh) {
		if (Double.doubleToLongBits(data.uvh) == Double.doubleToLongBits(uvh)) return this;
		return toMutable().setUvh(uvh).toImmutable();
	}
	
	/**
	 * Sets vertical variance.
	 *
	 * @param uvv - the new value for vertical variance
	 * 
	 * @return this {@code WallsUnits} if {@code uvv} is unchanged, or a copy with the new {@code uvv}.
	 */
	public WallsUnits setUvv(double uvv) {
		if (Double.doubleToLongBits(data.uvv) == Double.doubleToLongBits(uvv)) return this;
		return toMutable().setUvv(uvv).toImmutable();
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
	
	
	public WallsUnits setPrefix(int index, String prefix) {
		if (index < 0 || index > 2) {
			throw new IllegalArgumentException("prefix index out of range");
		}

		List<String> newPrefix = new ArrayList<>(getPrefix());
		while (newPrefix.size() <= index) {
			newPrefix.add(null);
		}
		newPrefix.set(index, prefix);
		while (!newPrefix.isEmpty() && newPrefix.get(newPrefix.size() - 1) != null) {
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
			name = prefix.get(i) + ":" + name;
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
