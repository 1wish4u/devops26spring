package model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

/**
 * Isolated unit tests for the Stats class using JUnit 5 and Mockito.
 *
 * Tests labeled "[BUG]" are intentionally written to FAIL against the original
 * buggy implementation — they define correct behavior and pass once bugs are fixed.
 *
 * Mockito spies stub internal collaborator calls (firstQuartile, thirdQuartile,
 * interquartileRange, createStats) so each method can be tested in true isolation
 * without depending on the correctness of other methods.
 *
 * aryEq() is required when stubbing createStats() because double[] uses reference
 * equality — aryEq() performs deep array comparison so the stub fires correctly.
 */
public class StatsTest {

    // =========================================================================
    // min()
    // =========================================================================

    @Test
    // Tests: min() | Edge: empty array hits the guard clause and returns 0
    void min_emptyArray_returnsZero() {
        Stats stats = new Stats();
        assertEquals(0.0, stats.min());
    }

    @Test
    // Tests: min() | Edge: single element — loop body never executes, returns that element
    void min_singleElement_returnsThatElement() {
        Stats stats = new Stats(7.0);
        assertEquals(7.0, stats.min());
    }

    @Test
    // Tests: min() | Happy: minimum is the first element — loop never replaces the initial value
    void min_minIsFirstElement_returnsCorrectMin() {
        Stats stats = new Stats(1.0, 3.0, 5.0);
        assertEquals(1.0, stats.min());
    }

    @Test
    // Tests: min() | Path: minimum is the last element — loop must run to full length to find it
    void min_minIsLastElement_returnsCorrectMin() {
        Stats stats = new Stats(5.0, 3.0, 1.0);
        assertEquals(1.0, stats.min());
    }

    @Test
    // Tests: min() | Path: minimum is a middle element — loop finds it partway through
    void min_minIsMiddleElement_returnsCorrectMin() {
        Stats stats = new Stats(5.0, 1.0, 3.0);
        assertEquals(1.0, stats.min());
    }

    @Test
    // Tests: min() | Equivalence: all-negative values — returns the most negative
    void min_allNegativeValues_returnsSmallestNegative() {
        Stats stats = new Stats(-1.0, -5.0, -3.0);
        assertEquals(-5.0, stats.min());
    }

    @Test
    // Tests: min() | Equivalence: all values equal — returns that shared value
    void min_allSameValues_returnsThatValue() {
        Stats stats = new Stats(3.0, 3.0, 3.0);
        assertEquals(3.0, stats.min());
    }

    @Test
    // Tests: min() | Equivalence: mixed positive and negative — returns the most negative
    void min_mixedSignValues_returnsMostNegative() {
        Stats stats = new Stats(-2.0, 4.0, 0.0);
        assertEquals(-2.0, stats.min());
    }

    // =========================================================================
    // max()
    // =========================================================================

    @Test
    // Tests: max() | Edge: empty array hits the guard clause and returns 0
    void max_emptyArray_returnsZero() {
        Stats stats = new Stats();
        assertEquals(0.0, stats.max());
    }

    @Test
    // Tests: max() | Edge: single element — loop body never executes, returns that element
    void max_singleElement_returnsThatElement() {
        Stats stats = new Stats(7.0);
        assertEquals(7.0, stats.max());
    }

    @Test
    // Tests: max() | Happy: maximum is the first element — loop never replaces the initial value
    void max_maxIsFirstElement_returnsCorrectMax() {
        Stats stats = new Stats(5.0, 3.0, 1.0);
        assertEquals(5.0, stats.max());
    }

    @Test
    // Tests: max() | Happy: maximum is a middle element — loop finds it partway through
    void max_maxIsMiddleElement_returnsCorrectMax() {
        Stats stats = new Stats(1.0, 5.0, 3.0);
        assertEquals(5.0, stats.max());
    }

    @Test
    // Tests: max() | [BUG] Loop condition `i < nums.length-1` stops one element early
    // so the last element is never checked. max([1, 2, 5]) returns 2.0, expects 5.0.
    // Fix: change loop condition to `i < nums.length`.
    void max_maxIsLastElement_bugRevealed() {
        Stats stats = new Stats(1.0, 2.0, 5.0);
        assertEquals(5.0, stats.max());  // FAILS on original: returns 2.0
    }

    @Test
    // Tests: max() | Equivalence: all values equal — returns that shared value
    void max_allSameValues_returnsThatValue() {
        Stats stats = new Stats(4.0, 4.0, 4.0);
        assertEquals(4.0, stats.max());
    }

    @Test
    // Tests: max() | Equivalence: all-negative values — returns the least negative
    void max_allNegativeValues_returnsLeastNegative() {
        Stats stats = new Stats(-5.0, -1.0, -3.0);
        assertEquals(-1.0, stats.max());
    }

    // =========================================================================
    // mean()
    // =========================================================================

    @Test
    // Tests: mean() | Edge: empty array hits the guard clause and returns 0
    void mean_emptyArray_returnsZero() {
        Stats stats = new Stats();
        assertEquals(0.0, stats.mean());
    }

    @Test
    // Tests: mean() | [BUG] Operator precedence: `sum / nums.length - 1` parses as
    // `(sum / nums.length) - 1`. mean([5.0]) = (5/1) - 1 = 4.0, expects 5.0.
    // Fix: remove the stray `- 1` so it reads `sum / nums.length`.
    void mean_singleElement_bugRevealed() {
        Stats stats = new Stats(5.0);
        assertEquals(5.0, stats.mean());  // FAILS on original: returns 4.0
    }

    @Test
    // Tests: mean() | [BUG] Same operator-precedence bug with multiple elements.
    // mean([2, 4, 6]) = (12/3) - 1 = 3.0, expects 4.0.
    void mean_multipleElements_bugRevealed() {
        Stats stats = new Stats(2.0, 4.0, 6.0);
        assertEquals(4.0, stats.mean());  // FAILS on original: returns 3.0
    }

    @Test
    // Tests: mean() | [BUG] Operator-precedence bug with all-negative values.
    // mean([-2, -4, -6]) = (-12/3) - 1 = -5.0, expects -4.0.
    void mean_negativeValues_bugRevealed() {
        Stats stats = new Stats(-2.0, -4.0, -6.0);
        assertEquals(-4.0, stats.mean());  // FAILS on original: returns -5.0
    }

    @Test
    // Tests: mean() | [BUG] Operator-precedence bug when all values are equal.
    // mean([3, 3, 3]) = (9/3) - 1 = 2.0, expects 3.0.
    void mean_allSameValues_bugRevealed() {
        Stats stats = new Stats(3.0, 3.0, 3.0);
        assertEquals(3.0, stats.mean());  // FAILS on original: returns 2.0
    }

    // =========================================================================
    // median()
    // =========================================================================

    @Test
    // Tests: median() | Edge: empty array hits the guard clause and returns 0
    void median_emptyArray_returnsZero() {
        Stats stats = new Stats();
        assertEquals(0.0, stats.median());
    }

    @Test
    // Tests: median() | Edge: single element — odd-length branch returns that element
    void median_singleElement_returnsThatElement() {
        Stats stats = new Stats(5.0);
        assertEquals(5.0, stats.median());
    }

    @Test
    // Tests: median() | Happy: already-sorted odd-length array — middle element is returned
    void median_sortedOddLength_returnsMiddleElement() {
        Stats stats = new Stats(1.0, 3.0, 5.0);
        assertEquals(3.0, stats.median());
    }

    @Test
    // Tests: median() | [BUG] Array is never sorted before indexing.
    // median([5, 1, 3]) picks nums[1] = 1.0 from the unsorted array; sorted median is 3.0.
    // Fix: sort a local copy before computing the middle index.
    void median_unsortedOddLength_bugRevealed() {
        Stats stats = new Stats(5.0, 1.0, 3.0);
        assertEquals(3.0, stats.median());  // FAILS on original: returns 1.0
    }

    @Test
    // Tests: median() | [BUG] Even-length arrays must average the two middle elements.
    // median([1, 2, 3, 4]) returns nums[2] = 3.0; correct answer is (2+3)/2 = 2.5.
    // Fix: when length is even, return (sorted[mid-1] + sorted[mid]) / 2.0.
    void median_evenLength_bugRevealed() {
        Stats stats = new Stats(1.0, 2.0, 3.0, 4.0);
        assertEquals(2.5, stats.median());  // FAILS on original: returns 3.0
    }

    @Test
    // Tests: median() | [BUG] Two-element even-length case: median([2, 8]) should be 5.0,
    // original returns nums[1] = 8.0 without averaging.
    void median_twoElements_bugRevealed() {
        Stats stats = new Stats(2.0, 8.0);
        assertEquals(5.0, stats.median());  // FAILS on original: returns 8.0
    }

    // =========================================================================
    // firstQuartile()
    // =========================================================================

    @Test
    // Tests: firstQuartile() | Edge: empty array hits the length < 2 guard and returns 0
    void firstQuartile_emptyArray_returnsZero() {
        Stats stats = new Stats();
        assertEquals(0.0, stats.firstQuartile());
    }

    @Test
    // Tests: firstQuartile() | Edge: single element hits the length < 2 guard and returns 0
    void firstQuartile_singleElement_returnsZero() {
        Stats stats = new Stats(5.0);
        assertEquals(0.0, stats.firstQuartile());
    }

    @Test
    // Tests: firstQuartile() | Happy: even-length [1,2,3,4] — lower half [1,2] — Q1 = 1.5
    void firstQuartile_evenLength_returnsCorrectQ1() {
        Stats stats = new Stats(1.0, 2.0, 3.0, 4.0);
        assertEquals(1.5, stats.firstQuartile());
    }

    @Test
    // Tests: firstQuartile() | Happy: odd-length [1,2,3,4,5] — lower half [1,2] — Q1 = 1.5
    void firstQuartile_oddLength_returnsCorrectQ1() {
        Stats stats = new Stats(1.0, 2.0, 3.0, 4.0, 5.0);
        assertEquals(1.5, stats.firstQuartile());
    }

    @Test
    // Tests: firstQuartile() | Happy: unsorted input [4,1,3,2] is sorted to [1,2,3,4]
    // before splitting, so Q1 is still computed correctly as 1.5
    void firstQuartile_unsortedInput_sortsBeforeSplitting() {
        Stats stats = new Stats(4.0, 1.0, 3.0, 2.0);
        assertEquals(1.5, stats.firstQuartile());
    }

    @Test
    // Tests: firstQuartile() | Isolated: spy stubs createStats() via aryEq() so the
    // median() call on the lower-half sub-array is fully controlled — proves firstQuartile()
    // delegates to createStats() then calls median() on the result
    void firstQuartile_isolated_usesCreateStatsForLowerHalf() {
        Stats spy = spy(new Stats(10.0, 20.0, 30.0, 40.0));
        Stats mockHalf = mock(Stats.class);
        when(mockHalf.median()).thenReturn(15.0);
        doReturn(mockHalf).when(spy).createStats(aryEq(new double[]{10.0, 20.0}));

        assertEquals(15.0, spy.firstQuartile());
        verify(mockHalf).median();
    }

    // =========================================================================
    // thirdQuartile()
    // =========================================================================

    @Test
    // Tests: thirdQuartile() | Edge: empty array hits the length < 2 guard and returns 0
    void thirdQuartile_emptyArray_returnsZero() {
        Stats stats = new Stats();
        assertEquals(0.0, stats.thirdQuartile());
    }

    @Test
    // Tests: thirdQuartile() | Edge: single element hits the length < 2 guard and returns 0
    void thirdQuartile_singleElement_returnsZero() {
        Stats stats = new Stats(5.0);
        assertEquals(0.0, stats.thirdQuartile());
    }

    @Test
    // Tests: thirdQuartile() | Path: even-length branch — upper half is a clean right split.
    // [1,2,3,4] → upper half [3,4] → Q3 = 3.5
    void thirdQuartile_evenLengthBranch_returnsCorrectQ3() {
        Stats stats = new Stats(1.0, 2.0, 3.0, 4.0);
        assertEquals(3.5, stats.thirdQuartile());
    }

    @Test
    // Tests: thirdQuartile() | Path: odd-length branch — middle element is excluded first.
    // [1,2,3,4,5] → upper half [4,5] → Q3 = 4.5
    void thirdQuartile_oddLengthBranch_returnsCorrectQ3() {
        Stats stats = new Stats(1.0, 2.0, 3.0, 4.0, 5.0);
        assertEquals(4.5, stats.thirdQuartile());
    }

    @Test
    // Tests: thirdQuartile() | Happy: unsorted input [4,1,3,2] is sorted to [1,2,3,4]
    // before splitting, so Q3 is still computed correctly as 3.5
    void thirdQuartile_unsortedInput_sortsBeforeSplitting() {
        Stats stats = new Stats(4.0, 1.0, 3.0, 2.0);
        assertEquals(3.5, stats.thirdQuartile());
    }

    @Test
    // Tests: thirdQuartile() | Isolated: spy stubs createStats() via aryEq() so the
    // median() call on the upper-half sub-array is fully controlled — proves thirdQuartile()
    // delegates to createStats() then calls median() on the result
    void thirdQuartile_isolated_usesCreateStatsForUpperHalf() {
        Stats spy = spy(new Stats(10.0, 20.0, 30.0, 40.0));
        Stats mockHalf = mock(Stats.class);
        when(mockHalf.median()).thenReturn(35.0);
        doReturn(mockHalf).when(spy).createStats(aryEq(new double[]{30.0, 40.0}));

        assertEquals(35.0, spy.thirdQuartile());
        verify(mockHalf).median();
    }

    // =========================================================================
    // interquartileRange()
    // =========================================================================

    @Test
    // Tests: interquartileRange() | Isolated: spy stubs firstQuartile() and thirdQuartile()
    // so only the subtraction logic (Q3 - Q1) is under test
    void interquartileRange_isolated_returnsQ3MinusQ1() {
        Stats spy = spy(new Stats(1.0, 2.0, 3.0, 4.0));
        doReturn(2.0).when(spy).firstQuartile();
        doReturn(7.0).when(spy).thirdQuartile();

        assertEquals(5.0, spy.interquartileRange());
        verify(spy).firstQuartile();
        verify(spy).thirdQuartile();
    }

    @Test
    // Tests: interquartileRange() | Isolated: IQR is zero when Q1 and Q3 are equal
    void interquartileRange_isolated_zeroWhenQ1EqualsQ3() {
        Stats spy = spy(new Stats(1.0, 2.0, 3.0, 4.0));
        doReturn(3.0).when(spy).firstQuartile();
        doReturn(3.0).when(spy).thirdQuartile();

        assertEquals(0.0, spy.interquartileRange());
    }

    @Test
    // Tests: interquartileRange() | Edge: single element — both quartiles return 0, so IQR = 0
    void interquartileRange_singleElement_returnsZero() {
        Stats stats = new Stats(5.0);
        assertEquals(0.0, stats.interquartileRange());
    }

    @Test
    // Tests: interquartileRange() | Integration: [1,2,3,4] → Q3(3.5) - Q1(1.5) = 2.0
    void interquartileRange_integration_returnsCorrectIQR() {
        Stats stats = new Stats(1.0, 2.0, 3.0, 4.0);
        assertEquals(2.0, stats.interquartileRange());
    }

    // =========================================================================
    // outliers()
    // =========================================================================

    @Test
    // Tests: outliers() | Edge: empty array hits the length < 2 guard and returns empty array
    void outliers_emptyArray_returnsEmptyArray() {
        Stats stats = new Stats();
        assertArrayEquals(new double[]{}, stats.outliers());
    }

    @Test
    // Tests: outliers() | Edge: single element hits the length < 2 guard and returns empty array
    void outliers_singleElement_returnsEmptyArray() {
        Stats stats = new Stats(5.0);
        assertArrayEquals(new double[]{}, stats.outliers());
    }

    @Test
    // Tests: outliers() | Isolated: lower-outlier path — value < Q1 - 1.5*IQR.
    // Fences: lower = 2.0 - 1.5*2.0 = -1.0; upper = 4.0 + 1.5*2.0 = 7.0.
    // -100.0 < -1.0 so it is flagged; 2.0-5.0 are inside fences so they are not.
    void outliers_isolated_detectsLowerOutlierCorrectly() {
        Stats spy = spy(new Stats(-100.0, 2.0, 3.0, 4.0, 5.0));
        doReturn(2.0).when(spy).firstQuartile();
        doReturn(4.0).when(spy).thirdQuartile();
        doReturn(2.0).when(spy).interquartileRange();

        double[] result = spy.outliers();
        assertTrue(result.length > 0);
        assertEquals(-100.0, result[0]);
    }

    @Test
    // Tests: outliers() | [BUG] Upper-outlier condition uses `<` instead of `>`.
    // `value < q3 + 1.5*iqr` misses real upper outliers entirely.
    // upper fence = 7.0; 100.0 > 7.0 so it must appear in results.
    // Fix: change second condition to `value > q3 + 1.5 * iqr`.
    void outliers_isolated_upperOutlierBugRevealed() {
        Stats spy = spy(new Stats(2.0, 3.0, 4.0, 5.0, 100.0));
        doReturn(2.0).when(spy).firstQuartile();
        doReturn(4.0).when(spy).thirdQuartile();
        doReturn(2.0).when(spy).interquartileRange();

        double[] result = spy.outliers();
        assertTrue(
            result.length > 0 && contains(result, 100.0),
            "Expected 100.0 to be detected as an upper outlier"
        );  // FAILS on original: condition uses < so 100.0 is never caught
    }

    @Test
    // Tests: outliers() | [BUG] Buggy condition `value < q3 + 1.5*iqr` produces false
    // positives — values clearly inside fences are incorrectly flagged as outliers.
    // Fix: change second condition to `value > q3 + 1.5 * iqr`.
    void outliers_isolated_falsePositivesFromBuggyCondition() {
        Stats spy = spy(new Stats(2.0, 3.0, 4.0, 5.0));
        doReturn(2.0).when(spy).firstQuartile();
        doReturn(4.0).when(spy).thirdQuartile();
        doReturn(2.0).when(spy).interquartileRange();

        double[] result = spy.outliers();
        assertArrayEquals(
            new double[]{}, result,
            "Expected no outliers — all values are inside fences [-1.0, 7.0]"
        );  // FAILS on original: buggy < flags values that are < 7.0
    }

    @Test
    // Tests: outliers() | Isolated: no values outside fences — result is empty array
    void outliers_isolated_noOutliersWhenAllInFences() {
        Stats spy = spy(new Stats(1.0, 2.0, 3.0, 4.0, 5.0));
        doReturn(1.5).when(spy).firstQuartile();
        doReturn(4.5).when(spy).thirdQuartile();
        doReturn(3.0).when(spy).interquartileRange();
        // lower fence = 1.5 - 4.5 = -3.0; upper fence = 4.5 + 4.5 = 9.0

        assertArrayEquals(new double[]{}, spy.outliers());
    }

    @Test
    // Tests: outliers() | Isolated: one lower and one upper outlier both detected correctly
    void outliers_isolated_detectsBothLowerAndUpperOutliers() {
        Stats spy = spy(new Stats(-100.0, 2.0, 3.0, 4.0, 100.0));
        doReturn(2.0).when(spy).firstQuartile();
        doReturn(4.0).when(spy).thirdQuartile();
        doReturn(2.0).when(spy).interquartileRange();
        // lower fence = -1.0; upper fence = 7.0

        double[] result = spy.outliers();
        assertEquals(2, result.length);
        assertTrue(contains(result, -100.0));
        assertTrue(contains(result, 100.0));
    }

    // =========================================================================
    // setNums()
    // =========================================================================

    @Test
    // Tests: setNums() | Happy: replacing the array updates all subsequent calculations —
    // verified via min() which returns the new minimum after setNums() is called
    void setNums_replacesArray_minReflectsNewValues() {
        Stats stats = new Stats(10.0, 20.0, 30.0);
        stats.setNums(1.0, 2.0, 3.0);
        assertEquals(1.0, stats.min());
    }

    @Test
    // Tests: setNums() | Edge: setting an empty array causes all subsequent guard clauses
    // to trigger, returning 0 from min(), max(), mean(), and median()
    void setNums_emptyArray_subsequentCallsReturnZero() {
        Stats stats = new Stats(10.0, 20.0, 30.0);
        stats.setNums();
        assertEquals(0.0, stats.min());
        assertEquals(0.0, stats.max());
        assertEquals(0.0, stats.mean());
        assertEquals(0.0, stats.median());
    }

    // =========================================================================
    // createStats()
    // =========================================================================

    @Test
    // Tests: createStats() | Happy: returns a non-null Stats instance wrapping the
    // given array — verified by calling min() on the returned instance
    void createStats_returnsNewStatsInstanceWrappingArray() {
        Stats stats = new Stats(1.0, 2.0, 3.0);
        Stats result = stats.createStats(new double[]{4.0, 5.0, 6.0});
        assertNotNull(result);
        assertEquals(4.0, result.min());
    }

    @Test
    // Tests: createStats() | Edge: wrapping an empty array produces a Stats instance
    // that correctly returns 0 from min() via its own guard clause
    void createStats_emptyArray_returnsStatsWithZeroMin() {
        Stats stats = new Stats(1.0, 2.0);
        Stats result = stats.createStats(new double[]{});
        assertNotNull(result);
        assertEquals(0.0, result.min());
    }

    // =========================================================================
    // Helper
    // =========================================================================

    /** Returns true if the array contains the target value. */
    private boolean contains(double[] arr, double target) {
        for (double v : arr) {
            if (Double.compare(v, target) == 0) return true;
        }
        return false;
    }
}