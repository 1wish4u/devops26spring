package model;


/**
 * Class to create a statistics report (minimum, median, mean, etc.) about an
 * array of doubles.
 * 
 * Is this comment sufficient?
 * 
 */
public class Reporter {
	private double[] nums;
	private int logDecimalPlaces = 4;

	public Reporter() {
		nums = new double[0];
	}
	

	/**
	 * Creates a formatted statistical report of the object's doubles in string
	 * form, ready to print to console.
	 * 
	 * @return - Formatted report.
	 */
	public String reportStatistics() {
		Stats stats = new Stats(nums);

		// Build the report with a StringBuilder
		StringBuilder outputBuilder = new StringBuilder();

		// Array of StatisticPairs, which contain a label and a statistic
		StatisticPair[] pairs = {
				new StatisticPair("Minimum", stats.min()),
				new StatisticPair("Maximum", stats.max()),
				new StatisticPair("Mean", stats.mean()),
				new StatisticPair("Median", stats.median()),
				new StatisticPair("Q1", stats.firstQuartile()),
				new StatisticPair("Q3", stats.thirdQuartile()),
				new StatisticPair("IQR", stats.interquartileRange()) };

		// Append the input values first, with a length limit of 80
		outputBuilder.append("Values: \n" + getNumberArrayString(nums, 80) + "\n\n");

		// Append a formatted string of all stats
		outputBuilder.append(formattedStatValuePairs(pairs, logDecimalPlaces) + "\n");

		// Outliers
		outputBuilder.append(outliers(stats.outliers()) + "\n");

		// Separator line
		outputBuilder.append("\n" + separatorLine() + "\n");

		return outputBuilder.toString();
	}

	/**
	 * Class to create objects which contain a label for a statistic, such as the
	 * median, and the statistic's value.
	 */
	private static class StatisticPair {
		private String label;
		private double value;

		/**
		 * @param label - The statistic's label (ex. "Median")
		 * @param value - The statistic's value (ex. 3.1)
		 */
		public StatisticPair(String label, double value) {
			this.label = label;
			this.value = value;
		}

		public String getLabel() {
			return label;
		}

		public double getValue() {
			return value;
		}
	}

	/**
	 * Returns a string of a formatted set of statistics and their labels.
	 * 
	 * @param pairs              - Pairs of labels and values for the stats.
	 * @param valueDecimalPlaces - The amount of decimal places to display.
	 * @return - The resulting formatted string.
	 */
	private String formattedStatValuePairs(StatisticPair[] pairs, int valueDecimalPlaces) {
		// Create two arrays of labels and their values
		String[] statLabels = new String[pairs.length];
		double[] values = new double[pairs.length];

		for (int i = 0; i < pairs.length; i++) {
			statLabels[i] = pairs[i].getLabel();
			values[i] = pairs[i].getValue();
		}

		// Find longest label to align all labels to the right
		int labelWidth = 0;
		for (String label : statLabels) {
			if (label.length() > labelWidth) {
				labelWidth = label.length();
			}
		}

		// Find widest number before the decimal point to align numbers to the decimal
		int intWidth = 0;
		for (double num : values) {
			double integer = Math.floor(num);
			int width = String.valueOf(integer).length() - 2;
			if (width > intWidth) {
				intWidth = width;
			}
		}

		// Build the output
		StringBuilder outputBuilder = new StringBuilder();
		for (int i = 0; i < statLabels.length; i++) {
			String label = statLabels[i];
			double num = values[i];

			outputBuilder.append(
					String.format("%" + labelWidth + "s: %" + (intWidth + valueDecimalPlaces + 1)
							+ "." + valueDecimalPlaces + "f%n", label, num));
		}

		return outputBuilder.toString();
	}

	/**
	 * Returns a formatted string of an array of outliers, or a message if there are
	 * no outliers in the array.
	 * 
	 * @param outliers - Input outliers array.
	 * @return - Formatted string, or message saying there are no outliers.
	 */
	private String outliers(double[] outliers) {
		if (outliers.length > 0) {
			return "Outliers: " + getNumberArrayString(outliers, 72);
		} else {
			return "No outliers.";
		}
	}

	/**
	 * Get a string of the doubles inside an array, each seperated by a comma and
	 * space. Use a length limit to restrict the length of the string. Values that
	 * exceed the length will use the notation "and n more".
	 * 
	 * @param numberArray - Input array
	 * @param lengthLimit - The maximum allowed length of the string.
	 * @return - String of the array's doubles
	 */
	private String getNumberArrayString(double[] numberArray, int lengthLimit) {
		// Build output string with a StringBuilder
		StringBuilder outputBuilder = new StringBuilder();

		for (int i = 0; i < numberArray.length; i++) {
			// Separate each value with a comma, but not if this is the first value
			String nextSeparator = i > 0 ? ", " : "";

			// String of the next value, plus the separator, if present
			String nextString = nextSeparator + numberArray[i];

			// Logic below calculates whether the string needs to be truncaated because
			// there are too many value to fit within the length limit.

			// Amount of values remaining, if the string does need to be truncated
			int amountRemaining = numberArray.length - i;
			String amountRemainingString = ", and " + amountRemaining + " more";

			// If true, the string can't be truncated if more values are added. Consider if
			// remaining values can be added without truncating, otherwise truncate and
			// summarize.
			if (outputBuilder.length() + nextString.length()
					+ amountRemainingString.length() > lengthLimit) {

				// Create a temporary StringBuilder to evaluate the length of the rest of the
				// values
				StringBuilder remainingValuesStringBuilder = new StringBuilder();
				for (int j = i; j < numberArray.length; j++) {
					remainingValuesStringBuilder.append(", " + numberArray[j]);
				}

				if (outputBuilder.length() + remainingValuesStringBuilder.length() > lengthLimit) {
					// String needs to be truncated.
					return outputBuilder.toString() + amountRemainingString;
				} else {
					// Truncation is not needed.
					return outputBuilder.toString() + remainingValuesStringBuilder.toString();
				}
			}

			// Append the next value.
			outputBuilder.append(nextString);
		}
		return outputBuilder.toString();
	}

	/**
	 * Returns a separator line for the console.
	 * 
	 * @return - Separator line string.
	 */
	public static String separatorLine() {
		return "--------------------------------------------------------------------------------";
	}
	
	public void setNums(double... nums) {
		this.nums = nums;
	}
}
