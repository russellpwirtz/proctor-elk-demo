package com.dev9.proctorDemo.domain;

import com.dev9.proctorDemo.GeoRangeTest;
import com.dev9.proctorDemo.GeoRangeTest;
import com.dev9.proctorDemo.GeoRangeTestManager;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.indeed.proctor.common.Identifiers;
import com.indeed.proctor.common.Proctor;
import com.indeed.proctor.common.ProctorResult;
import com.indeed.proctor.common.ProctorSpecification;
import com.indeed.proctor.common.ProctorUtils;
import com.indeed.proctor.common.UrlProctorLoader;
import com.indeed.proctor.common.model.TestType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Random;

import static net.logstash.logback.marker.Markers.appendEntries;

@Component
@Slf4j
public class DefinitionManager {

  private static final String DEFAULT_SPEC = "/com/dev9/proctorDemo/GeoRangeTest.json";

  public static final String DEFAULT_DEFINITION =
      "https://gist.githubusercontent.com/russellpwirtz/c88632755a07ddf4cd1b92b87022e1cd/raw/5816f5b1a25257242e7afa6f4b5588587d2063bd/proctor-location-test";

  private Map<String, Proctor> proctorCache = Maps.newHashMap();
  private Random random = new Random();

  public DefinitionManager() {
    try {
      final URLConnection uc = new URL(DEFAULT_DEFINITION).openConnection();
      uc.setDefaultUseCaches(false);
    } catch (Exception e) {
      log.error("Failed to disable caching", e);
    }
  }

  public GeoRangeTest getGeoRangeTest(
      @Nonnull final HttpServletRequest request,
      @Nonnull final HttpServletResponse response,
      @Nonnull String userId,
      @Nonnull String definitionUrl) {
    final Proctor proctor = load(definitionUrl, false);
    final GeoRangeTestManager groupsManager = new GeoRangeTestManager(Suppliers.ofInstance(proctor));
    final Identifiers identifiers = new Identifiers(TestType.USER, userId);
    final ProctorResult result = groupsManager.determineBuckets(request, response, identifiers, true);

    MDC.put("userKey", userId);

    GeoRangeTest groups = new GeoRangeTest(result);

    logResults(userId, groups);

    return groups;
  }

  public Proctor load(String definitionUrl, boolean forceReload) {
    Proctor proctor = proctorCache.get(definitionUrl);
    if (proctor != null && !forceReload) {
      return proctor;
    }

    try {
      ProctorSpecification spec = ProctorUtils.readSpecification(DefinitionManager.class.getResourceAsStream(DEFAULT_SPEC));
      UrlProctorLoader loader = new UrlProctorLoader(spec, definitionUrl + "?r=" + random.nextInt());
      proctor = loader.doLoad();

      proctorCache.put(definitionUrl, proctor);
    } catch (Throwable t) {
      log.error("Failed to load test spec/definition", t);
    }

    return proctor;
  }

  private void logResults(String userId, GeoRangeTest groups) {
    if (groups.getGeoTestPayload() != null) {
      log.info(appendEntries(getMap(userId, groups)), "bucket info");
    }
  }

  private Map<?, ?> getMap(String userId, GeoRangeTest groups) {
    return ImmutableMap.of(
        "userId", userId,
        "name", groups.getGeoTest().getFullName(),
        "payload", groups.getGeoTestPayload());
  }
}
