package application;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

public class MainFrame extends JFrame {

	public static final String applicationName = "ParaImage";
	public static final String applicationVersion = "0.95";
	public static final String appIcon = "images/logo.png";
	public static final String appLogo = "images/logo_name.png";

	public static int timeWasterSize = 150;// 120;

	private static final long serialVersionUID = 1391078337106684377L;
	public static int width = 1300;
	public static int height = 980;

	public static Image frameIcon = null;

	private int nextUntitledNum = 0;

	private JTabbedPane pane = new JTabbedPane();
	private ModePanel mode = new ModePanel(this);

	public static boolean isParallel = false;

	public MainFrame() {
		setTitle(applicationName + " v. " + applicationVersion);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				checkExit();
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setSize(width, height);

		try {
			frameIcon = ImageIO.read(new File(appIcon));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		setIconImage(frameIcon);

		Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		pane.setBorder(padding);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (dim.width - width) / 2;
		int y = (dim.height - height) / 2;
		setLocation(x, y);

		pane.addMouseListener(new TabMouseListener());
		pane.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
				updateProjectActions();
				updateTabIcons();
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				updateProjectActions();
				updateTabIcons();
			}
		});

		iconSave = new ImageIcon("images/save_16.png");
		addMenus();
		getContentPane().add(pane, BorderLayout.CENTER);
		setResizable(false);

		getContentPane().add(mode, BorderLayout.SOUTH);

		// for testing, to save time
		// newSearchProject();
		// newImageProject();
	}

	private Action actionShowAbout = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String message = "<html><font color=#8888ff size=+2>"
					+ applicationName + "</font><p><p>";
			message += "Version: " + applicationVersion + "<p><p>";
			message += "Copyright&copy 2009 Nasser Giacaman, Peter Nicolau and Oliver Sinnen<p><p>";
			message += "Sample application developed with ParaTask.<p>";
			message += "Visit <u><i>http://www.ece.auckland.ac.nz/~sinnen</i></u> for more information";
			JLabel label = new JLabel(message);
			JOptionPane.showMessageDialog(MainFrame.this, label, "About "
					+ applicationName, JOptionPane.PLAIN_MESSAGE,
					new ImageIcon(appLogo));
		}
	};

	private void showFutureWork() {
		String message = "<html><font color=#8888ff size=+2>Future release</font><p><p>";
		message += "Check back in a future release!<p><p>";
		JLabel label = new JLabel(message);
		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JOptionPane.showMessageDialog(MainFrame.this, label, "Future release",
				JOptionPane.PLAIN_MESSAGE, new ImageIcon("images/tools.png"));
	}

	private Action actionFutureWork = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			showFutureWork();
		}
	};

	private Action actionNewSearchProject = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			newSearchProject();
		}
	};

	private Action actionNewImageProject = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			newImageProject();
		}
	};

	private Action actionExit = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			checkExit();
		}
	};

	private Action actionCloseCurrentProject = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			closeCurrentProject();
		}
	};

	private Action actionPrintCurrentProject = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			showFutureWork();
		}
	};

	private Action actionSaveCurrentProject = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			saveCurrentProject();
		}
	};

	private void checkExit() {
		for (int i = 0; i < pane.getTabCount(); i++) {
			ProjectPanel project = (ProjectPanel) pane.getComponent(i);
			if (!project.isSaved() || project.isModified()) {
				int resp = JOptionPane
						.showConfirmDialog(
								MainFrame.this,
								"There are unsaved projects open. Are you sure you want to exit?",
								"Exit confirmation", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);
				if (resp == JOptionPane.YES_OPTION) {
					System.exit(0);
				} else {
					return;
				}
			}
		}
		System.exit(0);
	}

	private ImageIcon iconSave = null;

	public void updateProjectActions() {
		actionSaveCurrentProject.setEnabled(false);
		Component comp = pane.getSelectedComponent();
		if (comp instanceof ProjectPanel) {
			ProjectPanel project = (ProjectPanel) comp;
			// actionSaveCurrentProject.setEnabled(!project.isSaved() ||
			// project.isModified());

			if (project.isModified() || !project.isSaved()) {
				actionSaveCurrentProject.setEnabled(true);
			} else {
				actionSaveCurrentProject.setEnabled(false);
			}
		}
		actionCloseCurrentProject.setEnabled(pane.getTabCount() > 0);
		actionPrintCurrentProject.setEnabled(pane.getTabCount() > 0);
	}

	private void newImageProject() {
		String name = "Untitled project";
		if (nextUntitledNum != 0)
			name += " " + nextUntitledNum;
		nextUntitledNum++;
		ImageProjectPanel panel = new ImageProjectPanel(this, name);
		pane.addTab(panel.getProjectName(), iconSave, panel);
		pane.setSelectedComponent(panel);
	}

	private void newSearchProject() {
		String name = "Untitled search";
		if (nextUntitledNum != 0)
			name += " " + nextUntitledNum;
		nextUntitledNum++;
		SearchProjectPanel panel = new SearchProjectPanel(this, name);
		pane.addTab(panel.getProjectName(), iconSave, panel);
		pane.setSelectedComponent(panel);
	}

	public boolean saveCurrentProject() {
		Component comp = pane.getSelectedComponent();
		if (comp instanceof ProjectPanel) {
			ProjectPanel projectPanel = (ProjectPanel) comp;

			if (!projectPanel.isSaved()) {
				// -- first-time saving, therefore specify project name
				JFileChooser fc = new JFileChooser();
				int result = fc.showSaveDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					String name = fc.getSelectedFile().getName();
					projectPanel.setName(name);
					pane.setTitleAt(pane.indexOfComponent(projectPanel), name);
					projectPanel.saveProject();
					updateTabIcons();
					updateProjectActions();
					return true;
				} else {
					return false;
					// -- save cancelled
				}
			} else if (projectPanel.isModified()) {
				projectPanel.saveProject();
				updateTabIcons();
				updateProjectActions();
				return true;
			}
		}
		return false;
	}

	public void updateTabIcons() {
		for (int i = 0; i < pane.getTabCount(); i++) {
			Component comp = pane.getComponent(i);
			if (comp instanceof ProjectPanel) {
				ProjectPanel projectPanel = (ProjectPanel) comp;
				if (projectPanel.isModified() || !projectPanel.isSaved()) {
					pane.setIconAt(i, iconSave);
				} else {
					pane.setIconAt(i, null);
				}
			}
		}
	}

	private void closeCurrentProject() {
		Component comp = pane.getSelectedComponent();
		if (comp instanceof ProjectPanel) {
			ProjectPanel projectPanel = (ProjectPanel) comp;
			if (!projectPanel.isSaved()) {
				int resp = JOptionPane.showConfirmDialog(MainFrame.this,
						"Would you like to save this project before closing?",
						"Close confirmation", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (resp == JOptionPane.YES_OPTION) {
					boolean saved = saveCurrentProject();
					if (saved)
						pane.remove(comp);
				} else if (resp == JOptionPane.NO_OPTION) {
					pane.remove(comp);
				}
			} else if (projectPanel.isModified()) {
				int resp = JOptionPane.showConfirmDialog(MainFrame.this,
						"Save modifications before closing?",
						"Close confirmation", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (resp == JOptionPane.YES_OPTION) {
					boolean saved = saveCurrentProject();
					if (saved)
						pane.remove(comp);
				} else if (resp == JOptionPane.NO_OPTION) {
					pane.remove(comp);
				}
			} else {
				pane.remove(comp);
			}
		}
		updateTabIcons();
		updateProjectActions();
	}

	class TabMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON2) {
				closeCurrentProject();
			} else if (e.getButton() == MouseEvent.BUTTON1) {
				if (e.getClickCount() == 2) {
					Component comp = pane.getSelectedComponent();
					if (comp instanceof ProjectPanel) {
						saveCurrentProject();
					}
				}
			}
		}
	}

	private void addMenus() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("File");

		JMenu newItem = new JMenu("New");
		JMenuItem newSearch = new JMenuItem(actionNewSearchProject);
		newSearch.setText("Flickr search project");
		JMenuItem newImage = new JMenuItem(actionNewImageProject);
		newImage.setText("Image editing project");
		newItem.add(newSearch);
		newItem.add(newImage);
		menuFile.add(newItem);

		menuFile.addSeparator();
		JMenuItem projPrint = new JMenuItem(actionPrintCurrentProject);
		projPrint.setText("Print project");
		JMenuItem projSave = new JMenuItem(actionSaveCurrentProject);
		projSave.setText("Save project");
		JMenuItem projClose = new JMenuItem(actionCloseCurrentProject);
		projClose.setText("Close project");

		menuFile.add(projPrint);
		menuFile.add(projSave);
		menuFile.add(projClose);

		menuFile.addSeparator();
		JMenuItem exit = new JMenuItem(actionExit);
		exit.setText("Exit");
		menuFile.add(exit);

		JMenu menuSettings = new JMenu("Settings");
		JMenuItem settingShortcuts = new JMenuItem(actionFutureWork);
		JMenuItem settingApp = new JMenuItem(actionFutureWork);
		settingShortcuts.setText("Configure shortcuts...");
		settingApp.setText("Configure applications...");
		menuSettings.add(settingShortcuts);
		menuSettings.add(settingApp);

		JMenu menuHelp = new JMenu("Help");
		JMenuItem helpAbt = new JMenuItem(actionShowAbout);
		helpAbt.setText("About...");
		menuHelp.add(helpAbt);

		menuBar.add(menuFile);
		menuBar.add(menuSettings);
		menuBar.add(menuHelp);
		setJMenuBar(menuBar);
	}

	public static void main(String[] args) {
		MainFrame frame = new MainFrame();
		frame.setVisible(true);
	}

}
