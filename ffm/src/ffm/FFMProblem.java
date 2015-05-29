package ffm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * @author chenhuang
 *
 */
public class FFMProblem {
	//data : field_num:feature_num:value
	// max(feature_num) + 1
	public int n;
	// max(field_num) + 1
	public int m;
	public int l;
	// X[ [P[0], P[1]) ], length=nnz
	public FFMNode[] X;
	// length=l+1
	public int[] P;
	// Y[0], length=l
	public float[] Y;
	
	public static FFMProblem readFFMProblem(String path) throws IOException {
		FFMProblem problem = new FFMProblem();
		int l = 0, nnz = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "UTF-8"));
		String line = null;
		while((line = br.readLine()) != null) {
			l += 1;
			String[] fields = line.split(" |\t");
			for(int i=1; i<fields.length; i++) {
				nnz += 1;
			}
		}
		br.close();
		
		System.out.printf("reading %s, instance_num: %d, nnz: %d\n", path, l, nnz);
		
		problem.l = l;
		problem.X = new FFMNode[nnz];
		problem.Y = new float[l];
		problem.P = new int[l+1];
		problem.P[0] = 0;
		
		br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "UTF-8"));
		int p = 0;
		for(int i=0; (line=br.readLine())!=null; i++) {
			String[] fields = line.split(" |\t");
			problem.Y[i] = (Integer.parseInt(fields[0]) > 0)?1.f:-1.f;
			for(int j=1; j<fields.length; j++) {
				String[] subFields = fields[j].split(":");
				FFMNode node = new FFMNode();
				node.f = Integer.parseInt(subFields[0]);
				node.j = Integer.parseInt(subFields[1]);
				node.v = Float.parseFloat(subFields[2]);
				problem.X[p] = node;
				problem.m = Math.max(problem.m, node.f + 1);
				problem.n = Math.max(problem.n, node.j + 1);
				p++;
			}
			problem.P[i+1] = p;
		}
		br.close();
		
		return problem;
	}

	@Override
	public String toString() {
		return "FFMProblem [n=" + n + ", m=" + m + ", l=" + l + ", X="
				+ Arrays.toString(X) + ", P=" + Arrays.toString(P) + ", Y="
				+ Arrays.toString(Y) + "]";
	}
	
	public static void main(String[] args) throws IOException {
		FFMProblem problem = FFMProblem.readFFMProblem("aaa");
		System.out.println(problem);
	}
	
}
