package application;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;


import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;


import application.flickr.Search;
import util.Timer;

import com.flickr4java.flickr.photos.Photo;

public class PhotoPanelItem extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private Photo photo;
	private Image imageSquare;
	private Image imageLarge;
	private File preferredDir;
	private SearchProjectPanel parent;
	
	private String defaultName = "unnamed";

	private JButton btnDownload = new JButton(new ImageIcon(getClass().getResource("/res/download.png")));
	private JButton btnView = new JButton(new ImageIcon(getClass().getResource("/res/openfull.png")));
	private JButton btnSave = new JButton(new ImageIcon(getClass().getResource("/res/save2.png")));
	private JButton btnHash = new JButton(new ImageIcon(getClass().getResource("/res/hashComp.png")));
	private JButton btnColor = new JButton(new ImageIcon(getClass().getResource("/res/colorComp.png")));
	private JButton btnSettings = new JButton(new ImageIcon(getClass().getResource("/res/settingsSmall.png")));
	
	private static int height = 100;
	
	public PhotoPanelItem(Photo photo, Image imageSquare, File preferredDir, SearchProjectPanel parent) {
		this.photo = photo;
		this.imageSquare = imageSquare;
		this.preferredDir = preferredDir;
		this.parent = parent;
				
		setLayout(null);
		
		JLabel lblImage = new JLabel(new ImageIcon(imageSquare));
		add(lblImage);
		
		Dimension size = lblImage.getPreferredSize();
		lblImage.setBounds(40, 20, size.width, size.height);
		
		String photoTitle = photo.getTitle();
		if (photoTitle.equals(""))
			photoTitle = defaultName;
		
		JLabel lblTitle = new JLabel(photoTitle);
		add(lblTitle);
		size = lblTitle.getPreferredSize();
		lblTitle.setBounds(150, 20, size.width, size.height);

		JPanel pnlButtons = new JPanel();

		pnlButtons.add(btnDownload);
		btnDownload.setToolTipText("Retrieve full size");
		pnlButtons.add(btnView);
		btnView.setToolTipText("View full size");
		pnlButtons.add(btnSave);
		btnSave.setToolTipText("Save to file");
		pnlButtons.add(btnHash);
		btnHash.setToolTipText("Compare images by hash");
		pnlButtons.add(btnColor);
		btnColor.setToolTipText("Compare images by color");
		pnlButtons.add(btnSettings);
		btnSettings.setToolTipText("Modify attributes related to comparisons");
		
		btnView.setEnabled(false);
		btnSave.setEnabled(false);
		btnDownload.addActionListener(this);
		btnView.addActionListener(this);
		btnSave.addActionListener(this);
		btnHash.addActionListener(this);
		btnColor.addActionListener(this);
		btnSettings.addActionListener(this);
		
		Dimension btnSize = new Dimension(45,45);
		btnDownload.setPreferredSize(btnSize);
		btnView.setPreferredSize(btnSize);
		btnSave.setPreferredSize(btnSize);
		btnHash.setPreferredSize(btnSize);
		btnColor.setPreferredSize(btnSize);
		btnSettings.setPreferredSize(btnSize);
		
		add(pnlButtons);
		size = pnlButtons.getPreferredSize();
		pnlButtons.setBounds(580, 20, size.width, size.height);
		
		setPreferredSize(new Dimension(MainFrame.width-100, height));
	}
	
	public Photo getPhoto() {
		return photo;
	}
	
	public Image getSquarePhoto() {
		return imageSquare;
	}
	
	private void downloadComplete(Image image) {
		imageLarge = image;
		btnView.setEnabled(true);
		btnSave.setEnabled(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnDownload) {
			download();
		} else if (e.getSource() == btnView) {
			view();
		} else if (e.getSource() == btnSave) {
			save();
		} else if (e.getSource() == btnHash) {
			parent.compareHash(this);
		} else if (e.getSource() == btnColor) {
			parent.compareColor(this);
		} else if (e.getSource() == btnSettings) {
			parent.compareSettings();
		}
	}
	
	private void download() {
		Timer timer = new Timer("Download");
		btnDownload.setEnabled(false);
		if (MainFrame.isParallel) {
//			TaskID<Image> id = Search.getMediumImageTask(photo) notify(downloadCompleteTask(TaskID), timer::taskComplete());
		} else {
    		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			Image result = Search.getMediumImage(photo);
			downloadComplete(result);
        	setCursor(Cursor.getDefaultCursor());
        	timer.taskComplete();
		}
	}
	private void view() {
		JFrame frame = new JFrame(photo.getTitle());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		ZoomCanvas canvas = new ZoomCanvas(imageLarge);
		frame.add(canvas, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		canvas.createBufferStrategy(3);
	}
	
	private void save() {
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);
		JFileChooser fc = new JFileChooser(preferredDir);
		String fileName = photo.getTitle();
		if (fileName.equals(""))
			fileName = defaultName;
		fileName+=".jpeg";
		fc.setSelectedFile(new File(preferredDir, fileName));
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			File outputFile = new File(file.getParent(), file.getName());
			
			if (outputFile.exists()) {
				JOptionPane.showConfirmDialog(this, "File already exists, please select another name.", "File exists.", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
			} else {
				RenderedImage rendered = null;
		        if (imageLarge instanceof RenderedImage) {
		            rendered = (RenderedImage) imageLarge;
		        } else {
		            BufferedImage buffered = new BufferedImage(imageLarge.getWidth(null), imageLarge.getHeight(null), BufferedImage.TYPE_INT_RGB);
		            Graphics2D g = buffered.createGraphics();
		            g.drawImage(imageLarge, 0, 0, null);
		            g.dispose();
		            rendered = buffered;
		        }
		        try {
					ImageIO.write(rendered, "JPEG", outputFile);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
