package cz.eoa;

import cz.eoa.configuration.EvolutionConfiguration;
import cz.eoa.configuration.EvolutionConfigurationBuilder;
import cz.eoa.cycle.EvolutionExecutor;
import cz.eoa.impl.Polygon;
import cz.eoa.impl.function.PolygonsToImageDecoder;
import cz.eoa.impl.function.PolygonPopulationInitialization;
import cz.eoa.impl.function.SinglePointCrossover;
import cz.eoa.impl.function.TournamentSelection;
import cz.eoa.templates.Individual;
import cz.eoa.templates.IndividualWithAssignedFitness;
import cz.eoa.templates.StatisticsPerEpoch;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    //parameters + configuration
    private static final Random RANDOM = new Random();
    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private static final int POLYGON_COUNT = 50;
    private static final int POPULATION_SIZE = 100;
    private static final int GENERATION_COUNT = 500;
    private static final double CROSSOVER_PROBABILITY = 0.75;
    private static final float CROSSOVER_POINT = 0.9f;
    private static final float MIN_ALPHA = 0.1f;
    private static final float MAX_ALPHA = 0.2f;

    private static final int MAX_GENES = 24;
    private static final double MIN_X = 0.0;
    private static final double MAX_X = 150.0;
    private static final double RANGE_X = MAX_X - MIN_X;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Please specify input and output file names");
            System.exit(1);
        }
        // read the image
        String inputFileName = args[0];
        String outputDirName = args[1];
        BufferedImage inputImage = ImageIO.read(new File(inputFileName));

        //types by order: genes, decoded genes - solution, fitness, container with statistics
        EvolutionConfiguration<List<Polygon>, BufferedImage, Double, ImageStatisticsPerEpoch> configuration =
                new EvolutionConfigurationBuilder<List<Polygon>, BufferedImage, Double, ImageStatisticsPerEpoch>()
                        .populationSize(POPULATION_SIZE)
                        .populationInitialization(
                                new PolygonPopulationInitialization(inputImage, POLYGON_COUNT, MIN_ALPHA, MAX_ALPHA)
                        )
                        .decoding(new PolygonsToImageDecoder(
                                inputImage.getWidth(), inputImage.getHeight(), inputImage.getType()
                        ))
                        .selector(new TournamentSelection<>())
                        .crossover(new SinglePointCrossover(CROSSOVER_POINT))
                        .mutation()
                        //generational replacement strategy. keep nothing from previous population
                        .replacement(currentPopulation -> new ArrayList<>())
                        .fitnessAssessment()
                        .fitnessIsMaximized(false)
                        .parallel(false)
                        .probabilityOfCrossover(CROSSOVER_PROBABILITY)
                        .terminationCondition(epochs -> epochs.size() < GENERATION_COUNT)
                        .statisticsCreation(ImageStatisticsPerEpoch::new)
                        .build();
    }

    public static void example() {
        //types by order: genes, decoded genes - solution, fitness, container with statistics
        EvolutionConfiguration<int[], Double, Double, MyStatisticsPerEpoch> evolutionConfiguration =
                new EvolutionConfigurationBuilder<int[], Double, Double, MyStatisticsPerEpoch>()
                        //uniform crossover
                        .crossover((firstParent, secondParent) -> {
                            int[] firstSetOfGenes = new int[MAX_GENES], secondSetOfGenes = new int[MAX_GENES];
                            // Each gene is inherited either from the 1st or the 2nd parent
                            for (int i = 0; i < MAX_GENES; i++) {
                                if (RANDOM.nextBoolean()) {
                                    firstSetOfGenes[i] = firstParent.getGenes()[i];
                                } else {
                                    firstSetOfGenes[i] = secondParent.getGenes()[i];
                                }
                                if (RANDOM.nextBoolean()) {
                                    secondSetOfGenes[i] = secondParent.getGenes()[i];
                                } else {
                                    secondSetOfGenes[i] = firstParent.getGenes()[i];
                                }
                            }
                            return Stream.of(
                                    new Individual<int[], Double>(firstSetOfGenes),
                                    new Individual<int[], Double>(secondSetOfGenes))
                                    .collect(Collectors.toList());
                        })
                        //simple bit-flip mutation
                        .mutation(individual -> {
                            int[] genes = individual.getGenes().clone();
                            for (int i = 0; i < genes.length; i++) {
                                // Mutate each gene with probability Pm.
                                if (RANDOM.nextDouble() < 0.01) {
                                    genes[i] = (genes[i] + 1) % 2;    // swap between 0 and 1
                                }
                            }
                            return Optional.of(new Individual<>(genes));
                        })
                        //tournament selection
                        .selector(new TournamentSelection<>())
                        //generational replacement strategy. keep nothing from previous population
                        .replacement(currentPopulation -> new ArrayList<>())
                        //strategy to initialize single individual - do it randomly
                        .populationInitialization(() -> {
                            // Allocate memory for genes array.
                            int[] genes = new int[MAX_GENES];

                            // Randomly initialise genes of the individual.
                            for (int i = 0; i < MAX_GENES; i++) {
                                if (RANDOM.nextBoolean()) {
                                    genes[i] = 0;
                                } else {
                                    genes[i] = 1;
                                }
                            }
                            return new Individual<>(genes);
                        })
                        //strategy how to decode genes
                        .decoding(genes -> {
                            double pom = Math.pow(2.0, MAX_GENES) - 1.0;
                            int power = 1;
                            double temp = 0.0;
                            for (int i = 0; i < MAX_GENES; i++) {
                                temp += (power * genes[i]);
                                power *= 2;
                            }
                            return MIN_X + (temp / pom) * RANGE_X;
                        })
                        //how fitness is computed
                        .fitnessAssessment(x -> 2.0 + (x / 50.0) + Math.sin(x) + 2.0 * Math.sin(Math.PI * x / 50.0))
                        .fitnessIsMaximized(true)
                        .parallel(true)
                        .probabilityOfCrossover(0.75)
                        .populationSize(50)
                        //when to terminate evolution, after 100 epochs has been reached
                        .terminationCondition(epochs -> epochs.size() < 100)
                        //use own statistics
                        .statisticsCreation(MyStatisticsPerEpoch::new)
                        .build();
        EvolutionExecutor<int[], Double, Double, MyStatisticsPerEpoch> evolutionExecutor =
                new EvolutionExecutor<>(evolutionConfiguration);
        List<MyStatisticsPerEpoch> statistics = evolutionExecutor.run();

        // stats
        long time = statistics.stream()
                .mapToLong(StatisticsPerEpoch::getExecution)
                .sum();
        MyStatisticsPerEpoch bestEpoch = statistics.stream()
                .max(Comparator.comparing(stats -> stats.getBestIndividual().getFitness()))
                .orElseThrow(() -> new IllegalArgumentException("Empty stream of epochs"));
        LOG.info("Executed in " + time + ", best solution in epoch " + bestEpoch.getEpoch());
    }

    /**
     * Own implementation of class with statistics, most important is method getSummary().
     * It is used to store and print results
     */
    private static class MyStatisticsPerEpoch extends StatisticsPerEpoch<int[], Double, Double> {

        MyStatisticsPerEpoch(
                int epoch,
                long execution,
                int countOfFitnessEvaluations,
                IndividualWithAssignedFitness<int[], Double, Double> bestIndividual,
                List<IndividualWithAssignedFitness<int[], Double, Double>> population) {
            super(epoch, execution, countOfFitnessEvaluations, bestIndividual, population);
        }

        @Override
        public String getSummary() {
            return "Epoch " + epoch +
                    ", avg. fitness: " + population.stream()
                    .mapToDouble(IndividualWithAssignedFitness::getFitness)
                    .average()
                    .orElse(0) +
                    ", #fitness evaluations: " + countOfFitnessEvaluations +
                    ", execution time:" + execution + "\n" +
                    "result: " + decode(bestIndividual.getGenes()) +
                    ", best fitness: " + bestIndividual.getFitness().toString();
        }
    }

    private static class ImageStatisticsPerEpoch extends StatisticsPerEpoch<List<Polygon>, BufferedImage, Double> {

        public ImageStatisticsPerEpoch(
                int epoch,
                long execution,
                int countOfFitnessEvaluations,
                IndividualWithAssignedFitness<List<Polygon>, BufferedImage, Double> bestIndividual,
                List<IndividualWithAssignedFitness<List<Polygon>, BufferedImage, Double>> population) {
            super(epoch, execution, countOfFitnessEvaluations, bestIndividual, population);
        }
    }
}
