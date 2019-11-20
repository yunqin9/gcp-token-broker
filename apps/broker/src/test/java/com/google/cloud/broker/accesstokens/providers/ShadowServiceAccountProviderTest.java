// Copyright 2019 Google LLC
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.cloud.broker.accesstokens.providers;

import java.util.Map;

import static org.junit.Assert.*;
import com.google.cloud.broker.settings.SettingsOverride;
import org.junit.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import com.google.cloud.broker.settings.AppSettings;
import com.google.cloud.broker.accesstokens.AccessToken;


public class ShadowServiceAccountProviderTest {

    private static final String SCOPE = "https://www.googleapis.com/auth/devstorage.read_write";

    private static SettingsOverride backupSettings;

    @BeforeClass
    public static void setupClass() {
        // Override settings
        backupSettings = new SettingsOverride(Map.of(
            AppSettings.SHADOW_PROJECT, System.getenv().get("APP_SETTING_" + AppSettings.GCP_PROJECT),
            AppSettings.SHADOW_USERNAME_PATTERN, "%s-shadow",
            AppSettings.JWT_LIFE, 30
        ));
    }

    @AfterClass
    public static void teardDownClass() throws Exception {
        // Restore settings
        backupSettings.restore();
    }

    @Test
    public void testGoogleIdentity() throws Exception {
        try(SettingsOverride override = new SettingsOverride(Map.of(
            AppSettings.SHADOW_PROJECT, "MY_SHADOW_PROJECT",
            AppSettings.SHADOW_USERNAME_PATTERN, "xxx-%s-XXX"
        ))) {

            ShadowServiceAccountProvider provider = new ShadowServiceAccountProvider();
            assertEquals("xxx-alice-XXX@MY_SHADOW_PROJECT.iam.gserviceaccount.com", provider.getGoogleIdentity("alice@EXAMPLE.COM"));
            assertEquals("xxx-alice-XXX@MY_SHADOW_PROJECT.iam.gserviceaccount.com", provider.getGoogleIdentity("alice@EXAMPLE.NET"));
            assertEquals("xxx-alice-XXX@MY_SHADOW_PROJECT.iam.gserviceaccount.com", provider.getGoogleIdentity("alice"));

            try {
                provider.getGoogleIdentity("");
                fail("IllegalArgumentException not thrown");
            } catch (IllegalArgumentException e) {
            }

            try {
                provider.getGoogleIdentity("@EXAMPLE.NET");
                fail("IllegalArgumentException not thrown");
            } catch (IllegalArgumentException e) {
            }

            try {
                provider.getGoogleIdentity("@");
                fail("IllegalArgumentException not thrown");
            } catch (IllegalArgumentException e) {
            }
        }
    }

    @Test
    public void testSuccess() {
        ShadowServiceAccountProvider provider = new ShadowServiceAccountProvider();
        AccessToken accessToken = provider.getAccessToken("alice@EXAMPLE.COM", SCOPE);
        assertTrue(accessToken.getValue().length() > 0);
        assertTrue(accessToken.getExpiresAt() > 0);
    }

    @Test
    public void testUnauthorized() {
        ShadowServiceAccountProvider provider = new ShadowServiceAccountProvider();
        try {
            provider.getAccessToken("bob@EXAMPLE.COM", SCOPE);
            fail("StatusRuntimeException not thrown");
        } catch (StatusRuntimeException e) {
            assertEquals(Status.PERMISSION_DENIED.getCode(), e.getStatus().getCode());
        }
    }

}
