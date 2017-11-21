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
            java.awt.Polygon polygon = new java.awt.Polygon();
            for (int[] coords : gene.getPoints()) {
                polygon.addPoint(coords[0], coords[1]);
            }
            graphics.setColor(gene.getColour());
            graphics.fillPolygon(polygon);
        }

        return bufferedImage;
    }
}
