/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featureextractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import opennlp.tools.sentdetect.*;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.Span;

/**
 *
 * @author David Klecker
 */

public class FeatureExtractor {

    static String stopwords[] = {"edu", "a", "about", "above", "above", "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also","although","always","am","among", "amongst", "amoungst", "amount",  "an", "and", "another", "any","anyhow","anyone","anything","anyway", "anywhere", "are", "around", "as",  "at", "back","be","became", "because","become","becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill", "both", "bottom","but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry", "de", "describe", "detail", "do", "done", "down", "due", "during", "each", "eg", "eight", "either", "eleven","else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", "fify", "fill", "find", "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has", "hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "how", "however", "hundred", "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itself", "keep", "last", "latter", "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most", "mostly", "move", "much", "must", "my", "myself", "name", "namely", "neither", "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own","part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", "seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thickv", "thin", "third", "this", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward", "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", "yourselves", "the", "2000", "faq", };     
    public static ArrayList<DocumentObjectClass> Vector = new ArrayList<DocumentObjectClass>();

    public static class TokenObjectClass{
        String m_Word;
        Float m_TermFrequency;
        Float m_Probability;
        Integer m_Count;
        
        public TokenObjectClass(String Word, Integer Count){
            m_Word          = Word;
            m_Count         = Count;
            m_TermFrequency = new Float(0.0);
            m_Probability   = new Float(0.0);            
        }
    }
    
    public static class DocumentObjectClass{
        String m_DocumentName;
        int m_WordsInDocument;
        String m_Classification; //Headline, Sports, Entertainment. 
        
        HashMap<String, TokenObjectClass> m_TokenMap;
        
        public DocumentObjectClass(HashMap<String, TokenObjectClass> TokenMap, String Document, int WordCount){
            m_TokenMap = TokenMap;
            m_DocumentName          = Document;
            int m_WordsInDocument   = WordCount;
            m_Classification        = "";
        }
    }
   
    public static void main(String[] args) throws FileNotFoundException, IOException {
               
        String myDirectoryPath  = "./20_newsgroups/rec.autos";
        File dir                = new File(myDirectoryPath);
        File[] directoryListing = dir.listFiles();

        if (directoryListing != null) {
            int numberofFiles = 0;
            for (File child : directoryListing) {
                
                Scanner sc2 = null;
                
                try {
                    sc2             = new Scanner(new File(child.getPath()));
                    String document = "";
                    while (sc2.hasNextLine()) {
                        document += sc2.nextLine();
                    }
                        
                    String stopWordsPattern = String.join("|", stopwords);
                    Pattern pattern         = Pattern.compile("\\b(?:" + stopWordsPattern + ")\\b\\s*", Pattern.CASE_INSENSITIVE);
                    Matcher matcher         = pattern.matcher(document);
                    document                = matcher.replaceAll("");

                    //Loading the Tokenizer model 
                    InputStream inputStream = new FileInputStream("./OpenNLP_Models/de-token.bin"); 
                    TokenizerModel tokenModel = new TokenizerModel(inputStream); 

                    TokenizerME tokenizer   = new TokenizerME(tokenModel);

                    Span tokens[]           = tokenizer.tokenizePos(document); 

                    //Getting the probabilities of the recent calls to tokenizePos() method 
                    double[] probs          = tokenizer.getTokenProbabilities(); 
                    String word             = "";
                    
                    HashMap<String, TokenObjectClass> m_Map = new HashMap<String, TokenObjectClass>();
                    for(int i=0; i<tokens.length;i++)
                    {
                        Span token = tokens[i];
                        word = TrimWord(document.substring(token.getStart(), token.getEnd()));
                        
                        if(word.isEmpty()) continue;
                        
                        TokenObjectClass m_Token = m_Map.get(word);
                        if(m_Token != null){
                            Integer count = m_Token.m_Count;
                            m_Token.m_Count = (m_Map.containsKey(word)) ? new Integer(count.intValue() + 1) : new Integer(1);
                        }
                        else{
                            TokenObjectClass pToken = new TokenObjectClass(word, 1);
                            pToken.m_Probability = new Float(probs[i]);
                            m_Map.put(word, pToken);
                        }
                    }

                    HashMap<String, TokenObjectClass> SortedMap = sortByValues(m_Map);
                    
                    DocumentObjectClass pDoc = new DocumentObjectClass(SortedMap, child.getName(), SortedMap.size());

                    Set set = SortedMap.entrySet();
                    Iterator iterator = set.iterator();
                    int WordCount = SortedMap.size();
                    
                    while(iterator.hasNext()) {
                        Map.Entry mentry        = (Map.Entry)iterator.next();
                        TokenObjectClass pToken = (TokenObjectClass) mentry.getValue();
                        int value               = (int)pToken.m_Count;
                        pToken.m_TermFrequency  = 100*(value / (float)WordCount);
                    }
                    
                    Vector.add(pDoc);
                    
                } catch (FileNotFoundException e) {
                    e.printStackTrace();  
                }
            }
        } else {
          // Handle the case where dir is not really a directory.
          // Checking dir.isDirectory() above would not be sufficient
          // to avoid race conditions with another process that deletes
          // directories.
        } 
        
        System.out.println("Done!");
        
        Vector.forEach((vector1) -> {
            System.out.println(vector1.m_DocumentName);
            
            //List list = new LinkedList(vector1.m_TokenMap.entrySet());
            //Iterator VectorListIt1 = list.iterator();
            //while(VectorListIt1.hasNext())
            //{
                //Map.Entry mentry    = (Map.Entry)VectorListIt1.next();
                //String word         = (String) mentry.getKey();
                //Float frequency     = vector1.m_FrequencyMap.get(word);
                //Float Probs         = vector1.m_ProbMap.get(word);
                
                //System.out.printf ("%-15s TF:%-4f Probs:%-5.3f\n", word, frequency, Probs);
            //}
        });
    }
    
    private static String TrimWord(String word)
    {
        word = word.toLowerCase();

        if(word.length() == 1 || word.length() == 2) return "";
        if(word.contains(":")) return "";
        if(word.contains("<")) return "";
        if(word.contains(">")) return "";

        Pattern p = Pattern.compile("[a-zA-Z0-9][.][a-zA-Z0-9]");
        Matcher m = p.matcher(word);
        if (m.find()) return "";

        p = Pattern.compile("[0-9]");
        m = p.matcher(word);
        if (m.find()) return "";
        
        p = Pattern.compile("[(].");
        m = p.matcher(word);
        if (m.find()) return "";

        p = Pattern.compile(".[)]");
        m = p.matcher(word);
        if (m.find()) return "";

        p = Pattern.compile("[-]+");
        m = p.matcher(word);
        if (m.find()) return "";

        word = word.replaceAll("[^a-zA-Z0-9\\s]", "");
        if(word.length() == 0) return "";

        return word;
    } 

    //Taken from https://beginnersbook.com/2013/12/how-to-sort-hashmap-in-java-by-keys-and-values/

    private static HashMap sortByValues(HashMap map) { 
        List list = new LinkedList(map.entrySet());
        
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                Map.Entry<String, TokenObjectClass> Token1 = (Map.Entry<String, TokenObjectClass>)o1;
                Map.Entry<String, TokenObjectClass> Token2 = (Map.Entry<String, TokenObjectClass>)o2;
                
                return (int) ((Comparable) (((TokenObjectClass)Token1.getValue()).m_Count).compareTo(((TokenObjectClass)Token1.getValue()).m_Count));
            }
       });
        
       // Here I am copying the sorted list in HashMap
       // using LinkedHashMap to preserve the insertion order
       HashMap sortedHashMap = new LinkedHashMap();
       for (Iterator it = list.iterator(); it.hasNext();) {
              Map.Entry entry = (Map.Entry) it.next();
              sortedHashMap.put(entry.getKey(), entry.getValue());
       } 
       return sortedHashMap;
    }  
    

                        
            
            /*BagOfWords = GenerateBagOfWords(Vector.size());
            
            for(int i=0; i<BagOfWords.size(); i++)
            {
                BagOfWordsVector pObj = BagOfWords.get(i);
                System.out.printf ("Word:%-15s Appears in %-4d of the %d documents. Prob:%-5.3f\n", pObj.m_Word, pObj.m_TimesInTraining, Vector.size(), pObj.m_POfXGivenY);
            }            
        }*/
    //}
    
    /*private static ArrayList<BagOfWordsVector> GenerateBagOfWords(int NumberOfDocuments) { 
        
        
        BagOfWords = new ArrayList<>();
        ArrayList OnlyWords = new ArrayList();
        
        Vector.forEach((vector1) -> {
            List list = new LinkedList(vector1.m_Map.entrySet());
            Iterator VectorListIt1 = list.iterator();
            while(VectorListIt1.hasNext())
            {
                Map.Entry mentry        = (Map.Entry)VectorListIt1.next();
                float Frequency         = (int) mentry.getValue() / (float)vector1.m_CountWords;
                Frequency               = Frequency*100;
                if(Frequency > 0)
                {
                    Iterator it2 = Vector.iterator();
                    int Candidate       = 0;
                
                    while(it2.hasNext())
                    {
                        FeatureVectors vector2  = (FeatureVectors)it2.next();
                
                        if((vector1.m_DocumentName != vector2.m_DocumentName))
                        {                           
                            if(vector2.m_Map.containsKey(mentry.getKey()))
                                Candidate++;
                        }
                    }
                    
                    float CandidateFrequency = (float)Candidate / (float)Vector.size();
                    //System.out.println(Candidate + ":" + Vector.size() +", CandidateFrequency for "+ mentry.getKey() +": "+ CandidateFrequency);
                    
                    CandidateFrequency *= 100;
                    if(CandidateFrequency > 5.0) //This means I only want words that occur in more than 5% of the documents. 
                        
                    {                       
                        BagOfWordsVector pObj = new BagOfWordsVector(NumberOfDocuments, (String)mentry.getKey(), Candidate);
                        if(!OnlyWords.contains(pObj.m_Word))
                        {
                            OnlyWords.add(pObj.m_Word);
                            BagOfWords.add(pObj);
                        }
                    }
                }
            }
        });
            
        return BagOfWords;
    }

    
    }*/
    
}
