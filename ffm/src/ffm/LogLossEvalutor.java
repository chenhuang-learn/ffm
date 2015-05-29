package ffm;

public class LogLossEvalutor {
	
	private int testDataSize;
	private double[] logloss;
	private int position;
	private double totalLoss;
	private boolean enoughData;
	
	public LogLossEvalutor(int testDataSize) {
		this.testDataSize = testDataSize;
		logloss = new double[testDataSize];
		position = 0;
		totalLoss = 0.0;
	}
	
	public void addLogLoss(double loss) {
		totalLoss = totalLoss + loss - logloss[position];
		logloss[position] = loss;
		position += 1;
		if(position >= testDataSize) {
			position = 0;
			enoughData = true;
		}
	}
	
	public double getAverageLogLoss() {
		if(enoughData) {
			return totalLoss / testDataSize;
		} else {
			return totalLoss / position;
		}
	}
	
	/** prob: p(y=1|x;w), y: 1 or 0(-1) */
	public static double calLogLoss(double prob, double y) {
		double p = Math.max(Math.min(prob,  1-1e-15), 1e-15);
		return y == 1.? -Math.log(p) : -Math.log(1. - p);
	}
	
	public static void main(String[] args) {
		LogLossEvalutor evalutor = new LogLossEvalutor(4);
		double[] losses = {3, 2, 1, 0.7, 0.5, 0.2};
		for(int i=0; i<losses.length; i++) {
			evalutor.addLogLoss(losses[i]);
			System.out.println(evalutor.getAverageLogLoss());
		}
	}
	
}
