package com.smartcontact.controller;

import com.smartcontact.model.Contact;
import com.smartcontact.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportContacts() {
        String csv = contactService.exportToCsv();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=contacts.csv")
                .header("Content-Type", "text/csv")
                .body(csv);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Long id) {
        return contactService.getContactById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Contact>> searchContacts(@RequestParam String q) {
        return ResponseEntity.ok(contactService.searchContacts(q));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<Contact>> getFavorites() {
        return ResponseEntity.ok(contactService.getFavorites());
    }

    @PostMapping
    public ResponseEntity<Contact> createContact(@Valid @RequestBody Contact contact) {
        Contact savedContact = contactService.saveContact(contact);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedContact);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(@PathVariable Long id, @Valid @RequestBody Contact contact) {
        if (contactService.getContactById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        contact.setId(id);
        return ResponseEntity.ok(contactService.saveContact(contact));
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Contact> toggleFavorite(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(contactService.toggleFavorite(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        if (contactService.getContactById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        contactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }
}
