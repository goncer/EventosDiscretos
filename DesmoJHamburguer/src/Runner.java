

import java.util.Map;
import java.util.concurrent.TimeUnit;

import desmoj.extensions.experimentation.ui.ExperimentStarterApplication;
import desmoj.extensions.experimentation.ui.GraphicalObserverContext;
import desmoj.extensions.experimentation.ui.HistogramPlotter;
import desmoj.extensions.experimentation.ui.TimeSeriesPlotter;
import desmoj.extensions.experimentation.util.AccessUtil;
import desmoj.extensions.experimentation.util.ExperimentRunner;
import desmoj.core.util.AccessPoint;
import desmoj.core.util.SimRunListener;


/**
 * @author Nicolas Knaak
 * @author Philip Joschko
 *
 * A demo experiment runner
 */
public class Runner extends ExperimentRunner {
	
	public Runner() {
		super();
	}
	
	public Runner(HamburguerModel m) {
		super(m);
	}
	
	public SimRunListener[] createSimRunListeners(GraphicalObserverContext c) {
		HamburguerModel model = (HamburguerModel)getModel();
		TimeSeriesPlotter tp1 = new TimeSeriesPlotter("Clients",c, model.clientsArrived, 360,360);
		tp1.addTimeSeries(model.clientsServiced);
		HistogramPlotter hp = new HistogramPlotter("Cleints Wait Times", c, model.waitTimeHistogram,"h", 360,360, 365,0);
		HistogramPlotter hp2 = new HistogramPlotter("Employee Wait Times", c, model.waitTimeEmployeeHistogram,"h", 360,360, 365,0);
                return new SimRunListener[] {tp1, hp,hp2};
	}
	
	public Map<String,AccessPoint> createParameters() {
		Map<String,AccessPoint> pm = super.createParameters();
		AccessUtil.setValue(pm, EXP_STOP_TIME, 1500.0);
		AccessUtil.setValue(pm, EXP_TRACE_STOP, 100.0);
		AccessUtil.setValue(pm, EXP_REF_UNIT, TimeUnit.MINUTES);
		return pm;
	}
	
	public static void main(String[] args) throws Exception {
		new ExperimentStarterApplication(HamburguerModel.class, Runner.class).setVisible(true);
	}
}
