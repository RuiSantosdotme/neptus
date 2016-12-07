package pt.lsts.neptus.autoplanner;
import java.awt.Component;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
/**/
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

import com.google.common.eventbus.Subscribe;

import net.miginfocom.swing.MigLayout;
import pt.lsts.imc.Abort;
import pt.lsts.imc.AcousticOperation;
import pt.lsts.imc.AcousticOperation.OP;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.PlanControlState;
import pt.lsts.imc.TextMessage;
import pt.lsts.neptus.autoplanner.RealWorldPolygon.StartPosition;
import pt.lsts.neptus.comm.manager.imc.ImcMsgManager;
import pt.lsts.neptus.comm.manager.imc.ImcSystem;
import pt.lsts.neptus.comm.manager.imc.ImcSystemsHolder;
import pt.lsts.neptus.comm.manager.imc.MessageDeliveryListener;
import pt.lsts.neptus.console.notifications.Notification;
import pt.lsts.neptus.i18n.I18n;
import pt.lsts.neptus.plugins.NeptusProperty;
import pt.lsts.neptus.types.vehicle.VehicleType.SystemTypeEnum;
import pt.lsts.neptus.util.GuiUtils;
import pt.lsts.neptus.util.conf.GeneralPreferences;
/**/

import pt.lsts.neptus.console.ConsoleLayout;
import pt.lsts.neptus.console.ConsolePanel;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.plugins.Popup;
import pt.lsts.neptus.plugins.Popup.POSITION;
import pt.lsts.neptus.plugins.map.MapEditor;

import pt.lsts.neptus.plugins.map.interactions.*;

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
    
    
    private JLabel stateValueLabel;
    private JLabel stateLabel;
    private JLabel planIdValueLabel;
    private JLabel planIdLabel;
    private JLabel nodeIdValueLabel;
    private JLabel nodeIdLabel;
    private JLabel outcomeTitleLabel;
    private JLabel outcomeLabel;
    private JLabel outcomeTitleLabel1;
    private JLabel outcomeLabel1, AltIdValueLabel,AltIdLabel, AngIdLabel, AngIdValueLabel ;
    private PlanControlState.STATE state;
    private String planId = "";
    private String nodeId = "";
    private String lastOutcome = "<html><font color='0x666666'>" + I18n.text("N/A") + "</font>";
    private int nodeTypeImcId = -1;
    private long nodeStarTimeMillisUTC = -1;
    private long nodeEtaSec = -1;
    private long lastUpdated = -1;

    
    //Variaveis globais para aceder à opçao escolhida
    public static String selectedCam, selectedVeic, selectedRes, height, angle ;
    
    public static int highInt, angleInt;
    
    
   
    
    public AutoPlanner(ConsoleLayout console) {
        super(console);
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
        
        
        
     
        

        this.add(stateLabel, "");
        
        //ComboBox para Camera
        String[] Cam = new String[] {"Go Pro", "Sony"};
        JComboBox<String> CamList = new JComboBox<>(Cam);
        add(CamList);
        selectedCam = (String) CamList.getSelectedItem();
        
        this.add(stateValueLabel, "wrap");
        
        
        Action sendAbortAction = new AbstractAction(I18n.text("Send Abort")) {

            @Override
            public void actionPerformed(ActionEvent e) {

                
                
                PolygonInteraction.realCoordPolygon.CreateGrid(100, 0, 150, 0, 0, 0, null, false, 0, 0, getConsole());
                
             

            }
        };
        
        JButton sendAbort = new JButton(sendAbortAction);
        add(sendAbort);
        
        
        ActionListener cbActionListener = new ActionListener() {//add actionlistner to listen for change
            @Override
            public void actionPerformed(ActionEvent e) {

                Abort abortMsg = new Abort();
                send(abortMsg);                
                System.out.println("------------------------------------------------------");
                System.out.println("LATITUDE = "+ MapEditor.lat);
                System.out.println("------------------------------------------------------");
                System.out.println("LONGITUDE = "+ MapEditor.longi);
                
             

            
                selectedCam = (String) CamList.getSelectedItem();
                System.out.println("camera selecionada: "+ selectedCam);
                int a = 1;

            }
            
        };
        
        CamList.addActionListener(cbActionListener);
             
                
            
            
       
        
        planIdValueLabel = new JLabel();
        planIdValueLabel.setText("");
        planIdLabel = new JLabel();
        planIdLabel.setText("<html><b>" + I18n.text("Resolução") + ": ");

        this.add(planIdLabel);
        
        //ComboBox para Resolução 
        
        String[] Res = new String[] {"800 x 600", "1024 x 768", "1280 x 720", "1366 x 768"};
        
        JComboBox<String> ResList = new JComboBox<>(Res);
        add(ResList);
        selectedRes = (String) ResList.getSelectedItem();
        
        
        this.add(planIdValueLabel, "wrap");
        
        ActionListener resActionListener = new ActionListener() {//add actionlistner to listen for change
            @Override
            public void actionPerformed(ActionEvent e) {
            
                selectedRes = (String) ResList.getSelectedItem();
                System.out.println("Res selecionada: "+ selectedRes);
               
               
            }
            
        };
        
        ResList.addActionListener(resActionListener);

        nodeIdValueLabel = new JLabel();
        nodeIdValueLabel.setText("");
        nodeIdLabel = new JLabel();
        nodeIdLabel.setText("<html><b>" + I18n.text("Veiculo") + ": ");
        
        this.add(nodeIdLabel);

        //ComboBox para veículo 
        
        String[] Veiculo = new String[] {"X8 SkyWalker", "Mariner"};
        
        JComboBox<String> VeicList = new JComboBox<>(Veiculo);
        add(VeicList);
        selectedVeic = (String) VeicList.getSelectedItem();
              
        this.add(nodeIdValueLabel, "wrap");
        

        
        ActionListener VeicActionListener = new ActionListener() {//add actionlistner to listen for change
            @Override
            public void actionPerformed(ActionEvent e) {
            
                selectedVeic = (String) VeicList.getSelectedItem();
                System.out.println("Veiculo selecionado: "+ selectedVeic);
               
               
            }
            
        };
        
        VeicList.addActionListener(VeicActionListener);
        
        AltIdValueLabel = new JLabel();
        AltIdValueLabel.setText("");
        AltIdLabel = new JLabel();
        AltIdLabel.setText("<html><b>" + I18n.text("Altitude") + ": ");
        
        this.add(AltIdLabel);

        //spinbox para altitude
        
        SpinnerModel model =
                new SpinnerNumberModel(50,    //initial value
                                       0,    //min
                                       150, //max
                                       1); //step
        
        JSpinner spinner = new JSpinner(model);
        add(spinner);
              
        this.add(AltIdValueLabel, "wrap");
        
        spinner.addChangeListener(new ChangeListener() {      
            @Override
            public void stateChanged(ChangeEvent e) {
              // handle click
                
                try {
                    spinner.commitEdit();
                } catch ( java.text.ParseException d ) {  }
                
             
                highInt = (Integer) spinner.getValue();
                
                System.out.println("ALtitude: "+ highInt);
                
                
            }

            
          });
       
     
        
        AngIdValueLabel = new JLabel();
        AngIdValueLabel.setText("");
        AngIdLabel = new JLabel();
        AngIdLabel.setText("<html><b>" + I18n.text("Angulo") + ": ");
        
        this.add(AngIdLabel);

        //spinbox para angulo
        
        SpinnerModel modelA=
                new SpinnerNumberModel(90,    //initial value
                                       0,    //min
                                       180, //max
                                       1); //step
        
        JSpinner spinnerA = new JSpinner(modelA);
        add(spinnerA);
              
        this.add(AngIdValueLabel, "wrap");
        
        
        spinnerA.addChangeListener(new ChangeListener() {      
            @Override
            public void stateChanged(ChangeEvent e) {
              // handle click
                
                try {
                    spinnerA.commitEdit();
                } catch ( java.text.ParseException d ) {  }
                
             
                angleInt = (Integer) spinnerA.getValue();
                
                System.out.println("Angulo: "+ angleInt);
               
            }
          });
      
        
        
        
        
        
        
    }
    
    
    @Override
    public void cleanSubPanel() {

    }

}

