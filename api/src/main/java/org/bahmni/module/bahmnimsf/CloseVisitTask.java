package org.bahmni.module.bahmnimsf;

import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.openmrs.Concept;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class CloseVisitTask extends AbstractTask {

    @Override
    public void execute() {
        VisitService visitService = Context.getVisitService();
        ConceptService conceptService = Context.getConceptService();
        Concept firstStageSurgicalOutcomesConcept = conceptService.getConcept("PMIPA, Outcomes for 1st stage surgical validation");
        Concept validOutcomeConcept = conceptService.getConcept("Valid");
        List<Visit> openVisits = visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false);
        for (Visit openVisit : openVisits) {
            BahmniObsService bahmniObsService = Context.getService(BahmniObsService.class);
            Collection<BahmniObservation> latestObsByVisit = bahmniObsService.getLatestObsByVisit(openVisit, Arrays.asList(firstStageSurgicalOutcomesConcept), null, true);
            for (BahmniObservation bahmniObservation : latestObsByVisit) {
                EncounterTransaction.Concept obsValue = (EncounterTransaction.Concept) bahmniObservation.getValue();
                if (obsValue.getUuid().equals(validOutcomeConcept.getUuid())) {
                    visitService.endVisit(openVisit, new Date());
                }
            }
        }
    }
}
