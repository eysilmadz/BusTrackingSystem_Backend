package com.RotaDurak.RotaDurak.planner;

import com.RotaDurak.RotaDurak.dto.SegmentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// /api/planner?fromLat=...&fromLon=...&toLat=...&toLon=...&type=TIME
@RestController
@RequestMapping("/api/planner")
public class PlannerController {
    @Autowired private PlannerService plannerService;

    @GetMapping
    public List<Long> plan(
            @RequestParam double fromLat,
            @RequestParam double fromLon,
            @RequestParam double toLat,
            @RequestParam double toLon,
            @RequestParam CostType type
    ) {
        return plannerService.planRoute(fromLat,fromLon,toLat,toLon,type);
    }

    /** SegmentDto listesi döner */
    @GetMapping("/segments")
    public List<SegmentDto> planSegments(
            @RequestParam double fromLat,
            @RequestParam double fromLon,
            @RequestParam double toLat,
            @RequestParam double toLon,
            @RequestParam CostType type
    ) {
        return plannerService.planRouteSegments(fromLat, fromLon, toLat, toLon, type);
    }
}
