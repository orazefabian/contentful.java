package com.contentful.java.cda.interceptor;

import com.contentful.java.cda.interceptor.ContentfulUserAgentHeaderInterceptor.Section.OperatingSystem;
import com.contentful.java.cda.interceptor.ContentfulUserAgentHeaderInterceptor.Section.Version;

import org.junit.Test;

import static com.contentful.java.cda.interceptor.ContentfulUserAgentHeaderInterceptor.Section.OperatingSystem.Linux;
import static com.contentful.java.cda.interceptor.ContentfulUserAgentHeaderInterceptor.Section.OperatingSystem.Windows;
import static com.contentful.java.cda.interceptor.ContentfulUserAgentHeaderInterceptor.Section.OperatingSystem.macOS;
import static com.contentful.java.cda.interceptor.ContentfulUserAgentHeaderInterceptor.Section.Version.parse;
import static com.contentful.java.cda.interceptor.ContentfulUserAgentHeaderInterceptor.Section.app;
import static com.contentful.java.cda.interceptor.ContentfulUserAgentHeaderInterceptor.Section.integration;
import static com.contentful.java.cda.interceptor.ContentfulUserAgentHeaderInterceptor.Section.os;
import static com.contentful.java.cda.interceptor.ContentfulUserAgentHeaderInterceptor.Section.platform;
import static com.contentful.java.cda.interceptor.ContentfulUserAgentHeaderInterceptor.Section.sdk;
import static com.google.common.truth.Truth.assertThat;

public class ContentfulUserAgentHeaderInterceptorTest {
  @Test
  public void testCompleteHeaderGetsCreated() {
    final ContentfulUserAgentHeaderInterceptor header =
        new ContentfulUserAgentHeaderInterceptor(
            app("app", parse("1.0.0")),
            integration("int", parse("2.1.0")),
            sdk("sdk", parse("3.0.1")),
            platform("plat", parse("4.0.0-dev234")),
            os(OperatingSystem.Linux, parse("5.1.2-ASDF"))
        );

    final String value = header.getValue();
    final String name = header.getName();

    assertThat(name).isEqualTo("X-Contentful-User-Agent");
    assertThat(value)
        .isEqualTo("app app/1.0.0; " +
            "integration int/2.1.0; " +
            "sdk sdk/3.0.1; " +
            "platform plat/4.0.0-dev234; " +
            "os Linux/5.1.2-ASDF; ");
  }

  @Test
  public void testConvertJavaStyleVersions() {
    assertThat(parse("1.8.0_0123456780-b17").toString()).isEqualTo("1.8.0");
  }

  @Test
  public void testIncompleteHeaderIsFine() {
    final ContentfulUserAgentHeaderInterceptor header =
        new ContentfulUserAgentHeaderInterceptor(
            app("app", null)
        );

    final String value = header.getValue();
    final String name = header.getName();

    assertThat(name).isEqualTo("X-Contentful-User-Agent");
    assertThat(value).isEqualTo("app app; ");
  }

  @Test
  public void testTwiceSameNameTakesLast() {
    final ContentfulUserAgentHeaderInterceptor header =
        new ContentfulUserAgentHeaderInterceptor(
            app("foo", parse("1.0.0")),
            app("bar", parse("2.0.0"))
        );

    final String value = header.getValue();
    final String name = header.getName();

    assertThat(name).isEqualTo("X-Contentful-User-Agent");
    assertThat(value).isEqualTo("app bar/2.0.0; ");
  }

  @Test
  public void testNoNameInPairIgnoresApp() {
    assertThat(app(null, parse("1.0.0"))).isNull();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPairThrows() {
    new ContentfulUserAgentHeaderInterceptor();
  }

  @Test
  public void parsingNegativeVersionIgnoresVersion() {
    assertThat(parse("-1.0.0")).isNull();
  }

  @Test
  public void parsingGarbageIgnoresVersion() {
    assertThat(parse("♻")).isNull();
  }

  @Test
  public void createVersionDirectly() {
    final Version version = new Version(1, 2, 3);

    assertThat(version.toString()).isEqualTo("1.2.3");
    assertThat(version.getMajor()).isEqualTo(1);
    assertThat(version.getMinor()).isEqualTo(2);
    assertThat(version.getPatch()).isEqualTo(3);
    assertThat(version.getStability()).isNull();
  }

  @Test
  public void createVersionWithStabilityDirectly() {
    final Version version = new Version(1, 2, 3, "stable");

    assertThat(version.toString()).isEqualTo("1.2.3-stable");
    assertThat(version.getMajor()).isEqualTo(1);
    assertThat(version.getMinor()).isEqualTo(2);
    assertThat(version.getPatch()).isEqualTo(3);
    assertThat(version.getStability()).isEqualTo("stable");
  }

  @Test
  public void parsingVersionWithWrongStabilityIgnoresStability() {
    assertThat(parse("1.0.0-🤖").toString()).isEqualTo("1.0.0");
  }

  @Test
  public void missingPatchVersionNumberDoesNotThrow() {
    assertThat(parse("1.0").toString()).isEqualTo("1.0.0");
  }

  @Test
  public void nullVersionIsIgnored() {
    assertThat(parse(null)).isNull();
  }

  @Test
  public void allZeroVersionGetsIgnored() {
    assertThat(parse("0.0")).isNull();
  }

  @Test
  public void parsingOS() {
    assertThat(OperatingSystem.parse("Linux   i386   1.5.0_07")).isEqualTo(Linux);
    assertThat(OperatingSystem.parse("Linux   amd64   1.5.0_05")).isEqualTo(Linux);
    assertThat(OperatingSystem.parse("SunOS   x86   1.5.0_04")).isEqualTo(Linux);
    assertThat(OperatingSystem.parse("SunOS   sparc   1.5.0_02")).isEqualTo(Linux);
    assertThat(OperatingSystem.parse("FreeBSD   i386   1.4.2-p7")).isEqualTo(Linux);
    assertThat(OperatingSystem.parse("SomeOs   x86   1.5.0_02")).isEqualTo(Linux);
    assertThat(OperatingSystem.parse("Mac OS X   ppc   1.5.0_06")).isEqualTo(macOS);
    assertThat(OperatingSystem.parse("Mac OS X   i386   1.5.0_06")).isEqualTo(macOS);
    assertThat(OperatingSystem.parse("Windows XP   x86   1.5.0_07")).isEqualTo(Windows);
    assertThat(OperatingSystem.parse("Windows 2003   x86   1.5.0_07")).isEqualTo(Windows);
    assertThat(OperatingSystem.parse("Windows 2000   x86   1.5.0_02")).isEqualTo(Windows);
    assertThat(OperatingSystem.parse("Windows 98   x86   1.5.0_03")).isEqualTo(Windows);
    assertThat(OperatingSystem.parse("Windows NT   x86   1.5.0_02")).isEqualTo(Windows);
  }
}