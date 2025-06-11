package com.medilabo.solutions.note.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {

    @Id
    private String id;

    @Field("patId")
    private int patId;

    @Field("patient")
    private String patient;

    @Field("note")
    private String note;

}
