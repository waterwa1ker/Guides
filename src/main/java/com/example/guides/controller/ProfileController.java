package com.example.guides.controller;

import com.example.guides.dto.GuideDTO;
import com.example.guides.dto.PersonDTO;
import com.example.guides.model.Guide;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user-profile")
@Tag(name = "Контроллер для работы с профилем пользователя")
public class ProfileController {

    @Value("${auth.password}")
    private String password;

    private final PersonService personService;
    private final ModelMapper modelMapper;
    private final RegistrationService registrationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final ReferralService referralService;

    @Autowired
    public ProfileController(PersonService personService, ModelMapper modelMapper, RegistrationService registrationService, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager, ReferralService referralService) {
        this.personService = personService;
        this.modelMapper = modelMapper;
        this.registrationService = registrationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.referralService = referralService;
    }

    @PatchMapping
    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Изменить информация о пользователе")
    public ResponseEntity<?> changePersonInformation(@Parameter(name = "Измененные объект пользователя")
                                                        @RequestBody PersonDTO personDTO) {
        Optional<Person> optionalPerson = personService.findById(personDTO.getId());
        if (optionalPerson.isEmpty()) {
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        }
        Person person = optionalPerson.get();
        if (personDTO.getDescription() != null) {
            person.setDescription(personDTO.getDescription());
            personService.save(person);
        }
        return new ResponseEntity<>("Data has changed!", HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Получить информацию о пользователе")
    public PersonDTO getPersonInformation(@Parameter(name = "Токен пользователя")
                                            @RequestHeader(HttpHeaders.AUTHORIZATION)
                                          String token) {
        Optional<Person> optionalPerson = getPersonByToken(token);
        if (optionalPerson.isEmpty()) {
            return null;
        }
        return fromPerson(optionalPerson.get());
    }

    //LOGIN + REGISTRATION
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

    @GetMapping("/guides")
    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Получить все гайды пользователя")
    public List<GuideDTO> getGuides(@Parameter(name = "Токен пользователя")
                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        Optional<Person> optionalPerson = getPersonByToken(token);
        if (optionalPerson.isEmpty()) {
            return null;
        }
        Person person = optionalPerson.get();
        return person.getGuides().stream().map(this::fromGuide).collect(Collectors.toList());
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

    private Optional<Person> getPersonByToken(String token) {
        String username = jwtTokenProvider.getUsername(token);
        return personService.findByUsername(username);
    }

    private Person toPerson(PersonDTO personDTO) {
        return modelMapper.map(personDTO, Person.class);
    }

    private PersonDTO fromPerson(Person person) {
        return modelMapper.map(person, PersonDTO.class);
    }

    private GuideDTO fromGuide(Guide guide) {
        return modelMapper.map(guide, GuideDTO.class);
    }

}
