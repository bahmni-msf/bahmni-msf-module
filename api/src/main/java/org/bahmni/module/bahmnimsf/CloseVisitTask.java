package org.bahmni.module.bahmnimsf;

import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.openmrs.Visit;
import org.openmrs.Concept;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bedmanagement.BedDetails;
import org.openmrs.module.bedmanagement.BedManagementService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class CloseVisitTask extends AbstractTask {

    private String HOSPITAL_VISIT_TYPE = "Hospital";

    @Override
    public void execute() {
        VisitService visitService = Context.getVisitService();
        ConceptService conceptService = Context.getConceptService();
        Concept firstStageSurgicalOutcomesConcept = conceptService.getConcept("FSTG, Outcomes for 1st stage surgical validation");
        Concept followUpSurgicalOutcomesConcept = conceptService.getConcept("FUP, Outcomes for follow-up surgical validation");
        Concept finalValidationOutcomesConcept = conceptService.getConcept("FV, Outcomes FV");
        Concept networkFollowupConcept = conceptService.getConcept("Network Follow-up");
        List<Visit> openVisits = visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false);
        for (Visit openVisit : openVisits) {
            if (openVisit.getVisitType().getName().equals(HOSPITAL_VISIT_TYPE)) {
                ProgramWorkflowService programWorkflowService = Context.getService(ProgramWorkflowService.class);
                BedManagementService bedManagementService = Context.getService(BedManagementService.class);
                List<ProgramWorkflowState> programWorkflowStatesByConcept = programWorkflowService.getProgramWorkflowStatesByConcept(networkFollowupConcept);
                ProgramWorkflowState programWorkflowStateForNetWorkFollowUp = programWorkflowStatesByConcept.get(0);
                List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(openVisit.getPatient(), null, null, null, new Date(), null, false);
                for (PatientProgram patientProgram : patientPrograms) {
                    PatientState patientState = patientProgram.getCurrentState(null);
                    ProgramWorkflowState patientCurrentWorkFlowState = patientState.getState();
                    BedDetails bedAssignmentDetailsByPatient = bedManagementService.getBedAssignmentDetailsByPatient(openVisit.getPatient());
                    if (patientCurrentWorkFlowState.equals(programWorkflowStateForNetWorkFollowUp) && patientState.getEndDate() == null && bedAssignmentDetailsByPatient == null) {
                        visitService.endVisit(openVisit, new Date());
                    }
                }
            } else {
                BahmniObsService bahmniObsService = Context.getService(BahmniObsService.class);
                Collection<BahmniObservation> latestObsByVisit = bahmniObsService.getLatestObsByVisit(openVisit, Arrays.asList(firstStageSurgicalOutcomesConcept, followUpSurgicalOutcomesConcept, finalValidationOutcomesConcept), null, true);
                if (latestObsByVisit.size() > 0) {
                    visitService.endVisit(openVisit, new Date());
                }
            }
        }
    }
}
