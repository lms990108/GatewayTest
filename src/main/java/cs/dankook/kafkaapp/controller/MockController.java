package cs.dankook.kafkaapp.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockController {

    @GetMapping("/{path}")
    public ResponseEntity<String> handleRequest(@PathVariable String path, @RequestHeader HttpHeaders headers) {
        return ResponseEntity.ok("Mock Service received path: " + path + ", Headers: " + headers.toString());
    }
}

