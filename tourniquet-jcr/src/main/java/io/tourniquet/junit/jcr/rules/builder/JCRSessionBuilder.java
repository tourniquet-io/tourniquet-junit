/*
 * Copyright 2015-2016 DevCon5 GmbH, info@devcon5.ch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tourniquet.junit.jcr.rules.builder;

import io.tourniquet.junit.Builder;
import io.tourniquet.junit.jcr.rules.ActiveSession;
import io.tourniquet.junit.jcr.rules.ContentRepository;

/**
 * A builder for a {@link io.tourniquet.junit.jcr.rules.ActiveSession}. The {@link io.tourniquet.junit.jcr.rules.ActiveSession} rule requires {@link io.tourniquet.junit.jcr.rules.ContentRepository} as outer rule.
 *
 */
public class JCRSessionBuilder implements Builder<ActiveSession> {

    private final ActiveSession jcrSession;

    public JCRSessionBuilder(final ContentRepository contentRepository) {
        jcrSession = new ActiveSession(contentRepository);
    }

    @Override
    public ActiveSession build() {
        return jcrSession;
    }

}
