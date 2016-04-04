

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
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
public class Chef extends SimProcess {

	/**
	* Keeps a reference to the model this actor is a part of 
	* useful shortcut to access the model infrastructure
	*/
	private HamburguerModel myModel;
	
	/**
	 * This method constructs a new VC
	 *
	 * @param owner desmoj.Model     the associated model
	 * @param name java.lang.String  of the VC
	 * @param showInTrace boolean    show in trace file or not show in trace 
	 */
	public Chef(Model owner, String name, boolean showInTrace) {
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
			if (myModel.EmployeeNewOrderQueue.isEmpty()) { // NO,there is no one waiting
                                //myModel.boredChefQueue.insert(this);
				// insert yourself into the idle VC queue
				myModel.idleChefQueue.insert(this);

				// and wait for things to happen
				passivate();
				
			} else { //YES,there is a customer waiting

				//get the next employee to service station
				Employee nextEmployee = myModel.EmployeeNewOrderQueue.first();
				//again first() does not mean it is removed yet, so...
				myModel.EmployeeNewOrderQueue.remove(nextEmployee);
				
				//now service it
				//service time is represented by a hold to the VC process
				hold(new TimeSpan(myModel.getChefServiceTime()));
				//from inside to outside...
				//...draw a new period of service time
				//...make a TimeInstant object out of it
				//...and hold for this amount of time

				//now the truck has received its container and can leave
				//we will reactivate it though, to allow him to do some
				//more message sending
                                //nextEmployee.endWait();
				nextEmployee.activate(new TimeSpan(0.0));
				//the VC can return to top and check for a new customer
			}
		}
	}
}
