import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyHttpClient {

    private static final String URL = "http://localhost:3000/";

    private static String jsonString;
    private static String token;

    public static void main(String[] args) throws IOException, InterruptedException {
        String ip = "192.168.56.1";
        int port = 3000;//proxy port

        //CREATE POST PARAMS FOR REGISTER
        try {
            jsonString = new JSONObject()
                    .put("password" , "password")
                    .put("email" , "email@gmail.com")
                    .toString();
            System.out.println(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .GET() // GET is default
                .build();
        HttpResponse<Void> response = client.send(request,
                HttpResponse.BodyHandlers.discarding());
        System.out.println(response);
        if (response.statusCode() == 200) {
            System.out.println("\n\n POST request:");
            sendPost(ip, port);

            System.out.println("\n\n GET request:");
            sendGet(ip, port);

            System.out.println("\n\n HEAD request:");
            sendHead(ip, port);

            System.out.println("\n\n OPTIONS request:");
            sendOptions(ip, port);


        }

    }

    private static void sendGet(String hostname, int port) throws IOException {
        java.net.URL obj = new URL("http://localhost:3000/api/user/get_participants");
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port));
        HttpURLConnection con = (HttpURLConnection) obj.openConnection(proxy);
        con.setRequestMethod("GET");
        con.setRequestProperty("x-access-token", token);
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
            in.close();
        }
    }

    private static void sendHead(String hostname, int port) throws IOException, InterruptedException {
        System.setProperty("http.proxyHost", hostname);
        System.setProperty("http.proxyPort", String.valueOf(port));
        HttpClient client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(URL)).method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        HttpHeaders headers = response.headers();

        headers.map().forEach((key, values) -> {
            System.out.printf("%s: %s%n", key, values);
        });
    }


    public static void sendOptions(String hostname, int port) throws IOException {
        URL obj = new URL(URL);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port));
        HttpURLConnection con = (HttpURLConnection) obj.openConnection(proxy);
        con.setRequestMethod("OPTIONS");
        System.out.println("Allow: " + con.getHeaderField("Allow"));
    }

    public static void sendPost(String hostname, int port) throws IOException {
        URL obj = new URL("http://localhost:3000/api/user/login");
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port));
        byte[] postData       = jsonString.getBytes( StandardCharsets.UTF_8 );
        HttpURLConnection con= (HttpURLConnection) obj.openConnection(proxy);
        con.setDoOutput( true );
        con.setInstanceFollowRedirects( false );
        con.setRequestMethod("POST");
        con.setRequestProperty( "Content-Type", "application/json");
        con.setUseCaches( false );

        OutputStream os = con.getOutputStream();
        try( DataOutputStream wr = new DataOutputStream( con.getOutputStream())) {
            wr.write( postData );
        }
        os.flush();
        os.close();
        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                Pattern pattern = Pattern.compile("token\":\"(.*?)\"");
                Matcher matcher = pattern.matcher(inputLine);
                if (matcher.find()) {
                    System.out.println(matcher.group(1));
                    token = matcher.group(1);
                }
            }

            in.close();
        }
    }

}
