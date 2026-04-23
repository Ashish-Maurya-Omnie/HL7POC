package com.example.hl7poc.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.List;

public class PatientCreateRequest {

    @Valid
    private List<IdentifierDto> identifiers = new ArrayList<>();

    @Valid
    @NotEmpty(message = "At least one patient name is required.")
    private List<NameDto> names = new ArrayList<>();

    @Pattern(
            regexp = "male|female|other|unknown",
            message = "gender must be one of: male, female, other, unknown"
    )
    private String gender;

    @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "birthDate must be in format yyyy-MM-dd"
    )
    private String birthDate;

    @Valid
    private List<TelecomDto> telecoms = new ArrayList<>();

    @Valid
    private List<AddressDto> addresses = new ArrayList<>();

    public List<IdentifierDto> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<IdentifierDto> identifiers) {
        this.identifiers = identifiers;
    }

    public List<NameDto> getNames() {
        return names;
    }

    public void setNames(List<NameDto> names) {
        this.names = names;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public List<TelecomDto> getTelecoms() {
        return telecoms;
    }

    public void setTelecoms(List<TelecomDto> telecoms) {
        this.telecoms = telecoms;
    }

    public List<AddressDto> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressDto> addresses) {
        this.addresses = addresses;
    }

    public static class IdentifierDto {
        @NotBlank(message = "identifier.system is required.")
        private String system;

        @NotBlank(message = "identifier.value is required.")
        private String value;

        public String getSystem() {
            return system;
        }

        public void setSystem(String system) {
            this.system = system;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class NameDto {
        @NotBlank(message = "name.family is required.")
        private String family;

        @NotEmpty(message = "name.given must contain at least one value.")
        private List<@NotBlank(message = "name.given value cannot be blank.") String> given = new ArrayList<>();

        public String getFamily() {
            return family;
        }

        public void setFamily(String family) {
            this.family = family;
        }

        public List<String> getGiven() {
            return given;
        }

        public void setGiven(List<String> given) {
            this.given = given;
        }
    }

    public static class TelecomDto {
        @NotBlank(message = "telecom.system is required.")
        private String system;

        @NotBlank(message = "telecom.value is required.")
        private String value;

        private String use;

        public String getSystem() {
            return system;
        }

        public void setSystem(String system) {
            this.system = system;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getUse() {
            return use;
        }

        public void setUse(String use) {
            this.use = use;
        }
    }

    public static class AddressDto {
        private List<String> line = new ArrayList<>();
        private String city;
        private String state;
        private String postalCode;
        private String country;

        public List<String> getLine() {
            return line;
        }

        public void setLine(List<String> line) {
            this.line = line;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }
}
