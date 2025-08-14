package swyp.hobbi.swyphobbiback.search.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.hobbi.swyphobbiback.search.dto.SearchRequest;
import swyp.hobbi.swyphobbiback.search.dto.SearchResponse;
import swyp.hobbi.swyphobbiback.search.service.SearchService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/title-content")
    public ResponseEntity<List<SearchResponse>> getAllSearchForTitleAndContent(
            @RequestBody SearchRequest request,
            @RequestParam(value = "lastId", required = false) Long lastId,
            @RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize
    ) {
        List<SearchResponse> responses = searchService.getAllSearchForTitleAndContent(request, lastId, pageSize);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/author")
    public ResponseEntity<List<SearchResponse>> getAllSearchForAuthor(
            @RequestBody SearchRequest request,
            @RequestParam(value = "lastId", required = false) Long lastId,
            @RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize

    ) {
        List<SearchResponse> responses = searchService.getAllSearchForAuthor(request, lastId, pageSize);
        return ResponseEntity.ok(responses);
    }
}
