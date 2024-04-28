package br.com.carrefour.vtex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
public class HttpService {
    private HttpService() {
    }

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String HOST = "https://carrefourbr.vtexcommercestable.com.br";


    private static String[] getHeaders() {
        return Arrays.asList(
//                "Accept", "*.*",
//                "Accept-Encoding", "gzip, deflate, br",
//                "Cookie", "janus_sid=98bc760e-bb14-4dcd-a562-4c03155eb910",
                "X-VTEX-API-AppKey", "vtexappkey-carrefourbr-PDFRLF",
                "X-VTEX-API-AppToken", "SFIFQPCHYBPQTRWNPAGHIKYTBTHLZYMMAVISRHUBSJQJSPHRRVRATVNIHPDWHQIAPCQXIEVHFFPZUOAMMPUEGMSEHALCEYTBAPUQZJJNZKUOFONTORKRWEUDJLPHSTDH"
//                "X-VTEX-API-AppKey", "vtexappkey-carrefourbr-MYPJVA",
//                "X-VTEX-API-AppToken", "TDELEGMZOTAYMMPRPRYMBFYJNHTQTEIBYFXSHEYVODVESVSHABTUUDMCGKOQERJSWGUKDMQXVWOMTWUTZZDDWTWPMHTLTFUXOVVJNQLBWHYTMMBOGJDUNAGQLPYUTCQJ"
        ).toArray(new String[0]);
    }

    public static String getSkuContext(String skuId) {
        try {
            var url = String.format("%s/api/catalog/pvt/stockkeepingunit/%s", HOST, skuId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .headers(getHeaders())
                    .GET()
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200)
                return response.body();
        } catch (Exception e) {
            log.info(String.format("ERROR getSku [ %s ]", e.toString()));
        }

        return null;
    }

    public static CompletableFuture<HttpResponse<String>> getEansBySku(String skuId) {
        try {
            var url = String.format("%s/api/catalog/pvt/stockkeepingunit/%s/ean", HOST, skuId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .headers(getHeaders())
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.info(String.format("ERROR getOneEanVtex [ %s ]", e.toString()));
        }

        return null;
    }

    public static List<String> getOneEanVtex(String skuId) {
        try {
            var url = String.format("%s/api/catalog/pvt/stockkeepingunit/%s/ean", HOST, skuId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .headers(getHeaders())
                    .GET()
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<String> eans = new ArrayList<>();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode jn = mapper.readTree(response.body());

                jn.forEach(ean -> eans.add(ean.asText()));

                return eans;
            }
        } catch (Exception e) {
            log.info(String.format("ERROR getOneEanVtex [ %s ]", e.toString()));
        }

        return Collections.emptyList();
    }

    public static boolean delAllEansVtex(String skuId) {
        try {
            var url = String.format("%s/api/catalog/pvt/stockkeepingunit/%s/ean", HOST, skuId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .headers(getHeaders())
                    .DELETE()
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return true;
            }
        } catch (Exception e) {
            log.info(String.format("ERROR delAllEansVtex [ %s ]", e));
        }

        return false;
    }

    public static boolean addEanVtex(String skuId, String skuEan) {
        try {
            var url = String.format("%s/api/catalog/pvt/stockkeepingunit/%s/ean/%s", HOST, skuId, skuEan);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .headers(getHeaders())
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return true;
            }
        } catch (Exception e) {
            log.info(String.format("ERROR addEanVtex [ %s ]", e));
        }

        return false;
    }

    public static Path getXml(String url, String filePath) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .headers("Accept", "application/xml")
                    .GET()
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(Paths.get(filePath)));

            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            log.info(String.format("ERROR getXML [ %s ]", e));
        }

        return null;
    }

    private static Document loadTestDocument(String url) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new URL(url).openStream());
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }



}
