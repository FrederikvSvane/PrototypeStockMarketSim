package dk.dtu.company.api;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jspace.Space;


public class ApiDataFetcher {
      //API key needed for access to the API
    private static String apiKey = "eSpgWVgLc3Xde6SZK0m89gyThTBXfrLp";
    private static String stockTicker; //Ticker for det selskab vi vil have data fra.


    //The two methods is basically the same, but they query different endpoints to get different data.
    public static void sendRequestIncome(String ticker, Space tupleSpace) throws IOException, InterruptedException {

        stockTicker = ticker;

        //Creating the endpoint URL that we will query
        String apiUrl2 = "https://financialmodelingprep.com/api/v3/income-statement/" + stockTicker + "?period=annual&limit=50&apikey=" + apiKey;
        java.net.URL url = new URL(apiUrl2);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        //Gemmer response code fra GET request
        int responseCode = connection.getResponseCode();

        //Check if response code is 200, then we have data.
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            //Reads the response and appends it to a stringbuffer
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            //Closes the connection and reader
            in.close();

            //JSON is parsed here. The data that we actually want is saved.
            JSONArray jsonArray = new JSONArray(response.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //Tjekker responset for specifikke keys og gemmer værdierne.
                String year = jsonObject.getString("date").substring(0,4);
                int yearInt = Integer.parseInt(year);
                long revenue = jsonObject.getLong("revenue");
                long costOfRevenue = jsonObject.getLong("costOfRevenue");
                long grossProfit = jsonObject.getLong("grossProfit");

                //Create a FinancialData object with the data we have saved.
                FinancialData data = new FinancialData(revenue, costOfRevenue, grossProfit);

                //Puts in the data for a specific year in the tuplespace. This is done for each year.
                tupleSpace.put(ticker,"Income Statement", yearInt, data);
                System.out.println("date:"+year+"Revenue: " + revenue + " Cost of Revenue: " + costOfRevenue + " Gross Profit: " + grossProfit);
            }
        } else {
            System.out.println("GET request not worked");
            System.out.println("Response Code: " + responseCode);
            System.out.println("Response Message: " + connection.getResponseMessage());
        }
    }

    public static void sendRequestBalanceSheet(String ticker, Space tupleSpace) throws IOException, InterruptedException {
        stockTicker = ticker;
        //Sætter endpoint URL og laver en GET request for at få noget data.
        String apiUrl2 = "https://financialmodelingprep.com/api/v3/balance-sheet-statement/" + stockTicker + "?period=annual&limit=50&apikey=" + apiKey;
        java.net.URL url = new URL(apiUrl2);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        //Gemmer response code fra GET request
        int responseCode = connection.getResponseCode();
        //Hvis response code er 200, så har vi fået data.
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            //Lukker connection og reader.
            in.close();

            //Følgende kode er til at parse JSON dataen. Herefter bliver de data vi gerne vil bruge gemt.
            JSONArray jsonArray = new JSONArray(response.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //Tjekker responset for specifikke keys og gemmer værdierne.
                String year = jsonObject.getString("date").substring(0,4);
                int yearInt = Integer.parseInt(year);
                long cash = jsonObject.getLong("cashAndCashEquivalents");
                long netReceiveable = jsonObject.getLong("netReceivables"); //cash position + everything that is owed to the company
                long totalAssets = jsonObject.getLong("totalAssets");
                long totalDebt = jsonObject.getLong("totalDebt");

                //Laver et FinancialData objekt med de data vi har gemt.
                FinancialData data = new FinancialData(cash, netReceiveable, totalAssets, totalDebt);

                //Indsætter data for et bestemt år i tuplespace. Dette gøres for hvert år.
                tupleSpace.put(ticker,"Balance Sheet", yearInt, data);
                // System.out.println("date:"+year+"Revenue: " + revenue + " Cost of Revenue: " + costOfRevenue + " Gross Profit: " + grossProfit);
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

