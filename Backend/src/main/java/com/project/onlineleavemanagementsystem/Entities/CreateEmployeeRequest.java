package com.project.onlineleavemanagementsystem.Entities;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateEmployeeRequest {
    private String firstName;
    private String lastName;
    private String email;
}
