/*
 * Copyright (c) 2004-2015 Universidade do Porto - Faculdade de Engenharia
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
 * 30 Jun 2015
 */
package pt.lsts.neptus.hyperspectral;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.vecmath.Point2d;

import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.HyperSpecData;
import pt.lsts.imc.IMCMessage;
import pt.lsts.neptus.comm.IMCUtils;
import pt.lsts.neptus.i18n.I18n;
import pt.lsts.neptus.mra.importers.IMraLog;
import pt.lsts.neptus.mra.importers.IMraLogGroup;
import pt.lsts.neptus.mra.replay.LogReplayLayer;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.renderer2d.LayerPriority;
import pt.lsts.neptus.renderer2d.StateRenderer2D;
import pt.lsts.neptus.types.coord.LocationType;

/**
 * @author tsmarques
 *
 */
@LayerPriority(priority=-10)
@PluginDescription(icon="pt/lsts/neptus/mra/replay/globe.png")
public class HyperspectralReplay implements LogReplayLayer {
    private final List<HyperspectralData> dataset = new ArrayList<HyperspectralData>();

    public HyperspectralReplay() {

    }

    @Override
    public String getName() {
        return I18n.text("Hyperspectral Replay");
    }

    @Override
    public void paint(Graphics2D g, StateRenderer2D renderer) {
        if(dataset.isEmpty())
            return;

        for(int i = 0; i < dataset.size(); i++) {
//            System.out.println(dataset.size());
            HyperspectralData frame = dataset.get(i);
            Point2D dataPosition = renderer.getScreenPosition(frame.dataLocation);

            /* draw data with its center in the EstimatedState position */
            int dataX = (int) dataPosition.getX()- (frame.data.getWidth() / 2);
            int dataY = (int) dataPosition.getY() - (frame.data.getHeight() / 2);

            g.drawImage(frame.data, (int) dataPosition.getX(), dataY, null, renderer);
//            g.drawImage(frame.data, 10 + i, renderer.getHeight() /2, null, renderer);
        }
    }

    @Override
    public boolean canBeApplied(IMraLogGroup source, Context context) {
        //return source.getLog("HyperSpecData") != null;
        return true;
    }


    @Override
    public void parse(IMraLogGroup source) {
        //IMraLog hyperspecLog = source.getLog("hyperspecData");
        Queue<byte[]> frames = HyperspectralViewer.loadFrames("320/");
        IMraLog esLog = source.getLog("EstimatedState");
        EstimatedState state = (EstimatedState) esLog.firstLogEntry();

        while(state != null && !frames.isEmpty()) {
            HyperspectralData newData = new HyperspectralData(frames.poll(), state);
            dataset.add(newData);

            state = (EstimatedState) esLog.nextLogEntry();
        }
    }


    @Override
    public String[] getObservedMessages() {
        return null;
    }

    @Override
    public void onMessage(IMCMessage message) {

    }

    @Override
    public boolean getVisibleByDefault() {
        return true;
    }

    @Override
    public void cleanup() {

    }

    private class HyperspectralData {
        private double rotationAngle;
        public BufferedImage data;
        public LocationType dataLocation;

        private AffineTransform tx;
        private AffineTransformOp op;

        public HyperspectralData(byte[] dataBytes, EstimatedState state) {
            try {
                data = ImageIO.read(new ByteArrayInputStream(dataBytes));
                dataLocation = IMCUtils.parseLocation(state);
                rotationAngle = setRotationAngle(state.getPsi());

                /* Lay data horizontally and then rotate according to EstimatedState heading */
                tx = AffineTransform.getRotateInstance(rotationAngle, data.getWidth() / 2, data.getHeight() / 2);
                op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

                /* rotate data according to EstimatedState heading */
                data = op.filter(data, null);
            }
            catch (IOException e) { e.printStackTrace(); }
        }

        /* angle = 90 + (heading + 90) */
        private double setRotationAngle(double psi) {
            System.out.println(Math.abs(Math.toDegrees(psi)));
            double angle;
            psi = (Math.toDegrees(psi));
            if(psi < 0)
                angle = 360 + psi;
            else
                angle = psi;

            angle -= 90; /* make frame perpendicular to vehicles heading    */

            return Math.toRadians(angle);
        }
    }
}
