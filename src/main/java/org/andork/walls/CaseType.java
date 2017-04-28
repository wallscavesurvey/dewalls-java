package org.andork.walls;

public enum CaseType {
	/**
	 * Convert to upper case
	 */
	UPPER,
	/**
	 * Convert to lower case
	 */
	LOWER,
	/**
	 * Leave in original case
	 */
	MIXED;

	public String apply(String s) {
		switch (this) {
		case UPPER:
			return s.toUpperCase();
		case LOWER:
			return s.toLowerCase();
		default:
			return s;
		}
	}
}
