package com.pm.stack;



import com.amazonaws.services.rds.model.DBInstance;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.appmesh.HealthCheckConfig;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;
import software.amazon.awscdk.services.rds.DatabaseInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class LocalStack extends Stack {

    private final Vpc vpc;
    private final Cluster ecsCluster;

    public LocalStack(final App scope, final String id,final StackProps props) {

        super(scope, id , props);
        this.vpc = createVpc();

        DatabaseInstance authServiceDB = createDatabaseInstance("AuthServiceDB","auth-service-db");
        DatabaseInstance patientServiceDB = createDatabaseInstance("PatientServiceDB","patient-service-db");

        CfnHealthCheck authDBHealthCheck = checkDbHealth(authServiceDB,"authDBHealthCheck");
        CfnHealthCheck patientDBHealthCheck = checkDbHealth(patientServiceDB,"patientDBHealthCheck");

        CfnCluster mskKafkaCluster = createMskCluster();

        this.ecsCluster = createEcsCluster();

        FargateService authService = createFargateService("AuthService","auth-service"
        ,List.of(4005)
        ,authServiceDB,Map.of("JWT_SECRET","R29vZ0pvYlNlY3JldEtleVNlY3VyZUxvbmdBbmRFbmNvZGVkIQ==")
        );
        authService.getNode().addDependency(authDBHealthCheck);
        authService.getNode().addDependency(authServiceDB);

        FargateService billingService = createFargateService("BillingService","billing-dervice",
                List.of(4001,9001),
                null,null);

        FargateService analyticsService = createFargateService(
                "AnalyticsService","analytics-service",
                List.of(4002)
                ,null
                ,null
        );
        analyticsService.getNode().addDependency(mskKafkaCluster);

        FargateService patientService = createFargateService(
                "patientService",
                "patient-service",
                List.of(4000),
                patientServiceDB,
                Map.of(
                        "BILLING_SERVER_ADDRESS","host.docker.internal",
                        "BILLING_SERVER_GRPC_PORT","9001"
                )
        );
        patientService.getNode().addDependency(billingService);
        patientService.getNode().addDependency(mskKafkaCluster);
        patientService.getNode().addDependency(patientDBHealthCheck);
        patientService.getNode().addDependency(patientServiceDB);

        createAPIGatewayFargateService();
    }

    public Vpc createVpc() {
        return Vpc.Builder.create(this,"patientManagementVpc")
                .vpcName("patientManagementVpc")
                .maxAzs(2)
                .build();
    }

    public  Cluster createEcsCluster() {
       return Cluster.Builder.create(this,"patientManagementEcsCluster")
                .vpc(vpc)
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()//this gives a name to this cluster which can used for service discover eg: auth-service.patiant-management.local will be url for auth service
                        .name("patiant-management.local")
                        .build()).build();
    }

    public DatabaseInstance createDatabaseInstance(String id, String dbName) {

        return DatabaseInstance.Builder
                .create(this,id)
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps
                                .builder().version(PostgresEngineVersion.VER_17_2)
                                .build()
                ))
                .vpc(this.vpc)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))//we can use to specify size and instance type
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

    }

    public CfnHealthCheck checkDbHealth(DatabaseInstance db , String id) {
        return CfnHealthCheck.Builder.create(this,id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP")
                        .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                        .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30)
                        .failureThreshold(3)
                        .build())
        .build();
    }

    public CfnCluster createMskCluster(){

        return CfnCluster.Builder.create(this,"MskCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("2.8.0")
                .numberOfBrokerNodes(1)
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                        .instanceType("kafka.m5.xlarge")
                        .clientSubnets(vpc.getPrivateSubnets().stream()
                                .map(ISubnet::getSubnetId)
                                .collect(Collectors.toList()))
                        .brokerAzDistribution("DEFAULT")
                        .build())
                .build();
    }

    //to create a new ecs service
    private FargateService createFargateService(String id, String imageName, List<Integer> ports, DatabaseInstance dbInstance, Map<String, String> env) {
        FargateTaskDefinition fargateTaskDefinition = FargateTaskDefinition.Builder.create(this,id+"task")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();
        ContainerDefinitionOptions.Builder containerDefinitionOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry(imageName))
                .portMappings(ports.stream()
                        .map(port -> PortMapping.builder()
                                .containerPort(port)
                                .hostPort(port)
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                .logGroup(LogGroup.Builder.create(this,id+"logGroup")
                                        .logGroupName("/ecs/"+imageName)
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .retention(RetentionDays.ONE_DAY)
                                        .build())
                                .streamPrefix(imageName)
                        .build()));
        Map<String, String> envs = new HashMap<>();
        envs.put("SPRING_KAFKA_BOOTSTRAP_SERVERS","localhost.localstack.cloud:4510, localhost.localstack.cloud:4511, localhost.localstack.cloud:4512");

        if(env!=null)
            envs.putAll(env);
        if(dbInstance!=null)
        {
            envs.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db".formatted(
                    dbInstance.getInstanceIdentifier(),
                    dbInstance.getDbInstanceEndpointPort(),
                    imageName
            ));
            envs.put("SPRING_DATASOURCE_USERNAME", "admin_user");
            envs.put("SPRING_DATASOURCE_PASSWORD",
                    dbInstance.getSecret().secretValueFromJson("password").toString());
            envs.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
            envs.put("SPRING_SQL_INIT_MODE", "always");
            envs.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");
        }
        containerDefinitionOptions.environment(envs);
        fargateTaskDefinition.addContainer("container", containerDefinitionOptions.build());

        return FargateService.Builder.create(this, id)
                .cluster(ecsCluster)
                .taskDefinition(fargateTaskDefinition)
                .assignPublicIp(false)
                .serviceName(imageName)
                .build();

    }
    private void createAPIGatewayFargateService(){
        FargateTaskDefinition fargateTaskDefinition = FargateTaskDefinition.Builder.create(this,"ApiGatewayTaskDefinition")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();
        ContainerDefinitionOptions apiContainerDefinitionOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry("api-gateway"))
                .environment(Map.of(
                        "SPRING_PROFILE_ACTIVE","prod",
                        "AUTH_SERVICE_URL","http://docker.internal:4005"
                ))
                .portMappings(List.of(4004).stream()
                        .map(port -> PortMapping.builder()
                                .containerPort(port)
                                .hostPort(port)
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this,"ApiGatewaylogGroup")
                                .logGroupName("/ecs/api-gateway")
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_DAY)
                                .build())
                        .streamPrefix("api-gateway")
                        .build()))
                .build();

        fargateTaskDefinition.addContainer("ApiGatewayService",apiContainerDefinitionOptions);

        ApplicationLoadBalancedFargateService applicationLoadBalancedFargateService =
                ApplicationLoadBalancedFargateService.Builder.create(this,"APIGatewayFargateService")
                        .cluster(ecsCluster)
                        .serviceName("api-gateway")
                        .taskDefinition(fargateTaskDefinition)
                        .desiredCount(1)
                        .healthCheckGracePeriod(Duration.seconds(60))
                        .build();


    }

    public static void main(String[] args){

        App app = new App(AppProps.builder().outdir("./cdk.out").build());
        StackProps props = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer())
                .build();

        new LocalStack(app,"localstack",props);
        app.synth();
    }
}
