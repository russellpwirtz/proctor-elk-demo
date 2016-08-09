package com.dev9.proctorDemo.web;

import com.dev9.proctorDemo.ProctorGroups;
import com.dev9.proctorDemo.domain.DefinitionManager;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@Slf4j
public class ListingController {

  @Autowired
  DefinitionManager definitionManager;

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public ResponseEntity handle(final HttpServletRequest request,
                               final HttpServletResponse response,
                               @RequestParam String userId) {
    final ProctorGroups groups =
        definitionManager.getProctorGroups(request, response, userId, DefinitionManager.DEFAULT_DEFINITION);

    return ResponseEntity.ok(ImmutableMap.of("demo", ImmutableMap.of(
        "userId", userId,
        "group number", groups.getLocationTestValue(),
        "bucket name", groups.getLocationTest(),
        "payload", groups.getLocationTestPayload())));
  }
}
