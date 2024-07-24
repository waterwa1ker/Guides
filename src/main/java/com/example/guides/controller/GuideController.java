package com.example.guides.controller;

import com.example.guides.constant.Language;
import com.example.guides.dto.GuideDTO;
import com.example.guides.model.Chapter;
import com.example.guides.model.Guide;
import com.example.guides.model.Person;
import com.example.guides.model.PurchasedGuides;
import com.example.guides.security.JwtTokenProvider;
import com.example.guides.service.ChapterService;
import com.example.guides.service.GuideService;
import com.example.guides.service.PersonService;
import com.example.guides.service.PurchasedGuidesService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/guides")
@Tag(name = "Контроллер для работы с гайдами")
@AllArgsConstructor
@CrossOrigin("http://localhost:8081")
public class GuideController {

    private final GuideService guideService;
    private final PersonService personService;
    private final PurchasedGuidesService purchasedGuidesService;
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
    public GuideDTO findById(@Parameter(name = "Идентификатор гайда")
                             @PathVariable long id) {
        Optional<Guide> optionalGuide = guideService.findById(id);
        if (optionalGuide.isEmpty()) {
            return null;
        }
        return fromGuide(optionalGuide.get());
    }

    @GetMapping("/{id}/purchase")
    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Приобрести гайд")
    public ResponseEntity<?> purchaseGuide(@Parameter(name = "Идентификатор гайда")
                                               @PathVariable long id,
                                           @Parameter(name = "Токен пользователя")
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        Optional<Guide> optionalGuide = guideService.findById(id);
        Optional<Person> optionalPerson = getPersonByToken(token);
        if (optionalGuide.isEmpty()) {
            return new ResponseEntity<>("Guide not found", HttpStatus.BAD_REQUEST);
        } else if (optionalPerson.isEmpty()) {
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        }
        Guide guide = optionalGuide.get();
        Person person = optionalPerson.get();
        setNewEarnings(guide);
        saveNewPurchasedGuide(person, guide);
        guideService.save(guide);
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
        defineLanguage(guide);
        guideService.save(guide);
        saveChapters(guide, guide.getChapters());
        return new ResponseEntity<>("Ok", HttpStatus.OK);
    }

    private void defineLanguage(Guide guide) {
        StringBuilder builder = new StringBuilder();
        builder.append(guide.getDescription());
        for (Chapter chapter : guide.getChapters()) {
            builder.append(chapter.getText());
        }
        String fullText = builder.toString();
        guide.setLanguage(countSymbols(fullText));
    }

    private Language countSymbols(String fullText) {
        Pattern russianPattern = Pattern.compile("[а-яА-ЯёЁ]");
        Matcher russianMatcher = russianPattern.matcher(fullText);
        Pattern englishPattern = Pattern.compile("[a-zA-Z]");
        Matcher englishMatcher = englishPattern.matcher(fullText);
        int russianCount = 0;
        while (russianMatcher.find()) {
            russianCount++;
        }
        int englishCount = 0;
        while (englishMatcher.find()) {
            englishCount++;
        }
        float totalCount = russianCount + englishCount;
        boolean twentyPercentsOfFullText = totalCount / 5 <= russianCount;
        if (twentyPercentsOfFullText) {
            return Language.RUSSIAN;
        }
        return Language.ENGLISH;
    }

    private void setNewEarnings(Guide guide) {
        int newCount = guide.getCount() + 1;
        guide.setCount(newCount);
        guide.setEarnings(newCount * guide.getPrice());
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

    private void saveNewPurchasedGuide(Person person, Guide guide) {
        PurchasedGuides purchasedGuides = new PurchasedGuides(person, guide);
        purchasedGuidesService.save(purchasedGuides);
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
