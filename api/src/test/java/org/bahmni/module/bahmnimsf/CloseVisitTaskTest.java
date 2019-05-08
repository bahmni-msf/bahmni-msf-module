package org.bahmni.module.bahmnimsf;

import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bedmanagement.BedDetails;
import org.openmrs.module.bedmanagement.service.BedManagementService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class CloseVisitTaskTest {

    @Mock
    private ConceptService conceptService;

    @Mock
    private VisitService visitService;

    @Mock
    private BahmniObsService bahmniObsService;

    @Mock
    private ProgramWorkflowService programWorkflowService;

    @Mock
    private BedManagementService bedManagementService;

    @Mock
    private PatientProgram activePatientProgram;

    private CloseVisitTask closeVisitTask;
    private Concept firstStageSurgicalOutcomesConcept, followUpSurgicalOutcomesConcept, finalValidationOutcomesConcept, networkFollowupConcept;

    private Concept setUpConceptData(Integer conceptId, String name) {
        Concept concept = new Concept();
        concept.setId(conceptId);
        ConceptName conceptName = new ConceptName();
        conceptName.setName(name);
        conceptName.setLocale(new Locale("en", "GB"));
        concept.setNames(Arrays.asList(conceptName));
        return concept;
    }

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(Context.class);
        when(Context.getVisitService()).thenReturn(visitService);
        when(Context.getConceptService()).thenReturn(conceptService);
        when(Context.getService(BahmniObsService.class)).thenReturn(bahmniObsService);
        when(Context.getService(ProgramWorkflowService.class)).thenReturn(programWorkflowService);
        when(Context.getService(BedManagementService.class)).thenReturn(bedManagementService);
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.when(Context.getLocale()).thenReturn(defaultLocale);

        firstStageSurgicalOutcomesConcept = setUpConceptData(100, "FSTG, Outcomes for 1st stage surgical validation");
        followUpSurgicalOutcomesConcept = setUpConceptData(101, "FUP, Outcomes for follow-up surgical validation");
        finalValidationOutcomesConcept = setUpConceptData(102, "FV, Outcomes FV");
        networkFollowupConcept = setUpConceptData(103, "Network Follow-up");

        when(conceptService.getConcept("FSTG, Outcomes for 1st stage surgical validation")).thenReturn(firstStageSurgicalOutcomesConcept);
        when(conceptService.getConcept("FUP, Outcomes for follow-up surgical validation")).thenReturn(followUpSurgicalOutcomesConcept);
        when(conceptService.getConcept("FV, Outcomes FV")).thenReturn(finalValidationOutcomesConcept);
        when(conceptService.getConcept("Network Follow-up")).thenReturn(networkFollowupConcept);


        closeVisitTask = new CloseVisitTask();
    }

    @Test
    public void shouldCloseTheVisitWhenPatientDoesNotHaveAnyActivePrograms() {
        Visit visit = new Visit(1000);
        PatientProgram patientProgram = new PatientProgram();
        patientProgram.setId(1234);
        patientProgram.setDateCompleted(new Date());
        List<PatientProgram> patientPrograms = new ArrayList<>();
        patientPrograms.add(patientProgram);
        visit.setVisitType(new VisitType("First State Validation", "for initial assessments"));
        visit.setStartDatetime(new Date());
        visit.setPatient(new Patient(2));
        when(visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false)).thenReturn(Arrays.asList(visit));
        when(programWorkflowService.getPatientPrograms(isA(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false))).thenReturn(patientPrograms);

        closeVisitTask.execute();

        verify(visitService, times(1)).getVisits(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false), eq(false));
        verify(programWorkflowService, times(1)).getPatientPrograms(isA(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false));
        verify(visitService, times(1)).endVisit(eq(visit), isA(Date.class));
    }

    @Test
    public void shouldCloseTheVisitWhenThatVisitIsHopitalVisitAndProgramInNetworKFollowupStateAndHasNoBedAssignedForthePatient() {
        Patient patient = new Patient();
        patient.setUuid("Uuid");

        PatientState patientState = new PatientState();
        patientState.setId(1);
        patientState.setPatientProgram(new PatientProgram());

        List<ProgramWorkflowState> programWorkflowStates = new ArrayList<>();
        ProgramWorkflowState networkFollowUpProgramWorkflowState = new ProgramWorkflowState(1);
        networkFollowUpProgramWorkflowState.setName("Something");
        networkFollowUpProgramWorkflowState.setConcept(networkFollowupConcept);
        programWorkflowStates.add(networkFollowUpProgramWorkflowState);
        patientState.setState(networkFollowUpProgramWorkflowState);

        Visit hospitalVisit = new Visit(1001);
        hospitalVisit.setVisitType(new VisitType("Hospital", "visit"));
        hospitalVisit.setPatient(patient);

        List<PatientProgram> patientPrograms = new ArrayList<>();
        patientPrograms.add(activePatientProgram);

        List<Visit> visits = new ArrayList<>();
        visits.add(hospitalVisit);

        when(visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false)).thenReturn(visits);
        when(programWorkflowService.getPatientPrograms(isA(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false))).thenReturn(patientPrograms);
        when(programWorkflowService.getProgramWorkflowStatesByConcept(networkFollowupConcept)).thenReturn(programWorkflowStates);
        when(bedManagementService.getBedAssignmentDetailsByPatient(patient)).thenReturn(null);
        when(activePatientProgram.getDateCompleted()).thenReturn(null);
        when(activePatientProgram.getCurrentState(null)).thenReturn(patientState);

        closeVisitTask.execute();

        verify(visitService, times(1)).getVisits(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false), eq(false));
        verify(programWorkflowService, times(1)).getPatientPrograms(isA(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false));
        verify(visitService, times(1)).endVisit(eq(hospitalVisit), isA(Date.class));
    }

    @Test
    public void shouldNotCloseTheHospitalVisitWhenBedIsAssignedAndStateIsNetworkFollowup() {
        BedDetails bedDetails = new BedDetails();
        Patient patient = new Patient();
        patient.setUuid("Uuid");

        PatientState patientState = new PatientState();
        patientState.setId(1);
        patientState.setPatientProgram(new PatientProgram());

        List<ProgramWorkflowState> programWorkflowStates = new ArrayList<>();
        ProgramWorkflowState networkFollowUpProgramWorkflowState = new ProgramWorkflowState(1);
        networkFollowUpProgramWorkflowState.setName("Something");
        networkFollowUpProgramWorkflowState.setConcept(networkFollowupConcept);
        programWorkflowStates.add(networkFollowUpProgramWorkflowState);
        patientState.setState(networkFollowUpProgramWorkflowState);

        Visit hospitalVisit = new Visit(1001);
        hospitalVisit.setVisitType(new VisitType("Hospital", "visit"));
        hospitalVisit.setPatient(patient);

        List<PatientProgram> patientPrograms = new ArrayList<>();
        patientPrograms.add(activePatientProgram);

        List<Visit> visits = new ArrayList<>();
        visits.add(hospitalVisit);

        when(visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false)).thenReturn(visits);
        when(programWorkflowService.getPatientPrograms(isA(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false))).thenReturn(patientPrograms);
        when(programWorkflowService.getProgramWorkflowStatesByConcept(networkFollowupConcept)).thenReturn(programWorkflowStates);
        when(bedManagementService.getBedAssignmentDetailsByPatient(patient)).thenReturn(bedDetails);
        when(activePatientProgram.getDateCompleted()).thenReturn(null);
        when(activePatientProgram.getCurrentState(null)).thenReturn(patientState);

        closeVisitTask.execute();

        verify(visitService, times(1)).getVisits(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false), eq(false));
        verify(programWorkflowService, times(1)).getPatientPrograms(isA(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false));
        verify(visitService, times(0)).endVisit(eq(hospitalVisit), isA(Date.class));
    }

    @Test
    public void shouldNotCloseVisitWhenVisitIsHospitalAndWhenTheStateIsNotNetworkFollowup() {
        Patient patient = new Patient();
        patient.setUuid("Uuid");

        PatientState patientState = new PatientState();
        patientState.setId(1);
        patientState.setPatientProgram(new PatientProgram());

        List<ProgramWorkflowState> programWorkflowStates = new ArrayList<>();
        ProgramWorkflowState networkFollowUpProgramWorkflowState = new ProgramWorkflowState(1);
        networkFollowUpProgramWorkflowState.setName("Something");
        networkFollowUpProgramWorkflowState.setConcept(networkFollowupConcept);
        programWorkflowStates.add(networkFollowUpProgramWorkflowState);
        patientState.setState(new ProgramWorkflowState());

        Visit hospitalVisit = new Visit(1001);
        hospitalVisit.setVisitType(new VisitType("Hospital", "visit"));
        hospitalVisit.setPatient(patient);

        List<PatientProgram> patientPrograms = new ArrayList<>();
        patientPrograms.add(activePatientProgram);

        List<Visit> visits = new ArrayList<>();
        visits.add(hospitalVisit);

        when(visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false)).thenReturn(visits);
        when(programWorkflowService.getPatientPrograms(isA(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false))).thenReturn(patientPrograms);
        when(programWorkflowService.getProgramWorkflowStatesByConcept(networkFollowupConcept)).thenReturn(programWorkflowStates);
        when(bedManagementService.getBedAssignmentDetailsByPatient(patient)).thenReturn(null);
        when(activePatientProgram.getDateCompleted()).thenReturn(null);
        when(activePatientProgram.getCurrentState(null)).thenReturn(patientState);

        closeVisitTask.execute();

        verify(visitService, times(1)).getVisits(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false), eq(false));
        verify(programWorkflowService, times(1)).getPatientPrograms(isA(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false));
        verify(visitService, times(0)).endVisit(eq(hospitalVisit), isA(Date.class));

    }

    @Test
    public void shouldCloseTheVisitWhenOutcomesAreFilledAndIsNotAHospitalVisit() {
        Patient patient = new Patient();
        patient.setUuid("Uuid");
        Visit openVisit = new Visit();
        openVisit.setPatient(patient);
        openVisit.setVisitType(new VisitType("some", "visit"));
        List<Visit> visits = new ArrayList<>();
        visits.add(openVisit);

        PatientProgram patientProgram = new PatientProgram();
        patientProgram.setId(1234);
        patientProgram.setDateCompleted(null);
        List<PatientProgram> patientPrograms = new ArrayList<>();
        patientPrograms.add(patientProgram);
        Collection<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(new BahmniObservation());

        when(bahmniObsService.getLatestObsByVisit(eq(openVisit), any(Collection.class), eq(null), eq(true))).thenReturn(bahmniObservations);
        when(visitService.getVisits(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false), eq(false))).thenReturn(visits);
        when(programWorkflowService.getPatientPrograms(isA(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false))).thenReturn(patientPrograms);

        closeVisitTask.execute();

        verify(visitService, times(1)).getVisits(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false), eq(false));
        verify(programWorkflowService, times(1)).getPatientPrograms(isA(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false));
        verify(visitService, times(1)).endVisit(eq(openVisit), isA(Date.class));

    }

    @Test
    public void shouldNotCloseTheVisitWhenOutcomeConceptsAreNotFilled() {
        Patient patient = new Patient();
        patient.setUuid("Uuid");
        Visit openVisit = new Visit();
        openVisit.setPatient(patient);
        openVisit.setVisitType(new VisitType("some", "visit"));
        List<Visit> visits = new ArrayList<>();
        visits.add(openVisit);

        PatientProgram patientProgram = new PatientProgram();
        patientProgram.setId(1234);
        patientProgram.setDateCompleted(null);
        List<PatientProgram> patientPrograms = new ArrayList<>();
        patientPrograms.add(patientProgram);

        when(visitService.getVisits(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false), eq(false))).thenReturn(visits);
        when(programWorkflowService.getPatientPrograms(isA(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false))).thenReturn(patientPrograms);

        closeVisitTask.execute();

        verify(visitService, times(1)).getVisits(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false), eq(false));
        verify(programWorkflowService, times(1)).getPatientPrograms(isA(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false));
        verify(visitService, times(0)).endVisit(eq(openVisit), isA(Date.class));
    }
}