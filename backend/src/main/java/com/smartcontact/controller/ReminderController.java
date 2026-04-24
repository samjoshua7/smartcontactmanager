package com.smartcontact.controller;

import com.smartcontact.model.Reminder;
import com.smartcontact.service.ReminderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public ResponseEntity<List<Reminder>> getAllReminders() {
        return ResponseEntity.ok(reminderService.getAllReminders());
    }

    @GetMapping("/contact/{contactId}")
    public ResponseEntity<List<Reminder>> getRemindersByContact(@PathVariable Long contactId) {
        return ResponseEntity.ok(reminderService.getRemindersByContact(contactId));
    }

    @PostMapping
    public ResponseEntity<Reminder> createReminder(@RequestBody Reminder reminder) {
        Reminder savedReminder = reminderService.saveReminder(reminder);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReminder);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Reminder> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return reminderService.getReminderById(id)
                .map(r -> {
                    r.setStatus(status);
                    return ResponseEntity.ok(reminderService.saveReminder(r));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long id) {
        reminderService.deleteReminder(id);
        return ResponseEntity.noContent().build();
    }
}
