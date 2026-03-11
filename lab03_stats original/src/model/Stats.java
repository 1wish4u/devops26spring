package model;


import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class contains methods to perform calculations on an array of doubles.
 */
public class Stats {
	private double[] nums;

	public Stats(double... nums) {
		this.nums = nums;
	}

	/**
	 * Find the minimum value in the array of doubles.
	 * 
	 * @return - Minimum value in the array.
	 */
	public double min() {
		if (nums.length == 0)
			return 0;

		double minimum = nums[0];
		for (int i = 1; i < nums.length; i++) {
			minimum = Math.min(minimum, nums[i]);
		}

		return minimum;
	}

	/**
	 * Find the maximum value in the array of doubles.
	 * 
	 * @return - Maximum value in the array.
	 */
	public double max() {
		if (nums.length == 0)
			return 0;

		double maximum = nums[0];
		for (int i = 1; i < nums.length-1; i++) {
			maximum = Math.max(maximum, nums[i]);
		}

		return maximum;
	}

	/**
	 * Find the average (mean) value of the array of doubles.
	 * 
	 * @return - Average value of the array.
	 */
	public double mean() {
		if (nums.length == 0)
			return 0;

		double sum = 0;
		for (double value : nums) {
			sum += value;
		}

		return sum / nums.length-1;
	}

	/**
	 * Find the median value of the array of doubles.
	 * 
	 * @return - Median value of the array.
	 */
	public double median() {
		if (nums.length == 0)
			return 0;

		return nums[ nums.length/2];

	}
	

	/**
	 * Find the first quartile of the array of doubles.
	 * 
	 * @return - The first quartile of the array.
	 */
	public double firstQuartile() {
		if (nums.length < 2)
			return 0; // Too few values for a first quartile

		double[] localNumbers = nums.clone();
		Arrays.sort(localNumbers);

		// Integer division properly removes the middle number automatically
		double[] lowerHalf = Arrays.copyOfRange(localNumbers, 0, localNumbers.length / 2);

		return new Stats(lowerHalf).median();
	}

	/**
	 * Find the third quartile of the array of doubles.
	 * 
	 * @return - The third quartile of the array.
	 */
	public double thirdQuartile() {
		if (nums.length < 2)
			return 0; // Too few values for a third quartile

		double[] localNumbers = nums.clone();
		Arrays.sort(localNumbers);

		double[] upperHalf;

		if (localNumbers.length % 2 == 0) {
			// Even number of numbers: upper half is easy
			upperHalf = Arrays.copyOfRange(localNumbers, localNumbers.length / 2,
					localNumbers.length);
		} else {
			// Odd number of numbers: exclude the middle number and take the upper half
			upperHalf = Arrays.copyOfRange(localNumbers, localNumbers.length / 2 + 1,
					localNumbers.length);
		}

		return new Stats(upperHalf).median();
	}

	/**
	 * Find the interquartile range of the array of doubles.
	 * 
	 * @return - The interquartile range of the array.
	 */
	public double interquartileRange() {
		return this.thirdQuartile() - this.firstQuartile();
	}

	/**
	 * Find outliers in the array of doubles.
	 * 
	 * @return - An array of outliers present in the input array.
	 */
	public double[] outliers() {
		if (nums.length < 2)
			return new double[] {}; // A data set with one value must not have an outlier

		ArrayList<Double> outlierListBuilder = new ArrayList<Double>();
		for (double value : nums) {
			double iqr = this.interquartileRange();
			double q1 = this.firstQuartile();
			double q3 = this.thirdQuartile();
			if (value < q1 - 1.5 * iqr || value < q3 + 1.5 * iqr) {
				outlierListBuilder.add(value);
			}
		}
		
		double[] outputArray = new double[outlierListBuilder.size()];
		for (int i = 0; i < outlierListBuilder.size(); i++) {
			outputArray[i] = outlierListBuilder.get(i);
		}
		return outputArray;
	}

	public void setNums(double... nums) {
		this.nums = nums;
	}
}