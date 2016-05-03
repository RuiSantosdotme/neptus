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
 * 2 May 2016
 */
package pt.lsts.neptus.plugins.mvplanning.tests;

import java.util.List;

import pt.lsts.neptus.data.Pair;
import pt.lsts.neptus.plugins.mvplanning.planning.MapCell;
import pt.lsts.neptus.plugins.mvplanning.planning.algorithm.MST;
import pt.lsts.neptus.types.coord.LocationType;

/**
 * @author tsmarques
 *
 */
public class TestMST {
    public static void printMST(MapCell startCell) {
        MST mst = new MST(startCell);
        List<Pair<MapCell, MapCell>> edges = mst.getEdges();

        System.out.println("* Minimum spanning tree edges: ");

        for(Pair<MapCell, MapCell> edge : edges)
            System.out.println("** " + edge.first().id() + " -> " + edge.second().id());
    }

    public static void main(String[] args) {
        MapCell A = new MapCell(LocationType.FEUP, false);
        MapCell B = new MapCell(LocationType.FEUP, false);
        MapCell C = new MapCell(LocationType.FEUP, false);
        MapCell D = new MapCell(LocationType.FEUP, false);
        MapCell E = new MapCell(LocationType.FEUP, false);
        MapCell F = new MapCell(LocationType.FEUP, false);
        MapCell G = new MapCell(LocationType.FEUP, false);
        MapCell H = new MapCell(LocationType.FEUP, false);

        A.setId("A");
        B.setId("B");
        C.setId("C");
        D.setId("D");
        E.setId("E");
        F.setId("F");
        G.setId("G");
        H.setId("H");

        A.addNeighbour(B);
        A.addNeighbour(C);

        B.addNeighbour(A);
        B.addNeighbour(D);

        C.addNeighbour(A);
        C.addNeighbour(D);

        D.addNeighbour(B);
        D.addNeighbour(C);
        D.addNeighbour(E);

        E.addNeighbour(D);
        E.addNeighbour(F);
        E.addNeighbour(G);

        F.addNeighbour(E);
        F.addNeighbour(H);

        G.addNeighbour(E);
        G.addNeighbour(H);

        H.addNeighbour(F);
        H.addNeighbour(G);

        printMST(A);
    }
}