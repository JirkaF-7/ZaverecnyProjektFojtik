package cz.itnetwork;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.InputMismatchException;

/**
 * Třída zajišťující komunikaci s uživatelem (uživatelské rozhraní).
 * Zobrazuje menu, načítá vstupy, validuje je a vypisuje výsledky.
 */
public class UzivatelskeRozhrani {
    private SpravaPojistenych spravaPojistenych; // Instance pro přístup k datům a logice
    private Scanner scanner; // Objekt pro čtení vstupů z konzole

    // --- Konstanty pro validaci ---
    private static final int MIN_DELKA_JMENA = 2;
    private static final int MAX_DELKA_JMENA = 100;
    /** Regulární výraz pro validaci jména a příjmení.
     * Vyžaduje velké písmeno na začátku, pak písmena (vč. diakritiky), mezery, apostrofy, pomlčky.
     * Celková délka je omezena konstantami MIN_DELKA_JMENA a MAX_DELKA_JMENA. */
    private static final Pattern VZOR_JMENA_PRIJMENI = Pattern.compile("^[\\p{Lu}][\\p{L} '-]{" + (MIN_DELKA_JMENA - 1) + "," + (MAX_DELKA_JMENA - 1) + "}$");

    /** Maximální povolené stáří osoby pro validaci data narození. */
    private static final int MAX_STARI_LET = 120;
    /** Formát data používaný pro vstup od uživatele (např. "24.12.1990"). Používá striktní parsování. */
    private static final DateTimeFormatter FORMAT_DATA_VSTUP = DateTimeFormatter.ofPattern("d.M.uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    /** Česká telefonní předvolba. */
    private static final String PREDVOLBA_CZ = "+420";

    /** Maximální povolená délka emailové adresy. */
    private static final int MAX_DELKA_EMAILU = 100;

    /**
     * Konstruktor třídy uživatelského rozhraní.
     * Inicializuje scanner a přebírá instanci správce dat.
     *
     * @param spravaPojistenych Instance třídy {@link SpravaPojistenych}, která spravuje data.
     */
    public UzivatelskeRozhrani(SpravaPojistenych spravaPojistenych) {
        this.spravaPojistenych = spravaPojistenych;
        // Inicializace scanneru s kódováním UTF-8 pro podporu diakritiky
        this.scanner = new Scanner(System.in, "UTF-8");
    }

    /**
     * Spustí hlavní smyčku aplikace. Zobrazuje menu, načítá volbu uživatele
     * a volá odpovídající metody, dokud uživatel nezvolí konec.
     */
    public void spustit() {
        String volba;
        do {
            vypisMenu();
            System.out.print("Zadejte volbu: ");
            volba = scanner.nextLine().trim(); // Načtení volby a odstranění bílých znaků

            // Zpracování volby uživatele
            switch (volba) {
                case "1":
                    pridejNovehoPojisteneho();
                    break;
                case "2":
                    vypisVsechnyPojistene();
                    break;
                case "3":
                    vyhledejPojisteneho();
                    break;
                case "4":
                    upravPojisteneho();
                    break;
                case "5":
                    smazPojisteneho();
                    break;
                case "6": // Konec aplikace
                    System.out.println("Ukončuji aplikaci...");
                    break;
                default: // Neplatná volba
                    System.out.println("Neplatná volba, zkuste to znovu.");
                    break;
            }
            // Pozastavení běhu, aby si uživatel stihl přečíst výstup (kromě volby konce)
            if (!volba.equals("6")) {
                cekejNaEnter();
            }
        } while (!volba.equals("6")); // Smyčka běží, dokud není zvolen konec (6)

        scanner.close(); // Uzavření scanneru po ukončení aplikace
    }

    /**
     * Vypíše hlavní menu aplikace s dostupnými volbami.
     */
    private void vypisMenu() {
        System.out.println("\n--- Evidence pojištěných ---");
        System.out.println("1 - Přidat nového pojištěného");
        System.out.println("2 - Vypsat všechny pojištěné");
        System.out.println("3 - Vyhledat pojištěného");
        System.out.println("4 - Upravit pojištěného");
        System.out.println("5 - Smazat pojištěného");
        System.out.println("6 - Konec");
        System.out.println("---------------------------");
    }

    /**
     * Získá validní data od uživatele (jméno, příjmení, datum narození, telefon, email)
     * a přidá nového pojištěného do evidence (pouze v paměti).
     */
    private void pridejNovehoPojisteneho() {
        System.out.println("\n--- Zadání nového pojištěného ---");

        // Načtení a validace jednotlivých údajů pomocí specifických metod
        String jmeno = nactiValidniRetezec("Zadejte jméno:", VZOR_JMENA_PRIJMENI, MAX_DELKA_JMENA,
                String.format("Jméno musí začínat velkým písmenem, mít %d-%d znaků a obsahovat povolené znaky.", MIN_DELKA_JMENA, MAX_DELKA_JMENA));
        String prijmeni = nactiValidniRetezec("Zadejte příjmení:", VZOR_JMENA_PRIJMENI, MAX_DELKA_JMENA,
                String.format("Příjmení musí začínat velkým písmenem, mít %d-%d znaků a obsahovat povolené znaky.", MIN_DELKA_JMENA, MAX_DELKA_JMENA));
        LocalDate datumNarozeni = nactiValidniDatumNarozeni("Zadejte datum narození (formát dd.mm.rrrr):");
        String devetMist = nactiTelefonPoPredvolbe("Zadejte telefonní číslo:"); // Načte 9 číslic
        String telefon = PREDVOLBA_CZ + devetMist; // Přidá předvolbu
        String email = nactiValidniEmail("Zadejte email (vzor: jmeno.prijmeni@domena.cz):");

        // Vytvoření nového objektu Pojisteny
        Pojisteny novy = new Pojisteny(jmeno, prijmeni, datumNarozeni, telefon, email);
        // Přidání objektu do správy dat
        spravaPojistenych.pridejPojisteneho(novy);

        System.out.println("\nNový pojištěný byl úspěšně přidán (pouze dočasně).");
    }

    /**
     * Vypíše seznam všech aktuálně evidovaných pojištěných v tabulkovém formátu.
     * Pokud je evidence prázdná, vypíše informativní hlášku.
     */
    private void vypisVsechnyPojistene() {
        System.out.println("\n--- Seznam všech pojištěných ---");
        // Získání kopie seznamu pojištěných
        List<Pojisteny> vsichni = spravaPojistenych.vratVsechnyPojistene();

        if (vsichni.isEmpty()) {
            System.out.println("Evidence neobsahuje žádné pojištěné.");
        } else {
            // Tisk hlavičky tabulky
            System.out.printf("%-15s %-15s %-5s %-20s %-25s%n", "Jméno", "Příjmení", "Věk", "Telefon", "Email");
            System.out.println("------------------------------------------------------------------------------------"); // Oddělovač
            // Tisk jednotlivých záznamů
            for (Pojisteny p : vsichni) {
                System.out.println(p.formatujProVypisTabulky()); // Použití formátovací metody objektu Pojisteny
            }
        }
    }

    /**
     * Zobrazí podmenu pro výběr kritéria hledání a spustí odpovídající hledání.
     * Pokud je evidence prázdná, hledání se neprovádí.
     */
    private void vyhledejPojisteneho() {
        System.out.println("\n--- Vyhledání pojištěného ---");
        // Kontrola, zda je vůbec co prohledávat
        if (spravaPojistenych.vratVsechnyPojistene().isEmpty()) {
            System.out.println("Nelze vyhledávat, evidence je prázdná.");
            return; // Ukončení metody
        }

        zobrazMenuHledani(); // Zobrazení možností hledání
        System.out.print("Zadejte volbu kritéria: ");
        String volba = scanner.nextLine().trim();

        // Zpracování volby kritéria
        switch (volba) {
            case "1": hledejPodleJmena(); break;
            case "2": hledejPodleTelefonu(); break;
            case "3": hledejPodleEmailu(); break;
            case "4": hledejPodleVeku(); break;
            case "0": // Zpět - nedělá nic, vrátí se do hlavního menu
                break;
            default: System.out.println("Neplatná volba kritéria."); break;
        }
    }

    /**
     * Pomocná metoda pro zobrazení menu s kritérii pro vyhledávání.
     */
    private void zobrazMenuHledani() {
        System.out.println("Podle jakého kritéria chcete vyhledávat?");
        System.out.println("  1 - Podle jména a příjmení");
        System.out.println("  2 - Podle telefonního čísla");
        System.out.println("  3 - Podle emailu");
        System.out.println("  4 - Podle věku (rozsah)");
        System.out.println("  0 - Zpět do hlavního menu");
        System.out.println("--------------------------------------");
    }

    /**
     * Umožní uživateli vyhledat pojištěného a upravit jeho údaje.
     * Změny jsou platné pouze do ukončení aplikace (neukládají se trvale).
     */
    private void upravPojisteneho() {
        System.out.println("\n--- Úprava pojištěného ---");
        System.out.println("POZOR: Změny nebudou trvale uloženy."); // Upozornění

        if (spravaPojistenych.vratVsechnyPojistene().isEmpty()) {
            System.out.println("Nelze upravovat, evidence je prázdná.");
            return;
        }

        // 1. Najít pojištěného
        System.out.println("Zadejte jméno a příjmení pojištěného, kterého chcete upravit:");
        String hledaneJmeno = nactiValidniRetezec("Jméno:", VZOR_JMENA_PRIJMENI, MAX_DELKA_JMENA, "Neplatný formát jména.");
        String hledanePrijmeni = nactiValidniRetezec("Příjmení:", VZOR_JMENA_PRIJMENI, MAX_DELKA_JMENA, "Neplatný formát příjmení.");
        List<Pojisteny> nalezeni = spravaPojistenych.najdiPojisteneho(hledaneJmeno, hledanePrijmeni);

        // 2. Zpracovat výsledek hledání
        Pojisteny pojistenyKUprave = null;
        if (nalezeni.isEmpty()) {
            System.out.println("Pojištěný s tímto jménem a příjmením nebyl nalezen.");
            return;
        } else if (nalezeni.size() > 1) {
            System.out.println("Nalezeno více pojištěných s tímto jménem:");
            zobrazVysledkyHledani(nalezeni, "shodné jméno a příjmení"); // Zobrazíme nalezené pro informaci
            System.out.println("Úpravu nelze provést, identifikace není jednoznačná."); // Zatím nepodporujeme výběr z více
            return;
        } else {
            pojistenyKUprave = nalezeni.get(0); // Nalezen právě jeden
            System.out.println("\nNalezen pojištěný k úpravě:");
            System.out.println(pojistenyKUprave.formatujProVypisTabulky()); // Zobrazíme aktuální stav
            System.out.println();
        }

        // 3. Zeptat se na nové hodnoty pro každý atribut
        String noveJmeno = pojistenyKUprave.getJmeno();
        String novePrijmeni = pojistenyKUprave.getPrijmeni();
        LocalDate noveDatumNarozeni = pojistenyKUprave.getDatumNarozeni();
        String novyTelefon = pojistenyKUprave.getTelefonniCislo();
        String novyEmail = pojistenyKUprave.getEmail();

        // Dotaz na změnu jména
        System.out.printf("Aktuální jméno: %s%n", noveJmeno);
        if (chceZmenit("Přejete si změnit jméno? (a/n):")) {
            noveJmeno = nactiValidniRetezec("Zadejte nové jméno:", VZOR_JMENA_PRIJMENI, MAX_DELKA_JMENA, "Neplatný formát jména.");
        }
        // Dotaz na změnu příjmení
        System.out.printf("Aktuální příjmení: %s%n", novePrijmeni);
        if (chceZmenit("Přejete si změnit příjmení? (a/n):")) {
            novePrijmeni = nactiValidniRetezec("Zadejte nové příjmení:", VZOR_JMENA_PRIJMENI, MAX_DELKA_JMENA, "Neplatný formát příjmení.");
        }
        // Dotaz na změnu data narození
        System.out.printf("Aktuální datum narození: %s%n", noveDatumNarozeni.format(FORMAT_DATA_VSTUP));
        if (chceZmenit("Přejete si změnit datum narození? (a/n):")) {
            noveDatumNarozeni = nactiValidniDatumNarozeni("Zadejte nové datum narození (formát dd.mm.rrrr):");
        }
        // Dotaz na změnu telefonu
        System.out.printf("Aktuální telefon: %s%n", formatujTelefonProVypis(novyTelefon));
        if (chceZmenit("Přejete si změnit telefonní číslo? (a/n):")) {
            String devetMist = nactiTelefonPoPredvolbe("Zadejte nové telefonní číslo:");
            novyTelefon = PREDVOLBA_CZ + devetMist;
        }
        // Dotaz na změnu emailu
        System.out.printf("Aktuální email: %s%n", novyEmail);
        if (chceZmenit("Přejete si změnit email? (a/n):")) {
            novyEmail = nactiValidniEmail("Zadejte nový email (vzor: jmeno.prijmeni@domena.cz):");
        }

        // 4. Vytvoření nového objektu s upravenými daty
        Pojisteny upravenyPojisteny = new Pojisteny(noveJmeno, novePrijmeni, noveDatumNarozeni, novyTelefon, novyEmail);
        // 5. Nahrazení starého objektu novým ve správě dat
        boolean nahrazeno = spravaPojistenych.nahradPojisteneho(pojistenyKUprave, upravenyPojisteny);

        // 6. Informování uživatele o výsledku
        if (nahrazeno) {
            System.out.println("\nPojištěný byl úspěšně upraven (pouze dočasně).");
            System.out.println("POZOR: Změny nebudou trvale uloženy."); // Připomenutí
        } else {
            // Tato chyba by neměla snadno nastat, pokud byl objekt nalezen
            System.out.println("\nChyba: Pojištěného se nepodařilo upravit (možná byl mezitím smazán?).");
        }
    }

    /**
     * Umožní uživateli vyhledat a smazat pojištěného z evidence.
     * Vyžaduje potvrzení před smazáním.
     * Změny jsou platné pouze do ukončení aplikace (neukládají se trvale).
     */
    private void smazPojisteneho() {
        System.out.println("\n--- Smazání pojištěného ---");
        System.out.println("POZOR: Smazání nebude trvale uloženo."); // Upozornění

        if (spravaPojistenych.vratVsechnyPojistene().isEmpty()) {
            System.out.println("Nelze mazat, evidence je prázdná.");
            return;
        }

        // 1. Najít pojištěného
        System.out.println("Zadejte jméno a příjmení pojištěného, kterého chcete smazat:");
        String hledaneJmeno = nactiValidniRetezec("Jméno:", VZOR_JMENA_PRIJMENI, MAX_DELKA_JMENA, "Neplatný formát jména.");
        String hledanePrijmeni = nactiValidniRetezec("Příjmení:", VZOR_JMENA_PRIJMENI, MAX_DELKA_JMENA, "Neplatný formát příjmení.");
        List<Pojisteny> nalezeni = spravaPojistenych.najdiPojisteneho(hledaneJmeno, hledanePrijmeni);

        // 2. Zpracovat výsledek hledání
        Pojisteny pojistenyKeSmazani = null;
        if (nalezeni.isEmpty()) {
            System.out.println("Pojištěný s tímto jménem a příjmením nebyl nalezen.");
            return;
        } else if (nalezeni.size() > 1) {
            System.out.println("Nalezeno více pojištěných s tímto jménem:");
            zobrazVysledkyHledani(nalezeni, "shodné jméno a příjmení");
            System.out.println("Mazání nelze provést, identifikace není jednoznačná."); // Zatím nepodporujeme výběr z více
            return;
        } else {
            pojistenyKeSmazani = nalezeni.get(0); // Nalezen právě jeden
            System.out.println("\nNalezen pojištěný ke smazání:");
            System.out.println(pojistenyKeSmazani.formatujProVypisTabulky()); // Zobrazíme ho
            System.out.println();
        }

        // 3. Potvrzení od uživatele
        if (chceZmenit("Opravdu si přejete tohoto pojištěného smazat? (a/n):")) {
            // 4. Pokus o smazání ve správě dat
            boolean smazano = spravaPojistenych.smazPojisteneho(pojistenyKeSmazani);
            // 5. Informování o výsledku
            if (smazano) {
                System.out.println("\nPojištěný byl úspěšně smazán (pouze dočasně).");
                System.out.println("POZOR: Smazání nebude trvale uloženo."); // Připomenutí
            } else {
                // Tato chyba by neměla snadno nastat, pokud byl objekt nalezen a potvrzeno smazání
                System.out.println("\nChyba: Pojištěného se nepodařilo smazat (možná byl mezitím smazán?).");
            }
        } else {
            // Uživatel nepotvrdil smazání
            System.out.println("\nMazání zrušeno uživatelem.");
        }
    }


    // --- Metody pro dílčí kroky vyhledávání ---

    /** Načte jméno a příjmení a spustí hledání podle nich. */
    private void hledejPodleJmena() {
        System.out.println("\n-- Hledání podle jména a příjmení --");
        String jmeno = nactiValidniRetezec("Zadejte jméno hledané osoby:", VZOR_JMENA_PRIJMENI, MAX_DELKA_JMENA, "Neplatný formát jména.");
        String prijmeni = nactiValidniRetezec("Zadejte příjmení hledané osoby:", VZOR_JMENA_PRIJMENI, MAX_DELKA_JMENA, "Neplatný formát příjmení.");
        List<Pojisteny> nalezeni = spravaPojistenych.najdiPojisteneho(jmeno, prijmeni);
        zobrazVysledkyHledani(nalezeni, "jméno a příjmení");
    }

    /** Načte telefonní číslo a spustí hledání podle něj. */
    private void hledejPodleTelefonu() {
        System.out.println("\n-- Hledání podle telefonního čísla --");
        System.out.print("Zadejte hledané telefonní číslo (včetně předvolby +420): ");
        String telefon = scanner.nextLine().trim();
        // Poznámka: Zde neprovádíme striktní validaci formátu hledaného čísla
        List<Pojisteny> nalezeni = spravaPojistenych.najdiPodleTelefonu(telefon);
        zobrazVysledkyHledani(nalezeni, "telefonní číslo");
    }

    /** Načte email a spustí hledání podle něj. */
    private void hledejPodleEmailu() {
        System.out.println("\n-- Hledání podle emailu --");
        System.out.print("Zadejte hledaný email: ");
        String email = scanner.nextLine().trim();
        // Poznámka: Zde neprovádíme striktní validaci formátu hledaného emailu
        List<Pojisteny> nalezeni = spravaPojistenych.najdiPodleEmailu(email);
        zobrazVysledkyHledani(nalezeni, "email");
    }

    /** Načte rozsah věku a spustí hledání podle něj. */
    private void hledejPodleVeku() {
        System.out.println("\n-- Hledání podle věku --");
        // Načtení min a max věku s validací nezápornosti a vzájemného vztahu
        int minVek = nactiCeleCislo("Zadejte minimální věk:", 0);
        int maxVek = nactiCeleCislo("Zadejte maximální věk:", minVek); // Max musí být >= min
        // Kontrola, zda uživatel nezadal max menší než min (i když nactiCeleCislo to částečně ošetřuje)
        if (minVek > maxVek) {
            System.out.println("Chyba: Minimální věk nemůže být vyšší než maximální věk.");
            return;
        }
        // Volání metody pro hledání ve správě dat
        List<Pojisteny> nalezeni = spravaPojistenych.najdiPodleVeku(minVek, maxVek);
        // Zobrazení výsledků
        zobrazVysledkyHledani(nalezeni, String.format("věk v rozmezí %d-%d let", minVek, maxVek));
    }

    /**
     * Pomocná metoda pro jednotné zobrazení výsledků hledání v tabulce
     * nebo zobrazení hlášky, pokud nebylo nic nalezeno.
     * @param nalezeni Seznam nalezených pojištěných.
     * @param kriterium Popis kritéria hledání pro informační výpis.
     */
    private void zobrazVysledkyHledani(List<Pojisteny> nalezeni, String kriterium) {
        if (nalezeni.isEmpty()) {
            // Nic nenalezeno
            System.out.printf("\nNebyli nalezeni žádní pojištění odpovídající kritériu: %s.\n", kriterium);
        } else {
            // Nalezeno - tisk tabulky
            System.out.printf("\nNalezení pojištění podle kritéria: %s\n", kriterium);
            System.out.printf("%-15s %-15s %-5s %-20s %-25s%n", "Jméno", "Příjmení", "Věk", "Telefon", "Email");
            System.out.println("------------------------------------------------------------------------------------");
            for (Pojisteny p : nalezeni) {
                System.out.println(p.formatujProVypisTabulky());
            }
        }
    }


    // --- Pomocné metody pro načítání a VALIDACI vstupu ---

    /**
     * Načte od uživatele řetězec (pro jméno/příjmení), validuje jeho délku
     * a formát pomocí zadaného regulárního výrazu.
     * Opakovaně vyzývá uživatele k zadání, dokud vstup nesplňuje podmínky.
     *
     * @param vyzva        Textová výzva pro uživatele.
     * @param vzor         Regulární výraz (Pattern) pro validaci formátu.
     * @param maxDelka     Maximální povolená délka vstupu.
     * @param chybovaHlaska Text, který se zobrazí při neplatném vstupu.
     * @return Validní načtený řetězec (neprázdný).
     */
    private String nactiValidniRetezec(String vyzva, Pattern vzor, int maxDelka, String chybovaHlaska) {
        String vstup;
        Matcher matcher;
        while (true) {
            System.out.print(vyzva + " ");
            vstup = scanner.nextLine().trim();
            // Kontrola prázdného vstupu
            if (vstup.isEmpty()) {
                System.out.println("Chyba: Vstup nesmí být prázdný. Zadejte prosím znovu.");
                continue;
            }
            // Kontrola délky
            if (vstup.length() > maxDelka) {
                System.out.printf("Chyba: Vstup je příliš dlouhý (max %d znaků). Zadejte prosím znovu.\n", maxDelka);
                continue;
            }
            // Kontrola formátu pomocí regexu
            matcher = vzor.matcher(vstup);
            if (matcher.matches()) {
                return vstup; // Vrací validní vstup
            } else {
                // Formát neodpovídá
                System.out.println("Chyba: " + chybovaHlaska + " Zadejte prosím znovu.");
            }
        }
    }

    /**
     * Načte od uživatele 9místné telefonní číslo, které dopíše za zobrazenou předvolbu.
     * Odstraní mezery a validuje, že se jedná o 9 číslic.
     *
     * @param vyzvaText Text, který se zobrazí před předvolbou (např. "Zadejte telefonní číslo:").
     * @return Řetězec obsahující přesně 9 číslic (bez mezer).
     */
    private String nactiTelefonPoPredvolbe(String vyzvaText) {
        String userInput;
        String cisteCislo;
        while (true) {
            // Výpis výzvy a předvolby bez odřádkování
            System.out.print(vyzvaText + " " + PREDVOLBA_CZ + " ");
            userInput = scanner.nextLine().trim(); // Načtení zbytku řádku

            // Odstranění všech mezer (pro případ, že by uživatel zadal např. 123 456 789)
            cisteCislo = userInput.replaceAll("\\s+", "");

            // Validace: přesně 9 číslic
            if (cisteCislo.matches("^\\d{9}$")) {
                return cisteCislo; // Vrací validní 9místné číslo bez mezer
            } else {
                // Chyba - výpis a opakování cyklu
                System.out.println(); // Nový řádek pro přehlednost chyby
                System.out.println("Chyba: Za předvolbou " + PREDVOLBA_CZ + " musíte zadat přesně 9 číslic (mezery jsou povoleny). Zkuste to znovu.");
            }
        }
    }

    /**
     * Načte od uživatele emailovou adresu a provede sérii detailních validací.
     * Kontroluje prázdný vstup, délku, počet zavináčů, prázdné části,
     * nepovolené znaky, pozici tečky/spojovníku a základní strukturu domény.
     * Poskytuje specifické chybové hlášky.
     *
     * @param vyzva Textová výzva pro uživatele (měla by obsahovat vzor).
     * @return Validní emailovou adresu.
     */
    private String nactiValidniEmail(String vyzva) {
        String email;
        while (true) { // Smyčka pro opakované zadávání
            System.out.print(vyzva + " ");
            email = scanner.nextLine().trim();

            // --- Série validací pro email ---

            // 1. Prázdný vstup
            if (email.isEmpty()) {
                System.out.println("Chyba: Email nesmí být prázdný. Zadejte prosím znovu.");
                continue; // Pokračuje na další iteraci smyčky
            }

            // 2. Maximální délka
            if (email.length() > MAX_DELKA_EMAILU) {
                System.out.printf("Chyba: Email je příliš dlouhý (max %d znaků). Zadejte prosím znovu.\n", MAX_DELKA_EMAILU);
                continue;
            }

            // 3. Počet zavináčů (@)
            long pocetZavinacu = email.chars().filter(ch -> ch == '@').count();
            if (pocetZavinacu == 0) {
                System.out.println("Chyba: Email musí obsahovat zavináč (@). Zadejte prosím znovu.");
                continue;
            }
            if (pocetZavinacu > 1) {
                System.out.println("Chyba: Email nesmí obsahovat více než jeden zavináč (@). Zadejte prosím znovu.");
                continue;
            }

            // Rozdělení na části pro další kontroly
            int indexZavinace = email.indexOf('@');
            String uzivatel = email.substring(0, indexZavinace);
            String domena = email.substring(indexZavinace + 1);

            // 4. Kontrola prázdných částí
            if (uzivatel.isEmpty()) {
                System.out.println("Chyba: Část emailu před zavináčem nesmí být prázdná. Zadejte prosím znovu.");
                continue;
            }
            if (domena.isEmpty()) {
                System.out.println("Chyba: Část emailu za zavináčem (doména) nesmí být prázdná. Zadejte prosím znovu.");
                continue;
            }

            // 5. Kontrola začátku/konce uživatelské části (nesmí být . nebo -)
            if (uzivatel.startsWith(".") || uzivatel.startsWith("-")) {
                System.out.println("Chyba: Část emailu před zavináčem nesmí začínat tečkou ani spojovníkem. Zadejte prosím znovu.");
                continue;
            }
            if (uzivatel.endsWith(".") || uzivatel.endsWith("-")) {
                System.out.println("Chyba: Část emailu před zavináčem nesmí končit tečkou ani spojovníkem. Zadejte prosím znovu.");
                continue;
            }

            // 6. Kontrola nepovolených znaků (diakritika, mezery, jiné spec. znaky)
            // Povolené jsou pouze: a-z, A-Z, 0-9, '.', '_', '-', '@'
            if (!email.matches("^[a-zA-Z0-9._@-]+$")) {
                // Najdeme a vypíšeme problematické znaky
                String nepovolene = email.replaceAll("[a-zA-Z0-9._@-]", "");
                System.out.println("Chyba: Email obsahuje nepovolené znaky (např. mezery, diakritiku): '" + nepovolene + "'. Povolené jsou pouze písmena bez diakritiky, číslice a znaky . _ - @. Zadejte prosím znovu.");
                continue;
            }

            // 7. Kontrola dvou teček za sebou
            if (email.contains("..")) {
                System.out.println("Chyba: Email nesmí obsahovat dvě tečky (.) za sebou. Zadejte prosím znovu.");
                continue;
            }

            // 8. Kontrola základní struktury domény (musí obsahovat tečku a TLD min 2 znaky)
            int posledniTeckaDomeny = domena.lastIndexOf('.');
            // Musí existovat tečka, nesmí být hned za @ (min 1 znak domény před ní), a musí být min 2 znaky za ní (TLD)
            if (posledniTeckaDomeny <= 0 || posledniTeckaDomeny >= domena.length() - 2) {
                System.out.println("Chyba: Neplatný formát domény v emailu (např. chybí tečka nebo koncovka jako .cz, .com). Zadejte prosím znovu.");
                continue;
            }
            // (Dodatečná kontrola začátku/konce domény - volitelně)
            if (domena.startsWith(".") || domena.startsWith("-")) {
                System.out.println("Chyba: Doména (část za @) nesmí začínat tečkou ani spojovníkem. Zadejte prosím znovu.");
                continue;
            }
            if (domena.endsWith(".") || domena.endsWith("-")) {
                System.out.println("Chyba: Email nesmí končit tečkou ani spojovníkem. Zadejte prosím znovu.");
                continue;
            }

            // Pokud všechny kontroly prošly, email je validní
            return email;
        }
    }

    /**
     * Načte od uživatele datum narození ve formátu dd.mm.rrrr a validuje ho.
     * Kontroluje formát, platnost data (včetně přestupných roků díky STRICT resolveru)
     * a zda datum není v budoucnosti nebo příliš staré.
     *
     * @param vyzva Textová výzva pro uživatele.
     * @return Validní {@link LocalDate} objekt data narození.
     */
    private LocalDate nactiValidniDatumNarozeni(String vyzva) {
        LocalDate datum = null;
        while (datum == null) { // Opakuje, dokud není zadáno platné datum
            System.out.print(vyzva + " ");
            String vstup = scanner.nextLine().trim();
            // Kontrola prázdného vstupu
            if (vstup.isEmpty()) {
                System.out.println("Chyba: Datum narození nesmí být prázdné. Zadejte prosím znovu.");
                continue;
            }
            try {
                // Parsování data pomocí definovaného formátu (striktní mód)
                datum = LocalDate.parse(vstup, FORMAT_DATA_VSTUP);
                // Kontrola logického rozsahu data
                LocalDate dnes = LocalDate.now();
                LocalDate nejstarsiMozneDatum = dnes.minusYears(MAX_STARI_LET); // Nejstarší povolené datum

                if (datum.isAfter(dnes)) {
                    // Datum je v budoucnosti
                    System.out.println("Chyba: Datum narození nemůže být v budoucnosti. Zadejte prosím znovu.");
                    datum = null; // Neplatný vstup, vynulujeme pro opakování smyčky
                } else if (datum.isBefore(nejstarsiMozneDatum)) {
                    // Datum je příliš staré
                    System.out.printf("Chyba: Datum narození je příliš vzdálené v minulosti (starší než %d let). Zadejte prosím znovu.\n", MAX_STARI_LET);
                    datum = null; // Neplatný vstup
                }
                // Pokud datum prošlo kontrolami, je platné a smyčka skončí
            } catch (DateTimeParseException e) {
                // Chyba při parsování - neplatný formát nebo neexistující datum (např. 31.2.)
                System.out.println("Chyba: Neplatný formát data (dd.mm.rrrr) nebo neexistující datum. Zadejte prosím znovu.");
                datum = null; // Neplatný vstup
            }
        }
        return datum; // Vrací validní datum
    }

    /**
     * Načte od uživatele celé číslo (používá se pro zadání věku v rozsahu hledání).
     * Validuje, že vstup není prázdný, je to platné číslo a je větší nebo rovno než zadané minimum.
     *
     * @param vyzva Textová výzva pro uživatele.
     * @param minimum Minimální povolená hodnota čísla (včetně).
     * @return Načtené validní celé číslo.
     */
    private int nactiCeleCislo(String vyzva, int minimum) {
        int cislo;
        while (true) { // Opakuje, dokud není zadáno platné číslo
            System.out.print(vyzva + " ");
            try {
                String vstup = scanner.nextLine().trim();
                // Kontrola prázdného vstupu
                if (vstup.isEmpty()) {
                    System.out.println("Chyba: Vstup nesmí být prázdný. Zadejte prosím číslo.");
                    continue;
                }
                // Pokus o převod na int
                cislo = Integer.parseInt(vstup);
                // Kontrola minimální hodnoty
                if (cislo >= minimum) {
                    return cislo; // Vrací validní číslo
                } else {
                    // Chyba - číslo je menší než minimum
                    System.out.printf("Chyba: Číslo musí být %d nebo vyšší. Zadejte prosím znovu.\n", minimum);
                }
            } catch (NumberFormatException e) {
                // Chyba - vstup nebyl platné celé číslo
                System.out.println("Chyba: Zadejte prosím platné celé číslo.");
            }
        }
    }

    /**
     * Pomocná metoda pro získání odpovědi ano/ne od uživatele.
     * Ptá se pomocí zadané výzvy a opakuje dotaz, dokud uživatel nezadá 'a'/'ano' nebo 'n'/'ne'.
     * Nerozlišuje velikost písmen.
     *
     * @param vyzva Textová otázka pro uživatele (např. "Přejete si pokračovat? (a/n):").
     * @return true, pokud uživatel zadal ano, false, pokud zadal ne.
     */
    private boolean chceZmenit(String vyzva) {
        String odpoved;
        while (true) { // Opakuje, dokud není platná odpověď
            System.out.print(vyzva + " ");
            odpoved = scanner.nextLine().trim().toLowerCase(); // Načte, ořízne a převede na malá písmena
            if (odpoved.equals("a") || odpoved.equals("ano")) {
                return true; // Ano
            } else if (odpoved.equals("n") || odpoved.equals("ne")) {
                return false; // Ne
            } else {
                // Neplatná odpověď
                System.out.println("Neplatná odpověď, zadejte 'a' pro ano nebo 'n' pro ne.");
            }
        }
    }

    /**
     * Pomocná metoda pro formátování telefonního čísla pro výpis v rámci UI
     * (např. při zobrazení aktuální hodnoty před úpravou).
     * Používá stejnou logiku jako metoda v {@link Pojisteny}.
     * @param cislo Telefonní číslo ve formátu +420XXXXXXXXX.
     * @return Formátované číslo (+420 XXX XXX XXX) nebo původní/prázdné.
     */
    private String formatujTelefonProVypis(String cislo) {
        if (cislo != null && cislo.startsWith("+420") && cislo.length() == 13) {
            return cislo.replaceFirst("(\\+420)(\\d{3})(\\d{3})(\\d{3})", "$1 $2 $3 $4");
        }
        return cislo != null ? cislo : "";
    }

    /**
     * Pozastaví provádění programu a čeká, dokud uživatel nestiskne klávesu Enter.
     * Používá se pro možnost přečíst si výstup před zobrazením dalšího menu nebo výzvy.
     */
    private void cekejNaEnter() {
        System.out.println("\nStiskněte Enter pro pokračování...");
        scanner.nextLine(); // Čeká na stisk Enter
    }
}
