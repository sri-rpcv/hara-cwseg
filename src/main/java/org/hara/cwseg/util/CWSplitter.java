package org.hara.cwseg.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.hara.cwseg.constants.WordSegConstants;
import org.hara.cwseg.tree.Node;
import org.hara.cwseg.tree.WordTree;

public class CWSplitter {

	private WordTree baseWordsTree;
	private HashMap<String, String> bw2bwSylls;
	private ArrayList<String> bwSylTD;
	private ArrayList<String> bwSylBU;
	private String cwSyl;
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

	public void setBwFile(String bwFile) {
		this.bwFile = bwFile;
	}

	public void setCwFile(String cwFile) {
		this.cwFile = cwFile;
	}

	public CWSplitter() {
		baseWordsTree = new WordTree();
		bw2bwSylls = new HashMap<String, String>();
		bwSylBU = new ArrayList<String>();
		bwSylTD = new ArrayList<String>();
		sylProbs = new HashMap<String, Double>();
		tdPossibleNodes = new ArrayList<Node>();
		buPossibleNodes = new ArrayList<Node>();
		tdPossibleIndexes = new ArrayList<Integer>();
		buPossibleIndexes = new ArrayList<Integer>();
		cwSyl = null;
		bwFile = null;
		cwFile = null;
		nodeBUValue = null;
		nodeTDValue = null;
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
			bw2bwSylls.put(wordSyl[1], wordSyl[0]);
			baseWordsTree.insertTopDown(wordSyl[1]);
			baseWordsTree.insertBottomUp(wordSyl[1]);
			line = br.readLine();
		}
		br.close();
	}

	private void traverseBWTree() throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(cwFile));
		String line = br.readLine();
		while (line != null) {
			if (line.trim() != "") {
				clearLists();
				System.out
						.println("#############################################");
				System.out.println("CW :: " + line);
				String[] wordSyl = line.split("=");
				String sylls[] = wordSyl[1]
						.split(WordSegConstants.splitDelimeter);
				checkCWInTDTree(sylls);
				checkCWInBUTree(sylls);
				printPossibles(sylls.length);
				System.out.println("\nScoring Methods::\n");
				scoreSyllables();
				System.out.println();
				System.out
						.println("#############################################");
				System.out.println();
			}
			line = br.readLine();
		}
		br.close();
	}

	private void clearLists() {
		bwSylBU.clear();
		bwSylTD.clear();
		tdPossibleIndexes.clear();
		tdPossibleNodes.clear();
		buPossibleIndexes.clear();
		buPossibleNodes.clear();
	}

	private void printPossibles(int length) {
		System.out.println("Posssible Outcomes ::");
		for (int i = 0; i < tdPossibleIndexes.size(); i++) {
			Integer tdInteger = tdPossibleIndexes.get(i);
			if (buPossibleIndexes.contains((length - tdInteger) - 1)) {
				System.out.println("td="
						+ tdInteger
						+ " bu="
						+ buPossibleIndexes.get(buPossibleIndexes
								.indexOf(length - tdInteger - 1)));
				System.out.println("td="+tdPossibleNodes.get(i).valueToRoot);
				System.out.println("bu="+buPossibleNodes.get(buPossibleIndexes
						.indexOf(length - tdInteger - 1)).valueToRoot);
			}
		}
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
		if (!found) {
			cwSyl = sylls[i];
			tdIndex = i;
			System.out.println();
			System.out.println("matched value=" + nodeTopDown.valueToRoot);
			System.out.println("Possible BW's Top Down::");
			if (bw2bwSylls.containsKey(nodeTopDown.valueToRoot)) {
				nodeTDValue = bw2bwSylls.get(nodeTopDown.valueToRoot);
			}
			String possbStr = null;
			for (int j = 0; j < nodeTopDown.children.size(); j++) {
				possbStr = nodeTopDown.children.get(j).valueToRoot;
				System.out.println("value = "
						+ nodeTopDown.children.get(j).value);
				if (bw2bwSylls.containsKey(possbStr)) {
					System.out.println(bw2bwSylls.get(possbStr));
					bwSylTD.add(nodeTopDown.children.get(j).value);
				}
			}
		} else {
			System.out.println("not found td = " + nodeTopDown.valueToRoot);
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
		if (!found) {
			buIndex = i;
			System.out.println();
			System.out.println("matched value=" + nodeBottomUp.valueToRoot);
			System.out.println("Possible BW's Bottom Up::");
			String possbStr = null;
			String temp = "";
			for (int k = i + 1; k < sylls.length; k++) {
				temp += sylls[k];
				if (k < sylls.length - 1) {
					temp += WordSegConstants.addDelimeter;
				}
			}
			if (bw2bwSylls.containsKey(temp)) {
				nodeBUValue = bw2bwSylls.get(temp);
			}
			for (int j = 0; j < nodeBottomUp.children.size(); j++) {
				possbStr = "";
				possbStr += nodeBottomUp.children.get(j).value;
				possbStr += WordSegConstants.addDelimeter;
				possbStr += temp;
				if (bw2bwSylls.containsKey(possbStr)) {
					System.out.println(bw2bwSylls.get(possbStr));
					bwSylBU.add(nodeBottomUp.children.get(j).value);
				}
			}
		} else {
			System.out.println("not found bu = " + nodeBottomUp.valueToRoot);
		}
	}

	/**
	 * Scoring functions::
	 */
	private void scoreSyllables() {
		try {
			System.out.println("td index = " + tdIndex);
			System.out.println("bu index = " + buIndex);
			if (tdIndex > buIndex) {
				System.out.println(nodeTDValue);
				System.out.println(nodeBUValue);
			} else {
				loadSylProbs(WordSegConstants.sylProbsFile);
			}
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		for (int i = 0; i < bwSylTD.size(); i++) {
			scoreByMatch(bwSylTD.get(i), cwSyl);
		}
		for (int i = 0; i < bwSylBU.size(); i++) {
			scoreByMatch(bwSylBU.get(i), cwSyl);
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

	private void scoreByMatch(String bwSyl, String cwSyl) {
		System.out.println("bwSyl = " + bwSyl + " cwSYl = " + cwSyl);
		System.out.println("probs:: bw = " + sylProbs.get(bwSyl) + " cw = "
				+ sylProbs.get(cwSyl));
		int noCharsMatched = 0;
		int size = Math.min(bwSyl.length(), cwSyl.length());
		for (int i = 0; i < size; i++) {
			if (bwSyl.charAt(i) == cwSyl.charAt(i)) {
				noCharsMatched++;
			}
		}
		System.out.println("score = " + WordSegConstants.sylProb
				* noCharsMatched);
	}

	/*
	 * public static void main(String[] args) { try { CWSplitter cws = new
	 * CWSplitter(); cws.setBwFile(WordSegConstants.baseWordsSylFileSample);
	 * cws.setCwFile(WordSegConstants.compWordsSylFile); cws.generateBWFromCW();
	 * } catch (FileNotFoundException fnfe) { fnfe.printStackTrace(); } catch
	 * (IOException ioe) { ioe.printStackTrace(); } }
	 */
}
