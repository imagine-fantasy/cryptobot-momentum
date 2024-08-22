package com.crypto.cmtrade.cryptobot.client;

import com.crypto.cmtrade.cryptobot.model.CoinMarketCapResponse;
import com.crypto.cmtrade.cryptobot.model.CryptoData;
import com.google.gson.Gson;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CoinMarketCapApiClient {

    @Value("${coinmarketcap.api.key}")
    private String apiKey;

    @Value("${coinmarketcap.api.url}")
    private String apiUrl;

    public List<CryptoData> getTop20Cryptocurrencies() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            List<NameValuePair> parameters =new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("start","1"));
            parameters.add(new BasicNameValuePair("limit","20"));
            parameters.add(new BasicNameValuePair("convert","USD"));
            parameters.add(new BasicNameValuePair("sort_dir","desc"));
            parameters.add(new BasicNameValuePair("sort","percent_change_24h"));
            URIBuilder uriBuilder = new URIBuilder(apiUrl);
            uriBuilder.addParameters(parameters);

            HttpGet request = new HttpGet(uriBuilder.build());
            request.addHeader("X-CMC_PRO_API_KEY", apiKey);
            request.addHeader("Accept", "application/json");

            return httpClient.execute(request, response -> {
                String result = EntityUtils.toString(response.getEntity());
                Gson gson = new Gson();
                CoinMarketCapResponse cmcResponse = gson.fromJson(result, CoinMarketCapResponse.class);
                return cmcResponse.getData();
            });
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


}