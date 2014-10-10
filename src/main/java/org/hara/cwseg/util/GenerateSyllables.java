package org.hara.cwseg.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.hara.cwseg.constants.WordSegConstants;

public class GenerateSyllables {

	public static ArrayList<String> vowels = null;

	public GenerateSyllables() {
		vowels = new ArrayList<String>();
	}

	public void generateWords(String fileName) throws FileNotFoundException,
			IOException {
		PrintWriter pw = new PrintWriter(WordSegConstants.baseWordsFile);
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = br.readLine();
		while (line != null) {
			String[] fields = line.split("\t");
			if (fields.length == 3) {
				if (Character.isLetter(fields[1].charAt(0))) {
					pw.write(fields[1]);
					pw.write("\n");
				}
			}
			line = br.readLine();
		}
		br.close();
		pw.close();
	}

	public void generateSyllables(String inFile, String outFile)
			throws FileNotFoundException, IOException {
		PrintWriter pw = new PrintWriter(outFile);
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		String line = br.readLine();
		while (line != null) {
			if (line.trim() != "") {
				int noChars = line.length();
				String curStr = "";
				String sylStr = "";
				for (int i = 0; i < noChars;) {
					boolean found = false;
					curStr = Character.toString(line.charAt(i));
					while (vowels.contains(curStr)) {
						// System.out.println(curStr);
						i++;
						found = true;
						if (i >= noChars) {
							break;
						}
						curStr += Character.toString(line.charAt(i));
					}
					if (!found || i >= noChars) {
						sylStr += curStr;
						i++;
					} else {
						sylStr += curStr.substring(0, curStr.length() - 1);
						sylStr += WordSegConstants.addDelimeter;
					}
				}
				pw.write(line);
				pw.write("=");
				pw.write(sylStr);
				pw.write("\n");
			}
			line = br.readLine();
		}
		br.close();
		pw.close();
	}

	public void generateLetters(String inFile, String outFile)
			throws FileNotFoundException, IOException {
		PrintWriter pw = new PrintWriter(outFile);
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		String line = br.readLine();
		while (line != null) {
			if (line.trim() != "") {
				int noChars = line.length();
				String sylStr = "";
				for (int i = 0; i < noChars; i++) {
					sylStr += Character.toString(line.charAt(i));
					if (i < noChars - 1) {
						sylStr += WordSegConstants.addDelimeter;
					}
				}
				pw.write(line);
				pw.write("=");
				pw.write(sylStr);
				pw.write("\n");
			}
			line = br.readLine();
		}
		br.close();
		pw.close();
	}

	public void loadVowelsFromFile(String vowelsFile)
			throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(vowelsFile));
		String line = br.readLine();
		while (line != null) {
			vowels.add(line);
			line = br.readLine();
		}
		br.close();
	}

	public static void main(String[] args) {
		try {
			GenerateSyllables gs = new GenerateSyllables();
			// gs.generateWords("data/200k-Chunked.ssf.clean");
			gs.loadVowelsFromFile(WordSegConstants.vowelsFile);
			gs.generateSyllables(WordSegConstants.baseWordsFile,
					WordSegConstants.baseWordsSylFile);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
