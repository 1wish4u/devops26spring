package model;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
* Isolated unit tests for NumPrompter using JUnit 5 and Mockito.
*
* Isolation strategy:
*  - getReals() is tested directly via the package-private constructor, which
*    accepts an injected InputStream (ByteArrayInputStream wrapping known input)
*    and a mock PrintStream (to verify prompt output).
*  - getInts() is tested in isolation by spying on NumPrompter and stubbing
*    getReals(), so only the floor/cast conversion logic is exercised.
*
* Dependencies:
*   org.junit.jupiter:junit-jupiter:5.10+
*   org.mockito:mockito-core:5.x
*/
public class NumPrompterTest {
   // =========================================================================
   // Helper
   // =========================================================================
   /**
    * Creates a NumPrompter backed by a ByteArrayInputStream containing the
    * given input string (terminated with a newline so hasNextLine() is true)
    * and a mock PrintStream so prompt output can be verified or silently discarded.
    */
   private NumPrompter prompterWith(String input, PrintStream mockOut) {
       InputStream in = new ByteArrayInputStream(
               (input + "\n").getBytes(StandardCharsets.UTF_8));
       return new NumPrompter(in, mockOut);
   }
   /** Convenience overload — uses a silent mock PrintStream. */
   private NumPrompter prompterWith(String input) {
       return prompterWith(input, mock(PrintStream.class));
   }
   /** Creates a NumPrompter whose scanner has no lines (EOF immediately). */
   private NumPrompter emptyPrompter() {
       InputStream in = new ByteArrayInputStream(new byte[0]);
       return new NumPrompter(in, mock(PrintStream.class));
   }
   // =========================================================================
   // getReals() — message printing
   // =========================================================================
   @Test
   // getReals() | Happy: non-empty message → ostrm.println() called with that message.
   void getReals_nonEmptyMessage_printsMessage() {
       PrintStream mockOut = mock(PrintStream.class);
       NumPrompter np = prompterWith("1", mockOut);
       np.getReals("Enter numbers:");
       verify(mockOut, times(1)).println("Enter numbers:");
   }
   @Test
   // getReals() | Edge: empty message → ostrm.println() is never called.
   void getReals_emptyMessage_doesNotPrint() {
       PrintStream mockOut = mock(PrintStream.class);
       NumPrompter np = prompterWith("1", mockOut);
       np.getReals("");
       verify(mockOut, never()).println(anyString());
   }
   // =========================================================================
   // getReals() — scanner guard (no next line)
   // =========================================================================
   @Test
   // getReals() | Edge: scanner has no next line → returns empty array immediately.
   void getReals_noNextLine_returnsEmptyArray() {
       NumPrompter np = emptyPrompter();
       assertArrayEquals(new double[]{}, np.getReals("Enter:"));
   }
   // =========================================================================
   // getReals() — empty / whitespace-only input
   // =========================================================================
   @Test
   // getReals() | Edge: empty string input → no digits → returns empty array.
   void getReals_emptyInput_returnsEmptyArray() {
       NumPrompter np = prompterWith("");
       assertArrayEquals(new double[]{}, np.getReals(""));
   }
   @Test
   // getReals() | Edge: input is only separators → no digits found → returns empty array.
   void getReals_onlySeparators_returnsEmptyArray() {
       NumPrompter np = prompterWith(",,, ,  ,");
       assertArrayEquals(new double[]{}, np.getReals(""));
   }
   // =========================================================================
   // getReals() — single value paths
   // =========================================================================
   @Test
   // getReals() | Happy: single integer with no trailing separator →
   // flushed in the post-loop block.
   void getReals_singleIntegerNoTrailingSeparator_returnsOneElement() {
       NumPrompter np = prompterWith("5");
       assertArrayEquals(new double[]{5.0}, np.getReals(""));
   }
   @Test
   // getReals() | Happy: single integer with trailing separator →
   // value flushed inside the loop, post-loop block does nothing.
   void getReals_singleIntegerWithTrailingSeparator_returnsOneElement() {
       NumPrompter np = prompterWith("5,");
       assertArrayEquals(new double[]{5.0}, np.getReals(""));
   }
   @Test
   // getReals() | Happy: single decimal number → parsed correctly.
   void getReals_singleDecimal_returnsCorrectValue() {
       NumPrompter np = prompterWith("3.14");
       assertArrayEquals(new double[]{3.14}, np.getReals(""));
   }
   @Test
   // getReals() | Path: trailing decimal stripped → "3." becomes 3.0.
   void getReals_trailingDecimal_stripsDecimalAndParses() {
       NumPrompter np = prompterWith("3.");
       assertArrayEquals(new double[]{3.0}, np.getReals(""));
   }
   @Test
   // getReals() | Path: trailing decimal before separator → "3.," strips decimal.
   void getReals_trailingDecimalBeforeSeparator_stripsDecimal() {
       NumPrompter np = prompterWith("3.,");
       assertArrayEquals(new double[]{3.0}, np.getReals(""));
   }
   @Test
   // getReals() | Edge: zero → parsed correctly.
   void getReals_zero_returnszero() {
       NumPrompter np = prompterWith("0");
       assertArrayEquals(new double[]{0.0}, np.getReals(""));
   }
   @Test
   // getReals() | Equivalence: large integer → no overflow in double parsing.
   void getReals_largeInteger_parsedCorrectly() {
       NumPrompter np = prompterWith("123456789");
       assertArrayEquals(new double[]{123456789.0}, np.getReals(""));
   }
   // =========================================================================
   // getReals() — multiple values, various separators
   // =========================================================================
   @Test
   // getReals() | Happy: comma-separated integers → all parsed.
   void getReals_commaSeparated_returnsAllValues() {
       NumPrompter np = prompterWith("1,2,3");
       assertArrayEquals(new double[]{1.0, 2.0, 3.0}, np.getReals(""));
   }
   @Test
   // getReals() | Happy: space-separated integers → all parsed.
   void getReals_spaceSeparated_returnsAllValues() {
       NumPrompter np = prompterWith("1 2 3");
       assertArrayEquals(new double[]{1.0, 2.0, 3.0}, np.getReals(""));
   }
   @Test
   // getReals() | Equivalence: mixed separators (comma+space, semicolon) → all parsed.
   void getReals_mixedSeparators_returnsAllValues() {
       NumPrompter np = prompterWith("1, 2; 3");
       assertArrayEquals(new double[]{1.0, 2.0, 3.0}, np.getReals(""));
   }
   @Test
   // getReals() | Equivalence: multiple consecutive separators between values →
   // treated as a single separation (extra separator chars have length==0 currentValue so
   // the else-if branch silently skips them).
   void getReals_multipleConsecutiveSeparators_returnsValues() {
       NumPrompter np = prompterWith("1,,,,2");
       assertArrayEquals(new double[]{1.0, 2.0}, np.getReals(""));
   }
   @Test
   // getReals() | Path: leading separator → ignored, first value still parsed.
   void getReals_leadingSeparator_firstValueStillParsed() {
       NumPrompter np = prompterWith(",5");
       assertArrayEquals(new double[]{5.0}, np.getReals(""));
   }
   @Test
   // getReals() | Happy: comma-space separated decimals → parsed correctly.
   void getReals_commaSpaceSeparatedDecimals_parsedCorrectly() {
       NumPrompter np = prompterWith("3.14, 2.71");
       assertArrayEquals(new double[]{3.14, 2.71}, np.getReals(""));
   }
   // =========================================================================
   // getReals() — multi-decimal / special decimal handling
   // =========================================================================
   @Test
   // getReals() | Path: extra decimals are ignored — digits after second decimal
   // remain part of the same number. "3.14.15" → 3.1415 (not two numbers).
   void getReals_extraDecimalIgnored_digitsAppendedToSameNumber() {
       NumPrompter np = prompterWith("3.14.15");
       assertArrayEquals(new double[]{3.1415}, np.getReals(""));
   }
   @Test
   // getReals() | Path: decimal reset between numbers — second number can have
   // its own decimal after a separator resets hasDecimalPoint.
   void getReals_decimalResetBetweenNumbers_eachNumberCanHaveDecimal() {
       NumPrompter np = prompterWith("1.5,2.5");
       assertArrayEquals(new double[]{1.5, 2.5}, np.getReals(""));
   }
   // =========================================================================
   // getReals() — last-value flush (post-loop block)
   // =========================================================================
   @Test
   // getReals() | Path: last value has no trailing separator → post-loop block
   // flushes it. Verified by checking all values in "1,2,3" are returned.
   void getReals_lastValueNoTrailingSeparator_flushedAfterLoop() {
       NumPrompter np = prompterWith("1,2,3");
       double[] result = np.getReals("");
       assertEquals(3, result.length, "All three values including the last must be present");
       assertEquals(3.0, result[2]);
   }
   @Test
   // getReals() | Path: input ends with separator → post-loop currentValue is empty
   // → post-loop block adds nothing. Length must equal the number of real values only.
   void getReals_trailingSeparatorAtEnd_noExtraElementAppended() {
       NumPrompter np = prompterWith("1,2,3,");
       assertArrayEquals(new double[]{1.0, 2.0, 3.0}, np.getReals(""));
   }
   // =========================================================================
   // getInts() — isolated (stubs getReals via spy)
   // =========================================================================
   @Test
   // getInts() | Isolated: spy stubs getReals() → only the floor/cast conversion is tested.
   // Whole numbers: 1.0, 2.0, 3.0 → [1, 2, 3].
   void getInts_isolated_wholeNumbers_returnsCorrectInts() {
       NumPrompter spy = spy(new NumPrompter(
               new ByteArrayInputStream("\n".getBytes()), mock(PrintStream.class)));
       doReturn(new double[]{1.0, 2.0, 3.0}).when(spy).getReals("Enter:");
       assertArrayEquals(new int[]{1, 2, 3}, spy.getInts("Enter:"));
   }
   @Test
   // getInts() | Isolated: decimal values are floored (not rounded).
   // 3.7 → 3, 2.9 → 2.
   void getInts_isolated_decimalsFloored_notRounded() {
       NumPrompter spy = spy(new NumPrompter(
               new ByteArrayInputStream("\n".getBytes()), mock(PrintStream.class)));
       doReturn(new double[]{3.7, 2.9}).when(spy).getReals("");
       assertArrayEquals(new int[]{3, 2}, spy.getInts(""));
   }
   @Test
   // getInts() | Isolated: 0.9 floors to 0, not 1.
   void getInts_isolated_pointNineFloorsToZero() {
       NumPrompter spy = spy(new NumPrompter(
               new ByteArrayInputStream("\n".getBytes()), mock(PrintStream.class)));
       doReturn(new double[]{0.9}).when(spy).getReals("");
       assertArrayEquals(new int[]{0}, spy.getInts(""));
   }
   @Test
   // getInts() | Isolated: getReals() is called exactly once with the same message.
   void getInts_isolated_delegatesToGetRealsExactlyOnce() {
       NumPrompter spy = spy(new NumPrompter(
               new ByteArrayInputStream("\n".getBytes()), mock(PrintStream.class)));
       doReturn(new double[]{5.0}).when(spy).getReals("Prompt");
       spy.getInts("Prompt");
       verify(spy, times(1)).getReals("Prompt");
   }
   @Test
   // getInts() | Isolated: empty array from getReals() → getInts() returns empty int[].
   void getInts_isolated_emptyReals_returnsEmptyIntArray() {
       NumPrompter spy = spy(new NumPrompter(
               new ByteArrayInputStream("\n".getBytes()), mock(PrintStream.class)));
       doReturn(new double[]{}).when(spy).getReals("");
       assertArrayEquals(new int[]{}, spy.getInts(""));
   }
   @Test
   // getInts() | Isolated: large double floors correctly to int.
   void getInts_isolated_largeDoubleFloorsCorrectly() {
       NumPrompter spy = spy(new NumPrompter(
               new ByteArrayInputStream("\n".getBytes()), mock(PrintStream.class)));
       doReturn(new double[]{999.99}).when(spy).getReals("");
       assertArrayEquals(new int[]{999}, spy.getInts(""));
   }
}

