package search;
import java.util.*;
import java.io.*;

//import java.util.Map.Entry;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 * @professor Sesh Venugopal
 * @student Neil Bhavsar
 * 
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			//System.out.println("Noise Word= " + word);//make sure to delete this
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
		
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException {
		Scanner sc = new Scanner(new File(docFile));
		HashMap<String,Occurrence> returnMap = new HashMap<String,Occurrence>();
		while (sc.hasNext()) {
			String word = getKeyWord(sc.next());
			//System.out.println("Post Get KeyWord within loadkeyWords= " + word);
			if(word == null){
				continue;
			} else if(returnMap.containsKey(word)){
				returnMap.get(word).frequency +=1;
			} else {
				Occurrence putOccur = new Occurrence(docFile,1);
				//System.out.println("About to put " + word + "  into returnMap");
				returnMap.put(word,putOccur);
				}
			}
		return returnMap;
		}
		
	
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		//keywordsIndex is a hashMap with String and Occurrenece
		//loop through kws and see if that word is contained in keywords index
		//if it is not contained put it in the front i guess?
		//HashMap<String,ArrayList<Occurrence>> keywordsIndex;
		if(kws == null){
			return;
		}
		Iterator<Map.Entry<String,Occurrence>> it = kws.entrySet().iterator();
		while(it.hasNext()){
			HashMap.Entry<String, Occurrence> temp = it.next();
			String key = temp.getKey();
			Occurrence value = temp.getValue();
			String docFile = value.document;
			//int freq = value.frequency;
			if(keywordsIndex.containsKey(key)){
				//contains key string
				//also check if kws is already in the hashMap if it is return by checking docFile
				//insertLastOccurrence(ArrayList<Occurrence> occs)
				ArrayList<Occurrence> tempList = keywordsIndex.get(key);
				if(tempList.contains(docFile)){
					return;
				}
				tempList.add(value);
				insertLastOccurrence(tempList);
			} else {
				//does not contain key string
				ArrayList<Occurrence> tempList = new ArrayList<Occurrence>();
				tempList.add(value);
				keywordsIndex.put(key,tempList);
			}
		}
		return;
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		word = word.toLowerCase().trim();
		int len = word.length()-1;
		//running a reverse for loop to start at the back and see if any of these are true
		//one time it might cause an error is if the word was hello,hello, 
		//edit: I think I fixed it
		for(int i = len; i > 0; i--){
			if(Character.toString(word.charAt(i)).matches("[a-z]")){
				break;
			}
			if(word.charAt(i) == '.'||word.charAt(i) == ','||word.charAt(i) == '?'||word.charAt(i) == '!'
				||word.charAt(i) == ';'||word.charAt(i) == ':'){
				//System.out.println("Going through getKeyWord for loop " + word + " at " + i);
				word = word.substring(0, i);
			}
			}
		
		
		if(!word.matches("[a-z]+")){
			return null;
		} else {
			if(noiseWords.containsKey(word)){
				return null;
			}else{
				return word;
			}
		}
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search, 
	 * then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	private ArrayList<Integer> binarySearch(ArrayList<Occurrence> occs, ArrayList<Integer> returnList){
		Occurrence lastOccurrence = occs.get(occs.size()-1);
		int freq = lastOccurrence.frequency;
		int low = 0;
		int high = occs.size()-2;
		while(low<high){
			int middle = (low + high)/2;
			int midminus1 = occs.get(middle+1).frequency;
			int midfreq = occs.get(middle).frequency;
			returnList.add(middle);
			//if midfreq is greater than freq but freq is greater index after it since this is decsending arraylist
			if(midfreq > freq && midminus1 < freq ){
				Occurrence temp = lastOccurrence;
				occs.remove(lastOccurrence);
				occs.add(middle ,temp);
				return returnList;
			}
			if(midfreq == freq){
				Occurrence temp = lastOccurrence;
				occs.remove(lastOccurrence);
				occs.add(middle,temp);
				return returnList;
			}else if(midfreq > freq){
				//had to reverse since decsending order
				low = middle + 1;
			} else {
				high = middle - 1;
			}
		}
		//high will end up being the last firstIdx when binarySearch stops at the value at position 0
		if(high == 0){
			Occurrence temp = lastOccurrence;
			occs.remove(lastOccurrence);
			occs.add(0,temp);	
		}
		//dont have to do anything if none of this happens since it is already in the correct order
		return returnList;
		
	}
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		if(occs == null || occs.size() == 1){
			return null;
		}
		ArrayList<Integer> returnList = new ArrayList<Integer>();
		if(occs.size() == 2){
			//if arraylist isn't big enough for binary search
			//last input
			//Occurrence lastOcc = occs.get(occs.size() -1);
			Occurrence firstOcc = occs.get(0);
			Occurrence secondOcc = occs.get(1);
			//int lastFreq = lastOcc.frequency;
			int firstFreq = firstOcc.frequency;
			int secondFreq = secondOcc.frequency;
//			if(lastFreq > firstFreq){
//				Occurrence temp = lastOcc;
//				occs.remove(lastOcc);
//				occs.add(0,temp);
//			}else if(lastFreq > secondFreq){
//				Occurrence temp = lastOcc;
//				occs.remove(lastOcc);
//				occs.add(1,temp);
//			}	
			if(firstFreq > secondFreq){
				return null;
			} else {
				Occurrence temp = firstOcc;
				occs.remove(firstOcc);
				occs.add(temp);
				return null;
			}
		}
			return returnList = binarySearch(occs, returnList);
		
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	private ArrayList<Occurrence> compareTops(ArrayList<Occurrence>key1,ArrayList<Occurrence> key2){
		ArrayList<Occurrence> tops = new ArrayList<Occurrence>();
		for(int i = 0; i < key1.size(); i ++){
			//getting object of type occurrence which contains docfile and freq
			tops.add(key1.get(i));
		}
		//parse through key2 arraylist
		for(int j = 0; j < key2.size(); j ++){
			int key2freq = key2.get(j).frequency;
			//parse through tops arraylist
			for(int z = 0; z < tops.size(); z++ ){
				int	topsfreq = tops.get(z).frequency;
				if(key2freq > topsfreq){
					tops.add(z,key2.get(j));
					break;
				}else if(key2freq == topsfreq){
					//add after key1 entry will this work if there is nothing at index z + 1?
					tops.add(z+1,key2.get(j));
					break;
				}else{
					//add to end because key2 is less than everything so far
					if(z == tops.size()-1){
					tops.add(key2.get(j));
					break;
					}
				}
			}
		}
		return tops;
	}
	private ArrayList<String> findTop(ArrayList<Occurrence> key1, ArrayList<Occurrence> key2){
		ArrayList<String> returnList = new ArrayList<String>();
		if(key1 == null){
			for(int i = 0; i < key2.size() || i < 5; i ++){
				returnList.add(key2.get(i).document);
			}
			return returnList;
		}else if(key2 == null){
			for(int i = 0; i < key1.size() || i < 5; i ++){
				returnList.add(key1.get(i).document);
			}
			return returnList;
		}else {
			ArrayList<Occurrence> tops = compareTops(key1,key2);
			//add first 5 documents in list
			for(int j = 0; j < tops.size() && returnList.size() < 5; j ++){
				if(!returnList.contains(tops.get(j).document)){
				returnList.add(tops.get(j).document);
				}else{
					continue;
				}
			}
		}
		return returnList;
		
	}
	public ArrayList<String> top5search(String kw1, String kw2) {
		// COMPLETE THIS METHOD
		// THE FOLLOWING LINE HAS BEEN ADDED TO MAKE THE METHOD COMPILE
		//if kw1 and kw2 is null or if the index is empty or if the index doesn't contain either of the keywords
		kw1 = kw1.toLowerCase();
		kw2 = kw2.toLowerCase();
		if(kw1 == null && kw2 == null || keywordsIndex.isEmpty() || 
		(!keywordsIndex.containsKey(kw1) && !keywordsIndex.containsKey(kw2))){
			return null;
		}
		//getkw1ArrayList if not null
		//getkw2ArrayList if not null
		ArrayList<Occurrence> key1 = null;
		ArrayList<Occurrence> key2 = null;
		if(keywordsIndex.containsKey(kw1)){
			key1 = keywordsIndex.get(kw1);
		}
		if(keywordsIndex.containsKey(kw2)){
			key2 = keywordsIndex.get(kw2);
		}
//calls findTop which calls compareTops
		ArrayList<String> top5 = new ArrayList<String>(5);
		top5 = findTop(key1,key2);
		if(top5.isEmpty()){
			return null;
		}else{
			return top5;
		}
	
		
	}
	/*public static void main(String[] args) throws FileNotFoundException{
		String docFile = "/Users/neilbhavsar/Documents/workspace/Little Search Engine/docs.txt";
		String noiseWords = "/Users/neilbhavsar/Documents/workspace/Little Search Engine/noisewords.txt";
		LittleSearchEngine lse = new LittleSearchEngine();
		lse.makeIndex(docFile, noiseWords);
		for (String key : lse.keywordsIndex.keySet()) {
		    System.out.println(key + " " + lse.keywordsIndex.get(key));
		}
	ArrayList<String> top5 = lse.top5search("creeping", "saw");
	for(String a: top5){
		System.out.println("The doc file is within top5search " + a);
	}
	}*/
}