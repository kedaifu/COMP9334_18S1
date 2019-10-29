/**
 * @author Senlin Deng, z5014567
 * 
 * State: 
 * 	OFF = 0
 *	SETUP = 1
 *	BUSY = 2
 *	DELAYEDOFF = 3
 *
 */

public class Server {
	
	private int state;
	
	//SETUP
	private float setupCompletes;
	
	//DELAYEDOFF
	private float delayedoffExpires;
	
	//BUSY
	private float busyArrivalTime;
	private float busyProcessTime;
	
	public Server(int state){
		this.state = state;
		setSetupCompletes(0);
		setDelayedoffExpires(0);
		setBusyArrivalTime(0);
		setBusyProcessTime(0);
	}
	
	public int getState(){
		return this.state;
	}

	public float getSetupCompletes() {
		return setupCompletes;
	}

	public void setSetupCompletes(float setupCompletes) {
		this.setupCompletes = setupCompletes;
	}

	public float getDelayedoffExpires() {
		return delayedoffExpires;
	}

	public void setDelayedoffExpires(float delayedoffExpires) {
		this.delayedoffExpires = delayedoffExpires;
	}

	public float getBusyArrivalTime() {
		return busyArrivalTime;
	}

	public void setBusyArrivalTime(float busyArrivalTime) {
		this.busyArrivalTime = busyArrivalTime;
	}

	public float getBusyProcessTime() {
		return busyProcessTime;
	}

	public void setBusyProcessTime(float busyProcessTime) {
		this.busyProcessTime = busyProcessTime;
	}
	
	public void init(int state){
		this.state = state;
		setupCompletes = 0;
		delayedoffExpires = 0;
		busyArrivalTime = 0;
		busyProcessTime= 0;	
	}
	
}
