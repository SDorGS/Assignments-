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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * WebScraper class performs concurrent web scraping using Java's ExecutorService.
 * It fetches Google search results, extracts relevant URLs, and retrieves web page titles.
 *
 * Concurrency Features:
 * - Uses HttpClient for HTTP requests.
 * - Uses ExecutorService for parallel execution of tasks.
 * - Uses Jsoup for HTML parsing.
 * - Ensures thread safety with ConcurrentHashMap.
 * - Handles exceptions gracefully.
 */
public class WebScraper {

    /** Shared HTTP client instance for making requests */
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /** Thread pool with a fixed size of 10 workers */
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    /** Thread-safe set to store unique links */
    private static final Set<String> links = ConcurrentHashMap.newKeySet();

    /**
     * Entry point for the web scraper program.
     *
     * @param args Command-line arguments (not used).
     * @pre User provides a valid search query.
     * @post Extracted links are processed and displayed.
     */
    public static void main(String[] args) {
        System.out.println("Enter search query: ");
        Scanner scanner = new Scanner(System.in);
        String keyword = scanner.nextLine();
        scanner.close();

        String searchUrl = "https://www.google.com/search?q=" + keyword.replace(" ", "+");

        try {
            // Fetch HTML content from Google search results
            String searchResults = fetchContent(searchUrl);

            // Extract links from the search results
            List<String> extractedLinks = extractLinks(searchResults);
            System.out.println("\nProcessing URLs...");

            // Execute tasks concurrently
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
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Fetches the HTML content from the given URL.
     *
     * @param url The URL to fetch content from.
     * @return The HTML content as a string, or an empty string if an error occurs.
     * @pre URL must be a valid and accessible HTTP(S) link.
     * @post Returns the HTML response body or an empty string if a failure occurs.
     * @throws IllegalArgumentException if the URL is malformed.
     * @throws Exception if there is an issue with the HTTP request.
     */
    private static String fetchContent(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            System.err.println("Failed to fetch content: " + e.getMessage());
            return "";
        }
    }

    /**
     * Extracts URLs from the provided HTML content using regex.
     *
     * @param html The HTML content as a string.
     * @return A list of extracted URLs.
     * @pre The input must be a valid HTML string containing URLs.
     * @post Returns a list of unique extracted URLs.
     */
    private static List<String> extractLinks(String html) {
        Pattern pattern = Pattern.compile("(https?://[\\w./?=&#-]+)");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            links.add(matcher.group(1));
        }
        return List.copyOf(links);
    }

    /**
     * Fetches the title of a web page given its URL.
     *
     * @param url The URL of the page.
     * @return The page title, or an error message if fetching fails.
     * @pre URL must be a valid and accessible website.
     * @post Returns the extracted title or an error message.
     * @throws Exception if an error occurs while connecting to the page.
     */
    private static String fetchPageTitle(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return "Title: " + doc.title() + " | URL: " + url;
        } catch (Exception e) {
            return "Failed to fetch title for: " + url;
        }
    }
}
