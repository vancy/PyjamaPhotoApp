//Pyjama compiler version:v2.2.0
package application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import javax.swing.JOptionPane;
import java.awt.event.KeyEvent;
import java.util.*;
import application.flickr.PhotoWithImage;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import application.flickr.Search;
import operation.SearchCompare;
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

public class SearchProjectPanel extends ProjectPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private int currentOffset = 1;

    private int accuracy = 1;

    private int sensitivity = 32;

    private String tempTxt = "[enter search criteria here]";

    private JTextField txtSearch = new JTextField("", 18);

    private JButton btnSearch = new JButton(new ImageIcon("images/search.png"));

    private JButton btnStop = new JButton(new ImageIcon("images/stop.png"));

    private JSpinner spnResultsPerPage = null;

    private JLabel lblResPP = new JLabel("#pics");

    private JButton btnNext = new JButton(new ImageIcon("images/right.png"));

    private JButton btnPrev = new JButton(new ImageIcon("images/left.png"));

    private JTextField txtCurrentPage = new JTextField("-", 7);

    public JProgressBar progressBar = new JProgressBar(0, 100);

    private JPanel thumbnailsPanel = null;

    private Font userFont = null;

    private Font emptyFont = null;

    private boolean searchFieldInvalid() {{
        return txtSearch.getText().trim().equals("") || txtSearch.getText().equals(tempTxt);
    }
    }


    public SearchProjectPanel(MainFrame mainFrame, String projectName) {
        super(mainFrame, projectName);
        Dimension sizeBtns = new Dimension(35, 35);
        setLayout(new BorderLayout());
        btnSearch.addActionListener(this);
        btnStop.addActionListener(this);
        btnSearch.setPreferredSize(sizeBtns);
        btnSearch.setToolTipText("Search on Flickr");
        userFont = txtSearch.getFont();
        emptyFont = new Font(txtSearch.getFont().getName(), Font.ITALIC, txtSearch.getFont().getSize());
        txtSearch.setMargin(new Insets(0, 5, 0, 5));
        txtSearch.setText(tempTxt);
        btnSearch.setEnabled(false);
        txtSearch.setFont(emptyFont);
        txtSearch.setForeground(Color.GRAY);
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelSearch.add(txtSearch);
        progressBar.setStringPainted(true);
        txtSearch.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {{
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !searchFieldInvalid()) {
                    clearResults();
                    disableButtons();
                    currentOffset = 1;
                    search();
                }
            }
            }

        });
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {{
            }
            }


            @Override
            public void insertUpdate(DocumentEvent e) {{
                btnSearch.setEnabled(!searchFieldInvalid());
            }
            }


            @Override
            public void removeUpdate(DocumentEvent e) {{
                btnSearch.setEnabled(!searchFieldInvalid());
            }
            }

        });
        txtSearch.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {{
                txtSearch.setForeground(Color.BLACK);
                txtSearch.setFont(userFont);
                if (searchFieldInvalid()) {
                    txtSearch.setText("");
                }
            }
            }


            @Override
            public void focusLost(FocusEvent e) {{
                if (searchFieldInvalid()) {
                    txtSearch.setForeground(Color.GRAY);
                    txtSearch.setFont(emptyFont);
                    txtSearch.setText(tempTxt);
                } else {
                    txtSearch.setForeground(Color.BLACK);
                    txtSearch.setFont(userFont);
                }
            }
            }

        });
        txtSearch.setPreferredSize(new Dimension(txtSearch.getPreferredSize().width, sizeBtns.height));
        txtSearch.setToolTipText("Enter search criteria here");
        panelSearch.add(btnSearch);
        btnStop.setPreferredSize(sizeBtns);
        btnStop.setToolTipText("Cancel search");
        btnStop.setEnabled(false);
        panelSearch.add(btnStop);
        progressBar.setPreferredSize(new Dimension(100, sizeBtns.height));
        panelSearch.add(progressBar);
        JLabel space = new JLabel();
        space.setPreferredSize(new Dimension(15, sizeBtns.height));
        panelSearch.add(space);
        panelSearch.add(lblResPP);
        lblResPP.setToolTipText("Number of photos returned per page");
        SpinnerNumberModel spnModel = new SpinnerNumberModel(8, 1, 99, 1);
        spnResultsPerPage = new JSpinner(spnModel);
        spnResultsPerPage.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {{
                btnPrev.setEnabled(false);
                btnNext.setEnabled(false);
            }
            }

        });
        spnResultsPerPage.setPreferredSize(new Dimension(spnResultsPerPage.getPreferredSize().width, sizeBtns.height));
        spnResultsPerPage.setToolTipText("Number of photos returned per page");
        panelSearch.add(spnResultsPerPage);
        panelSearch.add(new JSeparator());
        panelSearch.add(new JSeparator());
        btnPrev.addActionListener(this);
        btnNext.setToolTipText("View next page of results");
        btnPrev.setToolTipText("View previous page of results");
        btnNext.addActionListener(this);
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        panelSearch.add(btnPrev);
        txtCurrentPage.setHorizontalAlignment(JTextField.CENTER);
        txtCurrentPage.setEditable(false);
        txtCurrentPage.setToolTipText("Current page of results");
        panelSearch.add(txtCurrentPage);
        txtCurrentPage.setPreferredSize(new Dimension(txtCurrentPage.getPreferredSize().width, sizeBtns.height));
        panelSearch.add(btnNext);
        btnPrev.setPreferredSize(sizeBtns);
        btnNext.setPreferredSize(sizeBtns);
        add(panelSearch, BorderLayout.NORTH);
        thumbnailsPanel = new JPanel();
        thumbnailsPanel.setLayout(new BoxLayout(thumbnailsPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(thumbnailsPanel);
        thumbnailsPanel.setVisible(true);
        scroll.setVisible(true);
        add(scroll, BorderLayout.CENTER);
    }

    private void clearResults() {{
        progressBar.setValue(0);
        thumbnailsPanel.removeAll();
        thumbnailsPanel.updateUI();
    }
    }


    private void finishedSearch() {{
        txtCurrentPage.setText("page " + (currentOffset));
        thumbnailsPanel.updateUI();
        isModified = true;
        mainFrame.updateTabIcons();
        enableButtons();
    }
    }


    public void addToDisplay(PhotoWithImage pi) {{
        thumbnailsPanel.add(new PhotoPanelItem(pi.getPhoto(), pi.getImage(), projectDir, this));
    }
    }


    private void enableButtons() {{
        btnStop.setEnabled(false);
        btnSearch.setEnabled(true);
        lblResPP.setEnabled(true);
        txtSearch.setEnabled(true);
        btnNext.setEnabled(true);
        spnResultsPerPage.setEnabled(true);
        if (currentOffset == 1) btnPrev.setEnabled(false); else btnPrev.setEnabled(true);
    }
    }


    private void search() {{
        Timer timer = new Timer("Search");
        String search = txtSearch.getText();
        int resPP = (Integer) spnResultsPerPage.getValue();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        List<PhotoWithImage> results = null;
        SearchProjectPanel tthis = this;
        /*OpenMP Target region (#1) -- START */
        _OMP_TargetTaskRegion_1 _OMP_TargetTaskRegion_1_in = new _OMP_TargetTaskRegion_1();
        _OMP_TargetTaskRegion_1_in.search = search;
        _OMP_TargetTaskRegion_1_in.resPP = resPP;
        _OMP_TargetTaskRegion_1_in.tthis = tthis;
        _OMP_TargetTaskRegion_1_in.results = results;
        _OMP_TargetTaskRegion_1_in.timer = timer;
        _OMP_TargetTaskRegion_1_in.currentOffset = currentOffset;
        if (PjRuntime.currentThreadIsTheTarget("worker")) {
            _OMP_TargetTaskRegion_1_in.run();
            search = _OMP_TargetTaskRegion_1_in.search;
            resPP = _OMP_TargetTaskRegion_1_in.resPP;
            tthis = _OMP_TargetTaskRegion_1_in.tthis;
            results = _OMP_TargetTaskRegion_1_in.results;
            timer = _OMP_TargetTaskRegion_1_in.timer;
            currentOffset = _OMP_TargetTaskRegion_1_in.currentOffset;
        } else {
            PjRuntime.submitTargetTask(Thread.currentThread(), "worker", _OMP_TargetTaskRegion_1_in);
        }
        PjRuntime.storeTargetHandlerByName(_OMP_TargetTaskRegion_1_in, "search");
        /*OpenMP Target region (#1) -- END */

    }
    }
class _OMP_TargetTaskRegion_2 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public List<PhotoWithImage> results;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            {
                finishedSearch();
            }
            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            ;
        }
        this.setFinish();
        return null;
    }
    
}



class _OMP_TargetTaskRegion_1 extends pj.pr.task.TargetTask<Void>{

    //#BEGIN shared, private variables defined here
    public SearchProjectPanel tthis;
    public List<PhotoWithImage> results;
    public String search;
    public Timer timer;
    public int resPP;
    public int currentOffset;
    //#END shared, private variables defined here

    private int OMP_state = 0;
    @Override
    public Void call() {
        try {
            /****User Code BEGIN***/
            {
                results = Search.search(search, resPP, currentOffset, tthis);
                /*OpenMP Target region (#2) -- START */
                _OMP_TargetTaskRegion_2 _OMP_TargetTaskRegion_2_in = new _OMP_TargetTaskRegion_2();
                _OMP_TargetTaskRegion_2_in.results = results;
                if (PjRuntime.currentThreadIsTheTarget("edt")) {
                    _OMP_TargetTaskRegion_2_in.run();
                    results = _OMP_TargetTaskRegion_2_in.results;
                } else {
                    PjRuntime.submitTargetTask(Thread.currentThread(), "edt", _OMP_TargetTaskRegion_2_in);
                }
                /*OpenMP Target region (#2) -- END */

                timer.taskComplete();
            }
            /****User Code END***/
        } catch(pj.pr.exceptions.OmpCancelCurrentTaskException e) {
            finishedSearch(1);
        }
        this.setFinish();
        return null;
    }
    
}





    private void finishedSearch(int a) {{
        txtCurrentPage.setText("page " + (currentOffset));
        thumbnailsPanel.updateUI();
        isModified = true;
        mainFrame.updateTabIcons();
        enableButtons();
        progressBar.setValue(0);
        setCursor(Cursor.getDefaultCursor());
        updateUI();
    }
    }


    private void stopSearch() {{
        Timer timer = new Timer("Stop-Search");
        PjRuntime.setCancellationFlagToTaskName("search");

        timer.taskComplete();
    }
    }


    private void disableButtons() {{
        btnStop.setEnabled(true);
        btnSearch.setEnabled(false);
        lblResPP.setEnabled(false);
        txtSearch.setEnabled(false);
        btnNext.setEnabled(false);
        btnPrev.setEnabled(false);
        spnResultsPerPage.setEnabled(false);
    }
    }


    @Override
    public void actionPerformed(ActionEvent e) {{
        if (e.getSource() == btnStop) {
            stopSearch();
        } else {
            clearResults();
            disableButtons();
            if (e.getSource() == btnSearch) {
                currentOffset = 1;
            } else if (e.getSource() == btnPrev) {
                currentOffset--;
            } else if (e.getSource() == btnNext) {
                currentOffset++;
            }
            search();
        }
    }
    }


    protected void compareHash(PhotoPanelItem compare) {{
        Timer timer = new Timer("Hash Compare");
        if (MainFrame.isParallel) {
            List<PhotoPanelItem> result = SearchCompare.compareHash2(thumbnailsPanel, compare, accuracy);
            thumbnailsPanel.removeAll();
            for (PhotoPanelItem pi : result) {
                thumbnailsPanel.add(pi);
            }
            thumbnailsPanel.updateUI();
            timer.taskComplete();
        } else {
            List<PhotoPanelItem> result = SearchCompare.compareHash(thumbnailsPanel, compare, accuracy);
            thumbnailsPanel.removeAll();
            for (PhotoPanelItem pi : result) {
                thumbnailsPanel.add(pi);
            }
            thumbnailsPanel.updateUI();
            timer.taskComplete();
        }
    }
    }


    protected void compareColor(PhotoPanelItem compare) {{
        Timer timer = new Timer("Color Compare");
        if (MainFrame.isParallel) {
            List<PhotoPanelItem> result = SearchCompare.compareColor2(thumbnailsPanel, compare, sensitivity, accuracy);
            thumbnailsPanel.removeAll();
            for (PhotoPanelItem pi : result) {
                thumbnailsPanel.add(pi);
            }
            thumbnailsPanel.updateUI();
            timer.taskComplete();
        } else {
            List<PhotoPanelItem> result = SearchCompare.compareColor(thumbnailsPanel, compare, sensitivity, accuracy);
            thumbnailsPanel.removeAll();
            for (PhotoPanelItem pi : result) {
                thumbnailsPanel.add(pi);
            }
            thumbnailsPanel.updateUI();
            timer.taskComplete();
        }
    }
    }


    protected void compareSettings() {{
        JPanel panel = new JPanel();
        JSlider accuracySlider = new JSlider(JSlider.HORIZONTAL, 1, 75, accuracy);
        accuracySlider.setBorder(BorderFactory.createTitledBorder("Accuracy Level"));
        accuracySlider.setMajorTickSpacing(74);
        accuracySlider.setMinorTickSpacing(5);
        accuracySlider.setPaintTicks(true);
        accuracySlider.setPaintLabels(true);
        class accuracySliderListener implements ChangeListener {

            public void stateChanged(ChangeEvent e) {{
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    accuracy = (int) source.getValue();
                }
            }
            }

        }
        accuracySlider.addChangeListener(new accuracySliderListener());
        panel.add(accuracySlider);
        JSlider sensitivitySlider = new JSlider(JSlider.HORIZONTAL, 1, 128, sensitivity);
        sensitivitySlider.setBorder(BorderFactory.createTitledBorder("Color Matching Sensitivity"));
        sensitivitySlider.setMajorTickSpacing(127);
        sensitivitySlider.setMinorTickSpacing(6);
        sensitivitySlider.setPaintTicks(true);
        sensitivitySlider.setPaintLabels(true);
        class sensitivitySliderListener implements ChangeListener {

            public void stateChanged(ChangeEvent e) {{
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    sensitivity = (int) source.getValue();
                }
            }
            }

        }
        sensitivitySlider.addChangeListener(new sensitivitySliderListener());
        panel.add(sensitivitySlider);
        JOptionPane.showConfirmDialog(SearchProjectPanel.this, panel, "Compare Settings", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
    }
    }

}
