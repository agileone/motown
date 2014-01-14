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
package io.motown.domain.app.axon;

import io.motown.domain.api.chargingstation.AuthorizationRequestedEvent;
import io.motown.domain.api.chargingstation.AuthorizeCommand;
import io.motown.domain.api.chargingstation.ChargingStationId;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.domain.EventMessage;
import org.axonframework.unitofwork.DefaultUnitOfWork;
import org.axonframework.unitofwork.UnitOfWorkListener;
import org.junit.Test;

import java.util.Collections;

import static io.motown.domain.app.axon.DomainAppTestUtils.getChargingStationId;
import static org.axonframework.domain.GenericEventMessage.asEventMessage;
import static org.junit.Assert.assertTrue;

public class CorrelationUnitOfWorkListenerTest {

    @Test(expected = NullPointerException.class)
    public void nullPointerExceptionThrownWhenCreatingWithCommandNull() {
        new CorrelationUnitOfWorkListener(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionThrownWhenCreatingWithCommandWithoutCorrectMetaData() {
        CommandMessage<AuthorizeCommand> command = new GenericCommandMessage<>(new AuthorizeCommand(new ChargingStationId("1"), "1"));
        new CorrelationUnitOfWorkListener(command);
    }

    @Test
    public void onEventRegisteredReturnsEventWithMetaData() {
        CommandMessage<AuthorizeCommand> command = new GenericCommandMessage<>(new AuthorizeCommand(getChargingStationId(), "1")).withMetaData(Collections.singletonMap("correlationId", "12345"));
        UnitOfWorkListener listener = new CorrelationUnitOfWorkListener(command);

        EventMessage event = listener.onEventRegistered(new DefaultUnitOfWork(), asEventMessage(new AuthorizationRequestedEvent(getChargingStationId(), "1")));

        assertTrue(event.getMetaData().get("correlationId").equals("12345"));
    }
}
