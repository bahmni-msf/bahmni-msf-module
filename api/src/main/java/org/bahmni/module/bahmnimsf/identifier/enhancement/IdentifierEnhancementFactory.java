package org.bahmni.module.bahmnimsf.identifier.enhancement;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.api.context.Context;

import java.util.Collection;

public class IdentifierEnhancementFactory {
    public static final String ABBREVIATION_CONCEPT_SOURCE = "Abbreviation";
    public static final String NATIONALITY_ATTRIBUTE = "nationality1";
    protected final Log log = LogFactory.getLog(this.getClass());

    public static void enhanceIdentifier(Patient patient) {
        PatientIdentifier identifier = patient.getPatientIdentifier();
        String patientIdentifier = identifier.getIdentifier().replaceAll("\\D+", "");
        String enhanceId = buildIdentifierWithCountryAndGender(patient, patientIdentifier);
        identifier.setIdentifier(enhanceId);
    }

    private static String buildIdentifierWithCountryAndGender(Patient patient, String patientIdentifier) {
        StringBuilder builder = new StringBuilder();
        builder.append(extractPrefixFromPatient(patient)).append(patientIdentifier).append(extractSuffixFromPatient(patient));
        return builder.toString();
    }

    private static String extractPrefixFromPatient(Patient patient) {
        PersonAttribute nationality1 = patient.getAttribute(NATIONALITY_ATTRIBUTE);
        Concept concept = Context.getConceptService().getConcept(nationality1.getValue());
        Collection<ConceptMap> conceptMappings = concept.getConceptMappings();
        for (ConceptMap conceptMapping : conceptMappings) {
            ConceptReferenceTerm conceptReferenceTerm = conceptMapping.getConceptReferenceTerm();
            if (conceptReferenceTerm.getConceptSource().getName().equals(ABBREVIATION_CONCEPT_SOURCE)) {
                return conceptReferenceTerm.getName();
            }
        }
        throw new RuntimeException("Country code for " + concept.getName().getName() + " not found.");
    }

    private static String extractSuffixFromPatient(Patient patient) {
        return patient.getGender();
    }
}
