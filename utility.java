public class utility {
	
	public static void sleep60fps() {
		try {
			Thread.sleep(16);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}	
	}
	
	
}
