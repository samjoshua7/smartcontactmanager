package com.smartcontact.service;

import com.smartcontact.model.Reminder;
import com.smartcontact.repository.ReminderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;

    public ReminderService(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    public List<Reminder> getAllReminders() {
        return reminderRepository.findAll();
    }

    public List<Reminder> getRemindersByContact(Long contactId) {
        return reminderRepository.findByContactId(contactId);
    }

    public Optional<Reminder> getReminderById(Long id) {
        return reminderRepository.findById(id);
    }

    public Reminder saveReminder(Reminder reminder) {
        if (reminder.getStatus() == null) {
            reminder.setStatus("PENDING");
        }
        return reminderRepository.save(reminder);
    }

    public void deleteReminder(Long id) {
        reminderRepository.deleteById(id);
    }
}
