package com.example.hl7poc.service;

import ca.uhn.fhir.context.FhirContext;
import com.example.hl7poc.client.FhirHttpClient;
import com.example.hl7poc.dto.PatientCreateRequest;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FhirPatientService {

    private final FhirHttpClient fhirHttpClient;
    private final FhirContext fhirContext;

    public FhirPatientService(FhirHttpClient fhirHttpClient, FhirContext fhirContext) {
        this.fhirHttpClient = fhirHttpClient;
        this.fhirContext = fhirContext;
    }

    public String getMetadata() {
        return fhirHttpClient.get("/metadata", Map.of());
    }

    public String readPatient(String id) {
        return fhirHttpClient.get("/Patient/" + id, Map.of());
    }

    public String searchPatients(String family, String given) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("family", family);
        queryParams.put("given", given);
        return fhirHttpClient.get("/Patient", queryParams);
    }

    public String createPatient(PatientCreateRequest request) {
        Patient patient = new Patient();

        if (request.getIdentifiers() != null) {
            for (PatientCreateRequest.IdentifierDto dto : request.getIdentifiers()) {
                Identifier identifier = new Identifier();
                identifier.setSystem(dto.getSystem());
                identifier.setValue(dto.getValue());
                patient.addIdentifier(identifier);
            }
        }

        if (request.getNames() != null) {
            for (PatientCreateRequest.NameDto dto : request.getNames()) {
                HumanName name = new HumanName();
                name.setFamily(dto.getFamily());
                for (String given : dto.getGiven()) {
                    name.addGiven(given);
                }
                patient.addName(name);
            }
        }

        if (request.getGender() != null && !request.getGender().isBlank()) {
            patient.setGender(Enumerations.AdministrativeGender.fromCode(request.getGender()));
        }

        if (request.getBirthDate() != null && !request.getBirthDate().isBlank()) {
            patient.setBirthDateElement(new org.hl7.fhir.r4.model.DateType(request.getBirthDate()));
        }

        if (request.getTelecoms() != null) {
            for (PatientCreateRequest.TelecomDto dto : request.getTelecoms()) {
                ContactPoint contactPoint = new ContactPoint();
                contactPoint.setSystem(ContactPoint.ContactPointSystem.fromCode(dto.getSystem()));
                contactPoint.setValue(dto.getValue());
                if (dto.getUse() != null && !dto.getUse().isBlank()) {
                    contactPoint.setUse(ContactPoint.ContactPointUse.fromCode(dto.getUse()));
                }
                patient.addTelecom(contactPoint);
            }
        }

        if (request.getAddresses() != null) {
            for (PatientCreateRequest.AddressDto dto : request.getAddresses()) {
                Address address = new Address();
                if (dto.getLine() != null) {
                    dto.getLine().forEach(address::addLine);
                }
                address.setCity(dto.getCity());
                address.setState(dto.getState());
                address.setPostalCode(dto.getPostalCode());
                address.setCountry(dto.getCountry());
                patient.addAddress(address);
            }
        }

        String patientJson = fhirContext.newJsonParser().encodeResourceToString(patient);
        return fhirHttpClient.post("/Patient", patientJson);
    }
}
