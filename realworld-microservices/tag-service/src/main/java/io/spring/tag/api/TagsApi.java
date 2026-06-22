package io.spring.tag.api;

import io.spring.tag.application.TagsQueryService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/tags")
@AllArgsConstructor
public class TagsApi {
    private TagsQueryService tagsQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTags() {
        Map<String, Object> result = new HashMap<>();
        result.put("tags", tagsQueryService.allTags());
        return ResponseEntity.ok(result);
    }
}
