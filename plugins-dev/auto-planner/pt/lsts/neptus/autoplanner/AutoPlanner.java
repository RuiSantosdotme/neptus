package pt.lsts.neptus.autoplanner;

/**/
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import pt.lsts.imc.PlanControlState;
import pt.lsts.neptus.console.ConsoleLayout;
import pt.lsts.neptus.console.ConsolePanel;
import pt.lsts.neptus.console.ContainerSubPanel;
import pt.lsts.neptus.console.IConsoleInteraction;
import pt.lsts.neptus.console.notifications.Notification;
import pt.lsts.neptus.console.plugins.planning.MapPanel;
import pt.lsts.neptus.i18n.I18n;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.plugins.map.interactions.PolygonInteraction;
import pt.lsts.neptus.renderer2d.InteractionAdapter;
import pt.lsts.neptus.renderer2d.StateRenderer2D;
import pt.lsts.neptus.renderer2d.StateRendererInteraction;
import pt.lsts.neptus.types.map.MapGroup;

/**
 * @author Equipa C - SEAI 2016
 *
 */
@PluginDescription(name = "Auto Planner")
//@Popup(accelerator='Y',pos = POSITION.RIGHT, height=200, width = 200)
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
    private JLabel GIdValueLabel;
    private JLabel GIdLabel;
    private JLabel AltIdValueLabel;
    private JLabel AltIdLabel;
    
    private JLabel stateValueLabel, entValueLabel, entLabel;
    private JLabel stateLabel;
    private JLabel nodeIdValueLabel;
    private JLabel nodeIdLabel;
    private JLabel FocIdValueLabel,FocIdLabel, AngIdLabel, AngIdValueLabel ;
    private PlanControlState.STATE state;
    private String lastOutcome = "<html><font color='0x666666'>" + I18n.text("N/A") + "</font>";
    private JComboBox<String> CamList;
    private JComboBox<String> ResList;
    private JComboBox<String> VeicList;
    private JSlider angleSlider;
    private JSlider GSDSlider;
    private JSpinner spinner, spinnerR;
    private JSpinner spinnerG ;
    private JSpinner spinnerA;
    private SpinnerModel modelA;
    private SpinnerModel model, modelR;
    private Map<String, Object> pluginsMap = new LinkedHashMap<String, Object>();
    private Map<String, Class<?>> plugins = new LinkedHashMap<String, Class<?>>();
    private ContainerSubPanel container;
    private JTextField focText, heigthSen, widthSen;
    JButton completePlan;
    JLabel AltSen;
    int a=0;
    
    private PolygonEditor editor = null;
    
 
    private JButton createPlan, EditPlan, DelPlan, PausePlan, ResumePlan, EditMode ;
    
    //Variaveis globais para aceder à opçao escolhida
    public static String selectedCam, selectedVeic, selectedRes, height, angle, FocusLength, Width, Heigth;
    public static float distanciaRetas;
    //Algumas das variaveis anteriores convertidas para INT (é melhor mesmo usar as STRINGs convertidas para INT, depois de tantas alterações nao sei se estas ainda estao OK))
    private static float Focal_len, angleInt, resInt, resH, resV;
    private static int GSDInt=10, altInt ;
    public static float  Altitud3,GSD;
    
    
   
    
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
   
        
      //  this.add(bValueLabel, "wrap");
        
   
        
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

                
                //porque é que puseram isto aqui?
                
               /* Abort abortMsg = new Abort();
                send(abortMsg);                
                System.out.println("------------------------------------------------------");
                System.out.println("LATITUDE = "+ MapEditor.lat);
                System.out.println("------------------------------------------------------");
                System.out.println("LONGITUDE = "+ MapEditor.longi);
                */
             

            
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
                    Focal_len = 8;
                    Width = "5.76";
                    Heigth = "4.29";
                    
                } else 
                if(selectedCam == "Canon")
                {
                    focText.setText("28");
                    widthSen.setText("6.17");
                    heigthSen.setText("4.56");
                    
                    FocusLength = "28";
                    Focal_len = 28;
                    Width = "6.17";
                    Heigth = "4.56";
                    
                    focText.setEnabled(false);
                    widthSen.setEnabled(false);
                    heigthSen.setEnabled(false);
                    
                    
                  //Criar Variaveis globais para guardar os dados
                    
                    
                    
                }
                
                else 
                    
                    if(selectedCam == "Custom")
                    {
                        FocusLength = focText.getText();
                        Focal_len = Float.valueOf(FocusLength);
                        Width = widthSen.getText();
                        Heigth = heigthSen.getText();
                        
                        
                        focText.setEnabled(true);
                        widthSen.setEnabled(true);
                        heigthSen.setEnabled(true);
                        
                      //Criar Variaveis globais para guardar os dados, neste caso ele guarda o que estiver escrito nas caixas de texto
                        
                        
                        
                    }
                
                
                        

                
                
                
                
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
        
             
     
        //Sensor Width
        widthIdValueLabel = new JLabel();
        widthIdValueLabel.setText("");
        widthIdLabel = new JLabel();
        widthIdLabel.setText("<html><b>" + I18n.text("Sensor Width") + ": ");
        
        this.add(widthIdLabel);
        
        widthSen= new JTextField();
        
        widthSen.setPreferredSize( new Dimension( 40, 24 ) );
        
        this.add(widthSen, "wrap");
        
      
        

        //Sensor Heigth
        
        heigthIdValueLabel = new JLabel();
        heigthIdValueLabel.setText("");
        heigthIdLabel = new JLabel();
        heigthIdLabel.setText("<html><b>" + I18n.text("Sensor Heigth") + ": ");
        
        this.add(heigthIdLabel);
        
        heigthSen= new JTextField();
        
        heigthSen.setPreferredSize( new Dimension( 40, 24 ) );
        
        this.add(heigthSen,"wrap");
        
        
        
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
        
        GSDSlider = new JSlider(JSlider.HORIZONTAL, 104, 415, 104);
        
        GSDSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                System.out.println(((JSlider) ce.getSource()).getValue());
                GSDIdLabel.setText("<html><b>" + I18n.text("GSD (px/m)") + ": " + ((JSlider) ce.getSource()).getValue());
            }
        });
        
        
        add(GSDSlider, "wrap");
        //framesPerSecond.addChangeListener(this);
        
      //Turn on labels at major tick marks.
        GSDSlider.setMajorTickSpacing(100);
        GSDSlider.setMinorTickSpacing(1);
        GSDSlider.setPaintTicks(true);
        GSDSlider.setPaintLabels(true);
        
        //GSD - Usar esta spinbox
        
        
        
        
//        SpinnerModel modelG =
//                new SpinnerNumberModel( 10,     //initial value
//                                        1,   //min
//                                        50, //max
//                                       1); //step
//        
//        GIdValueLabel = new JLabel();
//        GIdValueLabel.setText("");
//        GIdLabel = new JLabel();
//        GIdLabel.setText("<html><b>" + I18n.text("GSD (cm/px)") + ": ");
//        
//        this.add(GIdLabel);
//        
//        spinnerG = new JSpinner(modelG);
//        add(spinnerG);
//        
//       
//        spinnerG.addChangeListener(new ChangeListener() {      
//            @Override
//            public void stateChanged(ChangeEvent e) {
//              // handle click
//                
//                try {
//                    spinnerG.commitEdit();
//                } catch ( java.text.ParseException d ) {  }
//                
//             
//                GSDInt = (int) spinnerG.getValue();
//                
//                System.out.println("GSD: "+ GSDInt);
//                
//               
//            }
//          });
//        
//        this.add(GIdValueLabel, "wrap");
        
        
        //Altitude
        
        AltIdValueLabel = new JLabel();
        AltIdValueLabel.setText("");
        AltIdLabel = new JLabel();
        AltIdLabel.setText("<html><b>" + I18n.text("Altitude (m)") + ": ");
        
        this.add(AltIdLabel);
        
        AltSen= new JLabel();
        
        AltSen.setPreferredSize( new Dimension( 40, 24 ) );
        
        this.add(AltSen,"wrap");
        
        
       
        
        
        //3 botoes sem funçoes para já
        Action CreatePlanAction = new AbstractAction(I18n.text("Create Plan")) {

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
                a= 1;
                
                if(selectedCam == "Custom")
                {
                    
                    
                    focText.setEnabled(true);
                    widthSen.setEnabled(true);
                    heigthSen.setEnabled(true);
                    
                    FocusLength = focText.getText();
                    Focal_len = Float.valueOf(FocusLength);
                    Width = widthSen.getText();
                    Heigth = heigthSen.getText();
                                        
                    
                }
            

                
               //o codigo deste botao irá fazer o calculo, acho eu ...
                
                float GSD = 100*1/((float)GSDInt);
                
                float altH = (float) ( (Float.valueOf(GSD) * Focal_len * resH ) / (100.0 * Float.valueOf(Width))); 
                
                float altV = (float) ((Float.valueOf(GSD) * Focal_len * resV ) / (100.0 *Float.valueOf(Heigth) ));
                
                                    

                System.out.println("resH" + resH);
                System.out.println("resV"+ resV);
                
                System.out.println("w" + Float.valueOf(Width));
                System.out.println("h"+ Float.valueOf(Heigth));
                
                System.out.println("h" + altH);
                System.out.println("v"+ altV);
                
                
                if (altV > altH)
                    Altitud3 = altV;
                else 
                   Altitud3 =   altH;
                
                altInt = (int) Altitud3;
                
                if (altInt < 30)
                {
                    altInt =30; 
                    Altitud3 = (float) 30.0;
                }
                
                
                
                AltSen.setText(String.valueOf(altInt));  //Altitud3(altitude) a ser calculado
                
                //Calculo Para a distancia entre as retas
                
                float coberturaHor = (Altitud3 * Float.valueOf(Width) )/ Focal_len;
                
                float coberturaVert = (Altitud3 * Float.valueOf(Heigth) )/ Focal_len;
                
                
                
                
                float cob;
                if (coberturaHor > coberturaVert)
                    cob = coberturaHor;
                else
                    cob =coberturaVert;
                
                distanciaRetas = (float) ( (1-0.3) * cob);
                
                System.out.println("h " + coberturaHor);
                System.out.println("v "+ coberturaVert);
                System.out.println("d "+ distanciaRetas);

                PolygonInteraction.realCoordPolygon.CreateGrid(altInt, 0, distanciaRetas, angleSlider.getValue(), 0, 0, null, false, 0, 0,getConsole());
                
                createPlan.setEnabled(false);
                completePlan.setEnabled(true);
                
                getConsole().post(Notification.warning("Instruction", "Double click the map to add Polygon Vertices"));
                
                
                
                
            }
        };
        
        createPlan = new JButton(CreatePlanAction);
        add(createPlan);
        
        
        Action CompletePlanAction = new AbstractAction(I18n.text("Complete Plan")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                deactivatePolygonEditor(getPolygonEditorInstance());
               
                 
                
                
                createPlan.setEnabled(true);
                completePlan.setEnabled(false);
                
//                ActivatePolygonEditor activador2 = new ActivatePolygonEditor(getConsole());
//                activador2.editPolygon();
                
                
//                currentInteraction.setActive(false, renderer);
//                currentInteraction = null;
                   
            }
        };
        
        completePlan = new JButton(CompletePlanAction);
        add(completePlan, "wrap");
        
        
        
       
        completePlan.setEnabled(false);

        
        
      
      
        // Canon como camera pre definida
        focText.setText("28");
        widthSen.setText("6.17");
        heigthSen.setText("4.56");
        
        FocusLength = "28";
        Focal_len = 28;
        Width = "6.17";
        Heigth = "4.56";
        
        focText.setEnabled(false);
        widthSen.setEnabled(false);
        heigthSen.setEnabled(false);
        
        
        
        
        
        
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
    
    
    @Override
    public void cleanSubPanel() {

    }


}

