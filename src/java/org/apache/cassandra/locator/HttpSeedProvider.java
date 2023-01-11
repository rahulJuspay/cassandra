package org.apache.cassandra.locator;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.net.http.HttpClient;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.util.function.Supplier;
import java.net.URI;
import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;



public class HttpSeedProvider implements SeedProvider
{
    private static final Logger logger = LoggerFactory.getLogger(HttpSeedProvider.class);
    public HttpClient client = HttpClient.newHttpClient();

    public HttpSeedProvider(Map<String, String> args) {}

    public List<InetAddressAndPort> getSeeds()
    {
        Config conf;
        List<InetAddressAndPort> seeds = new ArrayList<>(0);
        try
        {
            conf = DatabaseDescriptor.loadConfig();
            

            Region region = Region.AP_NORTHEAST_1;
            LambdaClient lambda = LambdaClient.builder()
                 .region(region)
                //  .credentialsProvider(ProfileCredentialsProvider.create())
                 .build();
            InvokeRequest request2 = InvokeRequest.builder().functionName("arn:aws:lambda:ap-northeast-1:183853867090:function:getSeedsFromMem").build();
            var res2 = lambda.invoke(request2).payload().asUtf8String();
            System.out.println(res2);

            // var request = HttpRequest.newBuilder(
            // URI.create(conf.seed_provider.parameters.get("seedsUrl")))
            // .header("accept", "application/json")
            // .build();
            // var response = client.send(request, BodyHandlers.ofString());
            String[] hosts = res2.replace("\"", "").split(",", -1);
            seeds = new ArrayList<>(hosts.length);
            for (String host : hosts)
            {
                String hostPortString = host.trim();
                if (hostPortString.split(":").length != 2){
                    hostPortString = hostPortString + ":" + conf.seed_provider.parameters.get("defaultPort");
                }
                System.out.println(hostPortString);
                try
                {
                    if(!hostPortString.isEmpty()) {
                        seeds.add(InetAddressAndPort.getByName(hostPortString));
                    }
                }
                catch (UnknownHostException ex)
                {
                logger.warn("Seed provider couldn't lookup host {}", host);
                }
            }
        }
        catch (Exception e)
        {
            throw new AssertionError(e);
        }
        return Collections.unmodifiableList(seeds);
    }
}
