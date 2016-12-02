/*
 * Copyright (c) 2004-2016 Universidade do Porto - Faculdade de Engenharia
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
 * Author: grifo
 * 30/11/2016
 */
package pt.lsts.neptus.autoplanner;

import pt.lsts.neptus.types.coord.LocationType;
import pt.lsts.neptus.types.coord.UTMCoordinates;

import java.util.*;


/**
 * @author grifo
 *
 */
public class RealWorldPolygon {

    private List<LocationType> polygonLL = null;
    private List<UTMCoordinates> polygonUTM = null;
    final static float rad2deg = (float)(180 / Math.PI);
    final static float deg2rad = (float)(1.0 / rad2deg);
    private List<UTMCoordinates> waypoints = null;
    
    /**
     * 
     */
    public RealWorldPolygon() {
        polygonLL = new ArrayList<LocationType>();
        polygonUTM = new ArrayList<UTMCoordinates>();
        waypoints = new ArrayList<UTMCoordinates>();
    }
    
    public enum StartPosition {
        
        Home, BottomLeft, TopLeft, BottomRight, TopRight, Point;
        
    }
    
    
    
    public void insertPoint(LocationType point) {
        
        polygonLL.add(point);
        UTMCoordinates pointUTM = new UTMCoordinates(point.getLatitudeDegs(),point.getLongitudeDegs());
        polygonUTM.add(pointUTM);
        
    }
    
    public void printfPolygon() {
        System.out.println("Polygon Coordinates:");
        for (LocationType element : polygonLL) {
            System.out.println("Latitude: "+element.getLatitudeDegs());
            System.out.println("Longitude: "+element.getLongitudeDegs());
        }
        
    }
    
    Rect getPolyMinMax(List<UTMCoordinates> poly)
    {
        if (poly.size() == 0)
            return new Rect();

        double minx, miny, maxx, maxy;

        minx = maxx = poly.get(0).getLongitudeDegrees();
        miny = maxy = poly.get(0).getLatitudeDegrees();
        
        for (UTMCoordinates pnt : poly) {
            //Console.WriteLine(pnt.ToString());
            minx = Math.min(minx, pnt.getLongitudeDegrees());
            maxx = Math.max(maxx, pnt.getLongitudeDegrees());

            miny = Math.min(miny, pnt.getLatitudeDegrees());
            maxy = Math.max(maxy, pnt.getLatitudeDegrees());
        }

        return new Rect(minx, maxy, maxx, miny);
    }
    
    // polar x to rectangular
    static double newposx(double x, double bearing, double distance)
    {
        double degN = 90 - bearing;
        if (degN < 0)
            degN += 360;
        return (x + distance * Math.cos(degN * deg2rad));
    }
    // polar y to rectangular
    static double newposy(double y, double bearing, double distance)
    {
        double degN = 90 - bearing;
        if (degN < 0)
            degN += 360;
        return (y + distance * Math.sin(degN * deg2rad));
    }
    
    double offsetMtoLL(double dn) {

        //Earth’s radius, sphere
        double R=6378137;

        //Coordinate offsets in radians
        double dLat = dn/R;

        //OffsetPosition, decimal degrees
        double latO = dLat * 180/Math.PI;
        
        return latO;
        
    }
    
    public UTMCoordinates getIntersection(LineLLUTM l1, LineLLUTM l2) {
        
        double px, py;
        
        double x1 = l1.getP1().getLongitudeDegrees();
        double y1 = l1.getP1().getLatitudeDegrees();
        
        double x2 = l1.getP2().getLongitudeDegrees();
        double y2 = l1.getP2().getLatitudeDegrees();
        
        double x3 = l2.getP1().getLongitudeDegrees();
        double y3 = l2.getP1().getLatitudeDegrees();
        
        double x4 = l2.getP2().getLongitudeDegrees();
        double y4 = l2.getP2().getLatitudeDegrees();
        
        if(((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4)) == 0) {
            return null;
        } else {
        
            px = ((x1*y2-y1*x2)*(x3-x4)-(x1-x2)*(x3*y4-y3*x4))/((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));
            py = ((x1*y2-y1*x2)*(y3-y4)-(y1-y2)*(x3*y4-y3*x4))/((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));
            
            return new UTMCoordinates(py, px);
        }
        
    }
    
    public List<UTMCoordinates> CreateGrid(double altitude, double distance, double spacing, double angle, double overshoot1,double overshoot2, StartPosition startpos, boolean shutter, float minLaneSeparation, float leadin)
    {
        //DoDebug();

        if (spacing < 4 && spacing != 0)
            spacing = 4;

        if (distance < 0.1)
            distance = 0.1;

        if (polygonUTM.size() == 0)
            return new ArrayList<UTMCoordinates>();

        
        // Make a non round number in case of corner cases
        if (minLaneSeparation != 0)
            minLaneSeparation += 0.5F;
        // Lane Separation in meters
        double minLaneSeparationINMeters = minLaneSeparation * distance;

        List<UTMCoordinates> ans = new ArrayList<UTMCoordinates>();

        // utm zone distance calcs will be done in
        int utmzone = polygonUTM.get(0).getZoneNumber();
        

        // utm position list
//        List<utmpos> utmpositions = utmpos.ToList(PointLatLngAlt.ToUTM(utmzone, polygon), utmzone);
//
//        // close the loop if its not already
//        if (utmpositions[0] != utmpositions[utmpositions.Count - 1])
//            utmpositions.Add(utmpositions[0]); // make a full loop
//
        // get mins/maxs of coverage area ESTÁ ERRADO, O RECTANGULO NÃO TEM OS VARIOS PONTOS LÁ DENTRO
        Rect area = getPolyMinMax(polygonUTM);

        // get initial grid

        // used to determine the size of the outer grid area
        double diagdist = area.getDiagDistance();

        // somewhere to store out generated lines
        List<LineLLUTM> grid = new ArrayList<LineLLUTM>();
        // number of lines we need
        int lines = 0;
        
        double auxspacing = offsetMtoLL(spacing);
        
        for (double auxrising = area.Bottom; auxrising <= area.Top; auxrising += auxspacing) {
            
            LineLLUTM line = new LineLLUTM(new UTMCoordinates(auxrising, area.Right), new UTMCoordinates(auxrising, area.Left));
            grid.add(line);
            lines++;
            
        }
        
        LineLLUTM linePolygon = null;
        UTMCoordinates inter = null;
        
        for (int i = 0; i < polygonUTM.size(); i++) {
            
            if(i == polygonUTM.size()-1)
                linePolygon = new LineLLUTM(polygonUTM.get(i), polygonUTM.get(0));
            else
                linePolygon = new LineLLUTM(polygonUTM.get(i), polygonUTM.get(i+1));
            
            
            for(int j = 0; j < grid.size(); j++) {
                
                inter = getIntersection(linePolygon, grid.get(j));
                
                if((inter.getLatitudeDegrees() > area.Bottom) && (inter.getLatitudeDegrees() < area.Top) && (inter.getLongitudeDegrees() < area.Right) && (inter.getLongitudeDegrees() > area.Left)) {
                    waypoints.add(inter);
                }
                
            }
           
            
        }
        

        return null;
    }


}
