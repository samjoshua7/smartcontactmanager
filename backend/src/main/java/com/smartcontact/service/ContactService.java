package com.smartcontact.service;

import com.smartcontact.model.Contact;
import com.smartcontact.repository.ContactRepository;
import com.smartcontact.repository.InteractionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final InteractionRepository interactionRepository;

    public ContactService(ContactRepository contactRepository, InteractionRepository interactionRepository) {
        this.contactRepository = contactRepository;
        this.interactionRepository = interactionRepository;
    }

    public List<Contact> getAllContacts() {
        List<Contact> contacts = contactRepository.findAll();
        contacts.forEach(this::enrichContactData);
        return contacts;
    }

    public Optional<Contact> getContactById(Long id) {
        Optional<Contact> contactOpt = contactRepository.findById(id);
        contactOpt.ifPresent(this::enrichContactData);
        return contactOpt;
    }

    public List<Contact> searchContacts(String query) {
        String q = query.toLowerCase();
        return contactRepository.findAll().stream()
                .filter(c -> c.getName().toLowerCase().contains(q) || 
                             (c.getEmail() != null && c.getEmail().toLowerCase().contains(q)) ||
                             (c.getNotes() != null && c.getNotes().toLowerCase().contains(q)) ||
                             (c.getCategory() != null && c.getCategory().toLowerCase().contains(q)))
                .peek(this::enrichContactData)
                .collect(Collectors.toList());
    }

    public List<Contact> getFavorites() {
        return contactRepository.findAll().stream()
                .filter(Contact::isFavorite)
                .peek(this::enrichContactData)
                .collect(Collectors.toList());
    }

    @Transactional
    public Contact saveContact(Contact contact) {
        if (contact.getCreatedAt() == null) {
            contact.setCreatedAt(LocalDateTime.now());
        }
        if (contact.getLastContacted() == null) {
            contact.setLastContacted(LocalDateTime.now());
        }
        
        categorizeContact(contact);
        
        // Ensure ID is not manually set for new contacts
        if (contact.getId() != null && contact.getId() == 0) {
            contact.setId(null);
        }
        
        Contact saved = contactRepository.save(contact);
        enrichContactData(saved);
        return saved;
    }

    @Transactional
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }

    @Transactional
    public Contact toggleFavorite(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact with ID " + id + " not found"));
        
        boolean newState = !contact.isFavorite();
        System.out.println("Toggling favorite for " + contact.getName() + " to " + newState);
        contact.setFavorite(newState);
        
        Contact saved = contactRepository.save(contact);
        System.out.println("Favorite state after save: " + saved.isFavorite());
        return saved;
    }

    public String exportToCsv() {
        List<Contact> contacts = contactRepository.findAll();
        StringBuilder csv = new StringBuilder("ID,Name,Phone,Email,Category,Notes\n");
        for (Contact c : contacts) {
            csv.append(c.getId()).append(",")
               .append(escapeCsv(c.getName())).append(",")
               .append(escapeCsv(c.getPhone())).append(",")
               .append(escapeCsv(c.getEmail())).append(",")
               .append(escapeCsv(c.getCategory())).append(",")
               .append(escapeCsv(c.getNotes())).append("\n");
        }
        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void categorizeContact(Contact contact) {
        String notes = contact.getNotes() != null ? contact.getNotes().toLowerCase() : "";
        String email = contact.getEmail() != null ? contact.getEmail().toLowerCase() : "";

        if (notes.contains("work") || notes.contains("office") || notes.contains("company") || 
            notes.contains("boss") || notes.contains("manager") || email.contains("work") || email.contains("biz")) {
            contact.setCategory("Work");
        } else if (notes.contains("family") || notes.contains("relative") || notes.contains("home") || 
                   notes.contains("mom") || notes.contains("dad") || notes.contains("sister") || notes.contains("brother")) {
            contact.setCategory("Family");
        } else {
            contact.setCategory("Friends");
        }
    }

    private void enrichContactData(Contact contact) {
        calculateRelationshipScore(contact);
    }

    private void calculateRelationshipScore(Contact contact) {
        long interactionCount = interactionRepository.findByContactId(contact.getId()).size();
        if (interactionCount == 0 && contact.getLastContacted() != null) {
            interactionCount = 1;
        }

        long daysSinceLastContact = 0;
        if (contact.getLastContacted() != null) {
            daysSinceLastContact = ChronoUnit.DAYS.between(contact.getLastContacted(), LocalDateTime.now());
        }

        int score = (int) ((interactionCount * 15) - (daysSinceLastContact * 2));
        score = Math.max(0, Math.min(100, score));
        
        contact.setRelationshipScore(score);
        
        if (score >= 70) {
            contact.setRelationshipStrength("Strong");
        } else if (score >= 30) {
            contact.setRelationshipStrength("Medium");
        } else {
            contact.setRelationshipStrength("Weak");
        }
    }
}
