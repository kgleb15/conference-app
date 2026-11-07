package com.kulikov.conference_app.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kulikov.conference_app.repositories.PersonRepository;

/**
 * Ищет человека по логину для проверки пароля
 */
@Service
public class PersonUserDetailsService implements UserDetailsService {

    private final PersonRepository _personRepository;

    public PersonUserDetailsService(PersonRepository personRepository) {
        this._personRepository = personRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return _personRepository.findByEmail(email)
                .map(PersonUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }
}