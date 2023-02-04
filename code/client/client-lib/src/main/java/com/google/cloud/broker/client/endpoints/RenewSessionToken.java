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

import com.google.cloud.broker.apps.brokerserver.protobuf.RenewSessionTokenRequest;
import com.google.cloud.broker.apps.brokerserver.protobuf.RenewSessionTokenResponse;
import com.google.cloud.broker.client.connect.BrokerGateway;
import com.google.cloud.broker.client.connect.BrokerServerInfo;

public class RenewSessionToken {

  public static long submit(BrokerServerInfo serverInfo, String sessionToken) {
    BrokerGateway gateway = new BrokerGateway(serverInfo);
    gateway.setSPNEGOToken();
    RenewSessionTokenRequest request =
        RenewSessionTokenRequest.newBuilder().setSessionToken(sessionToken).build();
    RenewSessionTokenResponse response = gateway.getStub().renewSessionToken(request);
    gateway.getManagedChannel().shutdown();
    return response.getExpiresAt();
  }
}
