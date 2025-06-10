package com.medilabo.solution.note.mapper;

import org.mapstruct.Mapper;

import com.medilabo.solution.note.dto.NoteDto;
import com.medilabo.solution.note.model.Note;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    public NoteDto toDto(Note note);

    public Note toEntity(NoteDto noteDto);

}
