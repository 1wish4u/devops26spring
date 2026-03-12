package model;

import model.Stats;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * True isolated unit tests for {@link Stats}.
 *
 * =====================================================================
 *  BUGS IN THE CURRENT Stats.java — these tests WILL FAIL until fixed
 * =====================================================================
 *
 *  BUG-1  max()       Loop is `i < nums.length-1` → last element is
 *                     never compared.
 *
 *  BUG-2  mean()      `sum / nums.length-1` → Java operator precedence
 *                     makes this (sum/length)-1, always 1 too low.
 *
 *  BUG-3a median()    Array is never sorted before indexing.
 *
 *  BUG-3b median()    Even-length arrays must average two middle values;
 *                     current code just returns one of them.
 *
 *  BUG-4  outliers()  Upper-fence condition uses `<` instead of `>`:
 *                     `value < q3 + 1.5*iqr` flags almost every value
 *                     as an outlier and misses true high outliers.
 *
 * Tests that expose a bug are marked:  // *** FAILS until BUG-N fixed ***
 * All other tests pass against the current (buggy) code.
 */
class StatsTest {

    // =================================================================
    //  min()
    // =================================================================
    @Nested
    @DisplayName("min()")
    class MinTests {

        @Test
        @DisplayName("empty array returns 0")
        void emptyArray_returnsZero() {
            assertEquals(0.0, new Stats().min());
        }

        @Test
        @DisplayName("single element returns that element")
        void singleElement_returnsThatElement() {
            assertEquals(7.0, new Stats(7.0).min());
        }

        @Test
        @DisplayName("minimum is the first element")
        void minimumFirst() {
            assertEquals(1.0, new Stats(1.0, 3.0, 5.0, 7.0).min());
        }

        @Test
        @DisplayName("minimum is the last element")
        void minimumLast() {
            assertEquals(1.0, new Stats(9.0, 6.0, 3.0, 1.0).min());
        }

        @Test
        @DisplayName("minimum is a middle element")
        void minimumMiddle() {
            assertEquals(2.0, new Stats(5.0, 2.0, 8.0, 4.0).min());
        }

        @Test
        @DisplayName("all equal values returns that value")
        void allEqual() {
            assertEquals(4.0, new Stats(4.0, 4.0, 4.0).min());
        }

        @Test
        @DisplayName("array containing zero returns zero")
        void containsZero() {
            assertEquals(0.0, new Stats(5.0, 0.0, 3.0).min());
        }

        @Test
        @DisplayName("two elements returns the smaller")
        void twoElements() {
            assertEquals(3.0, new Stats(3.0, 9.0).min());
        }
    }

    // =================================================================
    //  max()   BUG-1: loop bound i < nums.length-1 skips last element
    // =================================================================
    @Nested
    @DisplayName("max()")
    class MaxTests {

        @Test
        @DisplayName("empty array returns 0")
        void emptyArray_returnsZero() {
            assertEquals(0.0, new Stats().max());
        }

        @Test
        @DisplayName("single element returns that element")
        void singleElement() {
            // single-element: loop never runs, maximum = nums[0] — passes now
            assertEquals(5.0, new Stats(5.0).max());
        }

        @Test
        @DisplayName("maximum is the first element — passes now")
        void maximumFirst() {
            assertEquals(9.0, new Stats(9.0, 3.0, 1.0).max());
        }

        @Test
        @DisplayName("maximum is a middle element — passes now")
        void maximumMiddle() {
            assertEquals(8.0, new Stats(2.0, 8.0, 5.0, 3.0).max());
        }

        @Test
        @DisplayName("maximum is the last element")
        void maximumLast() {
            // *** FAILS until BUG-1 fixed ***
            // loop runs while i < 2 (length-1), never checks index 2 (value 10)
            assertEquals(10.0, new Stats(1.0, 2.0, 10.0).max());
        }

        @Test
        @DisplayName("two elements — larger value is last")
        void twoElements_largerLast() {
            // *** FAILS until BUG-1 fixed ***
            // loop: i starts at 1, runs while i < 1 → never executes; stays at 3
            assertEquals(7.0, new Stats(3.0, 7.0).max());
        }

        @Test
        @DisplayName("two elements — larger value is first")
        void twoElements_largerFirst() {
            assertEquals(7.0, new Stats(7.0, 3.0).max());
        }

        @Test
        @DisplayName("all equal values returns that value")
        void allEqual() {
            assertEquals(6.0, new Stats(6.0, 6.0, 6.0).max());
        }
    }

    // =================================================================
    //  mean()   BUG-2: `sum / nums.length-1` is always 1 too low
    // =================================================================
    @Nested
    @DisplayName("mean()")
    class MeanTests {

        @Test
        @DisplayName("empty array returns 0")
        void emptyArray_returnsZero() {
            assertEquals(0.0, new Stats().mean());
        }

        @Test
        @DisplayName("single element returns itself")
        void singleElement() {
            // *** FAILS until BUG-2 fixed ***
            // sum(5)/1 - 1 = 4, not 5
            assertEquals(5.0, new Stats(5.0).mean());
        }

        @Test
        @DisplayName("two identical elements returns that value")
        void twoIdentical() {
            // *** FAILS until BUG-2 fixed ***
            // sum(12)/2 - 1 = 5, not 6
            assertEquals(6.0, new Stats(6.0, 6.0).mean());
        }

        @Test
        @DisplayName("three elements with known mean")
        void threeElements() {
            // *** FAILS until BUG-2 fixed ***
            // sum(12)/3 - 1 = 3, not 4
            assertEquals(4.0, new Stats(2.0, 4.0, 6.0).mean());
        }

        @Test
        @DisplayName("mean of all zeros is 0")
        void allZeros() {
            // *** FAILS until BUG-2 fixed ***
            // 0/3 - 1 = -1, not 0
            assertEquals(0.0, new Stats(0.0, 0.0, 0.0).mean());
        }

        @Test
        @DisplayName("five elements with whole-number mean")
        void fiveElements() {
            // *** FAILS until BUG-2 fixed ***
            // sum(15)/5 - 1 = 2, not 3
            assertEquals(3.0, new Stats(1.0, 2.0, 3.0, 4.0, 5.0).mean());
        }

        @Test
        @DisplayName("non-integer mean is accurate")
        void nonIntegerMean() {
            // *** FAILS until BUG-2 fixed ***
            // sum(3)/2 - 1 = 0.5, not 1.5
            assertEquals(1.5, new Stats(1.0, 2.0).mean(), 1e-9);
        }
    }

    // =================================================================
    //  median()
    //  BUG-3a: array never sorted before indexing
    //  BUG-3b: even-length arrays need to average two middle values
    // =================================================================
    @Nested
    @DisplayName("median()")
    class MedianTests {

        @Test
        @DisplayName("empty array returns 0")
        void emptyArray_returnsZero() {
            assertEquals(0.0, new Stats().median());
        }

        @Test
        @DisplayName("single element returns that element")
        void singleElement() {
            assertEquals(9.0, new Stats(9.0).median());
        }

        @Test
        @DisplayName("odd count already sorted returns middle")
        void oddSorted() {
            // passes now (lucky — already in sorted order)
            assertEquals(3.0, new Stats(1.0, 3.0, 5.0).median());
        }

        @Test
        @DisplayName("odd count unsorted returns sorted middle")
        void oddUnsorted() {
            // *** FAILS until BUG-3a fixed ***
            // [5,1,3]: nums[1] = 1, but sorted median = 3
            assertEquals(3.0, new Stats(5.0, 1.0, 3.0).median());
        }

        @Test
        @DisplayName("odd count five elements unsorted")
        void oddFiveUnsorted() {
            // *** FAILS until BUG-3a fixed ***
            // [9,1,7,3,5]: nums[2] = 7, sorted median = 5
            assertEquals(5.0, new Stats(9.0, 1.0, 7.0, 3.0, 5.0).median());
        }

        @Test
        @DisplayName("even count sorted averages two middle values")
        void evenSorted() {
            // *** FAILS until BUG-3b fixed ***
            // [1,2,3,4]: nums[2] = 3, correct = (2+3)/2 = 2.5
            assertEquals(2.5, new Stats(1.0, 2.0, 3.0, 4.0).median());
        }

        @Test
        @DisplayName("even count two elements averages them")
        void evenTwoElements() {
            // *** FAILS until BUG-3b fixed ***
            // [4,6]: nums[1] = 6, correct = (4+6)/2 = 5.0
            assertEquals(5.0, new Stats(4.0, 6.0).median());
        }

        @Test
        @DisplayName("even count unsorted sorts then averages")
        void evenUnsorted() {
            // *** FAILS until BUG-3a and BUG-3b fixed ***
            // [6,4]: nums[1] = 4, sorted = [4,6], correct = 5.0
            assertEquals(5.0, new Stats(6.0, 4.0).median());
        }
    }

    // =================================================================
    //  firstQuartile()
    // =================================================================
    @Nested
    @DisplayName("firstQuartile()")
    class FirstQuartileTests {

        @Test
        @DisplayName("empty array returns 0")
        void emptyArray_returnsZero() {
            assertEquals(0.0, new Stats().firstQuartile());
        }

        @Test
        @DisplayName("single element returns 0 (too few values)")
        void singleElement_returnsZero() {
            assertEquals(0.0, new Stats(5.0).firstQuartile());
        }

        @Test
        @DisplayName("two elements — lower half has one element")
        void twoElements() {
            // sorted [3,7]; lower=[3]; median(3)=3 — passes now
            assertEquals(3.0, new Stats(3.0, 7.0).firstQuartile());
        }

        @Test
        @DisplayName("even-length array Q1 = median of lower half")
        void evenLength() {
            // *** FAILS until BUG-3b fixed ***
            // sorted [2,4,6,8]; lower=[2,4]; correct median=3.0
            assertEquals(3.0, new Stats(2.0, 4.0, 6.0, 8.0).firstQuartile());
        }

        @Test
        @DisplayName("odd-length array middle excluded from lower half")
        void oddLength() {
            // *** FAILS until BUG-3b fixed ***
            // sorted [1,2,3,4,5]; lower=[1,2]; correct median=1.5
            assertEquals(1.5, new Stats(1.0, 2.0, 3.0, 4.0, 5.0).firstQuartile());
        }

        @Test
        @DisplayName("unsorted input sorted internally before Q1")
        void unsortedInput() {
            // *** FAILS until BUG-3b fixed ***
            // [5,1,3,7] → sorted [1,3,5,7]; lower=[1,3]; Q1=2.0
            assertEquals(2.0, new Stats(5.0, 1.0, 3.0, 7.0).firstQuartile());
        }
    }

    // =================================================================
    //  thirdQuartile()
    // =================================================================
    @Nested
    @DisplayName("thirdQuartile()")
    class ThirdQuartileTests {

        @Test
        @DisplayName("empty array returns 0")
        void emptyArray_returnsZero() {
            assertEquals(0.0, new Stats().thirdQuartile());
        }

        @Test
        @DisplayName("single element returns 0 (too few values)")
        void singleElement_returnsZero() {
            assertEquals(0.0, new Stats(5.0).thirdQuartile());
        }

        @Test
        @DisplayName("two elements — upper half has one element")
        void twoElements() {
            // sorted [3,7]; upper=[7]; median(7)=7 — passes now
            assertEquals(7.0, new Stats(3.0, 7.0).thirdQuartile());
        }

        @Test
        @DisplayName("even-length array Q3 = median of upper half")
        void evenLength() {
            // *** FAILS until BUG-3b fixed ***
            // sorted [2,4,6,8]; upper=[6,8]; correct median=7.0
            assertEquals(7.0, new Stats(2.0, 4.0, 6.0, 8.0).thirdQuartile());
        }

        @Test
        @DisplayName("odd-length array middle excluded from upper half")
        void oddLength() {
            // *** FAILS until BUG-3b fixed ***
            // sorted [1,2,3,4,5]; upper=[4,5]; correct median=4.5
            assertEquals(4.5, new Stats(1.0, 2.0, 3.0, 4.0, 5.0).thirdQuartile());
        }

        @Test
        @DisplayName("unsorted input sorted internally before Q3")
        void unsortedOddLength() {
            // *** FAILS until BUG-3b fixed ***
            // [7,1,5,3,9] → sorted [1,3,5,7,9]; upper=[7,9]; Q3=8.0
            assertEquals(8.0, new Stats(7.0, 1.0, 5.0, 3.0, 9.0).thirdQuartile());
        }
    }

    // =================================================================
    //  interquartileRange()
    //  Mockito spy isolates this method from firstQuartile()/thirdQuartile()
    // =================================================================
    @Nested
    @DisplayName("interquartileRange()  [Mockito spy]")
    class IQRTests {

        @Test
        @DisplayName("IQR = Q3 - Q1 with stubbed values 2 and 8 returns 6")
        void stubbedQ1AndQ3() {
            Stats stats = spy(new Stats(1.0, 2.0, 3.0, 4.0, 5.0));
            doReturn(2.0).when(stats).firstQuartile();
            doReturn(8.0).when(stats).thirdQuartile();

            assertEquals(6.0, stats.interquartileRange());
            verify(stats, times(1)).firstQuartile();
            verify(stats, times(1)).thirdQuartile();
        }

        @Test
        @DisplayName("IQR = 0 when Q1 equals Q3")
        void q1EqualsQ3_returnsZero() {
            Stats stats = spy(new Stats(5.0, 5.0, 5.0));
            doReturn(5.0).when(stats).firstQuartile();
            doReturn(5.0).when(stats).thirdQuartile();

            assertEquals(0.0, stats.interquartileRange());
        }

        @Test
        @DisplayName("IQR with decimal quartiles is accurate")
        void decimalQuartiles() {
            Stats stats = spy(new Stats(1.0, 2.0, 3.0));
            doReturn(1.5).when(stats).firstQuartile();
            doReturn(4.25).when(stats).thirdQuartile();

            assertEquals(2.75, stats.interquartileRange(), 1e-9);
        }

        @Test
        @DisplayName("IQR only calls firstQuartile() and thirdQuartile() — no other methods")
        void onlyDelegatesToQuartileMethods() {
            Stats stats = spy(new Stats(10.0, 20.0, 30.0, 40.0));
            doReturn(12.5).when(stats).firstQuartile();
            doReturn(37.5).when(stats).thirdQuartile();

            stats.interquartileRange();

            // You must verify the method you actually called on the spy!
            verify(stats).interquartileRange(); 
            verify(stats).firstQuartile();
            verify(stats).thirdQuartile();
            
            verifyNoMoreInteractions(stats);
        }
    }

    // =================================================================
    //  outliers()   BUG-4: upper-fence uses < instead of >
    //  Mockito spy isolates from Q1 / Q3 / IQR collaborators
    // =================================================================
    @Nested
    @DisplayName("outliers()  [Mockito spy]")
    class OutliersTests {

        /** Helper: returns a spy with all fence collaborators stubbed. */
        private Stats spyWithFences(double q1, double q3, double iqr, double... data) {
            Stats stats = spy(new Stats(data));
            doReturn(q1).when(stats).firstQuartile();
            doReturn(q3).when(stats).thirdQuartile();
            doReturn(iqr).when(stats).interquartileRange();
            return stats;
        }

        @Test
        @DisplayName("empty array returns empty")
        void emptyArray_returnsEmpty() {
            assertArrayEquals(new double[]{}, new Stats().outliers());
        }

        @Test
        @DisplayName("single element returns empty (too few values)")
        void singleElement_returnsEmpty() {
            assertArrayEquals(new double[]{}, new Stats(5.0).outliers());
        }

        @Test
        @DisplayName("no values outside fences returns empty")
        void noOutliers_returnsEmpty() {
            // *** FAILS until BUG-4 fixed ***
            // fences: lower=-1, upper=7; all values [1..5] within bounds
            // BUG-4: `value < q3+1.5*iqr` = `value < 7` is TRUE for every value
            // so all 5 values are wrongly flagged as outliers
            Stats stats = spyWithFences(2.0, 4.0, 2.0, 1.0, 2.0, 3.0, 4.0, 5.0);
            assertArrayEquals(new double[]{}, stats.outliers());
        }

        @Test
        @DisplayName("value below lower fence is detected as outlier")
        void belowLowerFence_detected() {
            // lower fence = 2 - 1.5*2 = -1; -10 < -1 → outlier (lower check is correct)
            Stats stats = spyWithFences(2.0, 4.0, 2.0, -10.0, 2.0, 3.0, 4.0, 5.0);
            double[] result = stats.outliers();
            assertEquals(1, result.length);
            assertEquals(-10.0, result[0]);
        }

        @Test
        @DisplayName("value above upper fence is detected as outlier")
        void aboveUpperFence_detected() {
            // *** FAILS until BUG-4 fixed ***
            // upper fence = 4 + 1.5*2 = 7; 100 > 7 → should be an outlier
            // BUG-4: `value < 7` is FALSE for 100, so 100 is completely missed
            Stats stats = spyWithFences(2.0, 4.0, 2.0, 1.0, 2.0, 3.0, 4.0, 100.0);
            assertTrue(
                java.util.Arrays.stream(stats.outliers()).anyMatch(v -> v == 100.0),
                "Expected 100.0 to be flagged as a high outlier"
            );
        }

        @Test
        @DisplayName("outliers on both sides are both detected")
        void bothSides_bothDetected() {
            // *** FAILS until BUG-4 fixed ***
            // -5 is below lower fence (-1); 50 is above upper fence (7)
            Stats stats = spyWithFences(2.0, 4.0, 2.0, -5.0, 2.0, 3.0, 4.0, 50.0);
            double[] result = stats.outliers();
            assertTrue(java.util.Arrays.stream(result).anyMatch(v -> v == -5.0),
                "Expected -5.0 as low outlier");
            assertTrue(java.util.Arrays.stream(result).anyMatch(v -> v == 50.0),
                "Expected 50.0 as high outlier");
        }

        @Test
        @DisplayName("value exactly on lower fence is NOT an outlier")
        void exactlyOnLowerFence_notOutlier() {
            // lower fence = -1; value=-1 is on the boundary, not outside it
            Stats stats = spyWithFences(2.0, 4.0, 2.0, -1.0, 2.0, 3.0, 4.0, 5.0);
            assertFalse(
                java.util.Arrays.stream(stats.outliers()).anyMatch(v -> v == -1.0),
                "Boundary value should NOT be an outlier"
            );
        }

        @Test
        @DisplayName("value exactly on upper fence is NOT an outlier")
        void exactlyOnUpperFence_notOutlier() {
            // *** FAILS until BUG-4 fixed ***
            // upper fence = 7; value=7 is on boundary, should not be flagged
            Stats stats = spyWithFences(2.0, 4.0, 2.0, 1.0, 2.0, 3.0, 4.0, 7.0);
            assertFalse(
                java.util.Arrays.stream(stats.outliers()).anyMatch(v -> v == 7.0),
                "Boundary value should NOT be an outlier"
            );
        }

        @Test
        @DisplayName("IQR=0 all identical values produces no outliers")
        void iQRzero_noOutliers() {
            // *** FAILS until BUG-4 fixed ***
            Stats stats = spyWithFences(5.0, 5.0, 0.0, 5.0, 5.0, 5.0);
            assertArrayEquals(new double[]{}, stats.outliers());
        }

        @Test
        @DisplayName("collaborator methods called once per element in the loop")
        void delegationCallCountMatchesElementCount() {
            Stats stats = spyWithFences(2.0, 4.0, 2.0, 1.0, 2.0, 3.0);
            stats.outliers();
            // 3 elements → each collaborator called exactly 3 times
            verify(stats, times(3)).interquartileRange();
            verify(stats, times(3)).firstQuartile();
            verify(stats, times(3)).thirdQuartile();
        }
    }

    // =================================================================
    //  setNums()
    // =================================================================
    @Nested
    @DisplayName("setNums()")
    class SetNumsTests {

        @Test
        @DisplayName("setNums replaces data used by min()")
        void replacesDataForMin() {
            Stats stats = new Stats(10.0, 20.0, 30.0);
            stats.setNums(1.0, 2.0, 3.0);
            assertEquals(1.0, stats.min());
        }

        @Test
        @DisplayName("setNums replaces data used by max()")
        void replacesDataForMax() {
            Stats stats = new Stats(1.0, 2.0, 3.0);
            stats.setNums(10.0, 20.0, 30.0);
            assertEquals(30.0, stats.max());
        }

        @Test
        @DisplayName("setNums to empty array causes min() to return 0")
        void setToEmpty_minReturnsZero() {
            Stats stats = new Stats(5.0);
            stats.setNums();
            assertEquals(0.0, stats.min());
        }
    }
}