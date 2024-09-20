package com.example.guides.controller;

import com.example.guides.dto.ChapterDTO;
import com.example.guides.dto.GuideDTO;
import com.example.guides.dto.PersonDTO;
import com.example.guides.model.Chapter;
import com.example.guides.model.Guide;
import com.example.guides.model.Person;
import com.example.guides.model.PurchasedGuides;
import com.example.guides.security.JwtTokenProvider;
import com.example.guides.service.ChapterService;
import com.example.guides.service.GuideService;
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
    private final GuideService guideService;
    private final ChapterService chapterService;
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
    @Operation(summary = "Получить все гайды пользователя либо купленные")
    public List<GuideDTO> getGuides(@Parameter(name = "Токен пользователя")
                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                     @RequestParam boolean own) {
        Optional<Person> optionalPerson = getPersonByToken(token);
        if (optionalPerson.isEmpty()) {
            return null;
        }
        Person person = optionalPerson.get();
        List<Guide> result;
        if (own) {
            result = person.getGuides();
        } else {
            result = person.getPurchasedGuides()
                    .stream().map(PurchasedGuides::getGuide)
                    .collect(Collectors.toList());
        }
        return result.stream().map(this::fromGuide).collect(Collectors.toList());
    }

    @GetMapping("/guides/{id}")
    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Получить все гайды пользователя")
    public GuideDTO getGuideById(@Parameter(name = "Идентификатор гайда")
                                 @PathVariable long id) {
        Optional<Guide> optionalGuide = guideService.findById(id);
        if (optionalGuide.isEmpty()) {
            return null;
        }
        return fromGuide(optionalGuide.get());
    }

    @PatchMapping("/guides/{id}")
    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Изменить гайд по идентификатору")
    public ResponseEntity<?> updateGuideById(@Parameter(name = "Идентификатор гайда")
                                             @PathVariable long id,
                                             @Parameter(name = "Измененный гайд")
                                             @RequestBody GuideDTO guideDTO) {
        Optional<Guide> optionalGuide = guideService.findById(id);
        if (optionalGuide.isEmpty()) {
            return new ResponseEntity<>("Guide not found", HttpStatus.BAD_REQUEST);
        }
        Guide guide = optionalGuide.get();
        updateGuide(guide, guideDTO);
        guideService.save(guide);
        return new ResponseEntity<>("Ok", HttpStatus.OK);
    }

    @GetMapping("/guides/{guideId}/chapters/{chapterId}")
    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Получить главу гайда по его идентификатору")
    public ChapterDTO getChapterById(@Parameter(name = "Идентификатор главы")
                                     @PathVariable long chapterId) {
        Optional<Chapter> optionalChapter = chapterService.findById(chapterId);
        if (optionalChapter.isEmpty()) {
            return null;
        }
        return fromChapter(optionalChapter.get());
    }

    @PatchMapping("/guides/{guideId}/chapters/{chapterId}")
    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Изменить главу гайда")
    public ResponseEntity<?> updateChapterById(@Parameter(name = "Идентификатор главы")
                                           @PathVariable long chapterId,
                                               @Parameter(name = "Изменная глава")
                                               @RequestBody ChapterDTO chapterDTO) {
        Optional<Chapter> optionalChapter = chapterService.findById(chapterId);
        if (optionalChapter.isEmpty()) {
            return new ResponseEntity<>("Chapter not found", HttpStatus.BAD_REQUEST);
        }
        Chapter chapter = optionalChapter.get();
        updateChapter(chapter, chapterDTO);
        chapterService.save(chapter);
        return new ResponseEntity<>("Ok", HttpStatus.OK);
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

    private void updateGuide(Guide guide, GuideDTO guideDTO) {
        if (guideDTO.getPrice() != 0) {
            guide.setPrice(guideDTO.getPrice());
        }
        if (guideDTO.getDescription() != null) {
            guide.setDescription(guideDTO.getDescription());
        }
    }

    private void updateChapter(Chapter chapter, ChapterDTO chapterDTO) {
        if (chapterDTO.getName() != null) {
            chapter.setName(chapterDTO.getName());
        }
        if (chapterDTO.getText() != null) {
            chapter.setText(chapterDTO.getText());
        }
    }


    private Optional<Person> getPersonByToken(String token) {
        String username = jwtTokenProvider.getUsername(token);
        return personService.findByUsername(username);
    }

    private ChapterDTO fromChapter(Chapter chapter) { return modelMapper.map(chapter, ChapterDTO.class); }

    private PersonDTO fromPerson(Person person) {
        return modelMapper.map(person, PersonDTO.class);
    }

    private GuideDTO fromGuide(Guide guide) {
        return modelMapper.map(guide, GuideDTO.class);
    }

}
