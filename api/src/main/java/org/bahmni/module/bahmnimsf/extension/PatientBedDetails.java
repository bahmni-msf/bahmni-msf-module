package org.bahmni.module.bahmnimsf.extension;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.web.extension.AppointmentResponseExtension;
import org.openmrs.module.bedmanagement.BedDetails;
import org.openmrs.module.bedmanagement.BedManagementService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PatientBedDetails implements AppointmentResponseExtension {

    @Override
    public Map<String, String> run(Appointment appointment) {
        Map<String, String> bedInfo = new HashMap<>();
        BedManagementService bedManagementService = Context.getService(BedManagementService.class);;
        BedDetails bedDetails = bedManagementService.getBedAssignmentDetailsByPatient(appointment.getPatient());
        if(bedDetails!= null) {
            bedInfo.put("LOCATION_KEY", bedDetails.getPhysicalLocation().getName());
            bedInfo.put("BED_NUMBER_KEY", bedDetails.getBedNumber());
        }
        return bedInfo;
    }
}
