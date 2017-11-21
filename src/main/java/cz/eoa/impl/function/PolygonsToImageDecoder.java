package cz.eoa.impl.function;

import cz.eoa.impl.Polygon;
import cz.eoa.templates.operations.DecodingStrategy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author Ondřej Kratochvíl
 */
public class PolygonsToImageDecoder implements DecodingStrategy<List<Polygon>, BufferedImage> {

    private final int width;
    private final int height;
    private final int type;

    public PolygonsToImageDecoder(int width, int height, int type) {
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public PolygonsToImageDecoder(BufferedImage inputImage) {
        this.width = inputImage.getWidth();
        this.height = inputImage.getHeight();
        this.type = inputImage.getType();
    }

    @Override
    public BufferedImage decode(List<Polygon> genes) {
        BufferedImage bufferedImage = new BufferedImage(width, height, type);
        Graphics2D graphics = bufferedImage.createGraphics();

        for (Polygon gene : genes) {
            int[][] points = gene.getPoints();
            int[] xPoints = new int[points.length];
            int[] yPoints = new int[points.length];
            for (int i = 0; i < points.length; ++i) {
                xPoints[i] = points[i][0];
                yPoints[i] = points[i][1];
            }
            java.awt.Polygon polygon = new java.awt.Polygon(xPoints, yPoints, 3);
            graphics.setColor(gene.getColour());
            graphics.fillPolygon(polygon);
        }

        return bufferedImage;
    }
}
