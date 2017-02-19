/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package naivebayes;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author welcome
 */
public class NaiveBayesPckg {

    
    public static String[] readLines(URL url) throws IOException {

        Reader fileReader = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));
        List<String> lines;
        try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            lines = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines.toArray(new String[lines.size()]);
    }
    
    
    public static void main(String[] args) throws IOException {
        //map of dataset files
        Map<String, URL> trainingFiles = new HashMap<>();
        trainingFiles.put("English", NaiveBayesPckg.class.getResource("/datasets/training.language.en.txt"));
        trainingFiles.put("French", NaiveBayesPckg.class.getResource("/datasets/training.language.fr.txt"));
        trainingFiles.put("German", NaiveBayesPckg.class.getResource("/datasets/training.language.de.txt"));
        
        //loading examples in memory
        Map<String, String[]> trainingExamples = new HashMap<>();
        for(Map.Entry<String, URL> entry : trainingFiles.entrySet()) {
            trainingExamples.put(entry.getKey(), readLines(entry.getValue()));
           // System.out.println(trainingExamples.entrySet());
        }
       /* for(String s : trainingExamples.keySet())
        {
            String [] temp =trainingExamples.get(s);
            for(String t :temp)
            {
                System.out.println(s+"  "+t);
            }
            
        }*/
       // System.out.println(trainingExamples.entrySet());
        
        //train classifier
        NaiveBayes nb = new NaiveBayes();
        nb.setChisquareCriticalValue(6.63); //0.01 pvalue
        nb.train(trainingExamples);
        
        //get trained classifier knowledgeBase
        NaiveBayesKnowledgeBase knowledgeBase = nb.getKnowledgeBase();
        
        nb = null;
        trainingExamples = null;
        
        
        //Use classifier
        nb = new NaiveBayes(knowledgeBase);
        //lab package submission
        String exampleEn = "lab package submission";
       // String exampleEn = "Internet appears to be ideal infrastructure";
        String outputEn = nb.predict(exampleEn);
        System.out.format("The sentense \"%s\" was classified as \"%s\".%n", exampleEn, outputEn);
        
        //un de l encyclopedie aller a est un nom utilise en avec 
        String exampleFr = "un de l encyclopedie aller a est un nom utilise en avec ";
        String outputFr = nb.predict(exampleFr);
        System.out.format("The sentense \"%s\" was classified as \"%s\".%n", exampleFr, outputFr);
        
        String exampleDe = "Guten Morgen einen schönen Tag";
        //Guten Morgen einen schönen Tag
        String outputDe = nb.predict(exampleDe);
        System.out.format("The sentense \"%s\" was classified as \"%s\".%n", exampleDe, outputDe);
        

    }
    
}
