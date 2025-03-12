package com.noteapp.api.service;

import com.noteapp.api.model.Note;
import com.noteapp.api.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public List<Note> getAllNotesByUser(String userId) {
        return noteRepository.findByUserIdOrderByCreationDateDesc(userId);
    }

    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    @Transactional
    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    @Transactional
    public Note updateNote(Long id, Note noteDetails, String userId) {
        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isPresent()) {
            Note existingNote = noteOpt.get();
            // Güvenlik kontrolü - sadece kendi notlarını güncelleyebilir
            if (!existingNote.getUserId().equals(userId)) {
                throw new SecurityException("You do not have permission to update this note");
            }
            existingNote.setTitle(noteDetails.getTitle());
            existingNote.setContent(noteDetails.getContent());
            return noteRepository.save(existingNote);
        }
        return null;
    }

    @Transactional
    public void deleteNote(Long id, String userId) {
        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isPresent()) {
            Note note = noteOpt.get();
            // Güvenlik kontrolü - sadece kendi notlarını silebilir
            if (!note.getUserId().equals(userId)) {
                throw new SecurityException("You do not have permission to delete this note");
            }
            noteRepository.deleteById(id);
        }
    }
}