/*
 * LeeEditorView.java
 */
package leesystem;

import java.awt.Color;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.BadLocationException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import leesystem.program.code.EnactTranslator;

/**
 * The application's main frame.
 */
public class LeeEditorView extends FrameView implements DocumentListener, UndoableEditListener {

    private String action;
    private EnactTranslator translator;
    private String location;
    private String temporal;
    private String rank;
    private String interest,  interest1;
    private String name;
    private int caretPos = 0;
    private int caretPoss[] = new int[20];
    private int c = 0,  mark,  dotPos,  spacePos;
    private JPopupMenu jpm,  tpop;
    private String author,  version,  runname,  filename;
    int colin = 0;
    private AbstractDocument doc;
    private String editorValues[] = new String[32];
    private int ev = 0;
    private JMenuItem tim;
    private String lastText;
    private boolean undoRed = true,  insertbool = false,  changebool = false,  removebool = false;
    private boolean spaceEdit = false;
    private String lastInsText;
    private String lastRemText;
    private int unindex = 0;
    private String currDoc;
    private boolean special = false;
    StyledDocument enactDoc;
    StringBuffer buffer = new StringBuffer();
    private String[] keywords = {"run", "enact", "enact::name", "enact::action", "enact::location", "enact::temporal", "enact::rank", "//", "enact::interest", "name", "::", "=", ";", "action", "location", "temporal", "interest", "rank", "@", "author", "version"};
    private Color keyColors[] = {Color.MAGENTA, Color.CYAN, Color.CYAN, Color.CYAN, Color.CYAN, Color.CYAN, Color.CYAN, Color.BLUE, Color.CYAN, Color.CYAN, Color.CYAN, Color.PINK, Color.GRAY,
        Color.CYAN, Color.CYAN, Color.CYAN, Color.CYAN, Color.CYAN, Color.BLUE, Color.BLUE, Color.BLUE
    };
    JPopupMenu jmMenu;
    private char keyChars[] = new char[100];
    private int t = 0;
    private int wordStart;
    private int wordLength;
    private String comment;
    private boolean openFile = true;

    public LeeEditorView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        ImageIcon icon = new ImageIcon(this.getClass().getResource("resources/icon.png"));
        this.getFrame().setIconImage(icon.getImage());
        doc = (AbstractDocument) editor.getDocument();
        enactDoc = (StyledDocument) doc;

        doc.addDocumentListener(this);
        // doc.addUndoableEditListener(this);

        jmMenu = new JPopupMenu("Action");

        JMenuItem jmi = new JMenuItem("Run");
        jmi.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                run();
            }
        });
        jmMenu.add(jmi);

        translateTxt.add(jmMenu);

        jpm = new JPopupMenu("Keywords");
        JMenuItem item = new JMenuItem("start");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                insertText("//sample\n", Color.BLUE);
                insertText("@author appiah\n @version 1.0\n", Color.BLUE);
                insertText("@run", Color.MAGENTA);
                insertText(" run_name", Color.GREEN);
                insertText("\n{\n", Color.ORANGE);
                insertText("enact::action", Color.CYAN);
                insertText(" a=", Color.PINK);
                insertText("\"buy\"", Color.YELLOW);
                insertText(";", Color.GRAY);
                insertText("\n}", Color.ORANGE);
            }
        });
        jpm.add(item);
        item = new JMenuItem("sample");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                insertText("//sample\n", Color.BLUE);
                insertText("@author appiah\n@version 1.0\n", Color.BLUE);
                insertText("@run", Color.MAGENTA);
                insertText(" sell_enact", Color.GREEN);
                insertText("\n{\n", Color.ORANGE);

                insertText("action ", Color.CYAN);
                insertText("a=", Color.PINK);
                insertText("'sell';\n", Color.YELLOW);
                insertText("interest ", Color.CYAN);
                insertText("ia=", Color.PINK);
                insertText("'sell_pc'", Color.YELLOW);
                insertText(", ", Color.GRAY);
                insertText("ia1=", Color.PINK);
                insertText("'buy_pc'", Color.YELLOW);
                insertText(";\n", Color.GRAY);

                insertText("rank ", Color.CYAN);
                insertText("r=", Color.PINK);
                insertText("'rank_9'", Color.YELLOW);
                insertText(";\n", Color.GRAY);

                insertText("location ", Color.CYAN);
                insertText("loc='", Color.PINK);
                insertText("tottenham'", Color.YELLOW);
                insertText(";\n", Color.GRAY);
                insertText("temporal ", Color.CYAN);
                insertText(" temp=", Color.PINK);
                insertText("'Sat-12/09/2019-12:00pm'", Color.YELLOW);
                insertText(";\n", Color.GRAY);
                insertText("name=", Color.CYAN);
                insertText("'Tottenham-Engagement'", Color.YELLOW);
                insertText(";\n", Color.GRAY);

            }
        });
        jpm.add(item);
        item = new JMenuItem("sample1");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String code = ("//sample\n @author appiah\n @version 1.0\n@run sell_enact \n{\n" + "enact::action a='sell';\n" +
                        "enact::interest ia='sell_pc', ia1='buy_pc';\n" +
                        "enact::rank r='rank_9';\n" +
                        "enact::location loc='AshantiReg-Kumasi';\n" +
                        "enact::temporal temp='Sat-12/09/2019-12:00pm';\n" +
                        "enact::name='Kumasi-Engagement';\n}");
                programStyle(code);
            }
        });
        jpm.add(item);
        item = new JMenuItem("sample2");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String code = ("//sample\n @author appiah\n @version 1.0\n@run sell_enact \n{\n" + "1. action a='sell';\n" +
                        "2. interest ia='sell_pc', ia1='buy_pc';\n" +
                        "3. rank r='rank_9';\n" +
                        "4. location loc='tottenham';\n" +
                        "5. temporal temp='12/4/20->14/05/21';\n" +
                        "6. name='Tottenham-Engagement';\n}");
                programStyle(code);
            }
        });
        jpm.add(item);
        item = new JMenuItem("sample3");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String code = ("//sample\n @author appiah\n @version 1.0\n@run pay_engage \n{\n" + "1:: action --> a='sell';\n" +
                        "2:: interest--> ia='sell_pc', ia1='buy_pc';\n" +
                        "3:: rank--> r='rank_9';\n" +
                        "4:: location--> loc='Accra-12N5E';\n" +
                        "5:: temporal--> temp='12/4/20->14/05/21';\n" +
                        "6:: name='Ofankor-Engagement';\n}");
                programStyle(code);
            }
        });
        jpm.add(item);
        item = new JMenuItem("temporal sample4");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String code = ("//sample\n @author appiah\n @version 1.0\n@run buy_enact22 \n{\n" + "1:: action --> a=\"sell\";\n" +
                        "2:: interest--> ia=\"buy_pc\", ia1=\"trade_mobile\";\n" +
                        "3:: rank--> r=\"rank_33\";\n" +
                        "4:: location--> loc=\"Accra-44N30E\";\n" +
                        "5:: temporal--> temp=\"before!12/4/20->at!4/05/21\";\n" +
                        "6:: name=\"Ofankor-Engagement\";\n}");
                programStyle(code);
            }
        });
        jpm.add(item);
        item = new JMenuItem("temporal sample5");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String code = ("//sample\n @author appiah\n @version 1.0\n@run pay_engage \n{\n" + "1:: action --> a='sell';\n" +
                        "2:: interest--> ia='sell_pc', ia1='buy_pc';\n" +
                        "3:: rank--> r='rank_pos12';\n" +
                        "4:: location--> loc='Accra-12N5E';\n" +
                        "5:: temporal--> temp='always!12/4/20->sometimes!14/05/21';\n" +
                        "6:: name='Ofankor-Engagement';\n}");
                programStyle(code);
            }
        });
        jpm.add(item);
        item = new JMenuItem("special sample6");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                declareDoc("Special Sample 6", "appiah", "pay_engage");
                declareBrac("{");
                declareAnnot("special");
                declareText("enact_E(a,l,t)-->enact_L(i1,i2,r);");
                declareStat("", "name", "special_agent");
                declareBrac("}");
            // editor.setText("//sample\n @author appiah\n @version 1.0\n@run pay_engage \n{\n" + "" +
            //         " @special\n enact_E(a,l,t)-->enact_L(i1,i2,r);\n name=\"special_agent\";\n}");
            }
        });
        jpm.add(item);
        jpm.addSeparator();
        setKeyword("enact::name");
        setKeyword("enact::interest");
        setKeyword("enact::action");
        setKeyword("enact::temporal");
        setKeyword("enact::location");
        setKeyword("enact::rank");
        jpm.addSeparator();

        JMenu temMenu = new JMenu("Temporal Operators");
        setKeyword("since!", temMenu);
        setKeyword("before!", temMenu);
        setKeyword("after!", temMenu);
        setKeyword("at!", temMenu);
        setKeyword("always!", temMenu);
        setKeyword("never!", temMenu);
        setKeyword("sometimes!", temMenu);
        setKeyword("within!", temMenu);
        setKeyword("inbetween!", temMenu);
        setKeyword("maybe!", temMenu);
        setKeyword("eventually!", temMenu);

        temMenu.setVisible(true);
        jpm.add(temMenu);

        JMenu actions = new JMenu("Action");

        JMenuItem jmi1 = new JMenuItem("Format");
        jmi1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    programStyle(enactDoc.getText(0, enactDoc.getLength()));
                } catch (BadLocationException ex) {
                    Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        actions.add(jmi1);
        jmi1 = new JMenuItem("Run");
        jmi1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                run();
            }
        });
        actions.add(jmi1);
        actions.setVisible(true);

        jpm.add(actions);
        tpop = new JPopupMenu();

        tim = new JMenuItem("Assign to Editor[Enter]");

        tpop.add(tim);
        tpop.addSeparator();
    }

    private void declareText(String text) {
        insertText(text + "\n", Color.ORANGE);
    }

    private void declareAnnot(String name) {
        insertText("@" + name + "\n", Color.BLUE);
    }

    private void declareBrac(String op) {
        insertText("\n" + op + "\n", Color.ORANGE);
    }

    private void declareDoc(String comment, String author, String runname) {
        insertText("//" + comment + "\n", Color.BLUE);
        insertText("@author " + author + "\n@version 1.0\n", Color.BLUE);
        insertText("@run", Color.MAGENTA);
        insertText(" " + runname, Color.GREEN);
    }

    private void declarePartialStat(String type, String var, String value) {
        insertText(type + " ", Color.CYAN);
        insertText(var + "=", Color.PINK);
        insertText("\"" + value + "\"", Color.YELLOW);
    }

    private void declareSparator() {
        insertText(", ", Color.GRAY);
    }

    private void declareEnd() {
        insertText(";\n", Color.GRAY);
    }

    private void declareStat(String type, String var, String value) {
        insertText(type + " ", Color.CYAN);
        insertText(var + "=", Color.PINK);
        insertText("\"" + value + "\"", Color.YELLOW);
        insertText(";\n", Color.GRAY);
    }

    private void insertText(String text, Color color) {
        try {
            SimpleAttributeSet sas = new SimpleAttributeSet();
            sas.addAttribute(StyleConstants.Foreground, color);
            int fir = doc.getLength();
            doc.insertString(editor.getCaretPosition(), text, sas);
            enactDoc.setCharacterAttributes(fir, doc.getLength(), sas, true);
        } catch (BadLocationException ex) {
            Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void insertText(String text, Color color, int offset) {
        try {
            SimpleAttributeSet sas = new SimpleAttributeSet();
            sas.addAttribute(StyleConstants.Foreground, color);
            int fir = doc.getLength();
            doc.insertString(offset, text, sas);
            enactDoc.setCharacterAttributes(offset, doc.getLength(), sas, true);
        } catch (BadLocationException ex) {
            Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void setKeyword(final String key) {
        JMenuItem item = new JMenuItem(key);
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    SimpleAttributeSet sas = new SimpleAttributeSet();
                    int p = 0;
                    for (p = 0; p < keywords.length; p++) {
                        if (keywords[p].contains(key)) {
                            break;
                        }
                    }
                    sas.addAttribute(StyleConstants.Foreground, keyColors[p]);

                    String text = editor.getText(0, caretPos);
                    doc.insertString(editor.getCaretPosition(), key, sas);
                    enactDoc.setCharacterAttributes(editor.getCaretPosition(), doc.getLength(), sas, true);

                    sas.addAttribute(StyleConstants.Foreground, Color.PINK);
                    String var = key.replace("enact::", " ");
                    var = var.replace(var.substring(0, var.length() / 3 + 1), " ");
                    doc.insertString(editor.getCaretPosition(), var, sas);
                    enactDoc.setCharacterAttributes(editor.getCaretPosition(), doc.getLength(), sas, true);

                    sas.addAttribute(StyleConstants.Foreground, Color.PINK);
                    doc.insertString(editor.getCaretPosition(), "=", sas);
                    enactDoc.setCharacterAttributes(editor.getCaretPosition(), doc.getLength(), sas, true);

                    sas.addAttribute(StyleConstants.Foreground, Color.YELLOW);
                    doc.insertString(editor.getCaretPosition(), "\"\"", sas);
                    enactDoc.setCharacterAttributes(editor.getCaretPosition(), doc.getLength(), sas, true);

                    if (key.contains("interest")) {
                        sas.addAttribute(StyleConstants.Foreground, Color.GRAY);
                        doc.insertString(editor.getCaretPosition(), ", ", sas);
                        enactDoc.setCharacterAttributes(editor.getCaretPosition(), doc.getLength(), sas, true);

                        sas.addAttribute(StyleConstants.Foreground, Color.PINK);
                        var = var.replace(var.substring(0, var.length() / 2 + 1), " ");
                        doc.insertString(editor.getCaretPosition(), var, sas);
                        enactDoc.setCharacterAttributes(editor.getCaretPosition(), doc.getLength(), sas, true);

                        sas.addAttribute(StyleConstants.Foreground, Color.PINK);
                        doc.insertString(editor.getCaretPosition(), "=", sas);
                        enactDoc.setCharacterAttributes(editor.getCaretPosition(), doc.getLength(), sas, true);

                        sas.addAttribute(StyleConstants.Foreground, Color.YELLOW);
                        doc.insertString(editor.getCaretPosition(), "\"\"", sas);
                        enactDoc.setCharacterAttributes(editor.getCaretPosition(), doc.getLength(), sas, true);
                    }
                    sas.addAttribute(StyleConstants.Foreground, Color.GRAY);
                    doc.insertString(editor.getCaretPosition(), ";", sas);
                    enactDoc.setCharacterAttributes(editor.getCaretPosition(), doc.getLength(), sas, true);

                } catch (BadLocationException ex) {
                    Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        jpm.add(item);

    }

    private void setKeyword(final String key, JMenu menu) {
        JMenuItem item = new JMenuItem(key);
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    SimpleAttributeSet sas = new SimpleAttributeSet();
                    sas.addAttribute(StyleConstants.Foreground, Color.YELLOW);

                    doc.insertString(editor.getCaretPosition(), key, sas);
                    enactDoc.setCharacterAttributes(editor.getCaretPosition(), key.length(), sas, true);

                } catch (BadLocationException ex) {
                    Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        menu.add(item);

    }

    private void editorChangeAction(final String nitem) {
        tim = new JMenuItem(nitem);
        tim.setActionCommand(editor.getText());
        tim.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    SimpleAttributeSet sas = new SimpleAttributeSet();
                    sas.addAttribute(StyleConstants.Foreground, Color.BLUE);
                    enactDoc.insertString(0, e.getActionCommand(), sas);
                    enactDoc.setCharacterAttributes(editor.getCaretPosition(), enactDoc.getLength(), sas, true);
                    parse();
                    editor.setText("");
                    declareDoc(comment, author, runname);
                    declareBrac("{");
                    declareStat("enact::action", "ac", action);
                    declareStat("enact::location", "loc", location);
                    declareStat("enact::temporal", "time", temporal);
                    declareStat("enact::rank", "ra", rank);
                    declarePartialStat("enact::interest", "inter", interest);
                    declareSparator();
                    declarePartialStat("", "inter1", interest1);
                    declareEnd();
                    declareStat("enact::name", "situation", name);

                    declareBrac("}");
                } catch (BadLocationException ex) {
                    Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        tpop.add(tim);
        tpop.validate();
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = LeeEditorApp.getApplication().getMainFrame();
            aboutBox = new LeeEditorAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        LeeEditorApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        editor = new javax.swing.JEditorPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        varText = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        exetable = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        codeTabPane = new javax.swing.JTabbedPane();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jScrollPane7 = new javax.swing.JScrollPane();
        enactH = new javax.swing.JTextPane();
        jScrollPane8 = new javax.swing.JScrollPane();
        enactageH = new javax.swing.JTextPane();
        jScrollPane9 = new javax.swing.JScrollPane();
        enactELH = new javax.swing.JTextPane();
        jScrollPane10 = new javax.swing.JScrollPane();
        interestactH = new javax.swing.JTextPane();
        jScrollPane11 = new javax.swing.JScrollPane();
        isimpliesH = new javax.swing.JTextPane();
        jScrollPane12 = new javax.swing.JScrollPane();
        locationH = new javax.swing.JTextPane();
        jScrollPane13 = new javax.swing.JScrollPane();
        temporalH = new javax.swing.JTextPane();
        jScrollPane14 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jTabbedPane4 = new javax.swing.JTabbedPane();
        jScrollPane15 = new javax.swing.JScrollPane();
        enactCC = new javax.swing.JTextPane();
        jScrollPane16 = new javax.swing.JScrollPane();
        enactageCC = new javax.swing.JTextPane();
        jScrollPane17 = new javax.swing.JScrollPane();
        enactELCC = new javax.swing.JTextPane();
        jScrollPane18 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jScrollPane19 = new javax.swing.JScrollPane();
        jTextPane3 = new javax.swing.JTextPane();
        jScrollPane20 = new javax.swing.JScrollPane();
        jTextPane4 = new javax.swing.JTextPane();
        jScrollPane21 = new javax.swing.JScrollPane();
        jTextPane5 = new javax.swing.JTextPane();
        jScrollPane25 = new javax.swing.JScrollPane();
        translateTxt = new javax.swing.JTextPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTabbedPane5 = new javax.swing.JTabbedPane();
        jTabbedPane6 = new javax.swing.JTabbedPane();
        jScrollPane22 = new javax.swing.JScrollPane();
        memoryTxt = new javax.swing.JTextPane();
        jScrollPane23 = new javax.swing.JScrollPane();
        jTextPane7 = new javax.swing.JTextPane();
        jScrollPane24 = new javax.swing.JScrollPane();
        heapDumpTxt = new javax.swing.JTextPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        output = new javax.swing.JEditorPane();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        newBtn = new javax.swing.JMenuItem();
        openBtn = new javax.swing.JMenuItem();
        saveBtn = new javax.swing.JMenuItem();
        saveOutBtn = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        runMenu = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        editMenu = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        formatMenu = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        undoAction = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        runBtn = new javax.swing.JButton();
        clearBtn = new javax.swing.JButton();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(leesystem.LeeEditorApp.class).getContext().getResourceMap(LeeEditorView.class);
        mainPanel.setBackground(resourceMap.getColor("mainPanel.background")); // NOI18N
        mainPanel.setName("mainPanel"); // NOI18N

        jSplitPane1.setDividerLocation(189);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jTabbedPane1.setBackground(resourceMap.getColor("jTabbedPane1.background")); // NOI18N
        jTabbedPane1.setForeground(resourceMap.getColor("jTabbedPane1.foreground")); // NOI18N
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        editor.setBackground(resourceMap.getColor("editor.background")); // NOI18N
        editor.setContentType(resourceMap.getString("editor.contentType")); // NOI18N
        editor.setForeground(resourceMap.getColor("editor.foreground")); // NOI18N
        editor.setToolTipText(resourceMap.getString("editor.toolTipText")); // NOI18N
        editor.setName("editor"); // NOI18N
        editor.setSelectedTextColor(resourceMap.getColor("editor.selectedTextColor")); // NOI18N
        editor.setSelectionColor(resourceMap.getColor("editor.selectionColor")); // NOI18N
        editor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                editorMousePressed(evt);
            }
        });
        editor.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                editorCaretUpdate(evt);
            }
        });
        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                editorKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                editorKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(editor);

        jTabbedPane1.addTab(resourceMap.getString("jScrollPane2.TabConstraints.tabTitle"), jScrollPane2); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridLayout(1, 2, 5, 0));

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        varText.setBackground(resourceMap.getColor("varText.background")); // NOI18N
        varText.setColumns(20);
        varText.setEditable(false);
        varText.setFont(resourceMap.getFont("varText.font")); // NOI18N
        varText.setForeground(resourceMap.getColor("varText.foreground")); // NOI18N
        varText.setRows(5);
        varText.setText(editor.getText());
        varText.setName("varText"); // NOI18N
        jScrollPane4.setViewportView(varText);

        jPanel1.add(jScrollPane4);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        exetable.setBackground(resourceMap.getColor("exetable.background")); // NOI18N
        exetable.setForeground(resourceMap.getColor("exetable.foreground")); // NOI18N
        exetable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Run Name", "Code Name", "Version", "Execution Time", "Author"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        exetable.setName("exetable"); // NOI18N
        exetable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                EditorValueHandler(evt);
            }
        });
        jScrollPane3.setViewportView(exetable);

        jPanel1.add(jScrollPane3);

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        jTabbedPane2.setBackground(resourceMap.getColor("jTabbedPane2.background")); // NOI18N
        jTabbedPane2.setForeground(resourceMap.getColor("jTabbedPane2.foreground")); // NOI18N
        jTabbedPane2.setName("jTabbedPane2"); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1058, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1000, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        codeTabPane.setBackground(resourceMap.getColor("codeTabPane.background")); // NOI18N
        codeTabPane.setForeground(resourceMap.getColor("codeTabPane.foreground")); // NOI18N
        codeTabPane.setTabPlacement(javax.swing.JTabbedPane.RIGHT);
        codeTabPane.setName("codeTabPane"); // NOI18N

        jTabbedPane3.setBackground(resourceMap.getColor("jTabbedPane3.background")); // NOI18N
        jTabbedPane3.setForeground(resourceMap.getColor("jTabbedPane3.foreground")); // NOI18N
        jTabbedPane3.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        jTabbedPane3.setName("jTabbedPane3"); // NOI18N

        jScrollPane7.setName("jScrollPane7"); // NOI18N

        enactH.setBackground(resourceMap.getColor("enactH.background")); // NOI18N
        enactH.setText(resourceMap.getString("enactH.text")); // NOI18N
        enactH.setName("enactH"); // NOI18N
        jScrollPane7.setViewportView(enactH);

        jTabbedPane3.addTab(resourceMap.getString("jScrollPane7.TabConstraints.tabTitle"), jScrollPane7); // NOI18N

        jScrollPane8.setName("jScrollPane8"); // NOI18N

        enactageH.setBackground(resourceMap.getColor("enactageH.background")); // NOI18N
        enactageH.setText(resourceMap.getString("enactageH.text")); // NOI18N
        enactageH.setName("enactageH"); // NOI18N
        jScrollPane8.setViewportView(enactageH);

        jTabbedPane3.addTab(resourceMap.getString("jScrollPane8.TabConstraints.tabTitle"), jScrollPane8); // NOI18N

        jScrollPane9.setName("jScrollPane9"); // NOI18N

        enactELH.setBackground(resourceMap.getColor("enactELH.background")); // NOI18N
        enactELH.setText(resourceMap.getString("enactELH.text")); // NOI18N
        enactELH.setName("enactELH"); // NOI18N
        jScrollPane9.setViewportView(enactELH);

        jTabbedPane3.addTab(resourceMap.getString("jScrollPane9.TabConstraints.tabTitle"), jScrollPane9); // NOI18N

        jScrollPane10.setName("jScrollPane10"); // NOI18N

        interestactH.setBackground(resourceMap.getColor("interestactH.background")); // NOI18N
        interestactH.setText(resourceMap.getString("interestactH.text")); // NOI18N
        interestactH.setName("interestactH"); // NOI18N
        jScrollPane10.setViewportView(interestactH);

        jTabbedPane3.addTab(resourceMap.getString("jScrollPane10.TabConstraints.tabTitle"), jScrollPane10); // NOI18N

        jScrollPane11.setName("jScrollPane11"); // NOI18N

        isimpliesH.setBackground(resourceMap.getColor("isimpliesH.background")); // NOI18N
        isimpliesH.setText(resourceMap.getString("isimpliesH.text")); // NOI18N
        isimpliesH.setName("isimpliesH"); // NOI18N
        jScrollPane11.setViewportView(isimpliesH);

        jTabbedPane3.addTab(resourceMap.getString("jScrollPane11.TabConstraints.tabTitle"), jScrollPane11); // NOI18N

        jScrollPane12.setName("jScrollPane12"); // NOI18N

        locationH.setBackground(resourceMap.getColor("locationH.background")); // NOI18N
        locationH.setText(resourceMap.getString("locationH.text")); // NOI18N
        locationH.setName("locationH"); // NOI18N
        jScrollPane12.setViewportView(locationH);

        jTabbedPane3.addTab(resourceMap.getString("jScrollPane12.TabConstraints.tabTitle"), jScrollPane12); // NOI18N

        jScrollPane13.setName("jScrollPane13"); // NOI18N

        temporalH.setBackground(resourceMap.getColor("temporalH.background")); // NOI18N
        temporalH.setText(resourceMap.getString("temporalH.text")); // NOI18N
        temporalH.setName("temporalH"); // NOI18N
        jScrollPane13.setViewportView(temporalH);

        jTabbedPane3.addTab(resourceMap.getString("jScrollPane13.TabConstraints.tabTitle"), jScrollPane13); // NOI18N

        jScrollPane14.setName("jScrollPane14"); // NOI18N

        jTextPane1.setBackground(resourceMap.getColor("jTextPane1.background")); // NOI18N
        jTextPane1.setText(resourceMap.getString("jTextPane1.text")); // NOI18N
        jTextPane1.setName("jTextPane1"); // NOI18N
        jScrollPane14.setViewportView(jTextPane1);

        jTabbedPane3.addTab(resourceMap.getString("jScrollPane14.TabConstraints.tabTitle"), jScrollPane14); // NOI18N

        codeTabPane.addTab(resourceMap.getString("jTabbedPane3.TabConstraints.tabTitle"), jTabbedPane3); // NOI18N

        jTabbedPane4.setBackground(resourceMap.getColor("jTabbedPane4.background")); // NOI18N
        jTabbedPane4.setForeground(resourceMap.getColor("jTabbedPane4.foreground")); // NOI18N
        jTabbedPane4.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        jTabbedPane4.setName("jTabbedPane4"); // NOI18N

        jScrollPane15.setName("jScrollPane15"); // NOI18N

        enactCC.setBackground(resourceMap.getColor("enactCC.background")); // NOI18N
        enactCC.setText(resourceMap.getString("enactCC.text")); // NOI18N
        enactCC.setName("enactCC"); // NOI18N
        jScrollPane15.setViewportView(enactCC);

        jTabbedPane4.addTab(resourceMap.getString("jScrollPane15.TabConstraints.tabTitle"), jScrollPane15); // NOI18N

        jScrollPane16.setName("jScrollPane16"); // NOI18N

        enactageCC.setBackground(resourceMap.getColor("enactageCC.background")); // NOI18N
        enactageCC.setText(resourceMap.getString("enactageCC.text")); // NOI18N
        enactageCC.setName("enactageCC"); // NOI18N
        jScrollPane16.setViewportView(enactageCC);

        jTabbedPane4.addTab(resourceMap.getString("jScrollPane16.TabConstraints.tabTitle"), jScrollPane16); // NOI18N

        jScrollPane17.setName("jScrollPane17"); // NOI18N

        enactELCC.setBackground(resourceMap.getColor("enactELCC.background")); // NOI18N
        enactELCC.setText(resourceMap.getString("enactELCC.text")); // NOI18N
        enactELCC.setName("enactELCC"); // NOI18N
        jScrollPane17.setViewportView(enactELCC);

        jTabbedPane4.addTab(resourceMap.getString("jScrollPane17.TabConstraints.tabTitle"), jScrollPane17); // NOI18N

        jScrollPane18.setName("jScrollPane18"); // NOI18N

        jTextPane2.setBackground(resourceMap.getColor("jTextPane2.background")); // NOI18N
        jTextPane2.setText(resourceMap.getString("jTextPane2.text")); // NOI18N
        jTextPane2.setName("jTextPane2"); // NOI18N
        jScrollPane18.setViewportView(jTextPane2);

        jTabbedPane4.addTab(resourceMap.getString("jScrollPane18.TabConstraints.tabTitle"), jScrollPane18); // NOI18N

        jScrollPane19.setName("jScrollPane19"); // NOI18N

        jTextPane3.setBackground(resourceMap.getColor("jTextPane3.background")); // NOI18N
        jTextPane3.setText(resourceMap.getString("jTextPane3.text")); // NOI18N
        jTextPane3.setName("jTextPane3"); // NOI18N
        jScrollPane19.setViewportView(jTextPane3);

        jTabbedPane4.addTab(resourceMap.getString("jScrollPane19.TabConstraints.tabTitle"), jScrollPane19); // NOI18N

        jScrollPane20.setName("jScrollPane20"); // NOI18N

        jTextPane4.setBackground(resourceMap.getColor("jTextPane4.background")); // NOI18N
        jTextPane4.setText(resourceMap.getString("jTextPane4.text")); // NOI18N
        jTextPane4.setName("jTextPane4"); // NOI18N
        jScrollPane20.setViewportView(jTextPane4);

        jTabbedPane4.addTab(resourceMap.getString("jScrollPane20.TabConstraints.tabTitle"), jScrollPane20); // NOI18N

        jScrollPane21.setName("jScrollPane21"); // NOI18N

        jTextPane5.setBackground(resourceMap.getColor("jTextPane5.background")); // NOI18N
        jTextPane5.setText(resourceMap.getString("jTextPane5.text")); // NOI18N
        jTextPane5.setName("jTextPane5"); // NOI18N
        jScrollPane21.setViewportView(jTextPane5);

        jTabbedPane4.addTab(resourceMap.getString("jScrollPane21.TabConstraints.tabTitle"), jScrollPane21); // NOI18N

        codeTabPane.addTab(resourceMap.getString("jTabbedPane4.TabConstraints.tabTitle"), jTabbedPane4); // NOI18N

        jTabbedPane2.addTab(resourceMap.getString("codeTabPane.TabConstraints.tabTitle"), codeTabPane); // NOI18N

        jScrollPane25.setName("jScrollPane25"); // NOI18N

        translateTxt.setBackground(resourceMap.getColor("translateTxt.background")); // NOI18N
        translateTxt.setForeground(resourceMap.getColor("translateTxt.foreground")); // NOI18N
        translateTxt.setName("translateTxt"); // NOI18N
        translateTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                translateTxtMousePressed(evt);
            }
        });
        jScrollPane25.setViewportView(translateTxt);

        jTabbedPane2.addTab(resourceMap.getString("jScrollPane25.TabConstraints.tabTitle"), jScrollPane25); // NOI18N

        jScrollPane5.setViewportView(jTabbedPane2);

        jTabbedPane1.addTab(resourceMap.getString("jScrollPane5.TabConstraints.tabTitle"), jScrollPane5); // NOI18N

        jScrollPane6.setName("jScrollPane6"); // NOI18N

        jTabbedPane5.setName("jTabbedPane5"); // NOI18N

        jTabbedPane6.setBackground(resourceMap.getColor("jTabbedPane6.background")); // NOI18N
        jTabbedPane6.setForeground(resourceMap.getColor("jTabbedPane6.foreground")); // NOI18N
        jTabbedPane6.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        jTabbedPane6.setName("jTabbedPane6"); // NOI18N

        jScrollPane22.setName("jScrollPane22"); // NOI18N

        memoryTxt.setBackground(resourceMap.getColor("memoryTxt.background")); // NOI18N
        memoryTxt.setName("memoryTxt"); // NOI18N
        jScrollPane22.setViewportView(memoryTxt);

        jTabbedPane6.addTab(resourceMap.getString("jScrollPane22.TabConstraints.tabTitle"), jScrollPane22); // NOI18N

        jScrollPane23.setName("jScrollPane23"); // NOI18N

        jTextPane7.setName("jTextPane7"); // NOI18N
        jScrollPane23.setViewportView(jTextPane7);

        jTabbedPane6.addTab(resourceMap.getString("jScrollPane23.TabConstraints.tabTitle"), jScrollPane23); // NOI18N

        jScrollPane24.setName("jScrollPane24"); // NOI18N

        heapDumpTxt.setName("heapDumpTxt"); // NOI18N
        jScrollPane24.setViewportView(heapDumpTxt);

        jTabbedPane6.addTab(resourceMap.getString("jScrollPane24.TabConstraints.tabTitle"), jScrollPane24); // NOI18N

        jTabbedPane5.addTab(resourceMap.getString("jTabbedPane6.TabConstraints.tabTitle"), jTabbedPane6); // NOI18N

        jScrollPane6.setViewportView(jTabbedPane5);

        jTabbedPane1.addTab(resourceMap.getString("jScrollPane6.TabConstraints.tabTitle"), jScrollPane6); // NOI18N

        jSplitPane1.setLeftComponent(jTabbedPane1);

        jScrollPane1.setFont(resourceMap.getFont("jScrollPane1.font")); // NOI18N
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        output.setBackground(resourceMap.getColor("output.background")); // NOI18N
        output.setFont(resourceMap.getFont("output.font")); // NOI18N
        output.setForeground(resourceMap.getColor("output.foreground")); // NOI18N
        output.setText(resourceMap.getString("output.text")); // NOI18N
        output.setToolTipText(resourceMap.getString("output.toolTipText")); // NOI18N
        output.setName("output"); // NOI18N
        output.setSelectedTextColor(resourceMap.getColor("output.selectedTextColor")); // NOI18N
        output.setSelectionColor(resourceMap.getColor("output.selectionColor")); // NOI18N
        jScrollPane1.setViewportView(output);

        jSplitPane1.setRightComponent(jScrollPane1);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                .addContainerGap())
        );

        menuBar.setBackground(resourceMap.getColor("menuBar.background")); // NOI18N
        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setBackground(resourceMap.getColor("fileMenu.background")); // NOI18N
        fileMenu.setForeground(resourceMap.getColor("fileMenu.foreground")); // NOI18N
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        newBtn.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newBtn.setText(resourceMap.getString("newBtn.text")); // NOI18N
        newBtn.setToolTipText(resourceMap.getString("newBtn.toolTipText")); // NOI18N
        newBtn.setName("newBtn"); // NOI18N
        newBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newBtnActionPerformed(evt);
            }
        });
        fileMenu.add(newBtn);

        openBtn.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openBtn.setText(resourceMap.getString("openBtn.text")); // NOI18N
        openBtn.setToolTipText(resourceMap.getString("openBtn.toolTipText")); // NOI18N
        openBtn.setName("openBtn"); // NOI18N
        openBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openBtnActionPerformed(evt);
            }
        });
        fileMenu.add(openBtn);

        saveBtn.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK));
        saveBtn.setText(resourceMap.getString("saveBtn.text")); // NOI18N
        saveBtn.setToolTipText(resourceMap.getString("saveBtn.toolTipText")); // NOI18N
        saveBtn.setName("saveBtn"); // NOI18N
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });
        fileMenu.add(saveBtn);

        saveOutBtn.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK));
        saveOutBtn.setText(resourceMap.getString("saveOutBtn.text")); // NOI18N
        saveOutBtn.setToolTipText(resourceMap.getString("saveOutBtn.toolTipText")); // NOI18N
        saveOutBtn.setName("saveOutBtn"); // NOI18N
        saveOutBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveOutBtnActionPerformed(evt);
            }
        });
        fileMenu.add(saveOutBtn);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.SHIFT_MASK));
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setToolTipText(resourceMap.getString("jMenuItem1.toolTipText")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem1);

        runMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        runMenu.setText(resourceMap.getString("runMenu.text")); // NOI18N
        runMenu.setToolTipText(resourceMap.getString("runMenu.toolTipText")); // NOI18N
        runMenu.setName("runMenu"); // NOI18N
        runMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runMenuActionPerformed(evt);
            }
        });
        fileMenu.add(runMenu);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(leesystem.LeeEditorApp.class).getContext().getActionMap(LeeEditorView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setForeground(resourceMap.getColor("jMenu1.foreground")); // NOI18N
        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        editMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        editMenu.setText(resourceMap.getString("editMenu.text")); // NOI18N
        editMenu.setToolTipText(resourceMap.getString("editMenu.toolTipText")); // NOI18N
        editMenu.setName("editMenu"); // NOI18N
        editMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuActionPerformed(evt);
            }
        });
        jMenu1.add(editMenu);

        jMenuItem3.setAction(new DefaultEditorKit.CopyAction ());
        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenu1.add(jMenuItem3);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenu1.add(jMenuItem4);

        formatMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        formatMenu.setText(resourceMap.getString("formatMenu.text")); // NOI18N
        formatMenu.setToolTipText(resourceMap.getString("formatMenu.toolTipText")); // NOI18N
        formatMenu.setName("formatMenu"); // NOI18N
        formatMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatMenuActionPerformed(evt);
            }
        });
        jMenu1.add(formatMenu);

        jMenuItem5.setAction(new DefaultEditorKit.PasteAction());
        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        jMenu1.add(jMenuItem5);

        undoAction.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoAction.setText(resourceMap.getString("undoAction.text")); // NOI18N
        undoAction.setName("undoAction"); // NOI18N
        undoAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoActionActionPerformed(evt);
            }
        });
        jMenu1.add(undoAction);

        menuBar.add(jMenu1);

        helpMenu.setForeground(resourceMap.getColor("helpMenu.foreground")); // NOI18N
        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        runBtn.setBackground(resourceMap.getColor("runBtn.background")); // NOI18N
        runBtn.setForeground(resourceMap.getColor("runBtn.foreground")); // NOI18N
        runBtn.setText(resourceMap.getString("runBtn.text")); // NOI18N
        runBtn.setToolTipText(resourceMap.getString("runBtn.toolTipText")); // NOI18N
        runBtn.setName("runBtn"); // NOI18N
        runBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runBtnActionPerformed(evt);
            }
        });

        clearBtn.setBackground(resourceMap.getColor("clearBtn.background")); // NOI18N
        clearBtn.setText(resourceMap.getString("clearBtn.text")); // NOI18N
        clearBtn.setName("clearBtn"); // NOI18N
        clearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clearBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 136, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
            .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(statusPanelLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(runBtn)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(statusMessageLabel)
                            .addComponent(statusAnimationLabel)
                            .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3))
                    .addComponent(clearBtn)))
            .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(statusPanelLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(runBtn)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents
    private void runMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runMenuActionPerformed
        openFile = false;
        run();
        invElement();
        Thread th = new Thread(new Runnable() {

            public void run() {
                editorChangeAction(name);
            }
            });
        th.start();
       
    }//GEN-LAST:event_runMenuActionPerformed

    private void run() {
        try {
            parse();
            preprocessor("./leeinterpreter ");
            preprocessor("./leeinterpretercont ");
            preprocessor("./leeinterpreterfinal ");
        } catch (BadLocationException ex) {
            Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void invElement() {
        Document doc = editor.getDocument();
        Element ele = doc.getDefaultRootElement();
        int elec = ele.getElementCount();
        for (int k = 0; k < elec; k++) {

        }
    }

    private void setOutput(String text) {
        String data = output.getText();
        data += "\n" + text;
        output.setText(data);

    }

    private void runBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runBtnActionPerformed
        try {
            openFile = false;
            parse();
            preprocessor("./leeinterpreter ");
            preprocessor("./leeinterpretercont ");
            preprocessor("./leeinterpreterfinal ");
            Thread ttt = new Thread(new Runnable() {

                public void run() {
                    editorChangeAction(name);
                }
            });
            ttt.start();
        } catch (BadLocationException ex) {
            Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }//GEN-LAST:event_runBtnActionPerformed

    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnActionPerformed

        output.setText("");
    }//GEN-LAST:event_clearBtnActionPerformed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        FileOutputStream fos = null;


        File f = new File("lee.vai");
        JFileChooser ifc = new JFileChooser();
        ifc.setAcceptAllFileFilterUsed(true);
        FileFilter filter = new FileNameExtensionFilter("(V)ariable (A)ssignment (I)nterpretation", ".vai", "vai", "VAI");
        ifc.setFileFilter(filter);
        ifc.setApproveButtonText("Save vai");
        ifc.setApproveButtonToolTipText("New VAI Saving");
        ifc.setDialogTitle("Variable Assignment Interpretation[Save Mode]");
        ifc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int r = ifc.showSaveDialog(this.getComponent());
        if (r == JFileChooser.APPROVE_OPTION) {
            try {
                f = ifc.getSelectedFile();
                fos = new FileOutputStream(f);
                fos.write(enactDoc.getText(0, enactDoc.getLength()).getBytes());
                this.getFrame().setTitle("LEELus Editor [" + f.getName() + "]");
            } catch (BadLocationException ex) {
                Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        setOutput("Finished in saving vai file to system.");
    }//GEN-LAST:event_saveBtnActionPerformed

    private void openBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openBtnActionPerformed

        File f = new File("/");
        JFileChooser ifc = new JFileChooser();
        ifc.setAcceptAllFileFilterUsed(true);
        FileFilter filter = new FileNameExtensionFilter("(V)ariable (A)ssignment (I)nterpretation", ".vai", "vai", "VAI");
        ifc.setFileFilter(filter);
        ifc.setApproveButtonText("Assign");
        ifc.setApproveButtonToolTipText("New VAI Setting");
        ifc.setDialogTitle("Variable Assignment Interpretation");
        ifc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int r = ifc.showOpenDialog(this.getComponent());
        if (r == JFileChooser.APPROVE_OPTION) {
            FileInputStream fis = null;
            try {
                f = ifc.getSelectedFile();
                fis = new FileInputStream(f);
                byte data[] = new byte[fis.available()];
                fis.read(data);
                programStyle(new String(data));
                setOutput(f.toString() + " is read and set for running....");
                this.getFrame().setTitle("LEELus Editor [" + f.getName() + "]");

            } catch (IOException ex) {
                Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }//GEN-LAST:event_openBtnActionPerformed

    private void programStyle(String code) {
        try {
            SimpleAttributeSet sas = new SimpleAttributeSet();
            sas.addAttribute(StyleConstants.Foreground, Color.BLUE);
            enactDoc.insertString(0, new String(code), sas);
            enactDoc.setCharacterAttributes(editor.getCaretPosition(), enactDoc.getLength(), sas, true);
            openFile = true;
            parse();
            editor.setText("");
            declareDoc(comment, author, runname);
            declareBrac("{");
            declareStat("enact::action", "ac", action);
            declareStat("enact::location", "loc", location);
            declareStat("enact::temporal", "time", temporal);
            declareStat("enact::rank", "ra", rank);
            declarePartialStat("enact::interest", "inter", interest);
            declareSparator();
            declarePartialStat("", "inter1", interest1);
            declareEnd();
            declareStat("enact::name", "situation", name);

            declareBrac("}");
        } catch (BadLocationException ex) {
            Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void saveOutBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveOutBtnActionPerformed
        FileOutputStream fos = null;


        File f = new File("out.lee");
        JFileChooser ifc = new JFileChooser();
        ifc.setAcceptAllFileFilterUsed(true);
        FileFilter filter = new FileNameExtensionFilter("(L)ogical (E)nactment (E)nterpretation", ".lee", "lee", "LEE");
        ifc.setFileFilter(filter);
        ifc.setApproveButtonText("Save lee");
        ifc.setApproveButtonToolTipText("New LEE Output Saving");
        ifc.setDialogTitle("Logical Enactment Interpretation[Save Mode]");
        ifc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int r = ifc.showSaveDialog(this.getComponent());
        if (r == JFileChooser.APPROVE_OPTION) {
            try {
                f = ifc.getSelectedFile();
                fos = new FileOutputStream(f);
                fos.write(output.getText().getBytes());
                this.getFrame().setTitle("LEELus Editor [" + f.getName() + "]");

            } catch (IOException ex) {
                Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        setOutput("Finished in saving lee output file to system....");
   
    }//GEN-LAST:event_saveOutBtnActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        FileInputStream fos = null;


        File f = new File("out.lee");
        JFileChooser ifc = new JFileChooser();
        ifc.setAcceptAllFileFilterUsed(true);
        FileFilter filter = new FileNameExtensionFilter("(L)ogical (E)nactment (I)nterpretation", ".lee", "lee", "LEE");
        ifc.setFileFilter(filter);
        ifc.setApproveButtonText("Open LEE out");
        ifc.setApproveButtonToolTipText("New LEE Output Opening");
        ifc.setDialogTitle("Logical Enactment Interpretation[Open Mode]");
        ifc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int r = ifc.showOpenDialog(this.getComponent());
        if (r == JFileChooser.APPROVE_OPTION) {
            try {
                f = ifc.getSelectedFile();
                fos = new FileInputStream(f);
                byte data[] = new byte[fos.available()];
                fos.read(data);
                output.setText(new String(data));
                this.getFrame().setTitle("LEELus Editor [" + f.getName() + "]");

            } catch (IOException ex) {
                Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        setOutput("Reading LEE output file into LEE Editor....");
        
        
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void newBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newBtnActionPerformed

        output.setText("");
        editor.setText("");
        varText.setText("");
        exetable.setModel(new DefaultTableModel(new Object[]{"Run Name", "Code Name", "Version", "Execution Time", "Author"}, 50));
        String code = ("//comment\n//Assign values to variables.\n @author yourname\n @version 1.0\n@run runname \n{\n" + "enact::action a=\"\";\n" +
                "enact::interest ia=\"\", ia1=\"\";\n" +
                "enact::rank r=\"\";\n" +
                "enact::location loc=\"\";\n" +
                "enact::temporal temp=\"\";\n" +
                "enact::name=\"\";\n}");
        programStyle(code);
           
    }//GEN-LAST:event_newBtnActionPerformed

    private void editorKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_editorKeyPressed

        if (evt.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
            jpm.show(this.getComponent(), editor.getLocation().x + 50, editor.getLocation().y + 20);
        }
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            spacePos = editor.getCaretPosition();
            spaceEdit = true;
        }
        buffer.append(evt.getKeyChar());
        if (t == 0) {
            wordStart = editor.getCaret().getDot() - 1;
            System.out.println("Wordstart::" + wordStart);
        }
        t++;
    }//GEN-LAST:event_editorKeyPressed

    private void editorCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_editorCaretUpdate

        mark = evt.getMark();
        dotPos = evt.getDot();
        currDoc = editor.getText();
    }//GEN-LAST:event_editorCaretUpdate

    private void editorKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_editorKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            caretPos = editor.getCaretPosition();
        }
        if (evt.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (buffer.length() > 0) {
                buffer.deleteCharAt(buffer.length() - 1);
            }
        }

        // if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
        //   keyColor(new String(keyChars).trim(), wordStart);
        buffer.trimToSize();
        if (buffer.length() > 0) {
            keyColor(buffer.toString(), wordStart);
            keyChars = new String("").toCharArray();
            t = 0;
        }
       

    }//GEN-LAST:event_editorKeyReleased

    private void keyColor(String word, int offset) {
        int l = 0;
        System.out.println("word::" + word + "  offset::" + offset);
        for (l = 0; l < keywords.length; l++) {
            if (keywords[l].contains(word)) {
                //insertText("", Color.lightGray, offset);
                insertText(word, keyColors[l]);
                // insertText("", Color.lightGray);
                break;
            }
        }
    }

    private void EditorValueHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_EditorValueHandler
        tpop.show(exetable, exetable.getLocation().x + 40, exetable.getLocation().y + 20);
    }//GEN-LAST:event_EditorValueHandler

    private void editMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuActionPerformed
        if (editor.isEditable()) {
            editor.setEditable(false);
            editMenu.setText("Editable");
        } else {
            editMenu.setText("UnEditable");
            editor.setEditable(true);
        }
    }//GEN-LAST:event_editMenuActionPerformed

    private void undoActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoActionActionPerformed
        undoRed = true;
        undoableEditHappened(null); 
    }//GEN-LAST:event_undoActionActionPerformed

    private void translateTxtMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_translateTxtMousePressed
        if (evt.getButton() == MouseEvent.BUTTON3) {
            openFile = false;
            jmMenu.show(translateTxt, evt.getPoint().x, evt.getPoint().y);
        }     
    }//GEN-LAST:event_translateTxtMousePressed

    private void editorMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editorMousePressed
        if (evt.getButton() == MouseEvent.BUTTON3) {
            jpm.show(editor, evt.getPoint().x, evt.getPoint().y);
        }
    }//GEN-LAST:event_editorMousePressed

    private void formatMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatMenuActionPerformed
        try {
            programStyle(enactDoc.getText(0, enactDoc.getLength()));//GEN-LAST:event_formatMenuActionPerformed
        } catch (BadLocationException ex) {
            Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void preprocessor(String name) {
        try {
            Runtime run = Runtime.getRuntime();
            String env = "-a " + action + " -l " + location + " -i " + interest + ":" + interest1 +
                    " -t " + temporal + " -r " + rank + " -n " + name;
            Process p = run.exec(name + env.toString());
            buildMemory(run);
            InputStream is = p.getInputStream();
            byte[] databy = new byte[is.available()];
            int hpos = is.available();
            is.read(databy);
            String out = new String(databy, "US-ASCII");

            if (out.startsWith("LEE")) {
            //output.setCaretColor(new Color(23, 55, 66));
            // output.setCaretPosition(3);
            } else {

            }
            run.gc();
            hpos++;
            setOutput(out);

        } catch (IOException ex) {
            Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void buildMemory(Runtime run) {
        memoryTxt.setText("No of Processors::" + run.availableProcessors());
        memoryTxt.setText(memoryTxt.getText() + "\nMax Memory::" + run.maxMemory());
        memoryTxt.setText(memoryTxt.getText() + "\nFree Memory::" + run.freeMemory());
        memoryTxt.setText(memoryTxt.getText() + "\nUsed Memory::" + (run.totalMemory() - run.freeMemory()));
        memoryTxt.setText(memoryTxt.getText() + "\nTotal Memory::" + run.totalMemory());

    }

    private void parse() throws BadLocationException {
        String varis = enactDoc.getText(0, enactDoc.getLength());//= editor.getText();
        String[] linvar = varis.split(";");
        int pos = 0;
        String data;
        String var = null;

        if (linvar.length < 1) {
            setOutput("\nLine variables less than 6 parameter....");
        }
        if (linvar.length > 5) {
            for (int k = 0; k < linvar.length; k++) {

                var = linvar[k];
                if (var.contains("action")) {
                    pos = var.indexOf("=");
                    action = var.substring(pos + 2, var.length() - 1).trim();
                    varText.append("\naction::=" + action);
                } else if (var.contains("location")) {
                    pos = var.indexOf("=");
                    location = var.substring(pos + 2, var.length() - 1).trim();
                    varText.append("\nlocation::=" + location);
                } else if (var.contains("temporal")) {
                    pos = var.indexOf("=");
                    temporal = var.substring(pos + 2, var.length() - 1).trim();
                    varText.append("\ntemporal::=" + temporal);
                } else if (var.contains("interest")) {
                    String[] isn = var.split(",");
                    pos = isn[0].indexOf("=");
                    interest = isn[0].substring(pos + 2, isn[0].length() - 1).trim();
                    pos = isn[1].indexOf("=");
                    interest1 = isn[1].substring(pos + 2, isn[1].length() - 1).trim();
                    varText.append("\ninterest::=" + interest + ":" + interest1);
                } else if (var.contains("rank")) {
                    pos = var.indexOf("=");
                    rank = var.substring(pos + 2, var.length() - 1).trim();
                    varText.append("\nrank::=" + rank);
                } else if (var.contains("name")) {
                    pos = var.indexOf("=");
                    name = var.substring(pos + 2, var.length() - 1).trim();
                    varText.append("\nSitutation Name::=" + name);
                }
            }
        } else {
            if (linvar[0].contains("name")) {
                var = linvar[0];
                pos = var.indexOf("=");
                name = var.substring(pos + 2, var.length() - 1);
                varText.append("\nSitutation Name::=" + name);
                varis = linvar[1];
            } else {
                var = linvar[1];
                pos = var.indexOf("=");
                name = var.substring(pos + 2, var.length() - 1);
                name = name.replace("{", " ");
                varText.append("\nSitutation Name::=" + name);
                varis = linvar[0];
            }
            var = varis.substring(varis.lastIndexOf("@") + 8, varis.length());
            var = var.trim();
            System.out.println("Code::" + var);
            setOutput("Code::" + var);
            if (var.contains("enact_E")) {
                String[] el = var.split(">");
                el[0] = el[0].substring(0, el[0].length() - 2);
                System.out.println("P1::" + el[0]);
                setOutput("P1::" + el[0]);
                el[0] = el[0].substring(0, el[0].length() - 1);
                String[] eanl = el[0].split(",");
                action = eanl[0].replace("enact_E(", "").trim();
                location = eanl[1].trim();
                temporal = eanl[2].trim();
                varText.append("\naction::=" + action);
                varText.append("\ntemporal::=" + temporal);
                varText.append("\nlocation::=" + location);

                el[1] = el[1].substring(0, el[1].length());
                System.out.println("P2::" + el[1]);
                setOutput("P2::" + el[1] + "\n");
                el[1] = el[1].substring(0, el[1].length() - 1);

                eanl = el[1].split(",");
                interest = eanl[0].replace("enact_L(", "").trim();
                interest1 = eanl[1].trim();
                rank = eanl[2].trim();
                varText.append("\ninterest::=" + interest + ":" + interest1);
                varText.append("\nrank::=" + rank);

                special = true;
            } else {
                setOutput("\nNeeds to start with enact_E-->enact_L");
            }
        }

        output.setText(output.getText() + " Preprocessor::" + "./leeinterpreter ");

        varText.append("\n======================");
        linvar = varis.split("/");
        comment = "::";
        // for (int k = 0; k < linvar.length; k++) {
        var = varis;
        if (var.contains("//")) {
            pos = var.indexOf("/");
            int pos1 = var.indexOf("@");
            comment += var.substring(pos + 2, pos1 - 1);
        }

        editor.setToolTipText(comment);

        linvar = varis.split("@");
        for (int k = 0; k < linvar.length; k++) {
            var = linvar[k];
            if (var.contains("author")) {
                pos = var.indexOf("@");
                author = var.substring(pos + 8, var.length());

            } else if (var.contains("version")) {
                pos = var.indexOf("@");
                version = var.substring(pos + 9, var.length());

            } else if (var.contains("run")) {
                int pos1 = var.indexOf("@run");
                pos = var.indexOf("{");
                runname = var.substring(pos1 + 5, pos - 1);
            }
        }
        if (special) {
            translator = new EnactTranslator();
            String edit = translator.translate(comment + "\n//Code:://" + var.replace("special", " ").trim() + "\n", author, version, runname, action, location, temporal, interest, interest1, rank, name);
            translateTxt.setText(edit);
            special = false;
        }

        if (!openFile) {
            exetable.setValueAt(name, colin, 0);
            exetable.setValueAt(runname, colin, 1);
            exetable.setValueAt(version, colin, 2);
            exetable.setValueAt(new Date().toString(), colin, 3);
            exetable.setValueAt(author, colin, 4);
            colin++;
        }
        setOutput("Parameter parsing is prepared for preprocessor...\n");

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearBtn;
    private javax.swing.JTabbedPane codeTabPane;
    private javax.swing.JMenuItem editMenu;
    private javax.swing.JEditorPane editor;
    private javax.swing.JTextPane enactCC;
    private javax.swing.JTextPane enactELCC;
    private javax.swing.JTextPane enactELH;
    private javax.swing.JTextPane enactH;
    private javax.swing.JTextPane enactageCC;
    private javax.swing.JTextPane enactageH;
    private javax.swing.JTable exetable;
    private javax.swing.JMenuItem formatMenu;
    private javax.swing.JTextPane heapDumpTxt;
    private javax.swing.JTextPane interestactH;
    private javax.swing.JTextPane isimpliesH;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane20;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane23;
    private javax.swing.JScrollPane jScrollPane24;
    private javax.swing.JScrollPane jScrollPane25;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JTabbedPane jTabbedPane5;
    private javax.swing.JTabbedPane jTabbedPane6;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    private javax.swing.JTextPane jTextPane3;
    private javax.swing.JTextPane jTextPane4;
    private javax.swing.JTextPane jTextPane5;
    private javax.swing.JTextPane jTextPane7;
    private javax.swing.JTextPane locationH;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextPane memoryTxt;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newBtn;
    private javax.swing.JMenuItem openBtn;
    private javax.swing.JEditorPane output;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton runBtn;
    private javax.swing.JMenuItem runMenu;
    private javax.swing.JMenuItem saveBtn;
    private javax.swing.JMenuItem saveOutBtn;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTextPane temporalH;
    private javax.swing.JTextPane translateTxt;
    private javax.swing.JMenuItem undoAction;
    private javax.swing.JTextArea varText;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;

    public void insertUpdate(DocumentEvent e) {
        try {
            doc = (AbstractDocument) e.getDocument();
            statusMessageLabel.setText("Length::" + e.getLength());
            lastInsText = doc.getText(0, doc.getLength());
            insertbool = true;
            changebool = false;
            removebool = false;
        //  wordStart=e.getOffset();
        //  wordLength=e.getLength();
        //  keyColor(doc.getText(wordStart, wordLength), wordStart);
        } catch (BadLocationException ex) {
            Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeUpdate(DocumentEvent e) {
        try {
            insertbool = false;
            changebool = false;
            removebool = true;
            lastRemText = doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
            Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void changedUpdate(DocumentEvent e) {
        try {
            insertbool = false;
            changebool = true;
            removebool = false;
            doc = (AbstractDocument) e.getDocument();
            lastText = doc.getText(0, doc.getLength());
            statusMessageLabel.setText(e.getType().toString());
        } catch (BadLocationException ex) {
            Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void undoableEditHappened(UndoableEditEvent e) {
        // UndoableEdit edit = e.getEdit();
        if (undoRed) {
            // String[] words = lastText.split(" ");
            if (removebool) {
                editor.setText(lastInsText);
            } else if (changebool) {
                editor.setText(lastInsText);
            } else if (insertbool) {
                try {

                    if (unindex < doc.getLength()) {
                        editor.setText(editor.getText(0, editor.getCaretPosition() - unindex++));
                    } else {
                        unindex = 0;
                    }
                } catch (BadLocationException ex) {
                    Logger.getLogger(LeeEditorView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            undoRed = false;
        } else {
            if (removebool) {
                editor.setText(lastRemText);
            } else if (changebool) {
                editor.setText(lastText);
            } else if (insertbool) {
                editor.setText(currDoc.substring(0, editor.getCaretPosition() + unindex));
            }
            if (unindex > 0) {
                unindex--;
            }

            undoRed = true;
        }

    }
}
