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
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/guides")
@Tag(name = "Контроллер для работы с гайдами")
@AllArgsConstructor
@CrossOrigin("http://localhost:8081")
public class GuideController {

    private final GuideService guideService;
    private final PersonService personService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ChapterService chapterService;
    private final ModelMapper modelMapper;


    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Посмотреть топ 15 гайдов по заработку")
    public List<GuideDTO> findTopGuides() {
        return guideService.findTopGuidesByEarnings()
                .stream().map(this::fromGuide)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Посмотреть гайд по идентификатору")
    public GuideDTO findById(@PathVariable long id) {
        Optional<Guide> optionalGuide = guideService.findById(id);
        if (optionalGuide.isEmpty()) {
            return null;
        }
        return fromGuide(optionalGuide.get());
    }

    //ВНИМАТЕЛЬНЕЕ
    @GetMapping("/{id}/purchase")
    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Приобрести гайд")
    public ResponseEntity<?> purchaseGuide(@PathVariable long id) {
        Optional<Guide> optionalGuide = guideService.findById(id);
        if (optionalGuide.isEmpty()) {
            return new ResponseEntity<>("Guide not found", HttpStatus.BAD_REQUEST);
        }
        Guide guide = optionalGuide.get();
        setNewEarnings(guide);
        return new ResponseEntity<>("OK", HttpStatus.OK);
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

    private void setNewEarnings(Guide guide) {
        int newCount = guide.getCount() + 1;
        guide.setCount(newCount);
        guide.setEarnings(newCount * guide.getPrice());
        guideService.save(guide);
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

    private GuideDTO fromGuide(Guide guide) {
        return modelMapper.map(guide, GuideDTO.class);
    }
}
