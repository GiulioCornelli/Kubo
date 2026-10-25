package kubo.storage.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@RestController 
public class AmministartorController {


    @GetMapping("/healthCheck")
    public ResponseEntity<Map> getMethodName() {
        return ResponseEntity.ok(Map.of("value","Service is oline"));
    }
    

}
