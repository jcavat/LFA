package fuzzyClient;

import net.sourceforge.jFuzzyLogic.FIS;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.IntegerGene;


public class EvoAlgo implements ChromosomeDefinition {
	
	/**
	 * The total number of times we'll let the population evolve.
	 * the Population
	 */
	private static final int MAX_ALLOWED_EVOLUTIONS = 10;
	private static final int POPULATION = 50;
	

	/**
	 * Executes the genetic algorithm
	 * @throws InvalidConfigurationException 
	 */
	public static IChromosome launchEvo(boolean accel) throws InvalidConfigurationException {
		
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
		TorcsFitnessFunction fitnessFunction = new TorcsFitnessFunction(accel);
		FitnessFunction myFunc = fitnessFunction;
		conf.setFitnessFunction(myFunc);

		// Now we need to tell the Configuration object how we want our
		// Chromosomes to be setup. We do that by actually creating a
		// sample Chromosome and then setting it on the Configuration
		// object.
		Gene[] genes = new Gene[ NB_DEFAULT + 
	                             NB_INPUT * NB_FA_IN + 
	                             NB_OUTPUT * NB_FA_OUT + 
	                             NB_REGLE * (NB_R_IN + NB_R_OUT)];
		
		//Configure the default gene
		for(int i = 0; i < NB_DEFAULT; i++)
			if(accel && i == 0)
				genes[i] = new DoubleGene(conf, ACCEL_MIN, ACCEL_MAX);
			else
				genes[i] = new DoubleGene(conf, STEER_MIN, STEER_MAX);
		
		// Configure the speed input genes and the angle input genes
		for(int i = 0; i < NB_FA_IN; i++){
			genes[i + NB_DEFAULT] = new DoubleGene(conf, SPEED_MIN, SPEED_MAX);
			genes[i + NB_DEFAULT + NB_FA_IN] = new DoubleGene(conf, ANGLE_MIN, ANGLE_MAX);
		}
		
		// Configure the sensors genes
        for(int i = NB_DEFAULT + 2 * NB_FA_IN; i < NB_INPUT * NB_FA_IN + NB_DEFAULT; i++ )
			genes[i] = new DoubleGene(conf, SENSOR_MIN, SENSOR_MAX);
        
        // Configure the output genes
        for(int i = 0; i < NB_OUTPUT * NB_FA_OUT; i++)
        	if(accel && i < NB_FA_OUT)
        		genes[i + NB_INPUT * NB_FA_IN + NB_DEFAULT] = new DoubleGene(conf, ACCEL_MIN, ACCEL_MAX);
        	else
        		genes[i + NB_INPUT * NB_FA_IN + NB_DEFAULT] = new DoubleGene(conf, STEER_MIN, STEER_MAX);
        
        // Configure the rules genes
        for(int i = 0; i < NB_REGLE; i++){
        	
        	// Configure the conditional genes
        	for(int j = 0; j < NB_R_IN; j++)
        		genes[i * (NB_R_IN + NB_R_OUT) + j + NB_OUTPUT * NB_FA_OUT + NB_INPUT * NB_FA_IN + NB_DEFAULT] = new IntegerGene(conf, REGLE_IN_MIN, REGLE_IN_MAX);
        	
        	// Configure the result genes
        	genes[i * (NB_R_IN + NB_R_OUT) + NB_R_IN + NB_OUTPUT * NB_FA_OUT + NB_INPUT * NB_FA_IN + NB_DEFAULT] = new IntegerGene(conf, REGLE_OUT_MIN, REGLE_OUT_MAX);
        		
        }
        
		IChromosome chromosome = new Chromosome(conf, genes);
		conf.setSampleChromosome(chromosome);
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
			IChromosome bestSolutionSoFar = population.getFittestChromosome();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Total evolution time: " + ( endTime - startTime) + " ms");

		IChromosome bestSolutionSoFar = population.getFittestChromosome();
		System.out.println("Meilleure solution (fitness : " + bestSolutionSoFar.getFitnessValue() + " / 1'000'000) : ");
		
		return bestSolutionSoFar;
	}
	
	/**
	 * The goal is to find the array [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
	 * @param args
	 * @throws InvalidConfigurationException 
	 */
	public static void main(String[] args) throws InvalidConfigurationException {
		launchEvo(false);
	}

}
