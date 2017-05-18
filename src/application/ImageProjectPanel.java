package application;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.UIManager;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import application.flickr.ImageCombo;
import operation.ImageFilter;
import operation.ImageManipulation;
import operation.MosaicBuilder;
import operation.PaletteItem;
import util.Timer;

public class ImageProjectPanel extends ProjectPanel {

	private static final long serialVersionUID = 1L;
	private JPanel thumbnailsPanel;
    private List<PaletteItem> palette = new CopyOnWriteArrayList<PaletteItem>();
	
	private int parallelism = 2;
	private int density = 16;
	private int size = 16;
	private int buttonSize = 80;
	
	public ImageProjectPanel(MainFrame mainFrame, String projectName) {
		super(mainFrame, projectName);
		setLayout(new BorderLayout());
		
		addToolButtonsPanel();
		
		thumbnailsPanel = new JPanel(new GridLayout(0,5));
		JScrollPane scroll = new JScrollPane(thumbnailsPanel);
		thumbnailsPanel.setVisible(true);
		scroll.setVisible(true);
		add(scroll, BorderLayout.CENTER);
	}
	
	private void addToolButtonsPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		panel.add(makeButton("images/add.png", actionAddImage, "Add more image(s) to the project"));
		panel.add(makeButton("images/saveimage.png", actionSaveSelected, "Apply changes to the selected image(s)"));
		panel.add(makeButton("images/undo.png", actionUndo, "Undo changes to the selected image(s)"));
		panel.add(makeButton("images/remove.png", actionRemoveImage, "Remove selected image(s) from view"));
		panel.add(makeButton("images/gradient.png", actionApplyEdge, "Edge detect on the selected image(s)"));
		panel.add(makeButton("images/video.png", actionInvert, "Invert colors on the selected image(s)"));
		panel.add(makeButton("images/blur.png", actionBlur, "Blur the selected image(s)"));
		panel.add(makeButton("images/sharpen.png", actionSharpen, "Sharpen the selected image(s)"));
		panel.add(makeButton("images/canvas.png", actionBuildMosaic, "Build a mosaic of the selected image(s)"));
		panel.add(makeButton("images/artwork.png", actionBuildImageMosaic, "Build an image mosaic of the selected image(s)"));
		panel.add(makeButton("images/palette.png", actionBuildPalette, "Build the palette to be used to make image mosaics"));
		panel.add(makeButton("images/clearPalette.png", actionClearPalette, "Clear the palette of images"));
		panel.add(makeButton("images/settings.png", actionMosaicSettings, "Modify attributes related to building mosaics"));
		
		JPanel grp = new JPanel(new GridLayout(3,1));
		grp.add(new JLabel("Select..",JLabel.CENTER));
		JButton btnAll = new JButton(actionSelectAll);
		btnAll.setText("All");
		btnAll.setToolTipText("Select all image(s)");
		grp.add(btnAll);
		JButton btnNone = new JButton(actionSelectNone);
		btnNone.setText("None");
		btnNone.setToolTipText("Deselect all image(s)");
		grp.add(btnNone);
		grp.setPreferredSize(new Dimension(buttonSize,buttonSize));
		panel.add(grp);
		
		add(panel, BorderLayout.NORTH);
		
		updateActions();
	}
	
    
    private void addToThumbnailsPanel(File file, Image large, Image square, Image medium) {
        thumbnailsPanel.add(new ImagePanelItem(file, large, square, medium, ImageProjectPanel.this));
        updateUI();
    }
    
    //This function should always be executed by EDT
    private void finishedAddingNewPanelItems() {
        isModified = true;
        updateActions();
        thumbnailsPanel.updateUI();
        mainFrame.updateTabIcons();
        mainFrame.updateProjectActions();
    }
    
	private List<ImagePanelItem> getAllPanels() {
		ArrayList<ImagePanelItem> list = new ArrayList<ImagePanelItem>();
		
		if (thumbnailsPanel != null) {
			Component[] comps = thumbnailsPanel.getComponents();
			for (int i = 0; i < comps.length; i++) {
				ImagePanelItem panel = (ImagePanelItem) comps[i];
				list.add(panel);
			}
		}

		return list;
	}
	
	private List<ImagePanelItem> getSelectedPanels() {
		ArrayList<ImagePanelItem> list = new ArrayList<ImagePanelItem>();
		
		Component[] comps = thumbnailsPanel.getComponents();
		for (int i = 0; i < comps.length; i++) {
			ImagePanelItem panel = (ImagePanelItem) comps[i];
			if (panel.isSelected())
				list.add(panel);
		}
		return list;
	}
	
	private void guiChanged() {
		isModified = true;
		updateActions();
        thumbnailsPanel.updateUI();
        mainFrame.updateTabIcons();
        mainFrame.updateProjectActions();
	}
	
	private boolean canUndoSomethingSelected() {
		Iterator<ImagePanelItem> it = getAllPanels().iterator();
		while (it.hasNext()) {
			ImagePanelItem panel = it.next();
			if (panel.isModified() && panel.isSelected())
				return true;
		}
		return false;
	}
	
	private void savePanels(List<ImagePanelItem> list) {
		Iterator<ImagePanelItem> it = list.iterator();
		while (it.hasNext()) {
			ImagePanelItem panel = it.next();
			panel.commit();
		}
		updateActions();
	}
	
	private JButton makeButton(String icon, Action action, String tooltip) {
		JButton btn = new JButton(action);
		btn.setToolTipText(tooltip);
		btn.setIcon(new ImageIcon(icon));
		btn.setPreferredSize(new Dimension(buttonSize,buttonSize));
		return btn;
	}
	
	@Override
	public void saveProject() {
		super.saveProject();
		savePanels(getAllPanels());
	}

    private Action actionAddImage = new AbstractAction() {

		private static final long serialVersionUID = 1L;
		@Override
        public void actionPerformed(ActionEvent arg0) {
            UIManager.put("FileChooser.readOnly", Boolean.TRUE);
            JFileChooser fc = new JFileChooser(projectDir);
            fc.setMultiSelectionEnabled(true);
            fc.setAcceptAllFileFilterUsed(false);
            fc.addChoosableFileFilter(new ImageFilter());
            int retValue = fc.showOpenDialog(ImageProjectPanel.this);
            if (retValue == JFileChooser.APPROVE_OPTION) {
				Timer timer = new Timer(fc.getSelectedFiles().length, "Add Image");
                File[] inputImages = fc.getSelectedFiles();
                if (MainFrame.isParallel) {
//                	TaskIDGroup grp = new TaskIDGroup(inputImages.length);
//                	for (int i = 0; i < inputImages.length; i++) {
//                    	TaskID<Image> idImage = ImageManipulation.getImageFullTask(inputImages[i]);
//                    	TaskID<Image> idMedium = ImageManipulation.getMediumTask(idImage) dependsOn(idImage);
//                    	TaskID<Image> idSmall = ImageManipulation.getSmallSquareTask(idImage) dependsOn(idImage);
//                    	TaskID id = addToThumbnailsPanelTask(inputImages[i], idImage, idSmall, idMedium)
//                    			notify(timer::taskComplete());
//                    			dependsOn(idSmall, idMedium);
//                    	grp.add(id);
//                	}
//                	TaskID finalTask = finishedAddingNewPanelItemsTask();
//                	dependsOn(grp);
                } else {
        			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                	for (int i = 0; i < inputImages.length; i++) {
                    	Image large = ImageManipulation.getImageFull(inputImages[i]);
                    	Image small = ImageManipulation.getSmallSquare(large);
                    	Image medium = ImageManipulation.getMedium(large);
                    	addToThumbnailsPanel(inputImages[i], large, small, medium);
                    	timer.taskComplete();
                	}
                    finishedAddingNewPanelItems();
            		setCursor(Cursor.getDefaultCursor());
                }
            }
        }
    };

	
	private Action actionUndo = new AbstractAction() {

		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Timer timer = new Timer("Undo");
			Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
			while (it.hasNext()) {
				ImagePanelItem panel = it.next();
				panel.restore();
			}
            updateActions();
            timer.taskComplete();
		}
	};
	
	
	private Action actionSelectAll = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Timer timer = new Timer("Select All");
			Component[] comps = thumbnailsPanel.getComponents();
			for (int i = 0; i < comps.length; i++) {
				ImagePanelItem panel = (ImagePanelItem) comps[i];
				panel.setSelected(true);
			}
            updateActions();
            timer.taskComplete();
		}
	};
	

	
	private Action actionSelectNone = new AbstractAction() {

		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Timer timer = new Timer("Select None");
			Component[] comps = thumbnailsPanel.getComponents();
			for (int i = 0; i < comps.length; i++) {
				ImagePanelItem panel = (ImagePanelItem) comps[i];
				panel.setSelected(false);
			}
            updateActions();
            timer.taskComplete();
		}
	};
	
	private Action actionInvert = new AbstractAction() {

		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Timer timer = new Timer(getSelectedPanels().size(), "Invert Colors");
			Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
			
			while (it.hasNext()) {
				ImagePanelItem panel = it.next();

				if (mainFrame.isParallel) {
//					// TaskIDGroup = .. 
//					TaskID<ImageCombo> id = ImageManipulation.invertTask(panel) 
//							notify(panel::setImageTask(TaskID), ImageProjectPanel.this::guiChanged(), timer::taskComplete()) 
//							dependsOn(panel.getHistory());
//					panel.addToHistory(id);
//					
//					// grp.add(id) ...
				} else {
				
        			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				
					ImageCombo res = ImageManipulation.invert(panel);
					panel.setImage(res);
					guiChanged();
            		setCursor(Cursor.getDefaultCursor());
            		timer.taskComplete();
				}
			}
		}
	};
	

	
	private Action actionBlur = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Timer timer = new Timer(getSelectedPanels().size(), "Blur");
			Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
			//if (it.hasNext())
				//isModified = true;
			
			while (it.hasNext()) {
				ImagePanelItem panel = it.next();

				if (mainFrame.isParallel) {
//					// TaskIDGroup = .. 
//					TaskID<ImageCombo> id = ImageManipulation.blurTask(panel) 
//							notify(panel::setImageTask(TaskID), ImageProjectPanel.this::guiChanged(), timer::taskComplete()) 
//							dependsOn(panel.getHistory());
//					panel.addToHistory(id);
//					
//					// grp.add(id) ...
				} else {
        			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					ImageCombo res = ImageManipulation.blur(panel);
					panel.setImage(res);
					guiChanged();
            		setCursor(Cursor.getDefaultCursor());
            		timer.taskComplete();
				}
			}
		}
	};
	
	private Action actionSharpen = new AbstractAction() {

		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Timer timer = new Timer(getSelectedPanels().size(), "Sharpen");
			Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
			//if (it.hasNext())
				//isModified = true;
			
			while (it.hasNext()) {
				ImagePanelItem panel = it.next();

				if (mainFrame.isParallel) {
//					// TaskIDGroup = .. 
//					TaskID<ImageCombo> id = ImageManipulation.sharpenTask(panel) 
//							notify(panel::setImageTask(TaskID), ImageProjectPanel.this::guiChanged(), timer::taskComplete()) 
//							dependsOn(panel.getHistory());
//					panel.addToHistory(id);
//					// grp.add(id) ...
				} else {
        			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					ImageCombo res = ImageManipulation.sharpen(panel);
					panel.setImage(res);
					guiChanged();
            		setCursor(Cursor.getDefaultCursor());
            		timer.taskComplete();
				}
			}
		}
	};
	

	
	private Action actionSaveSelected = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			Timer timer = new Timer("Apply Changes");
//			mainFrame.saveCurrentProject();
			savePanels(getSelectedPanels());
			timer.taskComplete();
		}
	};
	
	private Action actionApplyEdge = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Timer timer = new Timer(getSelectedPanels().size(), "Edge Detect");
			Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
			//if (it.hasNext())
				//isModified = true;
			
			while (it.hasNext()) {
				ImagePanelItem panel = it.next();

				if (mainFrame.isParallel) {
//					// TaskIDGroup = .. 
//					TaskID<ImageCombo> id = ImageManipulation.edgeDetectTask(panel) 
//							notify(panel::setImageTask(TaskID), ImageProjectPanel.this::guiChanged(), timer::taskComplete()) 
//							dependsOn(panel.getHistory());
//					panel.addToHistory(id);
//					// grp.add(id) ...
				} else {
        			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					ImageCombo res = ImageManipulation.edgeDetect(panel);
					panel.setImage(res);
					guiChanged();
            		setCursor(Cursor.getDefaultCursor());
            		timer.taskComplete();
				}
			}
		}
	};
	
	private Action actionRemoveImage = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Timer timer = new Timer("Remove Image");
			Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
			if (it.hasNext())
				isModified = true;	
			while (it.hasNext()) {
				thumbnailsPanel.remove(it.next());
			}

            updateActions();
            thumbnailsPanel.updateUI();
            mainFrame.updateTabIcons();
            mainFrame.updateProjectActions();
            timer.taskComplete();
		}
	};
	

	
	public void updateActions() {
		
		boolean empty = true;
		boolean somethingSelected = false;
		boolean allSelected = false;
		boolean paletteReady = false;
		
		if (thumbnailsPanel != null) {
			Component[] comps = thumbnailsPanel.getComponents();
			if (comps.length != 0) {
				empty = false;
				somethingSelected = getSelectedPanels().size() > 0;
				allSelected = getSelectedPanels().size() == comps.length;
				paletteReady = palette.size() > 0;
			} 
		}
		
		if (!empty) {
			actionSelectAll.setEnabled(!allSelected);
			actionRemoveImage.setEnabled(somethingSelected);
			actionSelectNone.setEnabled(somethingSelected);
			actionInvert.setEnabled(somethingSelected);
			actionApplyEdge.setEnabled(somethingSelected);
			actionBlur.setEnabled(somethingSelected);
			actionSharpen.setEnabled(somethingSelected);
			actionBuildMosaic.setEnabled(somethingSelected);
			actionBuildImageMosaic.setEnabled(somethingSelected && paletteReady);
			actionBuildPalette.setEnabled(somethingSelected && !paletteReady);
			actionClearPalette.setEnabled(paletteReady);
		} else {
			actionSelectAll.setEnabled(false);
			actionSelectNone.setEnabled(false);
			actionRemoveImage.setEnabled(false);
			actionInvert.setEnabled(false);
			actionApplyEdge.setEnabled(false);
			actionBlur.setEnabled(false);
			actionSharpen.setEnabled(false);
			actionBuildMosaic.setEnabled(false);
			actionBuildImageMosaic.setEnabled(false);
			actionBuildPalette.setEnabled(false);
			actionClearPalette.setEnabled(paletteReady);
		}
		actionUndo.setEnabled(canUndoSomethingSelected());
		actionSaveSelected.setEnabled(canUndoSomethingSelected());
	}
	

	

	

	

	
	private Action actionBuildMosaic = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Timer timer = new Timer(getSelectedPanels().size(), "Build Mosaic");
			Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
			while (it.hasNext()) {
				ImagePanelItem panel = it.next();

				if (mainFrame.isParallel) {
//					TaskID<ImageCombo> id = MosaicBuilder.buildMosaicTask(panel, density, size) 
//							notify(panel::setImageTask(TaskID), ImageProjectPanel.this::guiChanged(), timer::taskComplete()) 
//							dependsOn(panel.getHistory());
//					panel.addToHistory(id);
				} else {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					ImageCombo res = MosaicBuilder.buildMosaic(panel, density, size);
					panel.setImage(res);
					guiChanged();
					setCursor(Cursor.getDefaultCursor());
					timer.taskComplete();
				}
			}
		}
	};

	private Action actionBuildImageMosaic = new AbstractAction() {

		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Timer timer = new Timer(getSelectedPanels().size(), "Build Image Mosaic");
			Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
			while (it.hasNext()) {
				ImagePanelItem panel = it.next();

				if (mainFrame.isParallel) {
//					if (parallelism == 1) {
//						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//						ImageCombo res = MosaicBuilder.buildImageMosaic2(panel, palette, density);
//						panel.setImage(res);
//						guiChanged();
//						setCursor(Cursor.getDefaultCursor());
//						timer.taskComplete();
//					} else if (parallelism == 2){
//						TaskID<ImageCombo> id = MosaicBuilder.buildImageMosaicTask(panel, palette, density, parallelism) 
//							notify(panel::setImageTask(TaskID), ImageProjectPanel.this::guiChanged(), timer::taskComplete()) 
//							dependsOn(panel.getHistory());
//						panel.addToHistory(id);
//					} else if (parallelism == 3){
//						TaskID<ImageCombo> id = MosaicBuilder.buildImageMosaicTask3(panel, palette, density, parallelism) 
//							notify(panel::setImageTask(TaskID), ImageProjectPanel.this::guiChanged(), timer::taskComplete()) 
//							dependsOn(panel.getHistory());
//						panel.addToHistory(id);
//					} else if (parallelism == 4){
//						TaskID<ImageCombo> id = MosaicBuilder.buildImageMosaicTask4(panel, palette, density, parallelism) 
//							notify(panel::setImageTask(TaskID), ImageProjectPanel.this::guiChanged(), timer::taskComplete()) 
//							dependsOn(panel.getHistory());
//						panel.addToHistory(id);
//					} else {
//						TaskID<ImageCombo> id = MosaicBuilder.buildImageMosaicTask2(panel, palette, density, parallelism) 
//							notify(panel::setImageTask(TaskID), ImageProjectPanel.this::guiChanged(), timer::taskComplete()) 
//							dependsOn(panel.getHistory());
//						panel.addToHistory(id);
//					}
				} else {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					ImageCombo res = MosaicBuilder.buildImageMosaic(panel, palette, density);
					panel.setImage(res);
					guiChanged();
					setCursor(Cursor.getDefaultCursor());
					timer.taskComplete();
				}
			}
		}
	};

	private Action actionBuildPalette = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Timer timer = new Timer(getSelectedPanels().size(), "Build Palette");
			Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
			while (it.hasNext()) {
				ImagePanelItem panel = it.next();

				if (mainFrame.isParallel) {
//					TaskID<List<PaletteItem>> id = MosaicBuilder.buildMosaicPaletteItemTask(panel, palette, size)
//						notify(ImageProjectPanel.this::guiChanged(), timer::taskComplete());
//                    try {
//						palette = id.getReturnResult();
//					} catch (ExecutionException e) {
//			            e.printStackTrace();
//			        } catch (InterruptedException e) {
//			            e.printStackTrace();
//			        }

				} else {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					palette = MosaicBuilder.buildMosaicPaletteItem(panel, palette, size);
					guiChanged();
					setCursor(Cursor.getDefaultCursor());
					timer.taskComplete();
				}
			}
		}
	};
	
	private Action actionClearPalette = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Timer timer = new Timer("Clear Palette");
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			palette.clear();
			guiChanged();
			setCursor(Cursor.getDefaultCursor());
			timer.taskComplete();
		}
	};
	
	private Action actionMosaicSettings = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent arg0) {
        	JPanel panel = new JPanel();
        	JSlider parallelismSlider = new JSlider(JSlider.HORIZONTAL, 1, 5, parallelism);

        	parallelismSlider.setBorder(BorderFactory.createTitledBorder("Parallelism Level"));
        	parallelismSlider.setMajorTickSpacing(2);
        	parallelismSlider.setMinorTickSpacing(1);
        	parallelismSlider.setPaintTicks(true);
        	parallelismSlider.setPaintLabels(true);

			class ParallelismSliderListener implements ChangeListener {
    			public void stateChanged(ChangeEvent e) {
        			JSlider source = (JSlider)e.getSource();
        			if (!source.getValueIsAdjusting()) {
            			parallelism = (int)source.getValue();
        			}
        		}
        	}
        	
			parallelismSlider.addChangeListener(new ParallelismSliderListener());
			panel.add(parallelismSlider);

        	JSlider densitySlider = new JSlider(JSlider.HORIZONTAL, 1, 64, density);

        	densitySlider.setBorder(BorderFactory.createTitledBorder("Mosaic Density"));
        	densitySlider.setMajorTickSpacing(63);
        	densitySlider.setMinorTickSpacing(3);
        	densitySlider.setPaintTicks(true);
        	densitySlider.setPaintLabels(true);

			class DensitySliderListener implements ChangeListener {
    			public void stateChanged(ChangeEvent e) {
        			JSlider source = (JSlider)e.getSource();
        			if (!source.getValueIsAdjusting()) {
            			density = (int)source.getValue();
        			}
        		}
        	}
        	
			densitySlider.addChangeListener(new DensitySliderListener());
			panel.add(densitySlider);

        	JSlider sizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 64, size);

        	sizeSlider.setBorder(BorderFactory.createTitledBorder("Paint Size (requires new palette)"));
        	sizeSlider.setMajorTickSpacing(63);
        	sizeSlider.setMinorTickSpacing(3);
        	sizeSlider.setPaintTicks(true);
        	sizeSlider.setPaintLabels(true);

        	class SizeSliderListener implements ChangeListener {
    			public void stateChanged(ChangeEvent e) {
        			JSlider source = (JSlider)e.getSource();
        			if (!source.getValueIsAdjusting()) {
            			size = (int)source.getValue();
        			}
        		}
        	}

			sizeSlider.addChangeListener(new SizeSliderListener());
        	panel.add(sizeSlider);

            JOptionPane.showConfirmDialog(ImageProjectPanel.this, panel, "Mosaic Settings", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
        }
	};

}