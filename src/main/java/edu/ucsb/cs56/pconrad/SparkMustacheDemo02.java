package edu.ucsb.cs56.pconrad;

import static spark.Spark.port;

import org.apache.log4j.Logger;

// import alarm;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.Block;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Simple example of using Mustache Templates
 *
 */

public class SparkMustacheDemo02 {

	public static final String CLASSNAME="SparkMustacheDemo02";
	
	public static final Logger log = Logger.getLogger(CLASSNAME);

	public static void main(String[] args) {
        HashMap<String,String> envVars =  
			getNeededEnvVars(new String []{
					"MONGODB_USER",
					"MONGODB_PASS",
					"MONGODB_NAME",
					"MONGODB_HOST",
					"MONGODB_PORT"				
				});
		
		String uriString = mongoDBUri(envVars);

        port(getHerokuAssignedPort());
		
		Map map = new HashMap();
        map.put("name", "Sam");
		
        // hello.mustache file is in resources/templates directory
        get("/", (rq, rs) -> new ModelAndView(map, "home.mustache"), new MustacheTemplateEngine());

		get("/create", (rq, rs) -> new ModelAndView(map, "create.mustache"), new MustacheTemplateEngine());
        get("/join", (rq, rs) -> new ModelAndView(map, "join.mustache"), new MustacheTemplateEngine());
        post("/createresult", (rq, rs) -> {
            alarm newAlarm = new alarm (""+ rq.queryParams("date"), ""+rq.queryParams("time"), rq.queryParams("purpose"));
			write2DB(newAlarm,uriString);
			
			Map map2 = new HashMap();
			map2.put("date", newAlarm.getDate());
			map2.put("time", newAlarm.getTime());
			map2.put("purpose", newAlarm.getPurpose());
			map2.put("key", newAlarm.hashCode());

            return new ModelAndView(map2, "createresult.mustache");
        }, new MustacheTemplateEngine());
		get("/joinresult", (rq, rs) -> {
			int key = Integer.parseInt(rq.queryParams("key"));
			alarm newAlarm = fetchFromDB(key, uriString);
			Map map3 = new HashMap();
			// if (newAlarm.getPurpose().isEmpty()){
			// 	map3.put("Massage", "It seems like the Alarm you want to join does not exist");
			// }else{
				map3.put("Massage","You joined an GauchoAlarm successfully!");
				map3.put("date", newAlarm.getDate());
				map3.put("time", newAlarm.getTime());
				map3.put("purpose", newAlarm.getPurpose());
				map3.put("key", key);
			//}
			return new ModelAndView(map3, "joinresult.mustache");
		}, new MustacheTemplateEngine());
	}
	
    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }



    public static HashMap<String,String> getNeededEnvVars(String [] neededEnvVars) {

        ProcessBuilder processBuilder = new ProcessBuilder();
    		
		HashMap<String,String> envVars = new HashMap<String,String>();
		//System.out.print(processBuilder.environment());
		
		boolean error=false;		
		for (String k:neededEnvVars) {
			String v = processBuilder.environment().get(k);
			if ( v!= null) {
				envVars.put(k,v);
			} else {
				error = true;
				System.err.println("Error: Must define env variable " + k);
			}
        }
		
		if (error) { System.exit(1); }

		System.out.println("envVars=" + envVars);
		return envVars;	 
    }
    public static String mongoDBUri(HashMap<String,String> envVars) {

		System.out.println("envVars=" + envVars);
		
		// mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
		String uriString = "mongodb://" +
			envVars.get("MONGODB_USER") + ":" +
			envVars.get("MONGODB_PASS") + "@" +
			envVars.get("MONGODB_HOST") + ":" +
			envVars.get("MONGODB_PORT") + "/" +
			envVars.get("MONGODB_NAME");
		System.out.println("uriString=" + uriString);
		return uriString;
    }
    
    public static void write2DB(alarm Alarm, String uriString){
        MongoClientURI uri  = new MongoClientURI(uriString); 
        MongoClient client = new MongoClient(uri);
        MongoDatabase db = client.getDatabase(uri.getDatabase());
        MongoCollection<Document> data = db.getCollection("data");
        int key = Alarm.hashCode();
        String json = Alarm.toJson();
        
		data.insertOne(new Document("key", key)
                            .append("content", json));

	}
	
	public static alarm fetchFromDB(int key, String uriString){
		MongoClientURI uri  = new MongoClientURI(uriString); 
        MongoClient client = new MongoClient(uri);
        MongoDatabase db = client.getDatabase(uri.getDatabase());
		MongoCollection<Document> data = db.getCollection("data");

		Document doc = data.find(eq("key",key)).first();
		alarm newAlarm = new alarm();
		if (!doc.isEmpty()){
			newAlarm = alarm.toClass(doc.get("content").toString());
		}
		return newAlarm;

	}

	
}
