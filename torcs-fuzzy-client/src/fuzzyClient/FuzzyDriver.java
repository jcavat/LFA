package fuzzyClient;
import org.jgap.InvalidConfigurationException;

import net.sourceforge.jFuzzyLogic.FIS;

/*
 * Driver for the TORCS server, using fuzzy systems
 */
public class FuzzyDriver extends CarController{


	// Gear Changing Constants
	final int[]  gearUp={9000,9000,9000,9000,9000,0};
	final int[]  gearDown={0,2000,5000,5000,5000,5000};
	
	// Stuck constants
	final int  stuckTime = 25;
	final float  stuckAngle = (float) 0.523598775; //PI/6

	// Accel and Brake Constants
	final float maxSpeedDist=60;
	final float maxSpeed=300;
	final float sin5 = (float) 0.08716;
	final float cos5 = (float) 0.99619;

	// Steering constants
	final float steerLock=(float) 0.785398;
	final float steerSensitivityOffset=(float) 80.0;
	final float wheelSensitivityCoeff=1;

	// ABS Filter Constants
	final float wheelRadius[]={(float) 0.3179,(float) 0.3179,(float) 0.3276,(float) 0.3276};
	final float absSlip=(float) 2.0;
	final float absRange=(float) 3.0;
	final float absMinSpeed=(float) 3.0;

	// Clutching Constants
	final float clutchMax=(float) 0.5;
	final float clutchDelta=(float) 0.05;
	final float clutchRange=(float) 0.82;
	final float	clutchDeltaTime=(float) 0.02;
	final float clutchDeltaRaced=10;
	final float clutchDec=(float) 0.01;
	final float clutchMaxModifier=(float) 1.3;
	final float clutchMaxTime=(float) 1.5;

   // Current stuck
	private int stuck=0;

	// Current clutch
	private float clutch=0;
	
	// Fuzzy system
	private FIS fisSteer;
	private FIS fisAccel;
	private String fclFileSteer = "fclSteer.fcl";
	private String fclFileAccel = "fclAccel.fcl";
	

	public FuzzyDriver() {
		// Load from 'FCL' file
		//fisSteer = FIS.load(fclFileSteer,true);
		//fisAccel = FIS.load(fclFileAccel,true);
		
		// Evolve the FCL
		try{
			fisSteer = TorcsFitnessFunction.constructFCL(EvoAlgo.launchEvo(false));
			fisAccel = TorcsFitnessFunction.constructFCL(EvoAlgo.launchEvo(true));
			
			System.out.println(fisSteer);
			System.out.println(fisAccel);
		}
		catch(InvalidConfigurationException e){
			System.err.println(e);
		}
		 
		// Error while loading?
		if( fisSteer == null ) { 
		    System.err.println("Can't load file: '" + fclFileSteer + "'");
		    return;
		}
		if( fisAccel == null ) { 
		    System.err.println("Can't load file: '" + fclFileAccel + "'");
		    return;
		}
	}

	public void reset() {
		System.out.println("Restarting the race!");
	}

	public void shutdown() {
		System.out.println("Bye bye!");
	}

	private int getGear(SensorModel sensors){
		int gear = sensors.getGear();
		double rpm  = sensors.getRPM();

		// if gear is 0 (N) or -1 (R) just return 1 
		if (gear<1)
			return 1;
		// check if the RPM value of car is greater than the one suggested 
		// to shift up the gear from the current one     
		if (gear <6 && rpm >= gearUp[gear-1])
			return gear + 1;
		else
			// check if the RPM value of car is lower than the one suggested 
			// to shift down the gear from the current one
			if (gear > 1 && rpm <= gearDown[gear-1])
				return gear - 1;
			else // otherwise keep current gear
				return gear;
	}

	private float getSteer(SensorModel sensors){
		// Set inputs
		fisSteer.setVariable("angleToTrackAxis", sensors.getAngleToTrackAxis());
		fisSteer.setVariable("trackPosition", sensors.getTrackPosition());

        // Process the directions of the track
		double dir = sensors.getTrackEdgeSensors()[11]-sensors.getTrackEdgeSensors()[7];
		fisSteer.setVariable("direction", dir/2);

		// Evaluate the fuzzy system
		fisSteer.evaluate();
		
		return (float)fisSteer.getVariable("steer").getValue();
	}

	private float getAccel(SensorModel sensors, float steer)
	{
        // Set inputs
		fisAccel.setVariable("focus_front", sensors.getTrackEdgeSensors()[9]);
		
		// Evaluate the fuzzy system
		fisAccel.evaluate();
		
        // Get the target speed
		double targetSpeed = fisAccel.getVariable("speed").getValue();
		
		// Accel/brake command is exponentially scaled w.r.t. the difference between target speed and current one
		return (float) (2/(1+Math.exp(sensors.getSpeed() - targetSpeed)) - 1);
	}

	public Action control(SensorModel sensors){
		// check if car is currently stuck
		if ( Math.abs(sensors.getAngleToTrackAxis()) > stuckAngle )
		{
			// update stuck counter
			stuck++;
		}
		else
		{
			// if not stuck reset stuck counter
			stuck = 0;
		}

		// after car is stuck for a while apply recovering policy
		if (stuck > stuckTime)
		{
			/* set gear and sterring command assuming car is 
			 * pointing in a direction out of track */

			// to bring car parallel to track axis
			float steer = (float) (- sensors.getAngleToTrackAxis() / steerLock); 
			int gear=-1; // gear R

			// if car is pointing in the correct direction revert gear and steer  
			if (sensors.getAngleToTrackAxis()*sensors.getTrackPosition()>0)
			{
				gear = 1;
				steer = -steer;
			}
			clutch = clutching(sensors, clutch);
			// build a CarControl variable and return it
			Action action = new Action ();
			action.gear = gear;
			action.steering = steer;
			action.accelerate = 1.0;
			action.brake = 0;
			action.clutch = clutch;
			return action;
		}

		else // car is not stuck
		{
			// compute gear 
			int gear = getGear(sensors);
			// compute steering
			float steer = getSteer(sensors);
			// compute accel/brake command
			float accel_and_brake = getAccel(sensors, steer);

			// normalize steering
			if (steer < -1)
				steer = -1;
			if (steer > 1)
				steer = 1;

			// set accel and brake from the joint accel/brake command 
			float accel,brake;
			if (accel_and_brake>0)
			{
				accel = accel_and_brake;
				brake = 0;
			}
			else
			{
				accel = 0;
				// apply ABS to brake
				brake = filterABS(sensors,-accel_and_brake);
			}

			clutch = clutching(sensors, clutch);

			// build a CarControl variable and return it
			Action action = new Action ();
			action.gear = gear;
			action.steering = steer;
			action.accelerate = accel;
			action.brake = brake;
			action.clutch = clutch;
			return action;
		}
	}

	private float filterABS(SensorModel sensors,float brake){
		// convert speed to m/s
		float speed = (float) (sensors.getSpeed() / 3.6);
		// when spedd lower than min speed for abs do nothing
		if (speed < absMinSpeed)
			return brake;

		// compute the speed of wheels in m/s
		float slip = 0.0f;
		for (int i = 0; i < 4; i++)
		{
			slip += sensors.getWheelSpinVelocity()[i] * wheelRadius[i];
		}
		// slip is the difference between actual speed of car and average speed of wheels
		slip = speed - slip/4.0f;
		// when slip too high applu ABS
		if (slip > absSlip)
		{
			brake = brake - (slip - absSlip)/absRange;
		}

		// check brake is not negative, otherwise set it to zero
		if (brake<0)
			return 0;
		else
			return brake;
	}

	float clutching(SensorModel sensors, float clutch){
		return 0;
		/*
		float maxClutch = clutchMax;

		// Check if the current situation is the race start
		if (sensors.getCurrentLapTime()<clutchDeltaTime  && getStage()==Stage.RACE && sensors.getDistanceRaced()<clutchDeltaRaced)
			clutch = maxClutch;

		// Adjust the current value of the clutch
		if(clutch > 0)
		{
			double delta = clutchDelta;
			if (sensors.getGear() < 2)
			{
				// Apply a stronger clutch output when the gear is one and the race is just started
				delta /= 2;
				maxClutch *= clutchMaxModifier;
				if (sensors.getCurrentLapTime() < clutchMaxTime)
					clutch = maxClutch;
			}

			// check clutch is not bigger than maximum values
			clutch = Math.min(maxClutch,clutch);

			// if clutch is not at max value decrease it quite quickly
			if (clutch!=maxClutch)
			{
				clutch -= delta;
				clutch = Math.max((float) 0.0,clutch);
			}
			// if clutch is at max value decrease it very slowly
			else
				clutch -= clutchDec;
		}
		return clutch;
		*/
	}

	public float[] initAngles(){

		float[] angles = new float[19];

		/* set angles as {-90,-75,-60,-45,-30,-20,-15,-10,-5,0,5,10,15,20,30,45,60,75,90} */
		for (int i=0; i<5; i++)
		{
			angles[i]=-90+i*15;
			angles[18-i]=90-i*15;
		}

		for (int i=5; i<9; i++)
		{
			angles[i]=-20+(i-5)*5;
			angles[18-i]=20-(i-5)*5;
		}
		angles[9]=0;
		return angles;
	}
}
