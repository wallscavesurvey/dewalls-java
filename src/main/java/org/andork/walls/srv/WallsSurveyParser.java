package org.andork.walls.srv;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
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

public class WallsSurveyParser extends LineParser {
	WallsVisitor visitor = new AbstractWallsVisitor();
	MutableWallsUnits _units = new MutableWallsUnits();
	final Stack<WallsUnits> _stack = new Stack<>();
	final HashMap<String, String> _macros = new HashMap<>();
	boolean _inBlockComment;
	final List<String> _segment = new ArrayList<>();
	final List<String> _rootSegment = new ArrayList<>();
	Date _date;

	boolean _parsedSegmentDirective;
	Segment _fromStationSegment;
	Segment _toStationSegment;
	Segment _azmSegment;
	Segment _incSegment;

	Vector _vector;
	FixedStation _fixStation;

	Character escapedChar(Predicate<Character> charPredicate, String... expectedItems)
			throws SegmentParseException {
		Character c = expectChar(charPredicate, expectedItems);
		return c == '\\' ? oneOf(escapedChars) : c;
	}

	String escapedText(Predicate<Character> charPredicate, String... expectedItems)
			throws SegmentParseException {
		StringBuilder result = new StringBuilder();
		while (maybe(() -> result.append(escapedChar(charPredicate, expectedItems))).isPresent()) {
		}
		return result.toString();
	}

	<R> Optional<R> optional(Production<R> production) throws SegmentParseException {
		try {
			return Optional.of(production.run());
		} catch (SegmentParseExpectedException ex) {
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
		} catch (SegmentParseExpectedException ex) {
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
			} else {
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
		result.put("it",
				Arrays.asList(TapingMethodMeasurement.INSTRUMENT_HEIGHT, TapingMethodMeasurement.TARGET_HEIGHT));
		result.put("is", Arrays.asList(TapingMethodMeasurement.INSTRUMENT_HEIGHT, TapingMethodMeasurement.STATION));
		result.put("st", Arrays.asList(TapingMethodMeasurement.STATION, TapingMethodMeasurement.TARGET_HEIGHT));
		result.put("ss", Arrays.asList(TapingMethodMeasurement.STATION, TapingMethodMeasurement.STATION));
		return Collections.unmodifiableMap(result);
	}

	static Map<String, Integer> createPrefixDirectives() {
		Map<String, Integer> result = new HashMap<>();
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
		result.put("s", this::s);
		result.put("a", this::a);
		result.put("ab", this::ab);
		result.put("a/ab", this::a_ab);
		result.put("v", this::v);
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
	static final Set<CtMeasurement> requiredCtElements = new HashSet<>(
			Arrays.asList(CtMeasurement.DISTANCE, CtMeasurement.AZIMUTH));
	static final Map<Character, RectMeasurement> rectElements = createRectElements();
	static final Set<RectMeasurement> requiredRectElements = new HashSet<>(
			Arrays.asList(RectMeasurement.EAST, RectMeasurement.NORTH));
	static final Map<Character, LrudMeasurement> lrudElements = createLrudElements();
	static final Set<LrudMeasurement> requiredLrudElements = new HashSet<>(
			Arrays.asList(LrudMeasurement.LEFT, LrudMeasurement.RIGHT, LrudMeasurement.UP, LrudMeasurement.DOWN));
	static final Map<String, Boolean> correctedValues = createCorrectedValues();
	static final Map<String, CaseType> caseTypes = createCaseTypes();
	static final Map<String, LrudType> lrudTypes = createLrudTypes();
	public static final Map<String, List<TapingMethodMeasurement>> tapingMethods = createTapingMethods();
	static final Map<String, Integer> prefixDirectives = createPrefixDirectives();

	static final Set<Character> escapedChars = new HashSet<>(Arrays.asList(
			'r', 'n', 'f', 't', '"', '\\'));
	static final Pattern wordRx = Pattern.compile("\\w+");
	static final Pattern notSemicolonRx = Pattern.compile("[^;]+");
	static final Pattern UnitsOptionRx = Pattern.compile("[a-zA-Z_0-9/]*");
	static final Pattern directiveRx = Pattern.compile("#([]\\[]|[a-zA-Z0-9]+)");
	static final Pattern macroNameRx = Pattern.compile("[^()=,,# \t]*");
	static final Pattern stationRx = Pattern.compile("([^:;,,#/ \t]*:){0,3}[^:;,,#/ \t]{1,8}");
	static final Pattern prefixRx = Pattern.compile("[^:;,,#/ \t]*");

	static final Pattern optionalRx = Pattern.compile("-+");
	static final Pattern optionalStationRx = Pattern.compile("-+");

	static final Pattern isoDateRx = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
	static final Pattern usDateRx1 = Pattern.compile("\\d{2}-\\d{2}-\\d{2,4}");
	static final Pattern usDateRx2 = Pattern.compile("\\d{2}/\\d{2}/\\d{2,4}");
	static final Pattern usDateRx3 = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}");

	static final Pattern segmentPartRx = Pattern.compile("[^./\\;][^/\\;]+");

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
		} else {
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
		return oneOf(
				() -> unsignedLengthNonInches(defaultUnit),
				() -> unsignedLengthInches());
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
					value.orElse(0.0) + minutes.orElse(0.0) / 60.0 + seconds.orElse(0.0) / 3600, Angle.degrees);
		} else if (!value.isPresent()) {
			throwAllExpected();
		}
		return new UnitizedDouble<Angle>(value.get(), oneOf(unitSuffixes, defaultUnit));
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
				degrees.orElse(0.0) + minutes.orElse(0.0) / 60.0 + seconds.orElse(0.0) / 3600, Angle.degrees);
	}

	UnitizedDouble<Angle> latitude() throws SegmentParseException {
		int start = index;
		CardinalDirection side = oneOf(northSouth);
		UnitizedDouble<Angle> latitude = unsignedDmsAngle();

		if (approx(latitude.get(Angle.degrees)) > 90.0) {
			throw new SegmentParseException("latitude out of range", line.substring(start, index));
		}

		return side == CardinalDirection.SOUTH ? latitude.negate() : latitude;
	}

	UnitizedDouble<Angle> longitude() throws SegmentParseException {
		int start = index;
		CardinalDirection side = oneOf(eastWest);
		UnitizedDouble<Angle> longitude = unsignedDmsAngle();

		if (approx(longitude.get(Angle.degrees)) > 180.0) {
			throw new SegmentParseException("longitude out of range", line.substring(start, index));
		}

		return side == CardinalDirection.WEST ? longitude.negate() : longitude;
	}

	UnitizedDouble<Angle> nonQuadrantAzimuth(Unit<Angle> defaultUnit) throws SegmentParseException {
		int start = index;

		UnitizedDouble<Angle> result = unsignedAngle(azmUnitSuffixes, defaultUnit);

		if (approx(result.get(Angle.degrees)) >= 360.0) {
			throw new SegmentParseException("azimuth out of range", line.substring(start, index));
		}

		return result;
	}

	UnitizedDouble<Angle> quadrantAzimuth() throws SegmentParseException {
		CardinalDirection from = oneOf(cardinalDirections);

		int start = index;
		Optional<UnitizedDouble<Angle>> angle = maybe(() -> nonQuadrantAzimuth(Angle.degrees));
		if (angle.isPresent()) {
			if (approx(angle.get().get(Angle.degrees)) >= 90.0) {
				throw new SegmentParseException("azimuth out of range", line.substring(start, index));
			}

			CardinalDirection to = oneOf(
					from == CardinalDirection.NORTH ||
							from == CardinalDirection.SOUTH ? eastWest : northSouth);

			return from.quadrant(to, angle.get());
		}
		return from.angle;
	}

	UnitizedDouble<Angle> azimuth(Unit<Angle> defaultUnit) throws SegmentParseException {
		return oneOf(
				() -> quadrantAzimuth(),
				() -> nonQuadrantAzimuth(defaultUnit));
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
				throw new SegmentParseException("zero inclinations must not be preceded by a sign",
						line.substring(start, index));
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
		return oneOf(
				() -> quotedText(),
				() -> nonwhitespace().toString());
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
			} else if (c == '"') {
				break;
			}
		}

		return line.toString().substring(start, index);
	}

	void parseLine(String line) throws SegmentParseException {
		reset(new Segment(line, null, 0, 0));
		parseLine();
	}

	void parseLine(Segment line) throws SegmentParseException {
		reset(line);
		parseLine();
	}

	void parseLine() throws SegmentParseException {
		_parsedSegmentDirective = false;
		maybeWhitespace();

		if (isAtEnd()) {
			return;
		}

		if (_inBlockComment) {
			throwAllExpected(() -> oneOfWithLookahead(
					this::endBlockCommentLine,
					this::insideBlockCommentLine));
		} else {
			throwAllExpected(() -> oneOf(
					this::comment,
					this::directiveLine,
					this::vectorLine));
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
				if (!_macros.containsKey(macroName.toString())) {
					throw new SegmentParseException("macro not defined", macroName);
				}
				return _macros.get(macroName.toString());
			} else if (Character.isWhitespace(c)) {
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
		_inBlockComment = true;
	}

	void endBlockCommentLine() throws SegmentParseException {
		maybeWhitespace();
		expect("#]");
		remaining();
		_inBlockComment = false;
	}

	void insideBlockCommentLine() {
		visitor.parsedComment(remaining().toString());
	}

	Segment untilComment(List<String> expectedItems) throws SegmentParseException {
		return expect(notSemicolonRx, expectedItems);
	}

	void segmentLine() throws SegmentParseException {
		maybeWhitespace();
		_segment.clear();
		_segment.addAll(segmentDirective());
		maybeWhitespace();
		inlineCommentOrEndOfLine();
	}

	void segmentSeparator() throws SegmentParseException {
		oneOf(
				() -> expect("/"),
				() -> expect("\\"));
	}

	String initialSegmentPart() throws SegmentParseException {
		return oneOf(
				() -> expect("."),
				() -> expect("..")).toString();
	}

	String nonInitialSegmentPart() throws SegmentParseException {
		return expect(segmentPartRx, "<PATH ELEMENT>").toString();
	}

	List<String> segmentPath() throws SegmentParseException {
		final List<String> path = new ArrayList<>();

		oneOf(
				() -> {
					segmentSeparator();
					path.addAll(_rootSegment);
				},
				() -> {
					path.addAll(_segment);
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
		oneOf(
				() -> expectIgnoreCase("#segment"),
				() -> expectIgnoreCase("#seg"),
				() -> expectIgnoreCase("#s"));

		_parsedSegmentDirective = true;

		List<String> result = _segment;

		if (maybeWhitespace().isPresent()) {
			result = maybe(() -> segmentPath()).orElse(_segment);
		}

		return result;
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
			_units.setPrefix(prefixIndex, expect(prefixRx, "<PREFIX>").toString());
		}
	}

	void noteLine() throws SegmentParseException {
		maybeWhitespace();
		noteDirective();
		maybeWhitespace();
		inlineCommentOrEndOfLine();
	}

	void noteDirective() throws SegmentParseException {
		oneOf(
				() -> expectIgnoreCase("#note"),
				() -> expectIgnoreCase("#n"));

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
		oneOf(
				() -> expectIgnoreCase("#flag"),
				() -> expectIgnoreCase("#f"));

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
			_units.setFlag(_flag.orElse(null));
		} else {
			if (!_flag.isPresent()) {
				throwAllExpected();
			}
			visitor.parsedFlag(stations, _flag.get());
		}

		inlineCommentOrEndOfLine();
	}

	String slashPrefixedFlag() throws SegmentParseException {
		expect('/');
		return expect(notSemicolonRx, "<FLAG>").toString();
	}

	void symbolLine() throws SegmentParseException {
		maybeWhitespace();

		oneOf(
				() -> expectIgnoreCase("#symbol"),
				() -> expectIgnoreCase("#sym"));

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
		return oneOf(
				() -> isoDate(),
				() -> usDate1(),
				() -> usDate2(),
				() -> usDate3());
	}

	Date isoDate() throws SegmentParseExpectedException {
		Segment dateSegment = expect(isoDateRx, "<DATE>");
		TemporalAccessor accessor = DateTimeFormatter.ISO_DATE_TIME.parse(dateSegment.toString());
		return Date.from(Instant.from(accessor));
	}

	static DateFormat fullDateFormat1 = new SimpleDateFormat("MM-dd-yyyy");
	static DateFormat shortDateFormat1 = new SimpleDateFormat("MM-dd-yy");
	static DateFormat fullDateFormat2 = new SimpleDateFormat("MM/dd/yyyy");
	static DateFormat shortDateFormat2 = new SimpleDateFormat("MM/dd/yy");
	static DateFormat fullDateFormat3 = new SimpleDateFormat("yyyy-M-d");

	Date usDate1() throws SegmentParseExpectedException {
		String str = expect(usDateRx1, "<DATE>").toString();
		try {
			return (str.length() > 8 ? fullDateFormat1 : shortDateFormat1).parse(str);
		} catch (ParseException ex) {
			// should never happen because of regex
			return null;
		}
	}

	Date usDate2() throws SegmentParseExpectedException {
		String str = expect(usDateRx2, "<DATE>").toString();
		try {
			return (str.length() > 8 ? fullDateFormat2 : shortDateFormat2).parse(str);
		} catch (ParseException ex) {
			// should never happen because of regex
			return null;
		}
	}

	Date usDate3() throws SegmentParseExpectedException {
		String str = expect(usDateRx3, "<DATE>").toString();
		try {
			return fullDateFormat3.parse(str);
		} catch (ParseException ex) {
			// should never happen because of regex
			return null;
		}
	}

	void UnitsLine() throws SegmentParseException {
		maybeWhitespace();
		oneOf(
				() -> expectIgnoreCase("#units"),
				() -> expectIgnoreCase("#u"));

		visitor.willParseUnits();

		if (maybeWhitespace().isPresent()) {
			UnitsOptions();
			visitor.parsedUnits();
		}
	}

	void parseUnitsOptions(Segment options) throws SegmentParseException {
		reset(options);
		UnitsOptions();
		//		visitor.parsedUnits();
	}

	void UnitsOptions() throws SegmentParseException {
		boolean gotOne = false;
		while (!maybe(() -> inlineCommentOrEndOfLine()).isPresent()) {
			if (gotOne) {
				whitespace();
			} else {
				gotOne = true;
			}

			maybe(() -> oneOf(
					this::UnitsOption,
					this::macroOption));
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
		_macros.put(macroName, macroValue);
	}

	void save() throws SegmentParseException {
		if (_stack.size() >= 10) {
			throw new SegmentParseException("units stack is full", line.substring(index - 4, index));
		}
		_stack.push(_units.toImmutable());
	}

	void restore() throws SegmentParseException {
		if (_stack.isEmpty()) {
			throw new SegmentParseException("units stack is empty", line.substring(index - 7, index));
		}
		_units = _stack.pop().toMutable();
	}

	void reset_() {
		_units = new MutableWallsUnits();
	}

	void meters() {
		_units.setDUnit(Length.meters);
		_units.setSUnit(Length.meters);
	}

	void feet() {
		_units.setDUnit(Length.feet);
		_units.setSUnit(Length.feet);
	}

	void ct() {
		_units.setVectorType(VectorType.COMPASS_AND_TAPE);
	}

	void d() throws SegmentParseException {
		expect('=');
		_units.setDUnit(oneOfLowercase(nonwhitespaceRx, lengthUnits));
	}

	void s() throws SegmentParseException {
		expect('=');
		_units.setSUnit(oneOfLowercase(nonwhitespaceRx, lengthUnits));
	}

	void a() throws SegmentParseException {
		expect('=');
		_units.setAUnit(oneOfLowercase(nonwhitespaceRx, azmUnits));
	}

	void ab() throws SegmentParseException {
		expect('=');
		_units.setAbUnit(oneOfLowercase(nonwhitespaceRx, azmUnits));
	}

	void a_ab() throws SegmentParseException {
		expect('=');
		Unit<Angle> unit = oneOfLowercase(nonwhitespaceRx, azmUnits);
		_units.setAUnit(unit);
		_units.setAbUnit(unit);
	}

	void v() throws SegmentParseException {
		expect('=');
		_units.setVUnit(oneOfLowercase(nonwhitespaceRx, incUnits));
	}

	void vb() throws SegmentParseException {
		expect('=');
		_units.setVbUnit(oneOfLowercase(nonwhitespaceRx, incUnits));
	}

	void v_vb() throws SegmentParseException {
		expect('=');
		Unit<Angle> unit = oneOfLowercase(nonwhitespaceRx, incUnits);
		_units.setVUnit(unit);
		_units.setVbUnit(unit);
	}

	void order() throws SegmentParseException {
		expect('=');
		oneOf(
				this::ctOrder,
				this::rectOrder);
	}

	void ctOrder() throws SegmentParseException {
		_units.setCtOrder(elementChars(ctElements, requiredCtElements));
	}

	void rectOrder() throws SegmentParseException {
		_units.setRectOrder(elementChars(rectElements, requiredRectElements));
	}

	void decl() throws SegmentParseException {
		expect('=');
		_units.setDecl(azimuthOffset(_units.getAUnit()));
	}

	void grid() throws SegmentParseException {
		expect('=');
		_units.setGrid(azimuthOffset(_units.getAUnit()));
	}

	void rect() throws SegmentParseException {
		if (maybeChar('=')) {
			_units.setRect(azimuthOffset(_units.getAUnit()));
		} else {
			_units.setVectorType(VectorType.RECTANGULAR);
		}
	}

	void incd() throws SegmentParseException {
		expect('=');
		_units.setIncd(length(_units.getDUnit()));
	}

	void inch() throws SegmentParseException {
		expect('=');
		_units.setInch(length(_units.getSUnit()));
	}

	void incs() throws SegmentParseException {
		expect('=');
		_units.setIncs(length(_units.getSUnit()));
	}

	void inca() throws SegmentParseException {
		expect('=');
		_units.setInca(azimuthOffset(_units.getAUnit()));
	}

	void incab() throws SegmentParseException {
		expect('=');
		_units.setIncab(azimuthOffset(_units.getAbUnit()));
	}

	void incv() throws SegmentParseException {
		expect('=');
		_units.setIncv(inclination(_units.getVUnit()));
	}

	void incvb() throws SegmentParseException {
		expect('=');
		_units.setIncvb(inclination(_units.getVbUnit()));
	}

	void typeab() throws SegmentParseException {
		expect('=');
		_units.setTypeabCorrected(oneOfLowercase(wordRx, correctedValues));
		if (maybeChar(',')) {
			_units.setTypeabTolerance(new UnitizedDouble<Angle>(unsignedDoubleLiteral(), Angle.degrees));
			if (maybeChar(',')) {
				expectIgnoreCase('x');
				_units.setTypeabNoAverage(true);
			} else {
				_units.setTypeabNoAverage(false);
			}
		} else {
			_units.setTypeabTolerance(new UnitizedDouble<Angle>(2.0, Angle.degrees));
		}
	}

	void typevb() throws SegmentParseException {
		expect('=');
		_units.setTypevbCorrected(oneOfLowercase(wordRx, correctedValues));
		if (maybeChar(',')) {
			_units.setTypevbTolerance(new UnitizedDouble<Angle>(unsignedDoubleLiteral(), Angle.degrees));
			if (maybeChar(',')) {
				expectIgnoreCase('x');
				_units.setTypevbNoAverage(true);
			} else {
				_units.setTypevbNoAverage(false);
			}
		} else {
			_units.setTypevbTolerance(new UnitizedDouble<Angle>(2.0, Angle.degrees));
		}
	}

	void case_() throws SegmentParseException {
		expect('=');
		_units.setCase_(oneOfLowercase(nonwhitespaceRx, caseTypes));
	}

	void lrud() throws SegmentParseException {
		expect('=');
		_units.setLrud(oneOfLowercase(wordRx, lrudTypes));
		if (maybeChar(':')) {
			lrudOrder();
		} else {
			_units.setLrudOrder(Arrays.asList(LrudMeasurement.LEFT, LrudMeasurement.RIGHT, LrudMeasurement.UP,
					LrudMeasurement.DOWN));
		}
	}

	void lrudOrder() throws SegmentParseException {
		_units.setLrudOrder(elementChars(lrudElements, requiredLrudElements));
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
		_units.setPrefix(index, prefix);
	}

	void tape() throws SegmentParseException {
		expect('=');
		_units.setTape(oneOfLowercase(nonwhitespaceRx, tapingMethods));
	}

	void uvh() throws SegmentParseException {
		expect('=');
		_units.setUvh(unsignedDoubleLiteral());
	}

	void uvv() throws SegmentParseException {
		expect('=');
		_units.setUvv(unsignedDoubleLiteral());
	}

	void uv() throws SegmentParseException {
		expect('=');
		double value = unsignedDoubleLiteral();
		_units.setUvv(value);
		_units.setUvh(value);
	}

	void flag() throws SegmentParseException {
		String flag = null;
		if (maybeChar('=')) {
			flag = quotedTextOrNonwhitespace();
		}
		_units.setFlag(flag);
	}

	void vectorLine() throws SegmentParseException {
		maybeWhitespace();
		fromStation();
		_parsedSegmentDirective = false;
		whitespace();
		afterFromStation();
		maybeWhitespace();
		endOfLine();
		if (_parsedSegmentDirective) {
			_vector.segment = _segment;
		}
		_vector.date = _date;
		_vector.units = _units.toImmutable();
		visitor.parsedVector(_vector);
	}

	Segment station() throws SegmentParseException {
		return expect(stationRx, "<STATION>");
	}

	void fromStation() throws SegmentParseException {
		_fromStationSegment = station();
		String from = _fromStationSegment.toString();
		if (optionalStationRx.matcher(from).matches()) {
			from = null;
		}
		_vector = new Vector();
		_vector.sourceSegment = line;
		_vector.from = from;
	}

	void afterFromStation() throws SegmentParseException {
		oneOfWithLookahead(() -> {
			toStation();
			whitespace();
			afterToStation();
		}, () -> {
			// clear all measurements
			String from = _vector.from;
			_vector = new Vector();
			_vector.sourceSegment = line;
			_vector.from = from;
			lruds();
			afterLruds();
		});
	}

	void toStation() throws SegmentParseException {
		_toStationSegment = station();
		String to = _toStationSegment.toString();
		if (optionalStationRx.matcher(to).matches()) {
			to = null;
		}
		if (_vector.from == null && to == null) {
			throw new SegmentParseException("from and to station can't both be omitted", _toStationSegment);
		}
		_vector.to = to;
	}

	void afterToStation() throws SegmentParseException {
		int k = 0;
		if (_units.getVectorType() == VectorType.RECTANGULAR) {
			for (RectMeasurement elem : _units.getRectOrder()) {
				if (k++ > 0) {
					whitespace();
				}
				rectMeasurement(elem);
			}
		} else {
			for (CtMeasurement elem : _units.getCtOrder()) {
				if (k++ > 0) {
					whitespace();
				}
				ctMeasurement(elem);
			}
		}

		if (_units.getVectorType() == VectorType.COMPASS_AND_TAPE) {
			if (!UnitizedDouble.isFinite(_vector.frontsightAzimuth) &&
					!UnitizedDouble.isFinite(_vector.backsightAzimuth) &&
					!Vector.isVertical(_units.averageInclination(_vector.frontsightInclination, _vector.backsightInclination))) {
				throw new SegmentParseException("azimuth can only be omitted for vertical shots", _azmSegment);
			}

			maybeWithLookahead(() -> {
				whitespace();
				instrumentHeight();
			});
			maybeWithLookahead(() -> {
				whitespace();
				targetHeight();
			});
		}

		afterVectorMeasurements();
	}

	void rectMeasurement(RectMeasurement elem) throws SegmentParseException {
		UnitizedDouble<Length> measurement = length(_units.getDUnit());
		switch (elem) {
		case EAST:
			_vector.east = measurement;
			break;
		case NORTH:
			_vector.north = measurement;
			break;
		case UP:
			_vector.elevation = measurement;
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
		if ((measurement.isPositive() && measurement.add(correction).isNegative()) ||
				(measurement.isNegative() && measurement.add(correction).isPositive())) {
			throw new SegmentParseException("correction changes sign of measurement",
					line.substring(segStart, index));
		}
	}

	void distance() throws SegmentParseException {
		int start = index;
		UnitizedDouble<Length> dist = unsignedLength(_units.getDUnit());
		checkCorrectedSign(start, dist, _units.getIncd());
		_vector.distance = dist;
	}

	UnitizedDouble<Angle> azmDifference(UnitizedDouble<Angle> fs, UnitizedDouble<Angle> bs)
			throws SegmentParseException {
		if (!_units.isTypeabCorrected()) {
			bs = bs.compareTo(oneEighty) < 0
					? bs.add(oneEighty)
					: bs.sub(oneEighty);
		}
		UnitizedDouble<Angle> diff = fs.sub(bs).abs();
		return diff.compareTo(oneEighty) > 0
				? new UnitizedDouble<Angle>(360.0, Angle.degrees).sub(diff)
				: diff;
	}

	void azimuth() throws SegmentParseException {
		int start = index;

		oneOf(
				() -> {
					_vector.frontsightAzimuth = optional(() -> azimuth(_units.getAUnit())).orElse(null);
					maybe(() -> {
						expect('/');
						_vector.backsightAzimuth = optional(() -> azimuth(_units.getAbUnit())).orElse(null);
					});
				},
				() -> {
					expect('/');
					_vector.backsightAzimuth = optional(() -> azimuth(_units.getAbUnit())).orElse(null);
				});

		_azmSegment = line.substring(start, index);

		if (_vector.frontsightAzimuth != null && _vector.backsightAzimuth != null) {
			UnitizedDouble<Angle> diff = azmDifference(_vector.frontsightAzimuth, _vector.backsightAzimuth);
			if (diff.compareTo(_units.getTypeabTolerance().mul(1 + 1e-6)) > 0) {
				visitor.message(new WallsMessage("warning",
						String.format("azimuth fs/bs difference ({0}) exceeds tolerance ({1})",
								diff.toString(),
								_units.getTypeabTolerance().toString()),
						_azmSegment));
			}
		}
	}

	UnitizedDouble<Angle> incDifference(UnitizedDouble<Angle> fs, UnitizedDouble<Angle> bs)
			throws SegmentParseException {
		return _units.isTypevbCorrected()
				? fs.sub(bs).abs()
				: fs.add(bs).abs();
	}

	void inclination() throws SegmentParseException {
		int start = index;

		oneOf(
				() -> {
					_vector.frontsightInclination = optional(() -> inclination(_units.getVUnit())).orElse(null);
					maybe(() -> {
						expect('/');
						_vector.backsightInclination = optional(() -> inclination(_units.getVbUnit())).orElse(null);
					});
				},
				() -> {
					expect('/');
					_vector.backsightInclination = optional(() -> inclination(_units.getVbUnit())).orElse(null);
				});

		_incSegment = line.substring(start, index);

		if (_vector.frontsightInclination == null && _vector.backsightInclination == null) {
			_vector.frontsightInclination = new UnitizedDouble<Angle>(0, _units.getVUnit());
		} else if (_vector.frontsightInclination != null && _vector.backsightInclination != null) {
			UnitizedDouble<Angle> diff = incDifference(_vector.frontsightInclination, _vector.backsightInclination);
			if (diff.compareTo(_units.getTypevbTolerance().mul(1 + 1e-6)) > 0) {
				visitor.message(new WallsMessage("warning",
						String.format("inclination fs/bs difference ({0}) exceeds tolerance ({1})",
								diff.toString(),
								_units.getTypevbTolerance().toString()),
						_incSegment));
			}
		}
	}

	void instrumentHeight() throws SegmentParseException {
		int start = index;
		Optional<UnitizedDouble<Length>> ih = optional(() -> length(_units.getSUnit()));
		if (ih.isPresent()) {
			checkCorrectedSign(start, ih.get(), _units.getIncs());
			_vector.instrumentHeight = ih.get();
		}
	}

	void targetHeight() throws SegmentParseException {
		int start = index;
		Optional<UnitizedDouble<Length>> th = optional(() -> length(_units.getSUnit()));
		if (th.isPresent()) {
			checkCorrectedSign(start, th.get(), _units.getIncs());
			_vector.targetHeight = th.get();
		}
	}

	void lrudMeasurement(LrudMeasurement elem) throws SegmentParseException {
		int start = index;
		Optional<UnitizedDouble<Length>> measurement = optionalWithLookahead(() -> length(_units.getSUnit()));
		if (measurement.isPresent()) {
			warnIfNegative(measurement.get(), start, "LRUD");
			checkCorrectedSign(start, measurement.get(), _units.getIncs());
		}
		switch (elem) {
		case LEFT:
			_vector.left = measurement.orElse(null);
			break;
		case RIGHT:
			_vector.right = measurement.orElse(null);
			break;
		case UP:
			_vector.up = measurement.orElse(null);
			break;
		case DOWN:
			_vector.down = measurement.orElse(null);
			break;
		}
	}

	<T extends UnitType<T>> void warnIfNegative(UnitizedDouble<T> measurement, int start, String name) {
		if (UnitizedDouble.isFinite(measurement) && measurement.get(measurement.unit) < 0) {
			visitor.message(new WallsMessage("warning", String.format("negative {0} measurement", name),
					line.substring(start, index)));
		}
	}

	void afterVectorMeasurements() throws SegmentParseException {
		maybeWithLookahead(() -> {
			whitespace();
			varianceOverrides();
		});
		afterVectorVarianceOverrides();
	}

	void varianceOverrides() throws SegmentParseException {
		varianceOverrides(_vector);
	}

	void varianceOverrides(HasVarianceOverrides target) throws SegmentParseException {
		expect('(');
		maybeWhitespace();
		VarianceOverride horizontal = varianceOverride(_units.getDUnit());
		target.setHorizontalVarianceOverride(horizontal);
		maybeWhitespace();
		if (maybeChar(',')) {
			maybeWhitespace();
			VarianceOverride vertical = varianceOverride(_units.getDUnit());
			if (horizontal == null && vertical == null) {
				throw allExpected();
			}
			target.setVerticalVarianceOverride(vertical);
		} else if (horizontal != null) {
			// no this is not a typo
			target.setVerticalVarianceOverride(horizontal);
		}
		expect(')');
	}

	void afterVectorVarianceOverrides() throws SegmentParseException {
		maybeWithLookahead(() -> {
			whitespace();
			lruds();
		});
		afterLruds();
	}

	void lruds() throws SegmentParseException {
		oneOfWithLookahead(() -> {
			expect('<');
			try {
				lrudContent();
			} catch (SegmentParseExpectedException ex) {
				if (!ex.getSegment().toString().startsWith(">")) {
					throw ex;
				}
				visitor.message(new WallsMessage("warning",
						"missing LRUD measurment; use -- to indicate omitted measurements", ex.getSegment()));
			}
			expect('>');
		}, () -> {
			expect('*');
			try {
				lrudContent();
			} catch (SegmentParseExpectedException ex) {
				if (!ex.getSegment().toString().startsWith("*")) {
					throw ex;
				}
				visitor.message(new WallsMessage("warning",
						"missing LRUD measurement; use -- to indicate omitted measurements", ex.getSegment()));
			}
			expect('*');
		});
	}

	void lrudContent() throws SegmentParseException {
		maybeWhitespace();
		int m = 0;
		for (LrudMeasurement elem : _units.getLrudOrder()) {
			if (m++ > 0) {
				oneOfWithLookahead(
						() -> {
							maybeWhitespace();
							expect(',');
							maybeWhitespace();
						},
						this::whitespace);
			}
			if (!maybe(() -> lrudMeasurement(elem)).isPresent()) {
				visitor.message(new WallsMessage("warning",
						"missing LRUD measurement; use -- to indicate omitted measurements", line.substring(index)));
			}
		}
		maybeWhitespace();
		afterRequiredLrudMeasurements();
	}

	void afterRequiredLrudMeasurements() throws SegmentParseException {
		if (maybeChar(',')) {
			maybeWhitespace();
		}
		maybe(() -> {
			oneOf(() -> {
				lrudFacingAngle();
				maybeWhitespace();
				if (maybeChar(',')) {
					maybeWhitespace();
				}
				lrudCFlag();
			}, this::lrudCFlag);
		});
	}

	void lrudFacingAngle() throws SegmentParseException {
		_vector.lrudFacingAzimuth = azimuth(_units.getAUnit());
	}

	void lrudCFlag() throws SegmentParseException {
		expectIgnoreCase('c');
		_vector.cFlag = true;
	}

	void afterLruds() throws SegmentParseException {
		maybeWhitespace();
		if (maybe(() -> inlineDirective()).isPresent()) {
			maybeWhitespace();
		}
		inlineCommentOrEndOfLine();
	}

	void inlineDirective() throws SegmentParseException {
		// currently this is the only directive that can be on a vector line
		inlineSegmentDirective(_vector);
	}

	void inlineFixDirective() throws SegmentParseException {
		// currently this is the only directive that can be on a fix station line
		inlineSegmentDirective(_fixStation);
	}

	void inlineSegmentDirective(HasInlineSegment target) throws SegmentParseException {
		target.setSegment(segmentDirective());
	}

	void fixLine() throws SegmentParseException {
		maybeWhitespace();
		expectIgnoreCase("#fix");
		whitespace();
		fixedStation();
		whitespace();
		_parsedSegmentDirective = false;
		afterFixedStation();
		maybeWhitespace();
		endOfLine();
		if (!_parsedSegmentDirective) {
			_fixStation.segment = _segment;
		}
		_fixStation.date = _date;
		_fixStation.units = _units.toImmutable();
		visitor.parsedFixStation(_fixStation);
	}

	void fixedStation() throws SegmentParseException {
		String fixed = station().toString();
		_fixStation = new FixedStation();
		_fixStation.name = fixed;
	}

	void afterFixedStation() throws SegmentParseException {
		int k = 0;
		for (RectMeasurement elem : _units.getRectOrder()) {
			if (k++ > 0) {
				whitespace();
			}
			fixRectMeasurement(elem);
		}
		maybeWhitespace();
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
		oneOf(
				() -> {
					_fixStation.east = length(_units.getDUnit());
				},
				() -> {
					_fixStation.longitude = longitude();
				});
	}

	void fixNorth() throws SegmentParseException {
		oneOf(
				() -> {
					_fixStation.north = length(_units.getDUnit());
				},
				() -> {
					_fixStation.latitude = latitude();
				});
	}

	void fixUp() throws SegmentParseException {
		_fixStation.elevation = length(_units.getDUnit());
	}

	void afterFixMeasurements() throws SegmentParseException {
		if (maybe(() -> varianceOverrides(_fixStation)).isPresent()) {
			maybeWhitespace();
		}
		afterFixVarianceOverrides();
	}

	void afterFixVarianceOverrides() throws SegmentParseException {
		if (maybe(() -> inlineNote(_fixStation)).isPresent()) {
			maybeWhitespace();
		}
		afterInlineFixNote();
	}

	void inlineNote(HasNote target) throws SegmentParseException {
		expect('/');
		target.setNote(escapedText(c -> c != ';' && c != '#', "<NOTE>").trim());
	}

	void afterInlineFixNote() throws SegmentParseException {
		if (maybe(() -> inlineFixDirective()).isPresent()) {
			maybeWhitespace();
		}
		inlineCommentOrEndOfLine(_fixStation);
	}

	void inlineCommentOrEndOfLine() throws SegmentParseException {
		oneOf(() -> {
			inlineComment();
		},
				() -> {
					endOfLine();
				});
	}

	void inlineCommentOrEndOfLine(HasComment target) throws SegmentParseException {
		oneOf(() -> inlineComment(target),
				() -> endOfLine());
	}

	void comment() throws SegmentParseException {
		expect(';');
		visitor.parsedComment(remaining().toString());
	}

	void inlineComment() throws SegmentParseException {
		expect(';');
		visitor.parsedComment(remaining().toString());
	}

	void inlineComment(HasComment target) throws SegmentParseException {
		expect(';');
		target.setComment(remaining().toString());
	}
}
