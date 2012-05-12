package fuzzyClient;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;

/**
 * Test parsing an FCL file
 * @author pcingola@users.sourceforge.net
 */
public class TestTipper {

	public static void main(String[] args) throws Exception {
		// Load from 'FCL' file
		String fileName = "fcl/tipper.fcl";
		FIS fis = FIS.load(fileName, true);
		if (fis == null) { // Error while loading?
			System.err.println("Can't load file: '" + fileName + "'");
			return;
		}

		// Show ruleset
		FunctionBlock functionBlock = fis.getFunctionBlock(null);
		functionBlock.chart();

		// Set inputs
		functionBlock.setVariable("service", 3);
		functionBlock.setVariable("food", 7);

		// Evaluate 
		functionBlock.evaluate();

		// Show output variable's chart 
		functionBlock.getVariable("tip").chartDefuzzifier(true);

		// Print ruleSet
		System.out.println(functionBlock);
		System.out.println("TIP:" + functionBlock.getVariable("tip").getValue());
	}
}
