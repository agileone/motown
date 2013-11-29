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
package io.motown.operatorapi.json.commands;

import io.motown.domain.api.chargingstation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDomainCommandGateway implements DomainCommandGateway {

    private static final Logger log = LoggerFactory.getLogger(TestDomainCommandGateway.class);


    @Override
    public void send(RequestUnlockConnectorCommand command) {
        log.debug("RequestUnlockConnectorCommand:" + command.toString());
    }

    @Override
    public void send(ConfigureChargingStationCommand command) {
        log.debug("ConfigureChargingStationCommand:" + command.toString());
    }

    @Override
    public void sendAndWait(RegisterChargingStationCommand command) {
        log.debug("CreateChargingStationCommand:" + command.toString());
    }

}