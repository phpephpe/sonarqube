/*
 * SonarQube
 * Copyright (C) 2009-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.scanner.cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.zip.InflaterInputStream;
import org.sonar.api.scanner.fs.InputProject;
import org.sonar.api.utils.MessageException;
import org.sonar.core.util.Protobuf;
import org.sonar.scanner.bootstrap.DefaultScannerWsClient;
import org.sonar.scanner.protocol.internal.ScannerInternal;
import org.sonar.scanner.protocol.internal.ScannerInternal.AnalysisCacheMsg;
import org.sonar.scanner.scan.branch.BranchConfiguration;
import org.sonar.scanner.scan.branch.BranchType;
import org.sonarqube.ws.client.GetRequest;
import org.sonarqube.ws.client.HttpException;
import org.sonarqube.ws.client.WsResponse;

/**
 * Loads plugin cache into the local storage
 */
public class AnalysisCacheLoader {
  static final String CONTENT_ENCODING = "Content-Encoding";
  static final String ACCEPT_ENCODING = "Accept-Encoding";
  private static final String URL = "api/analysis_cache/get";

  private final DefaultScannerWsClient wsClient;
  private final InputProject project;
  private final BranchConfiguration branchConfiguration;

  public AnalysisCacheLoader(DefaultScannerWsClient wsClient, InputProject project, BranchConfiguration branchConfiguration) {
    this.project = project;
    this.branchConfiguration = branchConfiguration;
    this.wsClient = wsClient;
  }

  public Optional<AnalysisCacheMsg> load() {
    String url = URL + "?project=" + project.key();
    if (branchConfiguration.referenceBranchName() != null) {
      url = url + "&branch=" + branchConfiguration.referenceBranchName();
    }

    GetRequest request = new GetRequest(url).setHeader(ACCEPT_ENCODING, "gzip");

    try (WsResponse response = wsClient.call(request); InputStream is = response.contentStream()) {
      Optional<String> contentEncoding = response.header(CONTENT_ENCODING);
      if (contentEncoding.isPresent() && contentEncoding.get().equals("gzip")) {
        return Optional.of(decompress(is));
      } else {
        return Optional.of(Protobuf.read(is, AnalysisCacheMsg.parser()));
      }
    } catch (HttpException e) {
      if (e.code() == 404) {
        return Optional.empty();
      }
      throw MessageException.of("Failed to download analysis cache: " + DefaultScannerWsClient.createErrorMessage(e));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to download analysis cache", e);
    }
  }

  private static AnalysisCacheMsg decompress(InputStream is) {
    try (InflaterInputStream iis = new InflaterInputStream(is)) {
      return Protobuf.read(iis, ScannerInternal.AnalysisCacheMsg.parser());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to decompress analysis cache", e);
    }
  }
}
