package com.eurecom.sentinel;

/**
 * Main entrance of SentiNEL System
 *
 * @author SentiNEL
 *
 */
public class Main {

	static String mode;
	static String PATH = "";
	private static long startTime = System.currentTimeMillis();

	/**
	 * Main function
	 * @param args Command-Line Arguments
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		/*names of arff file where stores the features
		 * train train_dataset, call trainSvstem to create arff file, if savename == "", use defaut features
		 * eval test_dataset, first train classifier with arff file, then evaluate with test_dataset
		 */
		System.out.println("[INFO] Attention for the usage of the system");
		System.out.println("--If you want to get features of a dataset in order to train a system, please run the system with the following params:");
		System.out.println("  train trainDatasetFile saveFeaturesFile(optional)");
		System.out.println("--If you want to eval a system(firstly train a system with a featuresFile, then evaluate the system with the testDataset), please run the system with the following params:");
		System.out.println("  eval testDatasetFile featuresFile\n");

		String saveFeatures = "";
		String useFeatures = "";

		/*
		 * replace:
		 * args[0]: args[0].split("\\s")[0]
		 * args[1]: args[0].split("\\s")[1]
		 * args[2]: args[0].split("\\s")[2]
		 * */
		switch(args[0].split("\\s").length) {
			case 2 :
				break;
			case 3 :
				if (args[0].split("\\s")[0].equals("train")) {
					saveFeatures = args[0].split("\\s")[2];
				} else if (args[0].split("\\s")[0].equals("eval")) {
					useFeatures = args[0].split("\\s")[2];
				}
				break;
			default :
				System.out.println("[usage] <mode: train | eval> <datasetname> [savename]");
				System.exit(0);
		}

		PATH = args[0].split("\\s")[1];

		SentimentAnalysis sentimentAnalysis = new SentimentAnalysis(PATH);
		switch(args[0].split("\\s")[0]) {
			case "train":
				sentimentAnalysis.trainSystem(saveFeatures);
				break;
			case "eval":
				sentimentAnalysis.testSystem(useFeatures);
				break;
			default:
				throw new IllegalArgumentException("Invalid mode: " + args[0].split("\\s")[0]);
		}

		long endTime = System.currentTimeMillis();
		System.out.println("It took " + ((endTime - startTime) / 1000) + " seconds");

	}
}
