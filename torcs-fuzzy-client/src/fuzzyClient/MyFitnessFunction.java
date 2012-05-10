package fuzzyClient;

import org.jgap.*;
import org.jgap.Gene;
import org.jgap.IChromosome;


public class MyFitnessFunction extends FitnessFunction {

	@Override
	protected double evaluate(IChromosome a_subject) {

		//Reconstruct a int array from the chromosome
		int[] intArray = constructArray(a_subject);

		// Calculate the fitness of this solution
		// 1.0 is the worst fitness and then greater is better!
		return calculateFitness(intArray);
	}

	//Decode the chromosome in order to construct a int array
	public int[] constructArray(IChromosome a_potentialSolution) {
		
		Gene[] genes = a_potentialSolution.getGenes();

		int numberOfGene = genes.length;
		int[] intArray = new int[numberOfGene];
		
		for(int i = 0; i < numberOfGene; i++)
		{
			intArray[i] = (Integer)genes[i].getAllele();
		}
		
		return intArray;
	}
	
	//The fitness function.
	//In this example case we want obtain the int array [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
	private double calculateFitness(int[] intArray) {
		int arrayLength = intArray.length;
		int i = 0;
		//Count of much number are ordered
		while(i < arrayLength-1  && intArray[i] < intArray[i+1])
		{
			
			i++;
		}
		
		//More order number you have the better your fitness will be
		return 1.0 + i;
	}
}
