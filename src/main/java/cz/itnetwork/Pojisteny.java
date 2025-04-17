package cz.itnetwork;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Třída reprezentující jednoho pojištěného.
 * Uchovává základní údaje o osobě, včetně data narození a emailu.
 */
public class Pojisteny {
    private String jmeno;
    private String prijmeni;
    private LocalDate datumNarozeni;
    private String telefonniCislo;
    private String email; // Nový atribut

    private static final DateTimeFormatter FORMAT_DATA = DateTimeFormatter.ofPattern("d.M.yyyy");

    /**
     * Konstruktor pro vytvoření instance pojištěného.
     * @param jmeno Křestní jméno pojištěného.
     * @param prijmeni Příjmení pojištěného.
     * @param datumNarozeni Datum narození pojištěného.
     * @param telefonniCislo Telefonní číslo pojištěného.
     * @param email Emailová adresa pojištěného. // Nový parametr
     */
    public Pojisteny(String jmeno, String prijmeni, LocalDate datumNarozeni, String telefonniCislo, String email) { // Přidán email
        this.jmeno = jmeno;
        this.prijmeni = prijmeni;
        this.datumNarozeni = datumNarozeni;
        this.telefonniCislo = telefonniCislo;
        this.email = email; // Uložení emailu
    }

    // --- Gettery ---
    public String getJmeno() { return jmeno; }
    public String getPrijmeni() { return prijmeni; }
    public LocalDate getDatumNarozeni() { return datumNarozeni; }
    public String getTelefonniCislo() { return telefonniCislo; }
    public String getEmail() { return email; } // Getter pro email

    /**
     * Vypočítá aktuální věk pojištěného (zůstává stejná).
     * @return Aktuální věk v letech.
     */
    public int getVek() {
        if (datumNarozeni == null) {
            return 0;
        }
        return Period.between(datumNarozeni, LocalDate.now()).getYears();
    }

    /**
     * Vrací textovou reprezentaci vhodnou pro výpis do tabulky v konzoli.
     * Telefonní číslo je formátováno s mezerami pro lepší čitelnost.
     * @return Formátovaný řetězec pro výpis do tabulky.
     */
    public String formatujProVypisTabulky() {
        String formatovaneTelefonniCislo = formatujTelefon(this.telefonniCislo);

        // Použití formátovaného čísla ve výpisu
        return String.format("%-15s %-15s %-5d %-20s %-25s",
                jmeno,
                prijmeni,
                getVek(),
                formatovaneTelefonniCislo, // Zde použijeme naformátované číslo
                email);
    }

    /**
     * Pomocná metoda pro formátování telefonního čísla s mezerami.
     * Očekává vstup ve formátu +420XXXXXXXXX.
     * @param cislo Telefonní číslo ve formátu +420XXXXXXXXX.
     * @return Formátované číslo (+420 XXX XXX XXX) nebo původní číslo, pokud formát neodpovídá.
     */
    private String formatujTelefon(String cislo) {
        // Kontrola, zda má číslo očekávaný formát +420 a 9 číslic (celkem 13 znaků)
        if (cislo != null && cislo.startsWith("+420") && cislo.length() == 13) {

            // Použití replaceFirst s regulárním výrazem pro vložení mezer
            // (\+420) - skupina 1: předvolba
            // (\d{3}) - skupina 2: první 3 číslice
            // (\d{3}) - skupina 3: druhé 3 číslice
            // (\d{3}) - skupina 4: třetí 3 číslice
            // "$1 $2 $3 $4" - výstupní formát s mezerami mezi skupinami
            return cislo.replaceFirst("(\\+420)(\\d{3})(\\d{3})(\\d{3})", "$1 $2 $3 $4");
        }
        // Pokud formát neodpovídá, vrátíme původní číslo (pro případ chyby)
        return cislo != null ? cislo : ""; // Vrací prázdný řetězec pokud je cislo null
    }

    /**
     * Standardní metoda toString() pro obecné účely (např. ladění).
     * Zahrnuje nyní i email.
     * @return Textová reprezentace objektu.
     */
    @Override
    public String toString() {
        return "Pojisteny{" +
                "jmeno='" + jmeno + '\'' +
                ", prijmeni='" + prijmeni + '\'' +
                ", datumNarozeni=" + (datumNarozeni != null ? datumNarozeni.format(FORMAT_DATA) : "null") +
                ", telefonniCislo='" + telefonniCislo + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pojisteny pojisteny = (Pojisteny) o;
        // Porovnáváme jméno, příjmení a datum narození pro identifikaci
        return Objects.equals(jmeno.toLowerCase(), pojisteny.jmeno.toLowerCase()) && // Ignorujeme velikost písmen
                Objects.equals(prijmeni.toLowerCase(), pojisteny.prijmeni.toLowerCase()) &&
                Objects.equals(datumNarozeni, pojisteny.datumNarozeni);
    }

    @Override
    public int hashCode() {
        // Generujeme hashCode na základě stejných polí jako v equals
        return Objects.hash(jmeno.toLowerCase(), prijmeni.toLowerCase(), datumNarozeni);
    }
}

