package com.example.guides.controller;

import com.example.guides.constant.FilesFormat;
import com.example.guides.dto.GuideDTO;
import com.example.guides.dto.PersonDTO;
import com.example.guides.model.Guide;
import com.example.guides.model.Person;
import com.example.guides.security.JwtTokenProvider;
import com.example.guides.service.PersonService;
import com.example.guides.util.MediaSaver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user-profile")
@Tag(name = "Контроллер для работы с профилем пользователя")
@AllArgsConstructor
@CrossOrigin("http://localhost:8081")
public class ProfileController {

    private final PersonService personService;
    private final ModelMapper modelMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final MediaSaver mediaSaver;

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

    @PostMapping("/upload-photo")
    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Загрузить фото профиля")
    public ResponseEntity<?> uploadProfileImage(@Parameter(name = "Токен пользователя")
                                                @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                @Parameter(name = "Файл")
                                                @RequestParam(name = "file")
                                                MultipartFile file) {
        Optional<Person> optionalPerson = getPersonByToken(token);
        if (optionalPerson.isEmpty()) {
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        }
        Person person = optionalPerson.get();
        return mediaSaver.saveProfilePhoto(file, person)
                ? new ResponseEntity<>("Ok", HttpStatus.OK) : new ResponseEntity<>("Failed to save file", HttpStatus.BAD_REQUEST);
    }

    private Optional<Person> getPersonByToken(String token) {
        String username = jwtTokenProvider.getUsername(token);
        return personService.findByUsername(username);
    }
    private PersonDTO fromPerson(Person person) {
        return modelMapper.map(person, PersonDTO.class);
    }

    private GuideDTO fromGuide(Guide guide) {
        return modelMapper.map(guide, GuideDTO.class);
    }

}
