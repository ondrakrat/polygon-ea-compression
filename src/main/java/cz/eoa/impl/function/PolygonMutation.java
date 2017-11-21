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
public class PolygonMutation implements MutationStrategy<List<Polygon>, BufferedImage> {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private final BufferedImage inputImage;
    private final double mutationRate;
    private final int colourMutationDelta;
    private final double mutationExtent;

    public PolygonMutation(double mutationRate, double mutationExtent, BufferedImage inputImage) {
        assert mutationRate >= 0 && mutationRate <= 1;
        this.mutationRate = mutationRate;
        this.mutationExtent = mutationExtent;
        this.colourMutationDelta = (int) (255 * mutationExtent);
        this.inputImage = inputImage;
    }

    @Override
    public Optional<Individual<List<Polygon>, BufferedImage>> mutation(Individual<List<Polygon>, BufferedImage> individual) {
        List<Polygon> originalGenes = individual.getGenes();
        List<Polygon> mutatedGenes = originalGenes.stream() // TODO parallel
                .map(polygon -> {
                    Color mutatedColour = mutateColour(polygon.getColour());
                    int[][] mutatedPoints = mutatePoints(polygon.getPoints());
                    return new Polygon(mutatedPoints, mutatedColour);
                })
                .collect(Collectors.toList());
        return Optional.of(new ImageIndividual(mutatedGenes));
    }

    private Color mutateColour(Color originalColour) {
        if (RANDOM.nextDouble() < mutationRate) {
            int alpha = originalColour.getAlpha();  // do not modify alpha
            int red = originalColour.getRed();
            int green = originalColour.getGreen();
            int blue = originalColour.getBlue();
            return new Color(mutateColourPart(red), mutateColourPart(green), mutateColourPart(blue), alpha);
        } else {
            return new Color(
                    originalColour.getRed(),
                    originalColour.getGreen(),
                    originalColour.getBlue(),
                    originalColour.getAlpha()
            );
        }
    }

    private int mutateColourPart(int originalColour) {
        return RANDOM.nextInt(
                Math.max(0, originalColour - colourMutationDelta),
                Math.min(255, originalColour + colourMutationDelta) + 1
        );
    }

    private int[][] mutatePoints(int[][] originalPoints) {
        int xMutationDelta = (int) (inputImage.getWidth() * mutationExtent);
        int yMutationDelta = (int) (inputImage.getHeight() * mutationExtent);
        int[][] mutatedPoints = new int[originalPoints.length][originalPoints[0].length];

        for (int i = 0; i < mutatedPoints.length; ++i) {
            int[] originalPoint = originalPoints[i];
            // TODO mutate all points, or only random? Can be weighted out by adjusting mutationExtent/rate
            if (RANDOM.nextDouble() < mutationRate) {
                int xCoord = RANDOM.nextInt(
                        Math.max(0, originalPoint[0] - xMutationDelta),
                        Math.min(inputImage.getWidth(), originalPoint[0] + xMutationDelta)
                );
                int yCoord = RANDOM.nextInt(
                        Math.max(0, originalPoint[1] - yMutationDelta),
                        Math.min(inputImage.getHeight(), originalPoint[1] + yMutationDelta)
                );
                mutatedPoints[i] = new int[]{xCoord, yCoord};
            } else {
                mutatedPoints[i] = new int[]{originalPoint[0], originalPoint[1]};
            }
        }
        return mutatedPoints;
    }
}
