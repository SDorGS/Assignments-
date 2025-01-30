import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * WebScraper class for fetching search results from DuckDuckGo, extracting URLs, and retrieving page titles.
 *
 * <p>This program:
 * - Uses DuckDuckGo API to fetch search results.
 * - Extracts relevant URLs from the JSON response.
 * - Retrieves and displays page titles using concurrent HTTP requests.
 * - Implements thread safety using Java's concurrency features.
 *
 * <p>Concurrency Features:
 * - Uses ExecutorService to manage parallel HTTP requests.
 * - Utilizes Future and Callable for efficient thread management.
 * - Ensures thread safety with ConcurrentHashMap.
 * - Handles exceptions to prevent crashes.
 */
public class WebScraper {

    /** Shared HTTP client instance for making requests */
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /** Thread pool with a fixed size of 10 workers */
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    /** Thread-safe set to store unique links */
    private static final Set<String> links = ConcurrentHashMap.newKeySet();

    /**
     * Main method to execute the web scraper.
     *
     * @param args Command-line arguments (not used).
     * @pre User provides a valid search query.
     * @post Extracted links are processed, and page titles are displayed.
     */
    public static void main(String[] args) {
        System.out.println("Enter search query: ");
        Scanner scanner = new Scanner(System.in);
        String keyword = scanner.nextLine().trim();
        scanner.close();

        // Fetch search results and extract links
        List<String> extractedLinks = fetchSearchResults(keyword);

        if (extractedLinks.isEmpty()) {
            System.out.println("No results found. Try a different query.");
            return;
        }

        System.out.println("\nProcessing URLs...");

        // Execute tasks concurrently to fetch page titles
        List<Future<String>> futures = extractedLinks.stream()
                .map(url -> executor.submit(() -> fetchPageTitle(url)))
                .toList();

        // Retrieve and display results
        for (Future<String> future : futures) {
            try {
                System.out.println(future.get());
            } catch (ExecutionException | InterruptedException e) {
                System.err.println("Error processing a page: " + e.getMessage());
            }
        }

        // Shutdown executor gracefully
        shutdownExecutor();
    }

    /**
     * Fetches search results from DuckDuckGo API.
     *
     * @param query The search term to query.
     * @return A list of extracted URLs from the search results.
     * @pre Query must be a non-empty string.
     * @post Returns a list of URLs or an empty list if no results are found.
     * @throws IllegalArgumentException if the query is invalid.
     * @throws Exception if an error occurs during the HTTP request.
     */
    private static List<String> fetchSearchResults(String query) {
        if (query.isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty.");
        }

        String apiUrl = "https://api.duckduckgo.com/?q=" + query.replace(" ", "+") + "&format=json";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return extractLinks(response.body());
        } catch (Exception e) {
            System.err.println("Failed to fetch search results: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Extracts URLs from a JSON response.
     *
     * @param jsonResponse The JSON response containing search results.
     * @return A list of extracted URLs.
     * @pre The JSON response must be a valid DuckDuckGo search result.
     * @post Returns a list of URLs, or an empty list if no URLs are found.
     * @throws Exception if the JSON parsing fails.
     */
    private static List<String> extractLinks(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray relatedTopics = json.getJSONArray("RelatedTopics");

            for (int i = 0; i < relatedTopics.length(); i++) {
                JSONObject topic = relatedTopics.getJSONObject(i);
                if (topic.has("FirstURL")) {
                    links.add(topic.getString("FirstURL"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting links: " + e.getMessage());
        }
        return List.copyOf(links);
    }

    /**
     * Fetches the title of a webpage from a given URL.
     *
     * @param url The URL of the page.
     * @return The extracted page title or an error message if fetching fails.
     * @pre The URL must be a valid and accessible HTTP(S) link.
     * @post Returns the extracted title or an error message.
     * @throws Exception if an error occurs during the HTTP request.
     */
    private static String fetchPageTitle(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Matcher matcher = Pattern.compile("<title>(.*?)</title>").matcher(response.body());

            return matcher.find() ? "Title: " + matcher.group(1) + " | URL: " + url : "No title found | URL: " + url;
        } catch (Exception e) {
            return "Failed to fetch title for: " + url;
        }
    }

    /**
     * Shuts down the ExecutorService safely.
     *
     * @pre ExecutorService must be running.
     * @post The executor is shutdown gracefully.
     */
    private static void shutdownExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
