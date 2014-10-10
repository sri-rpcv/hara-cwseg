package org.hara.cwseg.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.hara.cwseg.constants.WordSegConstants;
import org.hara.cwseg.tree.Node;
import org.hara.cwseg.tree.WordTree;

@SuppressWarnings( { "unused" })
public class SandhiSplitter {

	private WordTree baseWordsTree;
	private HashMap<String, String> bwSylls2bw;
	private HashMap<String, String> bw2bwSylls;
	private TreeMap<Double, String> individualWords;
	private String bwFile;
	private String cwFile;
	private HashMap<String, Double> sylProbs;
	private int tdIndex;
	private int buIndex;
	private String nodeTDValue;
	private String nodeBUValue;
	private ArrayList<Node> tdPossibleNodes;
	private ArrayList<Integer> tdPossibleIndexes;
	private ArrayList<Node> buPossibleNodes;
	private ArrayList<Integer> buPossibleIndexes;
	private ArrayList<String> tdPossbValues;
	private ArrayList<String> buPossbValues;
	private ArrayList<Double> tdPossbProbs;
	private ArrayList<Double> buPossbProbs;
	private PrintWriter pw;

	public void setBwFile(String bwFile) {
		this.bwFile = bwFile;
	}

	public void setCwFile(String cwFile) {
		this.cwFile = cwFile;
	}

	public SandhiSplitter() {
		baseWordsTree = new WordTree();
		bwSylls2bw = new HashMap<String, String>();
		bw2bwSylls = new HashMap<String, String>();
		individualWords = new TreeMap<Double, String>();
		sylProbs = new HashMap<String, Double>();
		tdPossibleNodes = new ArrayList<Node>();
		buPossibleNodes = new ArrayList<Node>();
		tdPossibleIndexes = new ArrayList<Integer>();
		buPossibleIndexes = new ArrayList<Integer>();
		tdPossbValues = new ArrayList<String>();
		buPossbValues = new ArrayList<String>();
		tdPossbProbs = new ArrayList<Double>();
		buPossbProbs = new ArrayList<Double>();
		bwFile = null;
		cwFile = null;
		nodeBUValue = null;
		nodeTDValue = null;
		pw = null;
	}

	public void generateBWFromCW() throws FileNotFoundException, IOException {
		populateBWTree();
		traverseBWTree();
	}

	private void populateBWTree() throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(bwFile));
		String line = br.readLine();
		while (line != null) {
			String[] wordSyl = line.split("=");
			bwSylls2bw.put(wordSyl[1], wordSyl[0]);
			bw2bwSylls.put(wordSyl[0], wordSyl[1]);
			baseWordsTree.insertTopDown(wordSyl[1]);
			baseWordsTree.insertBottomUp(wordSyl[1]);
			line = br.readLine();
		}
		br.close();
	}

	private void traverseBWTree() throws FileNotFoundException, IOException {
		pw = new PrintWriter(WordSegConstants.outFile);
		BufferedReader br = new BufferedReader(new FileReader(cwFile));
		String line = br.readLine();
		while (line != null) {
			if (line.trim() != "") {
				clearLists();
				pw.write("#############################################\n");
				pw.write("CW :: " + line + "\n");
				String[] wordSyl = line.split("=");
				String sylls[] = wordSyl[1]
						.split(WordSegConstants.splitDelimeter);
				checkCWInTDTree(sylls);
				checkCWInBUTree(sylls);

				computePossibles(sylls);
				prunePossibles(sylls);
				pw.write("\nScoring Methods::\n");
				scoreMethods(sylls);
				pw.write("\n");
				pw.write("#############################################\n");
				pw.write("\n");
			}
			line = br.readLine();
		}
		br.close();
		pw.close();
	}

	private void prunePossibles(String[] sylls) {
		for (int i = 0; i < tdPossbValues.size(); i++) {
			String sylStr = bw2bwSylls.get(tdPossbValues.get(i));
			String[] split = sylStr.split(WordSegConstants.splitDelimeter);
			int len = split.length;
			if (sylls[len - 1].compareTo(split[len - 1]) != 0) {
				String cwStr = nonVowelPartOf(sylls[len - 1]);
				String bwStr = nonVowelPartOf(split[len - 1]);
				if (cwStr.compareTo(bwStr) != 0) {
					tdPossbValues.remove(i);
					i--;
				}
			}
		}
		ArrayList<Integer> possbLen = new ArrayList<Integer>();
		for (int i = 0; i < tdPossbValues.size(); i++) {
			String sylStr = bw2bwSylls.get(tdPossbValues.get(i));
			String[] split = sylStr.split(WordSegConstants.splitDelimeter);
			if (!possbLen.contains(split.length)) {
				possbLen.add(split.length);
			}
			Double noMatch = scoreByMatch(split[split.length - 1],
					sylls[split.length - 1]);
			tdPossbProbs.add(noMatch);
		}

		for (int i = 0; i < buPossbValues.size(); i++) {
			String sylStr = bw2bwSylls.get(buPossbValues.get(i));
			String[] split = sylStr.split(WordSegConstants.splitDelimeter);
			if (!possbLen.contains(sylls.length - split.length + 1)
					&& !possbLen.contains(sylls.length - split.length)) {
				buPossbValues.remove(i);
				i--;
			}
		}

		for (int i = 0; i < buPossbValues.size(); i++) {
			String sylStr = bw2bwSylls.get(buPossbValues.get(i));
			String[] split = sylStr.split(WordSegConstants.splitDelimeter);
			Double noMatch = scoreByMatch(split[0], sylls[sylls.length
					- split.length]);
			buPossbProbs.add(noMatch);
		}

		removeDuplicates();
	}

	private void removeDuplicates() {
		while (true) {
			boolean found = false;
			for (int i = 0; i < tdPossbValues.size(); i++) {
				int index = 0;
				index = tdPossbValues.lastIndexOf(tdPossbValues.get(i));
				if (index != i) {
					tdPossbValues.remove(index);
					tdPossbProbs.remove(index);
					found = true;
					i--;
				}
			}
			if (!found) {
				break;
			}
		}
		while (true) {
			boolean found = false;
			for (int i = 0; i < buPossbValues.size(); i++) {
				int index = 0;
				index = buPossbValues.lastIndexOf(buPossbValues.get(i));
				if (index != i) {
					buPossbValues.remove(index);
					buPossbProbs.remove(index);
					found = true;
					i--;
				}
			}
			if (!found) {
				break;
			}
		}
	}

	private String nonVowelPartOf(String str) {
		String retStr = "";
		for (int i = 0; i < str.length(); i++) {
			if (GenerateSyllables.vowels.contains(Character.toString(str
					.charAt(i)))) {
				break;
			}
			retStr += Character.toString(str.charAt(i));
		}
		return retStr;
	}

	private void clearLists() {
		tdPossibleIndexes.clear();
		buPossibleIndexes.clear();
		tdPossibleNodes.clear();
		buPossibleNodes.clear();
		tdPossbValues.clear();
		buPossbValues.clear();
		individualWords.clear();
		tdPossbProbs.clear();
		buPossbProbs.clear();
	}

	private void computePossibles(String[] sylls) {
		pw.write("Posssible Outcomes ::\n");
		for (int i = 0; i < tdPossibleIndexes.size(); i++) {
			tdIndex = tdPossibleIndexes.get(i);
			/*
			 * This is to handle cases like appatinuMci => appati + nuMci
			 */
			if (buPossibleIndexes.contains(tdIndex + 1)) {
				buIndex = buPossibleIndexes.indexOf(tdIndex + 1);
			} else {
				buIndex = -1;
			}
			if (buIndex != -1) {
				String tdValueToRoot = tdPossibleNodes.get(tdIndex).valueToRoot;
				String buValueToRoot = buPossibleNodes.get(buIndex).valueToRoot;
				buValueToRoot = reverseSyllables(buValueToRoot);
				if (noOfSyllables(tdValueToRoot) + noOfSyllables(buValueToRoot) == sylls.length) {
					if (bwSylls2bw.containsKey(tdValueToRoot)
							&& bwSylls2bw.containsKey(buValueToRoot)) {
						tdPossbValues.add(bwSylls2bw.get(tdValueToRoot));
						buPossbValues.add(bwSylls2bw.get(buValueToRoot));
					}
				}
			}
			/*
			 * This is to handle other cases
			 */
			if (buPossibleIndexes.contains(tdIndex + 2)) {
				buIndex = buPossibleIndexes.indexOf(tdIndex + 2);
			} else {
				buIndex = -1;
			}
			if (buIndex != -1) {
				Node tdNode = tdPossibleNodes.get(tdIndex);
				Node buNode = buPossibleNodes.get(buIndex);
				for (int j = 0; j < tdNode.children.size(); j++) {
					String tdValueToRoot = tdNode.children.get(j).valueToRoot;
					if (bwSylls2bw.containsKey(tdValueToRoot)) {
						tdPossbValues.add(bwSylls2bw.get(tdValueToRoot));
					}
				}
				for (int j = 0; j < buNode.children.size(); j++) {
					String buValueToRoot = buNode.children.get(j).valueToRoot;
					buValueToRoot = reverseSyllables(buValueToRoot);
					if (bwSylls2bw.containsKey(buValueToRoot)) {
						buPossbValues.add(bwSylls2bw.get(buValueToRoot));
					}
				}
			}
		}
	}

	private void printPossibles() {
		if (tdPossbValues.size() > 0 && buPossbValues.size() > 0) {
			for (int i = 0; i < tdPossbValues.size(); i++) {
				pw.write("td possb = " + tdPossbValues.get(i) + "\n");
			}
			for (int i = 0; i < buPossbValues.size(); i++) {
				pw.write("bu possb = " + buPossbValues.get(i) + "\n");
			}
		} else {
			pw.write("No base words present\n");
		}
	}

	private int noOfSyllables(String value) {
		return value.split(WordSegConstants.splitDelimeter).length;
	}

	private String reverseSyllables(String buValueToRoot) {
		String[] split = buValueToRoot.split(WordSegConstants.splitDelimeter);
		String revStr = "";
		for (int i = split.length - 1; i >= 0; i--) {
			revStr += split[i];
			if (i > 0) {
				revStr += WordSegConstants.addDelimeter;
			}
		}
		return revStr;
	}

	private void checkCWInTDTree(String[] sylls) {
		Node nodeTopDown = baseWordsTree.getRootTopDown();
		boolean found = false;
		int i = -1;
		for (i = 0; i < sylls.length; i++) {
			found = false;
			for (int j = 0; j < nodeTopDown.children.size(); j++) {
				if (nodeTopDown.children.get(j).value.compareTo(sylls[i]) == 0) {
					tdPossibleNodes.add(nodeTopDown.children.get(j));
					tdPossibleIndexes.add(i);
					nodeTopDown = nodeTopDown.children.get(j);
					found = true;
					break;
				}
			}
			if (!found) {
				break;
			}
		}
	}

	private void checkCWInBUTree(String[] sylls) {
		Node nodeBottomUp = baseWordsTree.getRootBottomUp();
		boolean found = false;
		int i = -1;
		for (i = sylls.length - 1; i >= 0; i--) {
			found = false;
			for (int j = 0; j < nodeBottomUp.children.size(); j++) {
				if (nodeBottomUp.children.get(j).value.compareTo(sylls[i]) == 0) {
					buPossibleNodes.add(nodeBottomUp.children.get(j));
					buPossibleIndexes.add(i);
					nodeBottomUp = nodeBottomUp.children.get(j);
					found = true;
					break;
				}
			}
			if (!found) {
				break;
			}
		}
	}

	/**
	 * Scoring functions::
	 */
	private void scoreMethods(String[] sylls) {
		try {
			loadSylProbs(WordSegConstants.sylProbsFile);
			computeJointProbs(sylls);
			printOutcomes();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void printOutcomes() {
/*		Set<Entry<Double, String>> entrySet = individualWords.entrySet();
		Iterator<Entry<Double, String>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Entry<Double, String> entry = iterator.next();
			pw.write(entry.getKey().toString());
			pw.write(" => ");
			pw.write(entry.getValue().toString());
			pw.write("\n");
		}*/
		pw.write("Only Top 10\n\n");
		NavigableMap<Double,String> descendingMap = individualWords.descendingMap();
		Set<Entry<Double,String>> descEntrySet = descendingMap.entrySet();
		Iterator<Entry<Double, String>> descIterator = descEntrySet.iterator();
		int count = 0;
		while(descIterator.hasNext()) {
			Entry<Double, String> entry = descIterator.next();
			pw.write(entry.getKey().toString());
			pw.write(" => ");
			pw.write(entry.getValue().toString());
			pw.write("\n");
			count++;
			if (count > WordSegConstants.topOutcomes) {
				break;
			}
		}
	}

	private void computeJointProbs(String[] sylls) {
		int sylProbsSize = sylProbs.size();
		for (int i = 0; i < tdPossbValues.size(); i++) {
			for (int j = 0; j < buPossbValues.size(); j++) {
				String tdStr = bw2bwSylls.get(tdPossbValues.get(i));
				String[] tdSylls = tdStr.split(WordSegConstants.splitDelimeter);
				String buStr = bw2bwSylls.get(buPossbValues.get(j));
				String[] buSylls = buStr.split(WordSegConstants.splitDelimeter);
				Double jointProb = 0.0;
				jointProb = sylProbs.get(tdSylls[tdSylls.length - 1])
				// * Math.log10(sylProbs.get(tdSylls[tdSylls.length - 1]))
						* sylProbs.get(buSylls[0]);
				// * Math.log10(sylProbs.get(buSylls[0]));
//				jointProb = sylProbs.get(sylls[tdSylls.length - 1])
//						* sylProbs.get(tdSylls[tdSylls.length - 1]);
//				jointProb += sylProbs.get(sylls[tdSylls.length - 1])
//						* sylProbs.get(buSylls[0]);
				double d = tdPossbProbs.get(i) / sylProbsSize;
				double e = buPossbProbs.get(j) / sylProbsSize;
				jointProb += d + e;
				jointProb += sylProbs.get(sylls[tdSylls.length - 1]);
				if (individualWords.containsKey(jointProb))
					jointProb += Math.pow(10, -20);
				individualWords.put(jointProb, tdPossbValues.get(i) + "$"
						+ buPossbValues.get(j));
			}
		}
	}

	private void loadSylProbs(String sylProbsFile)
			throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(sylProbsFile));
		String line = null;
		line = br.readLine();
		while (line != null) {
			if (line.trim() != "") {
				String[] probs = line.split("=");
				sylProbs.put(probs[0], Double.valueOf(probs[1]));
			}
			line = br.readLine();
		}
		br.close();
	}

	private Double scoreByMatch(String bwSyl, String cwSyl) {
		Double noCharsMatched = 0.0;
		int size = Math.min(bwSyl.length(), cwSyl.length());
		for (int i = 0; i < size; i++) {
			if (bwSyl.charAt(i) == cwSyl.charAt(i)) {
				noCharsMatched++;
			}
		}
		return WordSegConstants.sylProb * noCharsMatched;
	}

}
