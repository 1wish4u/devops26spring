import model.NumPrompter;
import model.Reporter;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the main application loop for the statistics calculator.
 *
 * <p>Accepts a {@link NumPrompter} for user input and a {@link PrintStream}
 * for output as constructor dependencies, making the class fully testable
 * without relying on {@code System.in} or {@code System.out} directly.
 *
 * <p>Typical usage via {@link Main}:
 * <pre>
 *     AppController app = new AppController(new NumPrompter(), System.out);
 *     app.run();
 * </pre>
 */
public class AppController {

    private final NumPrompter prompter;
    private final PrintStream out;

    private static final Logger logger = LoggerFactory.getLogger(AppController.class);

    /**
     * Constructs an AppController with the given input prompter and output stream.
     *
     * @param prompter - The {@link NumPrompter} used to collect numbers from the user.
     * @param out      - The {@link PrintStream} used to display reports.
     */
    public AppController(NumPrompter prompter, PrintStream out) {
        this.prompter = prompter;
        this.out = out;
        logger.info("AppController created");
    }

    /**
     * Starts the main application loop.
     *
     * <p>Repeatedly prompts the user for numbers, generates a statistics report,
     * and prints it to the output stream. Exits when the user provides no input
     * (i.e., {@link NumPrompter#getReals(String)} returns an empty array).
     */
    public void run() {
        logger.info("Application loop started");
        boolean quit = false;

        while (!quit) {
            double[] reals = prompter.getReals("""
                    To calculate stats, enter some non-negative numbers, separated by commas,
                    spaces, or any other non-numeric character, or press enter with no data to
                    quit...""");

            if (reals.length == 0) {
                logger.info("No input received; exiting application loop");
                quit = true;
                continue;
            }

            out.println("\n" + processInput(reals));
        }

        prompter.closeScanner();
        System.out.println("Program ended.");
        logger.info("Application ended");
    }

    /**
     * Generates a formatted statistics report for the given array of numbers.
     *
     * <p>This method is package-private to allow direct testing without
     * routing through the full {@link #run()} loop.
     *
     * @param reals - The array of doubles to report on.
     * @return A formatted statistics report string from {@link Reporter#reportStatistics()}.
     */
    String processInput(double[] reals) {
        logger.debug("Processing input of {} value(s)", reals.length);
        Reporter reporter = createReporter();
        reporter.setNums(reals);
        return reporter.reportStatistics();
    }

    /**
     * Creates and returns a new {@link Reporter} instance.
     *
     * <p>Extracted as a factory method so it can be stubbed in tests via a
     * __Mockito__ spy, decoupling {@link #processInput(double[])} from the real
     * {@link Reporter} implementation.
     *
     * @return A new {@link Reporter}.
     */
    Reporter createReporter() {
        return new Reporter();
    }
}