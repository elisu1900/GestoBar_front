package com.elias.gestobar.config;

import com.elias.gestobar.model.dto.UserSessionDto;
import com.elias.gestobar.model.enums.Role;

public class SessionManager {

    private static SessionManager instance;

    private Integer    userId;
    private String  name;
    private String lastName;
    private Role    role;
    private boolean active;

    private Long activeTicketId;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void login(UserSessionDto user) {
        this.userId   = user.id();
        this.name   = user.name();
        this.lastName = user.lastName();
        this.role     = user.role();
        this.active   = user.active();
    }

    public void clear() {
        this.userId        = null;
        this.name          = null;
        this.lastName      = null;
        this.role          = null;
        this.activeTicketId= null;
    }

    public boolean isLoggedIn() { return userId != null; }
    public boolean isAdmin()    { return role == Role.ADMIN; }
    public boolean isWaiter()   { return role == Role.WAITER; }

    public Integer   getUserId()   { return userId; }
    public String getNombre()   { return name; }
    public String getLastName() { return lastName; }
    public String getFullName() { return name + " " + lastName; }
    public Role   getRole()     { return role; }
    public boolean isActive()   { return active; }

    public Long getActiveTicketId()          { return activeTicketId; }
    public void setActiveTicketId(Long id)   { this.activeTicketId = id; }
    public void clearActiveTicketId()        { this.activeTicketId = null; }
}