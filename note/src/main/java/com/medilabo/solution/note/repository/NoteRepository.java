package com.medilabo.solution.note.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.medilabo.solution.note.model.Note;

public interface NoteRepository extends MongoRepository<Note, String> {

}
