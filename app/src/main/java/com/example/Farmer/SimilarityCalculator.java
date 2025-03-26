package com.example.Farmer;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.SimpleTokenizer;

public class SimilarityCalculator {

    private PorterStemmer stemmer;
    private SimpleTokenizer tokenizer;
    private Set<String> stopWords;

    public SimilarityCalculator(Context context) {
        // Initialize PorterStemmer and SimpleTokenizer
        stemmer = new PorterStemmer();
        tokenizer = SimpleTokenizer.INSTANCE;
        // Load stop words from file
        stopWords = loadStopWords(context);
    }

    private Set<String> loadStopWords(Context context) {
        Set<String> stopWords = new HashSet<>();
        InputStream inputStream = context.getResources().openRawResource(R.raw.stopwords);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopWords;
    }

    public Map<String, Integer> preprocessText(String text) {
        String[] words = tokenizer.tokenize(text);
        Map<String, Integer> wordCounts = new HashMap<>();

        for (String word : words) {
            String stemmedWord = stemmer.stem(word.toLowerCase());
            if (stemmedWord.matches("[a-zA-Z]+") && !stopWords.contains(stemmedWord)) {
                wordCounts.put(stemmedWord, wordCounts.getOrDefault(stemmedWord, 0) + 1);
            }
        }

        return wordCounts;
    }

    public double calculateJaccardSimilarity(Map<String, Integer> words1, Map<String, Integer> words2) {
        int intersection = 0;
        int union = 0;

        for (Map.Entry<String, Integer> entry : words1.entrySet()) {
            String word = entry.getKey();
            int count1 = entry.getValue();
            int count2 = words2.getOrDefault(word, 0);

            intersection += Math.min(count1, count2);
            union += Math.max(count1, count2);
        }

        for (Map.Entry<String, Integer> entry : words2.entrySet()) {
            if (!words1.containsKey(entry.getKey())) {
                union += entry.getValue();
            }
        }

        return union == 0 ? 0 : (double) intersection / union;
    }

    public List<Map.Entry<String, Double>> calculateSimilarity(Map<String, Integer> targetBlog, List<Map<String, Integer>> allBlogs) {
        List<Map.Entry<String, Double>> similarities = new ArrayList<>();

        for (Map<String, Integer> blog : allBlogs) {
            double similarity = calculateJaccardSimilarity(targetBlog, blog);
            similarities.add(new AbstractMap.SimpleEntry<>(blog.toString(), similarity));
        }

        return similarities;
    }
}