package org.com.techsalesmanagerclient.controller;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CorrectStructure {

    public boolean getMinSortedSymbols(int symbolsValue, String phrase) {
        int symbols = phrase.length();
        return symbols <= symbolsValue;
    }

    public boolean getMaxSortedSymbols(int symbolsValue, String phrase) {
        int symbols = phrase.length();
        return symbols >= symbolsValue;
    }

    public boolean containsSpace(String word) {
        return word.contains(" ");
    }

    public boolean containsSpecialSymbols(String word) {
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(word);
        return m.find();
    }
}
