package cz.eoa.impl.function;

import cz.eoa.impl.ImageIndividual;
import cz.eoa.impl.Polygon;
import cz.eoa.templates.Individual;
import cz.eoa.templates.operations.PopulationInitializationStrategy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generate random triangles of random colours.
 *
 * @author Ondřej Kratochvíl
 */
public class PolygonPopulationInitialization implements PopulationInitializationStrategy<List<Polygon>, BufferedImage> {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private final BufferedImage inputImage;
    private final int polygonCount;
    private final float minAlpha;
    private final float maxAlpha;

    public PolygonPopulationInitialization(BufferedImage inputImage, int polygonCount, float minAlpha, float maxAlpha) {
        this.inputImage = inputImage;
        this.polygonCount = polygonCount;
        this.minAlpha = minAlpha;
        this.maxAlpha = maxAlpha;
    }

    @Override
    public Individual<List<Polygon>, BufferedImage> initialize() {
        List<Polygon> genes = new ArrayList<>(polygonCount);
        float alpha = RANDOM.nextFloat() * (maxAlpha - minAlpha) + minAlpha;
        for (int i = 0; i < polygonCount; ++i) {
            // generate triangles
            int[][] vertices = new int[3][];
            for (int j = 0; j < vertices.length; ++j) {
                vertices[i] = new int[]{RANDOM.nextInt(inputImage.getWidth()), RANDOM.nextInt(inputImage.getHeight())};
            }
            Polygon polygon = new Polygon(
                    vertices,
                    new Color(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat(), alpha)
            );
            genes.add(polygon);
        }
        return new ImageIndividual(genes);
    }
}
