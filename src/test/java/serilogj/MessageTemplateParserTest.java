package serilogj;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import serilogj.events.MessageTemplate;
import serilogj.parsing.MessageTemplateParser;
import serilogj.parsing.PropertyToken;
import serilogj.parsing.TextToken;
import serilogj.parsing.MessageTemplateToken;

public class MessageTemplateParserTest {

	private final MessageTemplateParser parser = new MessageTemplateParser();

	@Test
	void parsesEmptyTemplate() {
		MessageTemplate template = parser.parse("");
		assertEquals(1, template.getTokens().size());
		assertInstanceOf(TextToken.class, template.getTokens().get(0));
	}

	@Test
	void parsesPlainText() {
		MessageTemplate template = parser.parse("Hello, world!");
		assertEquals(1, template.getTokens().size());
		assertInstanceOf(TextToken.class, template.getTokens().get(0));
	}

	@Test
	void parsesNamedProperty() {
		MessageTemplate template = parser.parse("Hello {Name}!");
		assertEquals(3, template.getTokens().size());
		assertInstanceOf(TextToken.class, template.getTokens().get(0));
		assertInstanceOf(PropertyToken.class, template.getTokens().get(1));
		assertInstanceOf(TextToken.class, template.getTokens().get(2));

		PropertyToken pt = (PropertyToken) template.getTokens().get(1);
		assertEquals("Name", pt.getPropertyName());
	}

	@Test
	void parsesMultipleProperties() {
		MessageTemplate template = parser.parse("{First} and {Second}");
		long propCount = template.getTokens().stream()
				.filter(t -> t instanceof PropertyToken).count();
		assertEquals(2, propCount);
	}

	@Test
	void parsesPositionalProperty() {
		MessageTemplate template = parser.parse("{0} and {1}");
		assertNotNull(template.getPositionalTokens());
		assertEquals(2, template.getPositionalTokens().size());
	}

	@Test
	void parsesPropertyWithFormat() {
		MessageTemplate template = parser.parse("{Timestamp:yyyy-MM-dd}");
		assertEquals(1, template.getTokens().size());
		PropertyToken pt = (PropertyToken) template.getTokens().get(0);
		assertEquals("Timestamp", pt.getPropertyName());
		assertEquals("yyyy-MM-dd", pt.getFormat());
	}

	@Test
	void parsesDestructuringHint() {
		MessageTemplate template = parser.parse("{@User}");
		assertEquals(1, template.getTokens().size());
		PropertyToken pt = (PropertyToken) template.getTokens().get(0);
		assertEquals("User", pt.getPropertyName());
	}

	@Test
	void handlesDoubleOpenBrace() {
		// {{ outputs literal { as text
		MessageTemplate template = parser.parse("Hello {{World");
		assertInstanceOf(TextToken.class, template.getTokens().get(0));
	}
}
