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
 * 01/12/2016
 */
package pt.lsts.neptus.autoplanner;

import pt.lsts.neptus.types.coord.UTMCoordinates;

/**
 * @author grifo
 *
 */
public class LineLatLng {
    
    // start of line
    private UTMCoordinates p1;
    // end of line
    private UTMCoordinates p2;
    // used as a base for grid along line (initial setout)
    private UTMCoordinates basepnt;
    
    /**
     * 
     */
    public LineLatLng() {
        this.p1 = null;
        this.p2 = null;
        this.basepnt = null;
    }
    
    /**
     * 
     */
    public LineLatLng(UTMCoordinates p1, UTMCoordinates p2, UTMCoordinates basepnt) {
        this.p1 = p1;
        this.p2 = p2;
        this.basepnt = basepnt;
    }
    
    public UTMCoordinates getP1() {
        return p1;
    }
    public UTMCoordinates getP2() {
        return p2;
    }
    public UTMCoordinates getBasepnt() {
        return basepnt;
    }

}
