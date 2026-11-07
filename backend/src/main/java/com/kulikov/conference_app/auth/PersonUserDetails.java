package com.kulikov.conference_app.auth;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.kulikov.conference_app.model.Person;

/**
 * Данные человека для Spring Security
 */
public class PersonUserDetails implements UserDetails {

    private final String _personId;
    private final String _email;
    private final String _passwordHash;
    private final List<GrantedAuthority> _authorities;

    public PersonUserDetails(Person person) {
        this._personId = person.getId();
        this._email = person.getEmail();
        this._passwordHash = person.getPasswordHash();
        this._authorities = List.of(new SimpleGrantedAuthority("ROLE_" + person.getRole().name()));
    }

    /**
     * Возвращает id человека
     */
    public String getPersonId() {
        return _personId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return _authorities;
    }

    @Override
    public String getPassword() {
        return _passwordHash;
    }

    @Override
    public String getUsername() {
        return _email;
    }
}
