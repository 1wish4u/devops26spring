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
       istrm   = System.in;
       ostrm   = System.out;
       scanner = new Scanner(this.istrm);
   }
   /**
    * Package-private constructor for testing.
    * Accepts explicit InputStream and PrintStream so tests can inject a
    * ByteArrayInputStream (to simulate keyboard input) and a mock/captured
    * PrintStream (to verify console output) without touching System.in/out.
    *
    * @param istrm - InputStream to read from.
    * @param ostrm - PrintStream to write prompts to.
    */
   NumPrompter(InputStream istrm, PrintStream ostrm) {
       this.istrm   = istrm;
       this.ostrm   = ostrm;
       this.scanner = new Scanner(istrm);
   }
   /**
    * Prompts the user for integers with a message, and returns an array.
    * Decimals are seen but truncated (via floor), because this method uses getReals.
    *
    * @param message - Message to prompt the user with.
    * @return Array of integers created from the user's input.
    */
   public int[] getInts(String message) {
       double[] reals = getReals(message);
       int[]    ints  = new int[reals.length];
       for (int i = 0; i < reals.length; i++) {
           ints[i] = (int) Math.floor(reals[i]);
       }
       return ints;
   }
   /**
    * Prompts the user for numbers with a message, and returns an array.
    *
    * @param message - Message to prompt the user with.
    * @return Array of doubles created from the user's input.
    */
   public double[] getReals(String message) {
       // Print message only if non-empty
       if (message.length() > 0) {
           getOstrm().println(message);
       }
       String userString = "";
       if (scanner.hasNextLine()) {
           userString = scanner.nextLine();
       } else {
           return new double[]{};
       }
       StringBuilder         currentValue      = new StringBuilder();
       ArrayList<Double>     numberListBuilder = new ArrayList<Double>();
       boolean               hasDecimalPoint   = false;
       for (int i = 0; i < userString.length(); i++) {
           char currentChar = userString.charAt(i);
           if (Character.isDigit(currentChar)) {
               currentValue.append(currentChar);
           } else if (currentChar == '.') {
               // Only allow one decimal point per number;
               // extra decimals are silently ignored (digits after them stay on the number)
               if (!hasDecimalPoint) {
                   currentValue.append(currentChar);
                   hasDecimalPoint = true;
               }
           } else if (currentValue.length() > 0) {
               // Any non-digit, non-dot character acts as a separator
               String valueStr = currentValue.toString();
               if (valueStr.endsWith(".")) {
                   valueStr = valueStr.substring(0, valueStr.length() - 1);
               }
               if (!valueStr.isEmpty()) {
                   numberListBuilder.add(Double.parseDouble(valueStr));
               }
               currentValue.setLength(0);
               hasDecimalPoint = false;
           }
       }
       // Flush the last value if the string ended without a trailing separator
       if (currentValue.length() > 0) {
           String valueStr = currentValue.toString();
           if (valueStr.endsWith(".")) {
               valueStr = valueStr.substring(0, valueStr.length() - 1);
           }
           if (!valueStr.isEmpty()) {
               numberListBuilder.add(Double.parseDouble(valueStr));
           }
       }
       double[] numbers = new double[numberListBuilder.size()];
       for (int i = 0; i < numberListBuilder.size(); i++) {
           numbers[i] = numberListBuilder.get(i);
       }
       return numbers;
   }
   public void closeScanner() {
       scanner.close();
   }
   protected InputStream getIstrm() { return istrm; }
   protected PrintStream getOstrm() { return ostrm; }
}
