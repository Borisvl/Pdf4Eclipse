/*******************************************************************************
 * Copyright (c) 2011 Boris von Loesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Boris von Loesch - initial API and implementation
 *     Andreas Turban   - Added interface ISynctexParser
 ******************************************************************************/
package de.vonloesch.synctex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;

/**
 * A simple parser for SyncTeX files. Allows forward and reverse search.
 * More information about SyncTeX and the official C-implementation can be found
 * here: <a href="http://itexmac.sourceforge.net/SyncTeX.html" />
 * 
 * This implementation is not related to the official one and will in general
 * produce different results than the official implementation.
 * 
 * @author Boris von Loesch
 *
 */
class SimpleSynctexParser implements ISynctexParser {
	public static final int SEARCH_FORWARD = 1;
	public static final int SEARCH_REVERSE = 2;
	
	//Use the information of the surrounding vbox.
	static final boolean CONSIDERVBOX = true;

	private BufferedReader in;
	
	int magnification;
	int xoffset, yoffset, unit;
	
	Map<Integer, String> fileMap;
		
	//Forward search information
	private String sourceFilePath;
	private int sourceLineNr;

	//helper (forward search)
	private int smallerLineNr;
	private Stack<VBoxData> vboxes = new Stack<VBoxData>();
	private boolean hasRelativeFileNames;
	
	//Reverse search information
	int pdfPage;
	int pdfX, pdfY;
	
	//Additional information from forward search
	int pdfX2, pdfY2;
		
	//helper (reverse search)
	private double dist;
		
	SimpleSynctexParser() {
		magnification = 1000;
		xoffset = 0;
		yoffset = 0;
		unit = 1;
		hasRelativeFileNames = false;
		fileMap = new TreeMap<Integer, String>();
	}
	
	/**
	 * Creates a new instance of SyncTeX parser
	 * @param r
	 */
	public SimpleSynctexParser(BufferedReader r) {
		this();
		in = r;
		resetBox();
		smallerLineNr = 0;
		dist = Double.MAX_VALUE;
	}
	
	
	
	/**
     * {@inheritDoc}
     */
    @Override
    public void setEclipseProject(IProject eclipseProject) {
        
    }

    /**
	 * Closes the SimpleSynctexParser and releases any
	 * system resources  associated with it.
	 * Once the stream has been closed, further 
	 * startForward or startReverse
	 * invocations will throw an IOException.
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (in != null) in.close();
	}
	
	private void resetBox() {
		pdfX = Integer.MAX_VALUE;
		pdfY = Integer.MAX_VALUE;
		pdfX2 = 0; pdfY2 = 0;		
	}
	
	public void setForwardSearchInformation(String name, int line) {
		sourceLineNr = line;
		try {
			this.sourceFilePath = (new File(name)).getCanonicalPath();
		} catch (IOException e) {
			//should not happen
			this.sourceFilePath = name;
		}
	}
	
	public void setReverseSearchInformation(int page, double posX, double posY) {
		pdfPage = page;
		pdfX = fromPDFCoord(posX);
		pdfY = fromPDFCoord(posY);
	}
	
	/**
	 * 
	 * @param t Tuple
	 * @return
	 */
	private boolean isForwardPosition(int[] t){
		//First check the line number
		if (t[1] > sourceLineNr) return false;
		
		String fname = fileMap.get(t[0]);
		if (sourceFilePath.equals(fname)) return true;

		//fname can be relative
		if (hasRelativeFileNames && sourceFilePath.toLowerCase().endsWith(fname.toLowerCase())) {
			return true;
		}

		return false;
	}
	
	private void updatePosition(int page, int[] pos, int sourceLine) {
		if (sourceLine <= sourceLineNr && sourceLine >= smallerLineNr) {
			if (sourceLine != smallerLineNr) {
				//We found a line that is nearer to our forwardSearchPosition
				pdfPage = page;
				resetBox();
				smallerLineNr = sourceLine;
			}
			if (page != pdfPage) {
				//Two possibilities here: Either we use the box on the first page where the entry
				//appears, or the last page

				//return;
				resetBox();
				pdfPage = page;
			}

			if (pos[0] < pdfX) pdfX = pos[0];
			if (pos[1] < pdfY) pdfY = pos[1];
			if (pos[0] > pdfX2) pdfX2 = pos[0];
			if (pos[1] > pdfY2) pdfY2 = pos[1];		
		}
	}
	
	/**
	 * Converts the position in the synctex file to a position in the pdf page.
	 * @param p
	 * @return
	 */
	double toPDFCoord (int p) {
		return (unit*p)/65781.76*(1000./magnification);
	}
	
	int fromPDFCoord (double p) {
		return (int) Math.round(p*65781.76/unit*(magnification/1000.));
	}
	
	/**
	 * 
	 * @return null, if no position was found, else
	 * [0] = page, [1-4] coordinates of rectangle
	 */
	public double[] getForwardSearchResult() {
		if (pdfPage == 0) {
			return null;
		}
		double[] result = new double[5];
		result[0] = pdfPage;
		result[1] = toPDFCoord(pdfX);
		result[2] = toPDFCoord(pdfY);
		result[3] = toPDFCoord(pdfX2);
		result[4] = toPDFCoord(pdfY2);
		return result;
	}
	
	/**
	 * Starts a forward search. If the given linenumber was not found in the
	 * synctex file, return the information for the largest line number that is 
	 * smaller than the given one.
	 * @throws IOException
	 */
	public void startForward() throws IOException {
		String line;
		parsePreamble();
		int pageLevel = 0;
		int currentPage = 0;
		while ((line=in.readLine()) != null) {
			char first = line.charAt(0);
			if (pageLevel > 0){
				if (first == '{') {
					pageLevel++;
					currentPage = getColonArgInt(line, 0);
					//System.out.println(currentPage);
				}
				else if (first == '}') {
					pageLevel--;
					assert pageLevel >= 0;
				}
				else if (first == '[') {
					//Start HBox
					int[] t = getColonArgTuple(line, 0);
					if (isForwardPosition(t)) {
						//Just take the largest box
						int[] pos = getColonArgTuple(line, nextPos);
						int[] size = getColonArgTriple(line, nextPos);
						int[] npos = new int[2];
						npos[0] = pos[0];
						npos[1] = pos[1] - size[1];
						updatePosition(currentPage, npos, t[1]);
						npos[0] = pos[0] + size[0];
						npos[1] = pos[1] + size[2];
						updatePosition(currentPage, npos, t[1]);
					}
				}
				else if (first == ']') {
					//End HBox
				}
				else if (first == '(') {
					//Start VBox
					int[] t = getColonArgTuple(line, 0);
					int[] pos = getColonArgTuple(line, nextPos);
					int[] size = getColonArgTriple(line, nextPos);
					VBoxData v = new VBoxData(pos, size);
					vboxes.push(v);
					if (isForwardPosition(t)) {
						//Just take the largest box
						int[] npos = new int[2];
						npos[0] = pos[0];
						npos[1] = pos[1] - size[1];
						updatePosition(currentPage, npos, t[1]);
						npos[0] = pos[0] + size[0];
						npos[1] = pos[1] + size[2];
						updatePosition(currentPage, npos, t[1]);
					}
				}
				else if (first == ')') {
					//End VBox
					vboxes.pop();
				}
				else if (first == 'x' || first == 'k' || first == '$' 
						|| first == 'v' || first == 'h' || first == 'g'){
					int[] t = getColonArgTuple(line, 0);
					if (isForwardPosition(t)) {
						int[] pos = getColonArgTuple(line, nextPos);
						updatePosition(currentPage, pos, t[1]);
						if (CONSIDERVBOX && vboxes.size() > 0) {
							//Also use the data of the surrounding VBOX
							VBoxData v = vboxes.peek();
							int[] npos = new int[2];
							npos[0] = v.pos[0];
							npos[1] = v.pos[1] - v.size[1];
							updatePosition(currentPage, npos, t[1]);
							npos[0] = v.pos[0] + v.size[0];
							npos[1] = v.pos[1] + v.size[2];
							updatePosition(currentPage, npos, t[1]);
						}
					}
				}
			}
			else if (pageLevel == 0) {
				assert (vboxes.size() == 0);
				if (first == '{') {
					currentPage = getColonArgInt(line, 0);

					//Optimization: Do not parse the page, if the source file was not found
					if ((!hasRelativeFileNames) && !fileMap.containsValue(sourceFilePath)) continue;
					
					pageLevel++;
					//We already have a position, so break it
					if (pdfPage != 0 && pdfPage < currentPage - 2) return;
					//System.out.println(currentPage);
				}
				else if (first == 'I' && line.startsWith("Input")) {
					parseInputLine(line);
				}				
			}
		}
	}
	
	private void checkCoord(int t[], int[] pos) {
		double cx = 1.*pos[0];
		double cy = 1.*pos[1];
		double d = Math.sqrt((cx-pdfX)*(cx-pdfX) + (cy-pdfY)*(cy-pdfY));
		if (d < dist) {
			sourceFilePath = fileMap.get(t[0]);
			sourceLineNr = t[1];
			dist = d;
		}
	}
	
	private void checkBox(int[] t, int[] pos, int[] size) {
		//Check the four corners
		int[] npos = new int[2];
		npos[0] = pos[0];
		npos[1] = pos[1] - size[2];
		checkCoord(t, npos);

		npos[0] = pos[0];
		npos[1] = pos[1] + size[1];
		checkCoord(t, npos);

		npos[0] = pos[0] + size[0];
		npos[1] = pos[1] - size[2];
		checkCoord(t, npos);

		npos[0] = pos[0] + size[0];
		npos[1] = pos[1] + size[1];
		checkCoord(t, npos);
	}
	
	public void startReverse() throws IOException {
		String line;
		parsePreamble();
		int pageLevel = 0;
		int currentPage = 0;
		while ((line=in.readLine()) != null) {
			char first = line.charAt(0);
			if (pageLevel > 0){
				if (first == '{') {
					pageLevel++;
					currentPage = getColonArgInt(line, 0);
					//System.out.println(currentPage);
				}
				else if (first == '}') {
					pageLevel--;
					assert pageLevel >= 0;
				}
				else if (first == '[' || first == '(') {
					//Start box
					int[] t = getColonArgTuple(line, 0);
					int[] pos = getColonArgTuple(line, nextPos);
					int[] size = getColonArgTriple(line, nextPos);
					checkBox(t, pos, size);
				}
				else if (first == ']') {
					//End HBox
				}
				else if (first == ')') {
					//End VBox
				}
				else if (first == 'x' || first == 'k' || first == '$' 
						|| first == 'v' || first == 'h' || first == 'g'){
					int[] t = getColonArgTuple(line, 0);
					int[] pos = getColonArgTuple(line, nextPos);
					checkCoord(t, pos);
				}
			}
			else if (pageLevel == 0) {
				assert (vboxes.size() == 0);
				if (first == '{') {
					currentPage = getColonArgInt(line, 0);

					//Do not consider the page, if its not the page in the pdf
					if (currentPage < pdfPage) continue;
					if (currentPage > pdfPage) return;
					
					pageLevel++;
					//System.out.println(currentPage);
				}
				else if (first == 'I' && line.startsWith("Input")) {
					parseInputLine(line);
				}				
			}
		}
	}
	
	void parsePreamble() throws IOException {
		//The header starts with 
		String line = in.readLine();
		assert line.startsWith("SyncTeX Version");
		
		while ((line=in.readLine()) != null) {
			if (line.startsWith("Input")) parseInputLine(line);
			else if (line.startsWith("Magnification")) {
				magnification = getColonArgInt(line, 13);
			}
			else if (line.startsWith("X Offset")) {
				xoffset = getColonArgInt(line, 8);
			}
			else if (line.startsWith("Y Offset")) {
				yoffset = getColonArgInt(line, 8);
			}
			else if (line.startsWith("Unit")) {
				unit = getColonArgInt(line, 4);
			}
			else if (line.startsWith("Content")) {
				//The main content starts
				return;
			}
		}
		//Error we should have stopped earlier
		assert false;
	}
	
	void parseInputLine(String line) {
		assert (line.startsWith("Input"));
		int fileNr = getColonArgInt(line, 5);
		String fileName = line.substring(nextPos+1);
		try {
			File f = new File(fileName);
			if (f.isAbsolute()) {
				fileName = (new File(fileName)).getCanonicalPath();				
			}
			else {
				hasRelativeFileNames = true;
			}
		} catch (IOException e) {
			//Should not happen
		}
		fileMap.put(fileNr, fileName);
	}
	
	int[] getColonArgTriple(String line, int pos) {
		String t = getColonArg(line, pos);
		//Find first comma
		final int firstC = t.indexOf(',');
		assert (firstC > 0);
		int[] tuple= new int[3];
		tuple[0] = Integer.parseInt(t.substring(0, firstC));
		final int secondC = t.indexOf(',', firstC+1);
		tuple[1] = Integer.parseInt(t.substring(firstC+1, secondC));
		tuple[2] = Integer.parseInt(t.substring(secondC+1));
		return tuple;
	}

	int[] getColonArgTuple(String line, int pos) {
		String t = getColonArg(line, pos);
		//Find first comma
		final int firstC = t.indexOf(',');
		assert (firstC > 0);
		int[] tuple= new int[2];
		tuple[0] = Integer.parseInt(t.substring(0, firstC));
		tuple[1] = Integer.parseInt(t.substring(firstC+1));
		return tuple;
	}

	int getColonArgInt(String line, int pos) {
		return Integer.parseInt(getColonArg(line, pos));
	}

	//Saves the next colon position in "getColonArg"
	int nextPos;

	String getColonArg(String line, int pos) {
		int endPos = line.indexOf(':', pos + 1);
		if (endPos < 0) endPos = line.length();
		nextPos = endPos;
		return line.substring(pos + 1, endPos);
	}

	@Override
	public String getSourceFilePath() {
	return sourceFilePath;
	}

	@Override
	public int getSourceLineNr() {
		return sourceLineNr;
	}
}
