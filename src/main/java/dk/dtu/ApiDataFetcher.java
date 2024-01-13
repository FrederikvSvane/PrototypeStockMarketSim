package dk.dtu;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;


public class ApiDataFetcher {
    private static String apiKey = "eSpgWVgLc3Xde6SZK0m89gyThTBXfrLp";
    private static String stockTicker = "AAPL";
    private static String apiUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + stockTicker + "&apikey=" + apiKey;
    private static String apiUrl2 = "https://financialmodelingprep.com/api/v3/search?query=" + stockTicker + "&apikey=" + apiKey;

    public static void main(String[] args) throws IOException {

    sendRequest();
    }

    public static void sendRequest() throws IOException {
        java.net.URL url = new URL(apiUrl2);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Print the response
            System.out.println(response.toString());
        } else {
            System.out.println("GET request not worked");
        }

    }

}
