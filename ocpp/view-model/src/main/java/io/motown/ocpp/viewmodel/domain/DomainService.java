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
package io.motown.ocpp.viewmodel.domain;

import com.google.common.collect.Maps;
import io.motown.domain.api.chargingstation.*;
import io.motown.ocpp.viewmodel.persistence.entities.ChargingStation;
import io.motown.ocpp.viewmodel.persistence.entities.TransactionIdentifier;
import io.motown.ocpp.viewmodel.persistence.repostories.ChargingStationRepository;
import io.motown.ocpp.viewmodel.persistence.repostories.TransactionIdentifierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class DomainService {

    private static final Logger log = LoggerFactory.getLogger(DomainService.class);

    @Resource(name = "domainCommandGateway")
    private DomainCommandGateway commandGateway;

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Autowired
    private TransactionIdentifierRepository transactionIdentifierRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    public BootChargingStationResult bootChargingStation(ChargingStationId chargingStationId, String chargingStationAddress, String vendor, String model) {

        // Check if we know the charging station, in order to determine if it is registered or not
        ChargingStation chargingStation = chargingStationRepository.findOne(chargingStationId.getId());
        if(chargingStation == null) {
            chargingStation = new ChargingStation(chargingStationId.getId());
        }

        // Keep track of the address on which we can reach the charging station
        chargingStation.setIpAddress(chargingStationAddress);
        chargingStationRepository.save(chargingStation);

        Map<String, String> attributes = Maps.newHashMap();
        attributes.put("vendor", vendor);
        attributes.put("model", model);
        attributes.put("address", chargingStationAddress);

        commandGateway.send(new BootChargingStationCommand(chargingStationId, attributes));

        // TODO: Where should the heartbeat-interval (60) come from? - Mark van den Bergh, November 15th 2013
        return new BootChargingStationResult(chargingStation.isRegistered(), 60, new Date());
    }

    public AuthorizationResult authorize(ChargingStationId chargingStationId, String idTag){
        AuthorizeCommand command = new AuthorizeCommand(chargingStationId, idTag);
        AuthorizationResultStatus resultStatus = commandGateway.sendAndWait(command, 60, TimeUnit.SECONDS);

        AuthorizationResult result = new AuthorizationResult(idTag, resultStatus);
        return result;
    }
    
    public void configureChargingStation(ChargingStationId chargingStationId, Map<String, String> configurationItems) {
        ConfigureChargingStationCommand command = new ConfigureChargingStationCommand(chargingStationId, configurationItems);
        commandGateway.send(command);
    }

    public int startTransaction(ChargingStationId chargingStationId, int connectorId, String idTag, int meterStart, Date timestamp) {
        ChargingStation chargingStation = chargingStationRepository.findOne(chargingStationId.getId());
        if(chargingStation == null) {
            //TODO refuse start transaction because we (the OCPP module) don't know the charging station? - Mark van den Bergh, December 2nd 2013
        }

        String transactionId = generateTransactionIdentifier(chargingStationId);

        StartTransactionCommand command = new StartTransactionCommand(chargingStationId, transactionId, connectorId, idTag, meterStart, timestamp);
        commandGateway.send(command);

        return extractOcppTransactionIdentifier(transactionId);
    }

    public DomainCommandGateway getCommandGateway() {
        return commandGateway;
    }

    public void setCommandGateway(DomainCommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public String retrieveChargingStationAddress(ChargingStationId id) {
        ChargingStation chargingStation = chargingStationRepository.findOne(id.getId());

        return chargingStation != null? chargingStation.getIpAddress() : "";
    }

    /**
     * Generates a transaction identifier based on the charging station, the module (OCPP) and a auto-incremented number.
     *
     * @param chargingStationId charging station identifier to use when generating a transaction identifier.
     * @return transaction identifier based on the charging station, module and auto-incremented number.
     */
    private String generateTransactionIdentifier(ChargingStationId chargingStationId) {
        TransactionIdentifier identifier = new TransactionIdentifier();

        transactionIdentifierRepository.saveAndFlush(identifier); // flush to make sure the generated id is populated

        return String.format("%s_OCPP_%s", chargingStationId.getId(), entityManagerFactory.getPersistenceUnitUtil().getIdentifier(identifier));
    }

    /**
     * Extracts the OCPP transaction identifier (integer) from the generated transaction identifier.
     *
     * @param transactionIdentifier transaction identifier as generated by {@code generateTransactionIdentifier}.
     * @return integer which represents the identifier used in the OCPP protocol
     */
    private int extractOcppTransactionIdentifier(String transactionIdentifier) {
        String[] split = transactionIdentifier.split("_");
        return Integer.parseInt(split[split.length - 1]);
    }

}
