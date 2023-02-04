// Copyright 2020 Google LLC
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.cloud.broker.client.endpoints;

import com.google.cloud.broker.apps.brokerserver.protobuf.GetSessionTokenRequest;
import com.google.cloud.broker.apps.brokerserver.protobuf.GetSessionTokenResponse;
import com.google.cloud.broker.client.connect.BrokerGateway;
import com.google.cloud.broker.client.connect.BrokerServerInfo;

public class GetSessionToken {

  public static String submit(
      BrokerServerInfo serverInfo,
      String owner,
      String renewer,
      Iterable<String> scopes,
      String target) {
    BrokerGateway gateway = new BrokerGateway(serverInfo);
    try {
      gateway.setSPNEGOToken();
    } catch (Exception e) {
      throw new RuntimeException(
          String.format(
              "Error while getting SPNEGO token for owner=`%s`, renewer=`%s`, scopes=`%s`, target=`%s`",
              owner, renewer, scopes, target),
          e);
    }
    GetSessionTokenRequest request =
        GetSessionTokenRequest.newBuilder()
            .addAllScopes(scopes)
            .setOwner(owner)
            .setRenewer(renewer)
            .setTarget(target)
            .build();
    GetSessionTokenResponse response = gateway.getStub().getSessionToken(request);
    gateway.getManagedChannel().shutdown();
    return response.getSessionToken();
  }
}
