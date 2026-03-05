package serilogj;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import serilogj.events.LogEventProperty;

public class LogEventPropertyTest {

	@Test
	void validSimpleName() {
		assertTrue(LogEventProperty.isValidName("Name"));
	}

	@Test
	void validNameWithUnderscore() {
		assertTrue(LogEventProperty.isValidName("_name"));
	}

	@Test
	void validNameWithDigits() {
		assertTrue(LogEventProperty.isValidName("name123"));
	}

	@Test
	void nullNameIsInvalid() {
		assertFalse(LogEventProperty.isValidName(null));
	}

	@Test
	void emptyNameIsInvalid() {
		assertFalse(LogEventProperty.isValidName(""));
	}

	@Test
	void spacesOnlyIsInvalid() {
		assertFalse(LogEventProperty.isValidName("   "));
	}

	@Test
	void nameStartingWithDigitIsInvalid() {
		assertFalse(LogEventProperty.isValidName("1name"));
	}

	@Test
	void nameWithSpacesIsInvalid() {
		assertFalse(LogEventProperty.isValidName("na me"));
	}

	@Test
	void nameWithSpecialCharsIsInvalid() {
		assertFalse(LogEventProperty.isValidName("name!"));
	}

	@Test
	void underscoreOnlyIsValid() {
		assertTrue(LogEventProperty.isValidName("_"));
	}
}
