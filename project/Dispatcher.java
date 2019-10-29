/**
 * @author Lucian
 * marked = true
 */

public class Dispatcher {
	private float arrivalTime;
	private float serviceTime;
	private boolean marked;
	
	public Dispatcher(float arrivalTime, float serviceTime, boolean marked){
		this.arrivalTime = arrivalTime;
		this.serviceTime = serviceTime;
		this.marked = marked;
	}
	
	public float getArrivalTime(){
		return this.arrivalTime;
	}
	
	public float getServiceTime(){
		return this.serviceTime;
	}
	
	public boolean getMarked(){
		return this.marked;
	}
	
	public void setMarked(boolean marked){
		this.marked = marked;
	}
}
