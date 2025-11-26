package com.genkey.partner.example;

import com.genkey.platform.rest.RemoteAccessService;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ServiceTestMatcher extends TypeSafeMatcher<RemoteAccessService> {

  @Override
  public void describeTo(Description description) {
    description.appendText("service succeed");
  }

  @Override
  public boolean matchesSafely(RemoteAccessService service) {
    return service.isSuccess();
  }

  public static Matcher<RemoteAccessService> serviceCallGood() {
    return new ServiceTestMatcher();
  }
}
