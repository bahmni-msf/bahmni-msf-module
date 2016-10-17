package org.bahmni.module.bahmnimsf.identifier.enhancement;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class IdentifierEnhancementFactoryTest {

    @Mock
    private ConceptService conceptService;

    @Mock
    private AdministrationService administrationService;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws IOException {
        PowerMockito.mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);
        when(Context.getAdministrationService()).thenReturn(administrationService);
    }

    @Test
    public void shouldAddNationalityCodeAsPrefixAndGenderAsSuffixToPatientIdentifier() {
        Patient patient = setUpPatientData();

        Concept concept = setUpConceptData();
        setupConceptSource("Abbreviation", concept);
        when(conceptService.getConcept("100")).thenReturn(concept);

        IdentifierEnhancementFactory.enhanceIdentifier(patient);

        assertEquals("SY100002M", patient.getPatientIdentifier().getIdentifier());
    }

    @Test
    public void shouldThrowRuntimeException() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Country code for Syrian not found");

        Patient patient = setUpPatientData();
        Concept concept = setUpConceptData();
        when(conceptService.getConcept("100")).thenReturn(concept);
        when(Context.getLocale()).thenReturn(new Locale("en", "GB"));

        IdentifierEnhancementFactory.enhanceIdentifier(patient);

    }

    private Concept setUpConceptData() {
        Concept concept = new Concept();
        concept.setId(100);
        ConceptName conceptName = new ConceptName();
        conceptName.setName("Syrian");
        conceptName.setLocale(new Locale("en", "GB"));
        concept.setNames(Arrays.asList(conceptName));
        return concept;
    }

    private void setupConceptSource(String conceptSourceDictionary, Concept concept) {
        ConceptSource source = new ConceptSource();
        source.setName(conceptSourceDictionary);
        ConceptMap conceptMap = new ConceptMap(new ConceptReferenceTerm(source, "SY", "SY"), new ConceptMapType());
        concept.setConceptMappings(Arrays.asList(conceptMap));
    }

    private Patient setUpPatientData() {
        Patient patient = new Patient();
        patient.setGender("M");
        PatientIdentifier patientIdentifier = new PatientIdentifier("100002", new PatientIdentifierType(), new Location());
        HashSet<PatientIdentifier> patientIdentifiers = new HashSet<PatientIdentifier>();
        patientIdentifiers.add(patientIdentifier);
        patient.setIdentifiers(patientIdentifiers);
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("nationality1");
        PersonAttribute personAttribute = new PersonAttribute(personAttributeType, "100");
        HashSet<PersonAttribute> personAttributes = new HashSet<PersonAttribute>();
        personAttributes.add(personAttribute);
        patient.setAttributes(personAttributes);
        return patient;
    }
}