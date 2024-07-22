package com.example.guides.controller;

import com.example.guides.dto.GuideDTO;
import com.example.guides.model.Chapter;
import com.example.guides.model.Guide;
import com.example.guides.model.Person;
import com.example.guides.security.JwtTokenProvider;
import com.example.guides.service.ChapterService;
import com.example.guides.service.GuideService;
import com.example.guides.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/guides")
@Tag(name = "Контроллер для работы с гайдами")
public class GuideController {

    private final GuideService guideService;
    private final PersonService personService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ChapterService chapterService;
    private final ModelMapper modelMapper;

    @Autowired
    public GuideController(GuideService guideService, PersonService personService, JwtTokenProvider jwtTokenProvider, ChapterService chapterService, ModelMapper modelMapper) {
        this.guideService = guideService;
        this.personService = personService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.chapterService = chapterService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Создать гайд")
    public ResponseEntity<?> createGuide(@Parameter(description = "Токен пользователя")
                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                            @Parameter(name = "Объект полученного гайда")
                                            @RequestBody GuideDTO guideDTO) {
        Optional<Person> optionalPerson = getPersonByToken(token);
        if (optionalPerson.isEmpty()) {
            return new ResponseEntity<>("User not found!", HttpStatus.BAD_REQUEST);
        }
        Person author = optionalPerson.get();
        Guide guide = toGuide(guideDTO, author);
        guideService.save(guide);
        saveChapters(guide, guide.getChapters());
        return new ResponseEntity<>("Ok", HttpStatus.OK);
    }

    private void saveChapters(Guide guide, List<Chapter> chapters) {
        for (Chapter chapter : chapters) {
            chapter.setGuide(guide);
            chapterService.save(chapter);
        }
    }

    private Optional<Person> getPersonByToken(String token) {
        String username = jwtTokenProvider.getUsername(token);
        return personService.findByUsername(username);
    }

    private Guide toGuide(GuideDTO guideDTO, Person author) {
        Guide guide = modelMapper.map(guideDTO, Guide.class);
        guide.setAuthor(author);
        return guide;
    }
}
