package org.apache.cassandra.config;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AllocateAddressRequest;
import software.amazon.awssdk.services.ec2.model.DomainType;
import software.amazon.awssdk.services.ec2.model.AllocateAddressResponse;
import software.amazon.awssdk.services.ec2.model.AssociateAddressRequest;
import software.amazon.awssdk.services.ec2.model.AssociateAddressResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.DescribeAddressesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeAddressesRequest;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Address;
import java.util.Arrays;
import java.util.Collection;
import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils;

// import software.amazon.awssdk.http.ApacheHttpClient;
// import com.amazonaws.util.EC2MetadataUtils;
// import com.amazonaws.services.ec2.model.Address;
// import com.amazonaws.services.ec2.model.DescribeAddressesResult;

// snippet-end:[ec2.java2.allocate_address.import]

/**
 * Before running this Java V2 code example, set up your development environment, including your credentials.
 *
 * For more information, see the following documentation topic:
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */
public class AllocateAddress {
    public static String allocateAddress() {
        try {
            String instanceId =  "i-0a17b1a5f2662ecac";   // EC2MetadataUtils.getInstanceId(); //
            Region region = Region.AP_NORTHEAST_1;
            Ec2Client ec2 = Ec2Client.builder()
                .region(region)
                .build();
            Collection<String> filterValues = Arrays.asList("cassandra");
            Filter filter = Filter.builder().name("tag:cluster").values(filterValues).build();
            Collection<Filter> filters = Arrays.asList(filter);
            DescribeAddressesRequest addressRequest = DescribeAddressesRequest.builder().filters(filters).build();
            DescribeAddressesResponse response = ec2.describeAddresses();


            for(Address address : response.addresses()) {
                if (address.instanceId() == null){
                    System.out.printf(
                        "Found address with public IP %s, " +
                        "domain %s, " +
                        "allocation id %s " +
                        "associationId %s " +
                        "and NIC id %s",
                        address.publicIp(),
                        address.domainAsString(),
                        address.allocationId(),
                        address.instanceId(),
                        address.networkInterfaceId());
                    AssociateAddressRequest associateRequest = AssociateAddressRequest.builder()
                       .instanceId(instanceId)
                       .allocationId(address.allocationId())
                       .build();
                    try {
                        AssociateAddressResponse associateResponse = ec2.associateAddress(associateRequest);
                        break;
                    }
                    catch (Ec2Exception e) {
                        continue;
                    }
                }
            }

            // AllocateAddressRequest allocateRequest = AllocateAddressRequest.builder()
            //     .domain(DomainType.VPC)
            //     .build();

            // AllocateAddressResponse allocateResponse = ec2.allocateAddress(allocateRequest);
            // String allocationId = allocateResponse.allocationId();
            // AssociateAddressRequest associateRequest = AssociateAddressRequest.builder()
            //    .instanceId(instanceId)
            //    .allocationId(allocationId)
            //    .build();

            // AssociateAddressResponse associateResponse = ec2.associateAddress(associateRequest);
            ec2.close();
            
            return "";

        } catch (Ec2Exception e) {
           System.err.println(e.awsErrorDetails().errorMessage());
        }
        return "";
    }
}