import io.nats.client.Connection;
import io.nats.client.Nats;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Scanner;

public class PrimeNumberServer {

    public static void main(String[] args) {
        try {
            // Configure the NATS connection
            Connection natsConnection = Nats.connect("nats://demo.nats.io:4222");

            // Read user configurable parameters
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter publishing interval (ms): ");
            int interval = scanner.nextInt();
            System.out.print("Enter max random number: ");
            int maxNumber = scanner.nextInt();

            // Secure random number generator
            SecureRandom random = new SecureRandom();

            while (true) {
                // Generate a random integer between 1 and the maximum number
                int randomNumber = random.nextInt(maxNumber) + 1;

                // Create and publish the message
                JSONObject message = new JSONObject();
                message.put("question", randomNumber);
                natsConnection.publish("primeNumbers", message.toString().getBytes(StandardCharsets.UTF_8));

                // Output the message to the server console
                System.out.println("Published: " + message.toString());

                // Sleep for the configured interval
                Thread.sleep(interval);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
