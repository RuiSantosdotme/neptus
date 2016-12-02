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
        
        
        
        

//        // get start point middle
//        double x = area.getMidWidth();
//        double y = area.getMidHeight();
//
//        //Deve ter a haver com a interface grafica, fica comentado por agora
//        //addtomap(new utmpos(x, y, utmzone),"Base");
//
//        
//        //Deve ter a haver com a interface grafica, fica comentado por agora 
//        // get left extent
//        double xb1 = x;
//        double yb1 = y;
//        
//        // to the left
//        xb1=newposx(xb1, angle - 90, diagdist / 2 + distance);
//        yb1=newposy(yb1, angle - 90, diagdist / 2 + distance);
//        // backwards
//        xb1=newposx(xb1, angle + 180, diagdist / 2 + distance);
//        yb1=newposy(yb1, angle + 180, diagdist / 2 + distance);
//
////        utmpos left = new utmpos(xb1, yb1, utmzone);
////
////        addtomap(left, "left");
//
//        // get right extent
//        double xb2 = x;
//        double yb2 = y;
//        // to the right
//        xb2=newposx(xb2, angle + 90, diagdist / 2 + distance);
//        yb2=newposy(yb2, angle + 90, diagdist / 2 + distance);
//        // backwards
//        xb2=newposx(xb2, angle + 180, diagdist / 2 + distance);
//        yb2=newposy(yb2, angle + 180, diagdist / 2 + distance);
//
////        utmpos right = new utmpos(xb2, yb2, utmzone);
////
////        addtomap(right,"right");
//
//        // set start point to left hand side
//        x = xb1;
//        y = yb1;
//
//        // draw the outergrid, this is a grid that cover the entire area of the rectangle plus more.
//        while (lines < ((diagdist + distance * 2) / distance))
//        {
//            // copy the start point to generate the end point
//            double nx = x;
//            double ny = y;
//            nx = newposx(nx, angle, diagdist + distance*2);
//            ny = newposy(ny, angle, diagdist + distance*2);
//
//            LineLatLng line = new LineLatLng(new UTMCoordinates(y, x), new UTMCoordinates(ny, nx), new UTMCoordinates(y, x));
//            grid.add(line);
//
//           // addtomap(line);
//
//            x = newposx(x, angle + 90, distance);
//            y = newposx(y, angle + 90, distance);
//            lines++;
//        }
//
//        // find intersections with our polygon
//
//        // store lines that dont have any intersections
//        List<LineLatLng> remove = new ArrayList<LineLatLng>();
//
//        int gridno = grid.size();
//
//        // cycle through our grid
//        for (int a = 0; a < gridno; a++)
//        {
//            double closestdistance = double.MaxValue;
//            double farestdistance = double.MinValue;
//
//            utmpos closestpoint = utmpos.Zero;
//            utmpos farestpoint = utmpos.Zero;
//
//            // somewhere to store our intersections
//            List<UTMCoordinates> matchs = new ArrayList<UTMCoordinates>();
//
//            int b = -1;
//            int crosses = 0;
//            UTMCoordinates newutmpos = utmpos.Zero;
//            
//            for (UTMCoordinates pnt : polygonUTM) {
//                b++;
//                if (b == 0)
//                {
//                    continue;
//                }
//                newutmpos = FindLineIntersection(utmpositions[b - 1], utmpositions[b], grid[a].p1, grid[a].p2);
//                if (!newutmpos.IsZero)
//                {
//                    crosses++;
//                    matchs.Add(newutmpos);
//                    if (closestdistance > grid[a].p1.GetDistance(newutmpos))
//                    {
//                        closestpoint.y = newutmpos.y;
//                        closestpoint.x = newutmpos.x;
//                        closestpoint.zone = newutmpos.zone;
//                        closestdistance = grid[a].p1.GetDistance(newutmpos);
//                    }
//                    if (farestdistance < grid[a].p1.GetDistance(newutmpos))
//                    {
//                        farestpoint.y = newutmpos.y;
//                        farestpoint.x = newutmpos.x;
//                        farestpoint.zone = newutmpos.zone;
//                        farestdistance = grid[a].p1.GetDistance(newutmpos);
//                    }
//                }
//            }
//            if (crosses == 0) // outside our polygon
//            {
//                if (!PointInPolygon(grid[a].p1, utmpositions) && !PointInPolygon(grid[a].p2, utmpositions))
//                    remove.Add(grid[a]);
//            }
//            else if (crosses == 1) // bad - shouldnt happen
//            {
//
//            }
//            else if (crosses == 2) // simple start and finish
//            {
//                linelatlng line = grid[a];
//                line.p1 = closestpoint;
//                line.p2 = farestpoint;
//                grid[a] = line;
//            }
//            else // multiple intersections
//            {
//                linelatlng line = grid[a];
//                remove.Add(line);
//
//                while (matchs.Count > 1)
//                {
//                    linelatlng newline = new linelatlng();
//
//                    closestpoint = findClosestPoint(closestpoint, matchs);
//                    newline.p1 = closestpoint;
//                    matchs.Remove(closestpoint);
//
//                    closestpoint = findClosestPoint(closestpoint, matchs);
//                    newline.p2 = closestpoint;
//                    matchs.Remove(closestpoint);
//
//                    newline.basepnt = line.basepnt;
//
//                    grid.Add(newline);
//                }
//            }
//        }
//
//        // cleanup and keep only lines that pass though our polygon
//        foreach (linelatlng line in remove)
//        {
//            grid.Remove(line);
//        }
//
//        // debug
//        foreach (linelatlng line in grid)
//        {
//            addtomap(line);
//        }
//
//        if (grid.Count == 0)
//            return ans;
//
//        // pick start positon based on initial point rectangle
//        utmpos startposutm;
//
//        switch (startpos)
//        {
//            default:
//            case StartPosition.Home:
//                startposutm = new utmpos(Host2.cs.HomeLocation);
//                break;
//            case StartPosition.BottomLeft:
//                startposutm = new utmpos(area.Left, area.Bottom, utmzone);
//                break;
//            case StartPosition.BottomRight:
//                startposutm = new utmpos(area.Right, area.Bottom, utmzone);
//                break;
//            case StartPosition.TopLeft:
//                startposutm = new utmpos(area.Left, area.Top, utmzone);
//                break;
//            case StartPosition.TopRight:
//                startposutm = new utmpos(area.Right, area.Top, utmzone);
//                break;
//            case StartPosition.Point:
//                startposutm = new utmpos(StartPointLatLngAlt);
//                break;
//        }
//
//        // find the closes polygon point based from our startpos selection
//        startposutm = findClosestPoint(startposutm, utmpositions);
//
//        // find closest line point to startpos
//        linelatlng closest = findClosestLine(startposutm, grid, 0 /*Lane separation does not apply to starting point*/, angle);
//
//        utmpos lastpnt;
//
//        // get the closes point from the line we picked
//        if (closest.p1.GetDistance(startposutm) < closest.p2.GetDistance(startposutm))
//        {
//            lastpnt = closest.p1;
//        }
//        else
//        {
//            lastpnt = closest.p2;
//        }
//
//        // S =  start
//        // E = end
//        // ME = middle end
//        // SM = start middle
//
//        while (grid.Count > 0)
//        {
//            // for each line, check which end of the line is the next closest
//            if (closest.p1.GetDistance(lastpnt) < closest.p2.GetDistance(lastpnt))
//            {
//                utmpos newstart = newpos(closest.p1, angle, -leadin);
//                newstart.Tag = "S";
//
//                addtomap(newstart, "S");
//                ans.Add(newstart);
//
//                closest.p1.Tag = "SM";
//                addtomap(closest.p1, "SM");
//                ans.Add(closest.p1);
//
//                if (spacing > 0)
//                {
//                    for (int d = (int)(spacing - ((closest.basepnt.GetDistance(closest.p1)) % spacing));
//                        d < (closest.p1.GetDistance(closest.p2));
//                        d += (int)spacing)
//                    {
//                        double ax = closest.p1.x;
//                        double ay = closest.p1.y;
//
//                        newpos(ref ax, ref ay, angle, d);
//                        var utmpos1 = new utmpos(ax, ay, utmzone) {Tag = "M"};
//                        addtomap(utmpos1, "M");
//                        ans.Add(utmpos1);
//                    }
//                }
//
//                closest.p2.Tag = "ME";
//                addtomap(closest.p2, "ME");
//                ans.Add(closest.p2);
//
//                utmpos newend = newpos(closest.p2, angle, overshoot1);
//                newend.Tag = "E";
//                addtomap(newend, "E");
//                ans.Add(newend);
//
//                lastpnt = closest.p2;
//
//                grid.Remove(closest);
//                if (grid.Count == 0)
//                    break;
//
//                closest = findClosestLine(newend, grid, minLaneSeparationINMeters, angle);
//            }
//            else
//            {
//                utmpos newstart = newpos(closest.p2, angle, leadin);
//                newstart.Tag = "S";
//                addtomap(newstart, "S");
//                ans.Add(newstart);
//
//                closest.p2.Tag = "SM";
//                addtomap(closest.p2, "SM");
//                ans.Add(closest.p2);
//
//                if (spacing > 0)
//                {
//                    for (int d = (int)((closest.basepnt.GetDistance(closest.p2)) % spacing);
//                        d < (closest.p1.GetDistance(closest.p2));
//                        d += (int)spacing)
//                    {
//                        double ax = closest.p2.x;
//                        double ay = closest.p2.y;
//
//                        newpos(ref ax, ref ay, angle, -d);
//                        var utmpos2 = new utmpos(ax, ay, utmzone) {Tag = "M"};
//                        addtomap(utmpos2, "M");
//                        ans.Add(utmpos2);
//                    }
//                }
//
//                closest.p1.Tag = "ME";
//                addtomap(closest.p1, "ME");
//                ans.Add(closest.p1);
//
//                utmpos newend = newpos(closest.p1, angle, -overshoot2);
//                newend.Tag = "E";
//                addtomap(newend, "E");
//                ans.Add(newend);
//
//                lastpnt = closest.p1;
//
//                grid.Remove(closest);
//                if (grid.Count == 0)
//                    break;
//                closest = findClosestLine(newend, grid, minLaneSeparationINMeters, angle);
//            }
//        }
//
//        // set the altitude on all points
//        ans.ForEach(plla => { plla.Alt = altitude; });
//
//        return ans;
        return null;
    }


}
