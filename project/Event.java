import java.util.ArrayList;

public class Event {
	
	float nextArrivalTime;
	ArrayList<Float> nextDepartureTime = new ArrayList<Float>();
	ArrayList<Float> nextSetupTime = new ArrayList<Float>(); 
	ArrayList<Float> nextDelayoffTime = new ArrayList<Float>();
	
	
	//initialize variables
	public Event(int numServer, float firstArrival){
		nextArrivalTime = firstArrival;
		for(int i=0;i<numServer;i++){
			nextDepartureTime.add(Float.POSITIVE_INFINITY);
			nextSetupTime.add(Float.POSITIVE_INFINITY);
			nextDelayoffTime.add(Float.POSITIVE_INFINITY);
		}
	}
	
	//min departure time
	public float minDeparture(){
		float min = Float.POSITIVE_INFINITY;
		for(Float tmp:nextDepartureTime){
			if(min>tmp){
				min = tmp;
			}
		}
		return min;
	}
	
	//min set up time

	public float minSetUp(){
		float min = Float.POSITIVE_INFINITY;
		for(Float tmp:nextSetupTime){
			if(min>tmp){
				min = tmp;
			}
		}
		return min;
	}
	
	//min Delayed off Time
	
	public float minDelayed(){
		float min = Float.POSITIVE_INFINITY;
		for(Float tmp:nextDelayoffTime){
			if(min>tmp){
				min = tmp;
			}
		}
		return min;
	}
	
	//min arrival
	
	public float minArrival(){
		return this.nextArrivalTime;
	}
	
	
	//min departure index
	public int minDepartureIndex(){
		float min = minDeparture();
		if(min == Float.POSITIVE_INFINITY){
			return -1;
		}
		for(int i=0;i<nextDepartureTime.size();i++){
			if(min == nextDepartureTime.get(i)){
				return i;
			}
		}
		return -1;
	}
	
	//min set up index
	public int minSetUpIndex(){
		float min = minSetUp();
		if(min == Float.POSITIVE_INFINITY){
			return -1;
		}
		for(int i=0;i<nextSetupTime.size();i++){
			if(min == nextSetupTime.get(i)){
				return i;
			}
		}
		return -1;
	}
	
	
	
	//min Delayed off index
	public int minDelayedIndex(){
		float min = minDelayed();
		if(min == Float.POSITIVE_INFINITY){
			return -1;
		}
		for(int i=0;i<nextDelayoffTime.size();i++){
			if(min == nextDelayoffTime.get(i)){
				return i;
			}
		}
		return -1;
	}
	
	
	
	
	
	//set departure 
	public void updateDeparture(int index, float element){
		this.nextDepartureTime.set(index, element);
	}
	
	//set set up 
	public void updateSetUp(int index, float element){
		this.nextSetupTime.set(index, element);
	}
	
	//set Delayed off 
	public void updateDelayed(int index, float element){
		this.nextDelayoffTime.set(index, element);
	}
	//set arrival
	public void updateArrival(float arrival){
		this.nextArrivalTime = arrival;
	}
	
	//next event
	//0: arrival
	//1: departure
	//2: finished its setup
	//3: expiry of the countdown timer
	public int nextEvent(){
		
		float a = minArrival();
		float b = minDeparture();
		float c = minSetUp();
		float d = minDelayed();
		//arrival
		if(a<=b && a<=c && a<=d){
			return 0;
		}
		//departure
		if(b<=a && b<=c && b<=d){
			return 1;
		}
		//set up 
		if(c<=a && c<=b && c<=d){
			return 2;
		}
		//expiry of the countdown timer
		if(d<=a && d<=b && d<=c){
			return 3;
		}
		
		return -1;
	}
	
	public float maxSetUp(){
		float max = 0;
		for(Float tmp:nextSetupTime){
			if(max<tmp && tmp!= Float.POSITIVE_INFINITY){
				max = tmp;
			}
		}
		return max;
	}
	
	public int maxSetUpIndex(){
		float max =  maxSetUp();
		if(max == Float.POSITIVE_INFINITY){
			return -1;
		}
		for(int i=0;i<nextSetupTime.size();i++){
			if(max == nextSetupTime.get(i)){
				return i;
			}
		}
		return -1;
	}
	
	public float maxDelayedOff(){
		float max = 0;
		for(Float tmp:nextDelayoffTime){
			if(max<tmp && tmp!= Float.POSITIVE_INFINITY){
				max = tmp;
			}
		}
		return max;
	}
	
	public boolean stop(){
		boolean stop = true;
		//float nextArrivalTime;
		//ArrayList<Float> nextDepartureTime = new ArrayList<Float>();
		//ArrayList<Float> nextSetupTime = new ArrayList<Float>(); 
		//ArrayList<Float> nextDelayoffTime = new ArrayList<Float>();
		if( nextArrivalTime != Float.POSITIVE_INFINITY){
			stop = false;
		}
		for(Float tmp:nextDepartureTime){
			if(tmp !=Float.POSITIVE_INFINITY){
				stop = false;
			}
		}
		for(Float tmp:nextSetupTime){
			if(tmp !=Float.POSITIVE_INFINITY){
				stop = false;
			}
		}
		for(Float tmp:nextDelayoffTime){
			if(tmp !=Float.POSITIVE_INFINITY){
				stop = false;
			}
		}
		 
		return stop;
	}
}
