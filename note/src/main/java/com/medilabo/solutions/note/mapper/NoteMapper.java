package com.medilabo.solutions.note.mapper;

import org.mapstruct.Mapper;

import com.medilabo.solutions.note.dto.NoteDto;
import com.medilabo.solutions.note.model.Note;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    public NoteDto toDto(Note note);

    public Note toEntity(NoteDto noteDto);

}
