package cz.eoa.impl.function;

import cz.eoa.templates.operations.FitnessAssessmentStrategy;

import java.awt.image.BufferedImage;

import static cz.eoa.impl.GraphicHelper.pixelDiff;

/**
 * @author Ondřej Kratochvíl
 */
public class ImageFitness implements FitnessAssessmentStrategy<BufferedImage, Double> {

    private final BufferedImage inputImage;

    public ImageFitness(BufferedImage inputImage) {
        this.inputImage = inputImage;
    }

    @Override
    public Double computeFitnessForIndividual(BufferedImage solution) {
        double fitness = 0;
        for (int j = 0; j < solution.getHeight(); ++j) {
            for (int i = 0; i < solution.getWidth(); ++i) {
                int inputPixel = inputImage.getRGB(i, j);
                int individualPixel = solution.getRGB(i, j);
                double pixelDiff = pixelDiff(inputPixel, individualPixel);
                fitness += pixelDiff;
            }
        }
        return fitness;
    }
}
