package com.example.project.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyDto {

    private Long id;
    private String pharmacyName;
    private String pharmacyAddress;
    private double latitude;
    private double longitude;
}
