package com.bericotech.clavin.extractor;

import java.util.ArrayList;
import java.util.List;

import cue.lang.stop.StopWords;

public class AlphaExtractor implements LocationExtractor {

	private static String[] filter_term = { "ha", "al", "de", "del", "el",
			"en", "la", "las", "lo", "los", "no", "si", "un", "una" };
	private List<String> filter;

	public AlphaExtractor() {
		// Construimos la lista de terminos a filtrar
		filter = new ArrayList<String>();
		for (String s : filter_term) {
			filter.add(s);
		}
	}

	@Override
	public List<LocationOccurrence> extractLocationNames(String plainText) {
		StopWords languaje = StopWords.guess(plainText);
		if (languaje==null)
			languaje = StopWords.Spanish;
		List<LocationOccurrence> lista = new ArrayList<LocationOccurrence>();
		int index = 0;
		for (String s : plainText.split(" ")) {
			if (!languaje.isStopWord(s)) {
				if (s.length() > 1) {
					index = plainText.indexOf(s, index);
					if (!filter.contains(s.toLowerCase())) {
						if (Character.isUpperCase(s.charAt(0)))
							lista.add(new LocationOccurrence(s.replace(".", "")
									.replace(",", ""), index));
					}
				}
			}
		}
		return lista;
	}

}
