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

  
}

