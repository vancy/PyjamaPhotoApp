//Pyjama compiler version:v2.2.0
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

import pj.pr.*;
import pj.PjRuntime;
import pj.Pyjama;
import pi.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.reflect.InvocationTargetException;
import pj.pr.exceptions.*;

public class ImageProjectPanel extends ProjectPanel {

    private static final long serialVersionUID = 1L;

    private JPanel thumbnailsPanel = null;

    private List<PaletteItem> palette = new CopyOnWriteArrayList<PaletteItem>();

    private int parallelism = 2;

    private int density = 16;

    private int size = 16;

    private int buttonSize = 80;

    public ImageProjectPanel(MainFrame mainFrame, String projectName) {
        super(mainFrame, projectName);
        setLayout(new BorderLayout());
        addToolButtonsPanel();
        thumbnailsPanel = new JPanel(new GridLayout(0, 5));
        JScrollPane scroll = new JScrollPane(thumbnailsPanel);
        thumbnailsPanel.setVisible(true);
        scroll.setVisible(true);
        add(scroll, BorderLayout.CENTER);
    }

    private void addToolButtonsPanel() {{
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(makeButton("/res/add.png", actionAddImage, "Add more image(s) to the project"));
        panel.add(makeButton("/res/saveimage.png", actionSaveSelected, "Apply changes to the selected image(s)"));
        panel.add(makeButton("/res/undo.png", actionUndo, "Undo changes to the selected image(s)"));
        panel.add(makeButton("/res/remove.png", actionRemoveImage, "Remove selected image(s) from view"));
        panel.add(makeButton("/res/gradient.png", actionApplyEdge, "Edge detect on the selected image(s)"));
        panel.add(makeButton("/res/video.png", actionInvert, "Invert colors on the selected image(s)"));
        panel.add(makeButton("/res/blur.png", actionBlur, "Blur the selected image(s)"));
        panel.add(makeButton("/res/sharpen.png", actionSharpen, "Sharpen the selected image(s)"));
        panel.add(makeButton("/res/canvas.png", actionBuildMosaic, "Build a mosaic of the selected image(s)"));
        panel.add(makeButton("/res/artwork.png", actionBuildImageMosaic, "Build an image mosaic of the selected image(s)"));
        panel.add(makeButton("/res/palette.png", actionBuildPalette, "Build the palette to be used to make image mosaics"));
        panel.add(makeButton("/res/clearPalette.png", actionClearPalette, "Clear the palette of images"));
        panel.add(makeButton("/res/settings.png", actionMosaicSettings, "Modify attributes related to building mosaics"));
        JPanel grp = new JPanel(new GridLayout(3, 1));
        grp.add(new JLabel("Select..", JLabel.CENTER));
        JButton btnAll = new JButton(actionSelectAll);
        btnAll.setText("All");
        btnAll.setToolTipText("Select all image(s)");
        grp.add(btnAll);
        JButton btnNone = new JButton(actionSelectNone);
        btnNone.setText("None");
        btnNone.setToolTipText("Deselect all image(s)");
        grp.add(btnNone);
        grp.setPreferredSize(new Dimension(buttonSize, buttonSize));
        panel.add(grp);
        add(panel, BorderLayout.NORTH);
        updateActions();
    }
    }


    private void addToThumbnailsPanel(File file, Image large, Image square, Image medium) {{
        thumbnailsPanel.add(new ImagePanelItem(file, large, square, medium, ImageProjectPanel.this));
        updateUI();
    }
    }


    private void finishedAddingNewPanelItems() {{
        isModified = true;
        updateActions();
        thumbnailsPanel.updateUI();
        mainFrame.updateTabIcons();
        mainFrame.updateProjectActions();
    }
    }


    private List<ImagePanelItem> getAllPanels() {{
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
    }


    private List<ImagePanelItem> getSelectedPanels() {{
        ArrayList<ImagePanelItem> list = new ArrayList<ImagePanelItem>();
        Component[] comps = thumbnailsPanel.getComponents();
        for (int i = 0; i < comps.length; i++) {
            ImagePanelItem panel = (ImagePanelItem) comps[i];
            if (panel.isSelected()) list.add(panel);
        }
        return list;
    }
    }


    private void guiChanged() {{
        isModified = true;
        updateActions();
        thumbnailsPanel.updateUI();
        mainFrame.updateTabIcons();
        mainFrame.updateProjectActions();
    }
    }


    private boolean canUndoSomethingSelected() {{
        Iterator<ImagePanelItem> it = getAllPanels().iterator();
        while (it.hasNext()) {
            ImagePanelItem panel = it.next();
            if (panel.isModified() && panel.isSelected()) return true;
        }
        return false;
    }
    }


    private void savePanels(List<ImagePanelItem> list) {{
        Iterator<ImagePanelItem> it = list.iterator();
        while (it.hasNext()) {
            ImagePanelItem panel = it.next();
            panel.commit();
        }
        updateActions();
    }
    }


    private JButton makeButton(String icon, Action action, String tooltip) {{
        JButton btn = new JButton(action);
        btn.setToolTipText(tooltip);
        btn.setIcon(new ImageIcon(getClass().getResource(icon)));
        btn.setPreferredSize(new Dimension(buttonSize, buttonSize));
        return btn;
    }
    }


    public void updateActions() {{
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
    }


    @Override
    public void saveProject() {{
        super.saveProject();
        savePanels(getAllPanels());
    }
    }


    private Action actionAddImage = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            UIManager.put("FileChooser.readOnly", Boolean.TRUE);
            JFileChooser fc = new JFileChooser(projectDir);
            fc.setMultiSelectionEnabled(true);
            fc.setAcceptAllFileFilterUsed(false);
            fc.addChoosableFileFilter(new ImageFilter());
            int retValue = fc.showOpenDialog(ImageProjectPanel.this);
            if (retValue == JFileChooser.APPROVE_OPTION) {
                Timer timer = new Timer(fc.getSelectedFiles().length, "Add Image");
                File[] inputImages = fc.getSelectedFiles();
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                /*OpenMP Target region (#3) -- START */
                _OMP_TargetTaskRegion_3 _OMP_TargetTaskRegion_3_in = new _OMP_TargetTaskRegion_3();
                _OMP_TargetTaskRegion_3_in.timer = timer;
                _OMP_TargetTaskRegion_3_in.inputImages = inputImages;
                if (PjRuntime.currentThreadIsTheTarget("worker")) {
                    _OMP_TargetTaskRegion_3_in.run();
                    timer = _OMP_TargetTaskRegion_3_in.timer;
                    inputImages = _OMP_TargetTaskRegion_3_in.inputImages;
                } else {
                    PjRuntime.submitTargetTask(Thread.currentThread(), "worker", _OMP_TargetTaskRegion_3_in);
                }
                PjRuntime.storeTargetHandlerByName(_OMP_TargetTaskRegion_3_in, "addimage");
                /*OpenMP Target region (#3) -- END */

            }
        }
        }
class _OMP_TargetTaskRegion_4 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            {
                finishedAddingNewPanelItems();
                setCursor(Cursor.getDefaultCursor());
            }
            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    
}



class _OMP_TargetTaskRegion_3 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public Timer timer;
    public File[] inputImages;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            {
                for (int i = 0; i < inputImages.length; i++) {
                    Image large = ImageManipulation.getImageFull(inputImages[i]);
                    Image small = ImageManipulation.getSmallSquare(large);
                    Image medium = ImageManipulation.getMedium(large);
                    addToThumbnailsPanel(inputImages[i], large, small, medium);
                }
                timer.taskComplete();
                /*OpenMP Target region (#4) -- START */
                _OMP_TargetTaskRegion_4 _OMP_TargetTaskRegion_4_in = new _OMP_TargetTaskRegion_4();
                if (PjRuntime.currentThreadIsTheTarget("edt")) {
                    _OMP_TargetTaskRegion_4_in.run();
                } else {
                    PjRuntime.submitTargetTask(Thread.currentThread(), "edt", _OMP_TargetTaskRegion_4_in);
                }
                /*OpenMP Target region (#4) -- END */

            }
            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    
}




    };

    private Action actionUndo = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            Timer timer = new Timer("Undo");
            Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
            while (it.hasNext()) {
                ImagePanelItem panel = it.next();
                panel.restore();
            }
            updateActions();
            timer.taskComplete();
        }
        }

    };

    private Action actionSelectAll = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            Timer timer = new Timer("Select All");
            Component[] comps = thumbnailsPanel.getComponents();
            for (int i = 0; i < comps.length; i++) {
                ImagePanelItem panel = (ImagePanelItem) comps[i];
                panel.setSelected(true);
            }
            updateActions();
            timer.taskComplete();
        }
        }

    };

    private Action actionSelectNone = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            Timer timer = new Timer("Select None");
            Component[] comps = thumbnailsPanel.getComponents();
            for (int i = 0; i < comps.length; i++) {
                ImagePanelItem panel = (ImagePanelItem) comps[i];
                panel.setSelected(false);
            }
            updateActions();
            timer.taskComplete();
        }
        }

    };

    private Action actionInvert = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            Timer timer = new Timer(getSelectedPanels().size(), "Invert Colors");
            Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            while (it.hasNext()) {
                ImagePanelItem panel = it.next();
                /*OpenMP Target region (#5) -- START */
                _OMP_TargetTaskRegion_5 _OMP_TargetTaskRegion_5_in = new _OMP_TargetTaskRegion_5();
                _OMP_TargetTaskRegion_5_in.timer = timer;
                _OMP_TargetTaskRegion_5_in.panel = panel;
                if (PjRuntime.currentThreadIsTheTarget("worker")) {
                    _OMP_TargetTaskRegion_5_in.run();
                    timer = _OMP_TargetTaskRegion_5_in.timer;
                    panel = _OMP_TargetTaskRegion_5_in.panel;
                } else {
                    PjRuntime.submitTargetTask(Thread.currentThread(), "worker", _OMP_TargetTaskRegion_5_in);
                }
                PjRuntime.storeTargetHandlerByName(_OMP_TargetTaskRegion_5_in, "invert");
                /*OpenMP Target region (#5) -- END */

            }
        }
        }
class _OMP_TargetTaskRegion_5 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public ImagePanelItem panel;
    public Timer timer;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            switch(OMP_state) {
            case 0:
                res = ImageManipulation.invert(panel);
                                /*OpenMP Target region (#6) -- START */
                _OMP_TargetTaskRegion_6_in = new _OMP_TargetTaskRegion_6();
                _OMP_TargetTaskRegion_6_in.panel = panel;
                _OMP_TargetTaskRegion_6_in.res = res;
                if (PjRuntime.currentThreadIsTheTarget("edt")) {
                    _OMP_TargetTaskRegion_6_in.run();
                    panel = _OMP_TargetTaskRegion_6_in.panel;
                    res = _OMP_TargetTaskRegion_6_in.res;
                } else {
                    _OMP_TargetTaskRegion_6_in.setOnCompleteCall(this, PjRuntime.getVirtualTargetOfCurrentThread());
                    PjRuntime.submitTargetTask(Thread.currentThread(), "edt", _OMP_TargetTaskRegion_6_in);
                    if (false == PjRuntime.checkFinish(_OMP_TargetTaskRegion_6_in))  {
                        this.OMP_state++;
                        return null;
                    }
                }
                this.OMP_state++;
                /*OpenMP Target region (#6) -- END */

            case 1:
                Throwable OMP_asyncThrow__OMP_TargetTaskRegion_6 = _OMP_TargetTaskRegion_6_in.getException();
                if (null != OMP_asyncThrow__OMP_TargetTaskRegion_6) {
                    if (OMP_asyncThrow__OMP_TargetTaskRegion_6 instanceof Error) {
                        throw (Error)OMP_asyncThrow__OMP_TargetTaskRegion_6;
                    } else if (OMP_asyncThrow__OMP_TargetTaskRegion_6 instanceof RuntimeException) {
                        throw (RuntimeException)OMP_asyncThrow__OMP_TargetTaskRegion_6;
                    }
                }
                panel = _OMP_TargetTaskRegion_6_in.panel;
                res = _OMP_TargetTaskRegion_6_in.res;
                                timer.taskComplete();
                                setCursor(Cursor.getDefaultCursor());
                default:
                    this.setFinish();
            }

            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    private _OMP_TargetTaskRegion_6 _OMP_TargetTaskRegion_6_in;

    private ImageCombo res;
}

class _OMP_TargetTaskRegion_6 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public ImagePanelItem panel;
    public ImageCombo res;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            {
                panel.setImage(res);
                guiChanged();
                setCursor(Cursor.getDefaultCursor());
            }
            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    
}







    };

    private Action actionBlur = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            Timer timer = new Timer(getSelectedPanels().size(), "Blur");
            Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            while (it.hasNext()) {
                ImagePanelItem panel = it.next();
                /*OpenMP Target region (#7) -- START */
                _OMP_TargetTaskRegion_7 _OMP_TargetTaskRegion_7_in = new _OMP_TargetTaskRegion_7();
                _OMP_TargetTaskRegion_7_in.timer = timer;
                _OMP_TargetTaskRegion_7_in.panel = panel;
                if (PjRuntime.currentThreadIsTheTarget("worker")) {
                    _OMP_TargetTaskRegion_7_in.run();
                    timer = _OMP_TargetTaskRegion_7_in.timer;
                    panel = _OMP_TargetTaskRegion_7_in.panel;
                } else {
                    PjRuntime.submitTargetTask(Thread.currentThread(), "worker", _OMP_TargetTaskRegion_7_in);
                }
                /*OpenMP Target region (#7) -- END */

            }
        }
        }
class _OMP_TargetTaskRegion_7 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public ImagePanelItem panel;
    public Timer timer;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            switch(OMP_state) {
            case 0:
                res = ImageManipulation.blur(panel);
                                /*OpenMP Target region (#8) -- START */
                _OMP_TargetTaskRegion_8_in = new _OMP_TargetTaskRegion_8();
                _OMP_TargetTaskRegion_8_in.panel = panel;
                _OMP_TargetTaskRegion_8_in.res = res;
                if (PjRuntime.currentThreadIsTheTarget("edt")) {
                    _OMP_TargetTaskRegion_8_in.run();
                    panel = _OMP_TargetTaskRegion_8_in.panel;
                    res = _OMP_TargetTaskRegion_8_in.res;
                } else {
                    _OMP_TargetTaskRegion_8_in.setOnCompleteCall(this, PjRuntime.getVirtualTargetOfCurrentThread());
                    PjRuntime.submitTargetTask(Thread.currentThread(), "edt", _OMP_TargetTaskRegion_8_in);
                    if (false == PjRuntime.checkFinish(_OMP_TargetTaskRegion_8_in))  {
                        this.OMP_state++;
                        return null;
                    }
                }
                this.OMP_state++;
                /*OpenMP Target region (#8) -- END */

            case 1:
                Throwable OMP_asyncThrow__OMP_TargetTaskRegion_8 = _OMP_TargetTaskRegion_8_in.getException();
                if (null != OMP_asyncThrow__OMP_TargetTaskRegion_8) {
                    if (OMP_asyncThrow__OMP_TargetTaskRegion_8 instanceof Error) {
                        throw (Error)OMP_asyncThrow__OMP_TargetTaskRegion_8;
                    } else if (OMP_asyncThrow__OMP_TargetTaskRegion_8 instanceof RuntimeException) {
                        throw (RuntimeException)OMP_asyncThrow__OMP_TargetTaskRegion_8;
                    }
                }
                panel = _OMP_TargetTaskRegion_8_in.panel;
                res = _OMP_TargetTaskRegion_8_in.res;
                                timer.taskComplete();
                                setCursor(Cursor.getDefaultCursor());
                default:
                    this.setFinish();
            }

            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    private _OMP_TargetTaskRegion_8 _OMP_TargetTaskRegion_8_in;

    private ImageCombo res;
}

class _OMP_TargetTaskRegion_8 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public ImagePanelItem panel;
    public ImageCombo res;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            {
                panel.setImage(res);
                guiChanged();
            }
            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    
}







    };

    private Action actionSharpen = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            Timer timer = new Timer(getSelectedPanels().size(), "Sharpen");
            Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            while (it.hasNext()) {
                ImagePanelItem panel = it.next();
                /*OpenMP Target region (#9) -- START */
                _OMP_TargetTaskRegion_9 _OMP_TargetTaskRegion_9_in = new _OMP_TargetTaskRegion_9();
                _OMP_TargetTaskRegion_9_in.timer = timer;
                _OMP_TargetTaskRegion_9_in.panel = panel;
                if (PjRuntime.currentThreadIsTheTarget("worker")) {
                    _OMP_TargetTaskRegion_9_in.run();
                    timer = _OMP_TargetTaskRegion_9_in.timer;
                    panel = _OMP_TargetTaskRegion_9_in.panel;
                } else {
                    PjRuntime.submitTargetTask(Thread.currentThread(), "worker", _OMP_TargetTaskRegion_9_in);
                }
                /*OpenMP Target region (#9) -- END */

            }
        }
        }
class _OMP_TargetTaskRegion_9 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public ImagePanelItem panel;
    public Timer timer;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            switch(OMP_state) {
            case 0:
                res = ImageManipulation.sharpen(panel);
                                /*OpenMP Target region (#10) -- START */
                _OMP_TargetTaskRegion_10_in = new _OMP_TargetTaskRegion_10();
                _OMP_TargetTaskRegion_10_in.res = res;
                _OMP_TargetTaskRegion_10_in.panel = panel;
                if (PjRuntime.currentThreadIsTheTarget("edt")) {
                    _OMP_TargetTaskRegion_10_in.run();
                    res = _OMP_TargetTaskRegion_10_in.res;
                    panel = _OMP_TargetTaskRegion_10_in.panel;
                } else {
                    _OMP_TargetTaskRegion_10_in.setOnCompleteCall(this, PjRuntime.getVirtualTargetOfCurrentThread());
                    PjRuntime.submitTargetTask(Thread.currentThread(), "edt", _OMP_TargetTaskRegion_10_in);
                    if (false == PjRuntime.checkFinish(_OMP_TargetTaskRegion_10_in))  {
                        this.OMP_state++;
                        return null;
                    }
                }
                this.OMP_state++;
                /*OpenMP Target region (#10) -- END */

            case 1:
                Throwable OMP_asyncThrow__OMP_TargetTaskRegion_10 = _OMP_TargetTaskRegion_10_in.getException();
                if (null != OMP_asyncThrow__OMP_TargetTaskRegion_10) {
                    if (OMP_asyncThrow__OMP_TargetTaskRegion_10 instanceof Error) {
                        throw (Error)OMP_asyncThrow__OMP_TargetTaskRegion_10;
                    } else if (OMP_asyncThrow__OMP_TargetTaskRegion_10 instanceof RuntimeException) {
                        throw (RuntimeException)OMP_asyncThrow__OMP_TargetTaskRegion_10;
                    }
                }
                res = _OMP_TargetTaskRegion_10_in.res;
                panel = _OMP_TargetTaskRegion_10_in.panel;
                                timer.taskComplete();
                                setCursor(Cursor.getDefaultCursor());
                default:
                    this.setFinish();
            }

            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    private _OMP_TargetTaskRegion_10 _OMP_TargetTaskRegion_10_in;

    private ImageCombo res;
}

class _OMP_TargetTaskRegion_10 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public ImagePanelItem panel;
    public ImageCombo res;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            {
                panel.setImage(res);
                guiChanged();
            }
            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    
}







    };

    private Action actionSaveSelected = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {{
            Timer timer = new Timer("Apply Changes");
            savePanels(getSelectedPanels());
            timer.taskComplete();
        }
        }

    };

    private Action actionApplyEdge = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            Timer timer = new Timer(getSelectedPanels().size(), "Edge Detect");
            Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            while (it.hasNext()) {
                ImagePanelItem panel = it.next();
                /*OpenMP Target region (#11) -- START */
                _OMP_TargetTaskRegion_11 _OMP_TargetTaskRegion_11_in = new _OMP_TargetTaskRegion_11();
                _OMP_TargetTaskRegion_11_in.panel = panel;
                _OMP_TargetTaskRegion_11_in.timer = timer;
                if (PjRuntime.currentThreadIsTheTarget("worker")) {
                    _OMP_TargetTaskRegion_11_in.run();
                    panel = _OMP_TargetTaskRegion_11_in.panel;
                    timer = _OMP_TargetTaskRegion_11_in.timer;
                } else {
                    PjRuntime.submitTargetTask(Thread.currentThread(), "worker", _OMP_TargetTaskRegion_11_in);
                }
                /*OpenMP Target region (#11) -- END */

            }
        }
        }
class _OMP_TargetTaskRegion_11 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public ImagePanelItem panel;
    public Timer timer;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            switch(OMP_state) {
            case 0:
                res = ImageManipulation.edgeDetect(panel);
                                /*OpenMP Target region (#12) -- START */
                _OMP_TargetTaskRegion_12_in = new _OMP_TargetTaskRegion_12();
                _OMP_TargetTaskRegion_12_in.panel = panel;
                _OMP_TargetTaskRegion_12_in.res = res;
                if (PjRuntime.currentThreadIsTheTarget("edt")) {
                    _OMP_TargetTaskRegion_12_in.run();
                    panel = _OMP_TargetTaskRegion_12_in.panel;
                    res = _OMP_TargetTaskRegion_12_in.res;
                } else {
                    _OMP_TargetTaskRegion_12_in.setOnCompleteCall(this, PjRuntime.getVirtualTargetOfCurrentThread());
                    PjRuntime.submitTargetTask(Thread.currentThread(), "edt", _OMP_TargetTaskRegion_12_in);
                    if (false == PjRuntime.checkFinish(_OMP_TargetTaskRegion_12_in))  {
                        this.OMP_state++;
                        return null;
                    }
                }
                this.OMP_state++;
                /*OpenMP Target region (#12) -- END */

            case 1:
                Throwable OMP_asyncThrow__OMP_TargetTaskRegion_12 = _OMP_TargetTaskRegion_12_in.getException();
                if (null != OMP_asyncThrow__OMP_TargetTaskRegion_12) {
                    if (OMP_asyncThrow__OMP_TargetTaskRegion_12 instanceof Error) {
                        throw (Error)OMP_asyncThrow__OMP_TargetTaskRegion_12;
                    } else if (OMP_asyncThrow__OMP_TargetTaskRegion_12 instanceof RuntimeException) {
                        throw (RuntimeException)OMP_asyncThrow__OMP_TargetTaskRegion_12;
                    }
                }
                panel = _OMP_TargetTaskRegion_12_in.panel;
                res = _OMP_TargetTaskRegion_12_in.res;
                                timer.taskComplete();
                default:
                    this.setFinish();
            }

            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    private _OMP_TargetTaskRegion_12 _OMP_TargetTaskRegion_12_in;

    private ImageCombo res;
}

class _OMP_TargetTaskRegion_12 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public ImagePanelItem panel;
    public ImageCombo res;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            {
                panel.setImage(res);
                guiChanged();
                setCursor(Cursor.getDefaultCursor());
            }
            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    
}







    };

    private Action actionRemoveImage = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            Timer timer = new Timer("Remove Image");
            Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
            if (it.hasNext()) isModified = true;
            while (it.hasNext()) {
                thumbnailsPanel.remove(it.next());
            }
            updateActions();
            thumbnailsPanel.updateUI();
            mainFrame.updateTabIcons();
            mainFrame.updateProjectActions();
            timer.taskComplete();
        }
        }

    };

    private Action actionBuildMosaic = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            Timer timer = new Timer(getSelectedPanels().size(), "Build Mosaic");
            Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
            while (it.hasNext()) {
                ImagePanelItem panel = it.next();
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                /*OpenMP Target region (#13) -- START */
                _OMP_TargetTaskRegion_13 _OMP_TargetTaskRegion_13_in = new _OMP_TargetTaskRegion_13();
                _OMP_TargetTaskRegion_13_in.density = density;
                _OMP_TargetTaskRegion_13_in.timer = timer;
                _OMP_TargetTaskRegion_13_in.size = size;
                _OMP_TargetTaskRegion_13_in.panel = panel;
                if (PjRuntime.currentThreadIsTheTarget("worker")) {
                    _OMP_TargetTaskRegion_13_in.run();
                    density = _OMP_TargetTaskRegion_13_in.density;
                    timer = _OMP_TargetTaskRegion_13_in.timer;
                    size = _OMP_TargetTaskRegion_13_in.size;
                    panel = _OMP_TargetTaskRegion_13_in.panel;
                } else {
                    PjRuntime.submitTargetTask(Thread.currentThread(), "worker", _OMP_TargetTaskRegion_13_in);
                }
                /*OpenMP Target region (#13) -- END */

            }
        }
        }
class _OMP_TargetTaskRegion_13 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public ImagePanelItem panel;
    public int density;
    public Timer timer;
    public int size;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            switch(OMP_state) {
            case 0:
                res = MosaicBuilder.buildMosaic(panel, density, size);
                                /*OpenMP Target region (#14) -- START */
                _OMP_TargetTaskRegion_14_in = new _OMP_TargetTaskRegion_14();
                _OMP_TargetTaskRegion_14_in.res = res;
                _OMP_TargetTaskRegion_14_in.panel = panel;
                if (PjRuntime.currentThreadIsTheTarget("edt")) {
                    _OMP_TargetTaskRegion_14_in.run();
                    res = _OMP_TargetTaskRegion_14_in.res;
                    panel = _OMP_TargetTaskRegion_14_in.panel;
                } else {
                    _OMP_TargetTaskRegion_14_in.setOnCompleteCall(this, PjRuntime.getVirtualTargetOfCurrentThread());
                    PjRuntime.submitTargetTask(Thread.currentThread(), "edt", _OMP_TargetTaskRegion_14_in);
                    if (false == PjRuntime.checkFinish(_OMP_TargetTaskRegion_14_in))  {
                        this.OMP_state++;
                        return null;
                    }
                }
                this.OMP_state++;
                /*OpenMP Target region (#14) -- END */

            case 1:
                Throwable OMP_asyncThrow__OMP_TargetTaskRegion_14 = _OMP_TargetTaskRegion_14_in.getException();
                if (null != OMP_asyncThrow__OMP_TargetTaskRegion_14) {
                    if (OMP_asyncThrow__OMP_TargetTaskRegion_14 instanceof Error) {
                        throw (Error)OMP_asyncThrow__OMP_TargetTaskRegion_14;
                    } else if (OMP_asyncThrow__OMP_TargetTaskRegion_14 instanceof RuntimeException) {
                        throw (RuntimeException)OMP_asyncThrow__OMP_TargetTaskRegion_14;
                    }
                }
                res = _OMP_TargetTaskRegion_14_in.res;
                panel = _OMP_TargetTaskRegion_14_in.panel;
                                timer.taskComplete();
                default:
                    this.setFinish();
            }

            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    private _OMP_TargetTaskRegion_14 _OMP_TargetTaskRegion_14_in;

    private ImageCombo res;
}

class _OMP_TargetTaskRegion_14 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public ImagePanelItem panel;
    public ImageCombo res;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            {
                panel.setImage(res);
                guiChanged();
                setCursor(Cursor.getDefaultCursor());
            }
            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    
}







    };

    private Action actionBuildPalette = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            Timer timer = new Timer(getSelectedPanels().size(), "Build Palette");
            Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            while (it.hasNext()) {
                ImagePanelItem panel = it.next();
                /*OpenMP Target region (#15) -- START */
                _OMP_TargetTaskRegion_15 _OMP_TargetTaskRegion_15_in = new _OMP_TargetTaskRegion_15();
                _OMP_TargetTaskRegion_15_in.palette = palette;
                _OMP_TargetTaskRegion_15_in.size = size;
                _OMP_TargetTaskRegion_15_in.panel = panel;
                _OMP_TargetTaskRegion_15_in.timer = timer;
                if (PjRuntime.currentThreadIsTheTarget("worker")) {
                    _OMP_TargetTaskRegion_15_in.run();
                    palette = _OMP_TargetTaskRegion_15_in.palette;
                    size = _OMP_TargetTaskRegion_15_in.size;
                    panel = _OMP_TargetTaskRegion_15_in.panel;
                    timer = _OMP_TargetTaskRegion_15_in.timer;
                } else {
                    PjRuntime.submitTargetTask(Thread.currentThread(), "worker", _OMP_TargetTaskRegion_15_in);
                }
                /*OpenMP Target region (#15) -- END */

            }
        }
        }
class _OMP_TargetTaskRegion_15 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public ImagePanelItem panel;
    public Timer timer;
    public List<PaletteItem> palette;
    public int size;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            switch(OMP_state) {
            case 0:
                                palette = MosaicBuilder.buildMosaicPaletteItem(panel, palette, size);
                                /*OpenMP Target region (#16) -- START */
                _OMP_TargetTaskRegion_16_in = new _OMP_TargetTaskRegion_16();
                if (PjRuntime.currentThreadIsTheTarget("edt")) {
                    _OMP_TargetTaskRegion_16_in.run();
                } else {
                    _OMP_TargetTaskRegion_16_in.setOnCompleteCall(this, PjRuntime.getVirtualTargetOfCurrentThread());
                    PjRuntime.submitTargetTask(Thread.currentThread(), "edt", _OMP_TargetTaskRegion_16_in);
                    if (false == PjRuntime.checkFinish(_OMP_TargetTaskRegion_16_in))  {
                        this.OMP_state++;
                        return null;
                    }
                }
                this.OMP_state++;
                /*OpenMP Target region (#16) -- END */

            case 1:
                Throwable OMP_asyncThrow__OMP_TargetTaskRegion_16 = _OMP_TargetTaskRegion_16_in.getException();
                if (null != OMP_asyncThrow__OMP_TargetTaskRegion_16) {
                    if (OMP_asyncThrow__OMP_TargetTaskRegion_16 instanceof Error) {
                        throw (Error)OMP_asyncThrow__OMP_TargetTaskRegion_16;
                    } else if (OMP_asyncThrow__OMP_TargetTaskRegion_16 instanceof RuntimeException) {
                        throw (RuntimeException)OMP_asyncThrow__OMP_TargetTaskRegion_16;
                    }
                }
                                timer.taskComplete();
                default:
                    this.setFinish();
            }

            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    private _OMP_TargetTaskRegion_16 _OMP_TargetTaskRegion_16_in;

}

class _OMP_TargetTaskRegion_16 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            {
                guiChanged();
                setCursor(Cursor.getDefaultCursor());
            }
            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    
}







    };

    private Action actionBuildImageMosaic = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            Timer timer = new Timer(getSelectedPanels().size(), "Build Image Mosaic");
            Iterator<ImagePanelItem> it = getSelectedPanels().iterator();
            while (it.hasNext()) {
                ImagePanelItem panel = it.next();
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

    private Action actionClearPalette = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            Timer timer = new Timer("Clear Palette");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            palette.clear();
            guiChanged();
            setCursor(Cursor.getDefaultCursor());
            timer.taskComplete();
        }
        }

    };

    private Action actionMosaicSettings = new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent arg0) {{
            JPanel panel = new JPanel();
            JSlider parallelismSlider = new JSlider(JSlider.HORIZONTAL, 1, 5, parallelism);
            parallelismSlider.setBorder(BorderFactory.createTitledBorder("Parallelism Level"));
            parallelismSlider.setMajorTickSpacing(2);
            parallelismSlider.setMinorTickSpacing(1);
            parallelismSlider.setPaintTicks(true);
            parallelismSlider.setPaintLabels(true);
            class ParallelismSliderListener implements ChangeListener {

                public void stateChanged(ChangeEvent e) {{
                    JSlider source = (JSlider) e.getSource();
                    if (!source.getValueIsAdjusting()) {
                        parallelism = (int) source.getValue();
                    }
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

                public void stateChanged(ChangeEvent e) {{
                    JSlider source = (JSlider) e.getSource();
                    if (!source.getValueIsAdjusting()) {
                        density = (int) source.getValue();
                    }
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

                public void stateChanged(ChangeEvent e) {{
                    JSlider source = (JSlider) e.getSource();
                    if (!source.getValueIsAdjusting()) {
                        size = (int) source.getValue();
                    }
                }
                }

            }
            sizeSlider.addChangeListener(new SizeSliderListener());
            panel.add(sizeSlider);
            JOptionPane.showConfirmDialog(ImageProjectPanel.this, panel, "Mosaic Settings", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
        }
        }

    };
}
