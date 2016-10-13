package pt.lsts.neptus.autoplanner;
import pt.lsts.neptus.console.ConsoleLayout;
import pt.lsts.neptus.console.ConsolePanel;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.plugins.Popup;
import pt.lsts.neptus.plugins.Popup.POSITION;

/**
 * @author Equipa C - SEAI 2016
 *
 */
@PluginDescription(name = "Sistema de planeamento e controlo de execução de  UAVs para aplicações em agricultura")
@Popup(accelerator='Y',pos = POSITION.RIGHT, height=200, width = 200)
@SuppressWarnings("serial")

public class AutoPlanner extends ConsolePanel {

    /**
     * @param console
     */
    public AutoPlanner(ConsoleLayout console) {
        super(console);
    }

    @Override
    public void initSubPanel() {

    }

    @Override
    public void cleanSubPanel() {

    }

}

