package cz.eoa.impl.function;

import cz.eoa.impl.Polygon;
import cz.eoa.templates.Individual;
import cz.eoa.templates.operations.CrossoverStrategy;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ondřej Kratochvíl
 */
public class SinglePointCrossover implements CrossoverStrategy<List<Polygon>, BufferedImage> {

    private final float crossoverPoint;

    public SinglePointCrossover(float crossoverPoint) {
        assert crossoverPoint > 0 && crossoverPoint < 1;
        this.crossoverPoint = crossoverPoint;
    }

    @Override
    public List<Individual<List<Polygon>, BufferedImage>> crossover(Individual<List<Polygon>, BufferedImage> firstParent, Individual<List<Polygon>, BufferedImage> secondParent) {
        // deep copy
        List<Polygon> firstParentGenes = firstParent.getGenes().stream()    // TODO parallel?
                .map(Polygon::new)
                .collect(Collectors.toList());
        List<Polygon> secondParentGenes = secondParent.getGenes().stream()  // TODO parallel?
                .map(Polygon::new)
                .collect(Collectors.toList());
        List<Polygon> childGenes1 = firstParentGenes
                .subList(0, (int) (firstParentGenes.size() * crossoverPoint));
        childGenes1.addAll(secondParentGenes
                .subList((int) Math.ceil(secondParentGenes.size() * (1 - crossoverPoint)), secondParentGenes.size()));
        List<Polygon> childGenes2 = secondParentGenes
                .subList(0, (int) (secondParentGenes.size() * crossoverPoint));
        childGenes2.addAll(firstParentGenes
                .subList((int) Math.ceil(firstParentGenes.size() * crossoverPoint), firstParentGenes.size()));
        // TODO remove test
        checkGenes(firstParentGenes, secondParentGenes, childGenes1, childGenes2);

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
            if (i < size) {
                assert childGenes1.get(i).equals(firstParentGenes.get(i));
                assert childGenes2.get(i).equals(secondParentGenes.get(i));
            } else {
                assert childGenes1.get(i).equals(secondParentGenes.get(i));
                assert childGenes2.get(i).equals(firstParentGenes.get(i));
            }
        }
    }
}
