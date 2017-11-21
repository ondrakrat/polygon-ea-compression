package cz.eoa.impl;

import cz.eoa.templates.Individual;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ondřej Kratochvíl
 */
public class ImageIndividual extends Individual<List<Polygon>, BufferedImage> {

    public ImageIndividual(List<Polygon> genes) {
        super(genes);
    }

    /**
     * Copy constructor.
     *
     * @param other instance from which the data should be deep copied.
     */
    public ImageIndividual(ImageIndividual other) {
        super(other.getGenes().stream()
                .map(Polygon::new)  // deep copy
                .collect(Collectors.toList())
        );
    }
}
