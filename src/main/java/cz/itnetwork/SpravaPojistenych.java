package cz.itnetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * Třída zajišťující logiku správy pojištěných osob.
 * Uchovává data v paměti (v kolekci ArrayList).
 */
public class SpravaPojistenych {
    private List<Pojisteny> pojisteni;

    /**
     * Konstruktor inicializuje prázdný seznam pojištěných.
     */
    public SpravaPojistenych() {
        pojisteni = new ArrayList<>();
        // Data se nenačítají ze souboru
    }

    /**
     * Přidá nového pojištěného do evidence (pouze v paměti).
     *
     * @param novyPojisteny Objekt {@link Pojisteny} k přidání. Nesmí být null.
     */
    public void pridejPojisteneho(Pojisteny novyPojisteny) {
        if (novyPojisteny != null) {
            pojisteni.add(novyPojisteny);
            // Změna se neukládá do souboru
        }
        // V reálné aplikaci by zde mohla být kontrola unikátnosti
    }

    /**
     * Najde původního pojištěného v seznamu a nahradí ho novým objektem (pouze v paměti).
     * Používá metodu {@link Pojisteny#equals(Object)} pro nalezení.
     *
     * @param staryPojisteny Objekt pojištěného, který má být nahrazen.
     * @param novyPojisteny  Objekt pojištěného, kterým se nahrazuje.
     * @return true, pokud byl objekt nalezen a nahrazen, jinak false.
     */
    public boolean nahradPojisteneho(Pojisteny staryPojisteny, Pojisteny novyPojisteny) {
        int index = pojisteni.indexOf(staryPojisteny); // Najde index pomocí equals()
        if (index != -1) { // Pokud byl nalezen
            pojisteni.set(index, novyPojisteny); // Nahradí objekt na daném indexu
            // Změna se neukládá do souboru
            return true;
        }
        return false; // Objekt nebyl v seznamu nalezen
    }

    /**
     * Odstraní zadaného pojištěného ze seznamu (pouze v paměti).
     * Používá metodu {@link Pojisteny#equals(Object)} pro nalezení objektu.
     *
     * @param pojistenyKeSmazani Objekt pojištěného, který má být odstraněn.
     * @return true, pokud byl objekt nalezen a odstraněn, jinak false.
     */
    public boolean smazPojisteneho(Pojisteny pojistenyKeSmazani) {
        // Metoda remove() seznamu ArrayList vyhledá prvek pomocí equals() a odstraní první nalezený výskyt.
        boolean smazano = pojisteni.remove(pojistenyKeSmazani);
        // Změna se neukládá do souboru
        return smazano;
    }

    /**
     * Vrátí nemodifikovatelnou kopii seznamu všech pojištěných v evidenci.
     * Chrání interní seznam před nechtěnými změnami zvenčí.
     *
     * @return Kopie seznamu všech pojištěných (List<Pojisteny>).
     */
    public List<Pojisteny> vratVsechnyPojistene() {
        return new ArrayList<>(pojisteni); // Vracíme kopii
    }

    /**
     * Vyhledá pojištěné podle zadaného jména a příjmení.
     * Hledání ignoruje velikost písmen.
     *
     * @param jmeno Hledané křestní jméno.
     * @param prijmeni Hledané příjmení.
     * @return Seznam nalezených pojištěných (může být prázdný).
     */
    public List<Pojisteny> najdiPojisteneho(String jmeno, String prijmeni) {
        List<Pojisteny> nalezeni = new ArrayList<>();
        // Základní kontrola vstupů
        if (jmeno == null || prijmeni == null) return nalezeni;
        String hledaneJmeno = jmeno.trim();
        String hledanePrijmeni = prijmeni.trim();

        // Procházení seznamu a porovnání (case-insensitive)
        for (Pojisteny p : pojisteni) {
            if (p.getJmeno().equalsIgnoreCase(hledaneJmeno) && p.getPrijmeni().equalsIgnoreCase(hledanePrijmeni)) {
                nalezeni.add(p);
            }
        }
        return nalezeni;
    }

    /**
     * Vyhledá pojištěné podle zadaného telefonního čísla.
     * Porovnává přesnou shodu s uloženým formátem (+420XXXXXXXXX).
     *
     * @param telefon Hledané telefonní číslo.
     * @return Seznam nalezených pojištěných (může být prázdný).
     */
    public List<Pojisteny> najdiPodleTelefonu(String telefon) {
        List<Pojisteny> nalezeni = new ArrayList<>();
        if (telefon == null || telefon.trim().isEmpty()) {
            return nalezeni;
        }
        String hledanyTelefon = telefon.trim();
        for (Pojisteny p : pojisteni) {
            // Přesná shoda telefonního čísla
            if (p.getTelefonniCislo() != null && p.getTelefonniCislo().equals(hledanyTelefon)) {
                nalezeni.add(p);
            }
        }
        return nalezeni;
    }

    /**
     * Vyhledá pojištěné podle zadané emailové adresy.
     * Porovnává bez ohledu na velikost písmen (case-insensitive).
     *
     * @param email Hledaná emailová adresa.
     * @return Seznam nalezených pojištěných (může být prázdný).
     */
    public List<Pojisteny> najdiPodleEmailu(String email) {
        List<Pojisteny> nalezeni = new ArrayList<>();
        if (email == null || email.trim().isEmpty()) {
            return nalezeni;
        }
        String hledanyEmail = email.trim();
        for (Pojisteny p : pojisteni) {
            // Porovnání emailů (case-insensitive)
            if (p.getEmail() != null && p.getEmail().equalsIgnoreCase(hledanyEmail)) {
                nalezeni.add(p);
            }
        }
        return nalezeni;
    }

    /**
     * Vyhledá pojištěné v zadaném věkovém rozmezí (včetně hranic).
     * Používá metodu {@link Pojisteny#getVek()} pro získání aktuálního věku.
     *
     * @param minVek Minimální věk (včetně). Musí být >= 0.
     * @param maxVek Maximální věk (včetně). Musí být >= minVek.
     * @return Seznam nalezených pojištěných (může být prázdný).
     */
    public List<Pojisteny> najdiPodleVeku(int minVek, int maxVek) {
        List<Pojisteny> nalezeni = new ArrayList<>();
        // Kontrola platnosti rozsahu
        if (minVek < 0 || maxVek < 0 || minVek > maxVek) {
            return nalezeni; // Neplatný rozsah, vracíme prázdný seznam
        }
        // Procházení a kontrola věku
        for (Pojisteny p : pojisteni) {
            int vekPojisteneho = p.getVek();
            if (vekPojisteneho >= minVek && vekPojisteneho <= maxVek) {
                nalezeni.add(p);
            }
        }
        return nalezeni;
    }
    }
