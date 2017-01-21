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
 * 11/01/2017
 */
package pt.lsts.neptus.autoplanner;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import pt.lsts.neptus.autoplanner.PolygonType.Vertex;
import pt.lsts.neptus.console.ConsoleInteraction;
import pt.lsts.neptus.console.ConsoleLayout;
import pt.lsts.neptus.console.plugins.planning.MapPanel;
import pt.lsts.neptus.gui.LocationPanel;
import pt.lsts.neptus.i18n.I18n;
import pt.lsts.neptus.plugins.NeptusProperty;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.renderer2d.InteractionAdapter;
import pt.lsts.neptus.renderer2d.Renderer2DPainter;
import pt.lsts.neptus.renderer2d.StateRenderer2D;
import pt.lsts.neptus.types.coord.LocationType;

/**
 * This class allows editing a polygon on the map
 * @author zp
 */
@PluginDescription(name = "Area editor")
public class PolygonEditor extends ConsoleInteraction implements Renderer2DPainter {
    
    protected PolygonType polygon = new PolygonType();
    protected PolygonType.Vertex vertex = null;
    protected Vector<MapPanel> maps = new Vector<>();
    public static RealWorldPolygon realCoordPolygon = new RealWorldPolygon();
    
    @NeptusProperty(name="Polygon color")
    protected Color polygonColor = Color.green;
    
    public PolygonEditor() {
        System.out.println("Polygon Editor!");
    }

    
    /**
     * Adds a layer to all map panels that paints the current polygon
     */
    @Override
    public void initInteraction() {
        maps = getConsole().getSubPanelsOfClass(MapPanel.class);
        System.out.println(maps);
        for (MapPanel p : maps)
            p.addPreRenderPainter(this);
    }

    /**
     * Removes layer from all map panels
     */
    @Override
    public void cleanInteraction() {
        for (MapPanel p : maps)
            p.removePostRenderPainter(this);
    }
    
    /** 
     * @return the currently edited polygon
     */
    public RealWorldPolygon getPolygon() {
        RealWorldPolygon poly = new RealWorldPolygon();
        for (Vertex v : polygon.getVertices())
            poly.insertPoint(new LocationType(v.lat, v.lon));
        return poly;
    }
    
    public void closePolygon() {
        polygon = new PolygonType();
    }
    
    /**
     * @param polygon The polygon to be edited
     */
    public void setPolygon(RealWorldPolygon polygon) {
        PolygonType poly = new PolygonType();
        for (LocationType l : polygon.getPolygonLL()) {
            l.convertToAbsoluteLatLonDepth();
            poly.addVertex(l.getLatitudeDegs(), l.getLongitudeDegs());
        }

        this.polygon = poly;
        this.vertex = null;
    }    

    /**
     * @see ConsoleInteraction
     */
    @Override
    public void setActive(boolean mode, StateRenderer2D source) {
        vertex = null;
        super.setActive(mode, source);
    }

    /**
     * Given a point in the map, checks if there is some vertex intercepted.
     */
    public PolygonType.Vertex intercepted(MouseEvent evt, StateRenderer2D source) {
        for (PolygonType.Vertex v : polygon.getVertices()) {
            Point2D pt = source.getScreenPosition(new LocationType(v.lat, v.lon));
            if (pt.distance(evt.getPoint()) < 5) {
                return v;
            }
        }
        return null;
    }

    /**
     * @see ConsoleInteraction
     */
    @Override
    public void mouseClicked(MouseEvent event, StateRenderer2D source) {
        System.out.println("Clicaste no ecrã!!!!!!");
        if (!SwingUtilities.isRightMouseButton(event)) {
            if (event.getClickCount() == 2) {
                LocationType loc = source.getRealWorldLocation(event.getPoint());
                polygon.addVertex(loc.getLatitudeDegs(), loc.getLongitudeDegs());
                source.repaint();
                return;
            }
            else {
                super.mouseClicked(event, source);
                return;
            }
        }

        Vertex v = intercepted(event, source);
        JPopupMenu popup = new JPopupMenu();
        if (v != null) {
            popup.add(I18n.text("Edit location")).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    LocationType l = new LocationType(v.lat, v.lon);
                    LocationType newLoc = LocationPanel.showLocationDialog(source, I18n.text("Edit Vertex Location"), l,
                            getConsole().getMission(), true);
                    if (newLoc != null) {
                        newLoc.convertToAbsoluteLatLonDepth();
                        v.lat = newLoc.getLatitudeDegs();
                        v.lon = newLoc.getLongitudeDegs();
                        polygon.recomputePath();
                    }
                    source.repaint();
                }
            });
            popup.add(I18n.text("Remove vertex")).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    polygon.removeVertex(v);
                    source.repaint();
                }
            });
        }
        else
            popup.add(I18n.text("Add vertex")).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    LocationType loc = source.getRealWorldLocation(event.getPoint());
                    polygon.addVertex(loc.getLatitudeDegs(), loc.getLongitudeDegs());
                    source.repaint();
                }
            });

        popup.show(source, event.getX(), event.getY());
    }

    @Override
    public void mousePressed(MouseEvent event, StateRenderer2D source) {
        Vertex v = intercepted(event, source);
        if (v == null)
            super.mousePressed(event, source);
        else
            vertex = v;
    }

    /**
     * @see ConsoleInteraction
     */
    @Override
    public void mouseDragged(MouseEvent event, StateRenderer2D source) {
        if (vertex == null)
            super.mouseDragged(event, source);
        else {
            LocationType loc = source.getRealWorldLocation(event.getPoint());
            vertex.lat = loc.getLatitudeDegs();
            vertex.lon = loc.getLongitudeDegs();
            polygon.recomputePath();
        }
    }

    /**
     * @see ConsoleInteraction
     */
    @Override
    public void mouseReleased(MouseEvent event, StateRenderer2D source) {
        super.mouseReleased(event, source);
        if (vertex != null)
            polygon.recomputePath();
        
        
        
        vertex = null;
    }

    /**
     * Paints both the polygon and the vertices of the polygon
     * @see ConsoleInteraction
     */
    @Override
    public void paintInteraction(Graphics2D g, StateRenderer2D source) {
        g.setTransform(source.getIdentity());
        paint(g, source);
        polygon.getVertices().forEach(v -> {
            Point2D pt = source.getScreenPosition(new LocationType(v.lat, v.lon));
            Ellipse2D ellis = new Ellipse2D.Double(pt.getX() - 5, pt.getY() - 5, 10, 10);
            Color c = Color.yellow;
            g.setColor(new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), 200));
            g.fill(ellis);
            g.setColor(c);
            g.draw(ellis);
        });
    }
    
    /**
     * Paints the polygon with color selected by the user
     * @see Renderer2DPainter
     */
    @Override
    public void paint(Graphics2D g, StateRenderer2D renderer) {
        polygon.paint(polygonColor, g, renderer);
    }    
}
