import model.NumPrompter;
import model.Reporter;

public class Main {
	
	public static void main(String[] args) {
		boolean quit = false;
		NumPrompter prompter = new NumPrompter();
		
		while (!quit) {
			double[] reals = prompter.getReals("""
					To calculate stats, enter some non-negative numbers, separated by commas,
					spaces, or any other non-numeric character, or press enter with no data to
					quit...""");
			
			if (reals.length == 0) {
				quit = true;
				continue;
			}
			
			Reporter reporter = new Reporter();
			reporter.setNums(reals);
			System.out.println("\n" + reporter.reportStatistics());
		}
		
		prompter.closeScanner();
		System.out.println("Program ended.");
	}
}