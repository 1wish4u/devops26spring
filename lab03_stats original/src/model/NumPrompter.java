package model;


import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class prompts the user for non-negative integers or doubles, and creates
 * an array with those values. Filters out and is compatible with any separator
 * character when prompting. Uses a scanner that must be closed with the
 * closeScanner method. Can be used repeatedly.
 */
public class NumPrompter {
	private InputStream istrm;
	private PrintStream ostrm;
	private Scanner scanner;

	public NumPrompter() {
		istrm = System.in;  // mah
		ostrm = System.out;
		scanner = new Scanner(this.getIstrm());
	}

	/**
	 * Prompts the user for integers with a message, and returns an array. Decimals
	 * are seen, but truncated, because this method uses the getReals method.
	 * 
	 * @param message - Message to prompt the user with
	 * @return Array of integers created from the user's input.
	 */
	public int[] getInts(String message) {
		double[] reals = getReals(message);
		int[] ints = new int[reals.length];
		
		for (int i = 0; i < reals.length; i++) {
			ints[i] = (int) Math.floor(reals[i]);
		}
		
		return ints;
	}

	/**
	 * Prompts the user for numbers with a message, and returns an array.
	 * 
	 * @param message - Message to prompt the user with
	 * @return Array of doubles created from the user's input.
	 */
	public double[] getReals(String message) {
		// Print message, if it is not empty
		if (message.length() > 0) {
			getOstrm().println(message);
		}

		String userString = "";

		// Check if there is even a next line in the scanner before continuing;
		// otherwise stop
		if (scanner.hasNextLine()) {
			userString = scanner.nextLine();
		} else {
			return new double[] {};
		}

		// StringBuilder to build the current value onto in the upcoming for loop
		StringBuilder currentValue = new StringBuilder();
		// ArrayList to append values onto when the StringBuilder has all of the digits
		// needed for the current number
		ArrayList<Double> numberListBuilder = new ArrayList<Double>();
		// Flag for if the current value already has a decimal in it, to avoid cases
		// where multiple decimals are typed into a number
		boolean hasDecimalPoint = false;

		// Iterate through the user's input characters, creating values consisting of
		// digits and an optional decimal. Values are seperated by any other character.
		for (int i = 0; i < userString.length(); i++) {
			char currentChar = userString.charAt(i);

			if (Character.isDigit(currentChar)) {
				// Standard case; append the digit
				currentValue.append(currentChar);
			} else if (currentChar == '.') {
				// A decimal shall only be appended if there is not one already in the number
				// Extra decimals are ignored, and the program assumes that the digits after the
				// extra decimal are supposed to be part of this value
				// For example, 3.14.15 parses as 3.1415 instead of 3.14 and 15 as two seperate
				// values
				if (!hasDecimalPoint) {
					currentValue.append(currentChar);
					hasDecimalPoint = true;
				}
			} else if (currentValue.length() > 0) {
				// Separator found; append the current value and reset
				String valueStr = currentValue.toString();

				if (valueStr.endsWith(".")) {
					// Remove dangling decimal if present
					valueStr = valueStr.substring(0, valueStr.length() - 1);
				}

				if (!valueStr.isEmpty()) {
					// Value is appended to the list here
					numberListBuilder.add(Double.parseDouble(valueStr));
				}

				// Reset for next value
				currentValue.setLength(0);
				hasDecimalPoint = false;
			}
		}

		// Add the last value if one was still being built without a seperator
		// afterwards to trigger it being added
		if (currentValue.length() > 0) {
			String valueStr = currentValue.toString();
			if (valueStr.endsWith(".")) {
				valueStr = valueStr.substring(0, valueStr.length() - 1);
			}
			if (!valueStr.isEmpty()) {
				numberListBuilder.add(Double.parseDouble(valueStr));
			}
		}

		// Convert ArrayList into standard array
		double[] numbers = new double[numberListBuilder.size()];
		for (int i = 0; i < numberListBuilder.size(); i++) {
			numbers[i] = numberListBuilder.get(i);
		}

		return numbers;
	}

	public void closeScanner() {
		scanner.close();
	}

	protected InputStream getIstrm() {
		return istrm;
	}

	protected PrintStream getOstrm() {
		return ostrm;
	}
}