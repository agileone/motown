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
package io.motown.vas.v10.soap;

import io.motown.vas.v10.soap.schema.ChargePoint;
import io.motown.vas.viewmodel.persistence.entities.ChargingStation;
import org.junit.Before;
import org.junit.Test;

import static io.motown.domain.api.chargingstation.test.ChargingStationTestUtils.CHARGING_STATION_ID;

public class VasConversionServiceTest {

    private VasConversionService service;

    @Before
    public void setup() {
        service = new VasConversionService();
    }

    @Test
    public void getVasRepresentationNewChargingStationNoExceptions() {
        service.getVasRepresentation(new ChargingStation(CHARGING_STATION_ID.getId()));
    }

    @Test
    public void getVasRepresentationNewChargingStationValidateReturnValue() {
        ChargingStation cs = new ChargingStation(CHARGING_STATION_ID.getId());

        ChargePoint vasRepresentation = service.getVasRepresentation(cs);

        VasSoapTestUtils.compareChargingStationToVasRepresentation(cs, vasRepresentation);
    }

    @Test
    public void getVasRepresentationFilledChargingStationValidateReturnValue() {
        ChargingStation cs = VasSoapTestUtils.getConfiguredAndFilledChargingStation();

        ChargePoint vasRepresentation = service.getVasRepresentation(cs);

        VasSoapTestUtils.compareChargingStationToVasRepresentation(cs, vasRepresentation);
    }


}
