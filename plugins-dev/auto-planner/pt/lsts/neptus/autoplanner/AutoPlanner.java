package pt.lsts.neptus.autoplanner;



import java.awt.Color;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.google.common.eventbus.Subscribe;

import net.miginfocom.swing.MigLayout;
import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.PlanControlState;
import pt.lsts.imc.PlanDB;
import pt.lsts.imc.PlanDB.OP;
import pt.lsts.imc.PlanDB.TYPE;
import pt.lsts.neptus.NeptusLog;
import pt.lsts.neptus.console.ConsoleLayout;
import pt.lsts.neptus.console.ConsolePanel;
import pt.lsts.neptus.console.events.ConsoleEventMainSystemChange;
import pt.lsts.neptus.i18n.I18n;
import pt.lsts.neptus.plugins.NeptusProperty;
import pt.lsts.neptus.plugins.NeptusProperty.LEVEL;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.plugins.update.Periodic;
import pt.lsts.neptus.util.DateTimeUtil;
import pt.lsts.neptus.util.GuiUtils;

/**
 * @author Equipa C - SEAI 2016
 *
 */
@PluginDescription(name = "Auto Planner")
//@Popup(accelerator='Y',pos = POSITION.RIGHT, height=200, width = 200)
//@SuppressWarnings("serial")

public class AutoPlanner extends ConsolePanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * @param console
     */
    
    private JLabel stateValueLabel;
    private JLabel stateLabel;
    private JLabel planIdValueLabel;
    private JLabel planIdLabel;
    private JLabel nodeIdValueLabel;
    private JLabel nodeIdLabel;
    private JLabel outcomeTitleLabel;
    private JLabel outcomeLabel;
    
    
    private PlanControlState.STATE state;
    private String planId = "";
    private String nodeId = "";
    private String lastOutcome = "<html><font color='0x666666'>" + I18n.text("N/A") + "</font>";
    private int nodeTypeImcId = -1;
    private long nodeStarTimeMillisUTC = -1;
    private long nodeEtaSec = -1;
    private long lastUpdated = -1;
    
    @NeptusProperty(name = "Request plans automatically", userLevel=LEVEL.ADVANCED, category="Planning", description = "Select if Neptus should ask the vehicle for plans it is executing but Neptus doesn't know about")
    public boolean requestPlans = false;
    
    public AutoPlanner(ConsoleLayout console) {
        super(console);
        removeAll();
        initialize();
    }
    
    
    private void initialize() {
        setSize(200, 200);
        this.setLayout(new MigLayout("ins 0"));
        stateValueLabel = new JLabel();
        stateValueLabel.setText("");
        stateLabel = new JLabel();
        stateLabel.setText("<html><b>" + I18n.text("BLABLABLABLABLABLABLA") + ": ");

        this.add(stateLabel, "");
        this.add(stateValueLabel, "wrap");
        

        nodeIdValueLabel = new JLabel();
        nodeIdValueLabel.setText("");
        nodeIdLabel = new JLabel();
        // / This is a plan node, keep it in one word.
        nodeIdLabel.setText("<html><b>" + I18n.textc("Man.", "Maneuver") + ": ");

        this.add(nodeIdLabel);
        this.add(nodeIdValueLabel, "wrap");

        outcomeTitleLabel = new JLabel("<html><b>" + I18n.text("Outcome") + ": ");
        outcomeTitleLabel.setHorizontalAlignment(SwingConstants.LEADING);

        outcomeLabel = new JLabel(lastOutcome);
        this.add(outcomeTitleLabel);
        this.add(outcomeLabel, "wrap");

      
    }

    @Override
    public void initSubPanel() {
        /*removeAll();

        Action sendAbortAction = new AbstractAction(I18n.text("Send Abort")) {

            @Override
            public void actionPerformed(ActionEvent e) {

                Abort abortMsg = new Abort();
                send(abortMsg);                
                System.out.println("------------------------------------------------------");
                System.out.println("LATITUDE = "+ MapEditor.lat);
                System.out.println("------------------------------------------------------");
                System.out.println("LONGITUDE = "+ MapEditor.longi);
                
                

               
            }
        };

        JButton sendAbort = new JButton(sendAbortAction);

        add(sendAbort);*/
    }

    @Override
    public void cleanSubPanel() {

    }

}

