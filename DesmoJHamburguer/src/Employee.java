

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
/**
 * This class is part of the "Vancarrier_1st_p_model".
 * See the description() method of the model class for
 * further documentation of the basis model.
 *
 * This class represents the vancarrier in the above 
 * mentioned model.
 * The VC is on service and waits for a truck to come
 * requesting service. He will head for the container and
 * deliver it to the truck.
 * If there is another truck waiting he services it.
 * If not he waits for the next truck to arrive.
 *
 * Creation date: (29.03.00 14:13:13)
 * @author: Olaf Neidhardt
 */
public class Employee extends SimProcess {

	/**
	* Keeps a reference to the model this actor is a part of 
	* useful shortcut to access the model infrastructure
	*/
	private HamburguerModel myModel;
	
        private TimeInstant startWait;
	
	private TimeInstant endWait;
        
        private TimeInstant startWaitChef;
	
	private TimeInstant endWaitChef;
        
        
        private Clients currentClient;
	/**
	 * This method constructs a new VC
	 *
	 * @param owner desmoj.Model     the associated model
	 * @param name java.lang.String  of the VC
	 * @param showInTrace boolean    show in trace file or not show in trace 
	 */
	public Employee(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);

		myModel = (HamburguerModel) owner;

	}
	/**
	 * This lifeCycle() describes what the vancarrier (VC) does when it
	 * becomes activated by DESMO-J,
	 * 
	 * It will cycle through a process like this:
	 * Check if there is a customer waiting.
	 * If there is someone waiting 
	 *   a) remove customer from queue
	 *   b) service customer
	 *   c) return to top
	 * If there is no one waiting
	 *   a) wait until you get activated again
	 *	 b) then return to top
	 *   
	 * The eventRoutine()/lifeCycle() methods are one of the most import
	 * methods within DESMO-J based simulations. This is where the real
	 * action happens.
	 * 
	 */
	public void lifeCycle() throws SuspendExecution {
	    
		//the servicer is always on duty and will never stop working
		while (true) {
			//check if there is someone waiting
			if (myModel.clientsQueue.isEmpty()) { // NO,there is no one waiting

				// insert yourself into the idle VC queue
				myModel.idleEmployeeQueue.insert(this);

				// and wait for things to happen
				passivate();
				
			} else { //YES,there is a customer waiting
                            
                            //get the next truck to service station
				currentClient = myModel.clientsQueue.first();
				//again first() does not mean it is removed yet, so...
				myModel.clientsQueue.remove(currentClient);
				
				hold(new TimeSpan(myModel.getServiceTime()));
                                startWait = presentTime();
                                myModel.boredEmployeeQueue.insert(this);
                            //thats all, I can sit back and wait untill chef is free or gime me the food.
                            // is any Chef available? 
                            if (!myModel.idleChefQueue.isEmpty()) { // it is available
                                
                                endWaitChef = presentTime();
                                //get the first (and only) VC from the idle VC queue
                                Chef chef = myModel.idleChefQueue.first();
                                //myModel.boredChefQueue.remove(chef);
                                //to get it does not mean it is removed yet (?!), so do it now:
                                myModel.idleChefQueue.remove(chef);

                                //place the VC on the event-list right after me,
                                //to ensure that I will be the next customer to get serviced
                                chef.activateAfter(this);
                                myModel.EmployeeNewOrderQueue.insert(this);
                                //thats all, I can sit back and wait
                                passivate();
                                
                                } else { // it is NOT available
                                        startWaitChef = presentTime();   
                                        // thus I do nothing and wait for service
                                        passivate();
                                }
                            myModel.boredEmployeeQueue.remove(this);
                            endWait = presentTime();
                            myModel.waitTimeEmployeeHistogram.update(getWaitTime());       
                            myModel.totalWastedTime += getWaitTime(); 
                            //passivate();
                            hold(new TimeSpan(myModel.getClientPaidServiceTime()));
                            currentClient.endWait();
                            currentClient.activate(new TimeSpan(0.0));
                            
			}
		}
	}
        public void endWait() throws SuspendExecution {
                endWait = presentTime();
	}
	
	public double getWaitTime() {
		if (startWait != null && endWait != null) 
			return TimeOperations.diff(startWait, endWait).getTimeAsDouble();
		else
			return Double.NaN;
	}
}
