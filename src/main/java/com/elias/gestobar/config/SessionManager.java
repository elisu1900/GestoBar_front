package com.elias.gestobar.config;

import com.elias.gestobar.model.dto.UserSessionDto;
import com.elias.gestobar.model.enums.Role;

// config/SessionManager.java
public class SessionManager {

    private static SessionManager instance;

    private Long    userId;
    private String  nombre;
    private String  apellido;
    private Role    role;
    private boolean active;

    // Para navegación entre pantallas
    private Long activeTicketId;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void login(UserSessionDto user) {
        this.userId   = user.id();
        this.nombre   = user.nombre();
        this.apellido = user.apellido();
        this.role     = user.role();
        this.active   = user.active();
    }

    public void clear() {
        this.userId        = null;
        this.nombre        = null;
        this.apellido      = null;
        this.role          = null;
        this.activeTicketId = null;
    }

    // --- Auth ---
    public boolean isLoggedIn() { return userId != null; }
    public boolean isAdmin()    { return role == Role.ADMIN; }
    public boolean isWaiter()   { return role == Role.WAITER; }

    // --- Getters usuario ---
    public Long   getUserId()   { return userId; }
    public String getNombre()   { return nombre; }
    public String getApellido() { return apellido; }
    public String getFullName() { return nombre + " " + apellido; }
    public Role   getRole()     { return role; }
    public boolean isActive()   { return active; }

    // --- Ticket activo ---
    public Long getActiveTicketId()          { return activeTicketId; }
    public void setActiveTicketId(Long id)   { this.activeTicketId = id; }
    public void clearActiveTicketId()        { this.activeTicketId = null; }
}