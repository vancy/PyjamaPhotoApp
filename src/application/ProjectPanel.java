package application;

import java.io.File;

import javax.swing.JPanel;

public class ProjectPanel extends JPanel {

	protected String projectName;
	protected boolean isSaved = false;
	protected boolean isModified = true;
	protected MainFrame mainFrame;
	protected File projectDir = new File("/home/lxuser/Downloads/small");

	public ProjectPanel(MainFrame mainFrame, String projectName) {
		this.mainFrame = mainFrame;
		this.projectName = projectName;
	}

	public String getProjectName() {
		return projectName;
	}

	public boolean isSaved() {
		return isSaved;
	}

	public boolean isModified() {
		return isModified;
	}

	public void saveProject() {
		isSaved = true;
		isModified = false;
	}

}
