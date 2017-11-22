package cz.eoa.impl.function;

import cz.eoa.impl.ImageIndividual;
import cz.eoa.impl.Polygon;
import cz.eoa.templates.Individual;
import cz.eoa.templates.operations.MutationStrategy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Ondřej Kratochvíl
 */
public class PolygonReplacementMutation implements MutationStrategy<List<Polygon>, BufferedImage> {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private final double mutationRate;
    private final BufferedImage inputImage;
    private final float minAlpha;
    private final float maxAlpha;

    public PolygonReplacementMutation(double mutationRate, float minAlpha, float maxAlpha, BufferedImage inputImage) {
        assert mutationRate >= 0 && mutationRate <= 1;
        this.mutationRate = mutationRate;
        this.inputImage = inputImage;
        this.minAlpha = minAlpha;
        this.maxAlpha = maxAlpha;
    }

    @Override
    public Optional<Individual<List<Polygon>, BufferedImage>> mutation(Individual<List<Polygon>, BufferedImage> individual) {
        List<Polygon> polygons = individual.getGenes();
        List<Polygon> mutatedPolygons = polygons.parallelStream()   // TODO not parallel?
                .map(polygon -> {
                    if (RANDOM.nextDouble() < mutationRate) {
                        // vertices
                        int[][] originalPoints = polygon.getPoints();
                        int[][] points = new int[originalPoints.length][originalPoints[0].length];
                        for (int j = 0; j < points.length; ++j) {
                            points[j] = new int[]{
                                    RANDOM.nextInt(inputImage.getWidth()),
                                    RANDOM.nextInt(inputImage.getHeight())
                            };
                        }

                        // colour
                        float alpha = RANDOM.nextFloat() * (maxAlpha - minAlpha) + minAlpha;
                        Color colour = new Color(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat(), alpha);
                        return new Polygon(points, colour);
                    } else {
                        return new Polygon(polygon);
                    }
                })
                .collect(Collectors.toList());
        return Optional.of(new ImageIndividual(mutatedPolygons));
    }
}
