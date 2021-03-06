/**
 * Copyright (C) 2013 Motown.IO (info@motown.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.motown.domain.api.chargingstation;

import io.motown.domain.api.security.IdentityContext;

import java.util.Objects;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@code AuthorizationListVersionReceivedEvent} is the event which is published when the authorization list version
 * has been received from the charging station.
 */
public final class AuthorizationListVersionReceivedEvent {

    private final ChargingStationId chargingStationId;

    private final int version;

    private final IdentityContext identityContext;

    /**
     * Creates a {@code AuthorizationListVersionReceivedEvent} with an identifier and a version.
     *
     * @param chargingStationId the identifier of the charging station.
     * @param version           the current version of the authorization list on the charging station.
     * @param identityContext   identity context.
     * @throws NullPointerException if {@code chargingStationId} or {@code identityContext} is {@code null}.
     */
    public AuthorizationListVersionReceivedEvent(ChargingStationId chargingStationId, int version, IdentityContext identityContext) {
        this.chargingStationId = checkNotNull(chargingStationId);
        this.version = version;
        this.identityContext = checkNotNull(identityContext);
    }

    /**
     * Gets the charging station identifier.
     *
     * @return the charging station identifier.
     */
    public ChargingStationId getChargingStationId() {
        return this.chargingStationId;
    }

    /**
     * @return the current version of the authorization list on the charging station
     */
    public int getVersion() {
        return version;
    }

    /**
     * Gets the identity context.
     *
     * @return the identity context.
     */
    public IdentityContext getIdentityContext() {
        return identityContext;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chargingStationId, version, identityContext);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AuthorizationListVersionReceivedEvent other = (AuthorizationListVersionReceivedEvent) obj;
        return Objects.equals(this.chargingStationId, other.chargingStationId) && Objects.equals(this.version, other.version) && Objects.equals(this.identityContext, other.identityContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this)
                .add("chargingStationId", chargingStationId)
                .add("version", version)
                .add("identityContext", identityContext)
                .toString();
    }
}
