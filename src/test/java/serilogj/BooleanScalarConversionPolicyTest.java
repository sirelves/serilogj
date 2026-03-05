package serilogj;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import serilogj.core.ScalarConversionPolicyResult;
import serilogj.events.ScalarValue;
import serilogj.policies.BooleanScalarConversionPolicy;

public class BooleanScalarConversionPolicyTest {

	private final BooleanScalarConversionPolicy policy = new BooleanScalarConversionPolicy();

	@Test
	void convertsTrueBoolean() {
		ScalarConversionPolicyResult result = policy.tryConvertToScalar(Boolean.TRUE, null);
		assertTrue(result.isValid);
		assertNotNull(result.result);
		assertEquals(true, ((ScalarValue) result.result).getValue());
	}

	@Test
	void convertsFalseBoolean() {
		ScalarConversionPolicyResult result = policy.tryConvertToScalar(Boolean.FALSE, null);
		assertTrue(result.isValid);
		assertNotNull(result.result);
		assertEquals(false, ((ScalarValue) result.result).getValue());
	}

	@Test
	void rejectsNonBoolean() {
		ScalarConversionPolicyResult result = policy.tryConvertToScalar("not a boolean", null);
		assertFalse(result.isValid);
	}
}
