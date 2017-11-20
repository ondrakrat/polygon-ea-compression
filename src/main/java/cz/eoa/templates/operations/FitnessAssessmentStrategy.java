package cz.eoa.templates.operations;

public interface FitnessAssessmentStrategy<T, K extends Comparable<K>> {
    K computeFitnessForIndividual(T solution);
}
