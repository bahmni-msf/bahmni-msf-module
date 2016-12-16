package org.bahmni.module.bahmnimsf.controller;

import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.bahmni.module.bahmnimsf.util.EncounterMapper;
import org.bahmni.module.bahmnimsf.model.ProgramEncounter;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

@Controller
public class ProgramEncounterController extends BaseRestController {
    private final String baseUrl = "/rest/" + RestConstants.VERSION_1;

    @RequestMapping(value = baseUrl + "/bahmniprogramencounter", method = RequestMethod.GET)
    @ResponseBody
    public Collection<ProgramEncounter> getEncounters(@RequestParam(value = "patientProgramUuid", required = true) String patientProgramUuid) {
        BahmniProgramWorkflowService bahmniProgramWorkflowService = Context.getService(BahmniProgramWorkflowService.class);

        Collection<Encounter> encounters = bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(patientProgramUuid);
        EncounterMapper encounterMapper = new EncounterMapper();
        return encounterMapper.map(encounters);
    }
}
