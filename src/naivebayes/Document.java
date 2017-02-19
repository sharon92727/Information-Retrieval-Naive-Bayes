/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package naivebayes;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author welcome
 */

public class Document {
    
    public Map<String, Integer> tokens;
    public String category;
    
    public Document() {
        tokens = new HashMap<>();
    }
}