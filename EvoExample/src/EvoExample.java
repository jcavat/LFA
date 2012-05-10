import java.util.Arrays;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;


public class EvoExample {
	
	/**
	 * The total number of times we'll let the population evolve.
	 * the Population
	 */
	private static final int MAX_ALLOWED_EVOLUTIONS = 100;
	private static final int POPULATION = 10000;

	/**
	 * Executes the genetic algorithm
	 * @throws InvalidConfigurationException 
	 */
	public static void launchEvo() throws InvalidConfigurationException {
		// Start with a DefaultConfiguration, which comes setup with the
		// most common settings.
		Configuration.reset();
		// -------------------------------------------------------------
		Configuration conf = new DefaultConfiguration();

		// Care that the fittest individual of the current population is
		// always taken to the next generation.
		// Consider: With that, the pop. size may exceed its original
		// size by one sometimes!
		// -------------------------------------------------------------
		conf.setPreservFittestIndividual(true);
		conf.setKeepPopulationSizeConstant(false);

		// Set the fitness function we want to use, which is our
		// TorcsFitnessFunction.
		// ---------------------------------------------------------
		ExampleFitnessFunction fitnessFunction = new ExampleFitnessFunction();
		FitnessFunction myFunc = fitnessFunction;
		conf.setFitnessFunction(myFunc);

		// Now we need to tell the Configuration object how we want our
		// Chromosomes to be setup. We do that by actually creating a
		// sample Chromosome and then setting it on the Configuration
		// object.
		int numberOfIntegerGenes = 10;
		Gene[] sampleGenes = new Gene[numberOfIntegerGenes];
		

		//Configure each gene
		for(int i = 0; i < numberOfIntegerGenes; i++ )
		{
			sampleGenes[i] = new IntegerGene(conf, 0, numberOfIntegerGenes-1);
		}

		IChromosome sampleChromosome = new Chromosome(conf, sampleGenes);
		conf.setSampleChromosome(sampleChromosome);
		// Finally, we need to tell the Configuration object how many
		// Chromosomes we want in our population. The more Chromosomes,
		// the larger number of potential solutions (which is good for
		// finding the answer), but the longer it will take to evolve
		// the population (which could be seen as bad).
		// ------------------------------------------------------------
		conf.setPopulationSize(POPULATION);
		Genotype population;
		population = Genotype.randomInitialGenotype(conf);

		// Evolve the population. Since we don't know what the best answer
		// is going to be, we just evolve the max number of times.
		// ---------------------------------------------------------------
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < MAX_ALLOWED_EVOLUTIONS; i++) {
				population.evolve();
				System.out.println("GEN : " + (i+1) + " / " + MAX_ALLOWED_EVOLUTIONS);
				/*
				IChromosome bestSolutionSoFar = population.getFittestChromosome();
				System.out.println("BEST SYSTEM : " + bestSolutionSoFar.getFitnessValue());
				*/
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Total evolution time: " + ( endTime - startTime) + " ms");

		IChromosome bestSolutionSoFar = population.getFittestChromosome();
		System.out.println("BEST SYSTEM fitness score : " + bestSolutionSoFar.getFitnessValue());
		int[] intArray = fitnessFunction.constructArray(bestSolutionSoFar);
		System.out.println("The best solution is : " + Arrays.toString(intArray));
	}
	
	/**
	 * The goal is to find the array [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
	 * @param args
	 * @throws InvalidConfigurationException 
	 */
	public static void main(String[] args) throws InvalidConfigurationException {
		
		System.out.println("Example Program");
		System.out.println("The goal is to find the int array [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]");
		launchEvo();
	}

}
