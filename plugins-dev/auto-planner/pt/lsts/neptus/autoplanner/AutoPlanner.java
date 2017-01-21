package pt.lsts.neptus.autoplanner;

/**/
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import pt.lsts.neptus.console.ConsoleLayout;
import pt.lsts.neptus.console.ConsolePanel;
import pt.lsts.neptus.console.IConsoleInteraction;
import pt.lsts.neptus.console.notifications.Notification;
import pt.lsts.neptus.console.plugins.planning.MapPanel;
import pt.lsts.neptus.i18n.I18n;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.renderer2d.StateRenderer2D;

/**
 * @author Equipa C - SEAI 2016
 *
 */
@PluginDescription(name = "Auto Planner")
@SuppressWarnings("serial")

public class AutoPlanner extends ConsolePanel {

    /**
     * @param console
     * 
     * 
     */

    private JLabel widthIdValueLabel;
    private JLabel widthIdLabel;
    private JLabel heigthIdValueLabel;
    private JLabel heigthIdLabel;
    private JLabel ResIdValueLabel;
    private JLabel ResIdLabel;

    private JLabel AltIdValueLabel;
    private JLabel AltIdLabel;

    private JLabel stateValueLabel;
    private JLabel stateLabel;
    private JLabel nodeIdValueLabel;
    private JLabel nodeIdLabel;
    private JLabel FocIdValueLabel,FocIdLabel;
    private JComboBox<String> CamList;
    private JComboBox<String> ResList;
    private JComboBox<String> VeicList;
    private JSlider angleSlider;
    private JSlider GSDSlider;
    private JTextField focText, heigthSen, widthSen;
    JLabel AltLabel;

    private PolygonEditor editor = null;


    private JButton createPolygon, generatePlan, editPolygon, deleteEverything;

    //Variaveis globais para aceder à opçao escolhida
    public static String selectedCam, selectedVeic, selectedRes, height, FocusLength, SensWidth, SensHeigth;
    public static float distanciaRetas, resH, resV, Focal_len, SensWidth_float, SensHeigth_float, Altitud3, GSD, GSD_cm_px, angle;
    private static int altInt;





    public AutoPlanner(ConsoleLayout console) {
        super(console);
    }

    public static StateRenderer2D getRenderer(ConsoleLayout console) throws Exception {
        Vector<MapPanel> maps = console.getSubPanelsOfClass(MapPanel.class);
        if (maps.isEmpty())
            throw new Exception("There is no map in the console");
        return maps.firstElement().getRenderer();
    }

    @Override
    public void initSubPanel() {
        setSize(300, 300);
        this.setLayout(new MigLayout("ins 0"));

        //código adaptado do PLanControlStatePanel.java

        setSize(300, 300);
        this.setLayout(new MigLayout("ins 0"));



        stateValueLabel = new JLabel();
        stateValueLabel.setText("");
        stateLabel = new JLabel();
        stateLabel.setText("<html><b>" + I18n.text("Camera") + ": ");


        nodeIdValueLabel = new JLabel();
        nodeIdValueLabel.setText("");
        nodeIdLabel = new JLabel();
        nodeIdLabel.setText("<html><b>" + I18n.text("Vehicle") + ": ");

        this.add(nodeIdLabel);





        //ComboBox para veículo 

        String[] Veiculo = new String[] {"X8 SkyWalker", "Mariner"};

        VeicList = new JComboBox<>(Veiculo);
        add(VeicList);
        selectedVeic = (String) VeicList.getSelectedItem();
        if (selectedVeic == "X8 SkyWalker")
        {
            selectedVeic = "x8-02";                  

        } 
        else
            if (selectedVeic == "Mariner")
            {
                selectedVeic = "mariner-02";                     
            }

        this.add(nodeIdValueLabel, "wrap");

        ActionListener VeicActionListener = new ActionListener() {//add actionlistner to listen for change
            @Override
            public void actionPerformed(ActionEvent e) {

                selectedVeic = (String) VeicList.getSelectedItem();
                System.out.println("Veiculo selecionado: "+ selectedVeic);

                if (selectedVeic == "X8 SkyWalker")
                {
                    selectedVeic = "x8-02";                  

                } 
                else
                    if (selectedVeic == "Mariner")
                    {
                        selectedVeic = "mariner-02";                     
                    }

                calculateParameters();
                updateGSDSlider();


            }

        };

        VeicList.addActionListener(VeicActionListener);

        this.add(stateLabel, "");



        //ComboBox para Camera
        String[] Cam = new String[] {"Canon", "Go Pro", "Custom"};
        CamList = new JComboBox<>(Cam);
        add(CamList);
        selectedCam = (String) CamList.getSelectedItem();

        this.add(stateValueLabel, "wrap");

        ActionListener cbActionListener = new ActionListener() {//add actionlistner to listen for change
            @Override
            public void actionPerformed(ActionEvent e) {

                selectedCam = (String) CamList.getSelectedItem();
                System.out.println("camera selecionada: "+ selectedCam);

                if (selectedCam == "Go Pro")

                {
                    focText.setText("8");
                    widthSen.setText("5.76");
                    heigthSen.setText("4.29");


                    focText.setEnabled(false);
                    widthSen.setEnabled(false);
                    heigthSen.setEnabled(false);


                    //Criar Variaveis globais para guardar os dados

                    FocusLength = "8";
                    SensWidth = "5.76";
                    SensHeigth = "4.29";

                    //                    Focal_len = (float) 8;
                    //                    SensWidth_float = (float) 5.76;
                    //                    SensHeigth_float = (float) 4.29;


                } else 
                    if(selectedCam == "Canon")
                    {
                        focText.setText("28");
                        widthSen.setText("6.17");
                        heigthSen.setText("4.56");

                        FocusLength = "28";
                        SensWidth = "6.17";
                        SensHeigth = "4.56";

                        //                        Focal_len = (float) 28;
                        //                        SensWidth_float = (float) 6.17;
                        //                        SensHeigth_float = (float) 4.56;

                        focText.setEnabled(false);
                        widthSen.setEnabled(false);
                        heigthSen.setEnabled(false);


                        //Criar Variaveis globais para guardar os dados



                    }

                    else 

                        if(selectedCam == "Custom")
                        {
                            FocusLength = focText.getText();
                            SensWidth = widthSen.getText();
                            SensHeigth = heigthSen.getText();

                            //                            Focal_len = Float.valueOf(FocusLength);
                            //                            SensWidth_float = Float.valueOf(SensWidth);
                            //                            SensHeigth_float = Float.valueOf(SensHeigth);

                            focText.setEnabled(true);
                            widthSen.setEnabled(true);
                            heigthSen.setEnabled(true);



                        }


                calculateParameters();
                updateGSDSlider();





            }

        };

        CamList.addActionListener(cbActionListener);






        FocIdValueLabel = new JLabel();
        FocIdValueLabel.setText("");
        FocIdLabel = new JLabel();
        FocIdLabel.setText("<html><b>" + I18n.text("Focal Length (mm)") + ": ");

        this.add(FocIdLabel);

        focText = new JTextField();

        focText.setPreferredSize( new Dimension( 40, 24 ) );

        this.add(focText);

        this.add(FocIdValueLabel, "wrap");

        ActionListener FclLen_Listener = new ActionListener() {//add actionlistner to listen for change
            @Override
            public void actionPerformed(ActionEvent e) {
                FocusLength = focText.getText();
                SensWidth = widthSen.getText();
                SensHeigth = heigthSen.getText();

                //                Focal_len = Float.valueOf(FocusLength);
                //                SensWidth_float = Float.valueOf(SensWidth);
                //                SensHeigth_float = Float.valueOf(SensHeigth);

                calculateParameters();
                updateGSDSlider();

            }
        };

        focText.addActionListener(FclLen_Listener);





        //Sensor Width
        widthIdValueLabel = new JLabel();
        widthIdValueLabel.setText("");
        widthIdLabel = new JLabel();
        widthIdLabel.setText("<html><b>" + I18n.text("Sensor Width") + ": ");

        this.add(widthIdLabel);

        widthSen= new JTextField();

        widthSen.setPreferredSize( new Dimension( 40, 24 ) );

        this.add(widthSen, "wrap");

        ActionListener SensWidth_Listener = new ActionListener() {//add actionlistner to listen for change
            @Override
            public void actionPerformed(ActionEvent e) {
                FocusLength = focText.getText();
                SensWidth = widthSen.getText();
                SensHeigth = heigthSen.getText();

                //                Focal_len = Float.valueOf(FocusLength);
                //                SensWidth_float = Float.valueOf(SensWidth);
                //                SensHeigth_float = Float.valueOf(SensHeigth);

                calculateParameters();
                updateGSDSlider();

            }
        };

        widthSen.addActionListener(SensWidth_Listener);




        //Sensor Heigth

        heigthIdValueLabel = new JLabel();
        heigthIdValueLabel.setText("");
        heigthIdLabel = new JLabel();
        heigthIdLabel.setText("<html><b>" + I18n.text("Sensor Heigth") + ": ");

        this.add(heigthIdLabel);

        heigthSen= new JTextField();

        heigthSen.setPreferredSize( new Dimension( 40, 24 ) );

        this.add(heigthSen,"wrap");

        ActionListener SensHeigth_Listener = new ActionListener() {//add actionlistner to listen for change
            @Override
            public void actionPerformed(ActionEvent e) {
                FocusLength = focText.getText();
                SensWidth = widthSen.getText();
                SensHeigth = heigthSen.getText();

                //                Focal_len = Float.valueOf(FocusLength);
                //                SensWidth_float = Float.valueOf(SensWidth);
                //                SensHeigth_float = Float.valueOf(SensHeigth);

                calculateParameters();
                updateGSDSlider();

            }
        };

        heigthSen.addActionListener(SensHeigth_Listener);


        //Resolution Combobox

        ResIdValueLabel = new JLabel();
        ResIdValueLabel.setText("");
        ResIdLabel = new JLabel();
        ResIdLabel.setText("<html><b>" + I18n.text("Resolution") + ": ");

        this.add(ResIdLabel);

        String[] res = new String[] {"640x480", "1024x780", "1600x1200"};



        ResList = new JComboBox<>(res);
        add(ResList);
        //  selectedRes = (String) ResList.getSelectedItem();




        resH = (float) 640.0;
        resV = (float) 480.0;


        this.add(ResIdValueLabel,"wrap");

        ActionListener ResActionListener = new ActionListener() { //add actionlistner to listen for change
            @Override
            public void actionPerformed(ActionEvent e) {

                selectedRes= (String) ResList.getSelectedItem();

                String[] tokens = selectedRes.split("x");


                resH = Float.valueOf(tokens[0]);
                resV = Float.valueOf(tokens[1]);

                calculateParameters();
                updateGSDSlider();


            }

        };

        ResList.addActionListener(ResActionListener);




        JLabel AngleIdLabel = new JLabel();
        AngleIdLabel.setText("<html><b>" + I18n.text("Angle (degrees)") + ": " + 0);

        this.add(AngleIdLabel);

        //Slider para angulo

        angleSlider = new JSlider(JSlider.HORIZONTAL, 0, 90, 0);

        angleSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                System.out.println(((JSlider) ce.getSource()).getValue());
                AngleIdLabel.setText("<html><b>" + I18n.text("Angle (degrees)") + ": " + ((JSlider) ce.getSource()).getValue());
                angle = (float) ((JSlider) ce.getSource()).getValue();
            }
        });


        add(angleSlider, "wrap");
        //framesPerSecond.addChangeListener(this);

        //Turn on labels at major tick marks.
        angleSlider.setMajorTickSpacing(45);
        angleSlider.setMinorTickSpacing(1);
        angleSlider.setPaintTicks(true);
        angleSlider.setPaintLabels(true);


        JLabel GSDIdLabel = new JLabel();
        GSDIdLabel.setText("<html><b>" + I18n.text("GSD (px/m)") + ": " + 104);

        this.add(GSDIdLabel);

        //Slider para GSD

        GSDSlider = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);

        GSDSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                System.out.println(((JSlider) ce.getSource()).getValue());
                GSDIdLabel.setText("<html><b>" + I18n.text("GSD (px/m)") + ": " + ((JSlider) ce.getSource()).getValue());
                GSD = (float) ((JSlider) ce.getSource()).getValue();
                calculateParameters(GSD);
            }
        });


        add(GSDSlider, "wrap");
        //framesPerSecond.addChangeListener(this);

        //Turn on labels at major tick marks.
        //GSDSlider.setMajorTickSpacing(0);
        GSDSlider.setMinorTickSpacing(1);
        GSDSlider.setPaintTicks(true);
        GSDSlider.setPaintLabels(true);

        //Altitude

        AltIdValueLabel = new JLabel();
        AltIdValueLabel.setText("");
        AltIdLabel = new JLabel();
        AltIdLabel.setText("<html><b>" + I18n.text("Altitude (m)") + ": ");

        this.add(AltIdLabel);

        AltLabel= new JLabel();

        AltLabel.setPreferredSize( new Dimension( 40, 24 ) );

        this.add(AltLabel,"wrap");

        //Create new Plan
        Action CreatePolygonAction = new AbstractAction(I18n.text("Create New Polygon")) {

            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    editor = getPolygonEditorInstance();
                    editor.setActive(true, getRenderer(getConsole()));
                    activatePolygonEditor(editor);
                    // clean previous polygon
                    editor.closePolygon();


                    System.out.println("IS NULL: " + (getRenderer(getConsole()) == null));
                    System.out.println("IS ACTIVE: " + editor.isActive());
                }
                catch (Exception e2) {
                    // TODO Auto-generated catch block
                    System.out.println("AQUI");
                    e2.printStackTrace();
                }



                createPolygon.setEnabled(false);
                generatePlan.setEnabled(true);
                deleteEverything.setEnabled(true);
                editPolygon.setEnabled(false);
                generatePlan.setLabel("Generate Plan");

                getConsole().post(Notification.warning("Instruction", "Double click the map to add Polygon Vertices"));




            }
        };

        createPolygon = new JButton(CreatePolygonAction);
        add(createPolygon);

        //Generate Plan
        Action GeneratePlanAction = new AbstractAction(I18n.text("Generate Plan")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                deactivatePolygonEditor(getPolygonEditorInstance());




                createPolygon.setEnabled(true);
                generatePlan.setEnabled(false);
                editPolygon.setEnabled(true);
                deleteEverything.setEnabled(true);
                generatePlan.setLabel("Generate Plan");


                RealWorldPolygon PlanPolygon = editor.getPolygon();

                PlanPolygon.CreateGrid(altInt, selectedVeic, (double) distanciaRetas, (double) angleSlider.getValue(), null, false, getConsole());

            }
        };

        generatePlan = new JButton(GeneratePlanAction);
        this.add(generatePlan, "wrap");


        //Edit Polygon
        Action EditPolygonAction = new AbstractAction(I18n.text("Edit Polygon")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    editor = getPolygonEditorInstance();
                    editor.setActive(true, getRenderer(getConsole()));
                    activatePolygonEditor(editor);
                }
                catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }



                createPolygon.setEnabled(false);
                generatePlan.setEnabled(true);
                editPolygon.setEnabled(false);
                generatePlan.setLabel("Refresh Plan");
                deleteEverything.setEnabled(true);



            }
        };

        editPolygon = new JButton(EditPolygonAction);
        add(editPolygon);


        //Button Delete Everything
        Action DeleteEverythingAction = new AbstractAction(I18n.text("Delete Everything")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                deactivatePolygonEditor(getPolygonEditorInstance());




                createPolygon.setEnabled(true);
                generatePlan.setEnabled(false);
                deleteEverything.setEnabled(false);
                editPolygon.setEnabled(false);

                editor.closePolygon();




            }
        };

        deleteEverything = new JButton(DeleteEverythingAction);
        this.add(deleteEverything, "wrap");




        generatePlan.setEnabled(false);
        editPolygon.setEnabled(false);
        deleteEverything.setEnabled(false);





        // Canon como camera pre definida
        focText.setText("28");
        widthSen.setText("6.17");
        heigthSen.setText("4.56");

        FocusLength = "28";
        SensWidth = "6.17";
        SensHeigth = "4.56";


        focText.setEnabled(false);
        widthSen.setEnabled(false);
        heigthSen.setEnabled(false);



        calculateParameters();
        updateGSDSlider();


    }

    public PolygonEditor getPolygonEditorInstance() {
        for(IConsoleInteraction tmp : getConsole().getInteractions()) {
            if(tmp.getClass() == PolygonEditor.class) {
                return (PolygonEditor) tmp;
            }
        }
        // PlanEditor.class
        return null;
    }

    public MapPanel getMapPanelInstance() {
        Vector<MapPanel> maps = getConsole().getSubPanelsOfInterface(MapPanel.class);

        if (maps.isEmpty()) {
            getConsole().post(Notification.error("Edit Polygon", "Could not fetch map panel"));
            return null;
        }


        return  maps.get(0);
    }




    public void activatePolygonEditor(PolygonEditor polygonEditor) {
        getMapPanelInstance().setActiveInteraction(polygonEditor);
    }

    public void deactivatePolygonEditor(PolygonEditor editor) {
        System.out.println("AQUI x2");
        getMapPanelInstance().setActiveInteraction(null);
        try {
            editor.setActive(false, getRenderer(getConsole()));
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void calculateParameters() {

        FocusLength = focText.getText();
        SensWidth = widthSen.getText();
        SensHeigth = heigthSen.getText();

        float fl = Float.valueOf(FocusLength);
        float sw = Float.valueOf(SensWidth);
        //float sh = Float.valueOf(SensHeigth);

        GSD_cm_px = 100*1/((float) GSDSlider.getValue());

        float altH = (float) ( (Float.valueOf(GSD_cm_px) * fl * resH ) / (100.0 * sw)); 

        altInt = (int) altH;

        AltLabel.setText(String.valueOf(altInt));  //Altitud3(altitude) a ser calculado

        //Calculo Para a distancia entre as retas

        float coberturaHor = (altH * sw )/ fl;

        //Para utilizar caso seja implementado a funcionalidade de controlo de velocidade/tempo para as definir o tempo entre as fotogorafias
        //float coberturaVert = (Altitud3 * sh )/ Focal_len;

        distanciaRetas = (float) ( 0.7 * coberturaHor);

    }

    public void calculateParameters(float GSDnoS) {

        FocusLength = focText.getText();
        SensWidth = widthSen.getText();
        SensHeigth = heigthSen.getText();

        float fl = Float.valueOf(FocusLength);
        float sw = Float.valueOf(SensWidth);
        //float sh = Float.valueOf(SensHeigth);

        GSD_cm_px = 100*1/((float) GSDnoS);

        float altH = (float) ( (Float.valueOf(GSD_cm_px) * fl * resH ) / (100.0 * sw)); 

        altInt = (int) altH;

        AltLabel.setText(String.valueOf(altInt));  //Altitud3(altitude) a ser calculado

        //Calculo Para a distancia entre as retas

        float coberturaHor = (altH * sw )/ fl;

        //Para utilizar caso seja implementado a funcionalidade de controlo de velocidade/tempo para as definir o tempo entre as fotogorafias
        //float coberturaVert = (Altitud3 * sh )/ Focal_len;

        distanciaRetas = (float) ( 0.7 * coberturaHor);
        int kjdgfksdf = 0;
    }


    //public static float distanciaRetas, resH, resV, Focal_len, SensWidth_float, SensHeigth_float, Altitud3, GSD, angle;
    public void updateGSDSlider() {
        FocusLength = focText.getText();
        SensWidth = widthSen.getText();
        SensHeigth = heigthSen.getText();

        float fl = Float.valueOf(FocusLength);
        float sw = Float.valueOf(SensWidth);
        //float sh = Float.valueOf(SensHeigth);


        int GSDnoSlider = GSDSlider.getValue();
        float GSDSliderMin = ((120*100*sw)/(fl*resH));
        float GSDSliderMax = ((30*100*sw)/(fl*resH));

        GSDSliderMin = 1/(GSDSliderMin/100);
        GSDSliderMax = 1/(GSDSliderMax/100);        

        int min = (int) Math.ceil(GSDSliderMin);
        int max = (int) Math.floor(GSDSliderMax);
        int tick = (int)(max-min)/2;

        System.out.println(GSDnoSlider + "  " + GSDSliderMin + "  " + GSDSliderMax + "  " + min + "  " + max + " " + tick);


        GSDSlider.setMinimum(min);
        GSDSlider.setMaximum(max);


        if(GSDnoSlider < min) {
            GSDSlider.setValue(min);
        }else if(GSDnoSlider > max) {
            GSDSlider.setValue(max);
        }else {
            GSDSlider.setValue(GSDnoSlider);
        }
        //        GSDSlider.setMajorTickSpacing(100);
        //        GSDSlider.setMinorTickSpacing(1);

    }



    @Override
    public void cleanSubPanel() {

    }


}

