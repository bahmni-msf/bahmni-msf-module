package org.bahmni.module.bahmnimsf.util;

import org.bahmni.module.bahmnimsf.model.ProgramEncounter;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EncounterMapper {
    public Collection<ProgramEncounter> map(Collection<Encounter> encounters) {
        if (encounters == null) return null;
        List<ProgramEncounter> programEncounters = new ArrayList<ProgramEncounter>();
        for (Encounter encounter : encounters) {
            if (encounter != null) {
                ProgramEncounter programEncounter = new ProgramEncounter();
                programEncounter.setEncounterDateTime(encounter.getEncounterDatetime());
                programEncounter.setEncounterTypeName(encounter.getEncounterType().getName());
                programEncounter.setVisitTypeName(encounter.getVisit().getVisitType().getName());
                programEncounter.setLocationName(encounter.getLocation().getName());
                Set<EncounterProvider> activeEncounterProviders = encounter.getEncounterProviders();
                programEncounter.setProviderName(activeEncounterProviders.iterator().next().getProvider().getName());
                programEncounters.add(programEncounter);
            }
        }
        return programEncounters;
    }
}
