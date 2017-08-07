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
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bedmanagement.Bed;
import org.openmrs.module.bedmanagement.BedDetails;
import org.openmrs.module.bedmanagement.BedManagementService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

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

    private CloseVisitTask closeVisitTask;
    private Concept firstStageSurgicalOutcomesConcept, followUpSurgicalOutcomesConcept, finalValidationOutcomesConcept, networkFollowupConcept;
    private Visit visit;

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
        visit = new Visit(1000);
        visit.setVisitType(new VisitType("First State Validation", "for initial assessments"));
        visit.setPatient(new Patient(2));
        visit.setStartDatetime(new Date());

        when(visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false)).thenReturn(Arrays.asList(visit));
        closeVisitTask = new CloseVisitTask();
    }

    @Test
    public void shouldNotCloseTheOpenVisitIfObsFilledAgainstAnyOfTheOutcomeConcepts() throws Exception {
        Collection<BahmniObservation> latestObsByVisit = new ArrayList<BahmniObservation>();
        when(bahmniObsService.getLatestObsByVisit(visit, Arrays.asList(firstStageSurgicalOutcomesConcept, followUpSurgicalOutcomesConcept, finalValidationOutcomesConcept), null, true)).thenReturn(latestObsByVisit);

        closeVisitTask.execute();

        verify(conceptService).getConcept("FSTG, Outcomes for 1st stage surgical validation");
        verify(conceptService).getConcept("FUP, Outcomes for follow-up surgical validation");
        verify(conceptService).getConcept("FV, Outcomes FV");
        verify(visitService).getVisits(null, null, null, null, null, null, null, null, null, false, false);
        verify(bahmniObsService).getLatestObsByVisit(visit, Arrays.asList(firstStageSurgicalOutcomesConcept, followUpSurgicalOutcomesConcept, finalValidationOutcomesConcept), null, true);
        verify(visitService, never()).endVisit(visit, eq(any(Date.class)));
    }

    @Test
    public void shouldCloseTheOpenVisitIfObsFilledAgainstAnyOfTheOutcomeConcepts() throws Exception {
        BahmniObservation bahmniObservation = new BahmniObservation();
        bahmniObservation.setConcept(new EncounterTransaction.Concept("uuid"));
        Collection<BahmniObservation> latestObsByVisit = new ArrayList<BahmniObservation>(Arrays.asList(bahmniObservation));
        when(bahmniObsService.getLatestObsByVisit(visit, Arrays.asList(firstStageSurgicalOutcomesConcept, followUpSurgicalOutcomesConcept, finalValidationOutcomesConcept), null, true)).thenReturn(latestObsByVisit);
        Date now = new Date();
        whenNew(Date.class).withNoArguments().thenReturn(now);

        closeVisitTask.execute();

        verify(conceptService).getConcept("FSTG, Outcomes for 1st stage surgical validation");
        verify(conceptService).getConcept("FUP, Outcomes for follow-up surgical validation");
        verify(conceptService).getConcept("FV, Outcomes FV");
        verify(visitService).getVisits(null, null, null, null, null, null, null, null, null, false, false);
        verify(bahmniObsService).getLatestObsByVisit(visit, Arrays.asList(firstStageSurgicalOutcomesConcept, followUpSurgicalOutcomesConcept, finalValidationOutcomesConcept), null, true);
        verify(visitService).endVisit(eq(visit), isA(Date.class));
    }


    private Concept setUpConceptData(Integer conceptId, String name) {
        Concept concept = new Concept();
        concept.setId(conceptId);
        ConceptName conceptName = new ConceptName();
        conceptName.setName(name);
        conceptName.setLocale(new Locale("en", "GB"));
        concept.setNames(Arrays.asList(conceptName));
        return concept;
    }

    @Test
    public void shouldCloseTheVisitIfVisitTypeIsHospitalAndProgramWorkFlowStateIsNetWorkFollowup() throws Exception {
        VisitType visitType = new VisitType("Hospital", "this visitType is used for ipd section");
        Patient patient = new Patient();
        Visit hospitalVisit = new Visit(patient, visitType, new Date());
        when(visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false)).thenReturn(Arrays.asList(hospitalVisit));
        ProgramWorkflowState networkFollowUpProgramWorkflowState = new ProgramWorkflowState(1);
        when(programWorkflowService.getProgramWorkflowStatesByConcept(networkFollowupConcept)).thenReturn(Arrays.asList(networkFollowUpProgramWorkflowState));
        PatientState patientState = new PatientState(1);
        patientState.setState(networkFollowUpProgramWorkflowState);
        Set<PatientState> patientStates = new TreeSet<PatientState>();
        patientStates.add(patientState);
        PatientProgram patientProgram = new PatientProgram(1);
        patientProgram.setStates(patientStates);
        when(programWorkflowService.getPatientPrograms(eq(hospitalVisit.getPatient()), any(Program.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(patientProgram));
        Date now = new Date();
        whenNew(Date.class).withNoArguments().thenReturn(now);

        closeVisitTask.execute();


        verify(conceptService).getConcept("Network Follow-up");
        verify(visitService).getVisits(null, null, null, null, null, null, null, null, null, false, false);
        verify(programWorkflowService).getProgramWorkflowStatesByConcept(networkFollowupConcept);
        verify(programWorkflowService).getPatientPrograms(eq(hospitalVisit.getPatient()), any(Program.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), eq(false));
        verify(visitService).endVisit(eq(hospitalVisit), isA(Date.class));
    }

    @Test
    public void shouldNotCloseTheVisitIfVisitTypeIsHospitalAndProgramWorkFlowStateIsNotNetWorkFollowup() throws Exception {
        VisitType visitType = new VisitType("Hospital", "this visitType is used for ipd section");
        Patient patient = new Patient();
        Visit hospitalVisit = new Visit(patient, visitType, new Date());
        when(visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false)).thenReturn(Arrays.asList(hospitalVisit));
        ProgramWorkflowState networkFollowUpProgramWorkflowState = new ProgramWorkflowState(1);
        ProgramWorkflowState identificationProgramWorkflowState = new ProgramWorkflowState(2);
        when(programWorkflowService.getProgramWorkflowStatesByConcept(networkFollowupConcept)).thenReturn(Arrays.asList(networkFollowUpProgramWorkflowState));
        PatientState patientState = new PatientState(1);
        patientState.setState(identificationProgramWorkflowState);
        Set<PatientState> patientStates = new TreeSet<PatientState>();
        patientStates.add(patientState);
        PatientProgram patientProgram = new PatientProgram(1);
        patientProgram.setStates(patientStates);
        when(programWorkflowService.getPatientPrograms(eq(hospitalVisit.getPatient()), any(Program.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(patientProgram));
        Date now = new Date();
        whenNew(Date.class).withNoArguments().thenReturn(now);

        closeVisitTask.execute();

        verify(conceptService).getConcept("Network Follow-up");
        verify(visitService).getVisits(null, null, null, null, null, null, null, null, null, false, false);
        verify(programWorkflowService).getProgramWorkflowStatesByConcept(networkFollowupConcept);
        verify(programWorkflowService).getPatientPrograms(eq(hospitalVisit.getPatient()), any(Program.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), eq(false));
    }

    @Test
    public void shouldCloseTheVisitWhenPatientIsNotAssaignedToBedAndTheProgramStateIsNetworkFollowUp() throws Exception {
        VisitType visitType = new VisitType("Hospital", "this visitType is used for ipd section");
        Patient patient = new Patient();
        Visit hospitalVisit = new Visit(patient, visitType, new Date());
        when(visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false)).thenReturn(Arrays.asList(hospitalVisit));
        ProgramWorkflowState networkFollowUpProgramWorkflowState = new ProgramWorkflowState(1);
        ProgramWorkflowState identificationProgramWorkflowState = new ProgramWorkflowState(2);
        when(programWorkflowService.getProgramWorkflowStatesByConcept(networkFollowupConcept)).thenReturn(Arrays.asList(networkFollowUpProgramWorkflowState));
        PatientState patientState = new PatientState(1);
        patientState.setState(networkFollowUpProgramWorkflowState);
        Set<PatientState> patientStates = new TreeSet<PatientState>();
        patientStates.add(patientState);
        PatientProgram patientProgram = new PatientProgram(1);
        patientProgram.setStates(patientStates);
        when(programWorkflowService.getPatientPrograms(eq(hospitalVisit.getPatient()), any(Program.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(patientProgram));
        when(bedManagementService.getBedAssignmentDetailsByPatient(patient)).thenReturn(null);
        Date now = new Date();
        whenNew(Date.class).withNoArguments().thenReturn(now);
        closeVisitTask.execute();

        verify(conceptService).getConcept("Network Follow-up");
        verify(visitService).getVisits(null, null, null, null, null, null, null, null, null, false, false);
        verify(programWorkflowService).getProgramWorkflowStatesByConcept(networkFollowupConcept);
        verify(programWorkflowService).getPatientPrograms(eq(hospitalVisit.getPatient()), any(Program.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), eq(false));
        verify(bedManagementService).getBedAssignmentDetailsByPatient(patient);
        verify(visitService).endVisit(eq(hospitalVisit), isA(Date.class));
    }

    @Test
    public void shouldNotCloseTheVisitWhenPatientIsAssaignedToBedAndTheProgramStateIsNetworkFollowUp() throws Exception {
        VisitType visitType = new VisitType("Hospital", "this visitType is used for ipd section");
        Patient patient = new Patient();
        Visit hospitalVisit = new Visit(patient, visitType, new Date());
        when(visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false)).thenReturn(Arrays.asList(hospitalVisit));
        ProgramWorkflowState networkFollowUpProgramWorkflowState = new ProgramWorkflowState(1);
        ProgramWorkflowState identificationProgramWorkflowState = new ProgramWorkflowState(2);
        when(programWorkflowService.getProgramWorkflowStatesByConcept(networkFollowupConcept)).thenReturn(Arrays.asList(networkFollowUpProgramWorkflowState));
        PatientState patientState = new PatientState(1);
        patientState.setState(networkFollowUpProgramWorkflowState);
        Set<PatientState> patientStates = new TreeSet<PatientState>();
        patientStates.add(patientState);
        PatientProgram patientProgram = new PatientProgram(1);
        patientProgram.setStates(patientStates);
        when(programWorkflowService.getPatientPrograms(eq(hospitalVisit.getPatient()), any(Program.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(patientProgram));
        BedDetails bedDetails = new BedDetails();
        bedDetails.setBed(new Bed());
        ArrayList<Patient> patients = new ArrayList<>();
        patients.add(patient);
        bedDetails.setPatients(patients);
        when(bedManagementService.getBedAssignmentDetailsByPatient(patient)).thenReturn(bedDetails);
        Date now = new Date();
        whenNew(Date.class).withNoArguments().thenReturn(now);
        closeVisitTask.execute();

        verify(conceptService).getConcept("Network Follow-up");
        verify(visitService).getVisits(null, null, null, null, null, null, null, null, null, false, false);
        verify(programWorkflowService).getProgramWorkflowStatesByConcept(networkFollowupConcept);
        verify(programWorkflowService).getPatientPrograms(eq(hospitalVisit.getPatient()), any(Program.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), eq(false));
        verify(bedManagementService).getBedAssignmentDetailsByPatient(patient);
        verify(visitService, times(0)).endVisit(eq(hospitalVisit), isA(Date.class));
    }
}