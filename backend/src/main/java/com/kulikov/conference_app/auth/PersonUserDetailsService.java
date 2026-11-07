package com.kulikov.conference_app.auth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kulikov.conference_app.model.Person;
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

    /**
     * Находит человека по логину
     *
     * @param email - email, который клиент указал при входе
     * @return карточка пользователя для Spring Security
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Person person = _personRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return User.withUsername(person.getId())
                .password(person.getPasswordHash()).roles(person.getRole().name()).build();
    }
}