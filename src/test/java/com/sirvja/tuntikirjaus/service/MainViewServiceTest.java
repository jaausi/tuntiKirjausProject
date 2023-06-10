package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.TestUtils;
import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.utils.TuntiKirjausDao;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MainViewServiceTest {

    private TuntiKirjausDao tuntiKirjausDao = mock(TuntiKirjausDao.class);
    private ObservableList<TuntiKirjaus> tuntiKirjausList = TestUtils.getTuntikirjausListMock(5);
    private MainViewService mainViewService;

    @BeforeEach
    void setup(){
        when(tuntiKirjausDao.getAllFrom(any())).thenReturn(Optional.of(tuntiKirjausList));
        mainViewService = new MainViewService(tuntiKirjausDao);
    }

    @Test
    void givenTuntikirjausData_whenGetTuntiDataForTable_thenReturnCorrectHours() throws NoSuchFieldException, IllegalAccessException {
        // Given
        ObservableList<TuntiKirjaus> hoursForDayExpected = TestUtils.getTuntikirjausListMock(1);
        // When
        mainViewService.setCurrentDate(new Paiva(TestUtils.getZeroDateTime().toLocalDate()));
        // Then
        ObservableList<TuntiKirjaus> hoursForDayActual = mainViewService.getTuntiDataForTable();
        assertEquals(5, hoursForDayActual.size());
        assertEquals(hoursForDayExpected, hoursForDayActual);
    }

    @Test
    void givenTuntikirjausData_whenGetPaivaListForTable_thenReturnPaivasAsObservableArrayList() {
        // Given
        ObservableList<Paiva> paivaListExpected = TestUtils.getPaivaListMock(5);
        // When

        // Then
        ObservableList<Paiva> paivaListActual = mainViewService.getPaivaDataForTable();
        assertEquals(5, paivaListActual.size());
        assertEquals(paivaListExpected, paivaListActual);
    }


}
