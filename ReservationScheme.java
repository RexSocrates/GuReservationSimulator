/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Hashtable;

/**
 *
 * @author Socrates
 */
public interface ReservationScheme {
    // every data that the reservation scheme need should be put in the hash table
    public double determineGU(Hashtable hashtable);
}
