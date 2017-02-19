/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package naivebayes;

/**
 *
 * @author welcome
 */
import java.util.HashMap;
import java.util.Map;


public class FeatureStats {
    public int n;
    
    public Map<String, Map<String, Integer>> featureCategoryJointCount;
    
    public Map<String, Integer> categoryCounts;

    public FeatureStats() {
        n = 0;
        featureCategoryJointCount = new HashMap<>();
        categoryCounts = new HashMap<>();
    }
}
