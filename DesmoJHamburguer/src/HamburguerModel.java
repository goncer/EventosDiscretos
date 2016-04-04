

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import desmoj.core.dist.ContDistExponential;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.util.AccessPoint;
import desmoj.core.util.Parameterizable;
import desmoj.extensions.experimentation.reflect.MutableFieldAccessPoint;
import desmoj.core.statistic.Histogram;
import desmoj.core.statistic.TimeSeries;
/**
 * This class is the "main" class of the vancarrier_1st_p model.
 * It is derived from desmoj.model and consists of all the
 * needed infrastructural issues of a DESMO-J based model.
 * It provides all random streams , queues, descriptions,
 * and simulation steering needed.
 * See the method comments for further information. 
 *
 * @author: Olaf Neidhardt
 */
public class HamburguerModel extends Model implements Parameterizable {
    
	/**
	* Random stream used to draw an arrival time for the next truck.
	* See Vancarrier_1st_p_model.init() method for stream parameters.
	*/
	private ContDistExponential clientArrivalTime;
	
	/**
	* Random stream used to draw a service time for this truck.
	* Describes the time needed by the VC to get and load the container
	* on the truck.
	* See Vancarrier_1st_p_model.init() method for stream parameters.
	*/
	//private ContDistUniform serviceTime;
        private ContDistExponential serviceTime;
        private ContDistExponential chefserviceTime;
        private ContDistExponential clientPaidTime;
	
	/**
	* A waiting-queue object is used to represent the parking area for
	* the trucks.
	* Every time a truck arrives it is inserted into this queue (it parks)
	* and will be removed by the VC for service.
	*
	* This way all necessary basic statistics are monitored by the queue.
	*/
	protected ProcessQueue<Clients> clientsQueue;
	protected ProcessQueue<Employee> employeeQueue;
	/**
	* A waiting-queue object is used to represent the parking spot for
	* the VC.
	* If there is no truck waiting for service the VC will return here
	* and wait for the next truck to come.
	* (Note: We don't use a status field here due to statistical reasons.)
	*
	* This way all idle time statistics of the VC are monitored by the queue.
	*/
	protected ProcessQueue<Employee> idleEmployeeQueue;
        protected ProcessQueue<Employee> boredEmployeeQueue;
        protected ProcessQueue<Employee> EmployeeNewOrderQueue;
        protected ProcessQueue<Chef> idleChefQueue;
        
        /** Model parameter: number of chefs */
	protected int chefNumber;
        
	/** Model parameter: number of VCs */
	protected int employeeNumber;

	/** Records numbers of arrived */
	protected TimeSeries clientsArrived;
	
	/** Records numbers of serviced trucks */
	protected TimeSeries clientsServiced;
	
	/** Records truck wait times */
	protected Histogram waitTimeHistogram;
        
        protected Histogram waitTimeEmployeeHistogram;

	/** Number of arrived trucks */
	protected int arrivedClients = 0;

	/** Number of finished trucks */
	protected int servicedClients = 0;
        
        protected double totalWastedTime = 0;


	/**
	 * Vancarrier_1st_p_model constructor.
	 *
	 * Creates a new Vancarrier_1st_p model with calling
	 * the superclasses constructor.
	 *
	 * @param owner desmoj.Model
	 * @param modelname java.lang.String
	 * @param showInReport boolean
	 * @param showInTrace boolean
	 */
	public HamburguerModel(
		Model owner,
		String modelName,
		boolean showInReport,
		boolean showInTrace) {
		super(owner, modelName, showInReport, showInTrace);
		employeeNumber = 4;
                chefNumber = 1;
	}

	/** 
	 * 	Default Constructor for use with Launcher. 
	 * 	showInReport and showInTrace are set to true. 
	 */
	public HamburguerModel() {
		this(null, "HamburguerModel", true, true);
	}

	/**
	 * Describes what the model does
	 */
	public String description() {
		return "This is the Vancarrier_1st_P_Model description,"
			+ "which means it is the first VanCarrier model in a "
			+ "process oriented version."
			+ " "
			+ "This model is a service station model located at a "
			+ "container harbour. Conatinertrucks will arrive and "
			+ "require the loading of a container. A Vancarrier (VC) is "
			+ "on duty and will head off to find the required container "
			+ "in the storage. He will then deliver the container to the "
			+ "truck. The truck then leaves the area."
			+ "In case the VC is busy, the truck waits "
			+ "for his turn on the parking-lot."
			+ "If the VC is idle, it waits on his own parking spot for the "
			+ "truck to come.";
	}
	/**
	 * doInitialSchedules method comment.
	 *
	 * This method is used to place all events or processes on the
	 * internal event-list of the simulator, which are necessary to start
	 * the simulation.
	 *
	 * In this case a first truck arrival and the vancarrier are neceesary entries.
	 */
	public void doInitialSchedules() {
	    
		//create the servicer, here make a vancarrier
		for (int i = 0; i < employeeNumber; i++) {
		    Employee eployee = new Employee(this, "Employee", true);

			//put the vancarrier on duty with placing it on the event-list first
			//it will deactivate itself into waiting status  
			//for the first truck right after activation
			eployee.activate();
		}
               
                for (int i = 0; i < chefNumber; i++) {
		    Chef chef = new Chef(this, "Chef", true);
                    chef.activate();
		}
                

		//create a truck spring
		ClientsGenerator firstarrival =
			new ClientsGenerator(this, "ClientArrival", false);

		//place the truck generator on the event-list, in order to
		//start producing truck arrivals when the first truck comes
		//therefore we must use "schedule" instead of "activate"
		firstarrival.schedule(new TimeSpan(getClientArrivalTime()));

	}
	
	/**
	 * Returns a sample out of the random stream used to measure
	 * time needed to find the container for this truck in the
	 * storage and the time the VC needs to get it to the truck. 
	 * 
	 * Creation date: (30.03.00 12:06:04)
	 * @return double    a serviceTime sample
	 */
	public double getServiceTime() {
		return serviceTime.sample();
	}
        public double getChefServiceTime() {
		return chefserviceTime.sample();
	}
         public double getClientPaidServiceTime(){
                return clientPaidTime.sample();
        }
	
	/**
	 * Returns a sample out of the random stream used to measure
	 * the next truck arrival time. 
	 *
	 * Creation date: (30.03.00 12:00:05)
	 * @return double       a truckArrivalTime sample
	 */
	public double getClientArrivalTime() {
		return clientArrivalTime.sample();
	}
       
	
	/**
	 * This method is used to initialize all 
	 * DESMO-J infrastructure we use
	 */
	public void init() {

        // dater collectors
        clientsArrived = new TimeSeries(this, "arrived", new TimeInstant(0), new TimeInstant(1500), true, false);
        clientsServiced = new TimeSeries(this, "finished", new TimeInstant(0), new TimeInstant(1500), true, false);
        waitTimeHistogram = new Histogram(this, "Client Wait Times", 0, 1000, 10, true, false);
        waitTimeEmployeeHistogram = new Histogram(this, "Employee Wait Times", 0, 20, 10, true, false);

        // distributions
        serviceTime = new ContDistExponential(this, "ServiceTimeStream",5.0,true,false);
        chefserviceTime = new ContDistExponential(this, "ChefServiceTimeStream",10.0,true,false);
        clientPaidTime = new ContDistExponential(this, "clientPaidTimeStream",2.0,true,false);
        
		//serviceTime = new ContDistUniform(this, "ServiceTimeStream", 3.0, 7.0, true, false);
        clientArrivalTime = new ContDistExponential(this, "ClientsArrivalTimeStream", 7.0, true, false);
		//truckArrivalTime = new ContDistExponential(this, "TruckArrivalTimeStream", 3.0, true, false);

		// queues
		clientsQueue = new ProcessQueue<Clients>(this, "Clients Queue", true, false);
		
                idleEmployeeQueue = new ProcessQueue<Employee>(this, "idle Employee Queue", true, false);
                boredEmployeeQueue = new ProcessQueue<Employee>(this, "Employee wait for cheff ", true, false);
                EmployeeNewOrderQueue = new ProcessQueue<Employee>(this, "idle Employee Queue", false, false);
                idleChefQueue = new ProcessQueue<Chef>(this, "idle Chef Queue", true, false);
                //boredChefQueue  = new ProcessQueue<Chef>(this, "idle Chef Queue", true, false);
	}
	
	/**
	 * Starts the application.
	 *
	 * In DESMO-J used to 
	 *    - instantiate the experiment
	 *    - instantiate the model
	 *    - connect the model to the experiment
	 *		- steer length of simulation and outputs
	 *		- start the simulation
	 *		- set the ending criteria (normally the time)
	 *		- initiate reporting
	 *		- clean up the experiment
	 *
	 * @param args  : is an array of command-line arguments
	 */
	public static void main(java.lang.String[] args) {

		// make a new experiment
		// Use as experiment name a OS filename compatible string!!
		// Otherwise your simulation will crash!!
        Experiment.setEpsilon(java.util.concurrent.TimeUnit.SECONDS);
        Experiment.setReferenceUnit(java.util.concurrent.TimeUnit.MINUTES);
        Experiment experiment =
            new Experiment("Hamburguer Model");

		// make a new model
		// null as first parameter because it is the main model and has no mastermodel
		HamburguerModel vc_1st_p_Model =
			new HamburguerModel(
				null,
				"Hamburguer Model",
				true,
				false);

		// connect Experiment and Model
		vc_1st_p_Model.connectToExperiment(experiment);

        // set trace
		experiment.tracePeriod(new TimeInstant(0), new TimeInstant(100));
                
		// now set the time this simulation should stop at 
		// let him work 1500 Minutes
		experiment.stop(new TimeInstant(1500));
		experiment.setShowProgressBar(false);

		// start the Experiment with start time 0.0
		experiment.start();

		// --> now the simulation is running until it reaches its ending criteria
		// ...
		// ...
		// <-- after reaching ending criteria, the main thread returns here

		// print the report about the already existing reporters into the report file
		experiment.report();
             
		// stop all threads still alive and close all output files
		experiment.finish();
	}

	/** 
	 * Returns the model parameters:
	 * vcNumber		: Number of VCs working in the yard.
	 */
	public Map<String, AccessPoint> createParameters() {
		Map<String, AccessPoint> pm = new TreeMap<String, AccessPoint>();
		pm.put("employeeNumber", new MutableFieldAccessPoint("employeeNumber", this));
                pm.put("chefNumber", new MutableFieldAccessPoint("chefNumber", this));
		return pm;
	}
}