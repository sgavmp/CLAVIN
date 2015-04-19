package com.bericotech.clavin.extractor;

import java.util.ArrayList;
import java.util.List;

public class AlphaExtractor implements LocationExtractor {

	private static String[] filter_term = {"ha","al","de","del","el","en","la","las","lo","los","no","si","un","una"};
	private List<String> filter;
	
	
	
	public AlphaExtractor() {
		//Construimos la lista de terminos a filtrar
		filter = new ArrayList<String>();
		for (String s : filter_term) {
			filter.add(s);
		}
	}



	@Override
	public List<LocationOccurrence> extractLocationNames(String plainText) {
		List<LocationOccurrence> lista = new ArrayList<LocationOccurrence>();
		int index=0;
		for (String s : plainText.split(" ")) {
			if (s.length()>1) {
				index=plainText.indexOf(s,index);
				if (!filter.contains(s.toLowerCase())) {
					if (Character.isUpperCase(s.charAt(0)))
						lista.add(new LocationOccurrence(s, index));
				}
			}
		}
		return lista;
	}

	
}
