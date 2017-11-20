package cz.eoa.impl;

import cz.eoa.templates.Individual;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author Ondřej Kratochvíl
 */
public class ImageIndividual extends Individual<List<Polygon>, BufferedImage> {

    public ImageIndividual(List<Polygon> genes) {
        super(genes);
    }
}
