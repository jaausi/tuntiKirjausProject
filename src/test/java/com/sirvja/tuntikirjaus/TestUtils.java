package com.sirvja.tuntikirjaus;

import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TestUtils {

    private final static LocalDateTime zeroDateTime = LocalDateTime.of(
            LocalDate.of(2023, 1, 1),
            LocalTime.of(0,0,0)
    );


    public static LocalDateTime getZeroDateTime(){
        return zeroDateTime;
    }
    public static ObservableList<TuntiKirjaus> getTuntikirjausListMock(int amountOfDays){
        ObservableList<TuntiKirjaus> tuntiKirjausList = FXCollections.observableArrayList();

        for(int i = 0; i<amountOfDays; i++){
            final LocalDateTime thisDateTime = zeroDateTime.plusDays(i);
            tuntiKirjausList.add(new TuntiKirjaus(1, thisDateTime, thisDateTime.plusHours(1), "IBD-1 Koodaus", true)); // 1h
            tuntiKirjausList.add(new TuntiKirjaus(2, thisDateTime.plusHours(1), thisDateTime.plusHours(2).plusMinutes(30), "IBD-2 Katselmointi", true)); // 2h30min
            tuntiKirjausList.add(new TuntiKirjaus(2, thisDateTime.plusHours(2).plusMinutes(30), thisDateTime.plusHours(4), "oaw Lounas", true)); // 4h
            tuntiKirjausList.add(new TuntiKirjaus(2, thisDateTime.plusHours(4), thisDateTime.plusHours(6).plusMinutes(23), "IBD-3 Suunnittelu", true)); // 6h23min
            tuntiKirjausList.add(new TuntiKirjaus(2, thisDateTime.plusHours(6).plusMinutes(23), thisDateTime.plusHours(8).plusMinutes(23), "IBD-4 Koodaus", true)); // 8h23min
        }

        return tuntiKirjausList;
    }

    public static ObservableList<Paiva> getPaivaListMock(int amountOfDays) {
        ObservableList<Paiva> paivaList = FXCollections.observableArrayList();

        for(int i = 0; i<amountOfDays; i++){
            final LocalDate thisDate = zeroDateTime.plusDays(i).toLocalDate();
            paivaList.add(new Paiva(thisDate));
        }

        return paivaList.sorted();
    }

    /**
     * Field value can be mocked by calling for e.g. TestUtils.mockPrivateFieldOfClass(mainViewService, "currentDate", LocalDate.now());
     * @param classInstance
     * @param fieldName
     * @param mockValue
     * @param <T>
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static <T> void mockPrivateFieldOfClass(Object classInstance, String fieldName, T mockValue) throws NoSuchFieldException, IllegalAccessException {
        Field field = classInstance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(classInstance, mockValue);
    }


}
