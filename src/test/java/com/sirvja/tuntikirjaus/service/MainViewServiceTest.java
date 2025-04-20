package com.sirvja.tuntikirjaus.service;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class MainViewServiceTest {

    TuntiKirjausService mockedTuntikirjausService = mock(TuntiKirjausService.class);
    AlertService alertService = mock(AlertService.class);
    MainViewService mainViewService = new MainViewService(mockedTuntikirjausService, alertService);

    @Test
    void test1(){

    }
}
