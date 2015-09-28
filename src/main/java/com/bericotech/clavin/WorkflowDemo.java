package com.bericotech.clavin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.bericotech.clavin.extractor.AlphaExtractor;
import com.bericotech.clavin.extractor.LocationOccurrence;
import com.bericotech.clavin.gazetteer.CountryCode;
import com.bericotech.clavin.resolver.ResolvedLocation;
import com.bericotech.clavin.util.TextUtils;

/*#####################################################################
 * 
 * CLAVIN (Cartographic Location And Vicinity INdexer)
 * ---------------------------------------------------
 * 
 * Copyright (C) 2012-2013 Berico Technologies
 * http://clavin.bericotechnologies.com
 * 
 * ====================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * ====================================================================
 * 
 * WorkflowDemo.java
 * 
 *###################################################################*/

/**
 * Quick example showing how to use CLAVIN's capabilities.
 * 
 */
public class WorkflowDemo {

	/**
	 * Run this after installing & configuring CLAVIN to get a sense of how to
	 * use it.
	 * 
	 * @param args
	 *            not used
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// Instantiate the CLAVIN GeoParser
		GeoParser parser = GeoParserFactory.getDefault("IndexDirectory",
				new AlphaExtractor("in,at,the,of,from,to,on,and,the"), 50, 15, false);
		// GeoParser parser = GeoParserFactory.getDefault("IndexDirectory", 50,
		// 15 , false);

		// Unstructured text file about Somalia to be geoparsed
		File inputFile = new File(
				"src/test/resources/sample-docs/Somalia-doc.txt");

		Document doc = Jsoup
				.connect(
						"http://www.fwi.co.uk/poultry/high-path-h5n8-bird-flu-in-germany-for-first-time.htm")
				.get();
		// Document doc =
		// Jsoup.connect("http://news.yahoo.com/german-cull-ordered-h5n8-bird-flu-confirmed-204501608.html").get();
		String texto = doc.select(".article-body").text();
		// String texto = doc.select("div.book div.body").text();
		// Grab the contents of the text file as a String
		String inputString = TextUtils.fileToString(inputFile);

		// Parse location names in the text into geographic entities
		List<ResolvedLocation> resolvedLocations = parser
				.parse(texto,true);

		// LocationOccurrence test = new LocationOccurrence("el puerto de ", 0);
		// List<LocationOccurrence> listaTest = new ArrayList<>();
		// listaTest.add(test);
		// List<ResolvedLocation> resolvedLocations =
		// parser.getResolver().resolveLocations(listaTest, 50, 15, true,
		// texto.toLowerCase());
		
		for (ResolvedLocation resolvedLocation : resolvedLocations) {
			System.out.println(resolvedLocation);
		}
			
			

		// And we're done...
		System.out.println("\n\"That's all folks!\"");
	}
}
