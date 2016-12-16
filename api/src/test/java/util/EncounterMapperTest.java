package util;

import org.bahmni.module.bahmnimsf.util.EncounterMapper;
import org.bahmni.module.bahmnimsf.model.ProgramEncounter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class EncounterMapperTest {
    private Collection<Encounter> encounters;
    private Date encounterDatetime = new Date();
    private EncounterMapper encounterMapper = new EncounterMapper();

    @Before
    public void setUp() throws Exception {
        encounters = new ArrayList<Encounter>();
        mockStatic(Context.class);
    }

    private Encounter setupEncounter() {
        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(encounterDatetime);
        encounter.setEncounterType(new EncounterType("Consultation", "Consultation"));
        Visit visit = new Visit();
        visit.setVisitType(new VisitType("First Stage Validation", "First Stage Validation"));
        encounter.setVisit(visit);
        Location location = new Location();
        location.setName("Validation");
        encounter.setLocation(location);
        Provider provider = new Provider();
        provider.setName("Data Admin");
        when(Context.getAuthenticatedUser()).thenReturn(new User());
        encounter.setProvider(new EncounterRole(), provider);
        return encounter;
    }

    @Test
    public void shouldMapEncountersIntoProgramEncounters() {
        encounters.add(setupEncounter());
        Collection<ProgramEncounter> programEncounters = encounterMapper.map(encounters);
        assertNotNull(programEncounters);
        assertEquals(1, programEncounters.size());
        ProgramEncounter programEncounter = programEncounters.iterator().next();
        assertEquals("Consultation", programEncounter.getEncounterTypeName());
        assertEquals("First Stage Validation", programEncounter.getVisitTypeName());
        assertEquals("Validation", programEncounter.getLocationName());
        assertEquals("Data Admin", programEncounter.getProviderName());
        assertEquals(encounterDatetime, programEncounter.getEncounterDateTime());
    }

    @Test
    public void shouldReturnNullIfEncountersAreNull() {
        encounters = null;
        Collection<ProgramEncounter> programEncounters = encounterMapper.map(null);
        assertNull(programEncounters);

    }

    @Test
    public void shouldReturnNullIfEncounterInEncountersIsNull() {
        encounters.add(null);
        Collection<ProgramEncounter> programEncounters;
        programEncounters = encounterMapper.map(encounters);
        assertNotNull(programEncounters);
        assertEquals(0, programEncounters.size());

        encounters.add(setupEncounter());
        programEncounters = encounterMapper.map(encounters);
        assertEquals(1, programEncounters.size());
    }
}