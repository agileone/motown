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

import io.motown.vas.v10.soap.schema.ChargePointStatus;
import io.motown.vas.viewmodel.model.ComponentStatus;
import org.junit.Test;

public class ChargePointStatusTest {

    @Test
    public void convertChargePointStatusToSoapRepresentationNoExceptions() {
        // every ChargePointStatus should be convertible to a SOAP ChargePointStatus
        for (ComponentStatus status : ComponentStatus.values()) {
            ChargePointStatus.fromValue(status.value());
        }
    }

}
