package swyp.hobbi.swyphobbiback.common.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import swyp.hobbi.swyphobbiback.common.repository.PostConstructRepository;
import swyp.hobbi.swyphobbiback.hobbytag.domain.HobbyTag;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostConstructService {
    private final PostConstructRepository postConstructRepository;

    @PostConstruct
    public void init() {
        List<HobbyTag> hobbyTags = postConstructRepository.findAll();
        if(hobbyTags.isEmpty()) {
            postConstructRepository.insertHobbyTag();
        }
    }
}
