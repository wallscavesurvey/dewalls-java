package org.andork.walls.srv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.andork.segment.Segment;
import org.andork.segment.SegmentParseException;
import org.andork.unit.Angle;
import org.andork.unit.Length;
import org.andork.unit.Unit;
import org.andork.unit.UnitType;
import org.andork.unit.UnitizedDouble;
import org.andork.walls.LineParser;
import org.andork.walls.Optional;
import org.andork.walls.SegmentParseExpectedException;
import org.andork.walls.WallsMessage;
import org.andork.walls.wpj.WallsProjectEntry;

public class WallsSurveyParser extends LineParser {
	WallsVisitor visitor = new AbstractWallsVisitor();
	MutableWallsUnits units = new MutableWallsUnits();
	final Stack<WallsUnits> unitsStack = new Stack<>();
	final HashMap<String, String> macros = new HashMap<>();
	boolean inBlockComment;
	final List<String> segment = new ArrayList<>();
	final List<String> rootSegment = new ArrayList<>();
	Date date;

	Segment fromStationSegment;
	Segment toStationSegment;
	Segment azmSegment;
	Segment incSegment;

	Vector vector;
	FixedStation fixStation;

	Character escapedChar(Predicate<Character> charPredicate, String... expectedItems) throws SegmentParseException {
		Character c = expectChar(charPredicate, expectedItems);
		return c == '\\' ? oneOf(escapedChars) : c;
	}

	String escapedText(Predicate<Character> charPredicate, String... expectedItems) throws SegmentParseException {
		StringBuilder result = new StringBuilder();
		while (maybe(() -> result.append(escapedChar(charPredicate, expectedItems))).isPresent()) {
		}
		return result.toString();
	}

	String escapedTextUntil(Pattern endPattern, String... expectedItems) throws SegmentParseException {
		Matcher m = endPattern.matcher(line);
		m.region(index, line.length());
		int endIndex = m.find() ? m.start() : line.length();
		StringBuilder result = new StringBuilder();
		while (index < endIndex) {
			result.append('\\' == line.charAt(index) ? oneOf(escapedChars) : line.charAt(index));
			index++;
		}
		return result.toString();
	}

	<R> Optional<R> optional(Production<R> production) throws SegmentParseException {
		try {
			return Optional.of(production.run());
		}
		catch (SegmentParseExpectedException ex) {
			if (maybe(() -> expect(optionalRx, "-", "--")).isPresent()) {
				return Optional.empty();
			}
			throw ex;
		}
	}

	<R> Optional<R> optionalWithLookahead(Production<R> production) throws SegmentParseException {
		int start = index;
		try {
			return Optional.of(production.run());
		}
		catch (SegmentParseExpectedException ex) {
			index = start;
			if (maybe(() -> expect(optionalRx, "-", "--")).isPresent()) {
				return Optional.empty();
			}
			throw ex;
		}
	}

	<T> List<T> elementChars(Map<Character, T> elements, Set<T> requiredElements) throws SegmentParseException {
		final Map<Character, T> finalElements = new HashMap<>(elements);
		requiredElements = new HashSet<>(requiredElements);
		List<T> result = new ArrayList<>();
		while (!elements.isEmpty()) {
			T element;
			if (requiredElements.isEmpty()) {
				Optional<T> optional = maybe(() -> oneOf(finalElements));
				if (!optional.isPresent()) {
					break;
				}
				element = optional.get();
			}
			else {
				element = oneOf(elements);
			}
			result.add(element);
			char c = line.charAt(index - 1);
			finalElements.remove(Character.toLowerCase(c));
			finalElements.remove(Character.toUpperCase(c));
			requiredElements.remove(element);
		}
		return result;
	}

	static Map<String, Unit<Length>> createLengthUnits() {
		Map<String, Unit<Length>> lengthUnits = new HashMap<>();
		lengthUnits.put("meters", Length.meters);
		lengthUnits.put("meter", Length.meters);
		lengthUnits.put("m", Length.meters);
		lengthUnits.put("feet", Length.feet);
		lengthUnits.put("foot", Length.feet);
		lengthUnits.put("ft", Length.feet);
		lengthUnits.put("f", Length.feet);
		return Collections.unmodifiableMap(lengthUnits);
	}

	static Map<String, Unit<Angle>> createAzmUnits() {
		Map<String, Unit<Angle>> result = new HashMap<>();
		result.put("degrees", Angle.degrees);
		result.put("degree", Angle.degrees);
		result.put("deg", Angle.degrees);
		result.put("d", Angle.degrees);
		result.put("mills", Angle.milsNATO);
		result.put("mils", Angle.milsNATO);
		result.put("mil", Angle.milsNATO);
		result.put("m", Angle.milsNATO);
		result.put("grads", Angle.gradians);
		result.put("grad", Angle.gradians);
		result.put("g", Angle.gradians);
		return Collections.unmodifiableMap(result);
	}

	static Map<String, Unit<Angle>> createIncUnits() {
		Map<String, Unit<Angle>> result = new HashMap<>(createAzmUnits());
		result.put("percent", Angle.percentGrade);
		result.put("p", Angle.percentGrade);
		return Collections.unmodifiableMap(result);
	}

	static <V> Map<Character, V> addUppercaseKeys(Map<Character, V> map) {
		for (Map.Entry<Character, V> entry : new ArrayList<>(map.entrySet())) {
			map.put(Character.toUpperCase(entry.getKey()), entry.getValue());
		}
		return Collections.unmodifiableMap(map);
	}

	static Map<Character, Unit<Length>> createLengthUnitSuffixes() {
		Map<Character, Unit<Length>> result = new HashMap<>();
		result.put('m', Length.meters);
		result.put('f', Length.feet);
		result.put('i', Length.inches);
		return addUppercaseKeys(result);
	}

	static Map<Character, Unit<Angle>> createAzmUnitSuffixes() {
		Map<Character, Unit<Angle>> result = new HashMap<>();
		result.put('d', Angle.degrees);
		result.put('g', Angle.gradians);
		result.put('m', Angle.milsNATO);
		return addUppercaseKeys(result);
	}

	static Map<Character, Unit<Angle>> createIncUnitSuffixes() {
		Map<Character, Unit<Angle>> result = new HashMap<>(createAzmUnitSuffixes());
		result.put('p', Angle.percentGrade);
		return addUppercaseKeys(result);
	}

	static Map<Character, CardinalDirection> createCardinalDirections() {
		Map<Character, CardinalDirection> result = new HashMap<>();
		result.put('n', CardinalDirection.NORTH);
		result.put('s', CardinalDirection.SOUTH);
		result.put('e', CardinalDirection.EAST);
		result.put('w', CardinalDirection.WEST);
		return addUppercaseKeys(result);
	}

	static Map<Character, CardinalDirection> createNorthSouth() {
		Map<Character, CardinalDirection> result = new HashMap<>();
		result.put('n', CardinalDirection.NORTH);
		result.put('s', CardinalDirection.SOUTH);
		return addUppercaseKeys(result);
	}

	static Map<Character, CardinalDirection> createEastWest() {
		Map<Character, CardinalDirection> result = new HashMap<>();
		result.put('e', CardinalDirection.EAST);
		result.put('w', CardinalDirection.WEST);
		return addUppercaseKeys(result);
	}

	static Map<Character, CtMeasurement> createCtElements() {
		Map<Character, CtMeasurement> result = new HashMap<>();
		result.put('d', CtMeasurement.DISTANCE);
		result.put('a', CtMeasurement.AZIMUTH);
		result.put('v', CtMeasurement.INCLINATION);
		return addUppercaseKeys(result);
	}

	static Map<Character, RectMeasurement> createRectElements() {
		Map<Character, RectMeasurement> result = new HashMap<>();
		result.put('e', RectMeasurement.EAST);
		result.put('n', RectMeasurement.NORTH);
		result.put('u', RectMeasurement.UP);
		return addUppercaseKeys(result);
	}

	static Map<Character, LrudMeasurement> createLrudElements() {
		Map<Character, LrudMeasurement> result = new HashMap<>();
		result.put('l', LrudMeasurement.LEFT);
		result.put('r', LrudMeasurement.RIGHT);
		result.put('u', LrudMeasurement.UP);
		result.put('d', LrudMeasurement.DOWN);
		return addUppercaseKeys(result);
	}

	static <V> Map<String, V> putFirstLettersOfKeys(Map<String, V> map) {
		for (Map.Entry<String, V> entry : new ArrayList<>(map.entrySet())) {
			String firstLetter = entry.getKey().substring(0, 1);
			if (!map.containsKey(firstLetter)) {
				map.put(firstLetter, entry.getValue());
			}
		}
		return Collections.unmodifiableMap(map);
	}

	static Map<String, Boolean> createCorrectedValues() {
		Map<String, Boolean> result = new HashMap<>();
		result.put("corrected", true);
		result.put("normal", false);
		return putFirstLettersOfKeys(result);
	}

	static Map<String, CaseType> createCaseTypes() {
		Map<String, CaseType> result = new HashMap<>();
		result.put("upper", CaseType.UPPER);
		result.put("lower", CaseType.LOWER);
		result.put("mixed", CaseType.MIXED);
		return putFirstLettersOfKeys(result);
	}

	static Map<String, LrudType> createLrudTypes() {
		Map<String, LrudType> result = new HashMap<>();
		result.put("from", LrudType.FROM);
		result.put("f", LrudType.FROM);
		result.put("fb", LrudType.FROM_BISECTOR);
		result.put("to", LrudType.TO);
		result.put("t", LrudType.TO);
		result.put("tb", LrudType.TO_BISECTOR);
		return Collections.unmodifiableMap(result);
	}

	static Map<String, List<TapingMethodMeasurement>> createTapingMethods() {
		Map<String, List<TapingMethodMeasurement>> result = new HashMap<>();
		result
			.put("it", Arrays.asList(TapingMethodMeasurement.INSTRUMENT_HEIGHT, TapingMethodMeasurement.TARGET_HEIGHT));
		result.put("is", Arrays.asList(TapingMethodMeasurement.INSTRUMENT_HEIGHT, TapingMethodMeasurement.STATION));
		result.put("st", Arrays.asList(TapingMethodMeasurement.STATION, TapingMethodMeasurement.TARGET_HEIGHT));
		result.put("ss", Arrays.asList(TapingMethodMeasurement.STATION, TapingMethodMeasurement.STATION));
		return Collections.unmodifiableMap(result);
	}

	static Map<String, Integer> createPrefixDirectives() {
		Map<String, Integer> result = new HashMap<>();
		result.put("#p", 0);
		result.put("#prefix", 0);
		result.put("#prefix1", 0);
		result.put("#prefix2", 1);
		result.put("#prefix3", 2);
		return Collections.unmodifiableMap(result);
	}

	Map<String, VoidProduction> createUnitsOptionMap() {
		Map<String, VoidProduction> result = new HashMap<>();
		result.put("save", this::save);
		result.put("restore", this::restore);
		result.put("reset", this::reset_);
		result.put("m", this::meters);
		result.put("meters", this::meters);
		result.put("f", this::feet);
		result.put("feet", this::feet);
		result.put("ct", this::ct);
		result.put("d", this::d);
		result.put("distance", this::d);
		result.put("s", this::s);
		result.put("a", this::a);
		result.put("azimuth", this::a);
		result.put("ab", this::ab);
		result.put("a/ab", this::a_ab);
		result.put("v", this::v);
		result.put("vertical", this::v);
		result.put("vb", this::vb);
		result.put("v/vb", this::v_vb);
		result.put("o", this::order);
		result.put("order", this::order);
		result.put("decl", this::decl);
		result.put("grid", this::grid);
		result.put("rect", this::rect);
		result.put("incd", this::incd);
		result.put("inch", this::inch);
		result.put("incs", this::incs);
		result.put("inca", this::inca);
		result.put("incab", this::incab);
		result.put("incv", this::incv);
		result.put("incvb", this::incvb);
		result.put("typeab", this::typeab);
		result.put("typevb", this::typevb);
		result.put("case", this::case_);
		result.put("lrud", this::lrud);
		result.put("tape", this::tape);
		result.put("p", this::prefix1);
		result.put("prefix", this::prefix1);
		result.put("prefix1", this::prefix1);
		result.put("prefix2", this::prefix2);
		result.put("prefix3", this::prefix3);
		result.put("uvh", this::uvh);
		result.put("uvv", this::uvv);
		result.put("uv", this::uv);
		result.put("flag", this::flag);

		return Collections.unmodifiableMap(result);
	}

	Map<String, VoidProduction> createDirectivesMap() {
		Map<String, VoidProduction> result = new HashMap<>();
		result.put("#units", this::UnitsLine);
		result.put("#u", this::UnitsLine);
		result.put("#flag", this::flagLine);
		result.put("#f", this::flagLine);
		result.put("#fix", this::fixLine);
		result.put("#note", this::noteLine);
		result.put("#n", this::noteLine);
		result.put("#symbol", this::symbolLine);
		result.put("#sym", this::symbolLine);
		result.put("#segment", this::segmentLine);
		result.put("#seg", this::segmentLine);
		result.put("#s", this::segmentLine);
		result.put("#date", this::dateLine);
		result.put("#[", this::beginBlockCommentLine);
		result.put("#]", this::endBlockCommentLine);
		result.put("#prefix1", this::prefixLine);
		result.put("#prefix2", this::prefixLine);
		result.put("#prefix3", this::prefixLine);
		result.put("#prefix", this::prefixLine);
		result.put("#p", this::prefixLine);
		result.put("#", this::emptyDirectiveLine);
		return Collections.unmodifiableMap(result);
	}

	double approx(double val) {
		return Math.floor(val * 1e6) * 1e-6;
	}

	static final Map<String, Unit<Length>> lengthUnits = createLengthUnits();
	static final Map<String, Unit<Angle>> azmUnits = createAzmUnits();
	static final Map<String, Unit<Angle>> incUnits = createIncUnits();
	static final Map<Character, Unit<Length>> lengthUnitSuffixes = createLengthUnitSuffixes();
	static final Map<Character, Unit<Angle>> azmUnitSuffixes = createAzmUnitSuffixes();
	static final Map<Character, Unit<Angle>> incUnitSuffixes = createIncUnitSuffixes();
	static final Map<Character, CardinalDirection> cardinalDirections = createCardinalDirections();
	static final Map<Character, CardinalDirection> northSouth = createNorthSouth();
	static final Map<Character, CardinalDirection> eastWest = createEastWest();
	static final Map<Character, CtMeasurement> ctElements = createCtElements();
	static final Set<CtMeasurement> requiredCtElements =
		new HashSet<>(Arrays.asList(CtMeasurement.DISTANCE, CtMeasurement.AZIMUTH));
	static final Map<Character, RectMeasurement> rectElements = createRectElements();
	static final Set<RectMeasurement> requiredRectElements =
		new HashSet<>(Arrays.asList(RectMeasurement.EAST, RectMeasurement.NORTH));
	static final Map<Character, LrudMeasurement> lrudElements = createLrudElements();
	static final Set<LrudMeasurement> requiredLrudElements =
		new HashSet<>(
			Arrays.asList(LrudMeasurement.LEFT, LrudMeasurement.RIGHT, LrudMeasurement.UP, LrudMeasurement.DOWN));
	static final Map<String, Boolean> correctedValues = createCorrectedValues();
	static final Map<String, CaseType> caseTypes = createCaseTypes();
	static final Map<String, LrudType> lrudTypes = createLrudTypes();
	public static final Map<String, List<TapingMethodMeasurement>> tapingMethods = createTapingMethods();
	static final Map<String, Integer> prefixDirectives = createPrefixDirectives();

	static final Set<Character> escapedChars = new HashSet<>(Arrays.asList('r', 'n', 'f', 't', '"', '\\'));
	static final Pattern wordRx = Pattern.compile("\\w+");
	static final Pattern notSemicolonRx = Pattern.compile("[^;]+");
	static final Pattern UnitsOptionRx = Pattern.compile("[a-zA-Z_0-9/]*");
	static final Pattern directiveRx = Pattern.compile("#([]\\[]|[a-zA-Z0-9]+)?");
	static final Pattern macroNameRx = Pattern.compile("[^()=,,# \t]*");
	static final Pattern stationRx = Pattern.compile("([^:;,,#/ \t]*:){0,3}[^:;,,#/ \t]{1,8}");
	static final Pattern prefixRx = Pattern.compile("[^:;,,#/ \t]*");

	static final Pattern optionalRx = Pattern.compile("-+");
	static final Pattern optionalStationRx = Pattern.compile("-+");

	static final Pattern usDateRx1 = Pattern.compile("\\d{2}-\\d{2}-\\d{2,4}");
	static final Pattern usDateRx2 = Pattern.compile("\\d{2}/\\d{2}/\\d{2,4}");
	static final Pattern usDateRx3 = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}");
	static final Pattern usDateRx4 = Pattern.compile("\\d{4}/\\d{1,2}/\\d{1,2}");

	static final Pattern segmentPartRx = Pattern.compile("[^./\\;]?[^/\\;]+");

	final Map<String, VoidProduction> UnitsOptionMap = createUnitsOptionMap();
	final Map<String, VoidProduction> directivesMap = createDirectivesMap();

	static final UnitizedDouble<Angle> oneEighty = new UnitizedDouble<Angle>(180.0, Angle.degrees);

	public WallsSurveyParser() {
		this("");
	}

	public WallsSurveyParser(String line) {
		this(new Segment(line, null, 0, 0));
	}

	public WallsSurveyParser(Segment segment) {
		super(segment);
	}

	public void setVisitor(WallsVisitor visitor) {
		if (visitor == null) {
			this.visitor = new AbstractWallsVisitor();
		}
		else {
			this.visitor = visitor;
		}
	}

	UnitizedDouble<Length> unsignedLengthInches() throws SegmentParseException {
		expectIgnoreCase('i');
		double inches = unsignedDoubleLiteral();
		return new UnitizedDouble<Length>(inches, Length.inches);
	}

	UnitizedDouble<Length> unsignedLengthNonInches(Unit<Length> defaultUnit) throws SegmentParseException {
		double value = unsignedDoubleLiteral();
		Unit<Length> unit = oneOf(lengthUnitSuffixes, defaultUnit);
		if (unit == Length.inches) {
			double inches = unsignedDoubleLiteral();
			return new UnitizedDouble<Length>(value * 12 + inches, Length.inches);
		}
		return new UnitizedDouble<Length>(value, unit);
	}

	UnitizedDouble<Length> unsignedLength(Unit<Length> defaultUnit) throws SegmentParseException {
		return oneOf(() -> unsignedLengthNonInches(defaultUnit), () -> unsignedLengthInches());
	}

	UnitizedDouble<Length> length(Unit<Length> defaultUnit) throws SegmentParseException {
		boolean negate = maybe(() -> expect('-')).isPresent();
		UnitizedDouble<Length> length = unsignedLength(defaultUnit);
		return negate ? length.negate() : length;
	}

	Segment expectColon() throws SegmentParseException {
		return expect(':');
	}

	UnitizedDouble<Angle> unsignedAngle(Map<Character, Unit<Angle>> unitSuffixes, Unit<Angle> defaultUnit)
		throws SegmentParseException {
		Optional<Double> value = maybe(this::unsignedDoubleLiteral);
		if (maybe(this::expectColon).isPresent()) {
			Optional<Double> minutes = maybe(this::unsignedDoubleLiteral);
			Optional<Double> seconds = Optional.empty();
			if (maybe(this::expectColon).isPresent()) {
				seconds = maybe(this::unsignedDoubleLiteral);
			}
			if (!(value.isPresent() || minutes.isPresent() || seconds.isPresent())) {
				throwAllExpected();
			}

			return new UnitizedDouble<Angle>(
				value.orElse(0.0) + minutes.orElse(0.0) / 60.0 + seconds.orElse(0.0) / 3600,
				Angle.degrees);
		}
		else if (!value.isPresent()) {
			throwAllExpected();
		}
		Unit<Angle> unit = unitSuffixes == null ? defaultUnit : oneOf(unitSuffixes, defaultUnit);
		return new UnitizedDouble<Angle>(value.get(), unit);
	}

	UnitizedDouble<Angle> unsignedDmsAngle() throws SegmentParseException {
		Optional<Double> degrees = maybe(this::unsignedDoubleLiteral);
		expect(':');
		Optional<Double> minutes = maybe(this::unsignedDoubleLiteral);
		Optional<Double> seconds = Optional.empty();
		if (maybe(this::expectColon).isPresent()) {
			seconds = maybe(this::unsignedDoubleLiteral);
		}
		if (!(degrees.isPresent() || minutes.isPresent() || seconds.isPresent())) {
			throwAllExpected();
		}
		return new UnitizedDouble<Angle>(
			degrees.orElse(0.0) + minutes.orElse(0.0) / 60.0 + seconds.orElse(0.0) / 3600,
			Angle.degrees);
	}

	UnitizedDouble<Angle> latitude() throws SegmentParseException {
		int start = index;
		CardinalDirection side = oneOf(northSouth);
		UnitizedDouble<Angle> latitude = unsignedAngle(null, Angle.degrees);

		if (approx(latitude.get(Angle.degrees)) > 90.0) {
			throw new SegmentParseException("latitude out of range", line.substring(start, index));
		}

		return side == CardinalDirection.SOUTH ? latitude.negate() : latitude;
	}

	UnitizedDouble<Angle> longitude() throws SegmentParseException {
		int start = index;
		CardinalDirection side = oneOf(eastWest);
		UnitizedDouble<Angle> longitude = unsignedAngle(null, Angle.degrees);

		if (approx(longitude.get(Angle.degrees)) > 180.0) {
			throw new SegmentParseException("longitude out of range", line.substring(start, index));
		}

		return side == CardinalDirection.WEST ? longitude.negate() : longitude;
	}

	UnitizedDouble<Angle> nonQuadrantAzimuth(Unit<Angle> defaultUnit) throws SegmentParseException {
		int start = index;

		UnitizedDouble<Angle> result = unsignedAngle(azmUnitSuffixes, defaultUnit);

		if (approx(result.get(Angle.degrees)) > 360.0) {
			throw new SegmentParseException("azimuth out of range", line.substring(start, index));
		}
		if (result.get(Angle.degrees) == 360.0) {
			visitor
				.message(
					new WallsMessage(
						"warning",
						"why do you have an azimuth of 360 degrees instead of 0?",
						line.substring(start, index)));
		}

		return result;
	}

	UnitizedDouble<Angle> quadrantAzimuth() throws SegmentParseException {
		CardinalDirection from = oneOf(cardinalDirections);

		int start = index;
		Optional<UnitizedDouble<Angle>> angle = maybe(() -> nonQuadrantAzimuth(Angle.degrees));
		if (angle.isPresent()) {
			if (approx(angle.get().get(Angle.degrees)) > 90.0) {
				throw new SegmentParseException("azimuth out of range", line.substring(start, index));
			}
			if (angle.get().get(Angle.degrees) == 90.0) {
				visitor
					.message(
						new WallsMessage(
							"warning",
							"why are you using 90 degrees in a quadrant azimuth?",
							line.substring(start, index)));
			}

			CardinalDirection to =
				oneOf(from == CardinalDirection.NORTH || from == CardinalDirection.SOUTH ? eastWest : northSouth);

			return from.quadrant(to, angle.get());
		}
		return from.angle;
	}

	UnitizedDouble<Angle> azimuth(Unit<Angle> defaultUnit) throws SegmentParseException {
		return oneOf(() -> quadrantAzimuth(), () -> nonQuadrantAzimuth(defaultUnit));
	}

	UnitizedDouble<Angle> azimuthOffset(Unit<Angle> defaultUnit) throws SegmentParseException {
		Optional<Double> signum = maybe(() -> oneOf(signSignums));
		return nonQuadrantAzimuth(defaultUnit).mul(signum.orElse(1.0));
	}

	UnitizedDouble<Angle> unsignedInclination(Unit<Angle> defaultUnit) throws SegmentParseException {
		int start = index;
		UnitizedDouble<Angle> result = unsignedAngle(incUnitSuffixes, defaultUnit);

		if (approx(result.get(Angle.degrees)) > 90.0) {
			throw new SegmentParseException("inclination out of range", line.substring(start, index));
		}

		return result;
	}

	UnitizedDouble<Angle> inclination(Unit<Angle> defaultUnit) throws SegmentParseException {
		int start = index;
		Optional<Double> signum = maybe(() -> oneOf(signSignums));
		UnitizedDouble<Angle> angle = unsignedInclination(defaultUnit);

		if (signum.isPresent()) {
			if (angle.get(angle.unit) == 0.0) {
				visitor
					.message(
						new WallsMessage(
							"warning",
							"why do you have an inclination of -0 instead of just 0?",
							line.substring(start, index)));
			}
			return angle.mul(signum.get());
		}
		return angle;
	}

	VarianceOverride varianceOverride(Unit<Length> defaultUnit) throws SegmentParseException {
		return oneOf(
			() -> floatedVectorVarianceOverride(),
			() -> floatedTraverseVarianceOverride(),
			() -> lengthVarianceOverride(defaultUnit),
			() -> rmsErrorVarianceOverride(defaultUnit),
			() -> null);
	}

	VarianceOverride floatedVectorVarianceOverride() throws SegmentParseException {
		expect('?');
		return VarianceOverride.FLOATED;
	}

	VarianceOverride floatedTraverseVarianceOverride() throws SegmentParseException {
		expect('*');
		return VarianceOverride.FLOATED_TRAVERSE;
	}

	VarianceOverride lengthVarianceOverride(Unit<Length> defaultUnit) throws SegmentParseException {
		return new VarianceOverride.Length(unsignedLength(defaultUnit));
	}

	VarianceOverride rmsErrorVarianceOverride(Unit<Length> defaultUnit) throws SegmentParseException {
		expectIgnoreCase('r');
		return new VarianceOverride.RMSError(unsignedLength(defaultUnit));
	}

	String quotedTextOrNonwhitespace() throws SegmentParseException {
		return oneOf(() -> quotedText(), () -> nonwhitespace().toString());
	}

	String quotedText() throws SegmentParseException {
		expect('"');
		String result = escapedText(c -> c != '"', "<QUOTED TEXT>");
		expect('"');
		return result;
	}

	String movePastEndQuote() {
		int start = index++;
		while (index < line.length()) {
			char c = line.charAt(index++);
			if (c == '\\') {
				if (index < line.length()) {
					index++;
				}
			}
			else if (c == '"') {
				break;
			}
		}

		return line.toString().substring(start, index);
	}

	public void parseFile(File file) throws IOException, SegmentParseException {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			int lineNumber = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				parseLine(new Segment(line, file, lineNumber++, 0));
			}
		}
	}

	public void parseSurveyEntry(WallsProjectEntry entry) throws SegmentParseException, IOException {
		if (!entry.isSurvey()) {
			throw new IllegalArgumentException("entry must be a survey");
		}
		for (Segment units : entry.allOptions()) {
			parseUnitsOptions(units);
		}
		if (!entry.segment().isEmpty()) {
			segment.clear();
			segment.addAll(entry.segment());
			rootSegment.clear();
			rootSegment.addAll(entry.segment());
		}
		parseFile(entry.absolutePath().toFile());
	}

	public void parseLine(String line) throws SegmentParseException {
		reset(new Segment(line, null, 0, 0));
		parseLine();
	}

	public void parseLine(Segment line) throws SegmentParseException {
		reset(line);
		parseLine();
	}

	void parseLine() throws SegmentParseException {
		maybeWhitespace();

		if (isAtEnd()) {
			return;
		}

		if (inBlockComment) {
			throwAllExpected(() -> oneOfWithLookahead(this::endBlockCommentLine, this::insideBlockCommentLine));
		}
		else {
			throwAllExpected(() -> oneOf(this::comment, this::directiveLine, this::vectorLine));
		}
	}

	void directiveLine() throws SegmentParseException {
		int start = index;
		VoidProduction fixLine = this::fixLine;
		VoidProduction directive = oneOfLowercase(directiveRx, directivesMap);
		index = start;
		// TODO check this!!
		if (!directive.equals(fixLine)) {
			replaceMacros();
		}
		directive.run();
	}

	String replaceMacro() throws SegmentParseException {
		index += 2;
		int start = index;
		while (index < line.length()) {
			char c = line.charAt(index++);
			if (c == ')') {
				Segment macroName = line.substring(start, index - 1);
				if (!macros.containsKey(macroName.toString())) {
					throw new SegmentParseException("macro not defined", macroName);
				}
				return macros.get(macroName.toString());
			}
			else if (Character.isWhitespace(c)) {
				throw new SegmentParseExpectedException(line.charAtAsSegment(index - 1), "<NONWHITESPACE>");
			}
		}
		throw new SegmentParseExpectedException(line.charAtAsSegment(index), Arrays.asList("<NONWHITESPACE>", ")"));
	}

	void replaceMacros() throws SegmentParseException {
		StringBuilder newLine = new StringBuilder();

		boolean replaced = false;

		while (index < line.length()) {
			char c = line.charAt(index);
			switch (c) {
			case '"':
				newLine.append(movePastEndQuote());
				break;
			case '$':
				if (index + 1 < line.length() && line.charAt(index + 1) == '(') {
					replaced = true;
					newLine.append(replaceMacro());
					break;
				}
			default:
				newLine.append(c);
				index++;
				break;
			}
		}

		index = 0;

		if (replaced) {
			line = new Segment(newLine.toString(), line.source, line.startLine, line.startCol);
		}
	}

	void beginBlockCommentLine() throws SegmentParseException {
		maybeWhitespace();
		expect("#[");
		inBlockComment = true;
	}

	void endBlockCommentLine() throws SegmentParseException {
		maybeWhitespace();
		expect("#]");
		remaining();
		inBlockComment = false;
	}

	void insideBlockCommentLine() {
		visitor.parsedComment(remaining().toString());
	}

	Segment untilComment(List<String> expectedItems) throws SegmentParseException {
		return expect(notSemicolonRx, expectedItems);
	}

	void segmentLine() throws SegmentParseException {
		maybeWhitespace();
		segment.clear();
		segment.addAll(segmentDirective());
		maybeWhitespace();
		inlineCommentOrEndOfLine();
	}

	void segmentSeparator() throws SegmentParseException {
		oneOf(() -> expect("/"), () -> expect("\\"));
	}

	String initialSegmentPart() throws SegmentParseException {
		return oneOf(() -> expect("."), () -> expect("..")).toString();
	}

	String nonInitialSegmentPart() throws SegmentParseException {
		return expect(segmentPartRx, "<PATH ELEMENT>").toString();
	}

	List<String> segmentPath() throws SegmentParseException {
		final List<String> path = new ArrayList<>();

		oneOf(() -> {
			segmentSeparator();
			path.addAll(rootSegment);
		}, () -> {
			path.addAll(segment);
			while (maybe(() -> {
				if (initialSegmentPart().equals("..") && !path.isEmpty()) {
					path.remove(path.size() - 1);
				}
			}).isPresent())
				;
		});

		while (maybe(() -> path.add(nonInitialSegmentPart())).isPresent())
			;

		if (!path.isEmpty()) {
			int last = path.size() - 1;
			path.set(last, path.get(last).replaceAll("\\s*$", ""));
		}

		return path;
	}

	List<String> segmentDirective() throws SegmentParseException {
		oneOf(() -> expectIgnoreCase("#segment"), () -> expectIgnoreCase("#seg"), () -> expectIgnoreCase("#s"));

		List<String> result = segment;

		if (maybeWhitespace().isPresent()) {
			result = maybe(() -> segmentPath()).orElse(segment);
		}

		return result == null ? null : new ArrayList<>(result);
	}

	void prefixLine() throws SegmentParseException {
		maybeWhitespace();
		prefixDirective();
		maybeWhitespace();
		inlineCommentOrEndOfLine();
	}

	void prefixDirective() throws SegmentParseException {
		int prefixIndex = oneOfLowercase(nonwhitespaceRx, prefixDirectives);

		if (maybeWhitespace().isPresent()) {
			units.setPrefix(prefixIndex, expect(prefixRx, "<PREFIX>").toString());
		}
		else {
			units.setPrefix(prefixIndex, "");
		}
	}

	void noteLine() throws SegmentParseException {
		maybeWhitespace();
		noteDirective();
		maybeWhitespace();
		inlineCommentOrEndOfLine();
	}

	void noteDirective() throws SegmentParseException {
		oneOf(() -> expectIgnoreCase("#note"), () -> expectIgnoreCase("#n"));

		whitespace();
		String _station = station().toString();
		whitespace();
		String _note = escapedText(c -> c != ';', "<NOTE>");

		visitor.parsedNote(_station, _note);
	}

	void flagLine() throws SegmentParseException {
		maybeWhitespace();
		flagDirective();
		maybeWhitespace();
		inlineCommentOrEndOfLine();
	}

	void flagDirective() throws SegmentParseException {
		oneOf(() -> expectIgnoreCase("#flag"), () -> expectIgnoreCase("#f"));

		List<String> stations = new ArrayList<>();

		maybeWhitespace();

		do {
			Optional<String> _station = maybe(() -> station().toString());
			if (!_station.isPresent())
				break;
			stations.add(_station.get());
		} while (maybe(() -> oneOf(() -> whitespace(), () -> expect(','))).isPresent());

		Optional<String> _flag = maybe(this::slashPrefixedFlag);
		maybeWhitespace();

		if (stations.isEmpty()) {
			units.setFlag(_flag.orElse(null));
		}
		visitor.parsedFlag(stations, _flag.orElse(null));

		inlineCommentOrEndOfLine();
	}

	String slashPrefixedFlag() throws SegmentParseException {
		expect('/');
		return expect(notSemicolonRx, "<FLAG>").toString();
	}

	void symbolLine() throws SegmentParseException {
		maybeWhitespace();

		oneOf(() -> expectIgnoreCase("#symbol"), () -> expectIgnoreCase("#sym"));

		// ignore the rest for now
		remaining();
	}

	void dateLine() throws SegmentParseException {
		maybeWhitespace();
		visitor.parsedDate(dateDirective());
		maybeWhitespace();
		inlineCommentOrEndOfLine();
	}

	Date dateDirective() throws SegmentParseException {
		expectIgnoreCase("#date");
		whitespace();
		return oneOf(() -> usDate1(), () -> usDate2(), () -> usDate3(), () -> usDate4());
	}

	void emptyDirectiveLine() throws SegmentParseException {
		maybeWhitespace();
	}

	static DateFormat fullDateFormat1 = new SimpleDateFormat("MM-dd-yyyy");
	static DateFormat shortDateFormat1 = new SimpleDateFormat("MM-dd-yy");
	static DateFormat fullDateFormat2 = new SimpleDateFormat("MM/dd/yyyy");
	static DateFormat shortDateFormat2 = new SimpleDateFormat("MM/dd/yy");
	static DateFormat fullDateFormat3 = new SimpleDateFormat("yyyy-M-d");
	static DateFormat fullDateFormat4 = new SimpleDateFormat("yyyy/M/d");

	Date usDate1() throws SegmentParseExpectedException {
		String str = expect(usDateRx1, "<DATE>").toString();
		try {
			return (str.length() > 8 ? fullDateFormat1 : shortDateFormat1).parse(str);
		}
		catch (ParseException ex) {
			// should never happen because of regex
			return null;
		}
	}

	Date usDate2() throws SegmentParseExpectedException {
		String str = expect(usDateRx2, "<DATE>").toString();
		try {
			return (str.length() > 8 ? fullDateFormat2 : shortDateFormat2).parse(str);
		}
		catch (ParseException ex) {
			// should never happen because of regex
			return null;
		}
	}

	Date usDate3() throws SegmentParseExpectedException {
		String str = expect(usDateRx3, "<DATE>").toString();
		try {
			return fullDateFormat3.parse(str);
		}
		catch (ParseException ex) {
			// should never happen because of regex
			return null;
		}
	}

	Date usDate4() throws SegmentParseExpectedException {
		String str = expect(usDateRx4, "<DATE>").toString();
		try {
			return fullDateFormat4.parse(str);
		}
		catch (ParseException ex) {
			// should never happen because of regex
			return null;
		}
	}

	void UnitsLine() throws SegmentParseException {
		maybeWhitespace();
		oneOf(() -> expectIgnoreCase("#units"), () -> expectIgnoreCase("#u"));

		visitor.willParseUnits();

		if (maybeWhitespace().isPresent()) {
			UnitsOptions();
			visitor.parsedUnits();
		}
	}

	void parseUnitsOptions(Segment options) throws SegmentParseException {
		reset(options);
		UnitsOptions();
		// visitor.parsedUnits();
	}

	void UnitsOptions() throws SegmentParseException {
		boolean gotOne = false;
		while (!maybe(() -> inlineCommentOrEndOfLine()).isPresent()) {
			if (gotOne) {
				whitespaceAndOrComma();
			}
			else {
				gotOne = true;
			}

			maybe(() -> oneOf(this::UnitsOption, this::macroOption));
		}
	}

	void UnitsOption() throws SegmentParseException {
		VoidProduction option = oneOfLowercase(UnitsOptionRx, UnitsOptionMap);
		option.run();
	}

	void macroOption() throws SegmentParseException {
		expect('$');
		String macroName = expect(macroNameRx, "<MACRO NAME>").toString();
		String macroValue = "";
		if (maybeChar('=')) {
			macroValue = quotedTextOrNonwhitespace();
		}
		macros.put(macroName, macroValue);
	}

	void save() throws SegmentParseException {
		if (unitsStack.size() >= 10) {
			throw new SegmentParseException("units stack is full", line.substring(index - 4, index));
		}
		unitsStack.push(units.toImmutable());
	}

	void restore() throws SegmentParseException {
		if (unitsStack.isEmpty()) {
			throw new SegmentParseException("units stack is empty", line.substring(index - 7, index));
		}
		units = unitsStack.pop().toMutable();
	}

	void reset_() {
		units = new MutableWallsUnits();
	}

	void meters() {
		units.setDUnit(Length.meters);
		units.setSUnit(Length.meters);
	}

	void feet() {
		units.setDUnit(Length.feet);
		units.setSUnit(Length.feet);
	}

	void ct() {
		units.setVectorType(VectorType.COMPASS_AND_TAPE);
	}

	void d() throws SegmentParseException {
		expect('=');
		units.setDUnit(oneOfLowercase(nonwhitespaceRx, lengthUnits));
	}

	void s() throws SegmentParseException {
		expect('=');
		units.setSUnit(oneOfLowercase(nonwhitespaceRx, lengthUnits));
	}

	void a() throws SegmentParseException {
		expect('=');
		units.setAUnit(oneOfLowercase(nonwhitespaceRx, azmUnits));
	}

	void ab() throws SegmentParseException {
		expect('=');
		units.setAbUnit(oneOfLowercase(nonwhitespaceRx, azmUnits));
	}

	void a_ab() throws SegmentParseException {
		expect('=');
		Unit<Angle> unit = oneOfLowercase(nonwhitespaceRx, azmUnits);
		units.setAUnit(unit);
		units.setAbUnit(unit);
	}

	void v() throws SegmentParseException {
		expect('=');
		units.setVUnit(oneOfLowercase(nonwhitespaceRx, incUnits));
	}

	void vb() throws SegmentParseException {
		expect('=');
		units.setVbUnit(oneOfLowercase(nonwhitespaceRx, incUnits));
	}

	void v_vb() throws SegmentParseException {
		expect('=');
		Unit<Angle> unit = oneOfLowercase(nonwhitespaceRx, incUnits);
		units.setVUnit(unit);
		units.setVbUnit(unit);
	}

	void order() throws SegmentParseException {
		expect('=');
		oneOf(this::ctOrder, this::rectOrder);
	}

	void ctOrder() throws SegmentParseException {
		units.setCtOrder(elementChars(ctElements, requiredCtElements));
	}

	void rectOrder() throws SegmentParseException {
		units.setRectOrder(elementChars(rectElements, requiredRectElements));
	}

	void decl() throws SegmentParseException {
		expect('=');
		units.setDecl(azimuthOffset(Angle.degrees));
	}

	void grid() throws SegmentParseException {
		expect('=');
		units.setGrid(azimuthOffset(Angle.degrees));
	}

	void rect() throws SegmentParseException {
		if (maybeChar('=')) {
			units.setRect(azimuthOffset(Angle.degrees));
		}
		else {
			units.setVectorType(VectorType.RECTANGULAR);
		}
	}

	void incd() throws SegmentParseException {
		expect('=');
		units.setIncd(length(units.getDUnit()));
	}

	void inch() throws SegmentParseException {
		expect('=');
		units.setInch(length(units.getDUnit()));
	}

	void incs() throws SegmentParseException {
		expect('=');
		units.setIncs(length(units.getSUnit()));
	}

	void inca() throws SegmentParseException {
		expect('=');
		units.setInca(azimuthOffset(units.getAUnit()));
	}

	void incab() throws SegmentParseException {
		expect('=');
		units.setIncab(azimuthOffset(units.getAbUnit()));
	}

	void incv() throws SegmentParseException {
		expect('=');
		units.setIncv(inclination(units.getVUnit()));
	}

	void incvb() throws SegmentParseException {
		expect('=');
		units.setIncvb(inclination(units.getVbUnit()));
	}

	void typeab() throws SegmentParseException {
		expect('=');
		units.setTypeabCorrected(oneOfLowercase(wordRx, correctedValues));
		if (maybeChar(',')) {
			Optional<Double> tolerance = maybe(this::unsignedDoubleLiteral);
			if (!tolerance.isPresent())
				return;
			units.setTypeabTolerance(new UnitizedDouble<Angle>(tolerance.get(), Angle.degrees));
			if (maybe(() -> expectIgnoreCase(",x")).isPresent()) {
				units.setTypeabNoAverage(true);
			}
			else {
				units.setTypeabNoAverage(false);
			}
		}
		else {
			units.setTypeabTolerance(new UnitizedDouble<Angle>(2.0, Angle.degrees));
		}
	}

	void typevb() throws SegmentParseException {
		expect('=');
		units.setTypevbCorrected(oneOfLowercase(wordRx, correctedValues));
		if (maybeChar(',')) {
			Optional<Double> tolerance = maybe(this::unsignedDoubleLiteral);
			if (!tolerance.isPresent())
				return;
			units.setTypevbTolerance(new UnitizedDouble<Angle>(tolerance.get(), Angle.degrees));
			if (maybe(() -> expectIgnoreCase(",x")).isPresent()) {
				units.setTypevbNoAverage(true);
			}
			else {
				units.setTypevbNoAverage(false);
			}
		}
		else {
			units.setTypevbTolerance(new UnitizedDouble<Angle>(2.0, Angle.degrees));
		}
	}

	void case_() throws SegmentParseException {
		expect('=');
		units.setCase_(oneOfLowercase(nonwhitespaceRx, caseTypes));
	}

	void lrud() throws SegmentParseException {
		expect('=');
		units.setLrud(oneOfLowercase(wordRx, lrudTypes));
		if (maybeChar(':')) {
			lrudOrder();
		}
		else {
			units
				.setLrudOrder(
					Arrays
						.asList(LrudMeasurement.LEFT, LrudMeasurement.RIGHT, LrudMeasurement.UP, LrudMeasurement.DOWN));
		}
	}

	void lrudOrder() throws SegmentParseException {
		units.setLrudOrder(elementChars(lrudElements, requiredLrudElements));
	}

	void prefix1() throws SegmentParseException {
		prefix(0);
	}

	void prefix2() throws SegmentParseException {
		prefix(1);
	}

	void prefix3() throws SegmentParseException {
		prefix(2);
	}

	void prefix(int index) throws SegmentParseException {
		String prefix = null;

		if (maybeChar('=')) {
			prefix = expect(prefixRx, "<PREFIX>").toString();
		}
		units.setPrefix(index, prefix);
	}

	void tape() throws SegmentParseException {
		expect('=');
		units.setTape(oneOfLowercase(nonwhitespaceRx, tapingMethods));
	}

	void uvh() throws SegmentParseException {
		expect('=');
		units.setUvh(unsignedDoubleLiteral());
	}

	void uvv() throws SegmentParseException {
		expect('=');
		units.setUvv(unsignedDoubleLiteral());
	}

	void uv() throws SegmentParseException {
		expect('=');
		double value = unsignedDoubleLiteral();
		units.setUvv(value);
		units.setUvh(value);
	}

	void flag() throws SegmentParseException {
		String flag = null;
		if (maybeChar('=')) {
			flag = quotedTextOrNonwhitespace();
		}
		units.setFlag(flag);
	}

	void vectorLine() throws SegmentParseException {
		maybeWhitespace();
		fromStation();
		whitespaceAndOrComma();
		afterFromStation();
		maybeWhitespaceAndOrComma();
		endOfLine();
		vector.date = date;
		vector.units = units.toImmutable();
		visitor.parsedVector(vector);
	}

	Segment station() throws SegmentParseException {
		return expect(stationRx, "<STATION>");
	}

	void fromStation() throws SegmentParseException {
		fromStationSegment = station();
		String from = fromStationSegment.toString();
		if (optionalStationRx.matcher(from).matches()) {
			from = null;
		}
		vector = new Vector();
		vector.segment = segment == null ? null : new ArrayList<>(segment);
		vector.sourceSegment = line;
		vector.from = from;
	}

	void afterFromStation() throws SegmentParseException {
		oneOfWithLookahead(() -> {
			// clear all measurements
			String from = vector.from;
			vector = new Vector();
			vector.segment = segment == null ? null : new ArrayList<>(segment);
			vector.sourceSegment = line;
			vector.from = from;
			lruds();
			afterLruds();
		}, () -> {
			toStation();
			afterToStation();
		});
	}

	void toStation() throws SegmentParseException {
		toStationSegment = station();
		String to = toStationSegment.toString();
		if (optionalStationRx.matcher(to).matches()) {
			to = null;
		}
		if (vector.from == null && to == null) {
			throw new SegmentParseException("from and to station can't both be omitted", toStationSegment);
		}
		vector.to = to;
	}

	boolean isAzimuthOptional() {
		return vector.distance != null
			&& Vector.isVertical(units.averageInclination(vector.frontsightInclination, vector.backsightInclination));
	}

	boolean isInclinationOptional() {
		return vector.distance != null && vector.hasAzimuth() && vector.isSplay();
	}

	boolean isCtMeasurementOptional(CtMeasurement elem) {
		switch (elem) {
		case AZIMUTH:
			return isAzimuthOptional();
		case INCLINATION:
			return isInclinationOptional();
		default:
			return false;
		}
	}

	void afterToStation() throws SegmentParseException {
		if (Objects.equals(vector.from, vector.to)) {
			vector.to = null;
			afterVectorMeasurements();
			return;
		}

		if (vector.isSplay()) {
			if (maybeWithLookahead(() -> afterVectorMeasurements()).isPresent()) {
				return;
			}
		}

		whitespaceAndOrComma();

		int k = 0;
		if (units.getVectorType() == VectorType.RECTANGULAR) {
			for (RectMeasurement elem : units.getRectOrder()) {
				if (k++ > 0) {
					whitespaceAndOrComma();
				}
				rectMeasurement(elem);
			}
		}
		else {
			for (CtMeasurement elem : units.getCtOrder()) {
				if (isCtMeasurementOptional(elem)) {
					maybeWithLookahead(() -> {
						whitespaceAndOrComma();
						ctMeasurement(elem);
					});
					break;
				}
				if (k++ > 0) {
					whitespaceAndOrComma();
				}
				ctMeasurement(elem);
			}
		}

		if (units.getVectorType() == VectorType.COMPASS_AND_TAPE) {
			if (!UnitizedDouble.isFinite(vector.frontsightAzimuth)
				&& !UnitizedDouble.isFinite(vector.backsightAzimuth)
				&& !Vector
					.isVertical(units.averageInclination(vector.frontsightInclination, vector.backsightInclination))) {
				throw new SegmentParseException("azimuth can only be omitted for vertical shots", azmSegment);
			}

			maybeWithLookahead(() -> {
				whitespaceAndOrComma();
				instrumentHeight();
			});
			maybeWithLookahead(() -> {
				whitespaceAndOrComma();
				targetHeight();
			});
		}

		afterVectorMeasurements();
	}

	void rectMeasurement(RectMeasurement elem) throws SegmentParseException {
		UnitizedDouble<Length> measurement = length(units.getDUnit());
		switch (elem) {
		case EAST:
			vector.east = measurement;
			break;
		case NORTH:
			vector.north = measurement;
			break;
		case UP:
			vector.elevation = measurement;
			break;
		}
	}

	void ctMeasurement(CtMeasurement elem) throws SegmentParseException {
		switch (elem) {
		case DISTANCE:
			distance();
			break;
		case AZIMUTH:
			azimuth();
			break;
		case INCLINATION:
			inclination();
			break;
		}
	}

	void checkCorrectedSign(int segStart, UnitizedDouble<Length> measurement, UnitizedDouble<Length> correction)
		throws SegmentParseException {
		if ((measurement.isPositive() && measurement.add(correction).isNegative())
			|| (measurement.isNegative() && measurement.add(correction).isPositive())) {
			throw new SegmentParseException("correction changes sign of measurement", line.substring(segStart, index));
		}
	}

	void distance() throws SegmentParseException {
		int start = index;
		UnitizedDouble<Length> dist = unsignedLength(units.getDUnit());
		checkCorrectedSign(start, dist, units.getIncd());
		vector.distance = dist;
	}

	UnitizedDouble<Angle> azmDifference(UnitizedDouble<Angle> fs, UnitizedDouble<Angle> bs)
		throws SegmentParseException {
		if (!units.isTypeabCorrected()) {
			bs = bs.compareTo(oneEighty) < 0 ? bs.add(oneEighty) : bs.sub(oneEighty);
		}
		UnitizedDouble<Angle> diff = fs.sub(bs).abs();
		return diff.compareTo(oneEighty) > 0 ? new UnitizedDouble<Angle>(360.0, Angle.degrees).sub(diff) : diff;
	}

	void azimuth() throws SegmentParseException {
		int start = index;

		oneOf(() -> {
			vector.frontsightAzimuth = optional(() -> azimuth(units.getAUnit())).orElse(null);
			maybe(() -> {
				expect('/');
				vector.backsightAzimuth = optional(() -> azimuth(units.getAbUnit())).orElse(null);
			});
		}, () -> {
			expect('/');
			vector.backsightAzimuth = optional(() -> azimuth(units.getAbUnit())).orElse(null);
		});

		azmSegment = line.substring(start, index);

		if (vector.frontsightAzimuth != null && vector.backsightAzimuth != null) {
			UnitizedDouble<Angle> diff = azmDifference(vector.frontsightAzimuth, vector.backsightAzimuth);
			if (diff.compareTo(units.getTypeabTolerance().mul(1 + 1e-6)) > 0) {
				visitor
					.message(
						new WallsMessage(
							"warning",
							String
								.format(
									"azimuth fs/bs difference (%1$s) exceeds tolerance (%2$s)",
									diff,
									units.getTypeabTolerance()),
							azmSegment));
			}
		}
	}

	UnitizedDouble<Angle> incDifference(UnitizedDouble<Angle> fs, UnitizedDouble<Angle> bs)
		throws SegmentParseException {
		return units.isTypevbCorrected() ? fs.sub(bs).abs() : fs.add(bs).abs();
	}

	void inclination() throws SegmentParseException {
		int start = index;

		oneOf(() -> {
			vector.frontsightInclination = optionalWithLookahead(() -> inclination(units.getVUnit())).orElse(null);
			maybe(() -> {
				expect('/');
				vector.backsightInclination = optionalWithLookahead(() -> inclination(units.getVbUnit())).orElse(null);
			});
		}, () -> {
			expect('/');
			vector.backsightInclination = optionalWithLookahead(() -> inclination(units.getVbUnit())).orElse(null);
		});

		incSegment = line.substring(start, index);

		if (vector.frontsightInclination == null && vector.backsightInclination == null) {
			vector.frontsightInclination = new UnitizedDouble<Angle>(0, units.getVUnit());
		}
		else if (vector.frontsightInclination != null && vector.backsightInclination != null) {
			UnitizedDouble<Angle> diff = incDifference(vector.frontsightInclination, vector.backsightInclination);
			if (diff.compareTo(units.getTypevbTolerance().mul(1 + 1e-6)) > 0) {
				visitor
					.message(
						new WallsMessage(
							"warning",
							String
								.format(
									"inclination fs/bs difference (%1$s) exceeds tolerance (%2$s)",
									diff.toString(),
									units.getTypevbTolerance().toString()),
							incSegment));
			}
		}
	}

	void instrumentHeight() throws SegmentParseException {
		int start = index;
		Optional<UnitizedDouble<Length>> ih = optional(() -> length(units.getSUnit()));
		if (ih.isPresent()) {
			checkCorrectedSign(start, ih.get(), units.getIncs());
			vector.instrumentHeight = ih.get();
		}
	}

	void targetHeight() throws SegmentParseException {
		int start = index;
		Optional<UnitizedDouble<Length>> th = optional(() -> length(units.getSUnit()));
		if (th.isPresent()) {
			checkCorrectedSign(start, th.get(), units.getIncs());
			vector.targetHeight = th.get();
		}
	}

	void lrudMeasurement(LrudMeasurement elem) throws SegmentParseException {
		int start = index;
		Optional<UnitizedDouble<Length>> measurement = optionalWithLookahead(() -> length(units.getSUnit()));
		if (measurement.isPresent()) {
			warnIfNegative(measurement.get(), start, "LRUD");
			checkCorrectedSign(start, measurement.get(), units.getIncs());
		}
		switch (elem) {
		case LEFT:
			vector.left = measurement.orElse(null);
			break;
		case RIGHT:
			vector.right = measurement.orElse(null);
			break;
		case UP:
			vector.up = measurement.orElse(null);
			break;
		case DOWN:
			vector.down = measurement.orElse(null);
			break;
		}
	}

	<T extends UnitType<T>> void warnIfNegative(UnitizedDouble<T> measurement, int start, String name) {
		if (UnitizedDouble.isFinite(measurement) && measurement.get(measurement.unit) < 0) {
			visitor
				.message(
					new WallsMessage(
						"warning",
						String.format("negative %1$s measurement", name),
						line.substring(start, index)));
		}
	}

	void afterVectorMeasurements() throws SegmentParseException {
		maybeWithLookahead(() -> {
			whitespaceAndOrComma();
			varianceOverrides();
		});
		afterVectorVarianceOverrides();
	}

	void varianceOverrides() throws SegmentParseException {
		varianceOverrides(vector);
	}

	void varianceOverrides(HasVarianceOverrides target) throws SegmentParseException {
		expect('(');
		maybeWhitespace();
		VarianceOverride horizontal = varianceOverride(units.getDUnit());
		target.setHorizontalVarianceOverride(horizontal);
		maybeWhitespace();
		if (maybeChar(',')) {
			maybeWhitespace();
			VarianceOverride vertical = varianceOverride(units.getDUnit());
			if (horizontal == null && vertical == null) {
				throw allExpected();
			}
			target.setVerticalVarianceOverride(vertical);
		}
		else if (horizontal != null) {
			// no this is not a typo
			target.setVerticalVarianceOverride(horizontal);
		}
		expect(')');
	}

	void afterVectorVarianceOverrides() throws SegmentParseException {
		maybeWithLookahead(() -> {
			whitespaceAndOrComma();
			lruds();
		});
		afterLruds();
	}

	static final Pattern lrudStartRx = Pattern.compile("[<*]");
	static final Pattern chevronRx = Pattern.compile("[<>]");

	void lruds() throws SegmentParseException {
		Segment start = expect(lrudStartRx);
		char closing = '<' == start.charAt(0) ? '>' : '*';
		try {
			lrudContent();
		}
		catch (SegmentParseExpectedException ex) {
			if (!ex.getSegment().toString().startsWith(String.valueOf(closing))) {
				visitor.message(new WallsMessage(ex));
			}
			else {
				visitor
					.message(
						new WallsMessage(
							"warning",
							"missing LRUD measurment; use -- to indicate omitted measurements",
							ex.getSegment()));
			}
			clearExpected();
		}
		try {
			// walls accepts utterly crap data within the LRUDs without erroring,
			// so there's a lot of utterly crap data in the wild, hence we
			// unfortunately have to accept it. So just skip to > if the LRUDs opened
			// with <, or to * if they opened with *.
			if ('>' == closing) {
				// But make sure not to skip past a second <, since Walls seems to
				// tolerate a line like
				// A <1 , 2, 3, 4, <5,6,7,8>
				// as a shot from A to <1 with DAV of 2, 3, 4 and LRUDs of 5, 6, 7, 8
				Matcher m = chevronRx.matcher(line);
				m.region(index, line.length());
				if (m.find())
					index = m.start();
			}
			else {
				index = line.indexOf('*', index);
			}
			if (index < 0)
				index = line.length();
			expect(closing);
		}
		catch (Exception ex) {
			vector.left = vector.right = vector.up = vector.down = null;
			throw ex;
		}
	}

	void lrudContent() throws SegmentParseException {
		maybeWhitespace();
		int m = 0;
		for (LrudMeasurement elem : units.getLrudOrder()) {
			if (m++ > 0) {
				whitespaceAndOrComma();
			}
			if (!maybe(() -> lrudMeasurement(elem)).isPresent()) {
				visitor
					.message(
						new WallsMessage(
							"warning",
							"missing LRUD measurement; use -- to indicate omitted measurements",
							line.substring(index)));
			}
		}
		maybeWhitespaceAndOrComma();
		afterRequiredLrudMeasurements();
	}

	void afterRequiredLrudMeasurements() throws SegmentParseException {
		maybe(() -> {
			oneOf(() -> {
				lrudFacingAngle();
				if (maybeWhitespaceAndOrComma().isPresent()) {
					maybe(this::lrudCFlag);
				}
			}, this::lrudCFlag);
		});
	}

	void lrudFacingAngle() throws SegmentParseException {
		vector.lrudFacingAzimuth = azimuth(units.getAUnit());
	}

	void lrudCFlag() throws SegmentParseException {
		expectIgnoreCase('C');
		vector.cFlag = true;
	}

	void afterLruds() throws SegmentParseException {
		maybeWhitespaceAndOrComma();
		if (maybe(() -> inlineDirective()).isPresent()) {
			maybeWhitespace();
		}
		inlineCommentOrEndOfLine();
	}

	void inlineDirective() throws SegmentParseException {
		// currently this is the only directive that can be on a vector line
		inlineSegmentDirective(vector);
	}

	void inlineFixDirective() throws SegmentParseException {
		// currently this is the only directive that can be on a fix station line
		inlineSegmentDirective(fixStation);
	}

	void inlineSegmentDirective(HasInlineSegment target) throws SegmentParseException {
		target.setSegment(segmentDirective());
	}

	void fixLine() throws SegmentParseException {
		maybeWhitespace();
		expectIgnoreCase("#fix");
		whitespace();
		fixedStation();
		whitespaceAndOrComma();
		afterFixedStation();
		maybeWhitespaceAndOrComma();
		endOfLine();
		fixStation.date = date;
		fixStation.units = units.toImmutable();
		visitor.parsedFixStation(fixStation);
	}

	void fixedStation() throws SegmentParseException {
		String fixed = station().toString();
		fixStation = new FixedStation();
		fixStation.segment = segment == null ? null : new ArrayList<>(segment);
		fixStation.name = fixed;
	}

	void afterFixedStation() throws SegmentParseException {
		int k = 0;
		for (RectMeasurement elem : units.getRectOrder()) {
			if (k++ > 0) {
				whitespaceAndOrComma();
			}
			fixRectMeasurement(elem);
		}
		maybeWhitespaceAndOrComma();
		afterFixMeasurements();
	}

	void fixRectMeasurement(RectMeasurement elem) throws SegmentParseException {
		switch (elem) {
		case EAST:
			fixEast();
			break;
		case NORTH:
			fixNorth();
			break;
		case UP:
			fixUp();
			break;
		}
	}

	void fixEast() throws SegmentParseException {
		oneOf(() -> {
			fixStation.east = length(units.getDUnit());
		}, () -> {
			fixStation.longitude = longitude();
		});
	}

	void fixNorth() throws SegmentParseException {
		oneOf(() -> {
			fixStation.north = length(units.getDUnit());
		}, () -> {
			fixStation.latitude = latitude();
		});
	}

	void fixUp() throws SegmentParseException {
		fixStation.elevation = length(units.getDUnit());
	}

	void afterFixMeasurements() throws SegmentParseException {
		if (maybe(() -> varianceOverrides(fixStation)).isPresent()) {
			maybeWhitespaceAndOrComma();
		}
		afterFixVarianceOverrides();
	}

	void afterFixVarianceOverrides() throws SegmentParseException {
		if (maybe(() -> inlineNote(fixStation)).isPresent()) {
			maybeWhitespaceAndOrComma();
		}
		afterInlineFixNote();
	}

	Pattern inlineNoteEndRx = Pattern.compile(";|#s(eg(ment)?)?|$", Pattern.CASE_INSENSITIVE);

	void inlineNote(HasNote target) throws SegmentParseException {
		expect('/');

		target.setNote(escapedTextUntil(inlineNoteEndRx, "<NOTE>").trim());
	}

	void afterInlineFixNote() throws SegmentParseException {
		if (maybe(() -> inlineFixDirective()).isPresent()) {
			maybeWhitespaceAndOrComma();
		}
		inlineCommentOrEndOfLine();
	}

	void inlineCommentOrEndOfLine() throws SegmentParseException {
		oneOf(() -> {
			inlineComment();
		}, () -> {
			endOfLine();
		});
	}

	void comment() throws SegmentParseException {
		expect(';');
		visitor.parsedComment(remaining().toString());
	}

	void inlineComment() throws SegmentParseException {
		expect(';');
		String comment = remaining().toString();
		if (comment != null && !comment.isEmpty()) {
			if (vector != null) {
				vector.comment = comment;
			}
			else if (fixStation != null) {
				fixStation.comment = comment;
			}
			visitor.parsedComment(comment);
		}
	}

	void inlineComment(HasComment target) throws SegmentParseException {
		expect(';');
		target.setComment(remaining().toString());
	}
}
