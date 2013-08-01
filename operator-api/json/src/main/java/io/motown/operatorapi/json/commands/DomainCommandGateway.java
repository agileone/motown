package io.motown.operatorapi.json.commands;

import io.motown.domain.api.chargingstation.RequestUnlockConnectorCommand;

interface DomainCommandGateway {
    void send(RequestUnlockConnectorCommand command);
}