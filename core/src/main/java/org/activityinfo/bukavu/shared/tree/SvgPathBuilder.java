/*
 * Copyright 2014-2023 BeDataDriven Groep B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activityinfo.bukavu.shared.tree;

public class SvgPathBuilder {

    private final StringBuilder d = new StringBuilder();

    private double currentX = Double.NaN;
    private double currentY = Double.NaN;

    public SvgPathBuilder moveTo(double x, double y) {
        d.append("M");
        appendCoords(x, y);
        currentX = x;
        currentY = y;
        return this;
    }

    public SvgPathBuilder moveTo(double[] coord) {
        return moveTo(coord[0], coord[1]);
    }

    public SvgPathBuilder horizontalLineTo(double x) {
        d.append("H");
        d.append(coord(x));
        currentX = x;
        return this;
    }

    public SvgPathBuilder lineTo(double x, double y) {
        d.append("L");
        appendCoords(x, y);
        currentX = x;
        currentY = y;
        return this;
    }

    //A rx ry x-axis-rotation large-arc-flag sweep-flag x y
    private SvgPathBuilder arcTo(double rx, double ry, int axisRotation, int arcFlag, int sweepFlag, double x, double y) {
        d.append("A");
        appendCoords(rx, ry);
        d.append(str(axisRotation));
        d.append(str(arcFlag));
        d.append(str(sweepFlag, true, true));
        appendCoords(x, y);
        currentX = x;
        currentY = y;
        return this;
    }

    //A rx ry x-axis-rotation large-arc-flag sweep-flag x y
    public SvgPathBuilder arcTo(double radius, int arcFlag, double x, double y) {
        return arcTo(radius, radius, 0, arcFlag, 0, x, y);
    }

    public SvgPathBuilder lineTo(double[] coord) {
        return lineTo(coord[0], coord[1]);
    }

    private void appendCoords(double x, double y) {
        d.append(coord(x));
        if(y >= 0) {
            d.append(' ');
        }
        d.append(coord(y));
    }

    private String coord(double coord) {
        return Double.toString(coord);
    }

    private String str(int value, boolean leadSpace, boolean trailSpace) {
        String str = Integer.toString(value);
        str =  leadSpace?" " + str: str;
        return trailSpace?str + " ":str;
    }

    private String str(int value) {
        return str(value, true, false);
    }

    public double[] getCurrentCoord(){
        double[] result = {currentX, currentY};
        return result;
    }

    public boolean isPathStarting(){
        return Double.isNaN(currentX);
    }


    @Override
    public String toString() {
        return d.toString();
    }
}
