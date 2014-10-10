package org.hara.cwseg.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.hara.cwseg.constants.WordSegConstants;

public class FileFilter {

	public FileFilter() {
	}

	public void removeStringFromFile(String removeStr, String fileName)
			throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		ArrayList<String> words = new ArrayList<String>();
		String line = br.readLine();
		while (line != null) {
			if (line.trim() != "" && (line.compareToIgnoreCase(removeStr)!= 0)) {
				words.add(line);
			}
			line = br.readLine();
		}
		br.close();
		
		/*
		 * Write back those filtered words to the same file.
		 */
		
		PrintWriter pw = new PrintWriter(fileName);
		int size = words.size();
		for (int i = 0; i < size; i++) {
			pw.write(words.get(i));
			pw.write("\n");
		}
		pw.close();
	}

	public static void main(String[] args) {
		try {
			FileFilter ff = new FileFilter();
			ff.removeStringFromFile("koeko+e+VM",
					WordSegConstants.baseWordsSylFileSample);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
