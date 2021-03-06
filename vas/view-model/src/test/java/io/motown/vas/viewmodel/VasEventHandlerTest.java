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
package io.motown.vas.viewmodel;

import com.google.common.collect.Iterables;
import io.motown.domain.api.chargingstation.*;
import io.motown.domain.api.chargingstation.ComponentStatus;
import io.motown.vas.viewmodel.model.*;
import io.motown.vas.viewmodel.model.ConnectorType;
import io.motown.vas.viewmodel.persistence.entities.ChargingStation;
import io.motown.vas.viewmodel.persistence.entities.Evse;
import io.motown.vas.viewmodel.persistence.repostories.ChargingStationRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import static io.motown.domain.api.chargingstation.test.ChargingStationTestUtils.*;
import static io.motown.vas.viewmodel.VasViewModelTestUtils.deleteFromDatabase;
import static io.motown.vas.viewmodel.VasViewModelTestUtils.getRegisteredAndConfiguredChargingStation;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ContextConfiguration("classpath:vas-view-model-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class VasEventHandlerTest {

    private VasEventHandler eventHandler;

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    @Autowired
    private ConfigurationConversionService configurationConversionService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private VasSubscriberService subscriberService;

    @Before
    public void setUp() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.clear();
        deleteFromDatabase(entityManager, ChargingStation.class);

        eventHandler = new VasEventHandler();

        subscriberService = mock(VasSubscriberService.class);

        eventHandler.setChargingStationRepository(chargingStationRepository);
        eventHandler.setConfigurationConversionService(configurationConversionService);
        eventHandler.setSubscriberService(subscriberService);
    }

    @Test
    public void chargingStationBootedEventChargingStationCreated() {
        assertNull(getTestChargingStationFromRepository());

        eventHandler.handle(new ChargingStationCreatedEvent(CHARGING_STATION_ID, USER_IDENTITIES_WITH_ALL_PERMISSIONS, NULL_USER_IDENTITY_CONTEXT));

        ChargingStation cs = getTestChargingStationFromRepository();

        assertNotNull(cs);
        assertEquals(cs.getId(), CHARGING_STATION_ID.getId());
    }

    @Test
    public void chargingStationAcceptedEventChargingStationRegistered() {
        chargingStationRepository.createOrUpdate(new ChargingStation(CHARGING_STATION_ID.getId()));
        assertFalse(getTestChargingStationFromRepository().isRegistered());

        eventHandler.handle(new ChargingStationAcceptedEvent(CHARGING_STATION_ID, ROOT_IDENTITY_CONTEXT));

        assertTrue(getTestChargingStationFromRepository().isRegistered());
    }

    @Test
    public void chargingStationAcceptedEventUnknownChargingStationNoExceptionThrown() {
        assertNull(getTestChargingStationFromRepository());

        eventHandler.handle(new ChargingStationAcceptedEvent(CHARGING_STATION_ID, ROOT_IDENTITY_CONTEXT));
    }

    @Test
    public void chargingStationPlacedEventCoordinatesEmptyAddress() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());

        eventHandler.handle(new ChargingStationPlacedEvent(CHARGING_STATION_ID, COORDINATES, null, ACCESSIBILITY, IDENTITY_CONTEXT));

        ChargingStation cs = getTestChargingStationFromRepository();
        assertEquals(Double.valueOf(COORDINATES.getLatitude()), cs.getLatitude());
        assertEquals(Double.valueOf(COORDINATES.getLongitude()), cs.getLongitude());

        assertEquals(ACCESSIBILITY.name(), cs.getAccessibility());
    }

    @Test
    public void chargingStationPlacedEventAddressEmptyCoordinates() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());

        eventHandler.handle(new ChargingStationPlacedEvent(CHARGING_STATION_ID, null, ADDRESS, ACCESSIBILITY, IDENTITY_CONTEXT));

        ChargingStation cs = getTestChargingStationFromRepository();
        assertEquals(ADDRESS.getAddressLine1(), cs.getAddress());
        assertEquals(ADDRESS.getPostalCode(), cs.getPostalCode());
        assertEquals(ADDRESS.getRegion(), cs.getRegion());
        assertEquals(ADDRESS.getCity(), cs.getCity());
        assertEquals(ADDRESS.getCountry(), cs.getCountry());

        assertEquals(ACCESSIBILITY.name(), cs.getAccessibility());
    }

    @Test
    public void chargingStationPlacedEventAddressAndCoordinates() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());

        eventHandler.handle(new ChargingStationPlacedEvent(CHARGING_STATION_ID, COORDINATES, ADDRESS, ACCESSIBILITY, IDENTITY_CONTEXT));

        ChargingStation cs = getTestChargingStationFromRepository();
        assertEquals(Double.valueOf(COORDINATES.getLatitude()), cs.getLatitude());
        assertEquals(Double.valueOf(COORDINATES.getLongitude()), cs.getLongitude());
        assertEquals(ADDRESS.getAddressLine1(), cs.getAddress());
        assertEquals(ADDRESS.getPostalCode(), cs.getPostalCode());
        assertEquals(ADDRESS.getRegion(), cs.getRegion());
        assertEquals(ADDRESS.getCity(), cs.getCity());
        assertEquals(ADDRESS.getCountry(), cs.getCountry());

        assertEquals(ACCESSIBILITY.name(), cs.getAccessibility());
    }

    @Test
    public void chargingStationPlacedEventUnknownChargingStationNoExceptionThrown() {
        assertNull(getTestChargingStationFromRepository());

        eventHandler.handle(new ChargingStationPlacedEvent(CHARGING_STATION_ID, COORDINATES, ADDRESS, ACCESSIBILITY, IDENTITY_CONTEXT));
    }

    @Test
    public void chargingStationLocationImprovedEventCoordinatesEmptyAddress() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());

        eventHandler.handle(new ChargingStationLocationImprovedEvent(CHARGING_STATION_ID, COORDINATES, null, ACCESSIBILITY, IDENTITY_CONTEXT));

        ChargingStation cs = getTestChargingStationFromRepository();
        assertEquals(Double.valueOf(COORDINATES.getLatitude()), cs.getLatitude());
        assertEquals(Double.valueOf(COORDINATES.getLongitude()), cs.getLongitude());

        assertEquals(ACCESSIBILITY.name(), cs.getAccessibility());
    }

    @Test
    public void chargingStationLocationImprovedEventAddressEmptyCoordinates() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());

        eventHandler.handle(new ChargingStationLocationImprovedEvent(CHARGING_STATION_ID, null, ADDRESS, ACCESSIBILITY, IDENTITY_CONTEXT));

        ChargingStation cs = getTestChargingStationFromRepository();
        assertEquals(ADDRESS.getAddressLine1(), cs.getAddress());
        assertEquals(ADDRESS.getPostalCode(), cs.getPostalCode());
        assertEquals(ADDRESS.getRegion(), cs.getRegion());
        assertEquals(ADDRESS.getCity(), cs.getCity());
        assertEquals(ADDRESS.getCountry(), cs.getCountry());

        assertEquals(ACCESSIBILITY.name(), cs.getAccessibility());
    }

    @Test
    public void chargingStationLocationImprovedEventAddressAndCoordinates() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());

        eventHandler.handle(new ChargingStationLocationImprovedEvent(CHARGING_STATION_ID, COORDINATES, ADDRESS, ACCESSIBILITY, IDENTITY_CONTEXT));

        ChargingStation cs = getTestChargingStationFromRepository();
        assertEquals(Double.valueOf(COORDINATES.getLatitude()), cs.getLatitude());
        assertEquals(Double.valueOf(COORDINATES.getLongitude()), cs.getLongitude());
        assertEquals(ADDRESS.getAddressLine1(), cs.getAddress());
        assertEquals(ADDRESS.getPostalCode(), cs.getPostalCode());
        assertEquals(ADDRESS.getRegion(), cs.getRegion());
        assertEquals(ADDRESS.getCity(), cs.getCity());
        assertEquals(ADDRESS.getCountry(), cs.getCountry());

        assertEquals(ACCESSIBILITY.name(), cs.getAccessibility());
    }

    @Test
    public void chargingStationLocationImprovedEventUnknownChargingStationNoExceptionThrown() {
        assertNull(getTestChargingStationFromRepository());

        eventHandler.handle(new ChargingStationLocationImprovedEvent(CHARGING_STATION_ID, COORDINATES, ADDRESS, ACCESSIBILITY, IDENTITY_CONTEXT));
    }

    @Test
    public void chargingStationMovedEventCoordinatesEmptyAddress() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());

        eventHandler.handle(new ChargingStationMovedEvent(CHARGING_STATION_ID, COORDINATES, null, ACCESSIBILITY, IDENTITY_CONTEXT));

        ChargingStation cs = getTestChargingStationFromRepository();
        assertEquals(Double.valueOf(COORDINATES.getLatitude()), cs.getLatitude());
        assertEquals(Double.valueOf(COORDINATES.getLongitude()), cs.getLongitude());

        assertEquals(ACCESSIBILITY.name(), cs.getAccessibility());
    }

    @Test
    public void chargingStationMovedEventAddressEmptyCoordinates() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());

        eventHandler.handle(new ChargingStationMovedEvent(CHARGING_STATION_ID, null, ADDRESS, ACCESSIBILITY, IDENTITY_CONTEXT));

        ChargingStation cs = getTestChargingStationFromRepository();
        assertEquals(ADDRESS.getAddressLine1(), cs.getAddress());
        assertEquals(ADDRESS.getPostalCode(), cs.getPostalCode());
        assertEquals(ADDRESS.getRegion(), cs.getRegion());
        assertEquals(ADDRESS.getCity(), cs.getCity());
        assertEquals(ADDRESS.getCountry(), cs.getCountry());

        assertEquals(ACCESSIBILITY.name(), cs.getAccessibility());
    }

    @Test
    public void chargingStationMovedEventAddressAndCoordinates() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());

        eventHandler.handle(new ChargingStationMovedEvent(CHARGING_STATION_ID, COORDINATES, ADDRESS, ACCESSIBILITY, IDENTITY_CONTEXT));

        ChargingStation cs = getTestChargingStationFromRepository();
        assertEquals(Double.valueOf(COORDINATES.getLatitude()), cs.getLatitude());
        assertEquals(Double.valueOf(COORDINATES.getLongitude()), cs.getLongitude());
        assertEquals(ADDRESS.getAddressLine1(), cs.getAddress());
        assertEquals(ADDRESS.getPostalCode(), cs.getPostalCode());
        assertEquals(ADDRESS.getRegion(), cs.getRegion());
        assertEquals(ADDRESS.getCity(), cs.getCity());
        assertEquals(ADDRESS.getCountry(), cs.getCountry());

        assertEquals(ACCESSIBILITY.name(), cs.getAccessibility());
    }

    @Test
    public void chargingStationMovedEventUnknownChargingStationNoExceptionThrown() {
        assertNull(getTestChargingStationFromRepository());

        eventHandler.handle(new ChargingStationMovedEvent(CHARGING_STATION_ID, COORDINATES, ADDRESS, ACCESSIBILITY, IDENTITY_CONTEXT));
    }

    @Test
    public void chargingStationMadeReservableEventChargingStationReservable() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());

        eventHandler.handle(new ChargingStationMadeReservableEvent(CHARGING_STATION_ID, ROOT_IDENTITY_CONTEXT));

        assertTrue(getTestChargingStationFromRepository().isReservable());
    }

    @Test
    public void chargingStationMadeNotReservableEventChargingStationNotReservable() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());

        eventHandler.handle(new ChargingStationMadeNotReservableEvent(CHARGING_STATION_ID, ROOT_IDENTITY_CONTEXT));

        assertFalse(getTestChargingStationFromRepository().isReservable());
    }

    @Test
    public void chargingStationMadeReservableEventUnknownChargingStationNoExceptionThrown() {
        assertNull(getTestChargingStationFromRepository());

        eventHandler.handle(new ChargingStationMadeReservableEvent(CHARGING_STATION_ID, ROOT_IDENTITY_CONTEXT));
    }

    @Test
    public void chargingStationConfiguredEventUnknownChargingStationShouldCreateChargingStation() {
        assertNull(getTestChargingStationFromRepository());

        eventHandler.handle(new ChargingStationConfiguredEvent(CHARGING_STATION_ID, EVSES, NULL_USER_IDENTITY_CONTEXT));

        assertNotNull(getTestChargingStationFromRepository());
    }

    @Test
    public void chargingStationConfiguredEventChargingStationShouldBeConfigured() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());

        eventHandler.handle(new ChargingStationConfiguredEvent(CHARGING_STATION_ID, EVSES, NULL_USER_IDENTITY_CONTEXT));

        assertTrue(getTestChargingStationFromRepository().isConfigured());
    }

    @Test
    public void chargingStationConfiguredEventVerifyChargeMode() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());
        ChargeMode expectedChargeMode = ChargeMode.fromChargingProtocol(Iterables.get(EVSES, 0).getConnectors().get(0).getChargingProtocol());

        eventHandler.handle(new ChargingStationConfiguredEvent(CHARGING_STATION_ID, EVSES, NULL_USER_IDENTITY_CONTEXT));

        assertEquals(expectedChargeMode, getTestChargingStationFromRepository().getChargeMode());
    }

    @Test
    public void chargingStationConfiguredEventVerifyConnectorTypes() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());
        Set<ConnectorType> expectedConnectorTypes = configurationConversionService.getConnectorTypesFromEvses(EVSES);

        eventHandler.handle(new ChargingStationConfiguredEvent(CHARGING_STATION_ID, EVSES, NULL_USER_IDENTITY_CONTEXT));

        // not testing if Set with expected values contain the correct values, configurationConversionService has its own test set
        assertEquals(expectedConnectorTypes, getTestChargingStationFromRepository().getConnectorTypes());
    }

    @Test
    public void chargingStationConfiguredEventVerifyEvses() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());
        Set<Evse> expectedEvses = configurationConversionService.getEvsesFromEventEvses(EVSES);

        eventHandler.handle(new ChargingStationConfiguredEvent(CHARGING_STATION_ID, EVSES, NULL_USER_IDENTITY_CONTEXT));

        // not testing if Set with expected values contain the correct values, configurationConversionService has its own test set
        assertEquals(expectedEvses, getTestChargingStationFromRepository().getEvses());
    }

    @Test
    public void chargingStationConfiguredEventVerifyChargingCapabilities() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());
        Set<ChargingCapability> expectedChargingCapabilities = configurationConversionService.getChargingCapabilitiesFromEvses(EVSES);

        eventHandler.handle(new ChargingStationConfiguredEvent(CHARGING_STATION_ID, EVSES, NULL_USER_IDENTITY_CONTEXT));

        // not testing if Set with expected values contain the correct values, configurationConversionService has its own test set
        assertEquals(expectedChargingCapabilities, getTestChargingStationFromRepository().getChargingCapabilities());
    }

    @Test
    public void chargingStationOpeningTimesSetEvent() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());
        eventHandler.handle(new ChargingStationOpeningTimesSetEvent(CHARGING_STATION_ID, OPENING_TIMES, IDENTITY_CONTEXT));

        ChargingStation chargingStation = getTestChargingStationFromRepository();
        assertEquals(OPENING_TIMES.size(), chargingStation.getOpeningTimes().size());
        OpeningTime[] cOT = OPENING_TIMES.toArray(new OpeningTime[OPENING_TIMES.size()]);
        io.motown.vas.viewmodel.persistence.entities.OpeningTime[] vOT = chargingStation.getOpeningTimes().toArray(new io.motown.vas.viewmodel.persistence.entities.OpeningTime[chargingStation.getOpeningTimes().size()]);
        assertEquals(cOT[0].getDay().value(), vOT[0].getDay().value());
        assertEquals(cOT[0].getTimeStart().getHourOfDay(), vOT[0].getTimeStart() / 60);
        assertEquals(cOT[0].getTimeStart().getMinutesInHour(), vOT[0].getTimeStart() % 60);
        assertEquals(cOT[0].getTimeStop().getHourOfDay(), vOT[0].getTimeStop() / 60);
        assertEquals(cOT[0].getTimeStop().getMinutesInHour(), vOT[0].getTimeStop() % 60);
    }

    @Test
    public void chargingStationOpeningTimesAddedEvent() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());
        eventHandler.handle(new ChargingStationOpeningTimesAddedEvent(CHARGING_STATION_ID, OPENING_TIMES, IDENTITY_CONTEXT));

        ChargingStation chargingStation = getTestChargingStationFromRepository();
        assertEquals(OPENING_TIMES.size(), chargingStation.getOpeningTimes().size());
        OpeningTime[] cOT = OPENING_TIMES.toArray(new OpeningTime[OPENING_TIMES.size()]);
        io.motown.vas.viewmodel.persistence.entities.OpeningTime[] vOT = chargingStation.getOpeningTimes().toArray(new io.motown.vas.viewmodel.persistence.entities.OpeningTime[chargingStation.getOpeningTimes().size()]);
        assertEquals(cOT[0].getDay().value(), vOT[0].getDay().value());
        assertEquals(cOT[0].getTimeStart().getHourOfDay(), vOT[0].getTimeStart() / 60);
        assertEquals(cOT[0].getTimeStart().getMinutesInHour(), vOT[0].getTimeStart() % 60);
        assertEquals(cOT[0].getTimeStop().getHourOfDay(), vOT[0].getTimeStop() / 60);
        assertEquals(cOT[0].getTimeStop().getMinutesInHour(), vOT[0].getTimeStop() % 60);
    }

    @Test
    public void chargingStationStatusNotificationReceivedEventUnknownChargingStationNoException() {
        assertNull(getTestChargingStationFromRepository());

        eventHandler.handle(new ChargingStationStatusNotificationReceivedEvent(CHARGING_STATION_ID, STATUS_NOTIFICATION, NULL_USER_IDENTITY_CONTEXT));
    }

    @Test
    public void chargingStationStatusNotificationAvailableReceivedEventVerifyChargingStationState() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());
        ChargingStation chargingStation = getTestChargingStationFromRepository();
        assertTrue(chargingStation.getState().equals(io.motown.vas.viewmodel.model.ComponentStatus.UNKNOWN));
        StatusNotification statusNotification = new StatusNotification(ComponentStatus.AVAILABLE, new Date(), new HashMap<String, String>());

        eventHandler.handle(new ChargingStationStatusNotificationReceivedEvent(CHARGING_STATION_ID, statusNotification, NULL_USER_IDENTITY_CONTEXT));

        assertTrue(getTestChargingStationFromRepository().getState().equals(io.motown.vas.viewmodel.model.ComponentStatus.AVAILABLE));
    }

    @Test
    public void chargingStationStatusNotificationVerifySubscriberServiceCall() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());
        StatusNotification statusNotification = new StatusNotification(ComponentStatus.AVAILABLE, FIVE_MINUTES_AGO, new HashMap<String, String>());

        eventHandler.handle(new ChargingStationStatusNotificationReceivedEvent(CHARGING_STATION_ID, statusNotification, NULL_USER_IDENTITY_CONTEXT));

        verify(subscriberService).updateSubscribers(getTestChargingStationFromRepository(), FIVE_MINUTES_AGO);
    }

    @Test
    public void componentStatusNotificationReceivedEventUnknownChargingStationNoException() {
        assertNull(getTestChargingStationFromRepository());

        eventHandler.handle(new ComponentStatusNotificationReceivedEvent(CHARGING_STATION_ID, ChargingStationComponent.EVSE, EVSE_ID, STATUS_NOTIFICATION, NULL_USER_IDENTITY_CONTEXT));
    }

    @Test
    public void componentStatusNotificationReceivedEventUnknownEvseNoException() {
        assertNull(getTestChargingStationFromRepository());

        eventHandler.handle(new ComponentStatusNotificationReceivedEvent(CHARGING_STATION_ID, ChargingStationComponent.EVSE, UNKNOWN_EVSE_ID, STATUS_NOTIFICATION, NULL_USER_IDENTITY_CONTEXT));
    }

    @Test
    public void componentStatusNotificationAvailableReceivedEventVerifyEvseState() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());
        ChargingStation chargingStation = getTestChargingStationFromRepository();
        assertTrue(chargingStation.getEvse(EVSE_ID.getNumberedId()).getState().equals(io.motown.vas.viewmodel.model.ComponentStatus.UNKNOWN));
        StatusNotification statusNotification = new StatusNotification(ComponentStatus.AVAILABLE, new Date(), new HashMap<String, String>());

        eventHandler.handle(new ComponentStatusNotificationReceivedEvent(CHARGING_STATION_ID, ChargingStationComponent.EVSE, EVSE_ID, statusNotification, NULL_USER_IDENTITY_CONTEXT));

        chargingStation = getTestChargingStationFromRepository();
        assertTrue(chargingStation.getEvse(EVSE_ID.getNumberedId()).getState().equals(io.motown.vas.viewmodel.model.ComponentStatus.AVAILABLE));
    }

    @Test
    public void componentStatusNotificationReceivedVerifySubscriptionServiceCall() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());
        StatusNotification statusNotification = new StatusNotification(ComponentStatus.AVAILABLE, FIVE_MINUTES_AGO, new HashMap<String, String>());

        eventHandler.handle(new ComponentStatusNotificationReceivedEvent(CHARGING_STATION_ID, ChargingStationComponent.EVSE, EVSE_ID, statusNotification, NULL_USER_IDENTITY_CONTEXT));

        verify(subscriberService).updateSubscribers(getTestChargingStationFromRepository(), FIVE_MINUTES_AGO);
    }

    @Test
    public void componentStatusNotificationReceivedForUnknownComponent() {
        chargingStationRepository.createOrUpdate(getRegisteredAndConfiguredChargingStation());
        StatusNotification statusNotification = new StatusNotification(ComponentStatus.AVAILABLE, FIVE_MINUTES_AGO, new HashMap<String, String>());

        // no exceptions thrown
        eventHandler.handle(new ComponentStatusNotificationReceivedEvent(CHARGING_STATION_ID, ChargingStationComponent.EVSE, UNKNOWN_EVSE_ID, statusNotification, NULL_USER_IDENTITY_CONTEXT));
    }

    private ChargingStation getTestChargingStationFromRepository() {
        return chargingStationRepository.findOne(CHARGING_STATION_ID.getId());
    }

}
