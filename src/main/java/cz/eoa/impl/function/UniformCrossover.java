package cz.eoa.impl.function;

import cz.eoa.impl.Polygon;
import cz.eoa.templates.Individual;
import cz.eoa.templates.operations.CrossoverStrategy;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ondřej Kratochvíl
 */
public class UniformCrossover implements CrossoverStrategy<List<Polygon>, BufferedImage> {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private final double parentProbability;

    public UniformCrossover(double parentProbability) {
        this.parentProbability = parentProbability;
    }

    @Override
    public List<Individual<List<Polygon>, BufferedImage>> crossover(Individual<List<Polygon>, BufferedImage> firstParent, Individual<List<Polygon>, BufferedImage> secondParent) {
        List<Polygon> firstParentGenes = firstParent.getGenes();
        List<Polygon> secondParentGenes = secondParent.getGenes();
        List<Polygon> childGenes1 = new ArrayList<>(firstParentGenes.size());
        List<Polygon> childGenes2 = new ArrayList<>(secondParentGenes.size());
        for (int i = 0; i < firstParentGenes.size(); ++i) {
            if (RANDOM.nextDouble() < parentProbability) {
                childGenes1.add(new Polygon(firstParentGenes.get(i)));
                childGenes2.add(new Polygon(secondParentGenes.get(i)));
            } else {
                childGenes1.add(new Polygon(secondParentGenes.get(i)));
                childGenes2.add(new Polygon(firstParentGenes.get(i)));
            }
        }
//        checkGenes(firstParentGenes, secondParentGenes, childGenes1, childGenes2);

        return Stream.of(
                new Individual<List<Polygon>, BufferedImage>(childGenes1),
                new Individual<List<Polygon>, BufferedImage>(childGenes2))
                .collect(Collectors.toList());
    }

    private void checkGenes(List<Polygon> firstParentGenes, List<Polygon> secondParentGenes,
                            List<Polygon> childGenes1, List<Polygon> childGenes2) {
        int size = childGenes1.size();
        assert size == childGenes2.size()
                && childGenes2.size() == firstParentGenes.size()
                && firstParentGenes.size() == secondParentGenes.size();
        for (int i = 0; i < firstParentGenes.size(); ++i) {
            assert childGenes1.get(i).equals(firstParentGenes.get(i))
                    || childGenes1.get(i).equals(secondParentGenes.get(i));
            assert childGenes2.get(i).equals(firstParentGenes.get(i))
                    || childGenes2.get(i).equals(secondParentGenes.get(i));
        }
    }
}
