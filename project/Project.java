import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

public class Project {
	
	private int numTests;
	
	private String mode;
	private int numServer;
	private float setupTime;
	private float delayedOffTime;
	
	//trace
	private ArrayList<Float> arrival;
	private ArrayList<Float> service;
	
	//random special
	
	private float timeEnd;
	private float lambda;
	private float mu;
	
	//queue and server
	private ArrayList<Dispatcher> queue;
	private ArrayList<Server> server;
	
	public Project(int numTests){
		this.numTests = numTests;
		this.arrival = new ArrayList<Float> ();
		this.service = new ArrayList<Float> ();
		this.queue = new ArrayList<Dispatcher> ();
		this.server = new ArrayList<Server> ();
	}
	
	public void outPutFile() {
		
		//num of test files
		for(int i=1; i< numTests+1;i++){
			
			int finishedJob = 0;
			float totalTime = 0;
			PrintWriter writer =null;
			PrintWriter meanWriter = null;
			try {
				writer = new PrintWriter("departure_"+i+".txt", "UTF-8");

				//initialize variables
				float masterClock = 0;	
				this.arrival = new ArrayList<Float> ();
				this.service = new ArrayList<Float> ();
				this.queue = new ArrayList<Dispatcher> ();
				this.server = new ArrayList<Server> ();
				
				/*
				 * get the mode of test file:
				 * trace or random
				 * */
				modeRead(i);
				
				//trace mode:
				if(mode.equals("trace")){
					
					/*
					 * initialize:
					 * 		numServer
					 * 		setupTime
					 * 		delayedOffTime
					 * 		arrival list
					 * 		service list
					 * 
					 * */
					traceParaRead(i);
					traceArrivalRead(i);
					traceServiceRead(i);
					
					
					/*
					 * 
					 * all server start from OFF
					 * 
					 * */
					for(int t = 0; t<numServer;t++){
						server.add(new Server(0));
					}
					
					
					/*
					 * four events:
					 * 		arrival (type = 0)
					 * 		departure (type = 1)
					 * 		SETUP time finished: SETUP->ON (type = 2)
					 * 		Tc finished: DELAYEDOFF -> OFF (type = 3)
					 * */ 
					Event event = new Event(numServer, arrival.get(0));
					int nextEventType = -1;
					float nextEventTime = 0;
					
					//initialize service time of next arrival
					float serviceTimeNextArrival = service.get(0);
					
					int jobCounter = 1;
					int jobNum = arrival.size();
					
					 
					while(true){

						nextEventType = event.nextEvent();
						
						if(nextEventType == 0){
							nextEventTime = event.minArrival();
						}
						else if(nextEventType == 1){
							nextEventTime = event.minDeparture();
						}
						else if(nextEventType == 2){
							nextEventTime = event.minSetUp();
						}
						else if(nextEventType == 3){
							nextEventTime = event.minDelayed();
						}
						
						
						//update M.C
						masterClock = nextEventTime;

						/**
						 * arrival case
						 */
						if(nextEventType == 0){
							
							int existDO = 0;
							int existOFF = 0;
							float maxDelayOffExpire = 0;
							
							for(Server tmpS:server){
								//exist delayed off
								if(tmpS.getState() == 3){
									existDO = 1;
									if(tmpS.getDelayedoffExpires()!= 0 && maxDelayOffExpire<tmpS.getDelayedoffExpires()){
										maxDelayOffExpire = tmpS.getDelayedoffExpires();
									}
								}
								//exist off
								if(tmpS.getState() == 0){
									existOFF = 1;
								}
							}
							
							//if exsit delayed off:
							if(existDO == 1){
								int j = 0;
								for(Server tmpS:server){
									if(tmpS.getDelayedoffExpires() == maxDelayOffExpire){
										tmpS.init(2);
										
										tmpS.setBusyArrivalTime(event.minArrival());
										
										tmpS.setBusyProcessTime(serviceTimeNextArrival);
										event.updateDeparture(j, masterClock+serviceTimeNextArrival);
										event.updateDelayed(j, Float.POSITIVE_INFINITY);
										
									}
									j++;
								}	
							}else{
								if(existOFF == 1){ 
									int j = 0;
									for(Server tmpS: server){
										if(tmpS.getState() == 0){
											tmpS.init(1);
											tmpS.setSetupCompletes(event.minArrival()+setupTime);
											event.updateSetUp(j, masterClock+setupTime);
											break;
										}
										j++;
									}
									//add to queue marked
									queue.add(new Dispatcher(event.minArrival(), serviceTimeNextArrival, true));
								}else{
									//add to queue unmarked
									queue.add(new Dispatcher(event.minArrival(), serviceTimeNextArrival, false));
								}
							}
							
							if(jobCounter < jobNum){
								event.updateArrival(arrival.get(jobCounter));
								serviceTimeNextArrival = service.get(jobCounter);
								jobCounter++;
							}else{
								event.updateArrival(Float.POSITIVE_INFINITY);
							}
						}
						
						/**
						 * departure case
						 */
						if(nextEventType == 1){
							
							int minIndex = event.minDepartureIndex();
							totalTime += masterClock - server.get(minIndex).getBusyArrivalTime();
							//output the fuking thing 
							writer.printf("%.3f",server.get(minIndex).getBusyArrivalTime());
							writer.print("\t");		
							writer.printf("%.3f", masterClock);
							writer.println();
							
							if(queue.size() == 0){
								
								server.set(minIndex,new Server(3));
								server.get(minIndex).setDelayedoffExpires(masterClock+delayedOffTime);
								event.updateDeparture(minIndex, Float.POSITIVE_INFINITY);
								event.updateDelayed(minIndex, masterClock+delayedOffTime);
								
								
							}else{
								server.set(minIndex,new Server(2));
								Dispatcher firstEle = queue.get(0);
								server.get(minIndex).setBusyArrivalTime(firstEle.getArrivalTime());
								server.get(minIndex).setBusyProcessTime(firstEle.getServiceTime());
								event.updateDeparture(minIndex, firstEle.getServiceTime()+masterClock);
								queue.remove(0);
								
								if(firstEle.getMarked() == true){
									int existUnmarked = 0 ;
									for(Dispatcher tmpD:queue){
										if(tmpD.getMarked() == false){
											tmpD.setMarked(true);
											existUnmarked = 1;
											break;
										}
									}
									if(existUnmarked == 0){
										server.set(event.maxSetUpIndex(), new Server(0));
										event.updateSetUp(event.maxSetUpIndex(), Float.POSITIVE_INFINITY);
									}		
								}
							}
							
							finishedJob++;
							if(finishedJob == jobNum){
								break;
							}
						}
						
						/**
						 * finished its setup
						 */
						if(nextEventType == 2){
							
							Dispatcher firstEle = queue.get(0);
							queue.remove(0);
							
							int maxSetUpIndex = event.minSetUpIndex();
							
							event.updateSetUp(maxSetUpIndex, Float.POSITIVE_INFINITY);
							
							server.set(maxSetUpIndex, new Server(2));
							server.get(maxSetUpIndex).setBusyArrivalTime(firstEle.getArrivalTime());
							
							server.get(maxSetUpIndex).setBusyProcessTime(firstEle.getServiceTime());
							event.updateDeparture(maxSetUpIndex, firstEle.getServiceTime()+masterClock);
						}
						
						/**
						 * expiry of the countdown timer
						 */
						if(nextEventType == 3){
							
							int minDOIndex = event.minDelayedIndex();
							event.updateDelayed(minDOIndex, Float.POSITIVE_INFINITY);
							server.set(minDOIndex, new Server(0));
						}
						
					}
					
					 
				}
				
				
				//random mode:
				if(mode.equals("random")){
					randomParaRead(i);
					randomArrivalRead(i);
					randomServiceRead(i);
					//setupTime, delayedOffTime
					//timeEnd,lambda,mu
					
					//initialize server
					for(int t = 0; t<numServer;t++){
						server.add(new Server(0));
					}
	
					
					float nextArrivalTime = (float) (-Math.log(1- Math.random(1))/lambda);
					float serviceTimeNextArrival = 0; 
					for(int kdf = 0;kdf<3;kdf++){
						serviceTimeNextArrival += (float) (-Math.log(1- Math.random(1))/mu);
					}
					
					Event event = new Event(numServer, nextArrivalTime);
					
					float nextEventTime = 0;
					int nextEventType = -1;
					
					
					while(masterClock<timeEnd){
						
						nextEventType = event.nextEvent();
						
						/**
						 * update next event time
						 *		0: arrival
						 *		1: departure
						 * 		2: finished its setup
						 *		3: expiry of the countdown timer
						 */
						if(nextEventType == 0){
							nextEventTime = event.minArrival();
						}
						else if(nextEventType == 1){
							nextEventTime = event.minDeparture();
						}
						else if(nextEventType == 2){
							nextEventTime = event.minSetUp();
						}
						else if(nextEventType == 3){
							nextEventTime = event.minDelayed();
						}
						
						//update M.C.
						masterClock = nextEventTime;
						if(masterClock>=timeEnd){
							break;
						}
						
						/**
						 * arrival case
						 */
						if(nextEventType == 0){
							
							int existDO = 0;
							int existOFF = 0;
							float maxDelayOffExpire = 0;
							
							for(Server tmpS:server){
								
								//exist off
								if(tmpS.getState() == 0){
									existOFF = 1;
								}
								//exist delayed off
								if(tmpS.getState() == 3){
									existDO = 1;
									if(tmpS.getDelayedoffExpires()!= 0 && maxDelayOffExpire<tmpS.getDelayedoffExpires()){
										maxDelayOffExpire = tmpS.getDelayedoffExpires();
									}
								}
							}
							
							//if exsit delayed off:
							if(existDO == 1){
								int j = 0;
								for(Server tmpS:server){
									if(tmpS.getDelayedoffExpires() == maxDelayOffExpire){
										tmpS.init(2);
										tmpS.setBusyArrivalTime(event.minArrival());
										tmpS.setBusyProcessTime(serviceTimeNextArrival);
										event.updateDeparture(j, masterClock+serviceTimeNextArrival);
										event.updateDelayed(j, Float.POSITIVE_INFINITY);
									}
									j++;
								}	
							}else{
								if(existOFF == 1){
									 
									int j = 0;
									int k = 0;
									for(Server tmpS: server){
										if(tmpS.getState() == 0){
											tmpS.init(1);
											tmpS.setSetupCompletes(event.minArrival()+setupTime);
											k=j;
											break;
										}
										j++;
									}
									
									event.updateSetUp(k, masterClock+setupTime);
									 
									//add to queue marked
									queue.add(new Dispatcher(event.nextArrivalTime, serviceTimeNextArrival, true));
								}else{
									//add to queue unmarked
									queue.add(new Dispatcher(event.nextArrivalTime, serviceTimeNextArrival, false));
								}
							}
							nextArrivalTime += -Math.log(1- Math.random(1))/lambda;
							event.updateArrival(nextArrivalTime);
							
							serviceTimeNextArrival = 0;
							for(int kdf = 0;kdf<3;kdf++){
								serviceTimeNextArrival += (float) (-Math.log(1- Math.random(1))/mu);
							}
							
						}
						
						
						/**
						 * departure case
						 */
						if(nextEventType == 1){
							finishedJob++;
							int minIndex = event.minDepartureIndex();
							totalTime += masterClock - server.get(minIndex).getBusyArrivalTime();
							//nothing in the queue
							if(queue.size() == 0){
							
								writer.printf("%.3f",server.get(minIndex).getBusyArrivalTime());
								writer.print("\t");		
								writer.printf("%.3f", masterClock);
								writer.println();
								server.set(minIndex,new Server(3));
								server.get(minIndex).setDelayedoffExpires(delayedOffTime+masterClock);
								event.updateDeparture(minIndex, Float.POSITIVE_INFINITY);
								event.updateDelayed(minIndex, delayedOffTime+masterClock);
						
							}else{
								 
								writer.printf("%.3f",server.get(minIndex).getBusyArrivalTime());
								writer.print("\t");		
								writer.printf("%.3f", masterClock);
								writer.println();
								server.set(minIndex,new Server(2));
								Dispatcher firstEle = queue.get(0);
								server.get(minIndex).setBusyArrivalTime(firstEle.getArrivalTime());
								server.get(minIndex).setBusyProcessTime(firstEle.getServiceTime());
								event.updateDeparture(minIndex, firstEle.getServiceTime()+masterClock);
								queue.remove(0);
								
								if(firstEle.getMarked() == true){
									int existUnmarked = 0 ;
									for(Dispatcher tmpD:queue){
										if(tmpD.getMarked() == false){
											tmpD.setMarked(true);
											existUnmarked = 1;
											break;
										}
									}
									if(existUnmarked == 0){
										server.set(event.maxSetUpIndex(), new Server(0));
										event.updateSetUp(event.maxSetUpIndex(), Float.POSITIVE_INFINITY);
									}		
								}
							}
						}
						
						
						
						/**
						 * finished its setup
						 */
						if(nextEventType == 2){
							
							Dispatcher firstEle = queue.get(0);
							queue.remove(0);
							
							int maxSetUpIndex = event.minSetUpIndex();
							
							event.updateSetUp(maxSetUpIndex, Float.POSITIVE_INFINITY);
							
							server.set(maxSetUpIndex, new Server(2));
							server.get(maxSetUpIndex).setBusyArrivalTime(firstEle.getArrivalTime());
							
							server.get(maxSetUpIndex).setBusyProcessTime(firstEle.getServiceTime());
							event.updateDeparture(maxSetUpIndex, firstEle.getServiceTime()+masterClock);
						}
						
						/**
						 * expiry of the countdown timer
						 */
						if(nextEventType == 3){
							
							int minDOIndex = event.minDelayedIndex();
							event.updateDelayed(minDOIndex, Float.POSITIVE_INFINITY);
							server.set(minDOIndex, new Server(0));
						}
						
					
					}
				}
				
				
			} catch (FileNotFoundException e) {
				 
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				 
				e.printStackTrace();
			}finally{
				if (writer != null) writer.close();
			}
			
			try {
				meanWriter = new PrintWriter("mrt_"+i+".txt", "UTF-8");
				
				float mrt = totalTime/finishedJob;
				meanWriter.printf("%.3f", mrt);
				meanWriter.println();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if (meanWriter != null) meanWriter.close();
			}
			
			
		}
	}
	
	
	
	
	/**
	 * read from mode_i.txt, get the mode type of current test
	 * 
	 * @param i the index of the test file
	 */
	public void modeRead(int i){
		Scanner modeInput = null;
		try{
			modeInput = new Scanner(new File("mode_"+i+".txt"));
			String line = modeInput.nextLine();
			String[] words = line.split(" ");
			mode  = words[0];
		}
		catch (FileNotFoundException e){
			System.out.println(e.getMessage());
		}
		finally{
			if (modeInput != null) modeInput.close();
		}
	}
	
	
	
	
	public void traceParaRead(int i){
		Scanner paraInput = null;
		try{
			paraInput = new Scanner(new File("para_"+i+".txt"));
			int n = 0;
			String line;
			while(paraInput.hasNextLine()){
				line = paraInput.nextLine();
				String[] words = line.split(" ");
				if(n == 0){
					numServer = Integer.parseInt(words[0]);
				}
				if(n == 1){
					setupTime = Float.parseFloat(words[0]);
				}
				if(n ==2){
					delayedOffTime = Float.parseFloat(words[0]);
				}
				n++;
			}
			
		}
		catch (FileNotFoundException e){
			System.out.println(e.getMessage());
		}
		finally{
			if (paraInput != null) paraInput.close();
		}
	}
	
	public void randomParaRead(int i){
		Scanner paraInput = null;
		try{
			paraInput = new Scanner(new File("para_"+i+".txt"));
			int n = 0;
			String line;
			while(paraInput.hasNextLine()){
				line = paraInput.nextLine();
				String[] words = line.split(" ");
				if(n == 0){
					numServer = Integer.parseInt(words[0]);
				}
				if(n == 1){
					setupTime = Float.parseFloat(words[0]);
				}
				if(n == 2){
					delayedOffTime = Float.parseFloat(words[0]);
				}
				if(n == 3){
					timeEnd = Float.parseFloat(words[0]);;
				}
				n++;
			}
			
		}
		catch (FileNotFoundException e){
			System.out.println(e.getMessage());
		}
		finally{
			if (paraInput != null) paraInput.close();
		}
	}
	
	public void traceArrivalRead(int i){
		Scanner arrivalInput = null;
		try{
			arrivalInput = new Scanner(new File("arrival_"+i+".txt"));
			while(arrivalInput.hasNextLine()){
				String line = arrivalInput.nextLine();
				String[] words = line.split(" ");
				float arvTime = Float.parseFloat(words[0]);
				this.arrival.add(arvTime);
			}
			
		}
		catch (FileNotFoundException e){
			System.out.println(e.getMessage());
		}
		finally{
			if (arrivalInput != null) arrivalInput.close();
		}
		
	}
	
	public void traceServiceRead(int i){
		Scanner serviceInput = null;
		try{
			serviceInput = new Scanner(new File("service_"+i+".txt"));
			while(serviceInput.hasNextLine()){
				String line = serviceInput.nextLine();
				String[] words = line.split(" ");
				float svcTime = Float.parseFloat(words[0]);
				this.service.add(svcTime);
			}
			
		}
		catch (FileNotFoundException e){
			System.out.println(e.getMessage());
		}
		finally{
			if (serviceInput != null) serviceInput.close();
		}
		
	}
	
	
	public void randomArrivalRead(int i){
		Scanner arrivalInput = null;
		try{
			arrivalInput = new Scanner(new File("arrival_"+i+".txt"));
			String line = arrivalInput.nextLine();
			String[] words = line.split(" ");
			lambda  = Float.parseFloat(words[0]);			
		}
		catch (FileNotFoundException e){
			System.out.println(e.getMessage());
		}
		finally{
			if (arrivalInput != null) arrivalInput.close();
		}
	}
	
	public void randomServiceRead(int i){
		Scanner serviceInput = null;
		try{
			serviceInput = new Scanner(new File("service_"+i+".txt"));
			String line = serviceInput.nextLine();
			String[] words = line.split(" ");
			mu = Float.parseFloat(words[0]);
		}
		catch (FileNotFoundException e){
			System.out.println(e.getMessage());
		}
		finally{
			if (serviceInput != null) serviceInput.close();
		}
	}
	
}
