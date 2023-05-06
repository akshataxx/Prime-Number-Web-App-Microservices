import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.Nats;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PrimeNumberClient {

    public static void main(String[] args) {
        try {
            // Configure the NATS connection
            Connection natsConnection = Nats.connect("nats://demo.nats.io:4222");

            // Read user configurable parameters
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter remote RPC endpoint URL: ");
            String remoteRpcEndpointUrl = scanner.nextLine();

            // Subscribe to the "primeNumbers" subject
            Dispatcher dispatcher = natsConnection.createDispatcher((Message message) -> {
                String jsonString = new String(message.getData(), StandardCharsets.UTF_8);
                JSONObject receivedMessage = new JSONObject(jsonString);
                int question = receivedMessage.getInt("question");

                if (question > 1000000) {
                    return; // Ignore questions with integer values larger than 1,000,000
                }

                // Calculate prime numbers and measure time
                long startTime = System.currentTimeMillis();
                List<Integer> primeNumbers = calculatePrimeNumbers(question);
                long timeTaken = System.currentTimeMillis() - startTime;

                // Create the response message
                JSONObject response = new JSONObject();
                response.put("answer", new JSONArray(primeNumbers));
                response.put("time_taken", timeTaken);

                // Output the response message to the client console
                System.out.println("Response: " + response.toString());

                // Send the response message as a POST REST call to a remote RPC endpoint
                HttpResponse<JsonNode> httpResponse = Unirest.post(remoteRpcEndpointUrl)
                        .header("Content-Type", "application/json")
                        .body(response)
                        .asJson();

                System.out.println("Response status: " + httpResponse.getStatus());
            });

            dispatcher.subscribe("primeNumbers");

            // Keep the client running
            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Integer> calculatePrimeNumbers(int maxNumber) {
        List<Integer> primeNumbers = new ArrayList<>();
        for (int i = 2; i <= maxNumber; i++) {
            if (isPrime(i)) {
                primeNumbers.add(i);
            }
        }
        return primeNumbers;
    }

    private static boolean isPrime(int number) {
        if (number <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }
}
