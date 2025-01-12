package ccsr.project.kafka.config;

import java.util.HashMap;
import java.util.Map;

public class Config {

    public static String KAFKA_SERVERS = "";
    public static HashMap<String,String> BD_CONFIG = new HashMap<>();

    public static void setServers (Map<String,Object> map){

        KAFKA_SERVERS = map.get("servers_kafka") == null ? "" : map.get("servers_kafka").toString();

        BD_CONFIG.put("db_host",map.get("db_host") == null ? "" : map.get("db_host").toString());
        BD_CONFIG.put("db_port",map.get("db_port") == null ? "" : map.get("db_port").toString());
        BD_CONFIG.put("db_name",map.get("db_name") == null ? "" : map.get("db_name").toString());
        BD_CONFIG.put("db_user",map.get("db_user") == null ? "" : map.get("db_user").toString());
        BD_CONFIG.put("db_password",map.get("db_password") == null ? "" : map.get("db_password").toString());

    }
}
