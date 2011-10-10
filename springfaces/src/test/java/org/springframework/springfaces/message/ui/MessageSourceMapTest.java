package org.springframework.springfaces.message.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;

import java.util.Arrays;
import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.springfaces.message.ui.MessageSourceMap.Value;

/**
 * Tests for {@link MessageSourceMap}.
 * 
 * @author Phillip Webb
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageSourceMapTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private MessageSource messageSource;

	@Test
	public void shouldNeedMessageSource() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("MessageSource must not be null");
		new MessageSourceMap(null, null);
	}

	@Test
	public void shouldThrowUnsupportedOperationExceptions() throws Exception {
		MessageSourceMap map = new MessageSourceMap(messageSource, null);
		thrown.expect(UnsupportedOperationException.class);
		map.size();
	}

	@Test
	public void shouldThrowNestedUnsupportedOperationExceptions() throws Exception {
		MessageSourceMap map = new MessageSourceMap(messageSource, null);
		Value value = map.get("x").get("y");
		thrown.expect(UnsupportedOperationException.class);
		value.size();
	}

	@Test
	public void shouldNotAllowNullKeys() throws Exception {
		MessageSourceMap map = new MessageSourceMap(messageSource, null);
		thrown.expect(IllegalStateException.class);
		map.get(null);
	}

	@Test
	public void shouldGetValue() throws Exception {
		MessageSourceMap map = new MessageSourceMap(messageSource, null);
		Value value = map.get("x");
		assertEquals("x", value.getCodes()[0]);
	}

	@Test
	public void shouldUsePrefixCodes() throws Exception {
		MessageSourceMap map = new MessageSourceMap(messageSource, new String[] { "a.", "b.", "c." });
		Value value = map.get("x");
		assertEquals(Arrays.asList("a.x", "b.x", "c.x"), Arrays.asList(value.getCodes()));
	}

	@Test
	public void shouldResolveOnGetValueToString() throws Exception {
		MessageSourceMap map = new MessageSourceMap(messageSource, null);
		Value value = map.get("x");
		given(messageSource.getMessage(msr("x"), nullLocale())).willReturn("message");
		assertEquals("message", value.toString());
	}

	@Test
	public void shouldUsePrefixCodesOnGetValueToString() throws Exception {
		MessageSourceMap map = new MessageSourceMap(messageSource, new String[] { "a.", "b.", "c." });
		Value value = map.get("x");
		given(messageSource.getMessage(msr("a.x", "b.x", "c.x"), nullLocale())).willReturn("message");
		assertEquals("message", value.toString());
	}

	@Test
	public void shouldAllowNesting() throws Exception {
		MessageSourceMap map = new MessageSourceMap(messageSource, null);
		Value value = map.get("x").get("y").get("z");
		assertEquals("x", value.getCodes()[0]);
		assertEquals(Arrays.asList("y", "z"), Arrays.asList(value.getArguments()));
	}

	@Test
	public void shouldUsePrefixCodesWhenNesting() throws Exception {
		MessageSourceMap map = new MessageSourceMap(messageSource, new String[] { "a.", "b.", "c." });
		Value value = map.get("x").get("y").get("z");
		assertEquals(Arrays.asList("a.x", "b.x", "c.x"), Arrays.asList(value.getCodes()));
		assertEquals(Arrays.asList("y", "z"), Arrays.asList(value.getArguments()));
	}

	@Test
	public void shouldSupportNullPrefixCodesWhenNesting() throws Exception {
		MessageSourceMap map = new MessageSourceMap(messageSource, new String[] { "a.", null, "c." });
		Value value = map.get("x").get("y").get("z");
		assertEquals(Arrays.asList("a.x", "x", "c.x"), Arrays.asList(value.getCodes()));
		assertEquals(Arrays.asList("y", "z"), Arrays.asList(value.getArguments()));
	}

	@Test
	public void shouldHaveToString() throws Exception {
		MessageSourceMap map = new MessageSourceMap(messageSource, new String[] { "a.", "b.", "c." });
		assertTrue(map.toString().contains(
				"messageSource = messageSource, prefixCodes = array<String>['a.', 'b.', 'c.']]"));
	}

	@Test
	public void shouldUseLocale() throws Exception {
		final Locale locale = Locale.ITALY;
		MessageSourceMap map = new MessageSourceMap(messageSource, null) {
			protected Locale getLocale() {
				return locale;
			};
		};
		Value value = map.get("x");
		given(messageSource.getMessage(msr("x"), eq(locale))).willReturn("message");
		assertEquals("message", value.toString());
	}

	@Test
	public void shouldHaveNullDefaultMessage() throws Exception {
		MessageSourceMap map = new MessageSourceMap(messageSource, null);
		Value value = map.get("x");
		assertNull(value.getDefaultMessage());
	}

	private Locale nullLocale() {
		return (Locale) isNull();
	}

	private MessageSourceResolvable msr(String... codes) {
		return argThat(new MessageSourceResolvableMatcher(codes));
	}

	private static class MessageSourceResolvableMatcher extends ArgumentMatcher<MessageSourceResolvable> {

		private String[] codes;

		public MessageSourceResolvableMatcher(String[] codes) {
			this.codes = codes;
		}

		@Override
		public boolean matches(Object argument) {
			if (!(argument instanceof MessageSourceResolvable)) {
				return false;
			}
			MessageSourceResolvable resolvable = (MessageSourceResolvable) argument;
			return Arrays.equals(codes, resolvable.getCodes());
		}
	}
}
