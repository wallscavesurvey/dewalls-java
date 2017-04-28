const notEquals = (a, b) => `!Objects.equals(${a}, ${b})`

module.exports = {
	type: 'pojo',
	imports: [
	  'java.util.List',
	  'java.util.ArrayList',
	  'java.util.Arrays',
	  'java.util.Objects',
	  'org.andork.unit.Unit',
	  'org.andork.unit.Angle',
	  'org.andork.unit.Length',
	  'org.andork.unit.UnitizedDouble',
	],
	generateProperties: false,
	generateSetters: true,
	generateUpdaters: false,
	fields: {
		vectorType: {type: 'VectorType', description: 'vector type', initValue: 'VectorType.COMPASS_AND_TAPE'},
		ctOrder: {type: 'List<CtMeasurement>', initValue: 'Arrays.asList(CtMeasurement.DISTANCE, CtMeasurement.AZIMUTH, CtMeasurement.INCLINATION)', description: 'order of compass and tape measurements', shouldSet: notEquals, isNotEqual: notEquals},
		rectOrder: {type: 'List<RectMeasurement>', initValue: 'Arrays.asList(RectMeasurement.EAST, RectMeasurement.NORTH, RectMeasurement.UP)', description: 'order of rectangular measurements', shouldSet: notEquals, isNotEqual: notEquals},
		dUnit: {type: 'Unit<Length>', initValue: 'Length.meters', description: 'distance unit'},
		sUnit: {type: 'Unit<Length>', initValue: 'Length.meters', description: 'LRUD unit'},
		aUnit: {type: 'Unit<Angle>', initValue: 'Angle.degrees', description: 'frontsight azimuth unit'},
		abUnit: {type: 'Unit<Angle>', initValue: 'Angle.degrees', description: 'backsight azimuth unit'},
		vUnit: {type: 'Unit<Angle>', initValue: 'Angle.degrees', description: 'frontsight inclination unit'},
		vbUnit: {type: 'Unit<Angle>', initValue: 'Angle.degrees', description: 'backsight inclination unit'},
		decl: {type: 'UnitizedDouble<Angle>', initValue: 'new UnitizedDouble<>(0, Angle.degrees)', description: 'declination', shouldSet: notEquals, isNotEqual: notEquals},
		grid: {type: 'UnitizedDouble<Angle>', initValue: 'new UnitizedDouble<>(0, Angle.degrees)', description: 'UTM grid correction', shouldSet: notEquals, isNotEqual: notEquals},
		rect: {type: 'UnitizedDouble<Angle>', initValue: 'new UnitizedDouble<>(0, Angle.degrees)', description: 'RECT correction', shouldSet: notEquals, isNotEqual: notEquals},
		incd: {type: 'UnitizedDouble<Length>', initValue: 'new UnitizedDouble<>(0, Length.meters)', description: 'distance correction', shouldSet: notEquals, isNotEqual: notEquals},
		inca: {type: 'UnitizedDouble<Angle>', initValue: 'new UnitizedDouble<>(0, Angle.degrees)', description: 'frontsight azimuth correction', shouldSet: notEquals, isNotEqual: notEquals},
		incab: {type: 'UnitizedDouble<Angle>', initValue: 'new UnitizedDouble<>(0, Angle.degrees)', description: 'backsight azimuth correction', shouldSet: notEquals, isNotEqual: notEquals},
		incv: {type: 'UnitizedDouble<Angle>', initValue: 'new UnitizedDouble<>(0, Angle.degrees)', description: 'frontsight inclination correction', shouldSet: notEquals, isNotEqual: notEquals},
		incvb: {type: 'UnitizedDouble<Angle>', initValue: 'new UnitizedDouble<>(0, Angle.degrees)', description: 'backsight inclination correction', shouldSet: notEquals, isNotEqual: notEquals},
		incs: {type: 'UnitizedDouble<Length>', initValue: 'new UnitizedDouble<>(0, Length.meters)', description: 'LRUD measurement correction', shouldSet: notEquals, isNotEqual: notEquals},
		inch: {type: 'UnitizedDouble<Length>', initValue: 'new UnitizedDouble<>(0, Length.meters)', description: 'vertical offset correction', shouldSet: notEquals, isNotEqual: notEquals},
		typeabCorrected: {type: 'boolean', initValue: 'false', description: 'whether backsight azimuths are corrected'},
		typeabTolerance: {type: 'UnitizedDouble<Angle>', initValue: 'new UnitizedDouble<>(2, Angle.degrees)', description: 'allowed frontsight/backsight azimuth disagreement', shouldSet: notEquals, isNotEqual: notEquals},
		typeabNoAverage: {type: 'boolean', initValue: 'false', description: 'whether to average the frontsight and backsight azimuth, or just use frontsight'},
		typevbCorrected: {type: 'boolean', initValue: 'false', description: 'whether backsight inclinations are corrected'},
		typevbTolerance: {type: 'UnitizedDouble<Angle>', initValue: 'new UnitizedDouble<>(2, Angle.degrees)', description: 'allowed frontsight/backsight inclination disagreement', shouldSet: notEquals, isNotEqual: notEquals},
		typevbNoAverage: {type: 'boolean', initValue: 'false', description: 'whether to average the frontsight and backsight inclination, or just use frontsight'},
		case_: {type: 'CaseType', initValue: 'CaseType.MIXED', description: 'how to change case of station names'},
		lrud: {type: 'LrudType', initValue: 'LrudType.FROM', description: 'type of LRUDs'},
		lrudOrder: {type: 'List<LrudMeasurement>', initValue: 'Arrays.asList(LrudMeasurement.LEFT, LrudMeasurement.RIGHT, LrudMeasurement.UP, LrudMeasurement.DOWN)', description: 'order of LRUD measurements', shouldSet: notEquals, isNotEqual: notEquals},
		tape: {type: 'List<TapingMethodMeasurement>', initValue: 'Arrays.asList(TapingMethodMeasurement.INSTRUMENT_HEIGHT, TapingMethodMeasurement.TARGET_HEIGHT)', description: 'order of taping method measurements', shouldSet: notEquals, isNotEqual: notEquals},
		flag: {type: 'String', initValue: 'null', description: 'station flag', shouldSet: notEquals, isNotEqual: notEquals},
		prefix: {type: 'List<String>', initValue: 'new ArrayList<>()', description: 'station name prefixes', shouldSet: notEquals, isNotEqual: notEquals},
		uvh: {type: 'double', initValue: '0', description: 'horizontal variance'},
		uvv: {type: 'double', initValue: '0', description: 'vertical variance'},
	},
	extraCode: `
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
	`,
	extraMutableCode: `
	public MutableWallsUnits setPrefix(int index, String prefix) {
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
	`,
}
