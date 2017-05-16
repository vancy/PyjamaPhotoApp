package application;

public class Timer {

	private long initTime;
	private int totalTasks;
	private int completedTasks = 0;
	private String description;

	public Timer(int totalTasks, String description) {
		this.initTime = System.currentTimeMillis();
		this.totalTasks = totalTasks;
		this.description = description + " ";
	}

	public Timer(int totalTasks) {
		this.initTime = System.currentTimeMillis();
		this.totalTasks = totalTasks;
		this.description = "";
	}

	public Timer(String description) {
		this.initTime = System.currentTimeMillis();
		this.totalTasks = 1;
		this.description = description + " ";
	}

	public Timer() {
		this.initTime = System.currentTimeMillis();
		this.totalTasks = 1;
		this.description = "";
	}

	public void taskComplete() {
		completedTasks++;
		if (completedTasks == totalTasks) {
			ModePanel.overlayText("Last " + description + "operation took "
					+ (System.currentTimeMillis() - initTime) + "ms");
			System.out.println("Last " + description + "operation took "
					+ (System.currentTimeMillis() - initTime) + "ms");
		}
	}

	public void addTask() {
		totalTasks++;
	}
}