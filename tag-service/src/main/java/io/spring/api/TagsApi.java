package io.spring.api;

import io.spring.application.TagsQueryService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "tags")
@AllArgsConstructor
public class TagsApi {
  private TagsQueryService tagsQueryService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getTags() {
    Map<String, Object> response = new HashMap<>();
    response.put("tags", tagsQueryService.allTags());
    return ResponseEntity.ok(response);
  }
}
