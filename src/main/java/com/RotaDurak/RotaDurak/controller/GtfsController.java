package com.RotaDurak.RotaDurak.controller;

import com.RotaDurak.RotaDurak.service.GtfsImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/gtfs")
public class GtfsController {
    @Autowired
    private GtfsImportService gtfsImportService;

    @PostMapping("/import")
    public ResponseEntity<String> importGtfs(
            @RequestParam("file") MultipartFile file,
            @RequestParam("city") String cityName) {
        System.out.println(">>> GtfsController.importGtfs çağrıldı");
        try{
            gtfsImportService.importGtfs(file,cityName);
            return ResponseEntity.ok("Import successful");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Import failed"+ e.getMessage());
        }
    }
}






