

import model.NumPrompter;
import model.Reporter;
import org.junit.jupiter.api.Test;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.argThat;

/**
 * Isolated unit tests for {@link AppController} using JUnit 5 and Mockito.
 *
 * <p>Isolation strategy:
 * <ul>
 *   <li>{@code processInput()} is tested directly since it is package-private,
 *       using a Mockito spy on {@link AppController} to stub
 *       {@code createReporter()} and inject a mock {@link Reporter}.</li>
 *   <li>{@code run()} is tested by injecting a mock {@link NumPrompter} that
 *       returns controlled arrays and a mock {@link PrintStream} to verify
 *       output, isolating the loop logic from real I/O.</li>
 * </ul>
 *
 * <p>Dependencies:
 * <ul>
 *   <li>org.junit.jupiter:junit-jupiter:5.10+</li>
 *   <li>org.mockito:mockito-core:5.x</li>
 * </ul>
 */
public class AppControllerTest {

    // =========================================================================
    // processInput() — happy paths
    // =========================================================================

    @Test
    // processInput() | Happy: valid multi-element array → returned string contains
    // all expected section headers from Reporter.reportStatistics().
    void processInput_validArray_containsAllReportSections() {
        NumPrompter mockPrompter = mock(NumPrompter.class);
        PrintStream mockOut = mock(PrintStream.class);
        AppController controller = new AppController(mockPrompter, mockOut);

        String result = controller.processInput(new double[]{1.0, 2.0, 3.0});

        assertTrue(result.contains("Values:"),   "Report must contain Values section");
        assertTrue(result.contains("Minimum"),   "Report must contain Minimum label");
        assertTrue(result.contains("Maximum"),   "Report must contain Maximum label");
        assertTrue(result.contains("Mean"),      "Report must contain Mean label");
        assertTrue(result.contains("Median"),    "Report must contain Median label");
        assertTrue(result.contains("Q1"),        "Report must contain Q1 label");
        assertTrue(result.contains("Q3"),        "Report must contain Q3 label");
        assertTrue(result.contains("IQR"),       "Report must contain IQR label");
        assertTrue(result.contains(Reporter.separatorLine()),
                "Report must contain separator line");
    }

    @Test
    // processInput() | Happy: single-element array → report is still produced
    // without error (edge case for Stats methods that guard on length).
    void processInput_singleElement_returnsReport() {
        NumPrompter mockPrompter = mock(NumPrompter.class);
        PrintStream mockOut = mock(PrintStream.class);
        AppController controller = new AppController(mockPrompter, mockOut);

        String result = controller.processInput(new double[]{42.0});

        assertNotNull(result, "Report must not be null for a single-element array");
        assertFalse(result.isBlank(), "Report must not be blank for a single-element array");
    }

    @Test
    // processInput() | Happy: returned report string contains the input values.
    void processInput_validArray_reportContainsInputValues() {
        NumPrompter mockPrompter = mock(NumPrompter.class);
        PrintStream mockOut = mock(PrintStream.class);
        AppController controller = new AppController(mockPrompter, mockOut);

        String result = controller.processInput(new double[]{7.0, 8.0, 9.0});

        assertTrue(result.contains("7.0"), "Report must contain first input value");
        assertTrue(result.contains("8.0"), "Report must contain second input value");
        assertTrue(result.contains("9.0"), "Report must contain third input value");
    }

    // =========================================================================
    // processInput() — isolation via spy + stubbed createReporter()
    // =========================================================================

    @Test
    // processInput() | Isolated: spy stubs createReporter() to inject a mock
    // Reporter; verifies setNums() and reportStatistics() are each called once,
    // confirming delegation without depending on Reporter's real implementation.
    void processInput_isolated_delegatesToReporter() {
        NumPrompter mockPrompter = mock(NumPrompter.class);
        PrintStream mockOut = mock(PrintStream.class);
        AppController spy = spy(new AppController(mockPrompter, mockOut));

        Reporter mockReporter = mock(Reporter.class);
        when(mockReporter.reportStatistics()).thenReturn("mock report");
        doReturn(mockReporter).when(spy).createReporter();

        double[] input = {1.0, 2.0, 3.0};
        String result = spy.processInput(input);

        verify(mockReporter, times(1)).setNums(1.0, 2.0, 3.0);
        verify(mockReporter, times(1)).reportStatistics();
        assertEquals("mock report", result,
                "processInput must return exactly what reportStatistics() returns");
    }

    // =========================================================================
    // run() — quit behaviour
    // =========================================================================

    @Test
    // run() | Edge: prompter returns empty array on first call → loop exits
    // immediately; out.println() is never called with a report.
    void run_emptyArrayOnFirstCall_quitsImmediately() {
        NumPrompter mockPrompter = mock(NumPrompter.class);
        PrintStream mockOut = mock(PrintStream.class);
        when(mockPrompter.getReals(anyString())).thenReturn(new double[]{});

        AppController controller = new AppController(mockPrompter, mockOut);
        controller.run();

        verify(mockPrompter, times(1)).getReals(anyString());
        verify(mockOut, never()).println(anyString());
    }

    @Test
    // run() | Happy: prompter returns valid data once then empty array → loop
    // runs exactly once; out.println() called once with the report.
    void run_oneValidInputThenQuit_printsReportOnce() {
        NumPrompter mockPrompter = mock(NumPrompter.class);
        PrintStream mockOut = mock(PrintStream.class);
        when(mockPrompter.getReals(anyString()))
                .thenReturn(new double[]{1.0, 2.0, 3.0})
                .thenReturn(new double[]{});

        AppController controller = new AppController(mockPrompter, mockOut);
        controller.run();

        verify(mockPrompter, times(2)).getReals(anyString());
        verify(mockOut, times(1)).println(anyString());
    }

    @Test
    // run() | Happy: prompter returns valid data three times then empty array →
    // loop runs exactly three times; out.println() called three times.
    void run_threeValidInputsThenQuit_printsReportThreeTimes() {
        NumPrompter mockPrompter = mock(NumPrompter.class);
        PrintStream mockOut = mock(PrintStream.class);
        when(mockPrompter.getReals(anyString()))
                .thenReturn(new double[]{1.0, 2.0})
                .thenReturn(new double[]{3.0, 4.0})
                .thenReturn(new double[]{5.0, 6.0})
                .thenReturn(new double[]{});

        AppController controller = new AppController(mockPrompter, mockOut);
        controller.run();

        verify(mockPrompter, times(4)).getReals(anyString());
        verify(mockOut, times(3)).println(anyString());
    }

    // =========================================================================
    // run() — output content
    // =========================================================================

    @Test
    // run() | Happy: the string passed to out.println() contains the separator
    // line, confirming the full report (not a partial string) is printed.
    void run_oneValidInput_printedStringContainsSeparatorLine() {
        NumPrompter mockPrompter = mock(NumPrompter.class);
        PrintStream mockOut = mock(PrintStream.class);
        when(mockPrompter.getReals(anyString()))
                .thenReturn(new double[]{10.0, 20.0, 30.0})
                .thenReturn(new double[]{});

        AppController controller = new AppController(mockPrompter, mockOut);
        controller.run();

        // Explicitly cast the lambda parameter to String to resolve overload ambiguity
        verify(mockOut).println(argThat((String s) -> s.contains(Reporter.separatorLine())));
    }

    // =========================================================================
    // createReporter() — real implementation
    // =========================================================================

    @Test
    // createReporter() | Happy: real body returns a non-null Reporter instance.
    // All spy tests stub this method, so its actual `return new Reporter()` line
    // is never executed in those tests — this test covers it directly.
    void createReporter_realImplementation_returnsReporterInstance() {
        NumPrompter mockPrompter = mock(NumPrompter.class);
        AppController controller = new AppController(mockPrompter, mock(PrintStream.class));
        assertNotNull(controller.createReporter(),
                "createReporter() must return a non-null Reporter instance");
    }

    // =========================================================================
    // run() — resource cleanup
    // =========================================================================

    @Test
    // run() | Edge: when the loop exits (empty input), closeScanner() must be called.
    void run_closesScannerAfterExit() {
        NumPrompter mockPrompter = mock(NumPrompter.class);
        when(mockPrompter.getReals(anyString())).thenReturn(new double[]{});
        AppController controller = new AppController(mockPrompter, mock(PrintStream.class));
        controller.run();
        verify(mockPrompter, times(1)).closeScanner();
    }
}