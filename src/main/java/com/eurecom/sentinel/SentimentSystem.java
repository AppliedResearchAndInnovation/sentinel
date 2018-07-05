package com.eurecom.sentinel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.core.stemmers.SnowballStemmer;
import weka.core.tokenizers.NGramTokenizer;
import cmu.arktweetnlp.Tagger.TaggedToken;
import cmu.arktweetnlp.impl.features.WordClusterPaths;

/**
 * Provides functions to get features
 * 
 * @author SentiNEL, Webis
 * Code is base on Webis
 * SentiNEL modify getNGrams method
 * SentiNEL add filterNGrams method
 * SentiNEL modify getCharNgrams method
 * SentiNEL add loadSentiWordNet method
 * SentiNEL add loadWord2Vec method
 * SentiNEL add getWord2VecScores method
 */
public class SentimentSystem {

	protected Set<Tweet> tweetList;
	private boolean debug = false;

	/**
	 * Constructor gets all Tweets in a list.
	 *
	 * @param tweetList the list with all Tweets.
	 */
	public SentimentSystem(Set<Tweet> tweetList) {
		this.tweetList = tweetList;
	}

	//helper functions to preprocess and get features

	/**
	 * Gets all NGrams that occur in the Target term
	 *
	 * @param tweet the Tweet to analyze
	 * @param from NGram range from
	 * @param to NGram range to
	 * @return returns all NGrams that occur in the Tweet
	 */
	protected Set<String> getNGrams(Tweet tweet, int from, int to) {
		Set<String> nGramList = new HashSet<String>();
		String tokenString = "";
		for (TaggedToken token : tweet.getWordList()){
			tokenString = tokenString + token.token + " ";
		}
		System.out.println(tokenString);
		NGramTokenizer tokenizer = new NGramTokenizer();
		tokenizer.setNGramMaxSize(to);
		tokenizer.setNGramMinSize(from);
		tokenizer.setDelimiters(" ");
		tokenizer.tokenize(tokenString);
		while(tokenizer.hasMoreElements()){
			nGramList.add((String) tokenizer.nextElement());
			if (debug) {
				System.out.println((String) tokenizer.nextElement());
			}
		}
		tweet.setNGrams(nGramList);

		// to return targetNGrams
		Set<String> nGramListFiltered = filterNGram(tweet, nGramList);
		tweet.setNGramsTarget(nGramListFiltered);

		System.out.println(nGramListFiltered);
		return nGramListFiltered;
	}

	/**
	 * filter N-gram for the Target Term
	 * ps: not perfect, to be improved
	 *
	 * @param tweet
	 * @param nGramSet
	 */
	private Set<String> filterNGram(Tweet tweet, Set<String> nGramSet) {
		// TODO Auto-generated method stub
		String target = tweet.getTargetContent();
		Set<String> nGramSetFiltered = new HashSet<String>();
		String[] targetWords = target.split(" "); // e.g "c", "d"

		for (String s : nGramSet) {
			String[] ngramWords = s.split(" "); // ["a", "b"], ["b", "c"], ["c", "d"], ["d", "e"]
			for (String tw : targetWords) {
				if (find(ngramWords, tw)) {
					nGramSetFiltered.add(s);
					//System.out.println(s);
				}
			}
		}

		return nGramSetFiltered;
	}

	/**
	 * find value in a string array
	 * @param arr
	 * @param targetValue
	 * @return
	 */
	private boolean find(String[] arr, String targetValue) {
		for (String s: arr) {
			if (s.equals(targetValue)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets all NGrams that occur in the Target Term
	 *
	 * @param tweet the Tweet to analyze
	 * @param n NGram range to, from is set to 1
	 * @return returns all NGrams that occur in the Tweet
	 */
	protected Set<String> getNGrams(Tweet tweet, int n) {
		return getNGrams(tweet, 1, n);
	}

	/**
	 * Gets all Char NGrams that occur in the Target term, from 3-Gram to 5-Gram
	 * @param tweet the Tweet to analyze
	 * @return returns all NGrams that occur in the Tweet
	 */

	protected Set<String> getCharNGrams(Tweet tweet){
		String target = tweet.getTargetContent();
		Set<String> nGramList = new HashSet<String>();
		for (int i = 0; i < target.length() - 2; i++){
			nGramList.add(target.substring(i, i + 3));
			if (i + 4 <= target.length()) nGramList.add(target.substring(i, i + 4));
			if (i + 5 <= target.length()) nGramList.add(target.substring(i, i + 5));
		}
		tweet.setCharNGramList(nGramList); // normally use tweet.setCharNGramListTarget(nGramList); here. For the ease to not change the codes in SentimentSystemSentinel.java
		return nGramList;
	}

	/**
	 *  Determine the Cluster IDs for words in the Tweet 
	 *
	 * @param tweet the Tweet to analyze
	 * @return returns a set of Cluster IDs
	 */
	protected Set<String> getClusters(Tweet tweet) {
		Set<String> clusterList = new HashSet<String>();
		for (TaggedToken token : tweet.getWordList()){
			String cluster = WordClusterPaths.wordToPath.get(token.token);
			if (cluster != null){
				clusterList.add(cluster);
			}
		}
		tweet.setClusters(clusterList);
		return clusterList;
	}

	/**
	 * Determine all emoticons in the Tweet
	 *
	 * @param tweet the Tweet to analyze
	 * @return returns a set of emoticons
	 */
	protected Set<String> getEmoticons(Tweet tweet){
		Set<String> emoticons = new HashSet<String>();
		String emoticon_string = "(?:[<>]?[:;=8][\\-o\\*\\']?[\\)\\]\\(\\[dDpP/\\:\\}\\{@\\|\\\\]|[\\)\\]\\(\\[dDpP/\\:\\}\\{@\\|\\\\] [\\-o\\*\\']?[:;=8][<>]?)";
		Matcher m = Pattern.compile(emoticon_string).matcher(tweet.getTargetContent());
		while (m.find()){
			emoticons.add(tweet.getTargetContent().substring(m.start(), m.end()));
			if(m.end() == tweet.getTargetContent().length()) tweet.setLastEmoticon(true);
		}
		tweet.setEmoticons(emoticons);
		return emoticons;
	}

	/**
	 * Negates the Tweet, all Word in the negation range get the suffix "_NEG", and the negation count get  increased
	 *
	 * @param tweet the Tweet to analyze
	 */
	protected void negate(Tweet tweet) {
		int negationCount = 0;
		boolean neg = false;
		for (TaggedToken token : tweet.getTargetWordList()){
			if(neg){
				if(token.token.matches("^[.:;!?]$")){
					neg = false;
					negationCount++;
				}
				else{
					token.token = token.token + "_NEG";
				}
			}
			if(token.token.toLowerCase().matches("^(?:never|no|nothing|nowhere|noone|none|not|havent|hasnt|hadnt|cant|couldnt|shouldnt|wont|wouldnt|dont|doesnt|didnt|isnt|arent|aint)|.*n't")){
				neg = true;
			}

		}
		if (neg) negationCount++;
		tweet.setNegationCount(negationCount);
	}


	/**
	 * Determine the stems for words in the Tweet
	 *
	 * @param tweet the Tweet to analyze
	 * @return returns a set of stems
	 */
	protected Set<String> getStems(Tweet tweet) {
		SnowballStemmer stemmer = new SnowballStemmer("english");
		Set<String> stemList = new HashSet<String>();
		for (TaggedToken token : tweet.getWordList()){
			stemList.add(stemmer.stem(token.token));
		}
		tweet.setStemList(stemList);
		return stemList;
	}

	/**
	 * Loads and parses a sentiment lexica
	 *
	 * @param path the path to the lexica
	 * @return returns a map with words and there sentiment
	 * @throws FileNotFoundException
	 */
	protected Map<String, Double> loadLexicon(String path) throws FileNotFoundException{
		Map<String, Double> lexiMap = new HashMap<String, Double>();
		File file = new File("resources/lexi/" + path + ".txt");
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split("\t");
			if (line.length == 4){
				lexiMap.put(line[0], Double.parseDouble(line[1]));
			}
		}
		scanner.close();
		return lexiMap;
	}


	/**
	 * Loads and parses the MPQA sentiment lexica
	 *
	 * @return returns a map with words and there sentiment
	 * @throws FileNotFoundException
	 */
	protected Map<String, Double> loadMPQA() throws FileNotFoundException{
		Map<String, Double> lexiMap = new HashMap<String, Double>();
		File file = new File("resources/lexi/subjclueslen1-HLTEMNLP05.tff");
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split(" ");
			if (line.length == 6){
				String word = line[2].replaceFirst("word1=", "");
				Double val = 0.0;
				if(line[5].replaceFirst("priorpolarity=", "").equals("positive")){
					val = 1.0;
				}
				else{
					if(line[5].replaceFirst("priorpolarity=", "").equals("negative")){
						val = -1.0;
					}
				}
				if(line[0].replaceFirst("type=", "").equals("strongsubj")){
					val = val * 5;
				}
				lexiMap.put(word, val);
			}
		}
		scanner.close();
		return lexiMap;
	}

	/**
	 *  Loads and parses the BingLiu sentiment lexica
	 *
	 * @return returns a map with words and there sentiment
	 * @throws FileNotFoundException
	 */
	protected Map<String, Double> loadBingLiu() throws FileNotFoundException{
		Map<String, Double> lexiMap = new HashMap<String, Double>();
		File file = new File("resources/lexi/bingliu/positive-words.txt");
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			lexiMap.put(scanner.nextLine(), 1.0);
		}
		scanner.close();
		File file2 = new File("resources/lexi/bingliu/negative-words.txt");
		Scanner scanner2 = new Scanner(file2);
		while (scanner2.hasNextLine()) {
			lexiMap.put(scanner2.nextLine(), -1.0);
		}
		scanner2.close();
		return lexiMap;
	}

	/**
	 * Loads and parses the NRC sentiment lexica
	 *
	 * @return returns a map with words and there sentiment
	 * @throws FileNotFoundException
	 */
	protected Map<String, Double> loadNRC() throws FileNotFoundException{
		Map<String, Double> lexiMap = new HashMap<String, Double>();
		File file = new File("resources/lexi/NRC-emotion-lexicon-wordlevel-alphabetized-v0.92.txt");
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split("\t");
			if (line.length == 3){
				if (line[1].equals("negative") && line[2].equals("1")){
					lexiMap.put(line[0], -1.0);
				}
				else{
					if (line[1].equals("positive") && line[2].equals("1")){
						lexiMap.put(line[0], 1.0);
					}
				}
			}
		}
		scanner.close();
		return lexiMap;
	}

	/**
	 * Loads and parses the SentiWordNet sentiment lexica
	 *
	 * @return returns a map with words and there sentiment
	 * @throws FileNotFoundException
	 */
	protected Map<String, Double> loadSentiWordNet() throws FileNotFoundException{
		Map<String, Double> sentiWordMap = new HashMap<String, Double>();
		File file = new File("resources/lexi/SentiWordNet_3.0.0.txt");
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split("\t");
			if (line.length == 6){
				for (String word : line[4].split(" ")){
					sentiWordMap.put(word.split("#")[0] + "+", Double.valueOf(line[2]));
					sentiWordMap.put(word.split("#")[0] + "-", Double.valueOf(line[3]));
				}
			}
		}
		scanner.close();
		return sentiWordMap;
	}

	/**
	 * Loads and parses the AFFINN sentiment lexica
	 *
	 * @return returns a map with words and there sentiment
	 * @throws FileNotFoundException
	 */
	protected Map<String, Double> loadAFINN() throws FileNotFoundException{
		Map<String, Double> afinnMap = new HashMap<String, Double>();
		File file = new File("resources/lexi/AFINN-111.txt");
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split("\t");
			if (line.length == 2){
				afinnMap.put(line[0], Double.valueOf(line[1]));
			}
		}
		scanner.close();
		return afinnMap;
	}

	/**
	 * Loads and parses the GeneralInquirer sentiment lexica
	 *
	 * @return returns a map with words and there sentiment
	 * @throws FileNotFoundException
	 */
	protected Map<String, Double> loadGeneralInquirer() throws FileNotFoundException{
		Map<String, Double> inquirernMap = new HashMap<String, Double>();
		File file = new File("resources/lexi/inquirerbasicttabsclean");
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split("\t");
			if (line.length == 186){
				if (line[3].equals("Negativ")){
					inquirernMap.put(line[0].toLowerCase(), -1.0);
				}
				else{
					if (line[2].equals("Positiv")){
						inquirernMap.put(line[0].toLowerCase(), 1.0);
					}
				}
			}
		}
		scanner.close();
		return inquirernMap;
	}

	/**
	 * Calculates the lexica score for the lexica features from unigrams
	 *
	 * @param lexi a map with all the lexica scores
	 * @param wordList a list of words that occur in the Tweet
	 * @param neg get positive or negative scores
	 * @return returns a list with all lexica features
	 */
	protected List<Double> getLexiScores(Map<String,Double> lexi, List<TaggedToken> wordList, boolean neg) {
		// need to add min score and count of non-negative score
		double totalCount = 0.0;
		double totalScore = 0.0;
		double maxScore = 0.0;
		double lastScore = 0.0;
		for (TaggedToken token : wordList){
			Double score = lexi.get(token.token);
			if(score != null){
				if ((neg && score < 0) || (!neg && score > 0)){
					totalCount++;
					totalScore = totalScore + score;
					if (neg){
						if(score < maxScore) maxScore = score;

					}else{
						if(score > maxScore) maxScore = score;
					}
					lastScore = score;
				}
			}
		}
		List<Double> scoreList = new ArrayList<Double>();
		scoreList.add(totalCount);
		scoreList.add(totalScore);
		scoreList.add(maxScore);
		scoreList.add(lastScore);
		return scoreList;
	}

	/**
	 * Calculates the lexica score for the lexica features from bigrams
	 *
	 * @param lexi a map with all the lexica scores
	 * @param wordList a list of words that occur in the Tweet
	 * @param neg get positive or negative scores
	 * @return returns a list with all lexica features
	 */
	protected List<Double> getLexiScoresBi(Map<String,Double> lexi, Set<String> wordList, boolean neg) {
		// need to add min score and count of non-negative score
		double totalCount = 0.0;
		double totalScore = 0.0;
		double maxScore = 0.0;
		double lastScore = 0.0;
		for (String token : wordList){
			Double score;
			score = lexi.get(token);
			if(score != null){
				if ((neg && score < 0) || (!neg && score > 0)){
					totalCount++;
					totalScore = totalScore + score;
					if (neg){
						if(score < maxScore) maxScore = score;

					}else{
						if(score > maxScore) maxScore = score;
					}
					lastScore = score;
				}
			}
		}
		List<Double> scoreList = new ArrayList<Double>();
		scoreList.add(totalCount);
		scoreList.add(totalScore);
		scoreList.add(maxScore);
		scoreList.add(lastScore);
		return scoreList;
	}

	/**
	 * Load the word2vec 
	 * @param path
	 * @return token and its vector
	 * @throws FileNotFoundException
	 */
	protected Map<String, ArrayList<Double>> loadWord2Vec(String path) throws FileNotFoundException{
		Map<String, ArrayList<Double>> lexiMap = new HashMap<String, ArrayList<Double>>();
		File file = new File("resources/word2vec/" + path + ".txt");
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split(" ");
			ArrayList<Double> scores = new ArrayList<Double>();
			for(int i = 1; i < line.length; i++) {
				scores.add(Double.parseDouble(line[i]));
			}
				lexiMap.put(line[0], scores);
		}
		scanner.close();
		return lexiMap;
	}

	protected List<Double> getWord2VecScores(Map<String, ArrayList<Double>> word2vec, List<TaggedToken> wordList) {
		// need to add min score and count of non-negative score
		double maxScore = 0.0;
		double avgScore = 0.0;
		double minScore = 0.0;
		double totalScore = 0.0;
		int count = 0;
		for (TaggedToken token : wordList){
			ArrayList<Double> scores = word2vec.get(token.token);
			if (scores != null) {
				for (Double score: scores) {
					if (score > maxScore) {
						maxScore = score;
					}
					if (score < minScore) {
						minScore = score;
					}
					totalScore += score;
					count++;
				}
			}
		}
		if (count != 0) {
			avgScore = totalScore/count;
		}
		
		List<Double> scoreList = new ArrayList<Double>();
		scoreList.add(maxScore);
		scoreList.add(avgScore);
		scoreList.add(minScore);
		return scoreList;
	}
	
}