package naivebayes;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author welcome
 */


public class NaiveBayesKnowledgeBase {
    public int n=0;
    
    public int c=0;
    
    public int d=0;
    
    /**
     * log priors for log( P(c) )
     */
    public Map<String, Double> logPriors = new HashMap<>();
    
    /**
     * log likelihood for log( P(x|c) ) 
     */
    public Map<String, Map<String, Double>> logLikelihoods = new HashMap<>();
}
