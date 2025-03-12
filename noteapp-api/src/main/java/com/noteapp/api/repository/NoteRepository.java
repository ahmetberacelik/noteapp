package com.noteapp.api.repository;

import com.noteapp.api.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserId(String userId);
    List<Note> findByUserIdOrderByCreationDateDesc(String userId);
}