/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CPD4414Assign3;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
   
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            
            if (!request.getParameterNames().hasMoreElements()) {
                out.println(getResults("SELECT * FROM product"));
            } else {
                if (request.getParameter("productID") == null) {
                    out.println(getResults("SELECT * FROM product ORDER BY productID DESC LIMIT 1"));
                } else {
                    int id = Integer.parseInt(request.getParameter("productID"));
                    out.println(getResults("SELECT * FROM product WHERE productID = ?", String.valueOf(id)));  
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (keySet.contains("name") && keySet.contains("description") && keySet.contains("quantity")) {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO `product`(`productID`, `name`, `description`, `quantity`) "
                        + "VALUES ("
                        +"null, '"
                        +request.getParameter("name")+"', '"
                        +request.getParameter("description")+"', "
                        +request.getParameter("quantity")
                        +");"
                );
                try {
                    pstmt.executeUpdate();
                    request.setAttribute("productID", 2);
                    request.getParameter("productID");
                    doGet(request, response); //shows updated row
                } catch (SQLException ex) {
                    Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error with inserting values.");
                    response.setStatus(500);
                }
            } else {
                out.println("Error: Not enough data to input");
                response.setStatus(500);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (keySet.contains("productID") && keySet.contains("name") && keySet.contains("description") && keySet.contains("quantity")) {
                PreparedStatement pstmt = conn.prepareStatement("UPDATE `product` SET `name`='"+request.getParameter("name")+"',`description`='"+request.getParameter("description")+"',`quantity`="+request.getParameter("quantity")+" WHERE `productID`="+request.getParameter("productID"));
                try {
                    pstmt.executeUpdate();
                    doGet(request, response); //shows updated row
                } catch (SQLException ex) {
                    Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error updating values.");
                    response.setStatus(500);
                }
            } else {
                out.println("Error: Not enough data to update");
                response.setStatus(500);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (keySet.contains("productID")) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM `product` WHERE `productID`=" + request.getParameter("productID"));
                try {
                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error deleting entry.");
                    response.setStatus(500);
                }
            } else {
                out.println("Error: Not enough data to delete");
                response.setStatus(500);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String getResults(String query, String... params) {
        StringBuilder results = new StringBuilder();
        String result = "a";
        
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            
            ResultSet rs = pstmt.executeQuery();
            JSONObject resultObj = new JSONObject();
            JSONArray productArr = new JSONArray();
            
            while (rs.next()) {
                Map productMap = new LinkedHashMap();
                productMap.put("productID", rs.getInt("productID"));
                productMap.put("name", rs.getString("name"));
                productMap.put("description", rs.getString("description"));
                productMap.put("quantity", rs.getInt("quantity"));
                productArr.add(productMap);
            }
            resultObj.put("product", productArr);
            result = resultObj.toString();
            
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
