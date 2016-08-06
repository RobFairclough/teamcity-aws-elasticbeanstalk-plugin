/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.runner.elasticbeanstalk;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.util.CollectionsUtil;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static jetbrains.buildServer.runner.elasticbeanstalk.ElasticBeanstalkConstants.*;
import static jetbrains.buildServer.util.amazon.AWSCommonParams.*;
import static org.assertj.core.api.BDDAssertions.then;

public class ParametersValidatorTest extends BaseTestCase {
  @Test
  public void mandatory_params() {
    then(validate()).as("Must detect empty params").hasSize(9).
        containsEntry(APP_NAME_PARAM, "Application Name mustn't be empty").
        containsEntry(ENV_NAME_PARAM, "Environment Name mustn't be empty").
        containsEntry(S3_BUCKET_NAME_PARAM, "S3 bucket mustn't be empty").
        containsEntry(S3_OBJECT_KEY_PARAM, "S3 object key mustn't be empty").
        containsEntry(APP_VERSION_PARAM, "Application Version mustn't be empty").
        containsEntry(REGION_NAME_PARAM, "AWS region mustn't be empty").
        containsEntry(CREDENTIALS_TYPE_PARAM, "Credentials type mustn't be empty").
        containsEntry(ACCESS_KEY_ID_PARAM, "Access key ID mustn't be empty").
        containsEntry(SECURE_SECRET_ACCESS_KEY_PARAM, "Secret access key mustn't be empty");
  }

  @Test
  public void s3_bucket_slashes() {
    then(validate(S3_BUCKET_NAME_PARAM, "abra/kadabra")).as("Must detect slashes in s3 bucket name").
        containsEntry(S3_BUCKET_NAME_PARAM, "S3 bucket mustn't contain / characters. For addressing folders use S3 object key parameter");
  }

  @Test
  public void s3_object_key_unsafe_chars() {
    then(validate(S3_OBJECT_KEY_PARAM, "abra~kadabra")).as("Must detect unsafe characters in s3 object key").
        containsEntry(S3_OBJECT_KEY_PARAM, "S3 object key must contain only safe characters");
  }

  @Test
  public void unexpected_wait_timeout() {
    then(validate(WAIT_FLAG_PARAM, "true", WAIT_TIMEOUT_SEC_PARAM, "10min")).as("Must detect unexpected wait timeout").
        containsEntry(WAIT_TIMEOUT_SEC_PARAM, "Timeout (seconds) must be a positive integer value");
  }

  @Test
  public void unexpected_wait_poll_interval() throws Exception {
    then(validateRuntime(
        params(WAIT_FLAG_PARAM, "true"),
        params(WAIT_POLL_INTERVAL_SEC_CONFIG_PARAM, "50sec"))).
        as("Must detect unexpected wait poll interval").
        containsEntry(WAIT_POLL_INTERVAL_SEC_CONFIG_PARAM, "elasticbeanstalk.wait.poll.interval.sec must be a positive integer value");
  }

  @NotNull
  private Map<String, String> validate(String... pairs) {
    return ParametersValidator.validateSettings(params(pairs));
  }

  @NotNull
  private Map<String, String> validateRuntime(@NotNull Map<String, String> runnerParams, @NotNull Map<String, String> configParams) throws IOException {
    return ParametersValidator.validateRuntime(runnerParams, configParams, createTempDir());
  }

  @NotNull
  private Map<String, String> params(String... pairs) {
    return CollectionsUtil.<String>asMap(pairs);
  }
}
