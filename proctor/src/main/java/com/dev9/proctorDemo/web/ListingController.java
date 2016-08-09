package com.dev9.proctorDemo.web;

import com.dev9.proctorDemo.GeoRangeTest;
import com.dev9.proctorDemo.domain.DefinitionManager;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@Slf4j
public class ListingController {

  @Autowired
  DefinitionManager definitionManager;

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public ResponseEntity handle(final HttpServletRequest request,
                               final HttpServletResponse response,
                               @RequestParam String userId,
                               @RequestParam(value = "q", required = false, defaultValue = "empty") String query
  ) {
    MDC.put("userKey", userId);

    final GeoRangeTest groups =
        definitionManager.getGeoRangeTest(request, response, userId, DefinitionManager.DEFAULT_DEFINITION);

    log.debug("Querying listings for search query: {}", query);

    return ResponseEntity.ok(ImmutableMap.of("demo", ImmutableMap.of(
        "userId", userId,
        "group number", groups.getGeoTestValue(),
        "bucket name", groups.getGeoTest(),
        "payload", groups.getGeoTestPayload())));
  }
}
