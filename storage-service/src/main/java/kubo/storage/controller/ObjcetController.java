package kubo.storage.controller;

import kubo.storage.dto.ObjectInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import kubo.storage.service.StorageService;

@RestController
@RequestMapping("api/objects")
public class ObjcetController {

    @Autowired
    private StorageService storageService;


    @GetMapping("/key")
    public ObjectInfoResponse info (@PathVariable String key){
        return storageService.head(key);
    }

}
