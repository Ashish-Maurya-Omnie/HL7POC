package com.example.hl7poc.controller;

import com.example.hl7poc.config.FhirClientProperties;
import com.example.hl7poc.dto.PatientCreateRequest;
import com.example.hl7poc.service.FhirPatientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Validated
@RestController
@RequestMapping(path = "/api/v1/fhir", produces = MediaType.APPLICATION_JSON_VALUE)
public class FhirController {

    private final FhirPatientService patientService;
    private final FhirClientProperties properties;

    public FhirController(FhirPatientService patientService, FhirClientProperties properties) {
        this.patientService = patientService;
        this.properties = properties;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "mode", properties.getMode().name(),
                "fhirBaseUrl", properties.activeBaseUrl()
        );
    }

    @GetMapping("/metadata")
    public String metadata() {
        return patientService.getMetadata();
    }

    @GetMapping("/patients/{id}")
    public String getPatient(@PathVariable @NotBlank String id) {
        return patientService.readPatient(id);
    }

    @GetMapping("/patients/search")
    public String searchPatients(
            @RequestParam(required = false) String family,
            @RequestParam(required = false) String given
    ) {
        return patientService.searchPatients(family, given);
    }

    @PostMapping(path = "/patients", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String createPatient(@Valid @RequestBody PatientCreateRequest patientRequest) {
        return patientService.createPatient(patientRequest);
    }
}
