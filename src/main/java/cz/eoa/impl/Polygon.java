package cz.eoa.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.awt.*;

/**
 * @author Ondřej Kratochvíl
 */
@Getter
@EqualsAndHashCode(of = {"points", "colour"})
public class Polygon {

    private final int[][] points;
    private Color colour;

    public Polygon(int vertices, Color colour) {
        this.points = new int[vertices][2];
        this.colour = colour;
    }

    public Polygon(int[][] points, Color colour) {
        this.points = points;
        this.colour = colour;
    }

    public int getNumOfVertices() {
        return points.length;
    }
}
