package cz.eoa.impl.function;

import cz.eoa.templates.IndividualWithAssignedFitness;
import cz.eoa.templates.operations.SelectorStrategy;

import java.util.List;
import java.util.Random;

/**
 * @author Ondřej Kratochvíl
 */
public class TournamentSelection<V, T, K extends Comparable<K>> implements SelectorStrategy<V, T, K> {

    private static final Random RANDOM = new Random();
    private final int tournamentSize;

    public TournamentSelection(int tournamentSize) {
        this.tournamentSize = tournamentSize;
    }

    @Override
    public IndividualWithAssignedFitness<V, T, K> select(List<IndividualWithAssignedFitness<V, T, K>> population) {
        int winnerIndex = RANDOM.nextInt(population.size());

        // Try and check another n randomly chosen individuals.
        for (int i = 0; i < tournamentSize; i++) {
            int candidate = RANDOM.nextInt(population.size());
            if (population.get(candidate).getFitness().compareTo(population.get(winnerIndex).getFitness()) > 0) {
                winnerIndex = candidate;
            }
        }

        return population.get(winnerIndex);
    }
}
