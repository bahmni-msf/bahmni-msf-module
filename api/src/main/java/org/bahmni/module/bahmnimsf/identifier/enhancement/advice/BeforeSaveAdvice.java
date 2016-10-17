package org.bahmni.module.bahmnimsf.identifier.enhancement.advice;

import org.bahmni.module.bahmnimsf.identifier.enhancement.IdentifierEnhancementFactory;
import org.openmrs.Patient;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

public class BeforeSaveAdvice implements MethodBeforeAdvice {

    private static final String methodToIntercept = "savePatient";

    @Override
    public void before(Method method, Object[] objects, Object o) {
        if (method.getName().equalsIgnoreCase(methodToIntercept)) {
            Patient patient = (Patient) objects[0];
            if (patient.getPatientId() == null) {
                IdentifierEnhancementFactory.enhanceIdentifier(patient);
            }
        }
    }
}
