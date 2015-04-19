package com.bericotech.clavin;

import java.io.File;
import java.util.List;

import com.bericotech.clavin.extractor.AlphaExtractor;
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
     * Run this after installing & configuring CLAVIN to get a sense of
     * how to use it.
     * 
     * @param args              not used
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        
        // Instantiate the CLAVIN GeoParser
        GeoParser parser = GeoParserFactory.getDefault("./IndexDirectory", new AlphaExtractor(), 50, 15, false);
        
        // Unstructured text file about Somalia to be geoparsed
        File inputFile = new File("src/test/resources/sample-docs/Somalia-doc.txt");
        
        // Grab the contents of the text file as a String
        String inputString = TextUtils.fileToString(inputFile);
        
        // Parse location names in the text into geographic entities
        List<ResolvedLocation> resolvedLocations = parser.parse("El origen del problema fue el viaje extraescolar a Inglaterra de una alumna de siete años. Los padres confirmaron la asistencia de la menor, después se arrepintieron y lo comunicaron al centro oralmente, pero no por escrito, según asegura la madre. La dirección del colegio le obligó a pagar el billete de la menor, sin decirles la cantidad que debían abonar, y si no lo hacían, tanto la chica como su hermano no podrían continuar estudiando en el centro privado Yago School de Castilleja de la Cuesta, en Sevilla. Los padres demandaron al colegio y, esta semana, un juez de Sevilla ha ordenado al centro a indemnizar a los padres y a los dos menores porque “no fueron renovados ilegítimamente” en el centro escolar en julio de 2013. El juez ve la decisión del director “desproporcionada y abusiva”, “precedida de una actuación incorrecta de los padres”, según se lee en la sentencia dictada por el juez. Este es el segundo aviso de la justicia al centro sevillano. Su director está acusado de un delito contra los derechos fundamentales y las libertades públicas y será juzgado por negarse a escolarizar supuestamente al hijo de una pareja homosexual." +
"Estoy contenta, feliz con esta sentencia. Mis hijos, los dos, no solo la chica, fueron expulsados porque no pagué una factura que al principio me negué a pagar pero de la que después no me decían la cantidad para poder resolver el problema. Ha sido un abuso de poder, una manera forzada de condicionar la continuidad de mis hijos en el centro, de echarlos”, resume Montserrat Rodríguez, la madre. Según el juez, el colegio deberá pagar a los padres los 500 euros de la reserva de plazas de los dos menores para el siguiente curso, que no fueron devueltos, y 1.500 euros de indemnización a los hijos. “Me parece perfecto lo que ha determinado el juez. Que la indemnización vaya para mis hijos, que son los que lo han sufrido”, añade la madre." +
"El viaje de la menor, que es adoptada de origen chino, estaba previsto para junio, por lo que la polémica se generó a final de curso. La madre tuvo margen para matricular a sus dos hijos, —el segundo también adoptado de origen etíope— en otro centro en el que estudian “sin ningún problema”, detalla la Rodríguez. “Lo mismo que la actora [madre] no acredita haber comunicado al colegio, en el plazo establecido, que su hija cancelaba el viaje, tampoco el colegio acredita de forma alguna haber concretado la deuda que los padres hoy reclamantes debían, permitiendo a estos liquidarla”, detalla el juez en los fundamentos de derecho. “Si me hubieran dicho que debo 140 euros, los hubiera pagado y se habría resuelto”, asegura Rodríguez con la sentencia en la mano, en la que se matiza que los padres deben pagar esa cantidad al centro por la cancelación del viaje de la menor." +
"El mismo centro ya acumula un informe remitido al juzgado por la Inspección educativa, que concluyó que el menor hijo de la pareja homosexual no había “tenido igualdad de trato” para su admisión.");
        
        // Display the ResolvedLocations found for the location names
        for (ResolvedLocation resolvedLocation : resolvedLocations)
            System.out.println(resolvedLocation);
        
        // And we're done...
        System.out.println("\n\"That's all folks!\"");
    }
}
