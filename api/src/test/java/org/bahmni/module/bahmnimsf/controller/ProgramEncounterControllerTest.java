package org.bahmni.module.bahmnimsf.controller;

import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.bahmni.module.bahmnimsf.model.ProgramEncounter;
import org.bahmni.module.bahmnimsf.util.EncounterMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.mockito.Mockito.times;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class ProgramEncounterControllerTest {

    @Mock
    private BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Mock
    private EncounterMapper encounterMapper;

    private ProgramEncounterController programEncounterController;
    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Context.class);
        PowerMockito.when(Context.getService(BahmniProgramWorkflowService.class)).thenReturn(bahmniProgramWorkflowService);
        programEncounterController = new ProgramEncounterController();
    }

    @Test
    public void shouldGetTheEncountersForPatientProgramUsingPatientProgramUuid() throws Exception {
        String patientProgramUuid = "patientProgramUuid";
        List<Encounter> programEncounters = new ArrayList<>();
        PowerMockito.when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(patientProgramUuid)).thenReturn(programEncounters);
        PowerMockito.whenNew(EncounterMapper.class).withNoArguments().thenReturn(encounterMapper);
        List<ProgramEncounter> mappedProgramEncounters = new ArrayList<>();
        PowerMockito.when(encounterMapper.map(programEncounters)).thenReturn(mappedProgramEncounters);

        Collection<ProgramEncounter> encounters = programEncounterController.getEncounters(patientProgramUuid);

        Assert.assertEquals(mappedProgramEncounters, encounters);
        Mockito.verify(bahmniProgramWorkflowService, times(1)).getEncountersByPatientProgramUuid(patientProgramUuid);
    }
}