package org.bahmni.module.bahmnimsf;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CloseVisitTask extends AbstractTask{

    @Override
    public void execute() {
        VisitService visitService = Context.getVisitService();
        ConceptService conceptService = Context.getConceptService();
        ObsService obsService = Context.getObsService();
        Concept firstStageSurgicalOutcomesConcept = conceptService.getConcept("PMIPA, Outcomes for 1st stage surgical validation");
        Concept validOutcomeConcept = conceptService.getConcept("Valid");
        List<Visit> openVisits = visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false);
        for (Visit openVisit : openVisits) {
            if (openVisit.getNonVoidedEncounters().size() > 0){
                List<Obs> observations = obsService.getObservations(Arrays.asList(openVisit.getPatient().getPerson()), openVisit.getNonVoidedEncounters(), Arrays.asList(firstStageSurgicalOutcomesConcept), null, null, null, null, null, null, null, null, false, null);
                for (Obs observation : observations) {
                    if (observation.getValueCoded().equals(validOutcomeConcept)) {
                        visitService.endVisit(openVisit, new Date());
                    }
                }
            }
        }
    }
}
