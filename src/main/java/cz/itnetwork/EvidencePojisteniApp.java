package cz.itnetwork;

/**
 * Hlavní třída aplikace pro spuštění evidence pojištění.
 */
public class EvidencePojisteniApp {
    public static void main(String[] args) {
        // Vytvoření instance pro správu dat
        // Scanner sc = new Scanner(System.in)
        SpravaPojistenych spravaDat = new SpravaPojistenych();

        // Vytvoření instance uživatelského rozhraní a předání správy dat
        UzivatelskeRozhrani ui = new UzivatelskeRozhrani(spravaDat);  // přidat sc

        // Spuštění hlavní smyčky aplikace
        ui.spustit();
    }
}
