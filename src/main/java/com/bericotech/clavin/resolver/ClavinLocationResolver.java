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
 * ClavinLocationResolver.java
 *
 *###################################################################*/

package com.bericotech.clavin.resolver;

import com.bericotech.clavin.ClavinException;
import com.bericotech.clavin.extractor.LocationOccurrence;
import com.bericotech.clavin.gazetteer.CountryCode;
import com.bericotech.clavin.gazetteer.query.FuzzyMode;
import com.bericotech.clavin.gazetteer.query.Gazetteer;
import com.bericotech.clavin.gazetteer.query.QueryBuilder;
import com.bericotech.clavin.util.ListUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves location names into GeoName objects.
 *
 * Takes location names extracted from unstructured text documents by
 * {@link com.bericotech.clavin.extractor.LocationExtractor} and resolves them into the appropriate
 * geographic entities (as intended by the document's author based on
 * context) by finding the best match in a gazetteer.
 */
public class ClavinLocationResolver {
    /**
     * The default number of candidate matches to consider.
     */
    public static final int DEFAULT_MAX_HIT_DEPTH = 5;

    /**
     * The default context window to consider when resolving matches.
     */
    public static final int DEFAULT_MAX_CONTEXT_WINDOW = 5;

    /**
     * The Gazetteer.
     */
    private final Gazetteer gazetteer;

    /**
     * Set of demonyms to filter out from extracted location names.
     */
    private static HashSet<String> demonyms;

    /**
     * Create a new ClavinLocationResolver.
     * @param gazetteer the Gazetteer to query
     */
    public ClavinLocationResolver(final Gazetteer gazetteer) {
        this.gazetteer = gazetteer;
    }

    /**
     * Get the Gazetteer used by this resolver.
     * @return the configured gazetteer
     */
    public Gazetteer getGazetteer() {
        return gazetteer;
    }

    /**
     * Resolves the supplied list of location names into
     * {@link ResolvedLocation}s containing {@link com.bericotech.clavin.gazetteer.GeoName} objects
     * using the defaults for maxHitDepth and maxContentWindow.
     *
     * Calls {@link Gazetteer#getClosestLocations} on
     * each location name to find all possible matches, then uses
     * heuristics to select the best match for each by calling
     * {@link ClavinLocationResolver#pickBestCandidates}.
     *
     * @param locations          list of location names to be resolved
     * @param fuzzy              switch for turning on/off fuzzy matching
     * @return                   list of {@link ResolvedLocation} objects
     * @throws ClavinException   if an error occurs parsing the search terms
     **/
    public List<ResolvedLocation> resolveLocations(final List<LocationOccurrence> locations, final boolean fuzzy, final String text)
            throws ClavinException {
        return resolveLocations(locations, DEFAULT_MAX_HIT_DEPTH, DEFAULT_MAX_CONTEXT_WINDOW, fuzzy, text);
    }

    /**
     * Resolves the supplied list of location names into
     * {@link ResolvedLocation}s containing {@link com.bericotech.clavin.gazetteer.GeoName} objects.
     *
     * Calls {@link Gazetteer#getClosestLocations} on
     * each location name to find all possible matches, then uses
     * heuristics to select the best match for each by calling
     * {@link ClavinLocationResolver#pickBestCandidates}.
     *
     * @param locations          list of location names to be resolved
     * @param maxHitDepth        number of candidate matches to consider
     * @param maxContextWindow   how much context to consider when resolving
     * @param fuzzy              switch for turning on/off fuzzy matching
     * @return                   list of {@link ResolvedLocation} objects
     * @throws ClavinException   if an error occurs parsing the search terms
     **/
    @SuppressWarnings("unchecked")
    public List<ResolvedLocation> resolveLocations(final List<LocationOccurrence> locations, final int maxHitDepth,
            final int maxContextWindow, final boolean fuzzy, final String text) throws ClavinException {
        // are you forgetting something? -- short-circuit if no locations were provided
        if (locations == null || locations.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        QueryBuilder builder = new QueryBuilder()
                .maxResults(maxHitDepth)
                // translate CLAVIN 1.x 'fuzzy' parameter into NO_EXACT or OFF; it isn't
                // necessary, or desirable to support FILL for the CLAVIN resolution algorithm
                .fuzzyMode(fuzzy ? FuzzyMode.NO_EXACT : FuzzyMode.OFF)
                .includeHistorical(true);

        // initialize return object
        List<ResolvedLocation> bestCandidates = new ArrayList<ResolvedLocation>();
        
        int pos=0;

        if (maxHitDepth > 1) { // perform context-based heuristic matching
            // stores all possible matches for each location name
            List<List<ResolvedLocation>> allCandidates = new ArrayList<List<ResolvedLocation>>();

            // loop through all the location names
            for (LocationOccurrence location : locations) {
            	if (location.getPosition()>=pos) {
	                // get all possible matches
	                List<ResolvedLocation> candidates = gazetteer.getClosestLocations(builder.location(location).build());
	
	                // if we found some possible matches, save them
	                if (candidates.size() > 0) {
	                	ResolvedLocation bestCandidate = pickBestCandidate(candidates,text);
	                	if (bestCandidate!=null) {
	                			pos= bestCandidate.getPosition()+bestCandidate.getMatchedName().length();
	                			bestCandidates.add(bestCandidate);
	                	}
	                }
            	}
            }

            return bestCandidates;
        } else { // use no heuristics, simply choose matching location with greatest population
            // initialize return object
            List<ResolvedLocation> resolvedLocations = new ArrayList<ResolvedLocation>();

            // stores possible matches for each location name
            List<ResolvedLocation> candidateLocations;

            // loop through all the location names
            for (LocationOccurrence location : locations) {
                // choose the top-sorted candidate for each individual
                // location name
                candidateLocations = gazetteer.getClosestLocations(builder.location(location).build());

                // if a match was found, add it to the return list
                if (candidateLocations.size() > 0) {
                    resolvedLocations.add(candidateLocations.get(0));
                }
            }

            return resolvedLocations;
        }
    }

    
    private ResolvedLocation pickBestCandidate(final List<ResolvedLocation> candidates, String text) {
    	ResolvedLocation bestCandidate = null;
    	Integer length=0;
    	
    	for (ResolvedLocation location : candidates) {
    		String nameLocation = location.getMatchedName().toLowerCase();
    		if (nameLocation.length()>=length) {
    			//Posicion de la palabra dentro de la localizacion
    			int secIndex = nameLocation.indexOf(location.getLocation().getText().toLowerCase());
    			if (text.indexOf(nameLocation, location.getLocation().getPosition()-secIndex)>-1) {
    				length=nameLocation.length();
    				bestCandidate = location;
    				bestCandidate.getLocation().setText(location.getMatchedName());
    				bestCandidate.setPosition(location.getLocation().getPosition()-secIndex);
    				if (location.getGeoname().getName().equals(nameLocation))
    					break;
    			}
    		}
    	}
        return bestCandidate;
    }

}
