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

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@code ProcessMeterValueCommand} is the command which is published when a charging station has sent a metervalue.
 */
public final class ProcessMeterValueCommand {

    @TargetAggregateIdentifier
    private final ChargingStationId chargingStationId;

    private TransactionId transactionId;
    private int connectorId;

    private List<MeterValue> meterValueList;

    /**
     * Creates a {@code ProcessMeterValueCommand} with an identifier.
     *
     * @param chargingStationId the identifier of the charging station.
     * @throws NullPointerException if {@code chargingStationId} is {@code null}.
     * @throws IllegalArgumentException if {@code connectorId} is negative.
     */
    public ProcessMeterValueCommand(ChargingStationId chargingStationId, TransactionId transactionId, int connectorId, List<MeterValue> meterValueList) {
        this.chargingStationId = checkNotNull(chargingStationId);
        this.transactionId = transactionId;
        checkArgument(connectorId >= 0);
        this.connectorId = connectorId;
        this.meterValueList = meterValueList;
    }

    /**
     * Gets the charging station identifier.
     *
     * @return the charging station identifier.
     */
    public ChargingStationId getChargingStationId() {
        return chargingStationId;
    }

    /**
     * Gets the optional transaction identifier.
     *
     * @return the transaction identifier
     */
    public TransactionId getTransactionId() {
        return transactionId;
    }

    /**
     * Gets the listing of meter values
     * @return
     */
    public List<MeterValue> getMeterValueList() {
        return meterValueList;
    }

    /**
     * Gets the connector identifier.
     *
     * @return the connector identifier
     */
    public int getConnectorId() {
        return connectorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessMeterValueCommand that = (ProcessMeterValueCommand) o;

        if (connectorId != that.connectorId) return false;
        if (!chargingStationId.equals(that.chargingStationId)) return false;
        if (meterValueList != null ? !meterValueList.equals(that.meterValueList) : that.meterValueList != null)
            return false;
        if (transactionId != null ? !transactionId.equals(that.transactionId) : that.transactionId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = chargingStationId.hashCode();
        result = 31 * result + (transactionId != null ? transactionId.hashCode() : 0);
        result = 31 * result + connectorId;
        result = 31 * result + (meterValueList != null ? meterValueList.hashCode() : 0);
        return result;
    }
}
