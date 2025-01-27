package com.mega.uwrite.uwriterestapi.controller;

import com.mega.uwrite.uwriterestapi.exception.LoginFailureException;
import com.mega.uwrite.uwriterestapi.exception.StoryNotFoundException;
import com.mega.uwrite.uwriterestapi.exception.UserNotFoundException;
import com.mega.uwrite.uwriterestapi.model.Story;
import com.mega.uwrite.uwriterestapi.model.User;
import com.mega.uwrite.uwriterestapi.repository.StoryRepository;
import com.mega.uwrite.uwriterestapi.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static com.mega.uwrite.uwriterestapi.controller.UserController.SESSION_COOKIE_TAG;

@RestController
@RequestMapping("stories")
public class StoryController {
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;

    public StoryController(UserRepository userRepository, StoryRepository storyRepository) {
        this.userRepository = userRepository;
        this.storyRepository = storyRepository;
    }

    @GetMapping("/get-list/{userId}")
    public ResponseEntity<List<Story>> getStoriesByUserId(HttpSession session, @PathVariable Long userId) {
        if(isNotLoggedIn(session))
            throw new LoginFailureException("User not logged in.");

        Optional<User> user = userRepository.findById(userId);

        if(user.isEmpty()) throw new UserNotFoundException("Invalid User ID");

        return ResponseEntity.ok().body(user.get().getStories());
    }

    @GetMapping("/get-story/{storyId}")
    public ResponseEntity<Story> getStoryById(HttpSession session, @PathVariable Long storyId) {
        if(isNotLoggedIn(session))
            throw new LoginFailureException("User not logged in.");

        Optional<Story> story = storyRepository.findById(storyId);

        if(story.isEmpty()) throw new StoryNotFoundException("Invalid Story ID");

        return ResponseEntity.ok().body(story.get());
    }

    @PutMapping("/new-story")
    public ResponseEntity<Story> createStory(HttpSession session, @RequestBody StoryRequest storyRequest) {
        if(isNotLoggedIn(session))
            throw new LoginFailureException("User not logged in.");

        Optional<User> user = userRepository.findById(storyRequest.userId());

        if(user.isEmpty()) throw new UserNotFoundException("Invalid User ID");

        User foundUser = user.get();
        Story story = Story.builder()
                .storyContent(storyRequest.content())
                .publishedDate(Timestamp.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)))
                .build();
        foundUser.getStories().add(story);

        Story savedStory = storyRepository.save(story);
        userRepository.save(foundUser);
        return ResponseEntity.ok().body(savedStory);
    }

    private boolean isNotLoggedIn(HttpSession httpSession) {
        return httpSession.getAttribute(SESSION_COOKIE_TAG) == null;
    }
}

record StoryRequest(Long userId, String content) {}