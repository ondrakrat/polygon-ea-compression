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
        assert vertices > 0;
        this.points = new int[vertices][2];
        this.colour = colour;
    }

    public Polygon(int[][] points, Color colour) {
        assert points.length > 0 && points[0].length == 2;
        this.points = points;
        this.colour = colour;
    }

    /**
     * Copy constructor.
     *
     * @param other instance from which the data should be deep copied
     */
    public Polygon(Polygon other) {
        int[][] pointsCopy = new int[other.points.length][other.points[0].length];
        for (int i = 0; i < pointsCopy.length; ++i) {
            System.arraycopy(other.points[i], 0, pointsCopy[i], 0, pointsCopy[0].length);
        }
        this.points = pointsCopy;
        Color otherColour = other.colour;
        this.colour = new Color(
                otherColour.getRed(),
                otherColour.getGreen(),
                otherColour.getBlue(),
                otherColour.getAlpha()
        );
    }

    public int getNumOfVertices() {
        return points.length;
    }
}
