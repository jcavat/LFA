package fuzzyClient;

/*
 * Define a chromosome.
 * The chromosome sequence :
 * |     Double     | ... | Double | Double | Double | ... | Double | Double | Double | Integer | Integer | Integer | Integer | ... |
 * |                |     |                          |     |                          |          Conditions         | Result  |
 * | Default Output |     |        First Input       |     |          Output          |               First Rule              |
 */
public interface ChromosomeDefinition {
	
	static final int NB_INPUT = 21;
	static final int NB_FA_IN = 3;
	static final int NB_OUTPUT = 1;
	static final int NB_DEFAULT = NB_OUTPUT;
	static final int NB_FA_OUT = 5;
	static final int NB_REGLE = 7;
	static final int NB_R_IN = 3;
	static final int NB_R_OUT = 1;
	
	static final double SPEED_MIN = -1.;
	static final double SPEED_MAX = 2.5;
	static final double ANGLE_MIN = -1.;
	static final double ANGLE_MAX = 1.;
	static final double SENSOR_MIN = -0.5;
	static final double SENSOR_MAX = 2.5;
	static final double STEER_MIN = -0.5;
	static final double STEER_MAX = 0.5;
	static final double ACCEL_MIN = -1.;
	static final double ACCEL_MAX = 1.;
	static final double INPUT_MIN = -5.;
	static final double INPUT_MAX = 5.;
	static final double OUTPUT_MIN = -5.;
	static final double OUTPUT_MAX = 5.;
	
	static final int REGLE_IN_MIN = 0;
	static final int REGLE_IN_MAX = NB_INPUT * NB_FA_IN;
	static final int REGLE_OUT_MIN = 0;
	static final int REGLE_OUT_MAX = NB_OUTPUT * NB_FA_OUT;
	
}
