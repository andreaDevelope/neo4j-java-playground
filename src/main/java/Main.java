import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.QueryConfig;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Map;

public class Main {

    public static void main(String... args) {

        // Carica le variabili d'ambiente dal file .env (se presente)
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Leggi le credenziali dalle variabili d'ambiente
        final String dbUri = dotenv.get("NEO4J_URI", System.getenv("NEO4J_URI"));
        final String dbUser = dotenv.get("NEO4J_USER", System.getenv("NEO4J_USER"));
        final String dbPassword = dotenv.get("NEO4J_PASSWORD", System.getenv("NEO4J_PASSWORD"));

        // Verifica che le credenziali siano presenti
        if (dbUri == null || dbUser == null || dbPassword == null) {
            System.err.println(
                    "ERRORE: Le variabili d'ambiente NEO4J_URI, NEO4J_USER e NEO4J_PASSWORD devono essere impostate.");
            System.err
                    .println("Crea un file .env nella root del progetto o imposta le variabili d'ambiente di sistema.");
            System.exit(1);
        }

        try (var driver = GraphDatabase.driver(dbUri, AuthTokens.basic(dbUser, dbPassword))) {
            driver.verifyConnectivity();
            System.out.println("Connection established.");

            var result = driver.executableQuery("""
                    CREATE (a:Persona {nome: $nome})
                    CREATE (b:Persona {nome: $amico})
                    CREATE (a)-[:CONOSCE]->(b)
                    RETURN a, b
                    """)
                    .withParameters(Map.of("nome", "Mario", "amico", "Lucia"))
                    .withConfig(QueryConfig.builder().withDatabase("neo4j").build())
                    .execute();

            System.out.println(result.records());

        }

    }

}
