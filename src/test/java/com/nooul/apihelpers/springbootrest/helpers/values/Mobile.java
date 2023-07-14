package com.nooul.apihelpers.springbootrest.helpers.values;

import com.nooul.apihelpers.springbootrest.annotations.ValueObject;
import lombok.ToString;

import java.util.Objects;

@ToString
@ValueObject
public class Mobile {

    private String number;

    private Mobile(String number){
        this.number = addPlus(number);
    }

    private static String addPlus(String number) {
        if(!number.startsWith("+")) {
            return "+" + number;
        }
        return number;
    }


    public static Mobile fromString(String phoneNumber){
        return new Mobile(phoneNumber);
    }

    public String formatAsStringWithoutPlus(){
        return this.number.replace("+", "");
    }

    public String formatAsString() {
        return this.number;
    }

    public static String makeItRegionPrefixed(String phoneNumber) {

        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        }

        if (phoneNumber.startsWith("30")) {
            return "+" + phoneNumber;
        }

        return "+30" + phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mobile mobile = (Mobile) o;
        return number.equals(mobile.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    public static String getNumber(Mobile mobile) {
        return mobile.formatAsStringWithoutPlus();
    }
}