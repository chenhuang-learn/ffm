package ffm;

/**
 * @author chenhuang
 *
 */
public class FFMParameter {
	// eta used for per-coordinate learning rate
	public float eta;
	// used for l2-regularization
	public float lambda;
	// max iterations
	public int n_iters;
	// latent factor dim
	public int k;
	// instance-wise normalization
	public boolean normalization;
	// randomization training order of samples
	public boolean random;
	
	public static FFMParameter defaultParameter() {
		FFMParameter parameter = new FFMParameter();
		parameter.eta = 0.1f;
		parameter.lambda = 0;
		parameter.n_iters = 15;
		parameter.k = 4;
		parameter.normalization = true;
		parameter.random = true;
		return parameter;
	}
	
	@Override
	public String toString() {
		return "FFMParameter [eta=" + eta + ", lambda=" + lambda + ", n_iters="
				+ n_iters + ", k=" + k + ", normalization=" + normalization
				+ ", random=" + random + "]";
	}	
}
