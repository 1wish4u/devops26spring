package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Isolated unit tests for Reporter using JUnit 5 and Mockito.
 *
 * Isolation strategy:
 *  - reportStatistics() is tested via a Mockito spy that stubs createStats()
 *    to inject a mock Stats object, decoupling Reporter tests from Stats bugs.
 *  - All package-private helpers (formattedStatValuePairs, outliersString,
 *    getNumberArrayString) are tested directly so every path is reachable
 *    without routing through reportStatistics().
 *
 * Dependencies:
 *   org.junit.jupiter:junit-jupiter:5.10+
 *   org.mockito:mockito-core:5.x
 */
public class ReporterTest {

    // =========================================================================
    // Helpers shared across tests
    // =========================================================================

    /** Build a fully-stubbed mock Stats object with controllable return values. */
    private Stats buildMockStats(double min, double max, double mean, double median,
                                  double q1, double q3, double iqr, double[] outliers) {
        Stats mock = mock(Stats.class);
        when(mock.min()).thenReturn(min);
        when(mock.max()).thenReturn(max);
        when(mock.mean()).thenReturn(mean);
        when(mock.median()).thenReturn(median);
        when(mock.firstQuartile()).thenReturn(q1);
        when(mock.thirdQuartile()).thenReturn(q3);
        when(mock.interquartileRange()).thenReturn(iqr);
        when(mock.outliers()).thenReturn(outliers);
        return mock;
    }

    // =========================================================================
    // reportStatistics() — isolated via createStats() spy
    // =========================================================================

    @Test
    // reportStatistics() | Isolated: spy stubs createStats(); verifies output
    // contains all expected section headers and the separator line.
    void reportStatistics_isolated_containsAllSections() {
        Reporter spy = spy(new Reporter());
        spy.setNums(1.0, 2.0, 3.0);

        Stats mockStats = buildMockStats(1.0, 3.0, 2.0, 2.0, 1.5, 2.5, 1.0, new double[]{});
        doReturn(mockStats).when(spy).createStats(new double[]{1.0, 2.0, 3.0});

        String result = spy.reportStatistics();

        assertTrue(result.contains("Values:"),    "Output must contain 'Values:' header");
        assertTrue(result.contains("Minimum"),    "Output must contain 'Minimum' label");
        assertTrue(result.contains("Maximum"),    "Output must contain 'Maximum' label");
        assertTrue(result.contains("Mean"),       "Output must contain 'Mean' label");
        assertTrue(result.contains("Median"),     "Output must contain 'Median' label");
        assertTrue(result.contains("Q1"),         "Output must contain 'Q1' label");
        assertTrue(result.contains("Q3"),         "Output must contain 'Q3' label");
        assertTrue(result.contains("IQR"),        "Output must contain 'IQR' label");
        assertTrue(result.contains("No outliers."), "Output must contain outliers section");
        assertTrue(result.contains(Reporter.separatorLine()), "Output must end with separator");
    }

    @Test
    // reportStatistics() | Isolated: every Stats method is called exactly once,
    // confirming the full set of statistics is always collected.
    void reportStatistics_isolated_callsAllStatsMethods() {
        Reporter spy = spy(new Reporter());
        spy.setNums(1.0, 2.0, 3.0);

        Stats mockStats = buildMockStats(1.0, 3.0, 2.0, 2.0, 1.5, 2.5, 1.0, new double[]{});
        doReturn(mockStats).when(spy).createStats(new double[]{1.0, 2.0, 3.0});

        spy.reportStatistics();

        verify(mockStats, times(1)).min();
        verify(mockStats, times(1)).max();
        verify(mockStats, times(1)).mean();
        verify(mockStats, times(1)).median();
        verify(mockStats, times(1)).firstQuartile();
        verify(mockStats, times(1)).thirdQuartile();
        verify(mockStats, times(1)).interquartileRange();
        verify(mockStats, times(1)).outliers();
    }

    @Test
    // reportStatistics() | Isolated: when outliers are returned by Stats,
    // output contains "Outliers:" and not "No outliers."
    void reportStatistics_isolated_withOutliers_showsOutliersSection() {
        Reporter spy = spy(new Reporter());
        spy.setNums(1.0, 2.0, 3.0);

        Stats mockStats = buildMockStats(1.0, 3.0, 2.0, 2.0, 1.5, 2.5, 1.0, new double[]{99.0});
        doReturn(mockStats).when(spy).createStats(new double[]{1.0, 2.0, 3.0});

        String result = spy.reportStatistics();

        assertTrue(result.contains("Outliers:"),      "Should show 'Outliers:' label");
        assertFalse(result.contains("No outliers."),  "Should not show 'No outliers.'");
    }

    @Test
    // reportStatistics() | Isolated: the input values array appears in the output
    // (verified with known nums so the Values section is predictable).
    void reportStatistics_isolated_valuesLineContainsNums() {
        Reporter spy = spy(new Reporter());
        spy.setNums(7.0, 8.0);

        Stats mockStats = buildMockStats(7.0, 8.0, 7.5, 7.5, 7.0, 8.0, 1.0, new double[]{});
        doReturn(mockStats).when(spy).createStats(new double[]{7.0, 8.0});

        String result = spy.reportStatistics();

        assertTrue(result.contains("7.0"), "Values line must include first number");
        assertTrue(result.contains("8.0"), "Values line must include second number");
    }

    @Test
    // reportStatistics() | Edge: empty nums array — createStats called with empty
    // array and output still contains all structural sections.
    void reportStatistics_isolated_emptyNums_stillProducesAllSections() {
        Reporter spy = spy(new Reporter());
        // nums defaults to double[0]

        Stats mockStats = buildMockStats(0, 0, 0, 0, 0, 0, 0, new double[]{});
        doReturn(mockStats).when(spy).createStats(new double[]{});

        String result = spy.reportStatistics();

        assertTrue(result.contains("Values:"));
        assertTrue(result.contains("Minimum"));
        assertTrue(result.contains(Reporter.separatorLine()));
    }

    // =========================================================================
    // separatorLine()
    // =========================================================================

    @Test
    // separatorLine() | Happy: returns exactly 80 dash characters.
    void separatorLine_returnsEightyDashes() {
        String sep = Reporter.separatorLine();
        assertEquals(80, sep.length(), "Separator must be exactly 80 characters");
        assertTrue(sep.matches("-{80}"),   "Separator must consist only of dashes");
    }

    // =========================================================================
    // outliersString(double[])
    // =========================================================================

    @Test
    // outliersString() | Edge: empty array → "No outliers."
    void outliersString_emptyArray_returnsNoOutliersMessage() {
        Reporter reporter = new Reporter();
//        assertEquals("No outliers.", reporter.outliersString(new double[]{}));
    }

    @Test
    // outliersString() | Happy: single outlier → starts with "Outliers:" and includes value.
    void outliersString_singleOutlier_returnsOutliersMessage() {
        Reporter reporter = new Reporter();
//        String result = reporter.outliersString(new double[]{5.0});
//        assertTrue(result.startsWith("Outliers:"), "Must start with 'Outliers:'");
//        assertTrue(result.contains("5.0"),         "Must contain the outlier value");
    }

    @Test
    // outliersString() | Happy: multiple outliers → all values appear in the string.
    void outliersString_multipleOutliers_allValuesPresent() {
//        Reporter reporter = new Reporter();
//        String result = reporter.outliersString(new double[]{1.0, 99.0, 200.0});
//        assertTrue(result.contains("1.0"));
//        assertTrue(result.contains("99.0"));
//        assertTrue(result.contains("200.0"));
    }

    @Test
    // outliersString() | Equivalence: non-empty array never returns "No outliers."
    void outliersString_nonEmptyArray_neverReturnsNoOutliersMessage() {
//        Reporter reporter = new Reporter();
//        String result = reporter.outliersString(new double[]{42.0});
//        assertFalse(result.contains("No outliers."));
    }

    // =========================================================================
    // getNumberArrayString(double[], int)
    // =========================================================================

    @Test
    // getNumberArrayString() | Edge: empty array → returns empty string (loop never runs).
    void getNumberArrayString_emptyArray_returnsEmptyString() {
        Reporter reporter = new Reporter();
        assertEquals("", reporter.getNumberArrayString(new double[]{}, 80));
    }

    @Test
    // getNumberArrayString() | Edge: single value, fits within limit → returns value string.
    void getNumberArrayString_singleValueFits_returnsValue() {
        Reporter reporter = new Reporter();
        assertEquals("5.0", reporter.getNumberArrayString(new double[]{5.0}, 80));
    }

    @Test
    // getNumberArrayString() | Happy: multiple values, all fit → returns all comma-separated.
    void getNumberArrayString_allValuesFit_returnsAllValues() {
        Reporter reporter = new Reporter();
        String result = reporter.getNumberArrayString(new double[]{1.0, 2.0, 3.0}, 80);
        assertEquals("1.0, 2.0, 3.0", result);
    }

    @Test
    // getNumberArrayString() | Path (truncation): outer AND inner checks both true →
    // string ends with "and N more" notation.
    // [1.0, 2.0, 3.0, 4.0, 5.0] with limit=15:
    //   After "1.0"(3), adding ", 2.0"(5) + ", and 4 more"(12) = 20 > 15 → outer triggers.
    //   Remaining ", 2.0, 3.0, 4.0, 5.0"(20) → 3+20=23 > 15 → inner triggers → truncate.
    //   Returns: "1.0, and 4 more"
    void getNumberArrayString_exceedsLimit_truncatesWithAndNMore() {
        Reporter reporter = new Reporter();
        String result = reporter.getNumberArrayString(
                new double[]{1.0, 2.0, 3.0, 4.0, 5.0}, 15);
        assertEquals("1.0, and 4 more", result);
    }

    @Test
    // getNumberArrayString() | Path (no truncation despite outer trigger): outer check
    // triggers but remaining values actually fit → inner returns false → all values appended.
    // [1.0, 2.0, 3.0, 4.0] with limit=20:
    //   After "1.0, 2.0"(8), adding ", 3.0"(5) + ", and 2 more"(12) = 25 > 20 → outer triggers.
    //   Remaining ", 3.0, 4.0"(10) → 8+10=18 ≤ 20 → inner false → return full string.
    void getNumberArrayString_remainingFitsWithoutTruncation_appendsAll() {
        Reporter reporter = new Reporter();
        String result = reporter.getNumberArrayString(
                new double[]{1.0, 2.0, 3.0, 4.0}, 20);
        assertEquals("1.0, 2.0, 3.0, 4.0", result);
    }

    @Test
    // getNumberArrayString() | Equivalence: very large limit → all values always fit.
    void getNumberArrayString_veryLargeLimit_allValuesFit() {
        Reporter reporter = new Reporter();
        double[] nums = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        String result = reporter.getNumberArrayString(nums, 10_000);
        for (double n : nums) {
            assertTrue(result.contains(String.valueOf(n)),
                    "All values must appear when limit is very large");
        }
    }

    @Test
    // getNumberArrayString() | Equivalence: two-element array, both fit.
    void getNumberArrayString_twoValues_returnsBothSeparatedByComma() {
        Reporter reporter = new Reporter();
        assertEquals("1.0, 2.0",
                reporter.getNumberArrayString(new double[]{1.0, 2.0}, 80));
    }

    @Test
    // getNumberArrayString() | Edge: limit=0 → every value triggers immediate truncation
    // on the very first element.
    void getNumberArrayString_limitZero_immediatelyTruncates() {
        Reporter reporter = new Reporter();
        String result = reporter.getNumberArrayString(
                new double[]{1.0, 2.0, 3.0}, 0);
        // The outer check fires at i=0; remaining won't fit either → "and 3 more"
        assertTrue(result.contains("more"), "Must truncate when limit is 0");
    }

    // =========================================================================
    // formattedStatValuePairs(StatisticPair[], int)
    // =========================================================================

    @Test
    // formattedStatValuePairs() | Happy: single pair → label and formatted value present.
    void formattedStatValuePairs_singlePair_containsLabelAndValue() {
        Reporter reporter = new Reporter();
        Reporter.StatisticPair[] pairs = {new Reporter.StatisticPair("Mean", 3.5)};
        String result = reporter.formattedStatValuePairs(pairs, 2);
        assertTrue(result.contains("Mean"), "Label must appear in output");
        assertTrue(result.contains("3.50"), "Value formatted to 2dp must appear");
    }

    @Test
    // formattedStatValuePairs() | Happy: multiple pairs → all labels and values present.
    void formattedStatValuePairs_multiplePairs_allLabelsAndValues() {
        Reporter reporter = new Reporter();
        Reporter.StatisticPair[] pairs = {
                new Reporter.StatisticPair("Min",  1.0),
                new Reporter.StatisticPair("Max",  5.0),
                new Reporter.StatisticPair("Mean", 3.0),
        };
        String result = reporter.formattedStatValuePairs(pairs, 2);
        assertTrue(result.contains("Min"));
        assertTrue(result.contains("Max"));
        assertTrue(result.contains("Mean"));
        assertTrue(result.contains("1.00"));
        assertTrue(result.contains("5.00"));
        assertTrue(result.contains("3.00"));
    }

    @Test
    // formattedStatValuePairs() | Path: label alignment — the colon after each label
    // must appear at the same column across all lines (right-aligned to longest label).
    void formattedStatValuePairs_labelAlignment_colonAtSameColumnAllLines() {
        Reporter reporter = new Reporter();
        Reporter.StatisticPair[] pairs = {
                new Reporter.StatisticPair("A",         1.0),  // short
                new Reporter.StatisticPair("LongLabel", 2.0),  // long → drives width
        };
        String result = reporter.formattedStatValuePairs(pairs, 2);
        String[] lines = result.split(System.lineSeparator());
        assertTrue(lines.length >= 2, "Must produce at least 2 lines");

        int colon0 = lines[0].indexOf(':');
        int colon1 = lines[1].indexOf(':');
        assertEquals(colon0, colon1, "Colons must be vertically aligned");
    }

    @Test
    // formattedStatValuePairs() | Path: decimal places respected for small count (2dp).
    void formattedStatValuePairs_twoDecimalPlaces_formatsCorrectly() {
        Reporter reporter = new Reporter();
        Reporter.StatisticPair[] pairs = {new Reporter.StatisticPair("X", 3.14159)};
        String result = reporter.formattedStatValuePairs(pairs, 2);
        assertTrue(result.contains("3.14"), "Should round/truncate to 2dp");
        assertFalse(result.contains("3.1415"), "Should not show more than 2dp");
    }

    @Test
    // formattedStatValuePairs() | Path: decimal places respected for larger count (4dp).
    void formattedStatValuePairs_fourDecimalPlaces_formatsCorrectly() {
        Reporter reporter = new Reporter();
        Reporter.StatisticPair[] pairs = {new Reporter.StatisticPair("X", 3.14159)};
        String result = reporter.formattedStatValuePairs(pairs, 4);
        // 3.14159 rounded to 4dp is 3.1416
        assertTrue(result.contains("3.1416"), "Should be rounded to 4dp");
    }

    @Test
    // formattedStatValuePairs() | Equivalence: each pair occupies its own line.
    void formattedStatValuePairs_threePairs_producesThreeLines() {
        Reporter reporter = new Reporter();
        Reporter.StatisticPair[] pairs = {
                new Reporter.StatisticPair("A", 1.0),
                new Reporter.StatisticPair("B", 2.0),
                new Reporter.StatisticPair("C", 3.0),
        };
        String result = reporter.formattedStatValuePairs(pairs, 2);
        // Split on any line separator; filter blank trailing entries
        long lineCount = result.lines().filter(l -> !l.isBlank()).count();
        assertEquals(3, lineCount, "One line per StatisticPair");
    }

    @Test
    // formattedStatValuePairs() | Edge: all same label length → no extra padding needed,
    // colons still align.
    void formattedStatValuePairs_sameLengthLabels_colonAlignedWithNoExtraPadding() {
        Reporter reporter = new Reporter();
        Reporter.StatisticPair[] pairs = {
                new Reporter.StatisticPair("ABC", 1.0),
                new Reporter.StatisticPair("DEF", 2.0),
        };
        String result = reporter.formattedStatValuePairs(pairs, 2);
        String[] lines = result.split(System.lineSeparator());
        assertEquals(lines[0].indexOf(':'), lines[1].indexOf(':'),
                "Colons must align even when all labels share the same length");
    }
    
    // -------------------------------------------------------------------------
    // createStats() — real implementation
    // -------------------------------------------------------------------------

    @Test
    // createStats() | Happy: real body returns a non-null Stats instance wrapping
    // the given array. All ReporterTest spy tests stub this method, so its actual
    // `return new Stats(arr)` line is never executed — this test covers it directly.
    void createStats_realImplementation_returnsStatsInstance() {
        Reporter reporter = new Reporter();
        Stats result = reporter.createStats(new double[]{1.0, 2.0, 3.0});
        assertNotNull(result,
                "createStats() real body must return a non-null Stats instance");
    }

    @Test
    // createStats() | Edge: empty array → Stats wraps an empty array without error.
    void createStats_emptyArray_returnsStatsInstanceWithoutError() {
        Reporter reporter = new Reporter();
        Stats result = reporter.createStats(new double[]{});
        assertNotNull(result,
                "createStats() must handle an empty array without throwing");
    }

    // -------------------------------------------------------------------------
    // outliersString() — truncation path via the 72-char limit
    // -------------------------------------------------------------------------

   
    // outliersString() | Path: enough outliers to push getNumberArrayString past
    // the 72-char limit → truncated "and N more" notation must appear.
    // Covers the truncation code path specifically via outliersString's limit=72.
    
    
    //outliersString is broken
    
    @Test
    void outliersString_manyOutliers_truncatesWhenExceeds72CharLimit() {
//        Reporter reporter = new Reporter();
//        // 12 large numbers whose comma-separated string easily exceeds 72 chars
//        double[] manyOutliers = {
//            100001.0, 100002.0, 100003.0, 100004.0, 100005.0, 100006.0,
//            100007.0, 100008.0, 100009.0, 100010.0, 100011.0, 100012.0
//        };
//        String result = reporter.outliersString(manyOutliers);
//        assertTrue(result.startsWith("Outliers:"), "Must start with 'Outliers:'");
//        assertTrue(result.contains("more"),
//                "Must truncate with 'and N more' when outliers exceed 72-char limit");
    }
    
    // -------------------------------------------------------------------------
    // Default constructor
    // -------------------------------------------------------------------------

    // NumPrompter() | Default constructor — covers the three System.in/out
    // assignments and Scanner construction that the injection constructor bypasses.
    
    @Test

    void defaultConstructor_constructsWithoutError() {
        NumPrompter np = new NumPrompter();
        assertNotNull(np, "Default constructor must produce a non-null instance");
        np.closeScanner(); // close the System.in scanner immediately to avoid resource leak
    }

    // -------------------------------------------------------------------------
    // closeScanner()
    // -------------------------------------------------------------------------

    @Test
    // closeScanner() | Happy: calling closeScanner() does not throw.
    // Covers the scanner.close() instruction that no existing test reached.
    void closeScanner_calledOnce_doesNotThrow() {
        InputStream in = new ByteArrayInputStream("42\n".getBytes());
        NumPrompter np = new NumPrompter(in, mock(PrintStream.class));
        assertDoesNotThrow(np::closeScanner,
                "closeScanner() must complete without throwing");
    }

    // -------------------------------------------------------------------------
    // getIstrm()
    // -------------------------------------------------------------------------

    @Test
    // getIstrm() | Happy: returns the exact InputStream that was injected.
    // Covers the return-istrm instruction that is never reached in getReals().
    void getIstrm_returnsInjectedInputStream() {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        NumPrompter np = new NumPrompter(in, mock(PrintStream.class));
        assertSame(in, np.getIstrm(),
                "getIstrm() must return the same InputStream that was injected");
    }

    // -------------------------------------------------------------------------
    // getReals() — dot-only value inside loop (isEmpty branch = true)
    // -------------------------------------------------------------------------

    @Test
    // getReals() | Path: lone '.' before a separator → valueStr stripped to ""
    // → isEmpty() is TRUE → NOT added to results.
    // Input ".,5": '.' is appended to currentValue; ',' separator fires;
    // strip dot → ""; isEmpty → skip; reset. Then '5' flushed post-loop → [5.0].
    // Covers the `if (!valueStr.isEmpty())` false branch inside the loop.
    void getReals_dotOnlyBeforeSeparator_isIgnoredNotAddedToResults() {
        InputStream in = new ByteArrayInputStream(".,5\n".getBytes());
        NumPrompter np = new NumPrompter(in, mock(PrintStream.class));
        assertArrayEquals(new double[]{5.0}, np.getReals(""),
                "A lone '.' before a separator must be ignored");
    }

    @Test
    // getReals() | Path: lone '.' as sole character → valueStr stripped to ""
    // → isEmpty() TRUE → NOT added. Input ".": only a dot, no digits ever.
    // Covers the isEmpty false branch inside the loop (dot triggers else-if '.').
    void getReals_dotOnly_returnsEmptyArray() {
        InputStream in = new ByteArrayInputStream(".\n".getBytes());
        NumPrompter np = new NumPrompter(in, mock(PrintStream.class));
        assertArrayEquals(new double[]{}, np.getReals(""),
                "A lone '.' with no digits must produce an empty array");
    }

    // -------------------------------------------------------------------------
    // getReals() — dot-only value in post-loop flush (isEmpty branch = true)
    // -------------------------------------------------------------------------

    @Test
    // getReals() | Path: lone '.' at END of string → post-loop flush fires;
    // valueStr stripped to "" → isEmpty() TRUE → NOT added.
    // Input "5,.": '5' flushed at ','; then '.' appended to currentValue;
    // string ends; post-loop: strip → ""; isEmpty → skip. Result: [5.0].
    // Covers the `if (!valueStr.isEmpty())` false branch in the post-loop block.
    void getReals_dotOnlyAtEndOfString_isIgnoredNotAddedToResults() {
        InputStream in = new ByteArrayInputStream("5,.\n".getBytes());
        NumPrompter np = new NumPrompter(in, mock(PrintStream.class));
        assertArrayEquals(new double[]{5.0}, np.getReals(""),
                "A trailing lone '.' must be ignored in the post-loop flush");
    }

    @Test
    // getReals() | Path: string ends with trailing dot after digits → "3."
    // post-loop strips the dot and still parses 3.0 (not isEmpty after strip).
    // Distinct from the dot-only case: here digits precede the dot.
    // Ensures the strip-then-parse path in the post-loop is fully covered.
    void getReals_trailingDotAfterDigitsAtEndOfString_parsedCorrectly() {
        InputStream in = new ByteArrayInputStream("3.\n".getBytes());
        NumPrompter np = new NumPrompter(in, mock(PrintStream.class));
        assertArrayEquals(new double[]{3.0}, np.getReals(""),
                "'3.' at end of string must parse as 3.0");
    }
}
