/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package naivebayes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 *
 * @author welcome
 */

public class NaiveBayes {
    private double chisquareCriticalValue; //equivalent to pvalue 0.001. It is used by feature selection algorithm
    
    private NaiveBayesKnowledgeBase knowledgeBase;
    
    public NaiveBayes(NaiveBayesKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }
    public NaiveBayes() {
        this(null);
    }
    
    public NaiveBayesKnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }
    
    
    public double getChisquareCriticalValue() {
        return chisquareCriticalValue;
    }
    
    
    public void setChisquareCriticalValue(double chisquareCriticalValue) {
        this.chisquareCriticalValue = chisquareCriticalValue;
    }
    
    private List<Document> preprocessDataset(Map<String, String[]> trainingDataset) {
        List<Document> dataset = new ArrayList<>();
                
        String category;
        String[] examples;
        
        Document doc;
        
        Iterator<Map.Entry<String, String[]>> it = trainingDataset.entrySet().iterator();
        
        //loop through all the categories and training examples
        while(it.hasNext()) {
            Map.Entry<String, String[]> entry = it.next();
            category = entry.getKey();
            examples = entry.getValue();
            
            for(int i=0;i<examples.length;++i) {
                //for each example in the category tokenize its text and convert it into a Document object.
                doc = TextTokenizer.tokenize(examples[i]);
                doc.category = category;
                dataset.add(doc);
                
                //examples[i] = null; //try freeing some memory
            }
            
            //it.remove(); //try freeing some memory
        }
        
        return dataset;
    }
    
    private FeatureStats selectFeatures(List<Document> dataset) {        
        FeatureExtraction featureExtractor = new FeatureExtraction();
        
        //the FeatureStats object contains statistics about all the features found in the documents
        FeatureStats stats = featureExtractor.extractFeatureStats(dataset); //extract the stats of the dataset
        System.out.println("                FEATURE CATEGORY COUNT:");
        for(String feature:stats.featureCategoryJointCount.keySet())
        {
            Map <String,Integer>m = stats.featureCategoryJointCount.get(feature);
            for(String category :m.keySet())
            {
                System.out.println(feature+ "   "+category+"    "+m.get(category));
            }
        }
        System.out.println("--------------------------------------------------------------------");
        //we pass this information to the feature selection algorithm and we get a list with the selected features
        Map<String, Double> selectedFeatures = featureExtractor.chisquare(stats, chisquareCriticalValue);
        System.out.println("                SELECTED FEATURES");
        for(String feature:selectedFeatures.keySet())
        {
            System.out.println(feature+"    "+selectedFeatures.get(feature));
        }
        System.out.println("--------------------------------------------------------------------");
        //clip from the stats all the features that are not selected
        Iterator<Map.Entry<String, Map<String, Integer>>> it = stats.featureCategoryJointCount.entrySet().iterator();
        while(it.hasNext()) {
            String feature = it.next().getKey();
        
            if(selectedFeatures.containsKey(feature)==false) {
                //if the feature is not in the selectedFeatures list remove it
                it.remove();
            }
        }
        
        return stats;
    }
    
    
    public void train(Map<String, String[]> trainingDataset, Map<String, Double> categoryPriors) throws IllegalArgumentException {
        //preprocess the given dataset
        List<Document> dataset = preprocessDataset(trainingDataset);
        
        
        //produce the feature stats and select the best features
        FeatureStats featureStats =  selectFeatures(dataset);
        
        
        //intiliaze the knowledgeBase of the classifier
        knowledgeBase = new NaiveBayesKnowledgeBase();
        knowledgeBase.n = featureStats.n; //number of observations
        knowledgeBase.d = featureStats.featureCategoryJointCount.size(); //number of features
        
        
        //check is prior probabilities are given
        if(categoryPriors==null) { 
            //if not estimate the priors from the sample
            knowledgeBase.c = featureStats.categoryCounts.size(); //number of cateogries
            knowledgeBase.logPriors = new HashMap<>();
            
            String category;
            int count;
            for(Map.Entry<String, Integer> entry : featureStats.categoryCounts.entrySet()) {
                category = entry.getKey();
                count = entry.getValue();
                
                knowledgeBase.logPriors.put(category, Math.log((double)count/knowledgeBase.n));
            }
        }
        else {
            //if they are provided then use the given priors
            knowledgeBase.c = categoryPriors.size();
            
            //make sure that the given priors are valid
            if(knowledgeBase.c!=featureStats.categoryCounts.size()) {
                throw new IllegalArgumentException("Invalid priors Array: Make sure you pass a prior probability for every supported category.");
            }
            
            String category;
            Double priorProbability;
            for(Map.Entry<String, Double> entry : categoryPriors.entrySet()) {
                category = entry.getKey();
                priorProbability = entry.getValue();
                if(priorProbability==null) {
                    throw new IllegalArgumentException("Invalid priors Array: Make sure you pass a prior probability for every supported category.");
                }
                else if(priorProbability<0 || priorProbability>1) {
                    throw new IllegalArgumentException("Invalid priors Array: Prior probabilities should be between 0 and 1.");
                }
                
                knowledgeBase.logPriors.put(category, Math.log(priorProbability));
            }
        }
  /*      System.out.println("            LOG PRIOR");
      for(String s :knowledgeBase.logPriors.keySet())
      {
           System.out.println(s+"   "+knowledgeBase.logPriors.get(s));
      }
      System.out.println("------------------------------------------------------------------------");
    */    
        // laplace smoothing
        Map<String, Double> featureOccurrencesInCategory = new HashMap<>();
        
        Integer occurrences;
        Double featureOccSum;
        for(String category : knowledgeBase.logPriors.keySet()) {
            featureOccSum = 0.0;
            for(Map<String, Integer> categoryListOccurrences : featureStats.featureCategoryJointCount.values()) {
                occurrences=categoryListOccurrences.get(category);
                if(occurrences!=null) {
                    featureOccSum+=occurrences;
                }
            }
            featureOccurrencesInCategory.put(category, featureOccSum);
        }
        System.out.println("            FEATURE CATEGORY OCCURANCE");
        for(String s :featureOccurrencesInCategory.keySet())
        {
            System.out.println(s+"   "+featureOccurrencesInCategory.get(s));
        }
      System.out.println("------------------------------------------------------------------------");
      
        //estimate log likelihoods
        String feature;
        Integer count;
        Map<String, Integer> featureCategoryCounts;
        double logLikelihood;
        System.out.println("            NAIVE BAYES VALUES");
        for(String category : knowledgeBase.logPriors.keySet()) {
            for(Map.Entry<String, Map<String, Integer>> entry : featureStats.featureCategoryJointCount.entrySet()) {
                feature = entry.getKey();
                featureCategoryCounts = entry.getValue();
                
                count = featureCategoryCounts.get(category);
                if(count==null) {
                    count = 0;
                }
                
                logLikelihood = Math.log((count+1.0)/(featureOccurrencesInCategory.get(category)+knowledgeBase.d));
                if(knowledgeBase.logLikelihoods.containsKey(feature)==false) {
                    knowledgeBase.logLikelihoods.put(feature, new HashMap<String, Double>());
                }
                System.out.println(feature+"    "+category+"    "+logLikelihood);
                knowledgeBase.logLikelihoods.get(feature).put(category, logLikelihood);
                
            }
        }
        System.out.println("---------------------------------------------------------------------");
       
     /*   for(String s :knowledgeBase.logLikelihoods.keySet()){
            for(String s1: knowledgeBase.logLikelihoods.get(s).keySet())
           System.out.println("   "+s1);
       }
       */
        
        featureOccurrencesInCategory=null;
    }
    
    
    public void train(Map<String, String[]> trainingDataset) {
        train(trainingDataset, null);
    }
    
   
    public String predict(String text) throws IllegalArgumentException {
        if(knowledgeBase == null) {
            throw new IllegalArgumentException("Knowledge Bases missing: Make sure you train first a classifier before you use it.");
        }
        
        //Tokenizes the text and creates a new document
        Document doc = TextTokenizer.tokenize(text);
        
        
        String category;
        String feature;
        Integer occurrences;
        Double logprob;
        
        String maxScoreCategory = null;
        Double maxScore=Double.NEGATIVE_INFINITY;
        System.out.println("        CATEGORY and LOG PROBABILITY");
        //Map<String, Double> predictionScores = new HashMap<>();
        for(Map.Entry<String, Double> entry1 : knowledgeBase.logPriors.entrySet()) {
            category = entry1.getKey();
            logprob = entry1.getValue(); //intialize the scores with the priors
            
            //foreach feature of the document
            for(Map.Entry<String, Integer> entry2 : doc.tokens.entrySet()) {
                feature = entry2.getKey();
                
                if(!knowledgeBase.logLikelihoods.containsKey(feature)) {
                    continue; //if the feature does not exist in the knowledge base skip it
                }
                
                occurrences = entry2.getValue(); //get its occurrences in text
                
                logprob += occurrences*knowledgeBase.logLikelihoods.get(feature).get(category); //multiply loglikelihood score with occurrences
            }
            //predictionScores.put(category, logprob); 
            System.out.println(category+"   "+logprob);
            if(logprob>maxScore) {
                maxScore=logprob;
                maxScoreCategory=category;
            }
        }
        
        return maxScoreCategory; //return the category with heighest score
    }
}
