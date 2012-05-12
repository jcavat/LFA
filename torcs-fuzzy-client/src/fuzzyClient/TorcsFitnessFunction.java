package fuzzyClient;

import java.util.HashMap;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierCenterOfGravity;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierCenterOfGravitySingletons;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunction;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionPieceWiseLinear;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionSingleton;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionTrapetzoidal;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionTriangular;
import net.sourceforge.jFuzzyLogic.membership.Value;
import net.sourceforge.jFuzzyLogic.plot.JDialogFis;
import net.sourceforge.jFuzzyLogic.rule.LinguisticTerm;
import net.sourceforge.jFuzzyLogic.rule.Rule;
import net.sourceforge.jFuzzyLogic.rule.RuleBlock;
import net.sourceforge.jFuzzyLogic.rule.RuleExpression;
import net.sourceforge.jFuzzyLogic.rule.RuleTerm;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethod;
import net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethodMax;
import net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethodSum;
import net.sourceforge.jFuzzyLogic.ruleActivationMethod.RuleActivationMethodMin;
import net.sourceforge.jFuzzyLogic.ruleConnectionMethod.RuleConnectionMethodAndMin;
import net.sourceforge.jFuzzyLogic.ruleConnectionMethod.RuleConnectionMethodOrMax;

import org.jgap.*;
import org.jgap.impl.DoubleGene;


public class TorcsFitnessFunction extends FitnessFunction implements ChromosomeDefinition{
	
	private boolean accel;
	
	public TorcsFitnessFunction(boolean accel) {
		this.accel = accel;
	}

	@Override
	protected double evaluate(IChromosome a_subject) {

		// Reconstruct a fcl from the chromosome
		int[] intArray = constructFCL(a_subject);

		// Calculate the fitness of this solution
		// 1.0 is the worst fitness and then greater is better!
		return calculateFitness(intArray);
	}

	// Decode the chromosome in order to construct a FCL
	public FIS constructFCL(IChromosome a_potentialSolution) {
		
		Gene[] genes = a_potentialSolution.getGenes();
		
		// Create the FCL
		FIS fis = new FIS();
		
		// FUNCTION_BLOCK fuzzyDriver
		FunctionBlock functionBlock = new FunctionBlock(fis);
		fis.addFunctionBlock("fuzzyDriver", functionBlock);

		//		VAR_INPUT              
		//		   input1  : REAL;
		//		   input2  : REAL
		//         input3  : REAL
		//         input4  : REAL
		//		   ...
		//         input21 : REAL
		//		END_VAR

		Variable[] inputs = new Variable[NB_INPUT];
		for(int i = 0; i < NB_INPUT; i++){
			inputs[i] = new Variable("input" + i);
			functionBlock.setVariable(inputs[i].getName(), inputs[i]);
		}
		
		//		VAR_OUTPUT
		//		   output1 : REAL;
		//		   output2 : REAL;
		//		   ...
		//		   outputn : REAL;
		//		END_VAR

		Variable[] outputs = new Variable[NB_OUTPUT];
		for(int i = 0; i < NB_OUTPUT; i++){
			outputs[i] = new Variable("output" + i);
			functionBlock.setVariable(outputs[i].getName(), outputs[i]);
		}
		
		// Decode the chromosome for the input
		MembershipFunction memFunc;
		for(int i = 0;i < NB_INPUT;i++){
			//		FUZZIFY inputi
			//		   TERM in_i_1 := (INPUT_MIN, 0) (INPUT_MIN, 1) (j, 1) (j+1, 0) ;
			//		   TERM in_i_2 := (j-1, 0) (j,1) (j+1,0);
			//		   TERM in_i_3 := (j-1, 0) (j, 1) (INPUT_MAX, 1) (INPUT_MAX, 0);
			//		END_FUZZIFY
			
			for(int j = 0;j < NB_FA_IN;j++){
				
				// Define the membership function
				if(j==0)
					memFunc = new MembershipFunctionTrapetzoidal(
									new Value(INPUT_MIN), 
									new Value(INPUT_MIN), 
									new Value((Double)genes[j+i*NB_FA_IN+NB_DEFAULT].getAllele()), 
									new Value((Double)genes[j+1+i*NB_FA_IN+NB_DEFAULT].getAllele()));
				else if(j == NB_FA_IN - 1)
					memFunc = new MembershipFunctionTrapetzoidal(
									new Value((Double)genes[j-1+i*NB_FA_IN+NB_DEFAULT].getAllele()), 
									new Value((Double)genes[j+i*NB_FA_IN+NB_DEFAULT].getAllele()),
									new Value(INPUT_MAX), 
									new Value(INPUT_MAX));
				else
					memFunc = new MembershipFunctionTriangular(
									new Value((Double)genes[j-1+i*NB_FA_IN+NB_DEFAULT].getAllele()),
									new Value((Double)genes[j+i*NB_FA_IN+NB_DEFAULT].getAllele()),
									new Value((Double)genes[j+1+i*NB_FA_IN+NB_DEFAULT].getAllele()));
				
				// Add a label and add it to the input variable
				inputs[i].add("in_"+(j+i*NB_FA_IN), memFunc);
			}
		}
		
		// Decode the chromosome for the output
    	//      DEFUZZIFY outputi
		//		   TERM out_i_1 := (OUTPUT_MIN, 0) (OUTPUT_MIN, 1) (j, 1) (j+1, 0) ;
		//		   TERM out_i_2 := (j-1, 0) (j,1) (j+1,0);
		//		   TERM out_i_3 := (j-1, 0) (j, 1) (OUTPUT_MAX, 1) (OUTPUT_MAX, 0);
		//		   METHOD : COG;
		//		   DEFAULT := defaultGene;
		//		END_DEFUZZIFY
        for(int i = 0; i < NB_OUTPUT; i++){
        	for(int j = 0; j < NB_FA_OUT; j++){
        		// Define the membership function
				memFunc = new MembershipFunctionSingleton(
								new Value((Double)genes[j+i*NB_FA_OUT+NB_INPUT*NB_FA_IN+NB_DEFAULT].getAllele()));
				
				// Add a label and add it to the output variable
				outputs[i].add("out_"+(j+i*NB_FA_OUT), memFunc);	
        	}

        	// Set the default value
        	outputs[i].setDefaultValue((Double)genes[i].getAllele());
        	
        	// Set the gravity center for singletons
        	outputs[i].setDefuzzifier(new DefuzzifierCenterOfGravitySingletons(outputs[i]));
        }
		
		
		//		RULEBLOCK No1
		//		   ACCU : MAX;
		//		   AND : MIN;
		//		   ACT : MIN;
		RuleBlock ruleBlock = new RuleBlock(functionBlock);
		ruleBlock.setName("No1");
		ruleBlock.setRuleAccumulationMethod(new RuleAccumulationMethodMax());
		ruleBlock.setRuleActivationMethod(new RuleActivationMethodMin());
		
		// Decode the chromosome to add the rules to the FCL
		Rule rule;
		int pos;
		for(int i = 0; i < NB_REGLE; i++){
		//		   RULE 1 : IF service IS poor OR food is rancid THEN tip IS cheap;
			rule = new Rule("Rule"+i, ruleBlock);
			
			// Create the terms of the expression
			RuleExpression expression = new RuleExpression();
			for(int j = 0; j < NB_R_IN; j++){
				pos = (Integer)genes[j+i*NB_R_IN+NB_OUTPUT*NB_FA_OUT+NB_INPUT*NB_FA_IN+NB_DEFAULT].getAllele();
				expression.add(new RuleTerm(inputs[pos / NB_FA_IN], "in_" + pos, false));
			}
			rule.setAntecedents(expression);
			
			// Add the consequent
			for(int j = 0; j < NB_R_OUT; j++){
				pos = (Integer)genes[j+NB_R_IN+i*(NB_R_IN + NB_R_OUT)+NB_OUTPUT*NB_FA_OUT+NB_INPUT*NB_FA_IN+NB_DEFAULT].getAllele();
				rule.addConsequent(outputs[pos / NB_FA_OUT], "out_" + pos, false);
			}
			
			ruleBlock.add(rule);
		}
		
		//		END_RULEBLOCK
		//
		//		END_FUNCTION_BLOCK
		HashMap<String, RuleBlock> ruleBlocksMap = new HashMap<String, RuleBlock>();
		ruleBlocksMap.put(ruleBlock.getName(), ruleBlock);
		functionBlock.setRuleBlocks(ruleBlocksMap);
		
		//---
		// Show generated FIS (FCL) and show animation
		//---
		System.out.println(fis);
		
		return fis;
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
