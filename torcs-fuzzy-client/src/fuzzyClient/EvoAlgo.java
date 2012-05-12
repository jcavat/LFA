package fuzzyClient;
import java.util.Arrays;

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


public class EvoAlgo {
	
	/**
	 * The total number of times we'll let the population evolve.
	 * the Population
	 */
	private static final int MAX_ALLOWED_EVOLUTIONS = 10;
	private static final int POPULATION = 50;
	
	private static final int NB_DEFAULT = 1;
	private static final int NB_INPUT = 21;
	private static final int NB_FA_IN = 3;
	private static final int NB_OUTPUT = 1;
	private static final int NB_FA_OUT = 5;
	private static final int NB_REGLE = 7;
	private static final int NB_R_IN = 3;
	private static final int NB_R_OUT = 1;
	
	private static final double SPEED_MIN = -1.;
	private static final double SPEED_MAX = 2.5;
	private static final double ANGLE_MIN = -1.;
	private static final double ANGLE_MAX = 1.;
	private static final double SENSOR_MIN = -0.5;
	private static final double SENSOR_MAX = 2.5;
	private static final double STEER_MIN = -0.5;
	private static final double STEER_MAX = 0.5;
	private static final double ACCEL_MIN = -1.;
	private static final double ACCEL_MAX = 1.;
	
	private static final int REGLE_IN_MIN = 1;
	private static final int REGLE_IN_MAX = NB_INPUT * NB_FA_IN;
	private static final int REGLE_OUT_MIN = NB_INPUT * NB_FA_IN + 1;
	private static final int REGLE_OUT_MAX = REGLE_OUT_MIN + NB_FA_OUT;
	
	

	/**
	 * Executes the genetic algorithm
	 * @throws InvalidConfigurationException 
	 */
	public static void launchEvo(boolean accel) throws InvalidConfigurationException {
		
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
		MyFitnessFunction fitnessFunction = new MyFitnessFunction();
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
		if(accel)
			genes[0] = new DoubleGene(conf, ACCEL_MIN, ACCEL_MAX);
		else
			genes[0] = new DoubleGene(conf, STEER_MIN, STEER_MAX);
		
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
        	if(accel)
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
//			IChromosome bestSolutionSoFar = population.getFittestChromosome();
//			System.out.println("BEST SYSTEM : " + bestSolutionSoFar.getFitnessValue());
				
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
		launchEvo(true);
	}

}
