package pdt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javax.ws.rs.core.*;
import javax.ws.rs.*;

@Path("query")
public class Query{
    
	static Connection c = null;
	
	@GET
	@Path("connect")
	@Produces(MediaType.TEXT_PLAIN)
	public void connectToDB() throws ClassNotFoundException, SQLException{
		DatabaseConnector dbc = new DatabaseConnector();
        c = dbc.connect();
	}
	
	@GET
	@Path("getByDistance/{x}/{y}/{d}/{a}")
	@Produces(MediaType.APPLICATION_JSON)
    public static String getByDistance(@PathParam("x") double x,@PathParam("y") double y, @PathParam("d") int d, @PathParam("a") String a) throws SQLException, JSONException, ClassNotFoundException{
                
        Statement statement  = null;
        JSONArray jsonArray = new JSONArray();
        try {
            statement = c.createStatement();
            ResultSet query_results = statement.executeQuery("SELECT ST_AsGeoJSON(ST_TRANSFORM(way, 4326)) as way, name, amenity FROM planet_osm_point as p where ST_Distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(way))::geography) < " + d + "AND (amenity = " + a + ")");
            while(query_results.next()){
                
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "Feature");
                JSONObject geometry = new JSONObject(query_results.getString("way"));
                jsonObject.put("geometry", geometry);
                JSONObject properties = new JSONObject();
                String name = query_results.getString("name");
                properties.put("name", name);
                String amenity = query_results.getString("amenity");
                if (amenity.equals("pub")){
                	properties.put("marker-color", "#33cc33");
                }
                else if (amenity.equals("bar")){
                	properties.put("marker-color", "#ffff00");
                }
                else if (amenity.equals("nightclub")){
                	properties.put("marker-color", "#003399");
                }
                else if (amenity.equals("casino")){
                	properties.put("marker-color", "#ff3300");
                }
                else if (amenity.equals("cinema")){
                	properties.put("marker-color", "#ff9900");
                }
                else if (amenity.equals("theatre")){
                	properties.put("marker-color", "#cc0099");
                }

    			properties.put("marker-symbol", "marker");
    			properties.put("marker-size", "medium");
    			properties.put("amenity", query_results.getString("amenity"));
    			jsonObject.put("properties", properties);
    			jsonArray.put(jsonObject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            statement.close();
        }
        return jsonArray.toString();
    
    }
    
    
	@GET
	@Path("getNearestAndATM/{x}/{y}/{a}")
	@Produces(MediaType.APPLICATION_JSON)
    public static String getNearestAndATM(@PathParam("x") double x,@PathParam("y") double y, @PathParam("a") String a, @PathParam("b") String b) throws SQLException, JSONException, ClassNotFoundException{
                
        Statement statement  = null;
        JSONArray jsonArray = new JSONArray();
        try {
            statement = c.createStatement();
            ResultSet query_results = statement.executeQuery("select q.way1 as way1, q.operator as operator, q.amenity as amenity, q.way2 as way2, q.name as name, q.d1+q.d2 as distance from (select ST_AsGeoJSON(ST_TRANSFORM(way, 4326)) as way1, operator, amenity, (select ST_AsGeoJSON(ST_TRANSFORM(way, 4326)) from (select st_distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(way))::geography) as dist, way from planet_osm_point where amenity = '" + a +"' order by dist limit 1) as nr) as way2, (select name from (select name, st_distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(way))::geography) as dist, way from planet_osm_point where amenity = '" + a + "' order by dist limit 1) as nr) as name, st_distance((select ST_GeomFromText(ST_AsText(way))::geography from (select st_distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(way))::geography) as dist, way from planet_osm_point where amenity = '" + a + "' order by dist limit 1) as nr), ST_GeomFromText(ST_AsText(p.way))::geography) as d1, st_distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(p.way))::geography) as d2 from planet_osm_point as p where p.amenity = 'atm') as q order by distance limit 1");
            while(query_results.next()){
                
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("type", "Feature");
                JSONObject geometry = new JSONObject(query_results.getString("way1"));
                jsonObject1.put("geometry", geometry);
                JSONObject properties = new JSONObject();
                String operator = query_results.getString("operator");
                properties.put("name", operator);
                String amenity = query_results.getString("amenity");
                properties.put("amenity", amenity);
                properties.put("marker-color", "#663300");
    			properties.put("marker-symbol", "bank");
    			properties.put("marker-size", "medium");
    			//properties.put("amenity", query_results.getString("amenity"));
    			jsonObject1.put("properties", properties);
    			jsonArray.put(jsonObject1);
    			
    			JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("type", "Feature");
                JSONObject geometry2 = new JSONObject(query_results.getString("way2"));
                jsonObject2.put("geometry", geometry2);
                JSONObject properties2 = new JSONObject();
                if (a.equals("pub")){
                	properties2.put("marker-color", "#33cc33");
                }
                else if (a.equals("bar")){
                	properties2.put("marker-color", "#ffff00");
                }
                else if (a.equals("nightclub")){
                	properties2.put("marker-color", "#003399");
                }
                else if (a.equals("casino")){
                	properties2.put("marker-color", "#ff3300");
                }
                else if (a.equals("cinema")){
                	properties2.put("marker-color", "#ff9900");
                }
                else if (a.equals("theatre")){
                	properties2.put("marker-color", "#cc0099");
                }

    			properties2.put("marker-symbol", "marker");
    			properties2.put("marker-size", "medium");
    			String name = query_results.getString("name");
    			properties2.put("name", name);
    			properties2.put("amenity", a);
    			//properties.put("amenity", query_results.getString("amenity"));
    			jsonObject2.put("properties", properties2);
    			jsonArray.put(jsonObject2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            statement.close();
        }
        
        return jsonArray.toString();
    
    }
    
	
	@GET
	@Path("getNearestAndATMType/{x}/{y}/{a}/{b}")
	@Produces(MediaType.APPLICATION_JSON)
    public static String getNearestAndATMType(@PathParam("x") double x,@PathParam("y") double y, @PathParam("a") String a, @PathParam("b") String b) throws SQLException, JSONException, ClassNotFoundException{
                
        Statement statement  = null;
        JSONArray jsonArray = new JSONArray();
        try {
            statement = c.createStatement();
            ResultSet query_results = statement.executeQuery("select q.way1 as way1, q.operator as operator, q.amenity as amenity, q.way2 as way2, q.name as name, q.d1+q.d2 as distance from (select ST_AsGeoJSON(ST_TRANSFORM(way, 4326)) as way1, operator, amenity, (select ST_AsGeoJSON(ST_TRANSFORM(way, 4326)) from (select st_distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(way))::geography) as dist, way from planet_osm_point where amenity = '" + a +"' order by dist limit 1) as nr) as way2, (select name from (select name, st_distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(way))::geography) as dist, way from planet_osm_point where amenity = '" + a + "' order by dist limit 1) as nr) as name, st_distance((select ST_GeomFromText(ST_AsText(way))::geography from (select st_distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(way))::geography) as dist, way from planet_osm_point where amenity = '" + a + "' order by dist limit 1) as nr), ST_GeomFromText(ST_AsText(p.way))::geography) as d1, st_distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(p.way))::geography) as d2 from planet_osm_point as p where p.amenity = 'atm' AND operator like '%" + b + "%' ) as q order by distance limit 1");
            while(query_results.next()){
                
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("type", "Feature");
                JSONObject geometry = new JSONObject(query_results.getString("way1"));
                jsonObject1.put("geometry", geometry);
                JSONObject properties = new JSONObject();
                String operator = query_results.getString("operator");
                properties.put("name", operator);
                String amenity = query_results.getString("amenity");
                properties.put("amenity", amenity);
                properties.put("marker-color", "#663300");
    			properties.put("marker-symbol", "bank");
    			properties.put("marker-size", "medium");
    			//properties.put("amenity", query_results.getString("amenity"));
    			jsonObject1.put("properties", properties);
    			jsonArray.put(jsonObject1);
    			
    			JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("type", "Feature");
                JSONObject geometry2 = new JSONObject(query_results.getString("way2"));
                jsonObject2.put("geometry", geometry2);
                JSONObject properties2 = new JSONObject();
                if (a.equals("pub")){
                	properties2.put("marker-color", "#33cc33");
                }
                else if (a.equals("bar")){
                	properties2.put("marker-color", "#ffff00");
                }
                else if (a.equals("nightclub")){
                	properties2.put("marker-color", "#003399");
                }
                else if (a.equals("casino")){
                	properties2.put("marker-color", "#ff3300");
                }
                else if (a.equals("cinema")){
                	properties2.put("marker-color", "#ff9900");
                }
                else if (a.equals("theatre")){
                	properties2.put("marker-color", "#cc0099");
                }

    			properties2.put("marker-symbol", "marker");
    			properties2.put("marker-size", "medium");
    			String name = query_results.getString("name");
    			properties2.put("name", name);
    			properties2.put("amenity", a);
    			//properties.put("amenity", query_results.getString("amenity"));
    			jsonObject2.put("properties", properties2);
    			jsonArray.put(jsonObject2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            statement.close();
        }
        
        return jsonArray.toString();
    
    }
	
	
	@GET
	@Path("getNearestAndATM5/{x}/{y}/{a}")
	@Produces(MediaType.APPLICATION_JSON)
    public static String getNearestAndATMType(@PathParam("x") double x,@PathParam("y") double y, @PathParam("a") String a) throws SQLException, JSONException, ClassNotFoundException{
                
        Statement statement  = null;
        JSONArray jsonArray = new JSONArray();
        try {
            statement = c.createStatement();
            ResultSet query_results = statement.executeQuery("select ST_AsGeoJSON(ST_TRANSFORM(way, 4326)) as way1, operator, amenity, (select ST_AsGeoJSON(ST_TRANSFORM(way, 4326)) from (select st_distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(way))::geography) as dist, way from planet_osm_point where amenity = '" + a + "' order by dist limit 1) as nr) as way2, (select name from (select name, st_distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(way))::geography) as dist, way from planet_osm_point where amenity = '" + a + "' order by dist limit 1) as nr) as name, st_distance((select ST_GeomFromText(ST_AsText(way))::geography as way3 from (select st_distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(way))::geography) as dist, way from planet_osm_point where amenity = '" + a + "' order by dist limit 1) as nr), ST_GeomFromText(ST_AsText(p.way))::geography) as dist from planet_osm_point as p where p.amenity = 'atm' order by dist limit 5");
            while(query_results.next()){
                
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("type", "Feature");
                JSONObject geometry = new JSONObject(query_results.getString("way1"));
                jsonObject1.put("geometry", geometry);
                JSONObject properties = new JSONObject();
                String operator = query_results.getString("operator");
                properties.put("name", operator);
                String amenity = query_results.getString("amenity");
                properties.put("amenity", amenity);
                properties.put("marker-color", "#663300");
    			properties.put("marker-symbol", "bank");
    			properties.put("marker-size", "medium");
    			//properties.put("amenity", query_results.getString("amenity"));
    			jsonObject1.put("properties", properties);
    			jsonArray.put(jsonObject1);
    			
    			JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("type", "Feature");
                JSONObject geometry2 = new JSONObject(query_results.getString("way2"));
                jsonObject2.put("geometry", geometry2);
                JSONObject properties2 = new JSONObject();
                if (a.equals("pub")){
                	properties2.put("marker-color", "#33cc33");
                }
                else if (a.equals("bar")){
                	properties2.put("marker-color", "#ffff00");
                }
                else if (a.equals("nightclub")){
                	properties2.put("marker-color", "#003399");
                }
                else if (a.equals("casino")){
                	properties2.put("marker-color", "#ff3300");
                }
                else if (a.equals("cinema")){
                	properties2.put("marker-color", "#ff9900");
                }
                else if (a.equals("theatre")){
                	properties2.put("marker-color", "#cc0099");
                }
    			properties2.put("marker-symbol", "marker");
    			properties2.put("marker-size", "medium");
    			String name = query_results.getString("name");
    			properties2.put("name", name);
    			properties2.put("amenity", a);
    			//properties.put("amenity", query_results.getString("amenity"));
    			jsonObject2.put("properties", properties2);
    			jsonArray.put(jsonObject2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            statement.close();
        }
        
        return jsonArray.toString();
    
    }
	
	
	@GET
	@Path("getByDistanceWithPark/{x}/{y}/{d}/{a}")
	@Produces(MediaType.APPLICATION_JSON)
    public static String getByDistanceWithPark(@PathParam("x") double x,@PathParam("y") double y, @PathParam("d") int d, @PathParam("a") String a) throws SQLException, JSONException, ClassNotFoundException{
                
        Statement statement  = null;
        JSONArray jsonArray = new JSONArray();
        try {
            statement = c.createStatement();
            ResultSet query_results = statement.executeQuery("select ST_AsGeoJSON(ST_TRANSFORM(p.way, 4326)) as way1, ST_AsGeoJSON(ST_TRANSFORM(q2.way, 4326)) as way2, q2.name as name, q2.amenity as amenity from planet_osm_point as p, (select way, name, amenity from (SELECT way as way, name, amenity FROM planet_osm_point as p where ST_Distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(way))::geography) < " + d + " AND (amenity = " + a + ")) as q1) as q2 where p.amenity = 'parking' and st_distance(ST_GeomFromText(ST_AsText(p.way))::geography, ST_GeomFromText(ST_AsText(q2.way))::geography) < 200");
            while(query_results.next()){
                   			
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("type", "Feature");
                JSONObject geometry2 = new JSONObject(query_results.getString("way2"));
                jsonObject2.put("geometry", geometry2);
                JSONObject properties2 = new JSONObject();
                String name2 = query_results.getString("name");
                properties2.put("name", name2);
                String amenity2 = query_results.getString("amenity");
                if (amenity2.equals("pub")){
                	properties2.put("marker-color", "#33cc33");
                }
                else if (amenity2.equals("bar")){
                	properties2.put("marker-color", "#ffff00");
                }
                else if (amenity2.equals("nightclub")){
                	properties2.put("marker-color", "#003399");
                }
                else if (amenity2.equals("casino")){
                	properties2.put("marker-color", "#ff3300");
                }
                else if (amenity2.equals("cinema")){
                	properties2.put("marker-color", "#ff9900");
                }
                else if (amenity2.equals("theatre")){
                	properties2.put("marker-color", "#cc0099");
                }

    			properties2.put("marker-symbol", "marker");
    			properties2.put("marker-size", "medium");
    			properties2.put("amenity", query_results.getString("amenity"));
    			jsonObject2.put("properties", properties2);
    			jsonArray.put(jsonObject2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            statement.close();
        }
        
        return jsonArray.toString();
    
    }
	
	
	@GET
	@Path("getPark/{x}/{y}")
	@Produces(MediaType.APPLICATION_JSON)
    public static String getPark(@PathParam("x") double x,@PathParam("y") double y) throws SQLException, JSONException, ClassNotFoundException{
                
        Statement statement  = null;
        JSONArray jsonArray = new JSONArray();
        try {
            statement = c.createStatement();
            ResultSet query_results = statement.executeQuery("SELECT ST_AsGeoJSON(ST_TRANSFORM(way, 4326)) as way FROM planet_osm_point as p where ST_Distance(ST_GeomFromText('POINT(" + x + " " + y + ")')::geography, ST_GeomFromText(ST_AsText(way))::geography) < 200 AND (amenity = 'parking')");
            while(query_results.next()){
                
            	JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("type", "Feature");
                JSONObject geometry = new JSONObject(query_results.getString("way"));
                jsonObject1.put("geometry", geometry);
                JSONObject properties = new JSONObject();
                String name = "Parking";
                properties.put("name", name);
                String amenity = "parking";
               
                properties.put("marker-color", "#3399ff");
    			properties.put("marker-symbol", "parking");
    			properties.put("marker-size", "medium");
    			properties.put("amenity", amenity);
    			jsonObject1.put("properties", properties);
    			jsonArray.put(jsonObject1);
    			
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            statement.close();
        }
        
        return jsonArray.toString();
    
    }
	
	
	
}