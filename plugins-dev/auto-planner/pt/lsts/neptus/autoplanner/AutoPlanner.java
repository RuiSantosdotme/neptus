package pt.lsts.neptus.autoplanner;
import pt.lsts.neptus.console.ConsoleLayout;

/**
 * @author You
 *
 */
@PluginDescription(name = "My Console Viz")
@Popup(pos = POSITION.RIGHT, width = 200, height = 200, accelerator = ‘Y')
@SuppressWarnings("serial")

public class MyConsoleViz extends ConsolePanel {

    /**
     * @param console
     */
    public MyConsoleViz(ConsoleLayout console) {

        super(console);

    }

    @Override
    public void initSubPanel() {

    }

    @Override
    public void cleanSubPanel() {

    }

}