import java.io.*;
import java.util.*;

/**
 * Created by manshu on 10/20/14.
 */

public class FrequentPatternMine {
    private ArrayList<ArrayList<Integer>> Transactional_Database;
    private static ArrayList<ArrayList<ArrayList<Integer>>> Topics_Transactional_Database = null;
    private static int num_topics;

    private HashMap<ArrayList<Integer>, Integer> candidate_set;
    private ArrayList<HashMap<ArrayList<Integer>, Integer>> all_frequent_itemsets;
    private ArrayList<HashMap<ArrayList<Integer>, Integer>> max_frequent_itemsets;
    private ArrayList<HashMap<ArrayList<Integer>, Integer>> closed_frequent_itemsets;
    private HashMap<Integer, ArrayList<ArrayList<String>>> frequent_itemsets_words;
    private HashMap<Integer, ArrayList<ArrayList<String>>> max_frequent_itemsets_words;
    private HashMap<Integer, ArrayList<ArrayList<String>>> closed_frequent_itemsets_words;

    private static HashMap<Integer, String>  vocab_map = null;
    private static HashMap<String,  Integer> rev_vocab_map = null;
    private static ArrayList<HashMap<ArrayList<Integer>, Integer>> Topics_Patterns = null;

    private double Min_Support;

    private static HashMap<ArrayList<Integer>, ArrayList<Integer>> pattern_frequency;
    private static ArrayList<HashMap<ArrayList<Integer>, Double>> TopicPatternPurity;

    private static int[][] Dt;

    private void print(String str){
        //System.out.println(str);
    }

    private HashMap<ArrayList<Integer>, Integer> find_1_frequent_set(HashMap<ArrayList<Integer>, Integer> candidate_set){
        HashMap<ArrayList<Integer>, Integer> hm = new HashMap<ArrayList<Integer>, Integer>();
        for (ArrayList<Integer> candidate : candidate_set.keySet()){
            if (candidate_set.get(candidate) >= Min_Support){
                hm.put(candidate, candidate_set.get(candidate));
            }
        }
        return hm;
    }

    private HashMap<ArrayList<Integer>, Integer> find_frequent_set(HashMap<ArrayList<Integer>, Integer> candidate_set){
        HashMap<ArrayList<Integer>, Integer> hm = new HashMap<ArrayList<Integer>, Integer>();

        for (ArrayList<Integer> candidate : candidate_set.keySet()){
            for (ArrayList<Integer> line : Transactional_Database){
                int match = 1;
                for (Integer i : candidate){
                    if (!line.contains(i))
                        match = -1;
                }
                if (match == 1)
                    candidate_set.put(candidate, candidate_set.get(candidate) + 1);
            }
        }

        for (ArrayList<Integer> candidate : candidate_set.keySet()){

            if (candidate_set.get(candidate) >= Min_Support){
                hm.put(candidate, candidate_set.get(candidate));
            }
        }

        return hm;
    }

    private HashMap<ArrayList<Integer>, Integer> apriori_gen(HashMap<ArrayList<Integer>, Integer> prev_frequent_item_set){
        HashMap<ArrayList<Integer>, Integer> new_candidate_set = new HashMap<ArrayList<Integer>, Integer>();

        for (ArrayList<Integer> l1 : prev_frequent_item_set.keySet()){
            int l1_size = l1.size();
            for (ArrayList<Integer> l2 : prev_frequent_item_set.keySet()){
                int match = 1;
                for (int i = 0; i <= l1.size() - 2; i++){
                    if (l1.get(i) != l2.get(i)){
                        match = -1;
                        break;
                    }
                }
                if (match == 1 && l1.get(l1_size - 1) < l2.get(l1_size - 1)){
                    ArrayList<Integer> arr = (ArrayList<Integer>) l1.clone();
                    arr.add(l2.get(l1_size - 1));
                    //Collections.sort(arr);
                    if (!has_infrequent_subset(arr, prev_frequent_item_set))
                        new_candidate_set.put(arr, 0);
                    else
                        print("Pruned " + arr);
                }

            }
        }
        return new_candidate_set;
    }

    private boolean has_infrequent_subset(ArrayList<Integer> new_candidate_subset, HashMap<ArrayList<Integer>, Integer> prev_frequent_item_set){
        for (int i = 0; i < new_candidate_subset.size(); i++){
            ArrayList<Integer> subsets = new ArrayList<Integer>();
            for (int j = 0; j < new_candidate_subset.size(); j++){
                if (i != j){
                    subsets.add(new_candidate_subset.get(j));
                }
            }
            Collections.sort(subsets);
            if (prev_frequent_item_set.get(subsets) == null)
                return true;
        }
        return false;
    }

    public FrequentPatternMine(String topic_file_path, String vocab_file_path, int file_num, int nTopics, double min_support) throws IOException, FileNotFoundException{
        Transactional_Database = new ArrayList<ArrayList<Integer>>();
        candidate_set = new HashMap<ArrayList<Integer>, Integer>();
        all_frequent_itemsets = new ArrayList<HashMap<ArrayList<Integer>, Integer>>();
        num_topics = nTopics;
        BufferedReader br = new BufferedReader(new FileReader(topic_file_path));
        String line = "";
        int num_transactions = 0;
        while ((line = br.readLine()) != null) {
            num_transactions++;
            //print(line);
            String words[] = line.split("\\s+");
            ArrayList<Integer> line_values = new ArrayList<Integer>();
            for (String word : words) {
                Integer word_id = Integer.parseInt(word);
                line_values.add(word_id);
                ArrayList<Integer> word_combination = new ArrayList<Integer>();
                word_combination.add(word_id);
                //Collections.sort(word_combination);
                if (candidate_set.get(word_combination) != null)
                    candidate_set.put(word_combination, candidate_set.get(word_combination) + 1);
                else {
                    candidate_set.put(word_combination, 1);
                }
            }
            Transactional_Database.add(line_values);
        }
        if (Topics_Transactional_Database == null)
            Topics_Transactional_Database = new ArrayList<ArrayList<ArrayList<Integer>>>();
        if (Topics_Patterns == null)
            Topics_Patterns = new ArrayList<HashMap<ArrayList<Integer>, Integer>>();
        Topics_Patterns.add(new HashMap<ArrayList<Integer>, Integer>());
        Topics_Transactional_Database.add(Transactional_Database);

        Min_Support = min_support * num_transactions;

        HashMap<ArrayList<Integer>, Integer> L = find_1_frequent_set(candidate_set);
        for (ArrayList<Integer> candidate : L.keySet()){
            print("Candidate : " + candidate + ", Count = " + L.get(candidate));
        }
        all_frequent_itemsets.add(L);
        for (int k = 2; L.size() != 0; k++){
            candidate_set = apriori_gen(L);
            /*
            for (ArrayList<Integer> candidate : candidate_set.keySet()){
                print("Candidate : " + candidate + ", Count = " + candidate_set.get(candidate));
            }
            print("Candidate Itemset-" + k + " has size of " + candidate_set.size());
            */
            L = find_frequent_set(candidate_set);
            if (L.size() == 0) break;
            print("Frequent Itemset-" + k + " ================================================================================ ");
            for (ArrayList<Integer> candidate : L.keySet()){
                print("L : " + candidate + ", Count = " + L.get(candidate));
            }
            print("Frequent Itemset-" + k + " has size of " + L.size() + " ================================================================================ ");
            all_frequent_itemsets.add(L);
        }

        int k = 1;
        for (HashMap<ArrayList<Integer>, Integer> hm : all_frequent_itemsets){
            System.out.println("===================================================================== Frequent Itemset-" + k++ + " ====================================================================");
            for (ArrayList<Integer> itemset : hm.keySet()){
                System.out.println(itemset + " : " + hm.get(itemset));
                Topics_Patterns.get(file_num).put(itemset, hm.get(itemset));
            }
            System.out.println("=============================================================================================================================================================");
            System.out.println();
        }

        if (vocab_map == null)
            VocabMap(vocab_file_path);

        frequent_itemsets_words = convertFrequentSets(all_frequent_itemsets);
        printPattern(frequent_itemsets_words, "pattern", file_num);
        findMaxPattern();
        max_frequent_itemsets_words = convertFrequentSets(max_frequent_itemsets);
        printPattern(max_frequent_itemsets_words, "max", file_num);
        findClosedPattern();
        closed_frequent_itemsets_words = convertFrequentSets(closed_frequent_itemsets);
        printPattern(closed_frequent_itemsets_words, "closed", file_num);
    }

    private boolean findSuperPattern(ArrayList<Integer> pattern, HashMap<ArrayList<Integer>, Integer> hm, boolean support_check, int support){
        int match = 0;
        for (ArrayList<Integer> alp : hm.keySet()){
            match = 1;
            for (Integer i : pattern)
                if (!alp.contains(i)){
                    match = -1;
                    break;
                }
            if (match == 1){
                if (!support_check)
                    return true;
                else{
                    if (hm.get(alp) == support)
                        return true;
                }
            }
        }
        return false;
    }

    private void findMaxPattern(){
        max_frequent_itemsets = new ArrayList<HashMap<ArrayList<Integer>, Integer>>();
        for (int i = 0; i < all_frequent_itemsets.size(); i++){
            HashMap<ArrayList<Integer>, Integer> hm = new HashMap<ArrayList<Integer>, Integer>();
            for (ArrayList<Integer> pattern : all_frequent_itemsets.get(i).keySet()){
                if (i != (all_frequent_itemsets.size() - 1) && findSuperPattern(pattern, all_frequent_itemsets.get(i + 1), false, 0)) continue;
                else{
                    hm.put(pattern, all_frequent_itemsets.get(i).get(pattern));
                }
            }
            max_frequent_itemsets.add(hm);
        }
    }

    private void findClosedPattern(){
        closed_frequent_itemsets = new ArrayList<HashMap<ArrayList<Integer>, Integer>>();
        for (int i = 0; i < all_frequent_itemsets.size(); i++){
            HashMap<ArrayList<Integer>, Integer> hm = new HashMap<ArrayList<Integer>, Integer>();
            for (ArrayList<Integer> pattern : all_frequent_itemsets.get(i).keySet()){
                if (i != (all_frequent_itemsets.size() - 1) && findSuperPattern(pattern, all_frequent_itemsets.get(i + 1), true, all_frequent_itemsets.get(i).get(pattern))) continue;
                else{
                    hm.put(pattern, all_frequent_itemsets.get(i).get(pattern));
                }

            }
            closed_frequent_itemsets.add(hm);
        }
    }

    public void VocabMap(String vocab_file_path) throws IOException, FileNotFoundException{
        vocab_map = new HashMap<Integer, String>();
        rev_vocab_map = new HashMap<String, Integer>();
        BufferedReader br = new BufferedReader(new FileReader(vocab_file_path));
        String line = "";
        int num_words = 0;
        Integer index;
        while ((line = br.readLine()) != null) {
            num_words++;
            String words[] = line.split("\\t");
            index = Integer.parseInt(words[0]);
            vocab_map.put(index, words[1]);
            rev_vocab_map.put(words[1], index);
        }
    }

    public HashMap<Integer, ArrayList<ArrayList<String>>> convertFrequentSets(ArrayList<HashMap<ArrayList<Integer>, Integer>> num_freq_items){
        HashMap<Integer, ArrayList<ArrayList<String>>> frequent_itemsets = new HashMap<Integer, ArrayList<ArrayList<String>>>();
        for (HashMap<ArrayList<Integer>, Integer> hm : num_freq_items){
            for (ArrayList<Integer> itemset : hm.keySet()){
                ArrayList<String> as = new ArrayList<String>();
                for (Integer index : itemset)
                    as.add(vocab_map.get(index));
                int support = hm.get(itemset);
                if (frequent_itemsets.containsKey(support))
                    frequent_itemsets.get(support).add(as);
                else{
                    ArrayList<ArrayList<String>> fitems = new ArrayList<ArrayList<String>>();
                    fitems.add(as);
                    frequent_itemsets.put(support, fitems);
                }
            }
        }
        return frequent_itemsets;
    }

    public static HashMap<Double, ArrayList<ArrayList<String>>> convertPuritySets(HashMap<ArrayList<Integer>, Double> hm){
        HashMap<Double, ArrayList<ArrayList<String>>> purity_itemsets = new HashMap<Double, ArrayList<ArrayList<String>>>();
        for (ArrayList<Integer> itemset : hm.keySet()) {
            ArrayList<String> as = new ArrayList<String>();
            for (Integer index : itemset)
                as.add(vocab_map.get(index));
            double support = hm.get(itemset);
            if (purity_itemsets.containsKey(support))
                purity_itemsets.get(support).add(as);
            else {
                ArrayList<ArrayList<String>> pitems = new ArrayList<ArrayList<String>>();
                pitems.add(as);
                purity_itemsets.put(support, pitems);
            }
        }
        return purity_itemsets;
    }

    public void printPattern(HashMap<Integer, ArrayList<ArrayList<String>>> frequent_itemsets, String name, int num) throws FileNotFoundException, IOException{
        ArrayList<Integer> supports = new ArrayList<Integer>();
        supports.addAll(frequent_itemsets.keySet());
        Collections.sort(supports);
        Collections.reverse(supports);
        File folder = new File(name);
        if (!folder.exists())
            folder.mkdir();
        PrintWriter writer = new PrintWriter(name + "/" + name + "-" + num + ".txt", "ASCII");

        for (Integer support : supports){
            for (ArrayList<String> as : frequent_itemsets.get(support)){
                System.out.println(support + " : " + as);
                writer.println(support + " " + as);
            }
        }

        writer.close();
    }

    public static void printPatternPurity(HashMap<Double, ArrayList<ArrayList<String>>> purity_itemsets, String name, int num) throws FileNotFoundException, IOException{
        ArrayList<Double> purities = new ArrayList<Double>();
        purities.addAll(purity_itemsets.keySet());
        Collections.sort(purities);
        Collections.reverse(purities);
        File folder = new File(name);
        if (!folder.exists())
            folder.mkdir();
        PrintWriter writer = new PrintWriter(name + "/" + name + "-" + num + ".txt", "ASCII");

        for (Double purity : purities){
            final int num_file = num;
            Collections.sort(purity_itemsets.get(purity), new Comparator<ArrayList<String>>() {
                @Override
                public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                    ArrayList<Integer> i1 = new ArrayList<Integer>();
                    ArrayList<Integer> i2 = new ArrayList<Integer>();
                    for (String s : o1)
                        i1.add(rev_vocab_map.get(s));
                    for (String s : o2)
                        i2.add(rev_vocab_map.get(s));
                    return Topics_Patterns.get(num_file).get(i2).compareTo(Topics_Patterns.get(num_file).get(i1));
                }
            });
            for (ArrayList<String> as : purity_itemsets.get(purity)){
                System.out.println(purity + " : " + as);
                writer.println(purity + " " + as);
            }
        }

        writer.close();
    }

    private static int findFrequencyInFile(ArrayList<Integer> pattern, int topic){
        int frequency_count = 0;
        for (ArrayList<Integer> line : Topics_Transactional_Database.get(topic)){
            int match = 1;
            for (Integer word : pattern){
                if (!line.contains(word)){
                    match = -1;
                    break;
                }
            }
            if (match == 1)
                frequency_count++;
        }
        return frequency_count;
    }

    private static int findFrequencyInTopic(ArrayList<Integer> pattern, int topic){
        if (Topics_Patterns.get(topic).containsKey(pattern))
            return Topics_Patterns.get(topic).get(pattern);
        else{
            return 0;//findFrequencyInFile(pattern, topic);
        }
    }

    public static void findTopicFrequencyPattern(){
        pattern_frequency = new HashMap<ArrayList<Integer>, ArrayList<Integer>>();
        for (HashMap<ArrayList<Integer>, Integer> topic_patterns : Topics_Patterns)
            for (ArrayList<Integer> pattern : topic_patterns.keySet()){
                if (!pattern_frequency.containsKey(pattern)){
                    ArrayList<Integer> arr = new ArrayList<Integer>(num_topics);
                    for (int i = 0; i < num_topics; i++){
                        arr.add(findFrequencyInTopic(pattern, i));
                    }
                    pattern_frequency.put(pattern, arr);
                }
            }

        for (ArrayList<Integer> ai : pattern_frequency.keySet()){
            System.out.println("Pattern = " + ai + " : Topic_Wise_Frequency = " + pattern_frequency.get(ai));
        }
    }

    public static void findPurity() throws IOException {
        TopicPatternPurity = new ArrayList<HashMap<ArrayList<Integer>, Double>>();
        for (int k = 0; k < num_topics; k++){
            HashMap<ArrayList<Integer>, Integer> topic_patterns = Topics_Patterns.get(k);
            TopicPatternPurity.add(new HashMap<ArrayList<Integer>, Double>());
            for (ArrayList<Integer> pattern : topic_patterns.keySet()){
                double max = Double.MIN_VALUE;
                for (int i = 0; i < num_topics; i++){
                    if ( k == i) continue;
                    double val = pattern_frequency.get(pattern).get(k) + pattern_frequency.get(pattern).get(i);
                    val = val / (Dt[k][i] * 1.0);
                    if (max < val)
                        max = val;
                }
                double entry = Math.log(pattern_frequency.get(pattern).get(k) / (1.0 * Dt[k][k])) - Math.log(max);
                entry = Math.round(entry * 10000.0) / 10000.0;
                TopicPatternPurity.get(k).put(pattern, entry);
            }
        }

        for (int k = 0; k < num_topics; k++){
            HashMap<ArrayList<Integer>, Double> topic_purity = TopicPatternPurity.get(k);
            System.out.println("For Topic = " + k + " ======================================================== ");
            for (ArrayList<Integer> pattern : topic_purity.keySet())
                System.out.println("Pattern = " + pattern + " Purity = " + topic_purity.get(pattern));
            System.out.println("================================================================================= ");
        }

        for (int k = 0; k < num_topics; k++) {
            HashMap<ArrayList<Integer>, Double> topic_purity = TopicPatternPurity.get(k);
            HashMap<Double, ArrayList<ArrayList<String>>> purity_set = convertPuritySets(topic_purity);
            printPatternPurity(purity_set, "purity", k);
        }

    }

    public static void main(String args[]){
        String topic_files_path = "";
        String vocab_file_path = "";
        Integer num_topics = 1;
        Double min_support = 0.001;
        FrequentPatternMine.Dt = new int [][]{
            {10047, 17326, 17988, 17999, 17820},
            {17326, 9674, 17446, 17902, 17486},
            {17988, 17446, 9959, 18077, 17492},
            {17999, 17902, 18077, 10161, 17912},
            {17820, 17486, 17492, 17912, 9845}
        };

        if (args.length >= 1)
            topic_files_path = args[0];
        vocab_file_path = topic_files_path + "/vocab.txt";
        try {
            long t1 = System.currentTimeMillis();
            String topic_file_path = "";
            for (int i = 0; i < num_topics; i++) {
                topic_file_path = topic_files_path + "/topic-" + i + ".txt";
                FrequentPatternMine fp = new FrequentPatternMine(topic_file_path, vocab_file_path, i, num_topics, min_support);
            }
            FrequentPatternMine.findTopicFrequencyPattern();
            long t2 = System.currentTimeMillis();
            FrequentPatternMine.findPurity();
            System.out.println(FrequentPatternMine.pattern_frequency.size());
            long t3 = System.currentTimeMillis();
            System.out.println("Time taken = " + (t3 - t1));
            System.out.println("Time taken = " + (t3 - t2));
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
