package com.ivanov.providerdbcreator;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.fluttercode.datafactory.impl.DataFactory;

/**
 * This class describes typically being in use information about 
 * provider subscribers: First Name, Last Name, city, address, 
 * traffic load.
 * 
 * @author Aleksandr Ivanov
 */
public class User {
    private String firstName;
    private String lastName;
    private String city;
    private String address;
    
    /** 
     * User traffic information represented here as map with
     * key as Date type and value as Integer type. Date means
     * a minute on usage time line. Integer means load in bytes
     * at this minute.
     */
    private Map<Date, Integer> traffic;
    
    /** 
     * Constructs a User with randomly appropriated values. 
     * {@link org.fluttercode.datafactory.impl.DataFactory} is used
     * for user information generation.
     */
    public User() {
        DataFactory df = new DataFactory();
        
        /* 
         * Seed for DataFactory (and randomize after that) is 
         * required. Otherwise generation results become the same 
         * with previous program start results.
         */
        int seedForDataFactory = (int) (Math.random() * 1_000_000);
        
        df.randomize(seedForDataFactory);
        
        this.firstName = df.getFirstName();
        this.lastName = df.getLastName();
        this.city = df.getCity();
        this.address = df.getAddress();
    }

    /**
     * Returns a Map of traffic load history by minutes. Traffic
     * load in bytes is generated randomly with java.lang.Math.random()
     * method.
     * 
     * @param timePoints set of minutes which defines time points to
     * sum traffic load
     * @return Map with entries: minute - traffic load bytes
     */
    public Map<Date, Integer> loadTraffic(Set<Date> timePoints) {
        traffic = new TreeMap<>();
        
        for (Date date : timePoints) {
            int currentTraffic = (int) (Math.random() * 100_000_000);
            
            traffic.put(date, currentTraffic);
        }
        
        return traffic;
    }

    /**
     * User hashcode calculation based upon first name, last name,
     * city and address.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((city == null) ? 0 : city.hashCode());
        result = prime * result
                + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result
                + ((lastName == null) ? 0 : lastName.hashCode());
        return result;
    }

    /**
     * Users equality based upon first names, last names,
     * cities and addresses equality.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        if (city == null) {
            if (other.city != null)
                return false;
        } else if (!city.equals(other.city))
            return false;
        if (firstName == null) {
            if (other.firstName != null)
                return false;
        } else if (!firstName.equals(other.firstName))
            return false;
        if (lastName == null) {
            if (other.lastName != null)
                return false;
        } else if (!lastName.equals(other.lastName))
            return false;
        return true;
    }
    
    public Map<Date, Integer> getTraffic() {
        return traffic;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }
}
