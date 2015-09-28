package com.bericotech.clavin.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cue.lang.stop.StopWords;

public class AlphaExtractor implements LocationExtractor {

	private List<String> filter;
	private static String patternStart = "(^|\\s|\\,|\\.|\\-)(\\s*)(", patternEnd = ")*(\\s)(?-i)([A-Z]\\w+)";

	public AlphaExtractor(String prefix) {
		// Construimos la lista de terminos de prefijo
		filter = new ArrayList<String>();
		for (String s : prefix.split(",")) {
			filter.add(s.trim().toLowerCase());
		}
	}

	@Override
	public List<LocationOccurrence> extractLocationNames(String plainText, boolean prefix) {
		StopWords languaje = StopWords.guess(plainText);
		if (languaje == null)
			languaje = StopWords.Spanish;
		List<LocationOccurrence> lista = new ArrayList<LocationOccurrence>();
		if (!prefix) {
			int index = 0;
			for (String s : plainText.split(" ")) {
				if (!languaje.isStopWord(s)) {
					if (s.length() > 1) {
						index = plainText.indexOf(s, index);
						if (!filter.contains(s.toLowerCase())) {
							if (Character.isUpperCase(s.charAt(0)))
								lista.add(new LocationOccurrence(s.replace(".", "").replace(",", ""), index));
						}
					}
				}
			}
		} else {
			String wordsPrePattern = "";
			boolean first = true;
			for (String pre : filter) {
				if (first) {
					wordsPrePattern = pre;
					first = false;
				} else
					wordsPrePattern = wordsPrePattern.concat(("|").concat(pre));
			}
			String patternString = patternStart.concat(wordsPrePattern).concat(patternEnd);
			Pattern p = Pattern.compile(patternString,
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = p.matcher(plainText);
			while (matcher.find()) {
				String word = matcher.group(5);
				if (!languaje.isStopWord(word))
					lista.add(new LocationOccurrence(word, matcher.start()));
			}
		}
		return lista;
	}

}
