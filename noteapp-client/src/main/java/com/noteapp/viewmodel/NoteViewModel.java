package com.noteapp.viewmodel;

import com.noteapp.client.NoteClient;
import com.noteapp.model.Note;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class NoteViewModel {

    private static final Logger logger = LoggerFactory.getLogger(NoteViewModel.class);

    private ObservableList<Note> notes;
    private StringProperty currentTitle;
    private StringProperty currentContent;
    private NoteClient noteClient;

    public NoteViewModel(NoteClient noteClient) {
        this.noteClient = noteClient;
        notes = FXCollections.observableArrayList();
        currentTitle = new SimpleStringProperty("");
        currentContent = new SimpleStringProperty("");
    }

    public void loadNotes() throws IOException, InterruptedException {
        logger.info("Notlar yükleniyor...");
        List<Note> loadedNotes = noteClient.getAllNotes();
        notes.clear();
        notes.addAll(loadedNotes);
        logger.info("{} not yüklendi", loadedNotes.size());
    }

    public void addNote() throws IOException, InterruptedException {
        if (!currentTitle.get().isEmpty()) {
            logger.info("Yeni not ekleniyor: {}", currentTitle.get());
            Note newNote = new Note(currentTitle.get(), currentContent.get());
            Note savedNote = noteClient.createNote(newNote);

            if (savedNote != null) {
                notes.add(savedNote);
                clearFields();
                logger.info("Yeni not başarıyla eklendi. ID: {}", savedNote.getId());
            }
        }
    }

    public void updateNote(Note note) throws IOException, InterruptedException {
        if (note != null) {
            logger.info("Not güncelleniyor. ID: {}", note.getId());
            note.setTitle(currentTitle.get());
            note.setContent(currentContent.get());

            Note updatedNote = noteClient.updateNote(note.getId(), note);

            if (updatedNote != null) {
                // Trigger list update
                int index = notes.indexOf(note);
                notes.set(index, updatedNote);
                logger.info("Not başarıyla güncellendi. ID: {}", updatedNote.getId());
            }
        }
    }

    public void deleteNote(Note note) throws IOException, InterruptedException {
        if (note != null) {
            logger.info("Not siliniyor. ID: {}", note.getId());
            boolean success = noteClient.deleteNote(note.getId());

            if (success) {
                notes.remove(note);
                clearFields();
                logger.info("Not başarıyla silindi. ID: {}", note.getId());
            }
        }
    }

    public void selectNote(Note note) {
        if (note != null) {
            currentTitle.set(note.getTitle());
            currentContent.set(note.getContent());
            logger.debug("Not seçildi. ID: {}", note.getId());
        }
    }

    public void clearFields() {
        currentTitle.set("");
        currentContent.set("");
        logger.debug("Alanlar temizlendi");
    }

    public ObservableList<Note> getNotes() {
        return notes;
    }

    public StringProperty currentTitleProperty() {
        return currentTitle;
    }

    public StringProperty currentContentProperty() {
        return currentContent;
    }
}