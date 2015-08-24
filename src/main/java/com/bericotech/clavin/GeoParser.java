package com.bericotech.clavin;

import com.bericotech.clavin.extractor.LocationExtractor;
import com.bericotech.clavin.extractor.LocationOccurrence;
import com.bericotech.clavin.gazetteer.CountryCode;
import com.bericotech.clavin.gazetteer.query.Gazetteer;
import com.bericotech.clavin.resolver.ClavinLocationResolver;
import com.bericotech.clavin.resolver.ResolvedLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * GeoParser.java
 *
 *###################################################################*/

/**
 * Performs geoparsing of documents; extracts location names from unstructured
 * text and resolves them against a gazetteer to produce structured geo data.
 *
 * Main API entry point for CLAVIN -- simply instantiate this class and call the
 * {@link GeoParser#parse} method on your text string.
 *
 */
public class GeoParser {

	private static final Logger logger = LoggerFactory
			.getLogger(GeoParser.class);

	// entity extractor to find location names in text
	private LocationExtractor extractor;

	// resolver to match location names against gazetteer records
	private ClavinLocationResolver resolver;

	// the maximum hit depth for CLAVIN searches
	private int maxHitDepth;

	// the maximum context window for CLAVIN searches
	private int maxContextWindow;

	// switch controlling use of fuzzy matching
	private final boolean fuzzy;

	/**
	 * Default constructor.
	 *
	 * @param extractor
	 *            extracts location names from text
	 * @param gazetteer
	 *            resolves location names to gazetteer
	 * @param maxHitDepth
	 *            the maximum hit depth
	 * @param maxContextWindow
	 *            the maximum context window
	 * @param fuzzy
	 *            switch to turn on/off fuzzy matching
	 */
	public GeoParser(LocationExtractor extractor, Gazetteer gazetteer,
			int maxHitDepth, int maxContextWindow, boolean fuzzy) {
		this.extractor = extractor;
		this.resolver = new ClavinLocationResolver(gazetteer);
		this.maxHitDepth = maxHitDepth;
		this.maxContextWindow = maxContextWindow;
		this.fuzzy = fuzzy;
	}

	/**
	 * Takes an unstructured text document (as a String), extracts the location
	 * names contained therein, and resolves them into geographic entities
	 * representing the best match for those location names.
	 *
	 * @param inputText
	 *            unstructured text to be processed
	 * @return list of geo entities resolved from text
	 * @throws Exception
	 */
	public List<ResolvedLocation> parse(String inputText)
			throws Exception {

		logger.trace("input: {}", inputText);

		// first, extract location names from the text
		List<LocationOccurrence> locationNames = extractor
				.extractLocationNames(inputText);

		logger.trace("extracted: {}", locationNames);

		// then, resolve the extracted location names against a
		// gazetteer to produce geographic entities representing the
		// locations mentioned in the original text
		Map<String, List<ResolvedLocation>> resolvedLocations = resolver
				.resolveLocations(locationNames, maxHitDepth, maxContextWindow,
						fuzzy, inputText.toLowerCase());

		logger.trace("resolved: {}", resolvedLocations);

		List<ResolvedLocation> lugaresEncontrados = new ArrayList<ResolvedLocation>();

		Map<CountryCode, Integer> countCountry = new HashMap();
		CountryCode maxCountry;
		Integer maxCount = 0;
		// Contamos las apariciones de cada pais
		for (String location : resolvedLocations.keySet()) {
			for (ResolvedLocation loc : resolvedLocations.get(location)) {
				CountryCode code = loc.getGeoname().getPrimaryCountryCode();
				Integer num = countCountry.getOrDefault(code, 0) + 1;
				countCountry.put(code, num);
				if (maxCount < num) {
					maxCount = num;
					maxCountry = code;
				}
			}
		}
		// Seleccionamos el pais con mas apraciones o los que mas en caso de
		// empate
		List<CountryCode> countries = new ArrayList<CountryCode>();
		for (CountryCode code : countCountry.keySet()) {
			if (countCountry.get(code) >= maxCount) {
				countries.add(code);
			}
		}

		// Display the ResolvedLocations found for the location names
		for (String location : resolvedLocations.keySet()) {
			List<ResolvedLocation> posiblesLugares = resolvedLocations
					.get(location);
			if (posiblesLugares.size() == 1) {
				lugaresEncontrados.add(posiblesLugares.get(0));
			} else {
				List<ResolvedLocation> lugaresDelPais = new ArrayList<ResolvedLocation>();
				for (ResolvedLocation resolvedLocation : posiblesLugares) {
					if (countries.contains(resolvedLocation.getGeoname()
							.getPrimaryCountryCode())) {
						lugaresDelPais.add(resolvedLocation);
					}
				}
				if (lugaresDelPais.size() == 1) {
					lugaresEncontrados.add(lugaresDelPais.get(0));
				} else if (lugaresDelPais.size() > 1) {
					lugaresEncontrados.add(lugaresDelPais.get(0));
				} else {
					ResolvedLocation elegido = null;
					for (ResolvedLocation resolvedLocation : posiblesLugares) {
						if (elegido == null) {
							elegido = resolvedLocation;
						} else {
							if (elegido.getGeoname().isAncestorOf(
									resolvedLocation.getGeoname())) {

							} else if (elegido.getGeoname().isDescendantOf(
									resolvedLocation.getGeoname())) {
								elegido = resolvedLocation;
							} else if (elegido.getGeoname().getFeatureClass()
									.ordinal() >= resolvedLocation.getGeoname()
									.getFeatureClass().ordinal()) {
								if (elegido.getGeoname().getFeatureClass()
										.ordinal() > resolvedLocation
										.getGeoname().getFeatureClass()
										.ordinal()) {
									elegido = resolvedLocation;
								} else {
									if (elegido.getGeoname().getFeatureCode()
											.ordinal() > resolvedLocation
											.getGeoname().getFeatureCode()
											.ordinal()) {

									}
								}
							}
						}
					}
					lugaresEncontrados.add(elegido);
				}
			}
		}

		return lugaresEncontrados;
	}

	public ClavinLocationResolver getResolver() {
		return resolver;
	}

	public void setResolver(ClavinLocationResolver resolver) {
		this.resolver = resolver;
	}

}
