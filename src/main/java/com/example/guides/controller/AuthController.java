package com.example.guides.controller;

import com.example.guides.dto.PersonDTO;
import com.example.guides.model.Person;
import com.example.guides.model.Referral;
import com.example.guides.security.JwtTokenProvider;
import com.example.guides.service.PersonService;
import com.example.guides.service.ReferralService;
import com.example.guides.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Контроллер для авторизации")
public class AuthController {

    @Value("${auth.password}")
    private String password;

    private final PersonService personService;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final RegistrationService registrationService;
    private final ReferralService referralService;

    @Autowired
    public AuthController(PersonService personService, AuthenticationManager authenticationManager, ModelMapper modelMapper, JwtTokenProvider jwtTokenProvider, RegistrationService registrationService, ReferralService referralService) {
        this.personService = personService;
        this.authenticationManager = authenticationManager;
        this.modelMapper = modelMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.registrationService = registrationService;
        this.referralService = referralService;
    }

    @PostMapping("/init")
    @Operation(summary = "Аутентификация + регистрация пользователя")
    public ResponseEntity<?> initPerson(@Parameter(name = "Реферальная ссылка пользователя")
                                        @RequestParam(required = false) String ref,
                                        @Parameter(name = "Данные о пользователе, который совершает вход в приложение")
                                        @RequestBody PersonDTO personDTO) {
        Optional<Person> optionalPerson = personService.findById(personDTO.getId());
        if (optionalPerson.isEmpty() && ref != null) {
            Optional<Person> byReferralLink = personService.findByReferralLink(ref);
            if (byReferralLink.isEmpty()) {
                return new ResponseEntity<>("Referral link is invalid", HttpStatus.BAD_REQUEST);
            } else {
                return register(personDTO, byReferralLink.get());
            }
        } else {
            if (optionalPerson.isPresent()) {
                return login(personDTO);
            }
            return register(personDTO, null);
        }
    }

    private ResponseEntity<?> login(PersonDTO personDTO) {
        String username = personDTO.getUsername();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        Person person = toPerson(personDTO);
        String token = jwtTokenProvider.createToken(username, person.getRole());
        return ResponseEntity.ok(createToken(token, username));
    }

    private ResponseEntity<?> register(PersonDTO newPerson, Person referralOwner) {
        Person person = toPerson(newPerson);
        registrationService.register(person);
        if (referralOwner != null) {
            referralService.save(new Referral(referralOwner, person));
        }
        String token = jwtTokenProvider.createToken(person.getUsername(), person.getRole());
        return ResponseEntity.ok(createToken(token, person.getUsername()));
    }

    private Map<String, String> createToken(String token, String username) {
        Map<String, String> response = new HashMap<>();
        response.put("username", username);
        response.put("token", token);
        return response;
    }

    private Person toPerson(PersonDTO personDTO) {
        return modelMapper.map(personDTO, Person.class);
    }


}
