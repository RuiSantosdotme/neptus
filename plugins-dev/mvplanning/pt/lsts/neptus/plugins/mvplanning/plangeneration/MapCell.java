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
 * https://www.lsts.pt/neptus/licence.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: tsmarques
 * 14 Mar 2016
 */
package pt.lsts.neptus.plugins.mvplanning.plangeneration;

import java.util.ArrayList;
import java.util.List;

import pt.lsts.neptus.types.coord.LocationType;

/**
 * @author tsmarques
 *
 */
public class MapCell {
    private LocationType cellLoc;
    private List<MapCell> neighbours;
    private boolean hasObstacle;
    
    public MapCell(LocationType cellLocation, boolean hasObstacle) {
        cellLoc = cellLocation;
        neighbours = new ArrayList<>();
        this.hasObstacle = hasObstacle;
    }
    
    public MapCell(LocationType cellLocation, List<MapCell> neighbours, boolean hasObstacle) {
        cellLoc = cellLocation;
        this.neighbours = neighbours;
        this.hasObstacle = hasObstacle;
    }
    
    public void addNeighbour(MapCell neighCell) {
        neighbours.add(neighCell);
    }
    
    public boolean isNeighbour(MapCell cell) {
        return neighbours.contains(cell);
    }

    public LocationType getLocation() {
        return cellLoc;
    }
    
    public void setHasObstacle(boolean value) {
        hasObstacle = value;
    }
    
    public boolean hasObstacle() {
       return hasObstacle; 
    }
}
