package ma.enset.tp7;

import ma.enset.tp7.model.DepartementCampus;
import ma.enset.tp7.model.SallePedagogique;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

public class App {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println(">>> DEMARRAGE TP7 - CACHE L2 & RESOLUTION DU PROBLEME N+1 (JPA) <<<");
        System.out.println("==========================================================================");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("tp7_cache_et_nplusone");

        // [1] Insertion des données de test
        EntityManager emInit = emf.createEntityManager();
        emInit.getTransaction().begin();

        DepartementCampus deptInfo = new DepartementCampus("DEP-INFO", "Informatique & Réseaux");
        deptInfo.ajouterSalle(new SallePedagogique("LAB-I1", 30));
        deptInfo.ajouterSalle(new SallePedagogique("LAB-I2", 35));

        DepartementCampus deptTelecom = new DepartementCampus("DEP-TEL", "Télécommunications & IoT");
        deptTelecom.ajouterSalle(new SallePedagogique("LAB-T1", 25));
        deptTelecom.ajouterSalle(new SallePedagogique("LAB-T2", 20));

        DepartementCampus deptGmp = new DepartementCampus("DEP-GMP", "Génie Mécanique & Productique");
        deptGmp.ajouterSalle(new SallePedagogique("LAB-M1", 40));

        emInit.persist(deptInfo);
        emInit.persist(deptTelecom);
        emInit.persist(deptGmp);

        emInit.getTransaction().commit();
        Long idDeptTest = deptInfo.getId();
        emInit.close();
        System.out.println("✓ Jeu de données initialisé : 3 départements et 5 salles associées.\n");

        // [2] Démonstration du Problème N+1 (Lazy loading classique)
        System.out.println("--- [2] DEMONSTRATION DU PROBLEME N+1 (LAZY LOADING) ---");
        EntityManager emN1 = emf.createEntityManager();
        System.out.println(">> Exécution de la requête initiale : SELECT d FROM DepartementCampus d");
        List<DepartementCampus> depts = emN1.createQuery("SELECT d FROM DepartementCampus d", DepartementCampus.class).getResultList();

        System.out.println(">> Accès aux collections paresseuses (déclenchement des N requêtes secondaires) :");
        for (DepartementCampus d : depts) {
            System.out.println("   Département : " + d.getIntitule() + " -> Nombre de salles : " + d.getSalles().size());
        }
        emN1.close();

        // [3] Solution 1 : Élimination de N+1 avec JOIN FETCH
        System.out.println("\n--- [3] SOLUTION 1 : ELIMINATION VIA JOIN FETCH ---");
        EntityManager emFetch = emf.createEntityManager();
        System.out.println(">> Exécution d'UNE SEULE requête avec JOIN FETCH :");
        List<DepartementCampus> deptsFetch = emFetch.createQuery(
                "SELECT DISTINCT d FROM DepartementCampus d JOIN FETCH d.salles",
                DepartementCampus.class
        ).getResultList();

        for (DepartementCampus d : deptsFetch) {
            System.out.println("   [JOIN FETCH] " + d.getIntitule() + " -> " + d.getSalles().size() + " salles chargées.");
        }
        emFetch.close();

        // [4] Solution 2 : Élimination de N+1 avec EntityGraph JPA
        System.out.println("\n--- [4] SOLUTION 2 : ELIMINATION VIA ENTITY GRAPH ---");
        EntityManager emGraph = emf.createEntityManager();
        EntityGraph<?> graph = emGraph.getEntityGraph("Departement.avecSalles");

        List<DepartementCampus> deptsGraph = emGraph.createQuery("SELECT d FROM DepartementCampus d", DepartementCampus.class)
                .setHint("javax.persistence.fetchgraph", graph)
                .getResultList();

        for (DepartementCampus d : deptsGraph) {
            System.out.println("   [ENTITY GRAPH] " + d.getIntitule() + " -> " + d.getSalles().size() + " salles chargées.");
        }
        emGraph.close();

        // [5] Démonstration du Cache de Second Niveau (L2 Cache)
        System.out.println("\n--- [5] DEMONSTRATION DU CACHE DE SECOND NIVEAU (L2 CACHE) ---");
        Statistics stats = emf.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        // Premier accès dans la Session 1 -> Miss dans le cache, lecture en base
        EntityManager emCache1 = emf.createEntityManager();
        System.out.println("Session 1 : Chargement de l'entité ID = " + idDeptTest);
        DepartementCampus d1 = emCache1.find(DepartementCampus.class, idDeptTest);
        System.out.println("Session 1 a lu : " + d1.getIntitule());
        emCache1.close(); // La session 1 est fermée, le cache L1 est détruit !

        // Deuxième accès dans la Session 2 -> Doit être servi par le L2 Cache sans requête SQL !
        EntityManager emCache2 = emf.createEntityManager();
        System.out.println("\nSession 2 : Nouveau chargement de l'entité ID = " + idDeptTest + " (après fermeture Session 1)");
        DepartementCampus d2 = emCache2.find(DepartementCampus.class, idDeptTest);
        System.out.println("Session 2 a lu : " + d2.getIntitule());
        emCache2.close();

        System.out.println("\n--- STATISTIQUES OFFICIELLES DU CACHE L2 ---");
        System.out.println("L2 Cache Hits   : " + stats.getSecondLevelCacheHitCount() + " (Servi depuis le cache mémoire)");
        System.out.println("L2 Cache Misses : " + stats.getSecondLevelCacheMissCount() + " (Lecture SQL en base)");
        System.out.println("L2 Cache Puts   : " + stats.getSecondLevelCachePutCount());

        emf.close();
        System.out.println("\n==========================================================================");
        System.out.println(">>> DEMONSTRATION TP7 TERMINEE AVEC SUCCES <<<");
        System.out.println("==========================================================================");
    }
}
