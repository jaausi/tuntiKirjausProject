package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.TestUtils;
import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.utils.TuntiKirjausDao;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MainViewServiceTest {

    private TuntiKirjausDao tuntiKirjausDao = mock(TuntiKirjausDao.class);
    private List<TuntiKirjaus> tuntiKirjausList = TestUtils.getTuntikirjausListMock(5);
    private MainViewService mainViewService;

    @BeforeEach
    void setup(){
        when(tuntiKirjausDao.getAllFrom(any())).thenReturn(tuntiKirjausList);
        mainViewService = new MainViewService();
    }

    @Test
    void givenTuntikirjausData_whenGetTuntiDataForTable_thenReturnCorrectHours() throws NoSuchFieldException, IllegalAccessException {
        // Given
        List<TuntiKirjaus> hoursForDayExpected = TestUtils.getTuntikirjausListMock(1);
        // When
        // Then
        ObservableList<TuntiKirjaus> hoursForDayActual = mainViewService.getTuntiDataForTable(TestUtils.getZeroDateTime().toLocalDate());
        assertEquals(5, hoursForDayActual.size());
        assertEquals(hoursForDayExpected, hoursForDayActual);
        assertNotSame(hoursForDayExpected, hoursForDayActual);
        assertNotNull(hoursForDayActual);
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
        assertNotSame(paivaListExpected, paivaListActual);
        assertNotNull(paivaListActual);
    }


}
