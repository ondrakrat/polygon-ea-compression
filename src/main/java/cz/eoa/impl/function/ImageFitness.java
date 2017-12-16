package cz.eoa.impl.function;

import cz.eoa.templates.operations.FitnessAssessmentStrategy;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;

import static cz.eoa.impl.GraphicHelper.pixelDiff;

/**
 * @author Ondřej Kratochvíl
 */
public class ImageFitness implements FitnessAssessmentStrategy<BufferedImage, Double> {

    private final BufferedImage inputImage;
    private final byte[] pixels;
    private final boolean hasAlpha;
    private final int pixelLength;

    private final short[][] alphaArray;
    private final short[][] redArray;
    private final short[][] greenArray;
    private final short[][] blueArray;

    public ImageFitness(BufferedImage inputImage) {
        this.inputImage = inputImage;
        this.pixels = ((DataBufferByte) inputImage.getRaster().getDataBuffer()).getData();
        this.hasAlpha = inputImage.getAlphaRaster() != null;
        this.pixelLength = hasAlpha ? 4 : 3;

        alphaArray = new short[inputImage.getWidth()][inputImage.getHeight()];
        redArray = new short[inputImage.getWidth()][inputImage.getHeight()];
        greenArray = new short[inputImage.getWidth()][inputImage.getHeight()];
        blueArray = new short[inputImage.getWidth()][inputImage.getHeight()];
        extractArrayFromImage();
    }

    @Override
    public Double computeFitnessForIndividual(BufferedImage solution) {
        DataBuffer solutionDataBuffer = solution.getRaster().getDataBuffer();
        return computeFitnessFast(((DataBufferByte) solutionDataBuffer).getData());
//        return computeFitnessSlow(solution);
    }

    private double computeFitnessSlow(BufferedImage solution) {
        double fitness = 0;
        for (int i = 0; i < solution.getWidth(); ++i) {
            for (int j = 0; j < solution.getHeight(); ++j) {
                int inputPixel = inputImage.getRGB(i, j);
                int individualPixel = solution.getRGB(i, j);
                double pixelDiff = pixelDiff(inputPixel, individualPixel);
                fitness += pixelDiff;
            }
        }
        return fitness * -1;
    }

    private double computeFitnessFast(byte[] solutionData) {
        double fitness = 0;
        for (int i = 0; i < inputImage.getWidth(); ++i) {
            for (int j = 0; j < inputImage.getHeight(); ++j) {
                fitness += Math.sqrt(pixelDiffFast(i, j, solutionData));
            }
        }
        return fitness * -1;
    }

    public int[] getWorstSegment(BufferedImage solution) {
        DataBuffer solutionDataBuffer = solution.getRaster().getDataBuffer();
        byte[] solutionData = ((DataBufferByte) solutionDataBuffer).getData();
        int factor = 10;
        double[][] segmentMatrix =
                new double[(inputImage.getHeight() / factor) + 1][(inputImage.getWidth() / factor) + 1];

        for (int i = 0; i < inputImage.getWidth(); ++i) {
            for (int j = 0; j < inputImage.getHeight(); ++j) {
                segmentMatrix[j / factor][i / factor] += pixelDiffFast(i, j, solutionData);
            }
        }

        double worstFitness = Double.MIN_VALUE;
        int worstI = -1;
        int worstJ = -1;
        for (int i = 0; i < segmentMatrix.length; ++i) {
            for (int j = 0; j < segmentMatrix[0].length; ++j) {
                if (segmentMatrix[i][j] > worstFitness) {
                    worstFitness = segmentMatrix[i][j];
                    worstI = i;
                    worstJ = j;
                }
            }
        }
        return new int[]{
                worstI * inputImage.getHeight() / factor,
                worstJ * inputImage.getWidth() / factor,
                inputImage.getHeight() / factor,    // TODO does not work precisely for last segment
                inputImage.getWidth() / factor
        };
    }

    private int getRgbFast(int x, int y, byte[] data) {
        int position = (y * pixelLength * inputImage.getWidth()) + (x * pixelLength);
        short alpha = hasAlpha ? (short) (data[position++] & 0xFF) : 0xFF;
        short red = (short) (data[position++] & 0xFF);
        short green = (short) (data[position++] & 0xFF);
        short blue = (short) (data[position++] & 0xFF);
        return (alpha << 24) + (red << 16) + (green << 8) + blue;
    }

    private int pixelDiffFast(int x, int y, byte[] data) {
        int position = (y * pixelLength * inputImage.getWidth()) + (x * pixelLength);
        int alphaDiff = (hasAlpha ? (data[position++] & 0xFF) : 0xFF) - alphaArray[x][y];
        int redDiff = (data[position++] & 0xFF) - redArray[x][y];
        int greenDiff = (data[position++] & 0xFF) - greenArray[x][y];
        int blueDiff = (data[position++] & 0xFF) - blueArray[x][y];
        return (alphaDiff * alphaDiff) + (redDiff * redDiff) + (greenDiff * greenDiff) + (blueDiff * blueDiff);
    }

    private void extractArrayFromImage() {
        for (int x = 0; x < inputImage.getWidth(); ++x) {
            for (int y = 0; y < inputImage.getHeight(); ++y) {
                int position = (y * pixelLength * inputImage.getWidth()) + (x * pixelLength);
                alphaArray[x][y] = hasAlpha ? (short) (pixels[position++] & 0xFF) : 0xFF;
                redArray[x][y] = (short) (pixels[position++] & 0xFF);
                greenArray[x][y] = (short) (pixels[position++] & 0xFF);
                blueArray[x][y] = (short) (pixels[position++] & 0xFF);
            }
        }
    }
}
