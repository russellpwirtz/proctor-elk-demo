package com.dev9.proctorDemo.domain;

import com.dev9.proctorDemo.ProctorGroups;
import com.dev9.proctorDemo.ProctorGroupsManager;
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

  private static final String DEFAULT_SPEC = "/com/dev9/proctorDemo/ProctorGroups.json";

  public static final String DEFAULT_DEFINITION =
      "https://gist.githubusercontent.com/russellpwirtz/c88632755a07ddf4cd1b92b87022e1cd/raw/7862f3358f6f584fbc12435b52e16a947050a56f/proctor-location-test";

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

  public ProctorGroups getProctorGroups(
      @Nonnull final HttpServletRequest request,
      @Nonnull final HttpServletResponse response,
      @Nonnull String userId,
      @Nonnull String definitionUrl) {
    final Proctor proctor = load(definitionUrl, false);
    final ProctorGroupsManager groupsManager = new ProctorGroupsManager(Suppliers.ofInstance(proctor));
    final Identifiers identifiers = new Identifiers(TestType.USER, userId);
    final ProctorResult result = groupsManager.determineBuckets(request, response, identifiers, true);

    ProctorGroups groups = new ProctorGroups(result);

    ImmutableMap map = ImmutableMap.of(
        "userId", userId,
        "name", groups.getLocationTest().getFullName(),
        "payload", groups.getLocationTestPayload());

    log.info(appendEntries(map), "bucket info");

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
}
