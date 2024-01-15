package dk.dtu;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import dk.dtu.company.Company;
import org.json.JSONArray;
import org.json.JSONObject;



public class ApiDataFetcher {
    public static String quarter = "Q1";
    public static int year = 2020;
    private static String apiKey = "eSpgWVgLc3Xde6SZK0m89gyThTBXfrLp";
    private static String stockTicker = "MSFT";
    private static String apiUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + stockTicker + "&apikey=" + apiKey;
    private static String apiUrl2 = "https://financialmodelingprep.com/api/v3/income-statement/" + stockTicker + "?period=annual&limit=50&apikey=" + apiKey;
    private static String apiQuarterURL = "https://financialmodelingprep.com/api/v3/income-statement/" + stockTicker + "?year="+year+"&period="+quarter+"&limit=50&apikey=" + apiKey;


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

            JSONArray jsonArray = new JSONArray(response.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //
                String year = jsonObject.getString("date");
                long revenue = jsonObject.getLong("revenue");
                long costOfRevenue = jsonObject.getLong("costOfRevenue");
                long grossProfit = jsonObject.getLong("grossProfit");

                System.out.println("date:"+year+"Revenue: " + revenue + " Cost of Revenue: " + costOfRevenue + " Gross Profit: " + grossProfit);
            }

            // Print the response
            //System.out.println(response.toString());
        } else {
            System.out.println("GET request not worked");
            System.out.println("Response Code: " + responseCode);
            System.out.println("Response Message: " + connection.getResponseMessage());
        }

    }

}
