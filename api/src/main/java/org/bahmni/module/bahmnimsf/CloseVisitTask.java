package org.bahmni.module.bahmnimsf;

import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.openmrs.Visit;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bedmanagement.BedDetails;
import org.openmrs.module.bedmanagement.service.BedManagementService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class CloseVisitTask extends AbstractTask {

    private String HOSPITAL_VISIT_TYPE = "Hospital";

    @Override
    public void execute() {
        VisitService visitService = Context.getVisitService();
        List<Visit> openVisits = visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false);
        ConceptService conceptService = Context.getConceptService();
        String[] outcomeConcepts = new String[]{"FSTG, Outcomes for 1st stage surgical validation", "FUP, Outcomes for follow-up surgical validation", "FV, Outcomes FV"};
        for (Visit openVisit : openVisits) {
            ProgramWorkflowService programWorkflowService = Context.getService(ProgramWorkflowService.class);
            PatientProgram activePatientProgram = getActivePatientProgramForPatient(openVisit.getPatient(), programWorkflowService);
            if (activePatientProgram != null) {
                if (isHospitalVisit(openVisit)) {
                    if (isNotInNetworkFollowupStateOrBedIsAssigned(conceptService, programWorkflowService, activePatientProgram, openVisit.getPatient())) {
                        continue;
                    }
                } else {
                    if (hasNoOutcomesFilledInFormsFor(outcomeConcepts, conceptService, openVisit)) {
                        continue;
                    }
                }
            }
            visitService.endVisit(openVisit, new Date());
        }
    }

    private boolean hasNoOutcomesFilledInFormsFor(String[] outcomeConcepts, ConceptService conceptService, Visit openVisit) {
        ArrayList<Concept> concepts = new ArrayList<>();
        for (String outcomeConcept : outcomeConcepts) {
            concepts.add(conceptService.getConcept(outcomeConcept));
        }
        BahmniObsService bahmniObsService = Context.getService(BahmniObsService.class);
        Collection<BahmniObservation> latestObsByVisit = bahmniObsService.getLatestObsByVisit(openVisit, concepts, null, true);
        return latestObsByVisit.size() == 0;
    }

    private PatientProgram getActivePatientProgramForPatient(Patient patient, ProgramWorkflowService programWorkflowService) {
        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(patient, null, null, null, null, null, false);
        PatientProgram activePatientProgram = null;
        for (PatientProgram patientProgram : patientPrograms) {
            if (patientProgram.getDateCompleted() == null) {
                activePatientProgram = patientProgram;
                break;
            }
        }
        return activePatientProgram;
    }

    private boolean isNotInNetworkFollowupStateOrBedIsAssigned(ConceptService conceptService, ProgramWorkflowService programWorkflowService, PatientProgram activePatientProgram, Patient patient) {
        Concept networkFollowupConcept = conceptService.getConcept("Network Follow-up");
        List<ProgramWorkflowState> programWorkflowStatesByConcept = programWorkflowService.getProgramWorkflowStatesByConcept(networkFollowupConcept);
        ProgramWorkflowState programWorkflowStateForNetWorkFollowUp = programWorkflowStatesByConcept.get(0);
        PatientState patientState = activePatientProgram.getCurrentState(null);
        BedManagementService bedManagementService = Context.getService(BedManagementService.class);
        ProgramWorkflowState patientCurrentWorkFlowState = patientState.getState();
        BedDetails bedAssignmentDetailsByPatient = bedManagementService.getBedAssignmentDetailsByPatient(patient);
        return !(patientCurrentWorkFlowState.equals(programWorkflowStateForNetWorkFollowUp) && patientState.getEndDate() == null && bedAssignmentDetailsByPatient == null);
    }

    private boolean isHospitalVisit(Visit openVisit) {
        return openVisit.getVisitType().getName().equals(HOSPITAL_VISIT_TYPE);
    }
}
