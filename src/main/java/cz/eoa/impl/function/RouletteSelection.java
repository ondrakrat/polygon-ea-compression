package cz.eoa.impl.function;

import cz.eoa.templates.IndividualWithAssignedFitness;
import cz.eoa.templates.operations.SelectorStrategy;

import java.util.List;
import java.util.Random;

/**
 * @author Ondřej Kratochvíl
 */
public class RouletteSelection<V, T> implements SelectorStrategy<V, T, Double> {

    private static final Random RANDOM = new Random();

    @Override
    public IndividualWithAssignedFitness<V, T, Double> select(List<IndividualWithAssignedFitness<V, T, Double>> population) {
        double totalFitness = 0;
        for (IndividualWithAssignedFitness<V, T, Double> individual : population) {
            totalFitness += individual.getFitness();
        }
        double random = RANDOM.nextFloat();
        double current = 0;
        for (IndividualWithAssignedFitness<V, T, Double> individual : population) {
            if (current + (individual.getFitness() / totalFitness) < random) {
                current += individual.getFitness() / totalFitness;
            } else {
                return individual;
            }
        }
        return null;
    }
}
