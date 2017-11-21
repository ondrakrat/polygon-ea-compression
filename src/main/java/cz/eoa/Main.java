package cz.eoa;

import cz.eoa.configuration.EvolutionConfiguration;
import cz.eoa.configuration.EvolutionConfigurationBuilder;
import cz.eoa.cycle.EvolutionExecutor;
import cz.eoa.impl.Polygon;
import cz.eoa.impl.function.*;
import cz.eoa.templates.Individual;
import cz.eoa.templates.IndividualWithAssignedFitness;
import cz.eoa.templates.StatisticsPerEpoch;
import cz.eoa.templates.operations.DecodingStrategy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class Main {

    // parameters + configuration
    private static final Random RANDOM = new Random();
    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private static final int POLYGON_COUNT = 50;
    private static final int POPULATION_SIZE = 100;
    private static final int GENERATION_COUNT = 1000;
    private static final int RENDER_FREQUENCY = 100;
    private static final double CROSSOVER_PROBABILITY = 0.75;
    private static final float CROSSOVER_POINT = 0.9f;
    private static final double MUTATION_RATE = 0.05;
    private static final double MUTATION_EXTENT = 0.05;
    private static final float MIN_ALPHA = 0.1f;
    private static final float MAX_ALPHA = 0.2f;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Please specify input and output file names");
            System.exit(1);
        }
        // read the image
        String inputFileName = args[0];
        String outputDirName = args[1];
        BufferedImage inputImage = ImageIO.read(new File(inputFileName));

        PolygonsToImageDecoder polygonsToImageDecoder = new PolygonsToImageDecoder(
                inputImage.getWidth(), inputImage.getHeight(), inputImage.getType()
        );

        //types by order: genes, decoded genes - solution, fitness, container with statistics
        EvolutionConfiguration<List<Polygon>, BufferedImage, Double, ImageStatisticsPerEpoch> configuration =
                new EvolutionConfigurationBuilder<List<Polygon>, BufferedImage, Double, ImageStatisticsPerEpoch>()
                        .populationSize(POPULATION_SIZE)
                        .populationInitialization(
                                new PolygonPopulationInitialization(inputImage, POLYGON_COUNT, MIN_ALPHA, MAX_ALPHA)
                        )
                        .decoding(polygonsToImageDecoder)
                        .selector(new TournamentSelection<>())
                        .crossover(new SinglePointCrossover(CROSSOVER_POINT))
                        .mutation(new PolygonMutation(MUTATION_RATE, MUTATION_EXTENT, inputImage))
                        //generational replacement strategy. keep nothing from previous population
                        .replacement(currentPopulation -> new ArrayList<>())
                        .fitnessAssessment(new ImageFitness(inputImage))
                        .fitnessIsMaximized(false)
                        .parallel(false)
                        .probabilityOfCrossover(CROSSOVER_PROBABILITY)
                        .terminationCondition(epochs -> epochs.size() < GENERATION_COUNT)
                        .statisticsCreation(
                                (epoch, execution, countOfFitnessEvaluations, bestIndividual, population) ->
                                        new ImageStatisticsPerEpoch(epoch, execution, countOfFitnessEvaluations,
                                                bestIndividual, population, polygonsToImageDecoder, outputDirName)
                        )
                        .build();

        EvolutionExecutor<List<Polygon>, BufferedImage, Double, ImageStatisticsPerEpoch> executor =
                new EvolutionExecutor<>(configuration);
        List<ImageStatisticsPerEpoch> statistics = executor.run();

        long time = statistics.stream()
                .mapToLong(StatisticsPerEpoch::getExecution)
                .sum();
        ImageStatisticsPerEpoch bestEpoch = statistics.stream()
                .max(Comparator.comparing(stats -> stats.getBestIndividual().getFitness()))
                .orElseThrow(() -> new IllegalArgumentException("Empty stream of epochs"));
        LOG.info("Executed in " + time + ", best solution in epoch " + bestEpoch.getEpoch());
    }

    private static class ImageStatisticsPerEpoch extends StatisticsPerEpoch<List<Polygon>, BufferedImage, Double> {

        private static final String FORMAT = "jpeg";
        private static final String FILE_PREFIX = "generation";
        private final String outputDir;

        public ImageStatisticsPerEpoch(
                int epoch,
                long execution,
                int countOfFitnessEvaluations,
                IndividualWithAssignedFitness<List<Polygon>, BufferedImage, Double> bestIndividual,
                List<IndividualWithAssignedFitness<List<Polygon>, BufferedImage, Double>> population,
                DecodingStrategy<List<Polygon>, BufferedImage> decodingStrategy,
                String outputDir
        ) {
            super(epoch, execution, countOfFitnessEvaluations, bestIndividual, population);
            this.outputDir = outputDir;
            // render partial solutions
            if (epoch == 1 || epoch % RENDER_FREQUENCY == 0 || epoch == GENERATION_COUNT) {
                Individual<List<Polygon>, BufferedImage> alphaIndividual = bestIndividual.getIndividual();
                renderSolution(alphaIndividual.decode(decodingStrategy));
            }
        }

        @Override
        public String getSummary() {
            return "Epoch " + epoch +
                    ", avg. fitness: " + population.stream()
                    .mapToDouble(IndividualWithAssignedFitness::getFitness)
                    .average()
                    .orElse(0) +
                    ", #fitness evaluations: " + countOfFitnessEvaluations +
                    ", execution time:" + execution +
                    ", best fitness: " + bestIndividual.getFitness().toString();
        }

        private void renderSolution(BufferedImage bufferedImage) {
            String fileName = String.format("%s/%s_%d.%s", outputDir, FILE_PREFIX, epoch, FORMAT);
            try {
                ImageIO.write(bufferedImage, FORMAT, new File(fileName));
            } catch (IOException e) {
                System.err.println("Unable to write rendered solution to file");
                e.printStackTrace();
            }
        }
    }
}
