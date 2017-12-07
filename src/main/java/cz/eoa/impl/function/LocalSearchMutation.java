package cz.eoa.impl.function;

import cz.eoa.impl.GraphicHelper;
import cz.eoa.impl.ImageIndividual;
import cz.eoa.impl.Polygon;
import cz.eoa.templates.Individual;
import cz.eoa.templates.operations.DecodingStrategy;
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
public class LocalSearchMutation implements MutationStrategy<List<Polygon>, BufferedImage> {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private static final int TOURNAMENT_SIZE = 10;
    private final BufferedImage inputImage;
    private final ImageFitness fitnessFunction;
    private final DecodingStrategy<List<Polygon>, BufferedImage> decoder;
    private final double mutationRate;
    private final float minAlpha;
    private final float maxAlpha;
    private final int polygonVertices;

    public LocalSearchMutation(
            double mutationRate,
            float minAlpha,
            float maxAlpha,
            BufferedImage inputImage,
            ImageFitness fitnessFunction,
            DecodingStrategy<List<Polygon>, BufferedImage> decoder, int polygonVertices) {
        assert mutationRate >= 0 && mutationRate <= 1;
        this.mutationRate = mutationRate;
        this.inputImage = inputImage;
        this.minAlpha = minAlpha;
        this.maxAlpha = maxAlpha;
        this.fitnessFunction = fitnessFunction;
        this.decoder = decoder;
        this.polygonVertices = polygonVertices;
    }

    @Override
    public Optional<Individual<List<Polygon>, BufferedImage>> mutation(Individual<List<Polygon>, BufferedImage> individual) {
        List<Polygon> polygons = individual.getGenes();
        if (RANDOM.nextDouble() > mutationRate) {
            return Optional.of(new ImageIndividual(polygons));
        }
        List<Polygon> mutatedPolygons = polygons.stream()
                .map(Polygon::new)
                .collect(Collectors.toList());
        mutatePolygon(mutatedPolygons);
        return Optional.of(new ImageIndividual(mutatedPolygons));
    }

    private void mutatePolygon(List<Polygon> polygons) {
//        int index = RANDOM.nextInt(0, polygons.size());
        int index = tournamentSelectionWorstPolygon(polygons);
        int[] worstSegment = fitnessFunction.getWorstSegment(decoder.decode(polygons));
        int xMin = worstSegment[1];
        int xMax = xMin + worstSegment[3];
        int yMin = worstSegment[0];
        int yMax = yMin + worstSegment[2];
        int[][] points = new int[polygonVertices][2];
        for (int i = 0; i < points.length; ++i) {
            points[i] = new int[]{
                    RANDOM.nextInt(xMin, xMax),
                    RANDOM.nextInt(yMin, yMax)
            };
        }
        int diameter = Math.min(xMax - xMin, yMax - yMin);
        int majorityColour = GraphicHelper.
                getMajorityColour(inputImage, xMax - xMin / 2, yMax - yMin / 2, diameter);
        int r = GraphicHelper.getRed(majorityColour);
        int g = GraphicHelper.getGreen(majorityColour);
        int b = GraphicHelper.getBlue(majorityColour);
        float alpha = RANDOM.nextFloat() * (maxAlpha - minAlpha) + minAlpha;
        Polygon polygon = new Polygon(points, new Color(r / 255.0f, g / 255.0f, b / 255.0f, alpha));
        polygons.set(index, polygon);
    }

    private int tournamentSelectionWorstPolygon(List<Polygon> polygons) {
        double worstFitness = -Double.MAX_VALUE;
        int worstIndex = -1;
        for (int i = 0; i < TOURNAMENT_SIZE; ++i) {
            int index = RANDOM.nextInt(0, polygons.size());
            List<Polygon> polygonsWithoutSelected = polygons.stream()
                    .filter(polygon -> !polygons.get(index).equals(polygon))
                    .collect(Collectors.toList());
            Double fitness = fitnessFunction.computeFitnessForIndividual(decoder.decode(polygonsWithoutSelected));
            if (fitness > worstFitness) {
                worstFitness = fitness;
                worstIndex = index;
            }
        }
        return worstIndex;
    }
}
