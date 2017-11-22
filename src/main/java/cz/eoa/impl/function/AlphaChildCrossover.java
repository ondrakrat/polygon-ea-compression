package cz.eoa.impl.function;

import cz.eoa.impl.Polygon;
import cz.eoa.templates.Individual;
import cz.eoa.templates.operations.CrossoverStrategy;
import cz.eoa.templates.operations.DecodingStrategy;
import cz.eoa.templates.operations.FitnessAssessmentStrategy;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Try to generate child of better fitness than parent, or return the best one.
 *
 * @author Ondřej Kratochvíl
 */
public class AlphaChildCrossover implements CrossoverStrategy<List<Polygon>, BufferedImage> {

    private final FitnessAssessmentStrategy<BufferedImage, Double> fitnessFunction;
    private final DecodingStrategy<List<Polygon>, BufferedImage> decoder;
    private final CrossoverStrategy<List<Polygon>, BufferedImage> crossoverStrategy;
    private final int maxTries;

    public AlphaChildCrossover(FitnessAssessmentStrategy<BufferedImage, Double> fitnessFunction,
                               DecodingStrategy<List<Polygon>, BufferedImage> decoder,
                               CrossoverStrategy<List<Polygon>, BufferedImage> crossoverStrategy,
                               int maxTries) {
        this.fitnessFunction = fitnessFunction;
        this.decoder = decoder;
        this.crossoverStrategy = crossoverStrategy;
        this.maxTries = maxTries;
    }

    @Override
    public List<Individual<List<Polygon>, BufferedImage>> crossover(Individual<List<Polygon>, BufferedImage> firstParent, Individual<List<Polygon>, BufferedImage> secondParent) {
        double bestParentFitness = Math.max(
                fitnessFunction.computeFitnessForIndividual(decoder.decode(firstParent.getGenes())),
                fitnessFunction.computeFitnessForIndividual(decoder.decode(secondParent.getGenes()))
        );
        Individual<List<Polygon>, BufferedImage> bestChild = null;
        double bestFitness = Integer.MIN_VALUE;
        int tries = 0;
        do {
            for (Individual<List<Polygon>, BufferedImage> child : crossoverStrategy.crossover(firstParent, secondParent)) {
                Double fitness = fitnessFunction.computeFitnessForIndividual(decoder.decode(child.getGenes()));
                if (fitness > bestParentFitness) {
                    return Collections.singletonList(child);
                }
                if (bestChild == null || fitness > bestFitness) {
                    bestChild = child;
                    bestFitness = fitness;
                }
            }
        } while (++tries < maxTries);
        return Collections.singletonList(bestChild);
    }
}
