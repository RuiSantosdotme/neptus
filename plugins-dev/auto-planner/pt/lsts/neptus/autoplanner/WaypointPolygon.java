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
 * 03/12/2016
 */
package pt.lsts.neptus.autoplanner;

import pt.lsts.neptus.types.coord.LocationType;

/**
 * @author grifo
 *
 */
public class WaypointPolygon {

    public LocationType point;
    public boolean used;
    public int idOrientacao;
    
    /**
     * 
     */
    public WaypointPolygon(LocationType point, int idOrientacao) {
        this.point = point;
        used = false;
        this.idOrientacao = idOrientacao;
    }
    
    public WaypointPolygon(LocationType point, int idOrientacao, boolean used) {
        this.point = point;
        used = false;
        this.idOrientacao = idOrientacao;
        this.used = used;
    }
    
    public boolean isUsed() {
        return used;
    }
    
    public void setUsed() {
        used = true;
    }
    public void resetUsed() {
        used = false;
    }
    
}
