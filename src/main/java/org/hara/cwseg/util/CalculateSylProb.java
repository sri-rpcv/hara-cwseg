package org.hara.cwseg.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.hara.cwseg.constants.WordSegConstants;

public class CalculateSylProb {

	private HashMap<String, Double> sylProb = null;

	public CalculateSylProb() {
		sylProb = new HashMap<String, Double>();
	}

	public void calSylProbs(String fileName) throws FileNotFoundException,
			IOException {
		int count = 0;
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = br.readLine();
		while (line != null) {
			if (line.trim() != "") {
				String[] wordSyl = line.split("=");
				String sylls[] = wordSyl[1]
						.split(WordSegConstants.splitDelimeter);
				for (int i = 0; i < sylls.length; i++) {
					if (sylProb.containsKey(sylls[i])) {
						Double intValue = sylProb.get(sylls[i]).doubleValue();
						intValue++;
						sylProb.put(sylls[i], intValue);
					} else {
						sylProb.put(sylls[i], 1.0);
					}
					count++;
				}
			}
			line = br.readLine();
		}
		br.close();
		
		System.out.println("count=" + count);
		Set<Entry<String,Double>> entrySet = sylProb.entrySet();
		Iterator<Entry<String, Double>> iterator = entrySet.iterator();
		while(iterator.hasNext()) {
			Entry<String, Double> entry = iterator.next();
			System.out.println(entry.getValue());
			entry.setValue(entry.getValue()/count);
		}
	}

	private void printSylProbs(String fileName) throws FileNotFoundException,
			IOException {
		PrintWriter pw = new PrintWriter(fileName);
		Set<Entry<String,Double>> entrySet = sylProb.entrySet();
		Iterator<Entry<String, Double>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Entry<String, Double> entry = iterator.next();
			pw.write(entry.getKey() + "=" + entry.getValue().doubleValue());
			pw.write("\n");
		}
		pw.close();
	}

	public static void main(String[] args) {
		CalculateSylProb csp = new CalculateSylProb();
		try {
			csp.calSylProbs(WordSegConstants.baseWordsSylFile);
			csp.printSylProbs(WordSegConstants.sylProbsFile);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
