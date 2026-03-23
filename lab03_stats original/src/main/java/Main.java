import model.NumPrompter;
import model.Reporter;

public class Main {
	
	public static void main(String[] args) {
		AppController app = new AppController(new NumPrompter(), System.out);
		app.run();
	}
}