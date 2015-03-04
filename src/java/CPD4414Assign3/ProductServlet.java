/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CPD4414Assign3;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Amanda Cohoon - c0628569
 */

@Path("/products")
public class ProductServlet {
    
    @GET
    @Produces("application/json")
    public Response doGet() {
        return Response.ok(getResults("SELECT * FROM product"), MediaType.APPLICATION_JSON).build();
    }
    
    @GET
    @Path("{productId}")
    @Produces("application/json")
    public Response doGet(@PathParam("productId") int id) {
        return Response.ok(getResults("SELECT * FROM product WHERE productID = ?", String.valueOf(id)), MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Consumes("application/json")
    public Response doPost(String data) throws SQLException {
        JsonReader reader = Json.createReader(new StringReader(data));
        JsonObject json = reader.readObject();
        Connection conn = getConnection();
        
        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO `product`(`productID`, `name`, `description`, `quantity`) "
               + "VALUES ("
               +"null, '"
               + json.getString("name") + "', '"
               + json.getString("description") +"', "
               + json.getInt("quantity")
               +");"
        );
        try {
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Post error").build();
        }
        return Response.ok(getResults("SELECT * FROM product ORDER BY productID DESC LIMIT 1"), MediaType.APPLICATION_JSON).build();
    }
   
    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public Response doPut(@PathParam("id") int id, String data) throws SQLException {
        JsonReader reader = Json.createReader(new StringReader(data));
        JsonObject json = reader.readObject();
        Connection conn = getConnection();
        
        PreparedStatement pstmt = conn.prepareStatement("UPDATE `product` SET `name`='"
                +json.getString("name")+"',`description`='"
                +json.getString("description")+"',`quantity`="
                +json.getInt("quantity")+" WHERE `productID`="
                +id
        );
        try {
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Update error").build();
        }
        return Response.ok(getResults("SELECT * FROM product WHERE productID = " + id), MediaType.APPLICATION_JSON).build();
    }
    
    @DELETE
    @Path("{id}")
    public Response doDelete(@PathParam("id") int id) throws SQLException {
        Connection conn = getConnection();
        
        PreparedStatement pstmt = conn.prepareStatement("DELETE FROM `product` WHERE `productID`=" + String.valueOf(id));
        try {
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Delete error").build();
        }
        return Response.ok("", MediaType.APPLICATION_JSON).build();
    }
    
    private String getResults(String query, String... params) {
        String result = "";
        
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            
            ResultSet rs = pstmt.executeQuery();
            StringWriter out = new StringWriter();
            JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
            JsonGenerator gen = factory.createGenerator(out);
            
            gen.writeStartArray();
            while (rs.next()) {
                gen.writeStartObject()
                    .write("productId", rs.getInt("productID"))
                    .write("name", rs.getString("name"))
                    .write("description", rs.getString("description"))
                    .write("quantity", rs.getInt("quantity"))
                    .writeEnd();
            }
            gen.writeEnd();
            gen.close();
            result = out.toString();
            
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    private Connection getConnection() throws SQLException {
        Connection conn = null;
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String jdbc = "jdbc:mysql://localhost/CPD4414";
            conn = DriverManager.getConnection(jdbc, "root", ""); 
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return conn;
    }
}
