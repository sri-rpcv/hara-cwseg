package org.hara.cwseg.run;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.hara.cwseg.constants.WordSegConstants;
import org.hara.cwseg.util.GenerateSyllables;
import org.hara.cwseg.util.SandhiSplitter;

public class RunCWSplitter {

	public static void main(String[] args) {
		try {
			long startTime = System.currentTimeMillis();
			GenerateSyllables gs = new GenerateSyllables();
//			gs.generateWords("data/200k-Chunked.ssf.clean");
			gs.loadVowelsFromFile(WordSegConstants.vowelsFile);
/*			gs.generateSyllables(WordSegConstants.baseWordsFile,
					WordSegConstants.baseWordsSylFile);
			gs.generateSyllables(WordSegConstants.compWordsFile,
					WordSegConstants.compWordsSylFile);
*/
/*			gs.generateLetters(WordSegConstants.baseWordsFile,
					WordSegConstants.baseWordsLetterFile);
			gs.generateLetters(WordSegConstants.compWordsFile,
					WordSegConstants.compWordsLetterFile);
*/
//			CWSplitter cws = new CWSplitter();
			SandhiSplitter cws = new SandhiSplitter();
			cws.setBwFile(WordSegConstants.baseWordsSylFile);
			cws.setCwFile(WordSegConstants.compWordsSylFile);
			cws.generateBWFromCW();

			long finalTime = System.currentTimeMillis();
			System.out.println();
			System.out.println("Total Time Taken :: " + (finalTime - startTime)/1000
					+ " sec");

		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
