package pt.lsts.neptus.autoplanner;
import java.awt.Component;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**/
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import com.google.common.eventbus.Subscribe;

import net.miginfocom.swing.MigLayout;
import pt.lsts.imc.Abort;
import pt.lsts.imc.AcousticOperation;
import pt.lsts.imc.AcousticOperation.OP;
import pt.lsts.imc.IMCMessage;
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
     */
    
    //Variaveis globais para aceder à opçao escolhida
    public static String selectedCam, selectedVeic, selectedRes ;
    
    
    
    public AutoPlanner(ConsoleLayout console) {
        super(console);
    }

    @Override
    public void initSubPanel() {
        removeAll();

        Action sendAbortAction = new AbstractAction(I18n.text("Send Abort")) {

            @Override
            public void actionPerformed(ActionEvent e) {

                Abort abortMsg = new Abort();
                send(abortMsg);                
                System.out.println("------------------------------------------------------");
                System.out.println("LATITUDE = "+ MapEditor.lat);
                System.out.println("------------------------------------------------------");
                System.out.println("LONGITUDE = "+ MapEditor.longi);
                
                PolygonInteraction.realCoordPolygon.CreateGrid(100, 0, 100, 0, 0, 0, null, false, 0, 0);
                
             

            }
        };

        JButton sendAbort = new JButton(sendAbortAction);

        add(sendAbort);
        
        
        //ComboBox para Camara
        String[] Cam = new String[] {"Camera 1", "Camera 2"};
        JComboBox<String> CamList = new JComboBox<>(Cam);
        add(CamList);
        selectedCam = (String) CamList.getSelectedItem();
        
        //ComboBox para Resolução 
        
        String[] Res = new String[] {"800 x 600", "1024 x 768", "1280 x 720", "1366 x 768"};
        
        JComboBox<String> ResList = new JComboBox<>(Res);
        add(ResList);
        selectedRes = (String) ResList.getSelectedItem();
        
        //ComboBox para resolução
        
        String[] Veiculo = new String[] {"X8 SkyWalker", "Mariner"};
        
        JComboBox<String> VeicList = new JComboBox<>(Veiculo);
        add(VeicList);
        selectedVeic = (String) VeicList.getSelectedItem();
        
        
        
        
        
        
        
        
        
    }

    @Override
    public void cleanSubPanel() {

    }

}

