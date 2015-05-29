package ffm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

public class FFMModel {
	// max(feature_num) + 1
	public int n;
	// max(field_num) + 1
	public int m;
	// latent factor dim
	public int k;
	// length = n * m * k * 2
	public float[] W;
	public boolean normalization;
	
	public FFMModel initModel(int n_, int m_, FFMParameter param) {
		n = n_;
		m = m_;
		k = param.k;
		normalization = param.normalization;
		W = new float[n * m * k * 2];
		
		float coef = (float) (0.5 / Math.sqrt(k));
		Random random = new Random();
		
		int position = 0;
		for (int j = 0; j < n; j++) {
			for(int f = 0; f < m; f++) {
				for(int d = 0; d < k; d++) {
					W[position] = coef * random.nextFloat();
					position += 1;
				}
				for(int d = this.k; d < 2*this.k; d++) {
					W[position] = 1.f;
					position += 1;
				}
			}
		}
			
		return this;
	}
	
	public void saveModel(String path) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(path)), "UTF-8"));
		bw.write("n " + n + "\n");
		bw.write("m " + m + "\n");
		bw.write("k " + k + "\n");
		bw.write("normalization " + normalization + "\n");
		int align0 = k * 2;
		int align1 = m * k * 2;
		for(int j=0; j<n; j++) {
			for(int f=0; f<m; f++) {
				bw.write("w" + j + "," + f + " ");
				for(int d = 0; d<k; d++) {
					bw.write(W[j*align1 + f*align0 + d] + " ");
				}
				bw.write("\n");
			}
		}
		bw.close();
	}
	
	public static FFMModel loadModel(String path) throws IOException {
		FFMModel model = new FFMModel();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "UTF-8"));
		model.n = Integer.parseInt(br.readLine().split(" ")[1]);
		model.m = Integer.parseInt(br.readLine().split(" ")[1]);
		model.k = Integer.parseInt(br.readLine().split(" ")[1]);
		model.normalization = Boolean.parseBoolean(br.readLine().split(" ")[1]);
		model.W = new float[model.n * model.m * model.k * 2];
		int align0 = model.k * 2;
		int align1 = model.m * model.k * 2;
		for(int j = 0; j < model.n; j++) {
			for(int f = 0; f < model.m; f++) {
				String line = br.readLine().trim();
				String[] fields = line.split(" ");
				for(int d = 0; d < model.k; d++) {
					model.W[j*align1 + f*align0 + d] = Float.parseFloat(fields[1+d]);
				}
			}
		}
		br.close();
		return model;
	}
	
	public static float[] normalize(FFMProblem problem, boolean normal) {
		float[] R = new float[problem.l];
		if(normal) {
			for(int i=0; i<problem.l; i++) {
				double norm = 0;
				for(int p=problem.P[i]; p<problem.P[i+1]; p++) {
					norm += problem.X[p].v * problem.X[p].v;
				}
				R[i] = (float) (1.f / norm);
			}
		} else {
			for(int i=0; i<problem.l; i++) {
				R[i] = 1.f;
			}
		}		
		return R;
	}
	
	public static int[] randomization(int l, boolean rand) {
		int[] order = new int[l];
		for (int i = 0; i < order.length; i++) {
			order[i] = i;
		}
		if(rand) {
			Random random = new Random();
			for(int i = order.length; i > 1; i--) {
				int tmp = order[i-1];
				int index = random.nextInt(i);
				order[i-1] = order[index];
				order[index] = tmp;
			}
		}
		return order;
	}
	
	public static float wTx(FFMProblem prob, int i, float r, FFMModel model,
			float kappa, float eta, float lambda, boolean do_update) {
		// kappa = -y * exp(-y*t) / (1+exp(-y*t))
		int start = prob.P[i];
		int end = prob.P[i+1];
		float t = 0.f;
		int align0 = model.k * 2;
		int align1 = model.m * model.k * 2;
		
		for(int N1 = start; N1 < end; N1++) {
			int j1 = prob.X[N1].j;
			int f1 = prob.X[N1].f;
			float v1 = prob.X[N1].v;
			if(j1 >= model.n || f1 >= model.m) continue;
			
			for(int N2 = N1+1; N2 < end; N2++) {
				int j2 = prob.X[N2].j;
				int f2 = prob.X[N2].f;
				float v2 = prob.X[N2].v;
				if(j2 >= model.n || f2 >= model.m) continue;
				
				int w1_index = j1 * align1 + f2 * align0;
				int w2_index = j2 * align1 + f1 * align0;
				float v = 2.f * v1 * v2 * r;
				
				if(do_update) {
					int wg1_index = w1_index + model.k;
					int wg2_index = w2_index + model.k;
					float kappav = kappa * v;
					for(int d = 0; d < model.k; d++) {
						float g1 = lambda * model.W[w1_index+d] + kappav * model.W[w2_index+d];
						float g2 = lambda * model.W[w2_index+d] + kappav * model.W[w1_index+d];
						
						float wg1 = model.W[wg1_index+d] + g1 * g1;
						float wg2 = model.W[wg2_index+d] + g2 * g2;
						
						model.W[w1_index+d] = model.W[w1_index+d] - eta / (float)(Math.sqrt(wg1)) * g1;
						model.W[w2_index+d] = model.W[w2_index+d] - eta / (float)(Math.sqrt(wg2)) * g2;
						
						model.W[wg1_index+d] = wg1;
						model.W[wg2_index+d] = wg2;
					}
				} else {
					for(int d = 0; d < model.k; d++) {
						t += model.W[w1_index + d] * model.W[w2_index + d] * v;
					}
				}
			}
		}	
		return t;
	}
	
	public static FFMModel train(FFMProblem tr, FFMProblem va, FFMParameter param) {
		FFMModel model = new FFMModel();
		model.initModel(tr.n, tr.m, param);
		
		float[] R_tr = normalize(tr, param.normalization);
		float[] R_va = null;	
		if(va != null) {
			R_va = normalize(va, param.normalization);
		}
			
		for(int iter = 0; iter < param.n_iters; iter++) {
			double tr_loss = 0.;
			int[] order = randomization(tr.l, param.random);
			for(int ii=0; ii<tr.l; ii++) {
				int i = order[ii];
				float y = tr.Y[i];
				float r = R_tr[i];
				float t = wTx(tr, i, r, model, 0.f, 0.f, 0.f, false);
				float expnyt = (float) Math.exp(-y * t);
				tr_loss += Math.log(1 + expnyt);
				float kappa = -y * expnyt / (1+expnyt);
				
				// System.out.printf("i:%3d, y:%.1f, t:%.3f, expynt:%.3f, kappa:%.3f\n", i, y, t, expnyt, kappa);
				
				wTx(tr, i, r, model, kappa, param.eta, param.lambda, true);
			}
			tr_loss /= tr.l;
			System.out.printf("iter: %2d, tr_loss: %.5f", iter+1, tr_loss);
			
			if(va != null && va.l != 0) {
				double va_loss = 0.;
				for(int i=0; i<va.l; i++) {
					float y = va.Y[i];
					float r = R_va[i];
					float t = wTx(va, i, r, model, 0.f, 0.f, 0.f, false);
					float expnyt = (float) Math.exp(-y * t);
					va_loss += Math.log(1 + expnyt);
				}
				va_loss /= va.l;
				System.out.printf(", va_loss: %.5f", va_loss);
			}
			
			System.out.println();
		}
		
		return model;
	}
	
	public static void test(FFMModel model, FFMProblem va, FFMParameter param,
			int testBufferSize, int printInterval) {
		float[] R_va = normalize(va, param.normalization);
		LogLossEvalutor evalutor = new LogLossEvalutor(testBufferSize);
		double total_loss = 0.0;
		for(int i=0; i<va.l; i++) {
			float y = va.Y[i];
			float r = R_va[i];
			float t = wTx(va, i, r, model, 0.f, 0.f, 0.f, false);
			double expnyt = Math.exp(-y * t);
			double loss = Math.log(1 + expnyt);
			total_loss += loss;
			evalutor.addLogLoss(loss);
			if((i+1) % printInterval == 0) {
				System.out.printf("%d, %f\n", (i+1)/printInterval, evalutor.getAverageLogLoss());
			}
		}
		System.out.printf("%f\n", total_loss/va.l);
	}
	
	public static void main(String[] args) throws IOException {
		if(args.length != 8) {
			System.out.println("java -jar ffm.jar <eta> <lambda> <n_iters> "
					+ "<k> <normal> <random> <train_file> <va_file>");
			System.out.println("for example:\n"
					+ "java -jar ffm.jar 0.1 0.01 15 4 true false tr_ va_");
		}
		
		FFMProblem tr = FFMProblem.readFFMProblem(args[6]);
		FFMProblem va = FFMProblem.readFFMProblem(args[7]);
		
		FFMParameter param = FFMParameter.defaultParameter();
		param.eta = Float.parseFloat(args[0]);
		param.lambda = Float.parseFloat(args[1]);
		param.n_iters = Integer.parseInt(args[2]);
		param.k = Integer.parseInt(args[3]);
		param.normalization = Boolean.parseBoolean(args[4]);
		param.random = Boolean.parseBoolean(args[5]);
		
		FFMModel.train(tr, va, param);
	}	
}
