package swyp.hobbi.swyphobbiback.search.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class SearchRequest {
    private String titleAndContent;
    private String author;
    private List<String> hobbyTags;
    private String mbti;
}
