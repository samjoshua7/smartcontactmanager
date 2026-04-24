const API_BASE_URL = '';

// DOM Elements
const drawer = document.getElementById('side-drawer');
const drawerOverlay = document.getElementById('drawer-overlay');
const menuBtn = document.getElementById('menu-btn');
const modalOverlay = document.getElementById('modal-overlay');

const addModal = document.getElementById('add-modal');
const detailModal = document.getElementById('detail-modal');
const reminderModal = document.getElementById('reminder-modal');
const dialPad = document.getElementById('dial-pad');

const fabBtn = document.getElementById('fab-btn');
const contactListEl = document.getElementById('contact-list');
const searchInput = document.getElementById('search-input');
const profileBtn = document.getElementById('profile-btn');
const profileMenu = document.getElementById('profile-menu');
const navDial = document.getElementById('nav-dial');

let currentView = 'all'; // 'all', 'favorites', 'reminders', 'recents'
let currentFilter = 'All';
let selectedContact = null;
let allContacts = [];
let recentCalls = [
    { name: 'John Doe', phone: '+1 234 567 890', type: 'incoming', time: '2 hours ago' },
    { name: 'Alice Smith', phone: '+1 987 654 321', type: 'missed', time: '5 hours ago' },
    { name: 'Bob Wilson', phone: '+1 555 000 111', type: 'outgoing', time: 'Yesterday' }
];

// Navigation & Global UI
function toggleDrawer(force) {
    drawer.classList.toggle('open', force);
    drawerOverlay.classList.toggle('active', force);
}

function toggleModal(modalEl, force) {
    modalEl.classList.toggle('open', force);
    modalOverlay.classList.toggle('active', force);
    if (!force) profileMenu.classList.remove('show');
}

menuBtn.addEventListener('click', () => toggleDrawer(true));
drawerOverlay.addEventListener('click', () => {
    toggleDrawer(false);
    toggleModal(addModal, false);
    toggleModal(detailModal, false);
    toggleModal(reminderModal, false);
    dialPad.classList.remove('open');
});

profileBtn.addEventListener('click', (e) => {
    e.stopPropagation();
    profileMenu.classList.toggle('show');
});

document.addEventListener('click', () => profileMenu.classList.remove('show'));

// API Functions
async function fetchContacts() {
    let url = `${API_BASE_URL}/contacts`;
    if (currentView === 'favorites') url = `${API_BASE_URL}/contacts/favorites`;
    
    try {
        const response = await fetch(url);
        allContacts = await response.json();
        renderView();
    } catch (error) {
        console.error('Error fetching contacts:', error);
        contactListEl.innerHTML = `<div class="loading" style="color:red">Error loading contacts.</div>`;
    }
}

async function saveContact(contactData) {
    try {
        const method = selectedContact ? 'PUT' : 'POST';
        const url = selectedContact ? `${API_BASE_URL}/contacts/${selectedContact.id}` : `${API_BASE_URL}/contacts`;
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(contactData)
        });
        if (response.ok) {
            toggleModal(addModal, false);
            fetchContacts();
        }
    } catch (error) { console.error('Save error:', error); }
}

async function toggleFavorite(id) {
    // Optimistic UI Update
    const contact = allContacts.find(c => c.id === id);
    if (contact) {
        contact.isFavorite = !contact.isFavorite;
        renderView();
        if (selectedContact && selectedContact.id === id) {
            selectedContact.isFavorite = contact.isFavorite;
            updateDetailView();
        }
    }

    try {
        await fetch(`${API_BASE_URL}/contacts/${id}/favorite`, { method: 'POST' });
        // Fetch again to ensure sync, but optimistic update already handled the visual
    } catch (error) { console.error('Toggle error:', error); }
}

async function deleteContact(id) {
    if (!confirm('Delete this contact?')) return;
    try {
        await fetch(`${API_BASE_URL}/contacts/${id}`, { method: 'DELETE' });
        toggleModal(detailModal, false);
        fetchContacts();
    } catch (error) { console.error('Delete error:', error); }
}

// Render Logic
function renderView() {
    dialPad.classList.remove('open');
    if (currentView === 'recents') { renderRecents(); return; }
    if (currentView === 'reminders') { renderRemindersView(); return; }
    
    let contacts = allContacts;
    if (currentFilter !== 'All') {
        contacts = contacts.filter(c => c.category === currentFilter);
    }
    
    renderContactList(contacts);
}

function renderContactList(contacts) {
    contactListEl.innerHTML = '';
    if (contacts.length === 0) {
        contactListEl.innerHTML = `<div class="loading">No contacts found.</div>`;
        return;
    }

    contacts.forEach(contact => {
        const strengthClass = (contact.relationshipStrength || 'weak').toLowerCase();
        const card = document.createElement('div');
        card.className = 'contact-card';
        card.innerHTML = `
            <div class="contact-avatar" style="background-color: ${getAvatarColor(contact.name)}">${contact.name[0]}</div>
            <div class="contact-info">
                <div class="contact-name">${contact.name} ${contact.isFavorite ? '⭐' : ''}</div>
                <div class="contact-phone">${contact.phone}</div>
                <div class="contact-strength-mini ${strengthClass}">${contact.relationshipStrength || 'Weak'}</div>
            </div>
        `;
        card.addEventListener('click', () => openDetailView(contact));
        contactListEl.appendChild(card);
    });
}

function openDetailView(contact) {
    selectedContact = contact;
    updateDetailView();
    toggleModal(detailModal, true);
}

function updateDetailView() {
    if (!selectedContact) return;
    document.getElementById('detail-name').textContent = selectedContact.name;
    document.getElementById('detail-phone').textContent = selectedContact.phone;
    document.getElementById('detail-email').textContent = selectedContact.email || 'No email';
    document.getElementById('detail-category').textContent = selectedContact.category || 'Friends';
    document.getElementById('detail-score').textContent = `${selectedContact.relationshipScore}% Strength (${selectedContact.relationshipStrength})`;
    document.getElementById('detail-notes').textContent = selectedContact.notes || 'No notes added.';
    document.getElementById('detail-avatar').textContent = selectedContact.name[0];
    document.getElementById('detail-avatar').style.backgroundColor = getAvatarColor(selectedContact.name);

    const favBtn = document.getElementById('detail-fav-btn');
    favBtn.classList.toggle('fav-active', selectedContact.isFavorite);
    favBtn.innerHTML = `<span class="material-icons">${selectedContact.isFavorite ? 'star' : 'star_border'}</span>${selectedContact.isFavorite ? 'Favorited' : 'Favorite'}`;
}

function renderRecents() {
    contactListEl.innerHTML = '<h2 style="padding:16px 0; font-size:18px;">Recent Calls</h2>';
    recentCalls.forEach(call => {
        const item = document.createElement('div');
        item.className = 'recent-item';
        item.innerHTML = `
            <div class="contact-avatar" style="background:#dadce0; color:#5f6368">${call.name[0]}</div>
            <div class="recent-info">
                <div class="contact-name">${call.name}</div>
                <div class="recent-type ${call.type}">
                    <span class="material-icons" style="font-size:14px;">${call.type === 'missed' ? 'call_missed' : call.type === 'incoming' ? 'call_received' : 'call_made'}</span>
                    ${call.type}
                </div>
            </div>
            <div class="recent-time">${call.time}</div>
        `;
        contactListEl.appendChild(item);
    });
}

async function renderRemindersView() {
    try {
        const response = await fetch(`${API_BASE_URL}/reminders`);
        const reminders = await response.json();
        contactListEl.innerHTML = '<h2 style="padding:16px 0; font-size:18px;">Reminders</h2>';
        if (reminders.length === 0) {
            contactListEl.innerHTML += `<div class="loading">No reminders set.</div>`;
            return;
        }
        reminders.forEach(r => {
            const card = document.createElement('div');
            card.className = 'contact-card';
            card.innerHTML = `
                <div class="contact-avatar" style="background:#f9ab00"><span class="material-icons">notifications</span></div>
                <div class="contact-info">
                    <div class="contact-name">${r.message}</div>
                    <div class="contact-phone">${new Date(r.reminderDate).toLocaleString()}</div>
                </div>
                <div class="badge" style="background:#feefc3; color:#b05a00; padding:4px 8px; border-radius:4px; font-size:11px;">${r.status}</div>
            `;
            contactListEl.appendChild(card);
        });
    } catch (e) { console.error(e); }
}

// Detail View Actions
document.getElementById('detail-fav-btn').addEventListener('click', () => toggleFavorite(selectedContact.id));
document.getElementById('detail-edit-btn').addEventListener('click', () => {
    toggleModal(detailModal, false);
    openEditModal();
});
document.getElementById('detail-delete-btn').addEventListener('click', () => deleteContact(selectedContact.id));
document.getElementById('detail-remind-btn').addEventListener('click', () => {
    toggleModal(detailModal, false);
    toggleModal(reminderModal, true);
});
document.getElementById('close-detail-btn').addEventListener('click', () => toggleModal(detailModal, false));

function openEditModal() {
    document.querySelector('#add-modal h2').textContent = 'Edit Contact';
    document.getElementById('name').value = selectedContact.name;
    document.getElementById('phone').value = selectedContact.phone;
    document.getElementById('email').value = selectedContact.email || '';
    document.getElementById('notes').value = selectedContact.notes || '';
    toggleModal(addModal, true);
}

// Dial Pad Logic
const dialDisplay = document.getElementById('dial-number');
document.querySelectorAll('.dial-key').forEach(key => {
    key.addEventListener('click', () => {
        dialDisplay.textContent += key.textContent;
    });
});
document.getElementById('dial-backspace').addEventListener('click', () => {
    dialDisplay.textContent = dialDisplay.textContent.slice(0, -1);
});
document.getElementById('dial-call-btn').addEventListener('click', () => {
    if (dialDisplay.textContent) {
        alert('Calling ' + dialDisplay.textContent + '...');
        dialDisplay.textContent = '';
        dialPad.classList.remove('open');
    }
});

// Navigation Click Handlers
document.querySelectorAll('.nav-item, .drawer-menu li').forEach(item => {
    item.addEventListener('click', (e) => {
        const view = item.getAttribute('data-view');
        if (!view) return;
        
        document.querySelectorAll('.nav-item, .drawer-menu li').forEach(i => i.classList.remove('active'));
        item.classList.add('active');
        
        currentView = view;
        currentFilter = 'All';
        toggleDrawer(false);
        fetchContacts();
    });
});

navDial.addEventListener('click', () => {
    dialPad.classList.toggle('open');
});

document.querySelectorAll('.pill').forEach(pill => {
    pill.addEventListener('click', () => {
        document.querySelectorAll('.pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        currentFilter = pill.textContent;
        renderView();
    });
});

fabBtn.addEventListener('click', () => {
    selectedContact = null;
    document.querySelector('#add-modal h2').textContent = 'Add Contact';
    document.getElementById('add-contact-form').reset();
    toggleModal(addModal, true);
});

document.getElementById('close-modal-btn').addEventListener('click', () => toggleModal(addModal, false));
document.getElementById('close-reminder-modal-btn').addEventListener('click', () => toggleModal(reminderModal, false));

document.getElementById('add-contact-form').addEventListener('submit', (e) => {
    e.preventDefault();
    saveContact({
        name: document.getElementById('name').value,
        phone: document.getElementById('phone').value,
        email: document.getElementById('email').value,
        notes: document.getElementById('notes').value
    });
});

document.getElementById('add-reminder-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
        contactId: selectedContact ? selectedContact.id : null,
        message: document.getElementById('reminder-msg').value,
        reminderDate: document.getElementById('reminder-date').value
    };
    try {
        await fetch(`${API_BASE_URL}/reminders`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        toggleModal(reminderModal, false);
    } catch (e) {}
});

searchInput.addEventListener('input', (e) => {
    const q = e.target.value.toLowerCase();
    const filtered = allContacts.filter(c => 
        c.name.toLowerCase().includes(q) || 
        c.phone.includes(q) || 
        (c.notes && c.notes.toLowerCase().includes(q))
    );
    renderContactList(filtered);
});

document.getElementById('menu-export-btn').addEventListener('click', () => {
    window.location.href = `${API_BASE_URL}/contacts/export`;
});

// Utilities
function getAvatarColor(name) {
    const colors = ['#1a73e8', '#d93025', '#1e8e3e', '#f9ab00', '#7b1fa2', '#0097a7', '#5c6bc0'];
    let hash = 0;
    for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
    return colors[Math.abs(hash) % colors.length];
}

// Init
document.addEventListener('DOMContentLoaded', fetchContacts);
