package frames;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class CompressionFrame extends JFrame {

    private final int FRAME_WIDTH = 170;
    private final int FRAME_HEIGHT = 285;
    private final String PATH_DATA = "/Users/giuseppealtobelli/GitHub/secure-jpeg-compression/data";
    private final String PATH_SCRIPT = "/Users/giuseppealtobelli/GitHub/secure-jpeg-compression/script/compression";

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public CompressionFrame(){
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setResizable(false);
        createMainPanel();
    }

    public void createMainPanel(){
        JPanel panel = new JPanel();
        //panel.setLayout(new GridLayout(1, 1, 0, 14));
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel options = createOptionsPanel();
        JPanel indietro = createIndietroPanel();

        panel.add(options);
        panel.add(indietro, BorderLayout.SOUTH);

        add(panel);
    }

    public JPanel createOptionsPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1, 0, 5));

        ArrayList<String> strs = populateComboFileName();
        // Sort dell'arraylist
        strs.sort(String::compareToIgnoreCase);
        JComboBox combo = new JComboBox(strs.toArray());

        ArrayList<String> classifiers = populateComboClassifierName();
        // Sort dell'arraylist
        classifiers.sort(String::compareToIgnoreCase);
        JComboBox comboClassifiers = new JComboBox(classifiers.toArray());

        ArrayList<String> ciphers = populateComboCipherName();
        JComboBox comboCiphers = new JComboBox(ciphers.toArray());

        ArrayList<String> modes = populateComboModeName();
        JComboBox comboModes = new JComboBox(modes.toArray());

        TextField text = new TextField();
        text.setText("password");
        text.setBackground(Color.WHITE);
        JButton btn = new JButton("Compress");

        class clickButton implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                String img = PATH_DATA + "/imgs/in/" + combo.getSelectedItem().toString();
                String type = PATH_DATA + "/classifiers/haarcascade_" + comboClassifiers.getSelectedItem().toString() + ".xml";
                String mask = PATH_DATA + "/imgs/masks/" + comboClassifiers.getSelectedItem().toString() + "_mask.jpg";

                String password = text.getText();
                String cipher = comboCiphers.getSelectedItem().toString().toLowerCase();
                String mode = "none";
                if (!cipher.equals("chacha20")) {
                    mode = comboModes.getSelectedItem().toString().toLowerCase();
                }

                try {
                    if(checkTextLength(cipher, password) == true) {
                        compress(img, type, mask, cipher, mode, password);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        ActionListener listener = new clickButton();
        btn.addActionListener(listener);

        panel.add(combo);
        panel.add(comboClassifiers);
        panel.add(comboCiphers);
        panel.add(comboModes);
        panel.add(text);
        panel.add(btn);
        panel.setBorder(new TitledBorder(new EtchedBorder(), "Options"));

        return panel;
    }

    public JPanel createIndietroPanel(){
        JPanel panel = new JPanel();

        JButton btn = new JButton("Back");

        class clickButton implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                StartFrame frame = new StartFrame();
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                CompressionFrame.this.dispose();
            }
        }

        ActionListener listener = new clickButton();
        btn.addActionListener(listener);

        panel.add(btn);

        return panel;
    }

    public ArrayList<String> populateComboFileName() {
        File folder = new File(PATH_DATA + "/imgs/in");
        File[] files = folder.listFiles();
        ArrayList<String> classifiers = new ArrayList<>();

        if(files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    if(files[i] != null) {
                        classifiers.add(files[i].getName());
                    }
                }
            }
        } else {
            classifiers.add("empty");
        }
        return classifiers;
    }

    public ArrayList<String> populateComboClassifierName() {
        File folder = new File(PATH_DATA + "/classifiers");
        File[] files = folder.listFiles();
        ArrayList<String> names = new ArrayList<>();

        if(files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    if(files[i] != null) {
                        String classifier = files[i].getName().split("_")[1].split("\\.")[0];
                        names.add(classifier);
                    }
                }
            }
        } else {
            names.add("empty");
        }
        return names;
    }

    public ArrayList<String> populateComboCipherName() {
        ArrayList<String> ciphers = new ArrayList<>();
        ciphers.add("AES");
        ciphers.add("DES");
        return ciphers;
    }

    public ArrayList<String> populateComboModeName() {
        ArrayList<String> modes = new ArrayList<>();
        modes.add("CBC");
        modes.add("CFB");
        modes.add("ECB");
        modes.add("OFB");
        return modes;
    }

    public void cleanDirectory(String directory) {
        File folder = new File(directory);
        File[] files = folder.listFiles();

        if(files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    if(files[i] != null) {
                        files[i].delete();
                    }
                }
            }
        }
    }

    public boolean checkTextLength(String cipher, String password) {
        if(cipher.equals("des")) {
            if(password.length() > 8) {
                showDialog("DES password must be at most eigth characters long.");
                return false;
            }
            return true;
        }
        return true;
    }

    public void showDialog(String msg) {
        JOptionPane.showMessageDialog(
                null,
                msg,
                "Compression dialog",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    public void compress(String imgIn, String type, String mask, String cipher, String mode, String password) throws IOException {
        System.out.println("Start compression");

        long startTime = System.nanoTime();

        String coords = "";

        //Pulisco le cartelle di output
        cleanDirectory(PATH_DATA + "/imgs/out/processed");
        cleanDirectory(PATH_DATA + "/imgs/out/roi");
        cleanDirectory(PATH_DATA + "/imgs/out/secure");

        Mat matrixImgIn = Imgcodecs.imread(imgIn);
        // Copia dell'immagine di partenza per sottrarre le ROI in modo tale che non vengano sovrascritte dopo aver applicato la maschera
        Mat matrixImgInCopy = Imgcodecs.imread(imgIn);

        // Tento prima il riconoscimento dei volti
        CascadeClassifier classifier = new CascadeClassifier(type);
        MatOfRect detections = new MatOfRect();
        classifier.detectMultiScale(matrixImgIn, detections);

        //System.out.println(String.format("Detected %s ROI", detections.toArray().length));

        int i = 1;
        for (Rect rect : detections.toArray()) {

            Rect roi = new Rect(rect.x, rect.y, rect.width, rect.height);
            Mat matrixImgROI = matrixImgInCopy.submat(roi);

            /*
            // Decommentalo per disegnare anche il rettangolo intorno le ROI
            Imgproc.rectangle(
                    matrixImgIn,
                    new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0),
                    1);
            */

            // Formato della stringa con le informaizoni di ogni ROI: ID,X,Y
            // usiamo il carattere '-' per dividere le informazioni di ogni ROI
            coords += i + "," + rect.x + "," + rect.y + "-";

            Imgcodecs.imwrite(PATH_DATA + "/imgs/out/roi/" + i + ".jpg", matrixImgROI);
            Mat matrixMask = Imgcodecs.imread(mask);
            Mat matrixMaskResized = new Mat();
            Imgproc.resize(matrixMask, matrixMaskResized, new Size(rect.width, rect.height));
            Mat matrixImgSecure = matrixImgIn.submat(new Rect(rect.x, rect.y, matrixMaskResized.cols(), matrixMaskResized.rows()));
            matrixMaskResized.copyTo(matrixImgSecure);

            // Parametri per la gestione della qualità della compressione JPEG
            ArrayList<Integer> list = new ArrayList();
            list.add(Imgcodecs.IMWRITE_JPEG_QUALITY);
            list.add(85);
            MatOfInt params = new MatOfInt();
            params.fromList(list);

            Imgcodecs.imwrite(PATH_DATA + "/imgs/out/secure/secure.jpg", matrixImgIn, params);
            i++;
        }

        //Imgcodecs.imwrite(PATH_DATA + "/imgs/out/processed/processed.jpg", matrixImgIn);

        String script = PATH_SCRIPT + "/" + cipher + "/compression.py";
        if (!mode.equals("none")) {
            script = PATH_SCRIPT + "/" + cipher + "/compression" + "-" + mode + ".py";
        }
        String msgRtn = execPythonScript(script, PATH_DATA + "/imgs/out/secure/secure.jpg", coords, password);

        long estimatedTime = System.nanoTime() - startTime;
        float seconds = estimatedTime/1000000000F;
        DecimalFormat df = new DecimalFormat("#.###");

        showDialog("Detected " + detections.toArray().length + " ROI.\n" + msgRtn + "\nCompression done in " + df.format(seconds) + " seconds.");
        System.out.println("End compression");
    }

    public String execPythonScript(String script, String file, String coords, String passw) throws IOException {
        String[] args = new String[] { "/usr/local/anaconda3/envs/env_compr/bin/python", script, file, coords, passw };
        Process process = new ProcessBuilder(args).start();

        // Leggo il ritorno dello script in python
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return decodeReturn(in.readLine());
    }

    public String decodeReturn(String ret) {
        String ret_decoded = "";
        switch (ret) {
            case "0":
                ret_decoded =  "Metadata set correctly.";
                break;
            case "-1":
                ret_decoded = "IPTC metadata not set correctly.";
                break;
            case "-2":
                ret_decoded = "Exiv metadata not set correctly.";
                break;
            case "-3":
                ret_decoded = "IPTC and Exiv metadata not set correctly.";
                break;
            case "error":
                ret_decoded = "Metadata not set correctly.";
                break;
        }
        return ret_decoded;
    }
}
