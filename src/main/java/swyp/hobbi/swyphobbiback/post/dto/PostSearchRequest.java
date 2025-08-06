package swyp.hobbi.swyphobbiback.post.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PostSearchRequest {
    private String titlePlusContent;
    private String authorNickname;
    private String mbtiLetters;
    private List<String> hobbyTags;
}
