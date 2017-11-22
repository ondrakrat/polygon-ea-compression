package cz.eoa.impl.function;

import cz.eoa.templates.IndividualWithAssignedFitness;
import cz.eoa.templates.operations.ReplacementStrategy;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ondřej Kratochvíl
 */
public class Elitism<V, T, K extends Comparable<K>> implements ReplacementStrategy<V, T, K> {

    private final int survivorCount;
    private final boolean maximizeFitness;

    public Elitism(int survivorCount, boolean maximizeFitness) {
        this.survivorCount = survivorCount;
        this.maximizeFitness = maximizeFitness;
    }

    @Override
    public List<IndividualWithAssignedFitness<V, T, K>> getIndividualsToIncludeInNextGeneration(List<IndividualWithAssignedFitness<V, T, K>> currentPopulation) {
        assert survivorCount < currentPopulation.size();
        return currentPopulation.stream()
                .sorted((o1, o2) -> {
                    if (maximizeFitness) {
                        return o2.compareTo(o1);
                    } else {
                        return o1.compareTo(o2);
                    }
                })
                .limit(survivorCount)
                .collect(Collectors.toList());

    }
}
