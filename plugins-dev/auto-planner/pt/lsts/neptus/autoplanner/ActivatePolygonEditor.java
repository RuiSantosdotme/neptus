/*
 * Copyright (c) 2004-2017 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENSE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * http://ec.europa.eu/idabc/eupl.html.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: zp
 * 16/01/2017
 */
package pt.lsts.neptus.autoplanner;

import java.util.Vector;

import pt.lsts.neptus.console.ConsoleLayout;
import pt.lsts.neptus.console.ConsolePanel;
import pt.lsts.neptus.console.notifications.Notification;
import pt.lsts.neptus.console.plugins.planning.MapPanel;
import pt.lsts.neptus.plugins.NeptusMenuItem;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.renderer2d.StateRendererInteraction;

/**
 * @author zp
 *
 */
@PluginDescription
public class ActivatePolygonEditor extends ConsolePanel {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * @param console
     */
    public ActivatePolygonEditor(ConsoleLayout console) {
        super(console);
    }

    @NeptusMenuItem("Tools>Edit Polygon")
    public void editPolygon() {
        Vector<MapPanel> maps = getConsole().getSubPanelsOfInterface(MapPanel.class);
        
        if (maps.isEmpty()) {
            getConsole().post(Notification.error("Edit Polygon", "Could not fetch map panel"));
            return;
        }
        
        MapPanel map = maps.get(0);
        
        for (StateRendererInteraction interaction : map.getInteractionModes()) {
            if (interaction.getClass() == PolygonEditor.class) {
                map.setActiveInteraction(interaction);     
                break;
            }
        }
    }
    
    @NeptusMenuItem("Tools>Generate Plan")
    public void generatePlan() {
        Vector<PolygonEditor> poly = getConsole().getSubPanelsOfInterface(PolygonEditor.class);

        if (poly.isEmpty()) {
            getConsole().post(Notification.error("Generate", "Could not fetch polygon editor"));
            return;
        }
        
        System.out.println("Generate plan using "+poly.get(0).getPolygon());
    }

   
    @Override
    public void cleanSubPanel() {
        // TODO Auto-generated method stub
        
    }

    
    @Override
    public void initSubPanel() {
        // TODO Auto-generated method stub
        
    }
    
}
