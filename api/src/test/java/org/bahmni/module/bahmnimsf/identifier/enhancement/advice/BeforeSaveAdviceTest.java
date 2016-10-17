package org.bahmni.module.bahmnimsf.identifier.enhancement.advice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.*;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;


@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class BeforeSaveAdviceTest {

    @Mock
    private ConceptService conceptService;

    @Mock
    private AdministrationService administrationService;

    private Method method;
    private BeforeSaveAdvice beforeSaveAdvice;
    Patient patient;

    @Before
    public void setup() throws NoSuchMethodException {
        PowerMockito.mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        Concept concept = setUpConceptData();
        when(conceptService.getConcept("100")).thenReturn(concept);
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.when(Context.getLocale()).thenReturn(defaultLocale);
        method = this.getClass().getMethod("savePatient");

        beforeSaveAdvice = new BeforeSaveAdvice();
        patient = setUpPatientData();

        setupConceptSource("Abbreviation", concept);
    }

    @Test
    public void shouldUpdateThePatientIdentiferWithCountryCodeAndGender(){
        Object output = new Object();
        Object[] input= {patient};
        beforeSaveAdvice.before(method, input, output);
        assertEquals("JO100001M", patient.getPatientIdentifier().getIdentifier());
    }


    public void savePatient(){
        // This is need for testing, As we can't mock Method class.
    }

    private Concept setUpConceptData() {
        Concept concept = new Concept();
        concept.setId(100);
        ConceptName conceptName = new ConceptName();
        conceptName.setName("Jordanian");
        conceptName.setLocale(new Locale("en", "GB"));
        concept.setNames(Arrays.asList(conceptName));
        return concept;
    }

    private void setupConceptSource(String conceptSourceDictionary, Concept concept) {
        ConceptSource source = new ConceptSource();
        source.setName(conceptSourceDictionary);
        ConceptMap conceptMap = new ConceptMap(new ConceptReferenceTerm(source, "JO", "JO"), new ConceptMapType());
        concept.setConceptMappings(Arrays.asList(conceptMap));
    }

    private Patient setUpPatientData() {
        Patient patient = new Patient();
        patient.setGender("M");
        PatientIdentifier patientIdentifier = new PatientIdentifier("100001", new PatientIdentifierType(), new Location());
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