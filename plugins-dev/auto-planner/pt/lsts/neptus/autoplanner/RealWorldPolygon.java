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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import pt.lsts.neptus.console.ConsoleLayout;
import pt.lsts.neptus.mp.templates.PlanCreator;
import pt.lsts.neptus.types.coord.LocationType;
import pt.lsts.neptus.types.coord.UTMCoordinates;
import pt.lsts.neptus.types.mission.plan.PlanType;


/**
 * @author Equipa C - SEAI 2016
 *
 */
public class RealWorldPolygon {

    private List<LocationType> polygonLL = null;
    private List<UTMCoordinates> polygonUTM = null;
    final static float rad2deg = (float)(180 / Math.PI);
    final static float deg2rad = (float)(1.0 / rad2deg);
    private List<WaypointPolygon> waypoints = null;
    private List<LocationType> orderedWaypoints = null;
    
    /**
     * 
     */
    public RealWorldPolygon() {
        polygonLL = new ArrayList<LocationType>();
        polygonUTM = new ArrayList<UTMCoordinates>();
        waypoints = new ArrayList<WaypointPolygon>();
        orderedWaypoints = new ArrayList<LocationType>();
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
    
    Rect getPolyMinMax(List<LocationType> poly)
    {
        if (poly.size() == 0)
            return new Rect();

        double minx, miny, maxx, maxy;

        minx = maxx = poly.get(0).getLongitudeDegs();
        miny = maxy = poly.get(0).getLatitudeDegs();
        
        for (LocationType pnt : poly) {
            //Console.WriteLine(pnt.ToString());
            minx = Math.min(minx, pnt.getLongitudeDegs());
            maxx = Math.max(maxx, pnt.getLongitudeDegs());

            miny = Math.min(miny, pnt.getLatitudeDegs());
            maxy = Math.max(maxy, pnt.getLatitudeDegs());
        }

        return new Rect(minx, maxy, maxx, miny);
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
    
    public LocationType getIntersection(LineLLUTM l1, LineLLUTM l2) {
        
        double px, py;
        
        double x1 = l1.getP1().getLongitudeDegs();
        double y1 = l1.getP1().getLatitudeDegs();
        
        double x2 = l1.getP2().getLongitudeDegs();
        double y2 = l1.getP2().getLatitudeDegs();
        
        double x3 = l2.getP1().getLongitudeDegs();
        double y3 = l2.getP1().getLatitudeDegs();
        
        double x4 = l2.getP2().getLongitudeDegs();
        double y4 = l2.getP2().getLatitudeDegs();
        
        if(((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4)) == 0) {
            return null;
        } else {
        
            px = ((x1*y2-y1*x2)*(x3-x4)-(x1-x2)*(x3*y4-y3*x4))/((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));
            py = ((x1*y2-y1*x2)*(y3-y4)-(y1-y2)*(x3*y4-y3*x4))/((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));
            
            return new LocationType(py, px);
        }
        
    }
    
    private int getIdClosestPointNotUsed(LocationType point) {
        
        int id=0;
        double aux = 0;
        double min=Double.MAX_VALUE;
        
        for(int i = 0; i < waypoints.size(); i++) {
            if(!waypoints.get(i).used) {
                aux = Math.min(min, point.getDistanceInMeters(waypoints.get(i).point));
                if (min!=aux) {
                    id=i;
                    min=aux;
                }
            }
        }
        
        return id;
    }
    
    private int getIdClosestPoint(WaypointPolygon last, List<Integer> oriPoints) {
        
        int id=0;
        double aux = 0;
        double min=Double.MAX_VALUE;
        
        WaypointPolygon aux2;
        
        for(int i = 0; i < oriPoints.size(); i++) {
            
            aux2=waypoints.get(oriPoints.get(i));
            
            if(last != aux2) {
                aux = Math.min(min, last.point.getDistanceInMeters(aux2.point));
                if (min!=aux) {
                    id=oriPoints.get(i);
                    min=aux;
                }
            }
        }
        
        return id;
    }
    
//    private boolean alreadyExists(LocationType point, List<WaypointPolygon> pointList) {
//        
//        for (WaypointPolygon pnt : pointList) {
//            if((pnt.point.getLongitudeDegs() == point.getLongitudeDegs()) && (pnt.point.getLatitudeDegs() == point.getLatitudeDegs()))
//                return true;
//        }
//        
//        return false;       
//    }
    
    boolean contains(List<LocationType> polyg, LocationType test)
    {
        double testx = test.getLongitudeDegs();
        double testy = test.getLatitudeDegs();
        int nvert = polyg.size();
        int i, j;
        boolean c = false;
        for (i = 0, j = nvert-1; i < nvert; j = i++) {
            if ( ((polyg.get(i).getLatitudeDegs()>testy) != (polyg.get(j).getLatitudeDegs()>testy)) &&
                    (testx < (polyg.get(j).getLongitudeDegs()-polyg.get(i).getLongitudeDegs()) * (testy-polyg.get(i).getLatitudeDegs()) / (polyg.get(j).getLatitudeDegs()-polyg.get(i).getLatitudeDegs()) + polyg.get(i).getLongitudeDegs()) )
                    c = !c;
            }
        return c;
    }
    
    private LocationType getPointInBetween(WaypointPolygon last, WaypointPolygon next) {
        double x, y;
        
        x = (last.point.getLongitudeDegs() + next.point.getLongitudeDegs())/2;
        
        y = (last.point.getLatitudeDegs() + next.point.getLatitudeDegs())/2;
        
        return new LocationType(y,x);
    }

    
    private List<Integer> getSameOrientationPointInside(WaypointPolygon last, List<WaypointPolygon> pointList) {
        
        List<Integer> oriPoints = new ArrayList<Integer>();
            
        for (int i = 0; i < pointList.size(); i++ ) {
            
            if((pointList.get(i).idOrientacao == last.idOrientacao) && (pointList.get(i) != last) && (contains(polygonLL,getPointInBetween(last, pointList.get(i))))) {
                LineLLUTM linePolygon, line = null;
                LocationType inter = null;
                line = new LineLLUTM(last.point, pointList.get(i).point);
                int numInterDentro = 0;
                for (int j = 0; j < polygonLL.size();j++) {
                    
                    if(j == polygonLL.size()-1)
                        linePolygon = new LineLLUTM(polygonLL.get(j), polygonLL.get(0));
                    else
                        linePolygon = new LineLLUTM(polygonLL.get(j), polygonLL.get(j+1));
                    
                    inter = getIntersection(linePolygon, line);
                    
                    boolean bool1 = ((inter.getLatitudeDegs() > Math.min(linePolygon.getP1().getLatitudeDegs(), linePolygon.getP2().getLatitudeDegs())) && 
                          (inter.getLatitudeDegs() < Math.max(linePolygon.getP1().getLatitudeDegs(), linePolygon.getP2().getLatitudeDegs())) && 
                          (inter.getLongitudeDegs() > Math.min(linePolygon.getP1().getLongitudeDegs(), linePolygon.getP2().getLongitudeDegs())) &&
                          (inter.getLongitudeDegs() < Math.max(linePolygon.getP1().getLongitudeDegs(), linePolygon.getP2().getLongitudeDegs())));
                    boolean bool2 = ((inter.getLatitudeDegs() + offsetMtoLL(1) > Math.min(line.getP1().getLatitudeDegs(), line.getP2().getLatitudeDegs())) && 
                          (inter.getLatitudeDegs() - offsetMtoLL(1) < Math.max(line.getP1().getLatitudeDegs(), line.getP2().getLatitudeDegs())) && 
                          (inter.getLongitudeDegs() + offsetMtoLL(1) > Math.min(line.getP1().getLongitudeDegs(), line.getP2().getLongitudeDegs())) &&
                          (inter.getLongitudeDegs() - offsetMtoLL(1) < Math.max(line.getP1().getLongitudeDegs(), line.getP2().getLongitudeDegs())) &&
                          (inter.getDistanceInMeters(line.getP1()) > 1) &&
                          (inter.getDistanceInMeters(line.getP2()) > 1)
                          );
                    
                    if (bool1 && bool2) {
                        numInterDentro++;
                        break;
                    }
                    
                    //
                    
//                   double dist1 = inter.getDistanceInMeters(line.getP1());
//                   double dist2 = inter.getDistanceInMeters(line.getP2());
//                   boolean bool1 = ((inter.getLatitudeDegs() > Math.min(linePolygon.getP1().getLatitudeDegs(), linePolygon.getP2().getLatitudeDegs())) && 
//                           (inter.getLatitudeDegs() < Math.max(linePolygon.getP1().getLatitudeDegs(), linePolygon.getP2().getLatitudeDegs())) && 
//                           (inter.getLongitudeDegs() > Math.min(linePolygon.getP1().getLongitudeDegs(), linePolygon.getP2().getLongitudeDegs())) &&
//                           (inter.getLongitudeDegs() < Math.max(linePolygon.getP1().getLongitudeDegs(), linePolygon.getP2().getLongitudeDegs())));
//                   boolean bool2 = ((inter.getLatitudeDegs() + offsetMtoLL(1) > Math.min(line.getP1().getLatitudeDegs(), line.getP2().getLatitudeDegs())) && 
//                           (inter.getLatitudeDegs() - offsetMtoLL(1) < Math.max(line.getP1().getLatitudeDegs(), line.getP2().getLatitudeDegs())) && 
//                           (inter.getLongitudeDegs() + offsetMtoLL(1) > Math.min(line.getP1().getLongitudeDegs(), line.getP2().getLongitudeDegs())) &&
//                           (inter.getLongitudeDegs() - offsetMtoLL(1) < Math.max(line.getP1().getLongitudeDegs(), line.getP2().getLongitudeDegs())) &&
//                           (line.getP1().getLongitudeDegs() != line.getP2().getLongitudeDegs()) &&
//                           (line.getP1().getLatitudeDegs() != line.getP2().getLatitudeDegs())
//                           );
//                   boolean bool3 = ((inter.getLatitudeDegs() + offsetMtoLL(1) > Math.min(line.getP1().getLatitudeDegs(), line.getP2().getLatitudeDegs())) && 
//                           (inter.getLatitudeDegs() - offsetMtoLL(1) < Math.max(line.getP1().getLatitudeDegs(), line.getP2().getLatitudeDegs())) &&
//                           (line.getP1().getLongitudeDegs() == line.getP2().getLongitudeDegs()) &&
//                           (Math.abs(inter.getLongitudeDegs() - line.getP2().getLongitudeDegs()) <= offsetMtoLL(1)) &&
//                           (inter.getDistanceInMeters(line.getP1()) > 1) &&
//                           (inter.getDistanceInMeters(line.getP2()) > 1)
//                           );
//                   boolean b1, b2, b3, b4, b5;
//                           b1 = (inter.getLongitudeDegs() + offsetMtoLL(1) > Math.min(line.getP1().getLongitudeDegs(), line.getP2().getLongitudeDegs()));
//                           b2 = (inter.getLongitudeDegs() - offsetMtoLL(1) < Math.max(line.getP1().getLongitudeDegs(), line.getP2().getLongitudeDegs()));
//                           b3 = (Math.abs(inter.getLatitudeDegs() - line.getP2().getLatitudeDegs()) <= offsetMtoLL(1));
//                           b4 = (inter.getDistanceInMeters(line.getP1()) > 1);
//                           b5 = (inter.getDistanceInMeters(line.getP2()) > 1);
//                   boolean bool4 = ((inter.getLongitudeDegs() + offsetMtoLL(1) > Math.min(line.getP1().getLongitudeDegs(), line.getP2().getLongitudeDegs())) && 
//                           (inter.getLongitudeDegs() - offsetMtoLL(1) < Math.max(line.getP1().getLongitudeDegs(), line.getP2().getLongitudeDegs())) &&
//                           (line.getP1().getLatitudeDegs() == line.getP2().getLatitudeDegs()) &&
//                           (Math.abs(inter.getLatitudeDegs() - line.getP2().getLatitudeDegs()) <= offsetMtoLL(1)) &&
//                           (inter.getDistanceInMeters(line.getP1()) > 1) &&
//                           (inter.getDistanceInMeters(line.getP2()) > 1)
//                           );
//                    
//                    if(((inter.getLatitudeDegs() > Math.min(linePolygon.getP1().getLatitudeDegs(), linePolygon.getP2().getLatitudeDegs())) && 
//                        (inter.getLatitudeDegs() < Math.max(linePolygon.getP1().getLatitudeDegs(), linePolygon.getP2().getLatitudeDegs())) && 
//                        (inter.getLongitudeDegs() > Math.min(linePolygon.getP1().getLongitudeDegs(), linePolygon.getP2().getLongitudeDegs())) &&
//                        (inter.getLongitudeDegs() < Math.max(linePolygon.getP1().getLongitudeDegs(), linePolygon.getP2().getLongitudeDegs())))
//                        && (
//                                ((inter.getLatitudeDegs() + offsetMtoLL(1) > Math.min(line.getP1().getLatitudeDegs(), line.getP2().getLatitudeDegs())) && 
//                                (inter.getLatitudeDegs() - offsetMtoLL(1) < Math.max(line.getP1().getLatitudeDegs(), line.getP2().getLatitudeDegs())) && 
//                                (inter.getLongitudeDegs() + offsetMtoLL(1) > Math.min(line.getP1().getLongitudeDegs(), line.getP2().getLongitudeDegs())) &&
//                                (inter.getLongitudeDegs() - offsetMtoLL(1) < Math.max(line.getP1().getLongitudeDegs(), line.getP2().getLongitudeDegs())) &&
//                                (Math.abs(line.getP1().getLongitudeDegs() - line.getP2().getLongitudeDegs()) >= offsetMtoLL(1)) &&
//                                (Math.abs(line.getP1().getLatitudeDegs() - line.getP2().getLatitudeDegs()) >= offsetMtoLL(1))
//                                )
//                                ||
//                                ((inter.getLatitudeDegs() + offsetMtoLL(1) > Math.min(line.getP1().getLatitudeDegs(), line.getP2().getLatitudeDegs())) && 
//                                (inter.getLatitudeDegs() - offsetMtoLL(1) < Math.max(line.getP1().getLatitudeDegs(), line.getP2().getLatitudeDegs())) &&
//                                (line.getP1().getLongitudeDegs() == line.getP2().getLongitudeDegs()) &&
//                                (Math.abs(inter.getLongitudeDegs() - line.getP2().getLongitudeDegs()) <= offsetMtoLL(1)) &&
//                                (inter.getDistanceInMeters(line.getP1()) > 1) &&
//                                (inter.getDistanceInMeters(line.getP2()) > 1)
//                                )
//                                ||
//                                ((inter.getLongitudeDegs() + offsetMtoLL(1) > Math.min(line.getP1().getLongitudeDegs(), line.getP2().getLongitudeDegs())) && 
//                                (inter.getLongitudeDegs() - offsetMtoLL(1) < Math.max(line.getP1().getLongitudeDegs(), line.getP2().getLongitudeDegs())) &&
//                                (line.getP1().getLatitudeDegs() == line.getP2().getLatitudeDegs()) &&
//                                (Math.abs(inter.getLatitudeDegs() - line.getP2().getLatitudeDegs()) <= offsetMtoLL(1)) &&
//                                (inter.getDistanceInMeters(line.getP1()) > 1) &&
//                                (inter.getDistanceInMeters(line.getP2()) > 1)
//                                )
//                                
//                        )
//                         
//                        ) {
//                         numInterDentro++;
//                         break;
//                        }
                    
                }
                if(numInterDentro == 0) {
                    oriPoints.add(i);
                    break;
                }
                
            }
        }        
        
        return oriPoints;
    }
    
    private List<WaypointPolygon> cleanDuplicatedValues(List<WaypointPolygon> list) {
        
        for (int i = 0; i < list.size(); i++ ) {
            
            for (int j = i+1; j < list.size(); j++) {
            
                if(list.get(i).point == list.get(j).point) {
                    list.remove(i);
                    i--;
                    break;
                }
            }
        }
        
        
        return list;
    }
    
    private int numNotUsed(List<WaypointPolygon> list) {
        int notUsed = 0;
        for (WaypointPolygon pnt : list) {
            if(!pnt.used)
                notUsed++;
        }
        
        return notUsed;
    }
    
    public void createCoverage(ConsoleLayout console, int altitude, String veiculo) {
        
        PlanCreator pc = new PlanCreator(console.getConsole().getMission());
        
        if(veiculo.equals("x8-02"))    
            pc.setSpeed(15, pt.lsts.neptus.mp.Maneuver.SPEED_UNITS.METERS_PS );
        else
            pc.setSpeed(4, pt.lsts.neptus.mp.Maneuver.SPEED_UNITS.METERS_PS );
        
        pc.setZ(altitude, pt.lsts.neptus.mp.ManeuverLocation.Z_UNITS.ALTITUDE);
        
        pc.setLocation(console.getConsole().getMission().getHomeRef());
        
        for (LocationType pnt : orderedWaypoints) {
            pc.setLocation(pnt);
            pc.addGoto(new LinkedHashMap<>());
        }

        
        PlanType plan = pc.getPlan();
        plan.setId("CoveragePlan");
        plan.setVehicle(veiculo);
     
       

        console.getConsole().getMission().addPlan(plan);
        console.getConsole().warnMissionListeners();
        console.getConsole().setPlan(plan);
        console.getConsole().getMission().save(false);
    }
    
//    public void deletePlan(ConsoleLayout console) {
//        TreeMap<String, PlanType> planos = console.getConsole().getMission().getIndividualPlansList();
//        
//        PlanType plaano = planos.get("CoveragePlan");
//        plaano = null;
//    }
    
    

    
    public List<LocationType> CreateGrid(int altitude, String veiculo, double spacing, double angle, StartPosition startpos, boolean shutter, ConsoleLayout console)
    {   //spacing em metros
        //DoDebug();

        if (spacing < 4 && spacing != 0)
            spacing = 4;

        if (polygonLL.size() == 0)
            return new ArrayList<LocationType>();
        
        angle= Math.toRadians(angle);
        
        while(angle > (Math.PI/2))
            angle=angle-Math.PI;
        
        while(angle <= -(Math.PI/2))
            angle=angle+Math.PI;


        Rect area = getPolyMinMax(polygonLL);

        List<LineLLUTM> grid = new ArrayList<LineLLUTM>();
        
        // number of lines
        int lines = 0;  
        
        
        
        if (angle == 0) {
            double auxspacing = offsetMtoLL(spacing);
            for (double auxrising = area.Bottom + auxspacing/2; auxrising <= area.Top; auxrising += auxspacing) {
                
                LineLLUTM line = new LineLLUTM(new LocationType(auxrising, area.Right), new LocationType(auxrising, area.Left));
                grid.add(line);
                lines++;
                
            }
        
        } else if (angle > 0) {
            double auxspacing = offsetMtoLL(spacing)/Math.cos(angle);
            for (double auxdropleft = area.Top - auxspacing/2, auxdropright = ((area.Top - auxspacing/2) + Math.tan(angle)*area.getWidth()) ; auxdropright >= area.Bottom; auxdropright -= auxspacing, auxdropleft -= auxspacing) {
                
                LineLLUTM line = new LineLLUTM(new LocationType(auxdropleft, area.Left), new LocationType(auxdropright, area.Right));
                grid.add(line);
                lines++;
                
            }
            
        } else if (angle < 0) {
            double auxspacing = offsetMtoLL(spacing)/Math.cos(angle*-1);
            for (double auxdropright = area.Top - auxspacing/2, auxdropleft = ((area.Top - auxspacing/2) + Math.tan(angle*-1)*area.getWidth()) ; auxdropleft >= area.Bottom; auxdropright -= auxspacing, auxdropleft -= auxspacing) {
                
                LineLLUTM line = new LineLLUTM(new LocationType(auxdropleft, area.Left), new LocationType(auxdropright, area.Right));
                grid.add(line);
                lines++;
                
            }
            
        } else if (angle == (Math.PI/2)) {
            double auxspacing = offsetMtoLL(spacing);
            for (double auxrising = area.Left + auxspacing/2; auxrising <= area.Right; auxrising += auxspacing) {
                
                LineLLUTM line = new LineLLUTM(new LocationType(area.Bottom, auxrising), new LocationType(area.Top, auxrising));
                grid.add(line);
                lines++;
                
            }
            
        }
            
        LineLLUTM linePolygon = null;
        LocationType inter = null;
        
        for (int i = 0; i < polygonLL.size(); i++) {
            
            if(i == polygonLL.size()-1)
                linePolygon = new LineLLUTM(polygonLL.get(i), polygonLL.get(0));
            else
                linePolygon = new LineLLUTM(polygonLL.get(i), polygonLL.get(i+1));
            
            
            for(int j = 0; j < grid.size(); j++) {
                
                inter = getIntersection(linePolygon, grid.get(j));
                
                if((inter.getLatitudeDegs() > Math.min(linePolygon.getP1().getLatitudeDegs(), linePolygon.getP2().getLatitudeDegs())) && 
                        (inter.getLatitudeDegs() < Math.max(linePolygon.getP1().getLatitudeDegs(), linePolygon.getP2().getLatitudeDegs())) && 
                        (inter.getLongitudeDegs() > Math.min(linePolygon.getP1().getLongitudeDegs(), linePolygon.getP2().getLongitudeDegs()))
                        && (inter.getLongitudeDegs() < Math.max(linePolygon.getP1().getLongitudeDegs(), linePolygon.getP2().getLongitudeDegs()))) {
                    waypoints.add(new WaypointPolygon(inter, j));
                }
                
            }
           
            
        }
        
        
        
        
//        createCoverage(console);
        int debug=0;
        
        
        //waypoints = cleanDuplicatedValues(waypoints);
        WaypointPolygon last = null;
        
        int state = 0;
        
        while(numNotUsed(waypoints) > 0) {
            
            if(state == 0) {
                debug++;
                double aux = 0;
                double min = waypoints.get(0).point.getLatitudeDegs();
                int index = -1;
                for (int i = 0; i < waypoints.size(); i++) {
                    aux = Math.min(min, waypoints.get(i).point.getLatitudeDegs());
                    if (min!=aux) {
                        index=i;
                        min=aux;
                    }
                }
                waypoints.get(index).used=true;
                last = waypoints.get(index); //vai buscar ponto de baixo                
                orderedWaypoints.add(last.point);
                state = 2;
            } else if (state == 1) { //Vai buscar o mais proximo sem ser usado               
                debug++;
                int index = getIdClosestPointNotUsed(last.point);         
                
                waypoints.get(index).used=true;
                last = waypoints.get(index);          
                orderedWaypoints.add(last.point);
                state = 2;
            } else if (state == 2) { //Vai buscar o mais proximo na mesma linha de orientação
                debug++;
                List<Integer> oriPoints = getSameOrientationPointInside(last, waypoints);
                
                int id = getIdClosestPoint(last, oriPoints); // Falta adicionar verificação a ver se não está usado
                
                waypoints.get(id).used=true;
                last = waypoints.get(id);
                orderedWaypoints.add(last.point);
                state = 1;
            }
            
            // https://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html#The Method
            
            
            
        }
        
        createCoverage(console, altitude, veiculo);
           

        return orderedWaypoints;
    }

    /**
     * @return the polygonLL
     */
    public final List<LocationType> getPolygonLL() {
        return polygonLL;
    }


}
