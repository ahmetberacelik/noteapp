package com.noteapp.api.controller;

import com.noteapp.api.model.Note;
import com.noteapp.api.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    @Autowired
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // Kullanıcı ID'sini Keycloak token'dan al
    private String getUserId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof KeycloakPrincipal) {
            KeycloakPrincipal<KeycloakSecurityContext> kp =
                    (KeycloakPrincipal<KeycloakSecurityContext>) authentication.getPrincipal();
            AccessToken token = kp.getKeycloakSecurityContext().getToken();
            return token.getSubject();
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes(HttpServletRequest request) {
        String userId = getUserId(request);
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Note> notes = noteService.getAllNotesByUser(userId);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(
            @PathVariable("id") Long id,
            HttpServletRequest request) {
        String userId = getUserId(request);
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return (ResponseEntity<Note>) noteService.getNoteById(id)
                .map(note -> {
                    // Kullanıcı sadece kendi notlarını görebilir
                    if (note.getUserId().equals(userId)) {
                        return new ResponseEntity<>(note, HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Note> createNote(
            @RequestBody Note note,
            HttpServletRequest request) {
        String userId = getUserId(request);
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        note.setUserId(userId);
        Note savedNote = noteService.saveNote(note);
        return new ResponseEntity<>(savedNote, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(
            @PathVariable("id") Long id,
            @RequestBody Note note,
            HttpServletRequest request) {
        String userId = getUserId(request);
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Note updatedNote = noteService.updateNote(id, note, userId);
            if (updatedNote != null) {
                return new ResponseEntity<>(updatedNote, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteNote(
            @PathVariable("id") Long id,
            HttpServletRequest request) {
        String userId = getUserId(request);
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            noteService.deleteNote(id, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}